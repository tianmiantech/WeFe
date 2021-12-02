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

package com.welab.wefe.board.service.service.data_resource;

import com.welab.wefe.board.service.api.data_resource.upload_progress.DataResourceUploadTaskQueryApi;
import com.welab.wefe.board.service.database.entity.data_resource.DataResourceMysqlModel;
import com.welab.wefe.board.service.database.entity.data_resource.DataResourceUploadTaskMysqlModel;
import com.welab.wefe.board.service.database.repository.data_resource.DataResourceRepository;
import com.welab.wefe.board.service.database.repository.data_resource.DataResourceUploadTaskRepository;
import com.welab.wefe.board.service.dto.base.PagingOutput;
import com.welab.wefe.board.service.dto.entity.DataSetTaskOutputModel;
import com.welab.wefe.board.service.dto.vo.MemberServiceStatusOutput;
import com.welab.wefe.board.service.dto.vo.data_resource.AbstractDataResourceUpdateInputModel;
import com.welab.wefe.board.service.service.AbstractService;
import com.welab.wefe.board.service.service.ServiceCheckService;
import com.welab.wefe.board.service.service.data_resource.table_data_set.TableDataSetAddService;
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
public class DataResourceUploadTaskService extends AbstractService {

    private static final Object LOCKER = new Object();
    @Autowired
    private TableDataSetAddService tableDataSetAddService;
    @Autowired
    private ServiceCheckService serviceCheckService;
    @Autowired
    protected DataResourceRepository dataResourceRepository;
    @Autowired
    private DataResourceUploadTaskRepository dataResourceUploadTaskRepository;

    public DataResourceUploadTaskMysqlModel add(AbstractDataResourceUpdateInputModel input) throws StatusCodeWithException, IOException {

        // Check database connection
        MemberServiceStatusOutput storageServiceStatus = serviceCheckService.checkStorageServiceStatus(true);
        if (!storageServiceStatus.isSuccess()) {
            throw new StatusCodeWithException(StatusCode.DATABASE_LOST, config.getDbType() + "连接失败，请检服务是否正常。");
        }

        DataResourceUploadTaskMysqlModel progress = new DataResourceUploadTaskMysqlModel();
        progress.setDataResourceName(input.getName());
        progress.setProgressRatio(0);
        progress.setDataResourceId(new DataResourceMysqlModel().getId());
        dataResourceUploadTaskRepository.save(progress);

        tableDataSetAddService.add(input, progress, CurrentAccount.get());

        return progress;
    }

    public DataResourceUploadTaskMysqlModel findByDataResourceId(String dataResource) {
        Specification<DataResourceUploadTaskMysqlModel> where = Where
                .create()
                .equal("dataResourceId", dataResource)
                .build(DataResourceUploadTaskMysqlModel.class);

        return dataResourceUploadTaskRepository.findOne(where).orElse(null);
    }

    /**
     * Update upload progress
     */
    public void updateProgress(String dataSetId, long totalDataRowCount, long readedDataRows, long repeatDataCount) {
        // Since storing data sets into storage is a concurrent operation, onerror, updateprogress, complete and other operations may occur simultaneously to update the same task.
        // In order to avoid disordered update sequence, lock operation is required here.
        synchronized (LOCKER) {
            DataResourceUploadTaskMysqlModel dataSetTask = findByDataResourceId(dataSetId);

            // Calculate progress
            int progress = Convert.toInt(readedDataRows * 100L / totalDataRowCount);

            // When the early reading speed is slow, force progress++
            if (dataSetTask.getProgressRatio() < 5
                    && readedDataRows < 10000
                    && readedDataRows > dataSetTask.getCompletedDataCount()
                    && progress <= dataSetTask.getProgressRatio()
            ) {
                progress = dataSetTask.getProgressRatio() + 1;
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

            dataSetTask.setInvalidDataCount(repeatDataCount);
            dataSetTask.setCompletedDataCount(readedDataRows);
            dataSetTask.setEstimateRemainingTime(estimateTime);
            dataSetTask.setProgressRatio(progress);
            dataSetTask.setUpdatedTime(new Date());

            dataResourceUploadTaskRepository.save(dataSetTask);

            LOG.info("数据集任务进度：" + dataSetTask.getProgressRatio() + " , " + readedDataRows + "/" + totalDataRowCount);
        }
    }

    /**
     * Upload complete
     */
    public void complete(String dataSetId) {
        synchronized (LOCKER) {
            DataResourceUploadTaskMysqlModel dataSetTask = findByDataResourceId(dataSetId);
            dataSetTask.setCompletedDataCount(dataSetTask.getTotalDataCount());
            dataSetTask.setEstimateRemainingTime(0);
            dataSetTask.setProgressRatio(100);
            dataSetTask.setUpdatedTime(new Date());

            dataResourceUploadTaskRepository.save(dataSetTask);
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

    public PagingOutput<DataSetTaskOutputModel> query(DataResourceUploadTaskQueryApi.Input input) {
        Specification<DataResourceUploadTaskMysqlModel> where = Where
                .create()
                .greaterThan("progress", 0)
                .lessThan("progress", 100)
                .greaterThan("updatedTime", DateUtil.getDate(System.currentTimeMillis() - TimeSpan.fromMinute(10).toMs()))
                .build(DataResourceUploadTaskMysqlModel.class);

        return dataResourceUploadTaskRepository.paging(where, input, DataSetTaskOutputModel.class);
    }

    /**
     * An exception occurred while saving the dataset
     */
    public void onError(String dataSetId, Exception e) {
        synchronized (LOCKER) {
            DataResourceUploadTaskMysqlModel dataSetTask = findByDataResourceId(dataSetId);
            if (dataSetTask == null) {
                return;
            }

            dataSetTask = findByDataResourceId(dataSetTask.getDataResourceId());
            dataSetTask.setErrorMessage(e.getMessage());
            dataSetTask.setUpdatedTime(new Date());

            dataResourceUploadTaskRepository.save(dataSetTask);
        }
    }
}
