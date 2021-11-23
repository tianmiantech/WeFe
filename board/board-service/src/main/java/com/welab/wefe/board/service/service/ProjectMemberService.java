/**
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

import com.welab.wefe.board.service.api.project.member.AddApi;
import com.welab.wefe.board.service.api.project.member.ListInProjectApi;
import com.welab.wefe.board.service.database.entity.job.*;
import com.welab.wefe.board.service.database.repository.ProjectMemberRepository;
import com.welab.wefe.board.service.dto.entity.ProjectMemberInput;
import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.data.mysql.Where;
import com.welab.wefe.common.enums.AuditStatus;
import com.welab.wefe.common.enums.FederatedLearningType;
import com.welab.wefe.common.enums.JobMemberRole;
import com.welab.wefe.common.enums.OrderBy;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.util.StringUtil;
import com.welab.wefe.common.web.CurrentAccount;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author zane.luo
 */
@Service
public class ProjectMemberService {

    protected final Logger LOG = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private ProjectMemberRepository projectMemberRepo;
    @Autowired
    private ProjectService projectService;
    @Autowired
    private GatewayService gatewayService;
    @Autowired
    private ProjectDataSetService projectDataSetService;
    @Autowired
    private JobService jobService;
    @Autowired
    private JobMemberService jobMemberService;

    /**
     * Add members to an existing project
     * <p>
     * adder
     * project_member: auditing
     * project_member_audit: agree
     * <p>
     * exist member
     * project_member: auditing
     * project_member_audit: auditing
     * <p>
     * new member
     * project: auditing
     * project_member: auditing
     */
    //@Transactional(rollbackFor = Exception.class)
    public synchronized void addMember(AddApi.Input input) throws StatusCodeWithException {

        List<ProjectMemberMySqlModel> members = findListByProjectId(input.getProjectId());

        for (ProjectMemberInput item : input.getMemberList()) {
            if (checkExistMember(item.getMemberId(), members)) {
                throw new StatusCodeWithException("您此次添加的成员 " + CacheObjects.getMemberName(item.getMemberId()) + " 已在该项目中，不可重复添加", StatusCode.PARAMETER_VALUE_INVALID);
            }
            JobMemberRole role = item.getMemberRole();
            if (role.equals(JobMemberRole.provider)) {
                addProviderMember(input, item);
            } else if (role.equals(JobMemberRole.promoter)) {
                addPromoterMember(input, item);
            }
        }
        members = findListByProjectId(input.getProjectId());
        if (!checkMembers(members)) {
            throw new StatusCodeWithException("改变项目类型时不允许有重复成员存在。", StatusCode.PARAMETER_VALUE_INVALID);
        }
        /**
         * Notify other members that there are new members waiting to join
         */
        gatewayService.syncToNotExistedMembers(input.getProjectId(), input, AddApi.class);

    }

    /**
     * @see AddApi.Input#checkAndStandardize()
     */
    private boolean checkExistMember(String currentMemberId, List<ProjectMemberMySqlModel> members) {
        List<ProjectMemberMySqlModel> promoters = members.stream()
                .filter(x -> x.getMemberRole() == JobMemberRole.promoter && !x.isExited()).collect(Collectors.toList());
        Set<String> promoterIds = new HashSet<>();
        promoters.forEach(p -> promoterIds.add(p.getMemberId()));
        if (promoters.size() != promoterIds.size()) {
            return true;
        }
        boolean mixFlag = promoters.size() >= 2;
        for (ProjectMemberMySqlModel m : members) {
            if (m.getMemberId().equals(currentMemberId) && mixFlag && !m.isExited()) {
                return true;
            }
        }
        return false;
    }

    private boolean checkMembers(List<ProjectMemberMySqlModel> members) {
        members = members.stream().filter(x -> !x.isExited() && x.getAuditStatus() != AuditStatus.disagree)
                .collect(Collectors.toList());
        List<ProjectMemberMySqlModel> promoters = members.stream()
                .filter(x -> x.getMemberRole() == JobMemberRole.promoter).collect(Collectors.toList());
        Set<String> mIds = new HashSet<>();
        members.forEach(m -> mIds.add(m.getMemberId()));
        boolean mixFlag = promoters.size() >= 2;
        if (mixFlag && mIds.size() != members.size()) {
            return false;
        }
        return true;

    }

    private void addPromoterMember(AddApi.Input input, ProjectMemberInput item) throws StatusCodeWithException {

        ProjectMemberMySqlModel member = findOneByMemberId(input.getProjectId(), item.getMemberId(),
                JobMemberRole.promoter);
        if (member == null) {

            member = new ProjectMemberMySqlModel();
            member.setProjectId(input.getProjectId());
            member.setCreatedBy(input);
            member.setMemberId(item.getMemberId());
            member.setMemberRole(JobMemberRole.promoter);
        }
        AuditStatus auditStatus = AuditStatus.auditing;

        member.setExited(false);
        member.setAuditStatus(auditStatus);
        member.setAuditStatusFromMyself(auditStatus);
        member.setAuditStatusFromOthers(null);
        member.setAuditComment("已邀请，等待 " + CacheObjects.getMemberName(member.getMemberId()) + " 同意加入。");
        member.setInviterId(input.fromGateway() ? input.callerMemberInfo.getMemberId() : CacheObjects.getMemberId());
        projectMemberRepo.save(member);

        // The member’s history has been added to the project,
        // and the historical data set status is unavailable. At this time,
        // the review status of the member’s data set needs to be updated.
        List<ProjectDataSetMySqlModel> dataSets = projectDataSetService.findDataSetList(input.getProjectId(),
                item.getMemberId(), item.getMemberRole());
        AuditStatus finalAuditStatus = auditStatus;
        dataSets.forEach(dataSet -> projectDataSetService.update(dataSet, x -> {
            x.setAuditStatus(finalAuditStatus);
            x.setAuditComment("");
        }));

        // If I was invited, I have to create/update the project.
        if (CacheObjects.getMemberId().equals(member.getMemberId()) && input.fromGateway()) {
            projectService.pullNewestProjectInfo(
                    input,
                    input.getProjectId(),
                    input.callerMemberInfo.getMemberId(),
                    JobMemberRole.promoter
            );
        }
    }

    private void addProviderMember(AddApi.Input input, ProjectMemberInput item) throws StatusCodeWithException {

        ProjectMemberMySqlModel member = findOneByMemberId(input.getProjectId(), item.getMemberId(),
                JobMemberRole.provider);

        if (member == null) {

            member = new ProjectMemberMySqlModel();
            member.setProjectId(input.getProjectId());
            member.setCreatedBy(input);
            member.setMemberId(item.getMemberId());
            member.setMemberRole(JobMemberRole.provider);
        }

        AuditStatus auditStatus = AuditStatus.auditing;

        // If other members pull member
        if (input.fromGateway()) {
            // If the added member is the provider identity of the promoter, the review status is passed.
            if (input.callerMemberInfo.getMemberRole() == JobMemberRole.promoter) {
                if (input.callerMemberInfo.getMemberId().equals(member.getMemberId())) {
                    auditStatus = AuditStatus.agree;
                }
            }
        }
        // If I pull member by myself
        else {
            ProjectMySqlModel project = projectService.findByProjectId(input.getProjectId());
            // If the added member is the provider identity of the promoter, the review status is passed.
            auditStatus = project.getMemberId().equals(member.getMemberId()) ? AuditStatus.agree : AuditStatus.auditing;
        }

        member.setExited(false);
        member.setAuditStatus(auditStatus);
        member.setAuditStatusFromMyself(auditStatus);
        member.setAuditStatusFromOthers(null);
        member.setAuditComment("已邀请，等待 " + CacheObjects.getMemberName(member.getMemberId()) + " 同意加入。");
        member.setInviterId(input.fromGateway() ? input.callerMemberInfo.getMemberId() : CacheObjects.getMemberId());
        projectMemberRepo.save(member);

        // The member’s history has been added to the project,
        // and the historical data set status is unavailable. At this time,
        // the review status of the member’s data set needs to be updated.
        List<ProjectDataSetMySqlModel> dataSets = projectDataSetService.findDataSetList(input.getProjectId(),
                item.getMemberId(), item.getMemberRole());
        AuditStatus finalAuditStatus = auditStatus;
        dataSets.forEach(dataSet -> projectDataSetService.update(dataSet, x -> {
            x.setAuditStatus(finalAuditStatus);
            x.setAuditComment("");
        }));

        // If I was invited, I have to create/update the project
        if (CacheObjects.getMemberId().equals(member.getMemberId()) && input.fromGateway()) {
            projectService.pullNewestProjectInfo(
                    input,
                    input.getProjectId(),
                    input.callerMemberInfo.getMemberId(),
                    JobMemberRole.provider
            );
        }
    }

    /**
     * Get the list of official members in the project
     */
    public List<ProjectMemberMySqlModel> listFormalProjectMembers(String projectId) {
        Specification<ProjectMemberMySqlModel> where = Where
                .create()
                .equal("projectId", projectId)
                .equal("auditStatus", AuditStatus.agree)
                .equal("exited", false)
                .build(ProjectMemberMySqlModel.class);

        return projectMemberRepo.findAll(where);
    }

    /**
     * Query ProjectMember based on the combination of conditions
     */
    public List<ProjectMemberMySqlModel> findListByProjectId(String projectId) {
        Specification<ProjectMemberMySqlModel> where = Where
                .create()
                .equal("projectId", projectId)
                .orderBy("createdTime", OrderBy.asc)
                .build(ProjectMemberMySqlModel.class);

        return projectMemberRepo.findAll(where);
    }


    /**
     * Get information about a single project member
     */
    public ProjectMemberMySqlModel findOneByMemberId(String projectId, String memberId, JobMemberRole role) {

        return projectMemberRepo.findOne(
                Where
                        .create()
                        .equal("projectId", projectId)
                        .equal("memberId", memberId)
                        .equal("memberRole", role)
                        .build(ProjectMemberMySqlModel.class)

        ).orElse(null);
    }

    /**
     * Get member list based on memberId
     * <p>
     * Note: Due to the fact that you and your own federation are modeled,
     * you may get the list according to the memberId.
     */
    public List<ProjectMemberMySqlModel> findListByMemberId(String projectId, String memberId) {

        return projectMemberRepo.findAll(
                Where
                        .create()
                        .equal("projectId", projectId)
                        .equal("memberId", memberId)
                        .build(ProjectMemberMySqlModel.class)

        );
    }

    /**
     * Update some fields of the projectMember table
     */
    public ProjectMemberMySqlModel updateProjectMember(ProjectMemberMySqlModel projectMemberMySqlModel,
                                                       Function<ProjectMemberMySqlModel, ProjectMemberMySqlModel> func) {
        if (projectMemberMySqlModel == null) {
            return null;
        }

        projectMemberMySqlModel = func.apply(projectMemberMySqlModel);
        projectMemberMySqlModel.setUpdatedBy(CurrentAccount.id());

        return projectMemberRepo.save(projectMemberMySqlModel);
    }

    public ProjectMemberMySqlModel update(ProjectMemberMySqlModel projectMemberMySqlModel, Consumer<ProjectMemberMySqlModel> func) {
        if (projectMemberMySqlModel == null) {
            return null;
        }

        func.accept(projectMemberMySqlModel);
        projectMemberMySqlModel.setUpdatedBy(CurrentAccount.id());

        return projectMemberRepo.save(projectMemberMySqlModel);

    }

    public List<ProjectMemberMySqlModel> findList(ListInProjectApi.Input input) throws StatusCodeWithException {
        List<ProjectMemberMySqlModel> projectMemberMySqlModelList = findListByProjectId(input.getProjectId());
        if (StringUtil.isEmpty(input.getOotJobId())) {
            return projectMemberMySqlModelList;
        }

        if (CollectionUtils.isEmpty(projectMemberMySqlModelList)) {
            throw new StatusCodeWithException("找不到项目的任何参与方信息。", StatusCode.DATA_NOT_FOUND);
        }
        JobMySqlModel jobMySqlModel = jobService.findByJobId(input.getOotJobId(), JobMemberRole.promoter);
        if (null == jobMySqlModel) {
            throw new StatusCodeWithException("找不到原流程任务信息。", StatusCode.DATA_NOT_FOUND);
        }

        List<JobMemberMySqlModel> jobMemberMySqlModelList = jobMemberService.findListByJobId(input.getOotJobId());
        if (CollectionUtils.isEmpty(jobMemberMySqlModelList)) {
            throw new StatusCodeWithException("找不到原流程任何参与方信息。", StatusCode.DATA_NOT_FOUND);
        }

        List<ProjectMemberMySqlModel> resultList = new ArrayList<>();
        for (JobMemberMySqlModel jobMemberMySqlModel : jobMemberMySqlModelList) {
            if (jobMemberMySqlModel.getJobRole() == JobMemberRole.arbiter) {
                continue;
            }
            String memberId = jobMemberMySqlModel.getMemberId();
            ProjectMemberMySqlModel projectMemberMySqlModel = projectMemberMySqlModelList.stream().
                    filter(x -> x.getMemberId().equals(memberId)).findFirst().orElse(null);
            if (FederatedLearningType.vertical.equals(jobMySqlModel.getFederatedLearningType())) {
                if (null == projectMemberMySqlModel || projectMemberMySqlModel.isExited()) {
                    throw new StatusCodeWithException("成员: " + CacheObjects.getMemberName(memberId) + " 已退出, 禁止打分验证", StatusCode.DATA_NOT_FOUND);
                }
            }
            if (null != projectMemberMySqlModel) {
                resultList.add(projectMemberMySqlModel);
            }
        }

        return resultList;
    }


}
