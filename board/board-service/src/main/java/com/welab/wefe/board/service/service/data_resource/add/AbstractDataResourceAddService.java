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
package com.welab.wefe.board.service.service.data_resource.add;

import com.welab.wefe.board.service.database.entity.data_resource.DataResourceMysqlModel;
import com.welab.wefe.board.service.database.entity.data_resource.DataResourceUploadTaskMysqlModel;
import com.welab.wefe.board.service.database.entity.data_resource.TableDataSetMysqlModel;
import com.welab.wefe.board.service.dto.vo.MemberServiceStatusOutput;
import com.welab.wefe.board.service.dto.vo.data_resource.AbstractDataResourceUpdateInputModel;
import com.welab.wefe.board.service.dto.vo.data_resource.DataResourceAddOutputModel;
import com.welab.wefe.board.service.service.AbstractService;
import com.welab.wefe.board.service.service.DataSetStorageService;
import com.welab.wefe.board.service.service.ServiceCheckService;
import com.welab.wefe.board.service.service.data_resource.DataResourceService;
import com.welab.wefe.board.service.service.data_resource.DataResourceUploadTaskService;
import com.welab.wefe.common.CommonThreadPool;
import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.enums.DataResourceType;
import com.welab.wefe.common.enums.DataSetStorageType;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.util.FileUtil;
import com.welab.wefe.common.util.StringUtil;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;

import java.nio.file.Paths;

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
        DataResourceUploadTaskMysqlModel task = dataResourceUploadTaskService.newTask(input);

        DataResourceMysqlModel model = new ModelMapper().map(input, getMysqlModelClass());
        model.setStorageResourceName("bloom_filter.data");
        model.setId(task.getDataResourceId());
        model.setCreatedBy(input);
        model.setDataResourceType(getDataResourceType());
        model.setTags(dataResourceService.standardizeTags(input.getTags()));
        dataResourceService.handlePublicMemberList(model);
        checkAndSetStorageLocation(model);

        // 异步执行资源保存动作
        CommonThreadPool.run(() -> {
            try {
                doAdd(input, task, model);
                unionService.uploadDataResource(model);
                dataResourceUploadTaskService.complete(task.getDataResourceId());
            } catch (StatusCodeWithException e) {
                LOG.error(e.getClass().getSimpleName() + " " + e.getMessage(), e);
                dataResourceUploadTaskService.onError(model.getId(), e);
            }
        });

        return new DataResourceAddOutputModel(task.getDataResourceId(), task.getId());
    }

    /**
     * 检查并设置资源的存储位置
     */
    private void checkAndSetStorageLocation(DataResourceMysqlModel model) throws StatusCodeWithException {
        Class<? extends DataResourceMysqlModel> mysqlModelClass = getMysqlModelClass();
        // table data set
        if (mysqlModelClass == TableDataSetMysqlModel.class) {
            MemberServiceStatusOutput storageServiceStatus = serviceCheckService.checkStorageServiceStatus(true);
            if (!storageServiceStatus.isSuccess()) {
                throw new StatusCodeWithException(StatusCode.DATABASE_LOST, config.getDbType() + "连接失败，请检服务是否正常。");
            }


            model.setStorageType(DataSetStorageType.StorageService);
            model.setStorageNamespace(DataSetStorageService.DATABASE_NAME);
            model.setStorageResourceName(dataSetStorageService.createRawDataSetTableName(model.getId()));
        }
        // image data set & bloom filter
        else {
            model.setStorageType(DataSetStorageType.LocalFileSystem);
            model.setStorageNamespace(
                    Paths.get(
                                    config.getFileUploadDir(),
                                    StringUtil.stringToUnderLineLowerCase(model.getDataResourceType().name()),
                                    model.getId()
                            )
                            .toAbsolutePath()
                            .toString()
            );

            FileUtil.createDir(model.getStorageNamespace());
        }

    }

}
