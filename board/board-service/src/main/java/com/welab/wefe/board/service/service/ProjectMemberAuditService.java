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

import com.welab.wefe.board.service.api.project.member.audit.AuditApi;
import com.welab.wefe.board.service.database.entity.job.ProjectMemberAuditMySqlModel;
import com.welab.wefe.board.service.database.entity.job.ProjectMemberMySqlModel;
import com.welab.wefe.board.service.database.entity.job.ProjectMySqlModel;
import com.welab.wefe.board.service.database.repository.ProjectDataSetRepository;
import com.welab.wefe.board.service.database.repository.ProjectMemberAuditRepository;
import com.welab.wefe.board.service.database.repository.ProjectMemberRepository;
import com.welab.wefe.board.service.database.repository.ProjectRepository;
import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.data.mysql.Where;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.wefe.enums.AuditStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * @author zane.luo
 */
@Service
public class ProjectMemberAuditService {

    @Autowired
    ProjectRepository projectRepo;
    @Autowired
    ProjectService projectService;
    @Autowired
    ProjectMemberRepository projectMemberRepo;

    @Autowired
    ProjectMemberService projectMemberService;

    @Autowired
    ProjectDataSetRepository projectDataSetRepo;

    @Autowired
    ProjectDataSetService projectDataSetService;

    @Autowired
    GatewayService gatewayService;

    @Autowired
    ProjectMemberAuditRepository projectMemberAuditRepository;

    public List<ProjectMemberAuditMySqlModel> findAllAuditList(String projectId, String memberId) {
        Where where = Where.create().equal("projectId", projectId).equal("memberId", memberId);

        Specification<ProjectMemberAuditMySqlModel> projectMemberAuditWhere = where
                .build(ProjectMemberAuditMySqlModel.class);

        return projectMemberAuditRepository.findAll(projectMemberAuditWhere);
    }

    /**
     * Check whether all formal members in the project have agreed to add new people
     *
     * @param projectId project id
     * @param memberId  id of the audited member
     */
    public boolean allMemberAgreed(String projectId, String memberId) {
        Specification<ProjectMemberAuditMySqlModel> where = Where
                .create()
                .equal("projectId", projectId)
                .equal("memberId", memberId)
                .notEqual("auditResult", AuditStatus.agree)
                .build(ProjectMemberAuditMySqlModel.class);

        return projectMemberAuditRepository.findAll(where).isEmpty();
    }

    /**
     * audit newly added project members
     * <p>
     * new provider
     * call api: project/add/audit
     * <p>
     * other
     * project_member: result
     * project_member_audit: result
     */
    @Transactional(rollbackFor = Exception.class)
    public synchronized void auditMember(AuditApi.Input input) throws StatusCodeWithException {

        ProjectMySqlModel project = projectService.findByProjectId(input.getProjectId());
        if (project == null) {
            throw new StatusCodeWithException("未找到相应的项目！", StatusCode.ILLEGAL_REQUEST);
        }

        List<ProjectMemberMySqlModel> needAuditMembers = projectMemberService.findListByMemberId(input.getProjectId(),
                input.getMemberId());
        if (needAuditMembers == null || needAuditMembers.isEmpty()) {
            throw new StatusCodeWithException("未找到项目关联的member！", StatusCode.ILLEGAL_REQUEST);
        }

        ProjectMemberMySqlModel needAuditMember = needAuditMembers.stream()
                .filter(s -> s.getAuditStatus() == AuditStatus.auditing && !s.isExited()).findFirst().get();

        if (needAuditMember == null) {
            throw new StatusCodeWithException("未找到项目关联的member！", StatusCode.ILLEGAL_REQUEST);
        }

        String auditorId = input.fromGateway() ? input.callerMemberInfo.getMemberId() : CacheObjects.getMemberId();

        // Since there is a situation where you model yourself and yourself,
        // you may get two people here through the member Id.
        List<ProjectMemberMySqlModel> auditors = projectMemberService.findListByMemberId(input.getProjectId(), auditorId);

        if (auditors.stream().anyMatch(x -> !AuditStatus.agree.equals(x.getAuditStatus()))) {
            throw new StatusCodeWithException("只有正式成员才能对其他人进行审核", StatusCode.ILLEGAL_REQUEST);
        }

        // save audit result
        ProjectMemberAuditMySqlModel model = findOne(input.getProjectId(), input.getMemberId(), auditorId);
        model.setAuditComment(input.getAuditComment());
        model.setAuditResult(input.getAuditResult());
        model.setUpdatedBy(input);
        projectMemberAuditRepository.save(model);

        gatewayService.syncToNotExistedMembers(input.getProjectId(), input, AuditApi.class);

        // audit completed: someone rejected
        if (input.getAuditResult() == AuditStatus.disagree) {
            projectMemberService
                    .updateProjectMember(needAuditMember, (x) -> {
                        x.setAuditStatus(AuditStatus.disagree);
                        x.setAuditStatusFromOthers(AuditStatus.disagree);
                        x.setAuditComment(input.getAuditComment());
                        x.setExited(true);
                        return x;
                    });
        }
        // audit completed: all formal members agree
        else if (allMemberAgreed(input.getProjectId(), input.getMemberId())) {
            projectMemberService
                    .updateProjectMember(needAuditMember, (x) -> {
                        x.setAuditStatus(AuditStatus.agree);
                        x.setAuditStatusFromOthers(AuditStatus.agree);
                        x.setAuditComment("审核通过");
                        x.setExited(false);
                        return x;
                    });

            // Update project audit status
            if (CacheObjects.getMemberId().equals(needAuditMember.getMemberId())) {
                project.setAuditStatus(AuditStatus.agree);
                project.setAuditStatusFromOthers(AuditStatus.agree);
                project.setAuditComment(null);
                project.setStatusUpdatedTime(new Date());
                projectRepo.save(project);
            }
        }

    }


    /**
     * Get an audit record
     */
    public ProjectMemberAuditMySqlModel findOne(String projectId, String memberId, String auditorId) {
        return projectMemberAuditRepository
                .findOne(
                        Where
                                .create()
                                .equal("projectId", projectId)
                                .equal("memberId", memberId)
                                .equal("auditorId", auditorId)
                                .build(ProjectMemberAuditMySqlModel.class)
                )
                .orElse(null);
    }

    /**
     * Get the review status of new members in the specified project
     */
    public List<ProjectMemberAuditMySqlModel> listAll(String projectId, String memberId) {
        return projectMemberAuditRepository
                .findAll(
                        Where
                                .create()
                                .equal("projectId", projectId)
                                .equal("memberId", memberId)
                                .build(ProjectMemberAuditMySqlModel.class)
                );
    }
}
