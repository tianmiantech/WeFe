/**
 * Copyright 2021 Tianmian Tech. All Rights Reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.welab.wefe.board.service.service.fusion.bloomfilter;

import com.welab.wefe.board.service.api.dataset_task.QueryApi;
import com.welab.wefe.board.service.constant.Config;
import com.welab.wefe.board.service.database.entity.fusion.bloomfilter.BloomFilterMySqlModel;
import com.welab.wefe.board.service.database.entity.fusion.bloomfilter.BloomFilterTaskMysqlModel;
import com.welab.wefe.board.service.database.repository.fusion.BloomFilterRepository;
import com.welab.wefe.board.service.database.repository.fusion.BloomFilterTaskRepository;
import com.welab.wefe.board.service.dto.base.PagingOutput;
import com.welab.wefe.board.service.dto.fusion.BloomFilterTaskOutputModel;
import com.welab.wefe.board.service.dto.vo.BloomfilterAddInputModel;
import com.welab.wefe.board.service.dto.vo.MemberServiceStatusOutput;
import com.welab.wefe.board.service.service.AbstractService;
import com.welab.wefe.board.service.service.ServiceCheckService;
import com.welab.wefe.common.Convert;
import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.TimeSpan;
import com.welab.wefe.common.data.mysql.Where;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.util.DateUtil;
import com.welab.wefe.common.web.CurrentAccount;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Date;
import java.util.function.Consumer;


/**
 * @author jacky.jiang
 */
@Service
public class BloomfilterTaskService extends AbstractService {

    private static final Object LOCKER = new Object();
    @Autowired
    private Config config;
    @Autowired
    private BloomfilterAddService bloomfilterAddService;
    @Autowired
    private ServiceCheckService serviceCheckService;
    @Autowired
    protected BloomFilterRepository bloomfilterRepository;
    @Autowired
    private BloomFilterTaskRepository bloomfilterTaskRepository;

    public BloomFilterTaskMysqlModel add(BloomfilterAddInputModel input) throws StatusCodeWithException, IOException {

        // Check database connection
        MemberServiceStatusOutput storageServiceStatus = serviceCheckService.checkStorageServiceStatus(true);
        if (!storageServiceStatus.isSuccess()) {
            throw new StatusCodeWithException(StatusCode.DATABASE_LOST, config.getDbType() + "连接失败，请检服务是否正常。");
        }

        if (bloomfilterRepository.countByName(input.getName()) > 0) {
            throw new StatusCodeWithException("此过滤器名称已存在，请换一个过滤器名称", StatusCode.PARAMETER_VALUE_INVALID);
        }

        BloomFilterTaskMysqlModel bloomfilterTask = new BloomFilterTaskMysqlModel();
        bloomfilterTask.setBloomfilterName(input.getName());
        bloomfilterTask.setProgress(0);
        bloomfilterTask.setBloomfilterId(new BloomFilterMySqlModel().getId());
        bloomfilterTaskRepository.save(bloomfilterTask);
        bloomfilterAddService.add(input, bloomfilterTask, CurrentAccount.get());

        return bloomfilterTask;
    }

    public BloomFilterTaskMysqlModel findByBloomfilterId(String bloomfilterId) {
        Specification<BloomFilterTaskMysqlModel> where = Where
                .create()
                .equal("bloomfilterId", bloomfilterId)
                .build(BloomFilterTaskMysqlModel.class);

        return bloomfilterTaskRepository.findOne(where).orElse(null);
    }

    /**
     * Update upload progress
     */
    public void updateProgress(String bloomfilterId, long totalDataRowCount, long readedDataRows, long repeatDataCount) {
        // Since storing bloomfilters into storage is a concurrent operation, onerror, updateprogress, complete and other operations may occur simultaneously to update the same task.
        // In order to avoid disordered update sequence, lock operation is required here.
        synchronized (LOCKER) {
            BloomFilterTaskMysqlModel bloomfilterTask = findByBloomfilterId(bloomfilterId);

            // Calculate progress
            int progress = Convert.toInt(readedDataRows * 100L / totalDataRowCount);

            // When the early reading speed is slow, force progress++
            if (bloomfilterTask.getProgress() < 5
                    && readedDataRows < 10000
                    && readedDataRows > bloomfilterTask.getAddedRowCount()
                    && progress <= bloomfilterTask.getProgress()
            ) {
                progress = bloomfilterTask.getProgress() + 1;
            }

            // Avoid dividing by 0
            if (progress == 0) {
                progress = 1;
            }

            // Because  the bloomfilter has not been updated yet. The progress cannot be set to 100 temporarily, otherwise the front end will jump in advance.
            if (progress == 100) {
                progress = 99;
            }

            // Calculate estimated time
            long estimateTime = 0;
            if (progress < 100) {
                long spend = System.currentTimeMillis() - bloomfilterTask.getCreatedTime().getTime();
                estimateTime = spend / progress * (100 - progress);
            }

            bloomfilterTask.setRepeatIdRowCount(repeatDataCount);
            bloomfilterTask.setAddedRowCount(readedDataRows);
            bloomfilterTask.setEstimateTime(estimateTime);
            bloomfilterTask.setProgress(progress);
            bloomfilterTask.setUpdatedTime(new Date());

            bloomfilterTaskRepository.save(bloomfilterTask);

            LOG.info("过滤器任务进度：" + bloomfilterTask.getProgress() + " , " + readedDataRows + "/" + totalDataRowCount);
        }
    }

    /**
     * Upload complete
     */
    public void complete(String bloomfilterId) {
        synchronized (LOCKER) {
            BloomFilterTaskMysqlModel bloomfilterTask = findByBloomfilterId(bloomfilterId);
            bloomfilterTask.setAddedRowCount(bloomfilterTask.getTotalRowCount());
            bloomfilterTask.setEstimateTime(0);
            bloomfilterTask.setProgress(100);
            bloomfilterTask.setUpdatedTime(new Date());

            bloomfilterTaskRepository.save(bloomfilterTask);
        }
    }

    public BloomFilterTaskMysqlModel findById(String id) {
        return bloomfilterTaskRepository.findById(id).orElse(null);
    }

    public void update(BloomFilterTaskMysqlModel bloomfilterTask, Consumer<BloomFilterTaskMysqlModel> func) {
        if (bloomfilterTask == null) {
            return;
        }

        func.accept(bloomfilterTask);
        bloomfilterTask.setUpdatedTime(new Date());
        bloomfilterTaskRepository.save(bloomfilterTask);
    }

    public PagingOutput<BloomFilterTaskOutputModel> query(QueryApi.Input input) {
        Specification<BloomFilterTaskMysqlModel> where = Where
                .create()
                .greaterThan("progress", 0)
                .lessThan("progress", 100)
                .greaterThan("updatedTime", DateUtil.getDate(System.currentTimeMillis() - TimeSpan.fromMinute(10).toMs()))
                .build(BloomFilterTaskMysqlModel.class);

        return bloomfilterTaskRepository.paging(where, input, BloomFilterTaskOutputModel.class);
    }

    /**
     * An exception occurred while saving the bloomfilter
     */
    public void onError(String bloomfilterId, Exception e) {
        synchronized (LOCKER) {
            BloomFilterTaskMysqlModel bloomfilterTask = findByBloomfilterId(bloomfilterId);
            if (bloomfilterTask == null) {
                return;
            }

            bloomfilterTask = findByBloomfilterId(bloomfilterTask.getBloomfilterId());
            bloomfilterTask.setErrorMessage(e.getMessage());
            bloomfilterTask.setUpdatedTime(new Date());

            bloomfilterTaskRepository.save(bloomfilterTask);
        }
    }
}
