/*
 * Copyright 2021 Tianmian Tech. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.welab.wefe.board.service.service.data_resource.bloom_filter;


import com.welab.wefe.board.service.database.entity.fusion.bloomfilter.BloomFilterTaskMysqlModel;
import com.welab.wefe.board.service.database.repository.data_resource.BloomFilterRepository;
import com.welab.wefe.board.service.database.repository.fusion.BloomFilterTaskRepository;
import com.welab.wefe.board.service.service.AbstractService;
import com.welab.wefe.common.Convert;
import com.welab.wefe.common.data.mysql.Where;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.function.Consumer;


/**
 * @author jacky.jiang
 */
@Service
public class BloomFilterTaskService extends AbstractService {

    private static final Object LOCKER = new Object();
    @Autowired
    protected BloomFilterRepository bloomfilterRepository;
    @Autowired
    private BloomFilterTaskRepository bloomfilterTaskRepository;


    public BloomFilterTaskMysqlModel findByBloomfilterId(String bloomFilterId) {
        Specification<BloomFilterTaskMysqlModel> where = Where
                .create()
                .equal("bloomFilterId", bloomFilterId)
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

            // Because  the bloom_filter has not been updated yet. The progress cannot be set to 100 temporarily, otherwise the front end will jump in advance.
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
    public void complete(String bloomFilterId) {
        synchronized (LOCKER) {
            BloomFilterTaskMysqlModel bloomfilterTask = findByBloomfilterId(bloomFilterId);
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

//    public PagingOutput<BloomFilterTaskOutputModel> query(QueryApi.Input input) {
//        Specification<BloomFilterTaskMysqlModel> where = Where
//                .create()
//                .greaterThan("progress", 0)
//                .lessThan("progress", 100)
//                .greaterThan("updatedTime", DateUtil.getDate(System.currentTimeMillis() - TimeSpan.fromMinute(10).toMs()))
//                .build(BloomFilterTaskMysqlModel.class);
//
//        return bloomfilterTaskRepository.paging(where, input, BloomFilterTaskOutputModel.class);
//    }

    /**
     * An exception occurred while saving the bloom_filter
     */
    public void onError(String bloomfilterId, Exception e) {
        synchronized (LOCKER) {
            BloomFilterTaskMysqlModel bloomfilterTask = findByBloomfilterId(bloomfilterId);
            if (bloomfilterTask == null) {
                return;
            }

            bloomfilterTask = findByBloomfilterId(bloomfilterTask.getBloomFilterId());
            bloomfilterTask.setErrorMessage(e.getMessage());
            bloomfilterTask.setUpdatedTime(new Date());

            bloomfilterTaskRepository.save(bloomfilterTask);
        }
    }
}
