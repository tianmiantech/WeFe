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

import com.welab.wefe.board.service.api.data_resource.bloom_filter.BloomFilterDataResourceListApi;
import com.welab.wefe.board.service.api.data_resource.bloom_filter.BloomFilterDeleteApi;
import com.welab.wefe.board.service.base.file_system.WeFeFileSystem;
import com.welab.wefe.board.service.constant.BloomfilterAddMethod;
import com.welab.wefe.board.service.database.entity.DataSourceMysqlModel;
import com.welab.wefe.board.service.database.entity.data_resource.BloomFilterMysqlModel;
import com.welab.wefe.board.service.database.entity.job.ProjectMemberMySqlModel;
import com.welab.wefe.board.service.database.entity.job.ProjectMySqlModel;
import com.welab.wefe.board.service.database.repository.DataSourceRepository;
import com.welab.wefe.board.service.database.repository.JobMemberRepository;
import com.welab.wefe.board.service.database.repository.JobRepository;
import com.welab.wefe.board.service.database.repository.ProjectRepository;
import com.welab.wefe.board.service.database.repository.base.RepositoryManager;
import com.welab.wefe.board.service.database.repository.data_resource.BloomFilterRepository;
import com.welab.wefe.board.service.dto.entity.BloomFilterDataResourceListOutputModel;
import com.welab.wefe.board.service.dto.entity.data_resource.output.BloomFilterOutputModel;
import com.welab.wefe.board.service.dto.entity.project.ProjectDetailMemberOutputModel;
import com.welab.wefe.board.service.dto.entity.project.data_set.ProjectDataResourceOutputModel;
import com.welab.wefe.board.service.dto.vo.data_resource.BloomFilterUpdateInputModel;
import com.welab.wefe.board.service.onlinedemo.OnlineDemoBranchStrategy;
import com.welab.wefe.board.service.service.CacheObjects;
import com.welab.wefe.board.service.service.ProjectDataSetService;
import com.welab.wefe.board.service.service.ProjectMemberService;
import com.welab.wefe.board.service.service.data_resource.DataResourceService;
import com.welab.wefe.board.service.util.JdbcManager;
import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.web.util.ModelMapper;
import com.welab.wefe.common.wefe.enums.DataResourceType;
import com.welab.wefe.common.wefe.enums.DataResourcePublicLevel;
import com.welab.wefe.common.wefe.enums.JobMemberRole;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.sql.Connection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author jacky.jiang
 */
@Service
public class BloomFilterService extends DataResourceService {

    @Autowired
    protected BloomFilterRepository repo;
    @Autowired
    protected BloomFilterStorageService bloomfilterStorageService;
    @Autowired
    protected JobRepository jobRepository;
    @Autowired
    protected JobMemberRepository jobMemberRepository;
    @Autowired
    protected JobRepository featureJobRepository;
    @Autowired
    DataSourceRepository dataSourceRepo;
    @Autowired
    private ProjectRepository projectRepo;
    @Autowired
    private ProjectMemberService projectMemberService;
    @Autowired
    private ProjectDataSetService projectDataSetService;


    public BloomFilterOutputModel findDataSetFromLocalOrUnion(String memberId, String dataSetId) throws StatusCodeWithException {

        if (memberId.equals(CacheObjects.getMemberId())) {
            BloomFilterMysqlModel dataSet = repo.findById(dataSetId).orElse(null);
            if (dataSet == null) {
                return null;
            }
            return ModelMapper.map(dataSet, BloomFilterOutputModel.class);
        } else {
            return unionService.getDataResourceDetail(dataSetId, BloomFilterOutputModel.class);
        }
    }

    /**
     * Get uploaded file
     */
    public File getBloomfilterFile(BloomfilterAddMethod method, String filename) throws StatusCodeWithException {
        File file = null;
        switch (method) {
            case HttpUpload:
                file = WeFeFileSystem.getFilePath(DataResourceType.BloomFilter, filename).toFile();
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
     * delete bloom_filter
     */
    public void delete(BloomFilterDeleteApi.Input input) throws StatusCodeWithException {
        BloomFilterMysqlModel model = repo.findById(input.getId()).orElse(null);
        if (model == null) {
            return;
        }

        OnlineDemoBranchStrategy.hackOnDelete(input, model, "只能删除自己添加的数据集。");

        delete(model);
    }

    /**
     * delete bloom_filter
     */
    public void delete(String bloomFilterId) throws StatusCodeWithException {
        BloomFilterMysqlModel model = repo.findById(bloomFilterId).orElse(null);
        if (model == null) {
            return;
        }

        delete(model);
    }

    /**
     * delete bloom_filter
     */
    public void delete(BloomFilterMysqlModel model) throws StatusCodeWithException {

        // delete bloom_filter from database
        repo.deleteById(model.getId());

        // delete bloom_filter from folder
        bloomfilterStorageService.deleteBloomfilter(model.getId());

        // Notify the union to do not public the bloom_filter
        unionService.deleteDataResource(model);

        // Refresh the bloom_filter tag list
        CacheObjects.refreshDataResourceTags(model.getDataResourceType());


    }

    /**
     * Process the list of visible members
     * <p>
     * When the scene is visible to the specified members, automatically add itself is also visible.
     */
    public void handlePublicMemberList(BloomFilterMysqlModel model) {

        // When the PublicLevel is PublicWithMemberList, if list contains yourself,
        // you will be removed, and union will handle the data that you must be visible.
        if (model.getPublicLevel() == DataResourcePublicLevel.PublicWithMemberList) {
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
     * get data source by id
     */
    public DataSourceMysqlModel getDataSourceById(String dataSourceId) {
        return dataSourceRepo.findById(dataSourceId).orElse(null);
    }


    public BloomFilterMysqlModel findOne(String bloomFilterId) {
        return repo.findById(bloomFilterId).orElse(null);
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


    public BloomFilterDataResourceListOutputModel query(BloomFilterDataResourceListApi.Input input) throws StatusCodeWithException {
        ProjectMySqlModel project = projectRepo.findOne("projectId", input.getProjectId(), ProjectMySqlModel.class);
        if (project == null) {
            throw new StatusCodeWithException("未找到相应的项目！", StatusCode.ILLEGAL_REQUEST);
        }

        BloomFilterDataResourceListOutputModel output = ModelMapper.map(project, BloomFilterDataResourceListOutputModel.class);


        ProjectMemberMySqlModel memberMySqlModel = projectMemberService.findOneByMemberId(input.getProjectId(), input.getMemberId(), input.getRole());
        ProjectDetailMemberOutputModel memberOutputModel = ModelMapper.map(memberMySqlModel, ProjectDetailMemberOutputModel.class);

        List<ProjectDataResourceOutputModel> allDataSetList = projectDataSetService.listRawDataSet(input.getProjectId(), null, input.getMemberId(), input.getRole(), null);
        memberOutputModel.setDataResourceList(
                allDataSetList.stream().filter(
                                x -> StringUtils.isEmpty(input.getName()) ? true : x.getDataResource().getName().contains(input.getName()))
                        .collect(Collectors.toList())
        );


        output.setDataSetList(memberOutputModel.getDataResourceList());

        return output;
    }

    /**
     * update bloom filter info
     */
    public void update(BloomFilterUpdateInputModel input) throws StatusCodeWithException {
        BloomFilterMysqlModel model = findOne(input.getId());
        if (model == null) {
            return;
        }

        model.setUpdatedBy(input);
        model.setName(input.getName());
        model.setDescription(input.getDescription());
        model.setPublicMemberList(input.getPublicMemberList());
        model.setPublicLevel(input.getPublicLevel());
        model.setTags(standardizeTags(input.getTags()));
        handlePublicMemberList(model);

        RepositoryManager.get(model.getClass()).save(model);


        unionService.upsertDataResource(model);
        CacheObjects.refreshDataResourceTags(model.getDataResourceType());
    }

}
