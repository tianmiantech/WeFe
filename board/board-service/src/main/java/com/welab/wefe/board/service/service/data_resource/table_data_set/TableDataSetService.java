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

package com.welab.wefe.board.service.service.data_resource.table_data_set;

import com.welab.wefe.board.service.api.data_resource.table_data_set.TableDataSetDeleteApi;
import com.welab.wefe.board.service.base.file_system.WeFeFileSystem;
import com.welab.wefe.board.service.constant.DataSetAddMethod;
import com.welab.wefe.board.service.database.entity.DataSourceMysqlModel;
import com.welab.wefe.board.service.database.entity.data_resource.DataResourceMysqlModel;
import com.welab.wefe.board.service.database.entity.data_resource.TableDataSetMysqlModel;
import com.welab.wefe.board.service.database.repository.DataSourceRepository;
import com.welab.wefe.board.service.database.repository.JobMemberRepository;
import com.welab.wefe.board.service.database.repository.JobRepository;
import com.welab.wefe.board.service.database.repository.data_resource.TableDataSetRepository;
import com.welab.wefe.board.service.dto.entity.data_resource.output.TableDataSetOutputModel;
import com.welab.wefe.board.service.dto.vo.data_resource.AbstractDataResourceUpdateInputModel;
import com.welab.wefe.board.service.dto.vo.data_resource.TableDataSetUpdateInputModel;
import com.welab.wefe.board.service.onlinedemo.OnlineDemoBranchStrategy;
import com.welab.wefe.board.service.service.CacheObjects;
import com.welab.wefe.board.service.service.DataSetColumnService;
import com.welab.wefe.board.service.service.DataSetStorageService;
import com.welab.wefe.board.service.service.data_resource.DataResourceService;
import com.welab.wefe.board.service.util.JdbcManager;
import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.data.mysql.Where;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.wefe.enums.ComponentType;
import com.welab.wefe.common.wefe.enums.DataResourceType;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.io.File;
import java.sql.Connection;

/**
 * @author Zane
 */
@Service
public class TableDataSetService extends DataResourceService {

    @Autowired
    protected DataSetColumnService dataSetColumnService;
    @Autowired
    protected DataSetStorageService dataSetStorageService;
    @Autowired
    protected JobRepository jobRepository;
    @Autowired
    protected JobMemberRepository jobMemberRepository;
    @Autowired
    protected JobRepository featureJobRepository;
    @Autowired
    DataSourceRepository dataSourceRepo;
    @Autowired
    private TableDataSetRepository tableDataSetRepository;

    /**
     * Get uploaded file
     */
    public File getDataSetFile(DataSetAddMethod method, String filename) throws StatusCodeWithException {
        File file = null;
        switch (method) {
            case HttpUpload:
                file = WeFeFileSystem.getFilePath(DataResourceType.TableDataSet, filename).toFile();
                break;
            case LocalFile:
                file = new File(filename);
                break;
            case Database:
                break;
            default:
        }

        if (null == file || !file.exists()) {
            throw new StatusCodeWithException("未找到文件：" + filename, StatusCode.PARAMETER_VALUE_INVALID);
        }

        return file;
    }


    /**
     * delete data set
     */
    public void delete(TableDataSetDeleteApi.Input input) throws StatusCodeWithException {
        TableDataSetMysqlModel model = tableDataSetRepository.findById(input.getId()).orElse(null);
        if (model == null) {
            return;
        }

        OnlineDemoBranchStrategy.hackOnDelete(input, model, "只能删除自己添加的数据集。");

        delete(input.getId());
    }

    /**
     * delete data set
     */
    public void delete(String dataSetId) throws StatusCodeWithException {
        TableDataSetMysqlModel model = tableDataSetRepository.findById(dataSetId).orElse(null);
        if (model == null) {
            return;
        }

        delete(model);
    }

    /**
     * delete data set
     */
    public void delete(TableDataSetMysqlModel model) throws StatusCodeWithException {

        // delete data set from database
        tableDataSetRepository.deleteById(model.getId());

        // delete data set from storage
        dataSetStorageService.deleteDataSet(model.getId());

        // is raw data set
        if (!model.isDerivedResource()) {
            // Refresh the data set tag list
            CacheObjects.refreshDataResourceTags(model.getDataResourceType());

            // Notify the union to do not public the data set
            unionService.doNotPublicDataSet(model);
        }

    }

    /**
     * get data source by id
     */
    public DataSourceMysqlModel getDataSourceById(String dataSourceId) {
        return dataSourceRepo.findById(dataSourceId).orElse(null);
    }

    /**
     * get data sets info from local or union
     */
    public TableDataSetOutputModel findDataSetFromLocalOrUnion(String memberId, String dataSetId) throws StatusCodeWithException {
        return super.findDataResourceFromLocalOrUnion(memberId,
                dataSetId,
                TableDataSetMysqlModel.class,
                TableDataSetOutputModel.class
        );
    }

    /**
     * Test whether SQL can be queried normally
     */
    public boolean testSqlQuery(String dataSourceId, String sql) throws StatusCodeWithException {
        DataSourceMysqlModel model = getDataSourceById(dataSourceId);
        if (model == null) {
            throw new StatusCodeWithException("dataSourceId在数据库不存在", StatusCode.DATA_NOT_FOUND);
        }

        if (StringUtils.isEmpty(sql)) {
            throw new StatusCodeWithException("请填入sql查询语句", StatusCode.PARAMETER_CAN_NOT_BE_EMPTY);
        }

        Connection conn = JdbcManager.getConnection(
                model.getDatabaseType(),
                model.getHost(),
                model.getPort(),
                model.getUserName(),
                model.getPassword(),
                model.getDatabaseName()
        );

        return JdbcManager.testQuery(conn, sql, true);
    }

    @Override
    public TableDataSetMysqlModel findOneById(String dataSetId) {
        return tableDataSetRepository.findById(dataSetId).orElse(null);
    }

    @Override
    protected void beforeUpdate(DataResourceMysqlModel m, AbstractDataResourceUpdateInputModel in) {
        TableDataSetUpdateInputModel input = (TableDataSetUpdateInputModel) in;
        // save data set column info to database
        dataSetColumnService.update(in.getId(), input.getMetadataList());
    }


    public TableDataSetMysqlModel query(String sourceJobId, ComponentType componentType) {
        Specification<TableDataSetMysqlModel> where = Where.create().equal("derivedFromJobId", sourceJobId)
                .equal("derivedFrom", componentType).build(TableDataSetMysqlModel.class);

        return tableDataSetRepository.findOne(where).orElse(null);
    }


    public void save(TableDataSetMysqlModel newDataSetModel) {
        tableDataSetRepository.save(newDataSetModel);
    }
}
