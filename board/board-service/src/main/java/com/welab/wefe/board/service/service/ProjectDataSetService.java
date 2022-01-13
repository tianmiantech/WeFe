/*
 * Copyright 2021 Tianmian Tech. All Rights Reserved.
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

package com.welab.wefe.board.service.service;

import com.welab.wefe.board.service.api.gateway.GetDerivedDataSetDetailApi;
import com.welab.wefe.board.service.api.project.dataset.QueryDerivedDataSetApi;
import com.welab.wefe.board.service.database.entity.data_resource.TableDataSetMysqlModel;
import com.welab.wefe.board.service.database.entity.job.ProjectDataSetMySqlModel;
import com.welab.wefe.board.service.database.repository.ProjectDataSetRepository;
import com.welab.wefe.board.service.dto.base.PagingOutput;
import com.welab.wefe.board.service.dto.entity.data_resource.output.DataResourceOutputModel;
import com.welab.wefe.board.service.dto.entity.data_resource.output.ImageDataSetOutputModel;
import com.welab.wefe.board.service.dto.entity.data_resource.output.TableDataSetOutputModel;
import com.welab.wefe.board.service.dto.entity.job.JobMemberOutputModel;
import com.welab.wefe.board.service.dto.entity.project.data_set.DerivedProjectDataSetOutputModel;
import com.welab.wefe.board.service.dto.entity.project.data_set.ProjectDataSetOutputModel;
import com.welab.wefe.board.service.dto.vo.JobMemberWithDataSetOutputModel;
import com.welab.wefe.board.service.service.data_resource.bloom_filter.BloomFilterService;
import com.welab.wefe.board.service.service.data_resource.image_data_set.ImageDataSetService;
import com.welab.wefe.board.service.service.data_resource.table_data_set.TableDataSetService;
import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.data.mysql.Where;
import com.welab.wefe.common.data.mysql.enums.OrderBy;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.util.JObject;
import com.welab.wefe.common.web.CurrentAccount;
import com.welab.wefe.common.web.util.ModelMapper;
import com.welab.wefe.common.wefe.enums.DataResourceType;
import com.welab.wefe.common.wefe.enums.DeepLearningJobType;
import com.welab.wefe.common.wefe.enums.JobMemberRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * @author zane.luo
 */
@Service
public class ProjectDataSetService extends AbstractService {

    @Autowired
    private TableDataSetService tableDataSetService;
    @Autowired
    private ImageDataSetService imageDataSetService;
    @Autowired
    private BloomFilterService bloomFilterService;
    @Autowired
    private ProjectDataSetRepository projectDataSetRepo;


    /**
     * Get the details of the derived data set
     */
    public DerivedProjectDataSetOutputModel getDerivedDataSetDetail(GetDerivedDataSetDetailApi.Input input) throws StatusCodeWithException {
        // 衍生数据集目前只有 TableDataSet
        TableDataSetMysqlModel dataSet = tableDataSetService.findOneById(input.getDataSetId());
        ProjectDataSetMySqlModel projectDataSet = findOne(input.getProjectId(), input.getDataSetId(), input.getMemberRole());

        if (dataSet == null || projectDataSet == null) {
            return null;
        }

        if (projectDataSet.getSourceType() == null) {
            throw new StatusCodeWithException("拒绝查询原始数据集信息", StatusCode.PARAMETER_VALUE_INVALID);
        }

        List<JobMemberWithDataSetOutputModel> members = ModelMapper.maps(jobMemberService.list(dataSet.getDerivedFromJobId(), false), JobMemberWithDataSetOutputModel.class);

        DerivedProjectDataSetOutputModel output = ModelMapper.map(projectDataSet, DerivedProjectDataSetOutputModel.class);
        output.setDataSet(ModelMapper.map(dataSet, TableDataSetOutputModel.class));
        output.setMembers(members);

        return output;
    }

    /**
     * Get a list of derived data sets in the project
     */
    public PagingOutput<DerivedProjectDataSetOutputModel> queryDerivedDataSet(QueryDerivedDataSetApi.Input input) {
        Where where = Where
                .create()
                .equal("projectId", input.getProjectId())
                .equal("dataResourceType", input.getDataResourceType())
                .equal("dataSetId", input.getDataSetId())
                .equal("sourceFlowId", input.getSourceFlowId())
                .equal("sourceJobId", input.getSourceJobId());

        if (input.getSourceType() == null) {
            where.notEqual("sourceType", null, false);
        } else {
            where.equal("sourceType", input.getSourceType());
        }

        where.orderBy("createdTime", OrderBy.desc);

        PagingOutput<ProjectDataSetMySqlModel> page = projectDataSetRepo.paging(where.build(ProjectDataSetMySqlModel.class), input);

        List<DerivedProjectDataSetOutputModel> list = page
                .getList()
                .parallelStream()
                .map(this::buildDerivedProjectDataSetOutputModel)
                .collect(Collectors.toList());

        return PagingOutput.of(page.getTotal(), list);

    }

    /**
     * 组装衍生数据集 output 对象
     * <p>
     * tips：衍生数据集目前只有 TableDataSet 类型
     */
    private DerivedProjectDataSetOutputModel buildDerivedProjectDataSetOutputModel(ProjectDataSetMySqlModel projectDataSet) {
        TableDataSetMysqlModel dataSet = tableDataSetService.findOneById(projectDataSet.getDataSetId());

        JObject json = JObject.create();
        if (dataSet != null) {
            json.putAll(JObject.create(dataSet));
        }
        json.putAll(JObject.create(projectDataSet));

        // Create a derived dataset object
        DerivedProjectDataSetOutputModel derivedDataSet = json.toJavaObject(DerivedProjectDataSetOutputModel.class);

        if (dataSet != null) {
            // Query the feature list from each member
            List<JobMemberOutputModel> jobMembers = jobMemberService.list(dataSet.getDerivedFromJobId(), false);
            List<JobMemberWithDataSetOutputModel> output = jobMembers
                    .stream()
                    .map(m -> {
                        JobMemberWithDataSetOutputModel member = ModelMapper.map(m, JobMemberWithDataSetOutputModel.class);

                        // Take your own feature list directly
                        TableDataSetOutputModel tableDataSet = null;
                        if (member.getMemberId().equals(derivedDataSet.getMemberId())) {
                            tableDataSet = (TableDataSetOutputModel) derivedDataSet.getDataSet();
                        }
                        // Others’ feature list should be checked remotely
                        else {
                            try {
                                DerivedProjectDataSetOutputModel derivedProjectDataSet = gatewayService.callOtherMemberBoard(
                                        member.getMemberId(),
                                        GetDerivedDataSetDetailApi.class,
                                        new GetDerivedDataSetDetailApi.Input(member.getProjectId(), projectDataSet.getDataSetId(), member.getJobRole()),
                                        DerivedProjectDataSetOutputModel.class
                                );
                                tableDataSet = (TableDataSetOutputModel) derivedProjectDataSet.getDataSet();
                            } catch (Exception e) {
                                super.log(e);
                            }
                        }

                        if (tableDataSet != null) {
                            member.setFeatureNameList(tableDataSet.getFeatureNameList());
                            member.setFeatureCount(tableDataSet.getFeatureCount());
                        }

                        return member;
                    })
                    .collect(Collectors.toList());

            derivedDataSet.setMembers(output);
        }

        return derivedDataSet;
    }

    @Autowired
    private JobMemberService jobMemberService;

    public List<ProjectDataSetOutputModel> listRawDataSet(String projectId, DataResourceType dataResourceType, String memberId, JobMemberRole memberRole, Boolean containsY) {
        return listRawDataSet(projectId, dataResourceType, memberId, memberRole, containsY, null);
    }

    /**
     * Display the list of data sets of the specified members in the project
     * <p>
     * When memberId is empty, check the data sets of all members.
     */
    public List<ProjectDataSetOutputModel> listRawDataSet(String projectId, DataResourceType dataResourceType, String memberId, JobMemberRole memberRole, Boolean containsY, DeepLearningJobType forJobType) {

        Specification<ProjectDataSetMySqlModel> where = Where
                .create()
                .equal("projectId", projectId)
                .equal("dataResourceType", dataResourceType)
                .equal("memberId", memberId)
                .equal("memberRole", memberRole)
                .equal("sourceType", null, false)
                .orderBy("createdTime", OrderBy.desc)
                .build(ProjectDataSetMySqlModel.class);

        List<ProjectDataSetMySqlModel> list = projectDataSetRepo.findAll(where);

        List<ProjectDataSetOutputModel> output = list
                .parallelStream()
                .map(x -> {

                    try {
                        ProjectDataSetOutputModel projectDataSet = ModelMapper.map(x, ProjectDataSetOutputModel.class);
                        DataResourceOutputModel dataSet = null;
                        if (x.getDataResourceType() == DataResourceType.TableDataSet) {
                            dataSet = tableDataSetService.findDataSetFromLocalOrUnion(x.getMemberId(), x.getDataSetId());
                        } else if (x.getDataResourceType() == DataResourceType.ImageDataSet) {
                            dataSet = imageDataSetService.findDataSetFromLocalOrUnion(x.getMemberId(), x.getDataSetId());
                        } else if (x.getDataResourceType() == DataResourceType.BloomFilter) {
                            dataSet = bloomFilterService.findDataSetFromLocalOrUnion(x.getMemberId(), x.getDataSetId());
                        }
                        // 如果这里没有拿到数据集信息，说明数据集已经被删除或者不可见。
                        if (dataSet == null) {
                            dataSet = new DataResourceOutputModel();
                            dataSet.setId(projectDataSet.getDataSetId());
                            dataSet.setDeleted(true);
                        }
                        projectDataSet.setDataSet(dataSet);
                        return projectDataSet;
                    } catch (StatusCodeWithException e) {
                        super.log(e);
                        return null;
                    }

                })
                .filter(x -> {
                    if (containsY != null && (x.getDataSet() instanceof TableDataSetOutputModel)) {
                        return containsY.equals(((TableDataSetOutputModel) x.getDataSet()).isContainsY());
                    }
                    return true;
                }).filter(x -> {
                    if (forJobType != null && (x.getDataSet() instanceof ImageDataSetOutputModel)) {
                        return forJobType.equals(((ImageDataSetOutputModel) x.getDataSet()).getForJobType());
                    }
                    return true;
                })
                .collect(Collectors.toList());

        return output;
    }

    /**
     * Show all the original data sets in the project
     */
    public List<ProjectDataSetMySqlModel> listAllRawDataSet(String projectId, String memberId) {

        Specification<ProjectDataSetMySqlModel> where = Where
                .create()
                .equal("projectId", projectId)
                .equal("memberId", memberId)
                .equal("sourceType", null, false)
                .orderBy("createdTime", OrderBy.desc)
                .build(ProjectDataSetMySqlModel.class);

        return projectDataSetRepo.findAll(where);
    }

    /**
     * Display the list of data sets of the specified members in the project
     * <p>
     * When memberId is empty, check the data sets of all members.
     */
    public List<ProjectDataSetOutputModel> list(String projectId, DataResourceType dataResourceType, String memberId) {

        Specification<ProjectDataSetMySqlModel> where = Where
                .create()
                .equal("projectId", projectId)
                .equal("dataResourceType", dataResourceType)
                .equal("memberId", memberId)
                .build(ProjectDataSetMySqlModel.class);

        List<ProjectDataSetMySqlModel> list = projectDataSetRepo.findAll(where);

        List<ProjectDataSetOutputModel> output = list
                .parallelStream()
                .map(x -> {
                    ProjectDataSetOutputModel projectDataSet = ModelMapper.map(x, ProjectDataSetOutputModel.class);
                    try {
                        TableDataSetOutputModel dataSet = tableDataSetService.findDataSetFromLocalOrUnion(x.getMemberId(), x.getDataSetId());
                        projectDataSet.setDataSet(dataSet);
                    } catch (StatusCodeWithException e) {
                        super.log(e);
                    }
                    return projectDataSet;

                })
                .collect(Collectors.toList());
        return output;
    }

    /**
     * Query ProjectDataSet based on the combination of conditions
     */
    public List<ProjectDataSetMySqlModel> findDataSetList(String projectId, String memberId, JobMemberRole memberRole) {

        return projectDataSetRepo.findAll(
                Where
                        .create()
                        .equal("projectId", projectId)
                        .equal("memberId", memberId)
                        .equal("memberRole", memberRole)
                        .build(ProjectDataSetMySqlModel.class)
        );
    }

    /**
     * Query ProjectDataSet based on the combination of conditions
     */
    public ProjectDataSetMySqlModel findOne(String projectId, String dataSetId, JobMemberRole memberRole) {

        return projectDataSetRepo
                .findOne(
                        Where
                                .create()
                                .equal("projectId", projectId)
                                .equal("dataSetId", dataSetId)
                                .equal("memberRole", memberRole)
                                .build(ProjectDataSetMySqlModel.class)
                ).orElse(null);
    }

    public ProjectDataSetMySqlModel findOne(String projectId, String dataSetId) {

        return projectDataSetRepo
                .findOne(
                        Where
                                .create()
                                .equal("projectId", projectId)
                                .equal("dataSetId", dataSetId)
                                .build(ProjectDataSetMySqlModel.class)
                ).orElse(null);
    }

    public List<ProjectDataSetMySqlModel> findAll(String projectId, String dataSetId) {
        return projectDataSetRepo.findAll(Where.create().equal("projectId", projectId).equal("dataSetId", dataSetId)
                .build(ProjectDataSetMySqlModel.class));
    }

    public void update(ProjectDataSetMySqlModel dataSet, Consumer<ProjectDataSetMySqlModel> func) {
        if (dataSet == null) {
            return;
        }

        func.accept(dataSet);
        dataSet.setUpdatedBy(CurrentAccount.id());

        projectDataSetRepo.save(dataSet);

    }

    public List<ProjectDataSetMySqlModel> listByDataSetId(String projectId, String dataSetId, JobMemberRole memberRole) {
        return projectDataSetRepo.findAll(
                Where
                        .create()
                        .equal("projectId", projectId)
                        .equal("dataSetId", dataSetId)
                        .equal("memberRole", memberRole)
                        .build(ProjectDataSetMySqlModel.class)
        );
    }
}
