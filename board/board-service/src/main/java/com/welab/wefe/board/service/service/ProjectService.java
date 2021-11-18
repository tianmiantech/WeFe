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

package com.welab.wefe.board.service.service;

import com.alibaba.fastjson.JSONObject;
import com.welab.wefe.board.service.api.project.dataset.AddDataSetApi;
import com.welab.wefe.board.service.api.project.dataset.RemoveDataSetApi;
import com.welab.wefe.board.service.api.project.member.ExitProjectApi;
import com.welab.wefe.board.service.api.project.member.ListApi;
import com.welab.wefe.board.service.api.project.member.RemoveApi;
import com.welab.wefe.board.service.api.project.project.*;
import com.welab.wefe.board.service.database.entity.job.*;
import com.welab.wefe.board.service.database.repository.*;
import com.welab.wefe.board.service.dto.base.PagingOutput;
import com.welab.wefe.board.service.dto.entity.ProjectDataSetInput;
import com.welab.wefe.board.service.dto.entity.ProjectMemberInput;
import com.welab.wefe.board.service.dto.entity.project.ProjectDetailMemberOutputModel;
import com.welab.wefe.board.service.dto.entity.project.ProjectMemberOutputModel;
import com.welab.wefe.board.service.dto.entity.project.ProjectOutputModel;
import com.welab.wefe.board.service.dto.entity.project.ProjectQueryOutputModel;
import com.welab.wefe.board.service.dto.entity.project.data_set.ProjectDataSetOutputModel;
import com.welab.wefe.board.service.dto.vo.AuditStatusCounts;
import com.welab.wefe.board.service.dto.vo.RoleCounts;
import com.welab.wefe.board.service.onlinedemo.OnlineDemoBranchStrategy;
import com.welab.wefe.board.service.service.dataset.DataSetService;
import com.welab.wefe.common.Convert;
import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.data.mysql.Where;
import com.welab.wefe.common.enums.AuditStatus;
import com.welab.wefe.common.enums.FederatedLearningType;
import com.welab.wefe.common.enums.JobMemberRole;
import com.welab.wefe.common.enums.ProjectFlowStatus;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.util.JObject;
import com.welab.wefe.common.util.StringUtil;
import com.welab.wefe.common.util.ThreadUtil;
import com.welab.wefe.common.web.CurrentAccount;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.common.web.util.ModelMapper;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author zane.luo
 */
@Service
public class ProjectService extends AbstractService {

    @Autowired
    private ProjectMemberService projectMemberService;

    @Autowired
    private ProjectDataSetService projectDataSetService;

    @Autowired
    private ProjectFlowService projectFlowService;

    @Autowired
    private ProjectFlowNodeService projectFlowNodeService;

    @Autowired
    private JobService jobService;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private ProjectRepository projectRepo;

    @Autowired
    private ProjectMemberRepository projectMemberRepo;

    @Autowired
    private ProjectDataSetRepository projectDataSetRepo;

    @Autowired
    private DataSetService dataSetService;

    @Autowired
    private ProjectMemberAuditRepository projectMemberAuditRepository;
    @Autowired
    ProjectMemberAuditService projectMemberAuditService;

    @Autowired
    private ProjectFlowRepository projectFlowRepository;

    @Autowired
    private ProjectFlowNodeRepository projectFlowNodeRepository;

    /**
     * New Project
     * <p>
     * promoter
     * project: agree
     * project_member: agree
     * <p>
     * provider
     * project: auditing
     * project_member: auditing
     */
    @Transactional(rollbackFor = Exception.class)
    public synchronized String addProject(AddApi.Input input) throws StatusCodeWithException {

        if (!input.fromGateway()) {
            // create projectId
            input.setProjectId(UUID.randomUUID().toString().replaceAll("-", ""));

            ProjectMemberInput promoter = new ProjectMemberInput();
            promoter.setMemberRole(JobMemberRole.promoter);
            promoter.setMemberId(CacheObjects.getMemberId());
            promoter.setDataSetList(input.getPromoterDataSetList());

            input.getProviderList().forEach(x -> x.setMemberRole(JobMemberRole.provider));
            List<ProjectMemberInput> members = new ArrayList<>();
            input.getProviderList().forEach(x -> members.add(x));
            input.setMembers(members);
            if (input.getPromoterList() != null && CollectionUtils.isNotEmpty(input.getPromoterList())) {
                input.getPromoterList().forEach(x -> x.setMemberRole(JobMemberRole.promoter));
                input.getMembers().addAll(0, input.getPromoterList().stream()
                        .filter(s -> !s.getMemberId().equals(promoter.getMemberId())).collect(Collectors.toList()));
            }
            input.getMembers().add(0, promoter);
        }


        ProjectMySqlModel project = new ProjectMySqlModel();
        project.setProjectType(input.getProjectType());
        project.setCreatedBy(input);
        project.setMemberId(input.fromGateway() ? input.callerMemberInfo.getMemberId() : CacheObjects.getMemberId());
        project.setMyRole(input.fromGateway() ? input.getRole() : JobMemberRole.promoter);
        project.setProjectId(input.getProjectId());
        project.setName(input.getName());
        project.setProjectDesc(input.getDesc());
        project.setAuditStatus(input.fromGateway() ? AuditStatus.auditing : AuditStatus.agree);
        project.setStatusUpdatedTime(new Date());
        project.setProgress(0);
        project.setProgressUpdatedTime(new Date());
        project.setUpdatedBy(input);
        project.setAuditStatusFromMyself(input.fromGateway() ? AuditStatus.auditing : AuditStatus.agree);
        project.setFlowStatusStatistics(JObject.create()
                .append(ProjectFlowStatus.editing.name(), 0)
                .append(ProjectFlowStatus.running.name(), 0)
                .append(ProjectFlowStatus.finished.name(), 0).toJSONString());
        projectRepo.save(project);

        // create and save ProjectMember to database
        for (ProjectMemberInput item : input.getMembers()) {
            ProjectMemberMySqlModel member = new ProjectMemberMySqlModel();
            member.setProjectId(project.getProjectId());
            member.setCreatedBy(input);
            member.setMemberId(item.getMemberId());
            member.setMemberRole(item.getMemberRole());

            // I directly agree to a project initiated by myself
            AuditStatus auditStatus = item.getMemberId().equals(project.getMemberId()) ? AuditStatus.agree : AuditStatus.auditing;
            member.setAuditStatus(auditStatus);
            member.setAuditStatusFromMyself(auditStatus);
            member.setAuditComment(item.getMemberId().equals(project.getMemberId()) ? "项目创建者" : null);
            member.setInviterId(item.getMemberId().equals(project.getMemberId()) ? null
                    : input.fromGateway() ? input.callerMemberInfo.getMemberId() : CacheObjects.getMemberId());
            member.setFromCreateProject(true);
            projectMemberRepo.save(member);

            // The pause of 2ms here is to generate the createdTime of each member in order,
            // which is convenient for displaying in order on the interface.
            ThreadUtil.sleep(2);


            // save ProjectDataSet to database
            for (ProjectDataSetInput dataSetInput : item.getDataSetList()) {
                ProjectDataSetMySqlModel dataSet = new ProjectDataSetMySqlModel();
                dataSet.setProjectId(project.getProjectId());
                dataSet.setCreatedBy(input);
                dataSet.setMemberId(dataSetInput.getMemberId());
                dataSet.setMemberRole(dataSetInput.getMemberRole());
                dataSet.setDataSetId(dataSetInput.getDataSetId());
                dataSet.setStatusUpdatedTime(new Date());
                dataSet.setAuditStatus(auditStatus);
                dataSet.setSourceType(null);

                projectDataSetRepo.save(dataSet);

                // Update the usage count of the dataset in the project
                if (auditStatus == AuditStatus.agree) {
                    dataSetService.updateUsageCountInProject(dataSet.getDataSetId());
                }
            }

        }

        gatewayService.syncToNotExistedMembers(project.getProjectId(), input, AddApi.class);

        return project.getProjectId();

    }


    public DataInfoApi.Output getDataInfo(DataInfoApi.Input input) throws StatusCodeWithException {

        DataInfoApi.Output output = new DataInfoApi.Output();

        ProjectMySqlModel project = projectRepo.findOne("projectId", input.getProjectId(), ProjectMySqlModel.class);

        if (project == null) {
            throw new StatusCodeWithException("未找到相应的项目！", StatusCode.DATA_NOT_FOUND);
        }

        List<ProjectMemberMySqlModel> projectMembers = projectMemberService.findListByProjectId(input.getProjectId());

        List<ProjectDataSetMySqlModel> projectDataSets = new ArrayList<>();
        for (ProjectMemberMySqlModel projectMember : projectMembers) {
            if (projectMember.getMemberRole() == JobMemberRole.provider) {
                ProjectMemberMySqlModel sameData = projectMembers
                        .stream()
                        .filter(x -> x.getMemberId().equals(projectMember.getMemberId()) && x.getMemberRole() == JobMemberRole.promoter)
                        .findFirst().orElse(null);
                if (sameData != null) {
                    continue;
                }
            }
            List<ProjectDataSetMySqlModel> projectMemberDataSets = projectDataSetService
                    .findDataSetList(input.getProjectId(), projectMember.getMemberId(), projectMember.getMemberRole());
            projectDataSets.addAll(projectMemberDataSets);
        }

        List<ProjectFlowMySqlModel> projectFlows = projectFlowService.findFlowsByProjectId(input.getProjectId());


        List<ProjectFlowNodeMySqlModel> projectFlowNodes = new ArrayList<>();
        for (ProjectFlowMySqlModel projectFlow : projectFlows) {

            List<ProjectFlowNodeMySqlModel> projectFlowNodeList = projectFlowNodeService.findNodesByFlowId(projectFlow.getFlowId());
            projectFlowNodes.addAll(projectFlowNodeList);
        }

        output.setProject(project);
        output.setProjectMembers(projectMembers);
        output.setProjectDataSets(projectDataSets);
        output.setProjectFlows(projectFlows);
        output.setProjectFlowNodes(projectFlowNodes);

        return output;

    }

    public ProjectOutputModel detail(String projectId) throws StatusCodeWithException {
        ProjectMySqlModel project = projectRepo.findOne("projectId", projectId, ProjectMySqlModel.class);
        if (project == null) {
            throw new StatusCodeWithException("未找到相应的项目！", StatusCode.ILLEGAL_REQUEST);
        }


        List<ProjectDetailMemberOutputModel> allMemberList = projectMemberService
                .findListByProjectId(projectId)
                .parallelStream()
                .map(x -> ModelMapper.map(x, ProjectDetailMemberOutputModel.class))
                .collect(Collectors.toList());

        List<ProjectDataSetOutputModel> allDataSetList = projectDataSetService.listRawDataSet(projectId, null, null, null, null);


        // Populate the member's data set list
        allMemberList.forEach(member ->
                member.setDataSetList(
                        allDataSetList
                                .stream()
                                .filter(dataSet ->
                                        dataSet != null
                                                && dataSet.getMemberId().equals(member.getMemberId())
                                                && dataSet.getMemberRole() == member.getMemberRole()
                                )
                                .collect(Collectors.toList())
                )
        );
        List<ProjectDetailMemberOutputModel> promoters = allMemberList.stream()
                .filter(x -> x.getMemberRole() == JobMemberRole.promoter).collect(Collectors.toList());
        ProjectDetailMemberOutputModel promoter = null;
        if (promoters.size() == 1) {
            promoter = allMemberList.stream().filter(x -> x.getMemberRole() == JobMemberRole.promoter).findFirst()
                    .orElse(null);
            promoters = null;
        } else if (promoters.size() > 1) {
            promoter = allMemberList.stream()
                    .filter(x -> x.getMemberRole() == JobMemberRole.promoter && StringUtils.isBlank(x.getInviterId()))
                    .findFirst().orElse(null);
            String creator = promoter.getMemberId();
            promoters = promoters.stream().filter(p -> !p.getMemberId().equals(creator)).collect(Collectors.toList());
        }

        List<ProjectDetailMemberOutputModel> providers = allMemberList.stream()
                .filter(x -> x.getMemberRole() == JobMemberRole.provider).collect(Collectors.toList());

        ProjectOutputModel output = ModelMapper.map(project, ProjectOutputModel.class);
        ProjectDetailMemberOutputModel newPromoter = JSONObject.parseObject(JSONObject.toJSONString(promoter),
                ProjectDetailMemberOutputModel.class);
        output.setPromoter(newPromoter);
        output.setProviderList(providers);
        output.setPromoterList(promoters);
        output.setIsCreator(
                promoter != null ? promoter.getMemberId().equalsIgnoreCase(CacheObjects.getMemberId()) : null);
        output.setIsExited(!(allMemberList.stream()
                .anyMatch(s -> s.getMemberId().equals(CacheObjects.getMemberId()) && !s.isExited())));
        return output;
    }


    /**
     * remove member
     */
    @Transactional(rollbackFor = Exception.class)
    public synchronized void removeMember(RemoveApi.Input input) throws StatusCodeWithException {
        ProjectMySqlModel project = findByProjectId(input.getProjectId());
        if (project == null) {
            throw new StatusCodeWithException("未找到相应的项目！", StatusCode.PARAMETER_VALUE_INVALID);
        }
        if (project.getMyRole() != JobMemberRole.promoter && !input.fromGateway()) {
            throw new StatusCodeWithException("移除成员的操作只有 promoter 能发起", StatusCode.ILLEGAL_REQUEST);
        }
        ProjectMemberMySqlModel member = projectMemberService.findOneByMemberId(input.getProjectId(), input.getMemberId(), input.getMemberRole());
        if (member == null) {
            return;
        }

        OnlineDemoBranchStrategy.hackOnDelete(input, member, "只能删除自己加的成员。");

        // Mark all data sets belonging to the member as unavailable
        projectDataSetRepo.disableDataSetWhenMemberExist(input.getProjectId(), input.getMemberId(), input.getMemberRole().name());

        // Update the usage count of the dataset in the project
        projectDataSetService
                .listAllRawDataSet(project.getProjectId(), member.getMemberId())
                .stream()
                .forEach(x -> dataSetService.updateUsageCountInProject(x.getDataSetId()));

        checkAuditingRecord(input.getProjectId(), input.getMemberId());

        gatewayService.syncToNotExistedMembers(input.getProjectId(), input, RemoveApi.class);

        projectMemberService.updateProjectMember(member, (x) -> {
            member.setExited(true);
            member.setFromCreateProject(false);
            member.setAuditComment("该成员已被 promoter 移除");
            return x;
        });

        // If you remove myself
        if (input.fromGateway() && input.getMemberId().equals(CacheObjects.getMemberId())) {
            project.setExited(true);
            project.setExitedBy(input.callerMemberInfo.getMemberId());
            project.setExitedTime(new Date());
            projectRepo.save(project);
        }

    }

    /**
     * Process and delete the audit information of a member
     * todo: winter
     */
    private void checkAuditingRecord(String projectId, String memberId) {
        // Find all audit information for this project
        Specification<ProjectMemberAuditMySqlModel> where = Where.create().equal("projectId", projectId)
                .build(ProjectMemberAuditMySqlModel.class);
        List<ProjectMemberAuditMySqlModel> allAuditList = projectMemberAuditRepository.findAll(where);
        if (CollectionUtils.isNotEmpty(allAuditList)) {

            // Auditee, audit list
            Map<String, Boolean> auditMap = new HashMap<>();
            for (ProjectMemberAuditMySqlModel audit : allAuditList) {
                // Information that needs to be reviewed by A
                if (audit.getAuditorId().equals(memberId) && audit.getAuditResult() == AuditStatus.auditing) {
                    // A was removed and temporarily set to agree
                    audit.setAuditComment("成员已被移除，自动同意");
                    audit.setAuditResult(AuditStatus.agree);
                }

                Boolean agree = auditMap.get(audit.getMemberId());
                if (agree == null) {
                    agree = audit.getAuditResult() == AuditStatus.agree;
                } else {
                    agree = audit.getAuditResult() == AuditStatus.agree && agree;
                }
                auditMap.put(audit.getMemberId(), agree);
            }

            for (Map.Entry<String, Boolean> auditEntry : auditMap.entrySet()) {
                // Person being audited
                String mId = auditEntry.getKey();
                boolean agree = auditEntry.getValue();
                if (!agree) {
                    continue;
                }
                // Person being audited
                List<ProjectMemberMySqlModel> needAuditMembers = projectMemberService.findListByMemberId(projectId,
                        mId);
                ProjectMemberMySqlModel needAuditMember = needAuditMembers.stream()
                        .filter(s -> s.getAuditStatus() == AuditStatus.auditing && !s.isExited()).findFirst().orElse(null);
                if (needAuditMember != null) {
                    projectMemberService.updateProjectMember(needAuditMember, (x) -> {
                        x.setAuditStatus(AuditStatus.agree);
                        x.setAuditStatusFromOthers(AuditStatus.agree);
                        x.setAuditComment("有成员被移除，自动审核通过");
                        x.setExited(false);
                        return x;
                    });
                    // If the person being reviewed is yourself,
                    // update the review status of the project
                    if (CacheObjects.getMemberId().equals(needAuditMember.getMemberId())) {
                        ProjectMySqlModel project = projectService.findByProjectId(projectId);
                        project.setAuditStatus(AuditStatus.agree);
                        project.setAuditStatusFromOthers(AuditStatus.agree);
                        project.setAuditComment(null);
                        project.setStatusUpdatedTime(new Date());
                        projectRepo.save(project);
                    }
                }
            }
        }
        // Delete all the members' pending audit records of this member,
        // and he no longer needs to review
        projectMemberAuditRepository.deleteAuditingRecord(projectId, memberId);
    }

    /**
     * Add a dataset to an existing project
     */
    @Transactional(rollbackFor = Exception.class)
    public synchronized ProjectMySqlModel addProjectDataSet(AddDataSetApi.Input input) throws StatusCodeWithException {
        ProjectMySqlModel project = findByProjectId(input.getProjectId());
        if (project == null) {
            throw new StatusCodeWithException("未找到相应的项目！", StatusCode.ILLEGAL_REQUEST);
        }

        if (!input.fromGateway()) {
            if (project.getAuditStatus() != AuditStatus.agree || project.isExited()) {
                throw new StatusCodeWithException("请在成为该项目的正式成员后再进行相关操作", StatusCode.ILLEGAL_REQUEST);
            }
        }

        if (CollectionUtils.isEmpty(input.getDataSetList())) {
            throw new StatusCodeWithException("数据集不能为空", StatusCode.ILLEGAL_REQUEST);
        }

        for (ProjectDataSetInput item : input.getDataSetList()) {
            // Determine whether the member exists
            ProjectMemberMySqlModel member = projectMemberService.findOneByMemberId(input.getProjectId(), item.getMemberId(), item.getMemberRole());
            if (member == null) {
                throw new StatusCodeWithException("该成员不存在", StatusCode.ILLEGAL_REQUEST);
            }

            // Add your own data set by yourself, the review status is agreed,
            // and add another person's data set, the status is under review.
            AuditStatus auditStatus;
            if (input.fromGateway()) {
                auditStatus = item.getMemberId().equals(input.callerMemberInfo.getMemberId())
                        ? AuditStatus.agree
                        : AuditStatus.auditing;
            } else {
                auditStatus = item.getMemberId().equals(CacheObjects.getMemberId())
                        ? AuditStatus.agree
                        : AuditStatus.auditing;
            }

            // Determine whether the data set exists
            ProjectDataSetMySqlModel projectDataSet = projectDataSetService.findOne(input.getProjectId(), item.getDataSetId(), item.getMemberRole());
            if (projectDataSet != null) {
                projectDataSet.setAuditStatus(auditStatus);
            } else {
                projectDataSet = ModelMapper.map(item, ProjectDataSetMySqlModel.class);
                projectDataSet.setProjectId(project.getProjectId());
                projectDataSet.setCreatedBy(input);
                projectDataSet.setAuditStatus(auditStatus);

                projectDataSet.setMemberRole(item.getMemberRole());
                projectDataSet.setStatusUpdatedTime(new Date());
                projectDataSet.setSourceType(null);
                projectDataSet.setDataSetType(item.getDataSetType());
            }

            projectDataSetRepo.save(projectDataSet);
            // Update the usage count of the dataset in the project
            if (projectDataSet.getAuditStatus() == AuditStatus.agree) {
                dataSetService.updateUsageCountInProject(projectDataSet.getDataSetId());
            }

        }

        gatewayService.syncToNotExistedMembers(input.getProjectId(), input, AddDataSetApi.class);

        return project;
    }


    /**
     * Remove the data set in the project
     */
    @Transactional(rollbackFor = Exception.class)
    public synchronized void removeDataSet(RemoveDataSetApi.Input input) throws StatusCodeWithException {

        ProjectMySqlModel project = findByProjectId(input.getProjectId());
        if (project == null) {
            throw new StatusCodeWithException("未找到相应的项目！", StatusCode.ILLEGAL_REQUEST);
        }

        if (!input.fromGateway()) {
            if (project.getAuditStatus() != AuditStatus.agree || project.isExited()) {
                throw new StatusCodeWithException("请在成为该项目的正式成员后再进行相关操作", StatusCode.ILLEGAL_REQUEST);
            }
        }

        // Determine whether the data set exists
        ProjectDataSetMySqlModel projectDataSet = projectDataSetService.findOne(input.getProjectId(), input.getDataSetId(), input.getMemberRole());
        if (projectDataSet == null) {
            return;
        }

        if (!input.fromGateway()) {

            // If it is the raw data set
            if (projectDataSet.getSourceType() == null) {
                // Not a promoter, you can't delete other people's data sets
                if (project.getMyRole() != JobMemberRole.promoter && !CacheObjects.getMemberId().equals(projectDataSet.getMemberId())) {
                    throw new StatusCodeWithException("不能删除别人的数据集", StatusCode.ILLEGAL_REQUEST);
                }

                OnlineDemoBranchStrategy.hackOnDelete(input, projectDataSet, "只能删除自己添加的数据集。");
            }
            // If it is the derived data set
            else {
                if (project.getMyRole() != JobMemberRole.promoter) {
                    throw new StatusCodeWithException("只有 promoter 才能删除衍生数据集", StatusCode.ILLEGAL_REQUEST);
                }

                dataSetService.delete(projectDataSet.getDataSetId());
            }

        }

        // delete ProjectDataSet
        projectDataSetRepo.deleteById(projectDataSet.getId());

        // Update the usage count of the dataset in the project
        dataSetService.updateUsageCountInProject(projectDataSet.getDataSetId());

        gatewayService.syncToNotExistedMembers(input.getProjectId(), input, RemoveDataSetApi.class);

    }

    /**
     * Count the number of items from roles and audit status respectively
     */
    public CountStatisticsApi.Output statistics(QueryApi.Input input) {
        // Statistics by role
        StringBuffer sql = new StringBuffer()
                .append("select my_role,count(*) as 'count' from project p ")
                .append(buildQueryWhere(input))
                .append(" group by my_role");

        Map<JobMemberRole, Long> byRole = projectRepo
                .query(sql.toString())
                .stream()
                .map(x ->
                        new RoleCounts(
                                JobMemberRole.valueOf(String.valueOf(x[0])),
                                Long.parseLong(String.valueOf(x[1]))
                        )
                ).collect(Collectors.toMap(x -> x.getRole(), x -> x.getCount()));

        for (JobMemberRole value : JobMemberRole.values()) {
            if (!byRole.containsKey(value)) {
                byRole.put(value, 0L);
            }
        }

        long total = 0;

        for (Long value : byRole.values()) {
            total += value;
        }

        // Statistics by audit status
        sql = new StringBuffer()
                .append("select p.audit_status,count(*) as 'count' from project p ")
                .append(buildQueryWhere(input))
                .append(" group by p.audit_status");

        Map<AuditStatus, Long> byAuditStatus = projectRepo
                .query(sql.toString())
                .stream()
                .map(x ->
                        new AuditStatusCounts(
                                AuditStatus.valueOf(String.valueOf(x[0])),
                                Long.parseLong(String.valueOf(x[1]))
                        )
                ).collect(Collectors.toMap(x -> x.getAuditStatus(), x -> x.getCount()));

        for (AuditStatus value : AuditStatus.values()) {
            if (!byAuditStatus.containsKey(value)) {
                byAuditStatus.put(value, 0L);
            }
        }

        return new CountStatisticsApi.Output(total, byRole, byAuditStatus);

    }

    public PagingOutput<ProjectQueryOutputModel> query(QueryApi.Input input) {

        StringBuffer sql = new StringBuffer(
                "select distinct(p.id),p.project_type,p.flow_status_statistics,p.deleted,p.name,p.project_desc,p.audit_status,p.status_updated_time"
                        + ",p.audit_status_from_myself,p.audit_status_from_others,p.audit_comment,p.exited,p.closed"
                        + ",p.closed_by,p.closed_time,p.exited_by,p.exited_time"
                        + ",p.project_id,p.member_id,p.my_role"
                        + ",p.start_time,p.finish_time,p.progress,p.progress_updated_time,p.message"
                        + ",p.created_by,p.created_time,p.updated_by,p.updated_time" + " from project p");

        int total = projectRepo.queryByClass(sql.append(buildQueryWhere(input)).toString(), ProjectMySqlModel.class).size();

        sql.append(" order by p.created_time desc");
        sql.append(" limit " + input.getPageIndex() * input.getPageSize() + "," + input.getPageSize());

        List<ProjectMySqlModel> projectList = projectRepo.queryByClass(sql.toString(), ProjectMySqlModel.class);
        List<ProjectQueryOutputModel> list = projectList
                .parallelStream()
                .map(this::buildJobOutputModel)
                .collect(Collectors.toList());
        return PagingOutput.of(total, list);
    }

    private String buildQueryWhere(QueryApi.Input input) {
        StringBuffer where = new StringBuffer();

        if (StringUtil.isNotBlank(input.getMemberId())) {
            where.append(" inner join project_member pm on p.project_id=pm.project_id where 1=1");
            where.append(" and pm.member_id = '" + input.getMemberId() + "'");
            if (input.getMemberRole() != null) {
                where.append(" and pm.member_role = '" + input.getMemberRole() + "'");
            }
        } else {
            where.append(" where 1=1");
        }


        where.append(" and p.deleted != true ");

        if (input.getProjectType() != null) {
            where.append(" and p.project_type = '" + input.getProjectType() + "'");
        }

        if (StringUtil.isNotBlank(input.getName())) {
            where.append(" and p.name like '%" + input.getName() + "%'");
        }

        if (StringUtil.isNotBlank(input.getStartCreateTime())) {
            where.append(" and p.created_time >= '" + input.getStartCreateTime() + "'");
        }

        if (StringUtil.isNotBlank(input.getEndCreateTime())) {
            where.append(" and p.created_time <= '" + input.getEndCreateTime() + "'");
        }

        if (input.getMyRole() != null) {
            where.append(" and p.my_role = '" + input.getMyRole() + "'");
        }

        if (input.getClosed() != null) {
            where.append(" and p.closed = " + input.getClosed());
        }

        where.append(" and p.exited = 0");

        if (input.getAuditStatus() != null) {
            if (input.getAuditStatus() == AuditStatus.auditing) {
                where.append(""
                        + " and ("
                        + "p.audit_status= '" + input.getAuditStatus() + "'"
                        + " or "
                        + " p.project_id in ("
                        + "select project_id from project_data_set where member_id='" + CacheObjects.getMemberId()
                        + "' and audit_status='" + AuditStatus.auditing.name() + "' and source_type is null"
                        + ")"
                        + ")"
                );
            } else {
                where.append(" and p.audit_status= '" + input.getAuditStatus() + "'");
            }

        }

        return where.toString();
    }

    private ProjectQueryOutputModel buildJobOutputModel(ProjectMySqlModel project) {
        ProjectQueryOutputModel output = ModelMapper.map(project, ProjectQueryOutputModel.class);

        // fill the list of members under the project
        List<ProjectMemberOutputModel> members = projectMemberService
                .findListByProjectId(project.getProjectId())
                .stream()
                .filter(m -> !m.isExited())
                .map(member -> {

                    if (member.getMemberRole().equals(JobMemberRole.promoter)
                            && member.getMemberId().equals(project.getMemberId())) {
                        output.setPromoter(member.getMemberId());
                        output.setPromoterName(CacheObjects.getMemberName(member.getMemberId()));
                    }
                    return ModelMapper.map(member, ProjectMemberOutputModel.class);

                })
                .collect(Collectors.toList());

        output.setNeedMeAuditDataSetCount(
                projectDataSetRepo.queryNeedAuditDataSetCount(project.getProjectId(), CacheObjects.getMemberId())
        );

        output.setMemberList(members);
        return output;
    }

    @Transactional(rollbackFor = Exception.class)
    public synchronized void updateProject(UpdateProjectApi.Input input) throws StatusCodeWithException {
        ProjectMySqlModel project = findByProjectId(input.getProjectId());
        if (project == null) {
            throw new StatusCodeWithException("未找到相应的项目！", StatusCode.ILLEGAL_REQUEST);
        }

        if (!input.fromGateway()) {
            if (project.getMyRole() != JobMemberRole.promoter
                    || !project.getMemberId().equals(CacheObjects.getMemberId())) {
                throw new StatusCodeWithException("只有发起方才能更改项目！", StatusCode.ILLEGAL_REQUEST);
            }
        }

        project.setName(input.getName());
        project.setProjectDesc(input.getDesc());
        project.setUpdatedBy(input);

        projectRepo.save(project);

        gatewayService.syncToNotExistedMembers(input.getProjectId(), input, UpdateProjectApi.class);
    }

    public ProjectMySqlModel findByProjectId(String projectId) {
        Specification<ProjectMySqlModel> where = Where.create().equal("projectId", projectId)
                .build(ProjectMySqlModel.class);
        return projectRepo.findOne(where).orElse(null);
    }

    /**
     * audit new projects
     * <p>
     * auditor
     * pullProjectInfo()
     * project: result
     * project_member: result
     * <p>
     * other
     * project_member: result
     */
    @Transactional(rollbackFor = Exception.class)
    public synchronized void auditProject(AuditApi.Input input) throws StatusCodeWithException {
        ProjectMySqlModel project = findByProjectId(input.getProjectId());
        if (project == null) {
            throw new StatusCodeWithException("未找到相应的项目！", StatusCode.PARAMETER_VALUE_INVALID);
        }

        if (!input.fromGateway() && project.getAuditStatus() != AuditStatus.auditing) {
            throw new StatusCodeWithException("不能重复审核！", StatusCode.PARAMETER_VALUE_INVALID);
        }

        String auditorId = input.fromGateway() ? input.callerMemberInfo.getMemberId() : CacheObjects.getMemberId();
        List<ProjectMemberMySqlModel> list = projectMemberService.findListByMemberId(project.getProjectId(), auditorId);
        if (list == null || list.isEmpty()) {
            throw new StatusCodeWithException("未找到项目关联的member！", StatusCode.PARAMETER_VALUE_INVALID);
        }

        ProjectMemberMySqlModel member = list.stream()
                .filter(s -> s.getAuditStatus() == AuditStatus.auditing && !s.isExited()).findFirst().get();
        if (member == null) {
            throw new StatusCodeWithException("未找到项目关联的member！", StatusCode.PARAMETER_VALUE_INVALID);
        }
        AuditStatus auditStatusFromMyself = input.getAuditResult();
        AuditStatus auditStatusFromOthers = null;
        AuditStatus auditStatus;

        // self audit passed
        if (auditStatusFromMyself == AuditStatus.agree) {

            // If it is a start-up member, pass it directly.
            if (member.isFromCreateProject()) {
                auditStatus = AuditStatus.agree;
            } else {
                auditStatus = AuditStatus.auditing;
                auditStatusFromOthers = AuditStatus.auditing;
                member.setAuditComment("自己已同意，等待其他成员审核中。");
            }
        }
        // self audit not pass
        else {
            auditStatus = AuditStatus.disagree;
            member.setAuditComment(input.getAuditComment());
            member.setFromCreateProject(false);
            member.setExited(true);
        }


        // update the audit status of the project
        if (!input.fromGateway()) {

            project.setAuditStatus(auditStatus);
            project.setAuditStatusFromMyself(auditStatusFromMyself);
            project.setAuditStatusFromOthers(auditStatusFromOthers);
            project.setAuditComment(input.getAuditComment());
            project.setStatusUpdatedTime(new Date());
            projectRepo.save(project);
        }

        // update the audit status of project members
        member.setAuditStatus(auditStatus);
        member.setAuditStatusFromMyself(auditStatusFromMyself);
        member.setAuditStatusFromOthers(auditStatusFromOthers);
        member.setUpdatedBy(input);
        projectMemberRepo.save(member);


        // If I am a newcomer, after I agree, I need other formal members to review myself.
        if (!member.isFromCreateProject() && input.getAuditResult() == AuditStatus.agree) {

            projectMemberService
                    .findListByProjectId(input.getProjectId())
                    .stream()
                    // Skip the auditor himself
                    .filter(x -> !x.getMemberId().equals(member.getMemberId()))
                    // Skip exited members
                    .filter(x -> !x.isExited())
                    // Select official members only
                    .filter(x -> AuditStatus.agree == x.getAuditStatus())
                    .forEach(x -> {
                        boolean isInviter = x.getMemberId().equals(member.getInviterId());

                        /**
                         * This person may have been pulled in, kicked out and pulled in before, so there is an audit record before.
                         * Since the database has a unique primary key setting, data cannot be added repeatedly.
                         * Make a judgment here
                         */
                        ProjectMemberAuditMySqlModel audit = projectMemberAuditService.findOne(input.getProjectId(), member.getMemberId(), x.getMemberId());

                        if (audit == null) {
                            audit = new ProjectMemberAuditMySqlModel();
                            audit.setCreatedBy(input);
                            audit.setAuditorId(x.getMemberId());
                            audit.setProjectId(input.getProjectId());
                            audit.setMemberId(member.getMemberId());
                        }
                        audit.setUpdatedBy(input);
                        audit.setAuditResult(isInviter ? AuditStatus.agree : AuditStatus.auditing);
                        audit.setAuditComment(isInviter ? "邀请人，自动通过。" : null);
                        projectMemberAuditRepository.save(audit);
                    });

            // Check if all official members have agreed
            if (projectMemberAuditService.allMemberAgreed(input.getProjectId(), member.getMemberId())) {
                projectMemberService
                        .updateProjectMember(member, (x) -> {
                            x.setAuditStatus(AuditStatus.agree);
                            x.setAuditStatusFromOthers(AuditStatus.agree);
                            x.setAuditComment("审核通过");
                            return x;
                        });

                // Update project audit status
                if (!input.fromGateway()) {
                    project.setAuditStatus(AuditStatus.agree);
                    project.setAuditStatusFromOthers(AuditStatus.agree);
                    project.setAuditComment(null);
                    project.setStatusUpdatedTime(new Date());

                    project.setExited(false);
                    projectRepo.save(project);
                }
            }
        }

        gatewayService.syncToNotExistedMembers(input.getProjectId(), input, AuditApi.class);
        if (!input.fromGateway()) {

            // Pull the latest status of the project from the promoter to the local
            if (input.getAuditResult() == AuditStatus.agree) {
                syncAuditProjectInfo(input.getProjectId(), input);
            }
        }

    }

    /**
     * after audit the project, pull the latest data from the promoter and synchronize the data.
     */
    public void syncAuditProjectInfo(String projectId, AuditApi.Input input) throws StatusCodeWithException {

        List<ProjectMemberMySqlModel> projectMembers = projectMemberService.listFormalProjectMembers(projectId);
        ProjectMemberMySqlModel promoterProjectMember = projectMembers.stream().filter(x -> x.getMemberRole() == JobMemberRole.promoter).findFirst().orElse(null);
        long promoterCount = projectMembers.stream().filter(x -> x.getMemberRole() == JobMemberRole.promoter).count();
        ProjectMySqlModel project = findByProjectId(projectId);

        if (promoterProjectMember == null) {
            throw new StatusCodeWithException(StatusCode.DATA_NOT_FOUND, "找不到promoter成员信息");
        }

        ApiResult<?> detailResult = gatewayService.sendToBoardRedirectApi(promoterProjectMember.getMemberId(), JobMemberRole.provider, new DataInfoApi.Input(projectId), DataInfoApi.class);

        DataInfoApi.Output dataInfoOutput = JSONObject.toJavaObject(JObject.create(detailResult.data), DataInfoApi.Output.class);

        for (ProjectMemberMySqlModel projectMemberMySqlModel : dataInfoOutput.getProjectMembers()) {

            // If the record exists, update the status;
            // if the record does not exist, insert the record.
            ProjectMemberMySqlModel projectMember = projectMemberService.findOneByMemberId(projectId, projectMemberMySqlModel.getMemberId(), projectMemberMySqlModel.getMemberRole());
            if (projectMember != null) {
                projectMemberService.update(projectMember, y -> {
                    y.setAuditStatus(projectMember.getAuditStatus());
                    y.setAuditStatusFromMyself(projectMember.getAuditStatusFromMyself());
                    y.setAuditStatusFromOthers(projectMember.getAuditStatusFromOthers());
                    y.setAuditComment(projectMember.getAuditComment());
                });
            } else {
                projectMemberRepo.save(projectMemberMySqlModel);
            }

        }

        for (ProjectDataSetMySqlModel dataSetMySqlModel : dataInfoOutput.getProjectDataSets()) {
            // Filter derived data sets
            if (dataSetMySqlModel.getSourceType() != null) {
                continue;
            }
            ProjectDataSetMySqlModel projectDataSet = projectDataSetService.findOne(dataSetMySqlModel.getProjectId(), dataSetMySqlModel.getDataSetId(), dataSetMySqlModel.getMemberRole());
            if (projectDataSet != null) {
                projectDataSetService.update(projectDataSet, dataSet -> {
                    dataSet.setAuditStatus(projectDataSet.getAuditStatus());
                    dataSet.setAuditComment(projectDataSet.getAuditComment());
                });

            } else {

                projectDataSetRepo.save(dataSetMySqlModel);
            }
        }
        List<String> excludeFlowIds = new ArrayList<>();
        for (ProjectFlowMySqlModel projectFlowMySqlModel : dataInfoOutput.getProjectFlows()) {

            // Eligible flow do not need to be copied
            if (promoterCount >= 2 && project.getMyRole() == JobMemberRole.promoter) {
                if (projectFlowMySqlModel.getFederatedLearningType() != FederatedLearningType.mix) {
                    excludeFlowIds.add(projectFlowMySqlModel.getFlowId());
                    continue;
                }
            }
            if (projectFlowService.findOne(projectFlowMySqlModel.getFlowId()) == null) {
                projectFlowMySqlModel.setMyRole(project.getMyRole());
                projectFlowMySqlModel.setFlowStatus(ProjectFlowStatus.editing);
                // todo: put creator_member_id on next version
                projectFlowMySqlModel.setCreatedBy("");
                projectFlowRepository.save(projectFlowMySqlModel);
            }
        }

        for (ProjectFlowNodeMySqlModel projectFlowNodeMySqlModel : dataInfoOutput.getProjectFlowNodes()) {
            // Eligible flow nodes do not need to be copied
            if (excludeFlowIds.contains(projectFlowNodeMySqlModel.getFlowId())) {
                continue;
            }
            if (projectFlowNodeService.findOne(projectFlowNodeMySqlModel.getFlowId(), projectFlowNodeMySqlModel.getNodeId()) == null) {
                projectFlowNodeRepository.save(projectFlowNodeMySqlModel);
            }
        }

    }

    /**
     * Pull the latest data of the project from the promoter
     * <p>
     * Due to changes in the project (data sets, processes, tasks) will only be synchronized to formal members
     * New members need to get data from the promoter
     */
    public void pullNewestProjectInfo(AbstractApiInput input, String projectId, String callerMemberId, JobMemberRole myRole) throws StatusCodeWithException {
        ProjectMySqlModel project = findByProjectId(projectId);
        DataInfoApi.Output projectOutputModel = getPromoterDataInfo(projectId, callerMemberId);

        // If the project does not exist, it means that it is a new member,
        // and the project and related data sets need to be created.
        if (project == null) {

            ProjectMySqlModel projectMySqlModel = projectOutputModel.getProject();

            project = new ProjectMySqlModel();
            project.setCreatedBy(input);
            project.setMemberId(CacheObjects.getMemberId());
            project.setMyRole(myRole);
            project.setProjectId(projectId);
            project.setName(projectMySqlModel.getName());
            project.setProjectDesc(projectMySqlModel.getProjectDesc());
            project.setStatusUpdatedTime(new Date());
            project.setProgress(0);
            project.setProgressUpdatedTime(new Date());
            project.setUpdatedBy(CurrentAccount.id());
            project.setAuditStatus(AuditStatus.auditing);
            project.setAuditStatusFromMyself(AuditStatus.auditing);
            project.setFlowStatusStatistics(JObject.create()
                    .append(ProjectFlowStatus.editing.name(), 0)
                    .append(ProjectFlowStatus.running.name(), 0)
                    .append(ProjectFlowStatus.finished.name(), 0).toJSONString());
            projectRepo.save(project);

            // save ProjectMember to database
            projectOutputModel.getProjectMembers()
                    .stream()
                    .filter(x -> !x.getMemberId().equals(CacheObjects.getMemberId()))
                    .forEach(x -> {
                        ProjectMemberMySqlModel member = new ProjectMemberMySqlModel();
                        member.setProjectId(x.getProjectId());
                        member.setCreatedBy(x.getCreatedBy());
                        member.setMemberId(x.getMemberId());
                        member.setMemberRole(x.getMemberRole());
                        member.setAuditComment(x.getAuditComment());
                        member.setInviterId(x.getInviterId());
                        member.setFromCreateProject(x.isFromCreateProject());
                        member.setAuditStatus(x.getMemberId().equals(CacheObjects.getMemberId()) ? AuditStatus.auditing : x.getAuditStatus());
                        member.setAuditStatusFromMyself(x.getMemberId().equals(CacheObjects.getMemberId()) ? AuditStatus.auditing : x.getAuditStatusFromMyself());
                        member.setExited(x.isExited());
                        projectMemberRepo.save(member);
                    });

            // save ProjectDataSet to database
            projectOutputModel.getProjectDataSets()
                    .stream()
                    .filter(x -> x.getSourceType() == null)
                    .forEach(x -> {
                        ProjectDataSetMySqlModel dataSet = new ProjectDataSetMySqlModel();
                        dataSet.setProjectId(x.getProjectId());
                        dataSet.setCreatedBy(x.getCreatedBy());
                        dataSet.setMemberId(x.getMemberId());
                        dataSet.setMemberRole(x.getMemberRole());
                        dataSet.setDataSetId(x.getDataSetId());
                        dataSet.setStatusUpdatedTime(x.getStatusUpdatedTime());
                        dataSet.setAuditStatus(x.getMemberId().equals(CacheObjects.getMemberId()) ? AuditStatus.auditing : x.getAuditStatus());
                        dataSet.setAuditComment(x.getMemberId().equals(CacheObjects.getMemberId()) ? "" : x.getAuditComment());
                        projectDataSetRepo.save(dataSet);
                    });

        } else {

            // The project exists, has joined the project in history.
            ProjectMySqlModel projectMySqlModel = projectOutputModel.getProject();
            Map<String, Object> projectUpdateMap = new HashMap<>();
            projectUpdateMap.put("name", projectMySqlModel.getName());
            projectUpdateMap.put("projectDesc", projectMySqlModel.getProjectDesc());
            projectUpdateMap.put("statusUpdatedTime", new Date());
            projectUpdateMap.put("progress", 0);
            projectUpdateMap.put("progressUpdatedTime", new Date());
            projectUpdateMap.put("updatedBy", CurrentAccount.id());
            projectUpdateMap.put("auditStatus", AuditStatus.auditing);
            projectUpdateMap.put("auditStatusFromMyself", AuditStatus.auditing);
            projectUpdateMap.put("auditStatusFromOthers", null);
            projectUpdateMap.put("exited", false);
            projectUpdateMap.put("myRole", myRole);
            projectRepo.updateById(project.getId(), projectUpdateMap, ProjectMySqlModel.class);

            projectOutputModel.getProjectMembers().forEach(x -> {

                ProjectMemberMySqlModel model = projectMemberService.findOneByMemberId(x.getProjectId(), x.getMemberId(), x.getMemberRole());
                if (model != null) {
                    Map<String, Object> params = new HashMap<>();
                    if (model.getMemberRole() == myRole && model.getMemberId().equals(CacheObjects.getMemberId())) {
                        params.put("auditStatus", AuditStatus.auditing);
                        params.put("auditStatusFromMyself", AuditStatus.auditing);
                        params.put("auditStatusFromOthers", null);
                        params.put("exited", false);
                        params.put("auditComment", "");
                        params.put("fromCreateProject", x.isFromCreateProject());
                    } else {
                        params.put("auditStatus", x.getAuditStatus());
                        params.put("auditStatusFromMyself", x.getAuditStatus());
                        params.put("auditStatusFromOthers", x.getAuditStatusFromOthers());
                        params.put("exited", x.isExited());
                        params.put("fromCreateProject", x.isFromCreateProject());
                        params.put("auditComment", x.getAuditComment());
                    }
                    projectMemberRepo.updateById(model.getId(), params, ProjectMemberMySqlModel.class);
                } else {
                    if (x.getMemberId().equals(CacheObjects.getMemberId())) {
                        x.setAuditStatus(AuditStatus.auditing);
                        x.setAuditStatusFromMyself(AuditStatus.auditing);
                        x.setAuditStatusFromOthers(null);
                        x.setAuditComment("");
                    }
                    projectMemberRepo.save(x);
                }

            });

            projectOutputModel.getProjectDataSets()
                    .stream()
                    .filter(x -> x.getSourceType() == null)
                    .forEach(x -> {

                        ProjectDataSetMySqlModel model = projectDataSetService.findOne(x.getProjectId(), x.getDataSetId(), x.getMemberRole());
                        if (model != null) {
                            Map<String, Object> params = new HashMap<>();
                            params.put("auditStatus",
                                    model.getMemberId().equals(CacheObjects.getMemberId())
                                            && model.getMemberRole() == myRole ? AuditStatus.auditing
                                            : model.getAuditStatus());
                            params.put("auditComment", model.getMemberId().equals(CacheObjects.getMemberId()) ? ""
                                    : model.getAuditComment());
                            projectDataSetRepo.updateById(model.getId(), params, ProjectDataSetMySqlModel.class);
                        } else {
//                            x.setAuditStatus(AuditStatus.auditing);
//                            x.setAuditComment("");
                            projectDataSetRepo.save(x);
                        }
                    });
        }

    }

    public DataInfoApi.Output getPromoterDataInfo(String projectId, String callerMemberId) throws StatusCodeWithException {
        // Get all project members from the sender
        ApiResult<?> membersResult = gatewayService.sendToBoardRedirectApi(callerMemberId, JobMemberRole.provider, new ListApi.Input(projectId), ListApi.class);

        // Find the promoter in the current project from all members of the sender
        ProjectMemberOutputModel promoterMember = JObject.create(membersResult.data)
                .getJSONList("list")
                .stream()
                .map(x -> JSONObject.toJavaObject(x, ProjectMemberOutputModel.class))
                .filter(x -> x.getMemberRole() == JobMemberRole.promoter)
                .findFirst()
                .orElse(null);

        if (promoterMember == null) {
            throw new StatusCodeWithException(StatusCode.DATA_NOT_FOUND, "找不到promoter方");
        }

        String promoterMemberId = promoterMember.getMemberId();

        // Get project details from the promoter
        ApiResult<?> detailResult = gatewayService.sendToBoardRedirectApi(promoterMemberId, JobMemberRole.provider, new DataInfoApi.Input(projectId), DataInfoApi.class);

        DataInfoApi.Output projectOutputModel = JSONObject.toJavaObject(JObject.create(detailResult.data), DataInfoApi.Output.class);

        return projectOutputModel;
    }


    /**
     * Exit the project
     */
    public void exitProject(ExitProjectApi.Input input) throws StatusCodeWithException {

        ProjectMySqlModel project = findByProjectId(input.getProjectId());
        if (project == null) {
            throw new StatusCodeWithException(StatusCode.DATA_NOT_FOUND, "找不到对应的项目。");
        }


        if (!input.fromGateway()) {
            if (project.getMyRole() == JobMemberRole.promoter) {
                throw new StatusCodeWithException("promoter 不能退出项目", StatusCode.PARAMETER_VALUE_INVALID);
            }


            project.setExited(true);
            project.setExitedTime(new Date());
            project.setExitedBy(project.getOperatorId(input));
            projectRepo.save(project);
        }

        // Notify other members
        gatewayService.syncToOtherFormalProjectMembers(input.getProjectId(), input, ExitProjectApi.class);

        String memberId = input.fromGateway() ? input.callerMemberInfo.getMemberId() : CacheObjects.getMemberId();
        ProjectMemberMySqlModel member = projectMemberService.findOneByMemberId(input.getProjectId(), memberId, JobMemberRole.provider);
        member.setExited(true);
        member.setUpdatedBy(input);
        projectMemberRepo.save(member);


        // Mark all data sets belonging to the member as unavailable
        projectDataSetRepo.disableDataSetWhenMemberExist(input.getProjectId(), memberId, member.getMemberRole().name());

        // Delete all the members' pending audit records of this member, and he no longer needs to audit.
        projectMemberAuditRepository.deleteAuditingRecord(input.getProjectId(), memberId);

        // Update the usage count of the dataset in the project
        projectDataSetService
                .listAllRawDataSet(project.getProjectId(), memberId)
                .stream()
                .forEach(x -> dataSetService.updateUsageCountInProject(x.getDataSetId()));
    }


    /**
     * close project
     */
    public void closeProject(CloseProjectApi.Input input) throws StatusCodeWithException {

        ProjectMySqlModel project = findByProjectId(input.getProjectId());
        if (project == null) {
            throw new StatusCodeWithException(StatusCode.DATA_NOT_FOUND, "找不到对应的项目。");
        }

        if (!input.fromGateway()) {
            if (project.getMyRole() != JobMemberRole.promoter || !project.getMemberId().equals(CacheObjects.getMemberId())) {
                throw new StatusCodeWithException("非发起方无法关闭项目。", StatusCode.ILLEGAL_REQUEST);
            }
        }

        OnlineDemoBranchStrategy.hackOnDelete(input, project, "只能关闭自己创建的项目。");

        project.setClosed(true);
        project.setClosedTime(new Date());
        project.setClosedBy(project.getOperatorId(input));

        projectRepo.save(project);

        projectDataSetService
                .listAllRawDataSet(project.getProjectId(), null)
                .stream()
                .forEach(x -> dataSetService.updateUsageCountInProject(x.getDataSetId()));

        // Notify other members that the project is closed
        try {
            gatewayService.syncToNotExistedMembers(input.getProjectId(), input, CloseProjectApi.class);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    /**
     * update flow_status_statistics
     */
    public void updateFlowStatusStatistics(String projectId) {

        List<Object[]> projectFlowStatusCount = projectFlowRepository.countProjectFlowStatus(projectId);

        int runningCount = 0;
        int editingCount = 0;
        int finishedCount = 0;
        for (Object[] row : projectFlowStatusCount) {
            String flowStatus = String.valueOf(row[0]);
            int count = Convert.toInt(row[1]);

            switch (ProjectFlowStatus.valueOf(flowStatus)) {
                case editing:
                    editingCount += count;
                    break;

                case running:
                case wait_run:
                case wait_stop:
                case wait_success:
                    runningCount += count;
                    break;

                case success:
                case stop_on_running:
                case error_on_running:
                    finishedCount += count;
                    break;
                default:
            }
        }
        JObject result = JObject.create()
                .append(ProjectFlowStatus.editing.name(), editingCount)
                .append(ProjectFlowStatus.running.name(), runningCount)
                .append(ProjectFlowStatus.finished.name(), finishedCount);

        ProjectMySqlModel project = findByProjectId(projectId);
        projectRepo.updateById(project.getId(), "flowStatusStatistics", result.toJSONString(), ProjectMySqlModel.class);
    }

    /**
     * find project by job_id
     */
    public ProjectMySqlModel findProjectByJobId(String jobId) {
        List<JobMySqlModel> jobs = jobService.listByJobId(jobId);

        if (CollectionUtils.isEmpty(jobs)) {
            return null;
        }

        return projectService.findByProjectId(jobs.get(0).getProjectId());
    }

}
