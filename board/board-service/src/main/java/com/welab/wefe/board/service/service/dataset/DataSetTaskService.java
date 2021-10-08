/**
 * Copyright 2021 The WeFe Authors. All Rights Reserved.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.welab.wefe.board.service.service.dataset;

import com.welab.wefe.board.service.api.dataset_task.QueryApi;
import com.welab.wefe.board.service.constant.Config;
import com.welab.wefe.board.service.database.entity.data_set.DataSetMysqlModel;
import com.welab.wefe.board.service.database.entity.data_set.DataSetTaskMysqlModel;
import com.welab.wefe.board.service.database.repository.DataSetRepository;
import com.welab.wefe.board.service.database.repository.DataSetTaskRepository;
import com.welab.wefe.board.service.dto.base.PagingOutput;
import com.welab.wefe.board.service.dto.entity.DataSetTaskOutputModel;
import com.welab.wefe.board.service.dto.vo.DataSetAddInputModel;
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
 * @author lonnie
 */
@Service
public class DataSetTaskService extends AbstractService {

    private static final Object LOCKER = new Object();
    @Autowired
    private Config config;
    @Autowired
    private DataSetAddService dataSetAddService;
    @Autowired
    private ServiceCheckService serviceCheckService;
    @Autowired
    protected DataSetRepository dataSetRepository;
    @Autowired
    private DataSetTaskRepository dataSetTaskRepository;

    public DataSetTaskMysqlModel add(DataSetAddInputModel input) throws StatusCodeWithException, IOException {

        // Check database connection
        MemberServiceStatusOutput storageServiceStatus = serviceCheckService.checkStorageServiceStatus(true);
        if (!storageServiceStatus.isSuccess()) {
            throw new StatusCodeWithException(StatusCode.DATABASE_LOST, config.getDbType() + "连接失败，请检服务是否正常。");
        }

        if (dataSetRepository.countByName(input.getName()) > 0) {
            throw new StatusCodeWithException("此数据集名称已存在，请换一个数据集名称", StatusCode.PARAMETER_VALUE_INVALID);
        }

        DataSetTaskMysqlModel dataSetTask = new DataSetTaskMysqlModel();
        dataSetTask.setDataSetName(input.getName());
        dataSetTask.setProgress(0);
        dataSetTask.setDataSetId(new DataSetMysqlModel().getId());
        dataSetTaskRepository.save(dataSetTask);

        dataSetAddService.add(input, dataSetTask, CurrentAccount.get());

        return dataSetTask;
    }

    public DataSetTaskMysqlModel findByDataSetId(String dataSetId) {
        Specification<DataSetTaskMysqlModel> where = Where
                .create()
                .equal("dataSetId", dataSetId)
                .build(DataSetTaskMysqlModel.class);

        return dataSetTaskRepository.findOne(where).orElse(null);
    }

    /**
     * Update upload progress
     */
    public void updateProgress(String dataSetId, long totalDataRowCount, long readedDataRows, long repeatDataCount) {
        // Since storing data sets into storage is a concurrent operation, onerror, updateprogress, complete and other operations may occur simultaneously to update the same task.
        // In order to avoid disordered update sequence, lock operation is required here.
        synchronized (LOCKER) {
            DataSetTaskMysqlModel dataSetTask = findByDataSetId(dataSetId);

            // Calculate progress
            int progress = Convert.toInt(readedDataRows * 100L / totalDataRowCount);

            // When the early reading speed is slow, force progress++
            if (dataSetTask.getProgress() < 5
                    && readedDataRows < 10000
                    && readedDataRows > dataSetTask.getAddedRowCount()
                    && progress <= dataSetTask.getProgress()
            ) {
                progress = dataSetTask.getProgress() + 1;
            }

            // Avoid dividing by 0
            if (progress == 0) {
                progress = 1;
            }

            // Because  the data_set has not been updated yet. The progress cannot be set to 100 temporarily, otherwise the front end will jump in advance.
            if (progress == 100) {
                progress = 99;
            }

            // Calculate estimated time
            long estimateTime = 0;
            if (progress < 100) {
                long spend = System.currentTimeMillis() - dataSetTask.getCreatedTime().getTime();
                estimateTime = spend / progress * (100 - progress);
            }

            dataSetTask.setRepeatIdRowCount(repeatDataCount);
            dataSetTask.setAddedRowCount(readedDataRows);
            dataSetTask.setEstimateTime(estimateTime);
            dataSetTask.setProgress(progress);
            dataSetTask.setUpdatedTime(new Date());

            dataSetTaskRepository.save(dataSetTask);

            LOG.info("数据集任务进度：" + dataSetTask.getProgress() + " , " + readedDataRows + "/" + totalDataRowCount);
        }
    }

    /**
     * Upload complete
     */
    public void complete(String dataSetId) {
        synchronized (LOCKER) {
            DataSetTaskMysqlModel dataSetTask = findByDataSetId(dataSetId);
            dataSetTask.setAddedRowCount(dataSetTask.getTotalRowCount());
            dataSetTask.setEstimateTime(0);
            dataSetTask.setProgress(100);
            dataSetTask.setUpdatedTime(new Date());

            dataSetTaskRepository.save(dataSetTask);
        }
    }

    public DataSetTaskMysqlModel findById(String id) {
        return dataSetTaskRepository.findById(id).orElse(null);
    }

    public void update(DataSetTaskMysqlModel dataSetTask, Consumer<DataSetTaskMysqlModel> func) {
        if (dataSetTask == null) {
            return;
        }

        func.accept(dataSetTask);
        dataSetTask.setUpdatedTime(new Date());
        dataSetTaskRepository.save(dataSetTask);
    }

    public PagingOutput<DataSetTaskOutputModel> query(QueryApi.Input input) {
        Specification<DataSetTaskMysqlModel> where = Where
                .create()
                .greaterThan("progress", 0)
                .lessThan("progress", 100)
                .greaterThan("updatedTime", DateUtil.getDate(System.currentTimeMillis() - TimeSpan.fromMinute(10).toMs()))
                .build(DataSetTaskMysqlModel.class);

        return dataSetTaskRepository.paging(where, input, DataSetTaskOutputModel.class);
    }

    /**
     * An exception occurred while saving the dataset
     */
    public void onError(String dataSetId, Exception e) {
        synchronized (LOCKER) {
            DataSetTaskMysqlModel dataSetTask = findByDataSetId(dataSetId);
            if (dataSetTask == null) {
                return;
            }

            dataSetTask = findByDataSetId(dataSetTask.getDataSetId());
            dataSetTask.setErrorMessage(e.getMessage());
            dataSetTask.setUpdatedTime(new Date());

            dataSetTaskRepository.save(dataSetTask);
        }
    }
}
