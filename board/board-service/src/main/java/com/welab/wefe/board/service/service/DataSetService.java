/**
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

package com.welab.wefe.board.service.service;

import com.welab.wefe.board.service.api.dataset.DeleteApi;
import com.welab.wefe.board.service.api.dataset.QueryApi;
import com.welab.wefe.board.service.api.dataset.UpdateApi;
import com.welab.wefe.board.service.constant.Config;
import com.welab.wefe.board.service.constant.DataSetAddMethod;
import com.welab.wefe.board.service.database.entity.DataSourceMySqlModel;
import com.welab.wefe.board.service.database.entity.data_set.DataSetMysqlModel;
import com.welab.wefe.board.service.database.entity.job.ProjectDataSetMySqlModel;
import com.welab.wefe.board.service.database.entity.job.ProjectMySqlModel;
import com.welab.wefe.board.service.database.repository.*;
import com.welab.wefe.board.service.dto.base.PagingOutput;
import com.welab.wefe.board.service.dto.entity.data_set.DataSetOutputModel;
import com.welab.wefe.board.service.dto.entity.project.ProjectUsageDetailOutputModel;
import com.welab.wefe.board.service.onlinedemo.OnlineDemoBranchStrategy;
import com.welab.wefe.board.service.sdk.UnionService;
import com.welab.wefe.board.service.util.JdbcManager;
import com.welab.wefe.board.service.util.ModelMapper;
import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.data.mysql.Where;
import com.welab.wefe.common.enums.DataSetPublicLevel;
import com.welab.wefe.common.enums.OrderBy;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.util.StringUtil;
import com.welab.wefe.common.web.CurrentAccount;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.io.File;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * @author Zane
 */
@Service
public class DataSetService extends AbstractService {

    @Autowired
    protected DataSetRepository repo;
    @Autowired
    protected DataSetColumnService dataSetColumnService;
    @Autowired
    protected UnionService unionService;
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
    private DataSetRepository dataSetRepository;
    @Autowired
    private Config config;
    @Autowired
    private ProjectDataSetRepository projectDataSetRepository;
    @Autowired
    private ProjectRepository projectRepository;

    /**
     * Get uploaded file
     */
    public File getDataSetFile(DataSetAddMethod method, String filename) throws StatusCodeWithException {
        File file = null;
        switch (method) {
            case HttpUpload:
                file = new File(config.getFileUploadDir(), filename);
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
     * Paging query data set
     */
    public PagingOutput<DataSetOutputModel> query(QueryApi.Input input) {

        Specification<DataSetMysqlModel> where = Where
                .create()
                .equal("id", input.getId())
                .contains("name", input.getName())
                .containsItem("tags", input.getTag())
                .equal("containsY", input.getContainsY())
                .equal("createdBy", input.getCreator())
                .equal("sourceType", null, false)
                .orderBy("createdTime", OrderBy.desc)
                .build(DataSetMysqlModel.class);

        return repo.paging(where, input, DataSetOutputModel.class);
    }

    /**
     * delete data set
     */
    public void delete(DeleteApi.Input input) throws StatusCodeWithException {
        DataSetMysqlModel model = repo.findById(input.getId()).orElse(null);
        if (model == null) {
            return;
        }

        OnlineDemoBranchStrategy.hackOnDelete(input, model, "只能删除自己添加的数据集。");

        delete(model);
    }

    /**
     * delete data set
     */
    public void delete(String dataSetId) throws StatusCodeWithException {
        DataSetMysqlModel model = repo.findById(dataSetId).orElse(null);
        if (model == null) {
            return;
        }

        delete(model);
    }

    /**
     * delete data set
     */
    public void delete(DataSetMysqlModel model) throws StatusCodeWithException {

        // delete data set from database
        repo.deleteById(model.getId());

        // delete data set from storage
        dataSetStorageService.deleteDataSet(model.getId());

        // is raw data set
        if (model.getSourceType() == null) {
            // Notify the union to do not public the data set
            unionService.dontPublicDataSet(model.getId());

            // Refresh the data set tag list
            CacheObjects.refreshDataSetTags();
        }

    }

    /**
     * update data set info
     */
    public void update(UpdateApi.Input input) throws StatusCodeWithException {

        if (repo.countByName(input.getName(), input.getId()) > 0) {
            throw new StatusCodeWithException("此数据集名称已存在，请换一个数据集名称", StatusCode.PARAMETER_VALUE_INVALID);
        }

        DataSetMysqlModel model = repo.findById(input.getId()).orElse(null);
        if (model == null) {
            return;
        }

        model.setUpdatedBy(CurrentAccount.id());
        model.setName(input.getName());
        model.setTags(StringUtil.join(input.getTags(), ","));
        model.setDescription(input.getDescription());
        model.setPublicMemberList(input.getPublicMemberList());
        model.setPublicLevel(input.getPublicLevel());
        model.setTags(standardizeTags(input.getTags()));

        handlePublicMemberList(model);

        repo.save(model);

        // save data set column info to database
        dataSetColumnService.update(input.getId(), input.getMetadataList(), CurrentAccount.get());

        unionService.uploadDataSet(model);

        CacheObjects.refreshDataSetTags();
    }

    /**
     * Process the list of visible members
     * <p>
     * When the scene is visible to the specified members, automatically add itself is also visible.
     */
    public void handlePublicMemberList(DataSetMysqlModel model) {

        // When the PublicLevel is PublicWithMemberList, if list contains yourself,
        // you will be removed, and union will handle the data that you must be visible.
        if (model.getPublicLevel() == DataSetPublicLevel.PublicWithMemberList) {
            String memberId = CacheObjects.getMemberId();


            if (model.getPublicMemberList().contains(memberId)) {
                String list = model.getPublicMemberList()
                        .replace(memberId, "")
                        .replace(",,", ",");

                model.setPublicMemberList(list);
            }
        }

    }


    /**
     * Standardize the tag list
     */
    public String standardizeTags(List<String> tags) {
        if (tags == null) {
            return "";
        }

        tags = tags.stream()
                // Remove comma(,，)
                .map(x -> x.replace(",", "").replace("，", ""))
                // Remove empty elements
                .filter(x -> !StringUtil.isEmpty(x))
                .distinct()
                .sorted()
                .collect(Collectors.toList());

        // Concatenate into a string, add a comma before and after it to facilitate like query.
        return "," + StringUtil.join(tags, ',') + ",";

    }

    /**
     * get data source by id
     */
    public DataSourceMySqlModel getDataSourceById(String dataSourceId) {
        return dataSourceRepo.findById(dataSourceId).orElse(null);
    }

    /**
     * get data sets info from local or union
     */
    public DataSetOutputModel findDataSetFromLocalOrUnion(String memberId, String dataSetId) throws StatusCodeWithException {

        if (memberId.equals(CacheObjects.getMemberId())) {
            DataSetMysqlModel dataSet = repo.findById(dataSetId).orElse(null);
            if (dataSet == null) {
                return null;
            }
            return ModelMapper.map(dataSet, DataSetOutputModel.class);
        } else {
            return unionService.queryDataSetDetail(dataSetId);
        }
    }

    public DataSetMysqlModel findOne(String dataSetId) {
        return repo.findById(dataSetId).orElse(null);

    }

    /**
     * Test whether SQL can be queried normally
     */
    public boolean testSqlQuery(String dataSourceId, String sql) throws StatusCodeWithException {
        DataSourceMySqlModel model = getDataSourceById(dataSourceId);
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

    /**
     * Update the number of data sets used in the project
     */
    public void updateUsageCountInProject(String dataSetId) {
        dataSetRepository.updateUsageCountInProject(dataSetId);

        DataSetMysqlModel model = repo.findById(dataSetId).orElse(null);
        if (model == null) {
            return;
        }

        try {
            unionService.uploadDataSet(model);
        } catch (StatusCodeWithException e) {
            super.log(e);
        }
    }

    /**
     * The number of data sets used in the flow ++
     */
    public void usageCountInFlowIncrement(String dataSetId) throws StatusCodeWithException {
        updateUsageCount(dataSetId, x -> x.setUsageCountInProject(x.getUsageCountInProject() + 1));
    }

    /**
     * The number of data sets used in the flow --
     */
    public void usageCountInFlowDecrement(String dataSetId) throws StatusCodeWithException {
        updateUsageCount(dataSetId, x -> x.setUsageCountInFlow(x.getUsageCountInFlow() - 1));
    }

    /**
     * The number of data sets used in the job ++
     */
    public void usageCountInJobIncrement(String dataSetId) throws StatusCodeWithException {
        updateUsageCount(dataSetId, x -> x.setUsageCountInJob(x.getUsageCountInJob() + 1));
    }

    /**
     * Update the various usage count of the data set
     */
    private void updateUsageCount(String dataSetId, Consumer<DataSetMysqlModel> func) throws StatusCodeWithException {
        DataSetMysqlModel model = repo.findById(dataSetId).orElse(null);
        if (model == null) {
            return;
        }

        func.accept(model);
        repo.save(model);

        unionService.uploadDataSet(model);
    }

    /**
     * Query the project information used by the dataset in the project
     */
    public List<ProjectUsageDetailOutputModel> queryUsageInProject(String dataSetId) {
        List<ProjectUsageDetailOutputModel> ProjectUsageDetailOutputModelList = new ArrayList<>();
        List<ProjectDataSetMySqlModel> usageInProjectList = projectDataSetRepository.queryUsageInProject(dataSetId);
        if (usageInProjectList == null || usageInProjectList.isEmpty()) {
            return ProjectUsageDetailOutputModelList;
        }

        for (ProjectDataSetMySqlModel usageInProject : usageInProjectList) {
            ProjectMySqlModel projectMySqlModel = projectRepository.findOneById(usageInProject.getProjectId());
            ProjectUsageDetailOutputModel projectUsageDetailOutputModel = new ProjectUsageDetailOutputModel();
            projectUsageDetailOutputModel.setName(projectMySqlModel.getName());
            projectUsageDetailOutputModel.setProjectId(projectMySqlModel.getProjectId());
            ProjectUsageDetailOutputModelList.add(projectUsageDetailOutputModel);
        }

        return ProjectUsageDetailOutputModelList;
    }
}
