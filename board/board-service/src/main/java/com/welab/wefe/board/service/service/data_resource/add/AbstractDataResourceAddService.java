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
package com.welab.wefe.board.service.service.data_resource.add;

import com.welab.wefe.board.service.base.file_system.WeFeFileSystem;
import com.welab.wefe.board.service.database.entity.data_resource.BloomFilterMysqlModel;
import com.welab.wefe.board.service.database.entity.data_resource.DataResourceMysqlModel;
import com.welab.wefe.board.service.database.entity.data_resource.DataResourceUploadTaskMysqlModel;
import com.welab.wefe.board.service.database.entity.data_resource.TableDataSetMysqlModel;
import com.welab.wefe.board.service.dto.vo.data_resource.AbstractDataResourceUpdateInputModel;
import com.welab.wefe.board.service.dto.vo.data_resource.DataResourceAddOutputModel;
import com.welab.wefe.board.service.service.AbstractService;
import com.welab.wefe.board.service.service.CacheObjects;
import com.welab.wefe.board.service.service.DataSetStorageService;
import com.welab.wefe.board.service.service.ServiceCheckService;
import com.welab.wefe.board.service.service.checkpoint.StorageCheckpoint;
import com.welab.wefe.board.service.service.data_resource.DataResourceService;
import com.welab.wefe.board.service.service.data_resource.DataResourceUploadTaskService;
import com.welab.wefe.common.CommonThreadPool;
import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.util.FileUtil;
import com.welab.wefe.common.wefe.checkpoint.dto.ServiceCheckPointOutput;
import com.welab.wefe.common.wefe.enums.DataResourceStorageType;
import com.welab.wefe.common.wefe.enums.DataResourceType;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author zane
 * @date 2021/12/2
 */
public abstract class AbstractDataResourceAddService extends AbstractService {
    @Autowired
    protected DataResourceUploadTaskService dataResourceUploadTaskService;
    @Autowired
    private DataResourceService dataResourceService;
    @Autowired
    protected DataSetStorageService dataSetStorageService;
    @Autowired
    private ServiceCheckService serviceCheckService;

    // region abstract method

    protected abstract void doAdd(AbstractDataResourceUpdateInputModel in, DataResourceUploadTaskMysqlModel task, DataResourceMysqlModel m) throws StatusCodeWithException;

    protected abstract Class<? extends DataResourceMysqlModel> getMysqlModelClass();

    protected abstract DataResourceType getDataResourceType();

    // endregion


    /**
     * 添加资源的公共方法
     *
     * @return 资源Id
     */
    public DataResourceAddOutputModel add(AbstractDataResourceUpdateInputModel input) throws StatusCodeWithException {
        DataResourceType dataResourceType = getDataResourceType();
        DataResourceUploadTaskMysqlModel task = dataResourceUploadTaskService.newTask(dataResourceType, input);

        DataResourceMysqlModel model = new ModelMapper().map(input, getMysqlModelClass());
        model.setId(task.getDataResourceId());
        model.setCreatedBy(input);
        model.setDataResourceType(dataResourceType);
        model.setTags(dataResourceService.standardizeTags(input.getTags()));
        dataResourceService.handlePublicMemberList(model);
        checkAndSetStorageLocation(model);

        // 异步执行资源保存动作
        CommonThreadPool.run(() -> {
            try {
                doAdd(input, task, model);
                unionService.upsertDataResource(model);
                dataResourceUploadTaskService.complete(task.getDataResourceId());
            } catch (Exception e) {
                LOG.error(e.getClass().getSimpleName() + " " + e.getMessage(), e);
                dataResourceUploadTaskService.onError(model.getId(), e);
            }
        });

        // Refresh the data set tag list
        CacheObjects.refreshDataResourceTags(model.getDataResourceType());

        return new DataResourceAddOutputModel(task.getDataResourceId(), task.getId());
    }

    @Autowired
    private StorageCheckpoint storageCheckpoint;

    /**
     * 检查并设置资源的存储位置
     */
    private void checkAndSetStorageLocation(DataResourceMysqlModel model) throws StatusCodeWithException {
        Class<? extends DataResourceMysqlModel> mysqlModelClass = getMysqlModelClass();
        // table data set
        if (mysqlModelClass == TableDataSetMysqlModel.class) {
            ServiceCheckPointOutput availableInfo = storageCheckpoint.check();
            if (!availableInfo.isSuccess()) {
                StatusCode
                        .DATABASE_LOST
                        .throwException("storage 服务访问失败：" + availableInfo.getMessage() + "，请检服务是否正常：" + config.getDbType());
            }

            model.setStorageType(DataResourceStorageType.StorageService);
            model.setStorageNamespace(DataSetStorageService.DATABASE_NAME);
            model.setStorageResourceName(dataSetStorageService.createRawDataSetTableName(model.getId()));
        }
        // image data set & bloom filter
        else {
            model.setStorageType(DataResourceStorageType.LocalFileSystem);
            model.setStorageNamespace(
                    WeFeFileSystem
                            .getFileDir(model.getDataResourceType())
                            .resolve(model.getId())
                            .toAbsolutePath()
                            .toString()
            );

            // 生成的过滤器文件统一文件名
            if (mysqlModelClass == BloomFilterMysqlModel.class) {
                model.setStorageResourceName("bloom_filter.data");
            }

            FileUtil.createDir(model.getStorageNamespace());
        }

    }

}
