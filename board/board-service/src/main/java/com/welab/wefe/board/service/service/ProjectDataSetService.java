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

package com.welab.wefe.board.service.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.welab.wefe.board.service.api.gateway.GetDerivedDataSetDetailApi;
import com.welab.wefe.board.service.api.project.dataset.QueryDerivedDataSetApi;
import com.welab.wefe.board.service.database.entity.data_set.DataSetMysqlModel;
import com.welab.wefe.board.service.database.entity.job.ProjectDataSetMySqlModel;
import com.welab.wefe.board.service.database.repository.ProjectDataSetRepository;
import com.welab.wefe.board.service.dto.base.PagingOutput;
import com.welab.wefe.board.service.dto.entity.data_set.DataSetOutputModel;
import com.welab.wefe.board.service.dto.entity.job.JobMemberOutputModel;
import com.welab.wefe.board.service.dto.entity.project.DerivedProjectDataSetOutputModel;
import com.welab.wefe.board.service.dto.entity.project.ProjectDataSetOutputModel;
import com.welab.wefe.board.service.dto.vo.JobMemberWithDataSetOutputModel;
import com.welab.wefe.board.service.exception.MemberGatewayException;
import com.welab.wefe.board.service.util.ModelMapper;
import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.data.mysql.Where;
import com.welab.wefe.common.enums.JobMemberRole;
import com.welab.wefe.common.enums.OrderBy;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.util.JObject;
import com.welab.wefe.common.web.CurrentAccount;
import com.welab.wefe.common.web.dto.ApiResult;
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
    private DataSetService dataSetService;
    @Autowired
    private ProjectDataSetRepository projectDataSetRepo;


    /**
     * Get the details of the derived data set
     */
    public DerivedProjectDataSetOutputModel getDerivedDataSetDetail(GetDerivedDataSetDetailApi.Input input) throws StatusCodeWithException {
        DataSetMysqlModel dataSet = dataSetService.findOne(input.getDataSetId());
        ProjectDataSetMySqlModel projectDataSet = findOne(input.getProjectId(), input.getDataSetId(), input.getMemberRole());

        if (dataSet == null || projectDataSet == null) {
            return null;
        }

        if (projectDataSet.getSourceType() == null) {
            throw new StatusCodeWithException("拒绝查询原始数据集信息", StatusCode.PARAMETER_VALUE_INVALID);
        }

        JObject json = JObject.create();
        json.putAll(JObject.create(dataSet));
        json.putAll(JObject.create(projectDataSet));

        DerivedProjectDataSetOutputModel output = json.toJavaObject(DerivedProjectDataSetOutputModel.class);
        List<JobMemberWithDataSetOutputModel> members = ModelMapper.maps(jobMemberService.list(dataSet.getSourceJobId(), false), JobMemberWithDataSetOutputModel.class);
        output.setMembers(members);

        LOG.info("members:" + JSON.toJSONString(members, true));
        LOG.info("input.callerMemberInfo:" + JSON.toJSONString(input.callerMemberInfo, true));

        return output;
    }

    /**
     * Get a list of derived data sets in the project
     */
    public PagingOutput<DerivedProjectDataSetOutputModel> queryDerivedDataSet(QueryDerivedDataSetApi.Input input) {
        Where where = Where
                .create()
                .equal("projectId", input.getProjectId())
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

    private DerivedProjectDataSetOutputModel buildDerivedProjectDataSetOutputModel(ProjectDataSetMySqlModel projectDataSet) {
        DataSetMysqlModel dataSet = dataSetService.findOne(projectDataSet.getDataSetId());

        JObject json = JObject.create();
        if (dataSet != null) {
            json.putAll(JObject.create(dataSet));
        }
        json.putAll(JObject.create(projectDataSet));

        // Create a derived dataset object
        DerivedProjectDataSetOutputModel derivedDataSet = json.toJavaObject(DerivedProjectDataSetOutputModel.class);

        if (dataSet != null) {
            // Query the feature list from each member
            List<JobMemberOutputModel> jobMembers = jobMemberService.list(dataSet.getSourceJobId(), false);
            List<JobMemberWithDataSetOutputModel> output = jobMembers
                    .stream()
                    .map(m -> {
                        JobMemberWithDataSetOutputModel member = ModelMapper.map(m, JobMemberWithDataSetOutputModel.class);
                        // Take your own feature list directly
                        if (member.getMemberId().equals(derivedDataSet.getMemberId())) {
                            member.setFeatureNameList(derivedDataSet.getFeatureNameList());
                            member.setFeatureCount(derivedDataSet.getFeatureCount());
                        }
                        // Others’ feature list should be checked remotely
                        else {
                            try {
                                ApiResult<?> apiResult = gatewayService.callOtherMemberBoard(
                                        member.getMemberId(),
                                        GetDerivedDataSetDetailApi.class,
                                        new GetDerivedDataSetDetailApi.Input(member.getProjectId(), projectDataSet.getDataSetId(), member.getJobRole())

                                );
                                if (apiResult.data != null) {
                                    DerivedProjectDataSetOutputModel derivedProjectDataSet = ((JSONObject) apiResult.data).toJavaObject(DerivedProjectDataSetOutputModel.class);
                                    member.setFeatureNameList(derivedProjectDataSet.getFeatureNameList());
                                    member.setFeatureCount(derivedProjectDataSet.getFeatureCount());
                                }

                            } catch (MemberGatewayException e) {
                                super.log(e);
                            }
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

    /**
     * Display the list of data sets of the specified members in the project
     * <p>
     * When memberId is empty, check the data sets of all members.
     */
    public List<ProjectDataSetOutputModel> listRawDataSet(String projectId, String memberId, JobMemberRole memberRole, Boolean containsY) {

        Specification<ProjectDataSetMySqlModel> where = Where
                .create()
                .equal("projectId", projectId)
                .equal("memberId", memberId)
                .equal("memberRole", memberRole)
                .equal("sourceType", null, false)
                .orderBy("createdTime", OrderBy.desc)
                .build(ProjectDataSetMySqlModel.class);

        List<ProjectDataSetMySqlModel> list = projectDataSetRepo.findAll(where);

        List<ProjectDataSetOutputModel> output = list
                .parallelStream()
                .map(x -> {
                    DataSetOutputModel dataSet = null;
                    try {
                        dataSet = dataSetService.findDataSetFromLocalOrUnion(x.getMemberId(), x.getDataSetId());
                        // The data set does not exist and is marked as deleted.
                        if (dataSet == null) {
                            ProjectDataSetOutputModel foo = JObject
                                    .create(x)
                                    .toJavaObject(ProjectDataSetOutputModel.class);
                            foo.setName("此数据集已被删除或不可见");
                            foo.setRowCount(0L);
                            foo.setDeleted(true);
                            return foo;
                        }
                    } catch (StatusCodeWithException e) {
                        e.printStackTrace();

                        return null;
                    }

                    JObject item = JObject.create();
                    item.putAll(JObject.create(dataSet));
                    item.putAll(JObject.create(x));

                    return item.toJavaObject(ProjectDataSetOutputModel.class);

                })
                .filter(x -> {
                    if (containsY != null) {
                        return containsY.equals(x.getContainsY());
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
    public List<ProjectDataSetOutputModel> list(String projectId, String memberId) {

        Specification<ProjectDataSetMySqlModel> where = Where
                .create()
                .equal("projectId", projectId)
                .equal("memberId", memberId)
                .build(ProjectDataSetMySqlModel.class);

        List<ProjectDataSetMySqlModel> list = projectDataSetRepo.findAll(where);

        List<ProjectDataSetOutputModel> output = list
                .parallelStream()
                .map(x -> {
                    DataSetOutputModel dataSet = null;
                    try {
                        dataSet = dataSetService.findDataSetFromLocalOrUnion(x.getMemberId(), x.getDataSetId());
                        // The data set does not exist and is marked as deleted.
                        if (dataSet == null) {
                            ProjectDataSetOutputModel foo = JObject
                                    .create(x)
                                    .toJavaObject(ProjectDataSetOutputModel.class);
                            foo.setName("此数据集已被删除或不可见");
                            foo.setRowCount(0L);
                            foo.setDeleted(true);
                            return foo;
                        }
                    } catch (StatusCodeWithException e) {
                        e.printStackTrace();

                        return null;
                    }

                    JObject item = JObject.create();
                    item.putAll(JObject.create(dataSet));
                    item.putAll(JObject.create(x));

                    return item.toJavaObject(ProjectDataSetOutputModel.class);

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
