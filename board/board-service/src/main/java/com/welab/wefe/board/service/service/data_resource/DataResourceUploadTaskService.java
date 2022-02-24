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

package com.welab.wefe.board.service.service.data_resource;

import com.welab.wefe.board.service.api.data_resource.upload_task.DataResourceUploadTaskQueryApi;
import com.welab.wefe.board.service.database.entity.data_resource.DataResourceMysqlModel;
import com.welab.wefe.board.service.database.entity.data_resource.DataResourceUploadTaskMysqlModel;
import com.welab.wefe.board.service.database.repository.data_resource.DataResourceRepository;
import com.welab.wefe.board.service.database.repository.data_resource.DataResourceUploadTaskRepository;
import com.welab.wefe.board.service.dto.base.PagingOutput;
import com.welab.wefe.board.service.dto.entity.data_resource.output.DataResourceUploadTaskOutputModel;
import com.welab.wefe.board.service.dto.vo.data_resource.AbstractDataResourceUpdateInputModel;
import com.welab.wefe.board.service.service.AbstractService;
import com.welab.wefe.common.Convert;
import com.welab.wefe.common.TimeSpan;
import com.welab.wefe.common.data.mysql.Where;
import com.welab.wefe.common.util.DateUtil;
import com.welab.wefe.common.wefe.enums.DataResourceType;
import com.welab.wefe.common.wefe.enums.DataResourceUploadStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.function.Consumer;

/**
 * @author lonnie
 */
@Service
public class DataResourceUploadTaskService extends AbstractService {

    private static final Object LOCKER = new Object();
    @Autowired
    protected DataResourceRepository dataResourceRepository;
    @Autowired
    private DataResourceUploadTaskRepository dataResourceUploadTaskRepository;

    /**
     * 创建一个新的上传任务
     */
    public DataResourceUploadTaskMysqlModel newTask(DataResourceType dataResourceType, AbstractDataResourceUpdateInputModel input) {

        DataResourceUploadTaskMysqlModel task = new DataResourceUploadTaskMysqlModel();
        task.setDataResourceName(input.getName());
        task.setProgressRatio(0);
        task.setDataResourceId(new DataResourceMysqlModel().getId());
        task.setStatus(DataResourceUploadStatus.uploading);
        task.setDataResourceType(dataResourceType);
        dataResourceUploadTaskRepository.save(task);
        return task;
    }

    public DataResourceUploadTaskMysqlModel findByDataResourceId(String dataResource) {
        Specification<DataResourceUploadTaskMysqlModel> where = Where
                .create()
                .equal("dataResourceId", dataResource)
                .build(DataResourceUploadTaskMysqlModel.class);

        return dataResourceUploadTaskRepository.findOne(where).orElse(null);
    }

    /**
     * 开始计算进度之前，更新message。
     */
    public void updateMessageBeforeStart(String dataResourceId, String message) {
        updateProgress(dataResourceId, 0, 0, 0, message);
    }

    public void updateProgress(String dataResourceId, long totalDataRowCount, long completedDataCount, long invalidDataCount) {
        updateProgress(dataResourceId, totalDataRowCount, completedDataCount, invalidDataCount, null);
    }

    /**
     * Update upload progress
     */
    public void updateProgress(String dataResourceId, long totalDataRowCount, long completedDataCount, long invalidDataCount, String message) {
        // Since storing data sets into storage is a concurrent operation, onerror, updateprogress, complete and other operations may occur simultaneously to update the same task.
        // In order to avoid disordered update sequence, lock operation is required here.
        synchronized (LOCKER) {
            DataResourceUploadTaskMysqlModel task = findByDataResourceId(dataResourceId);

            // 已经结束的任务不再更新进度
            if (task.getStatus() != DataResourceUploadStatus.uploading) {
                return;
            }

            int progress = 0;
            if (totalDataRowCount > 0) {
                // Calculate progress
                progress = Convert.toInt(completedDataCount * 100L / totalDataRowCount);
            }

            if (completedDataCount > 0) {
                // When the early reading speed is slow, force progress++
                if (task.getProgressRatio() < 5
                        && completedDataCount < 10000
                        && completedDataCount > task.getCompletedDataCount()
                        && progress <= task.getProgressRatio()
                ) {
                    progress = task.getProgressRatio() + 1;
                }
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
                long spend = System.currentTimeMillis() - task.getCreatedTime().getTime();
                estimateTime = spend / progress * (100 - progress);
            }

            task.setTotalDataCount(totalDataRowCount);
            task.setInvalidDataCount(invalidDataCount);
            task.setCompletedDataCount(completedDataCount);
            task.setEstimateRemainingTime(estimateTime);
            task.setProgressRatio(progress);
            task.setErrorMessage(message);
            task.setUpdatedTime(new Date());

            dataResourceUploadTaskRepository.save(task);

            LOG.info("资源上传任务进度：" + task.getProgressRatio() + " , " + completedDataCount + "/" + totalDataRowCount);
        }
    }

    /**
     * Upload complete
     */
    public void complete(String dataResourceId) {
        synchronized (LOCKER) {
            DataResourceUploadTaskMysqlModel task = findByDataResourceId(dataResourceId);
            task.setCompletedDataCount(task.getTotalDataCount());
            task.setEstimateRemainingTime(0);
            task.setProgressRatio(100);
            task.setUpdatedTime(new Date());
            task.setStatus(DataResourceUploadStatus.completed);
            task.setErrorMessage("已完成");
            dataResourceUploadTaskRepository.save(task);
        }
    }

    public DataResourceUploadTaskMysqlModel findById(String id) {
        return dataResourceUploadTaskRepository.findById(id).orElse(null);
    }

    public void update(DataResourceUploadTaskMysqlModel dataSetTask, Consumer<DataResourceUploadTaskMysqlModel> func) {
        if (dataSetTask == null) {
            return;
        }

        func.accept(dataSetTask);
        dataSetTask.setUpdatedTime(new Date());
        dataResourceUploadTaskRepository.save(dataSetTask);
    }

    public PagingOutput<DataResourceUploadTaskOutputModel> query(DataResourceUploadTaskQueryApi.Input input) {
        Specification<DataResourceUploadTaskMysqlModel> where = Where
                .create()
                .greaterThan("updatedTime", DateUtil.getDate(System.currentTimeMillis() - TimeSpan.fromMinute(10).toMs()))
                .build(DataResourceUploadTaskMysqlModel.class);

        return dataResourceUploadTaskRepository.paging(where, input, DataResourceUploadTaskOutputModel.class);
    }

    /**
     * An exception occurred while saving the dataset
     */
    public void onError(String dataResourceId, Exception e) {
        synchronized (LOCKER) {
            DataResourceUploadTaskMysqlModel task = findByDataResourceId(dataResourceId);
            if (task == null) {
                return;
            }

            task.setErrorMessage(e.getMessage());
            task.setUpdatedTime(new Date());
            task.setStatus(DataResourceUploadStatus.failed);
            dataResourceUploadTaskRepository.save(task);
        }
    }
}
