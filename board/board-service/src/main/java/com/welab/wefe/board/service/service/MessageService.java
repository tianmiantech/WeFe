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

package com.welab.wefe.board.service.service;


import com.welab.wefe.board.service.api.message.QueryApi;
import com.welab.wefe.board.service.database.entity.MessageMysqlModel;
import com.welab.wefe.board.service.database.entity.job.ProjectDataSetMySqlModel;
import com.welab.wefe.board.service.database.entity.job.ProjectMemberAuditMySqlModel;
import com.welab.wefe.board.service.database.entity.job.ProjectMySqlModel;
import com.welab.wefe.board.service.database.repository.MessageRepository;
import com.welab.wefe.board.service.dto.base.PagingOutput;
import com.welab.wefe.board.service.dto.entity.MessageOutputModel;
import com.welab.wefe.board.service.dto.entity.data_resource.output.DataResourceOutputModel;
import com.welab.wefe.board.service.dto.vo.message.*;
import com.welab.wefe.board.service.service.data_resource.DataResourceService;
import com.welab.wefe.common.data.mysql.Where;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.util.StringUtil;
import com.welab.wefe.common.wefe.enums.AuditStatus;
import com.welab.wefe.common.wefe.enums.JobMemberRole;
import com.welab.wefe.common.wefe.enums.MessageEvent;
import com.welab.wefe.common.wefe.enums.ProducerType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

/**
 * @author Zane
 */
@Service
public class MessageService extends AbstractService {

    @Autowired
    private MessageRepository messageRepository;
    @Autowired
    private DataResourceService dataResourceService;
    @Autowired
    private ProjectMemberAuditService projectMemberAuditService;

    public PagingOutput<MessageOutputModel> query(QueryApi.Input input) {

        Specification<MessageMysqlModel> where = Where
                .create()
                .equal("todo", input.todo)
                .equal("todoComplete", input.todoComplete)
                .equal("level", input.level)
                .equal("unread", input.unread)
                .in("event", input.eventList)
                .build(MessageMysqlModel.class);

        return messageRepository.paging(where, input, MessageOutputModel.class);
    }

    public void read(String id) {
        messageRepository.updateById(id, "unread", false, MessageMysqlModel.class);
    }

    public void add(MessageEvent event, String title, String content) {
        TextMessageContent messageContent = new TextMessageContent();
        messageContent.message = content;
        add(event, title, content);
    }

    public void add(MessageEvent event, AbstractMessageContent content) {
        add(event, null, content, null);
    }

    public void add(MessageEvent event, AbstractMessageContent content, boolean isTodo) {
        add(event, null, content, isTodo);
    }

    public void add(MessageEvent event, String title, AbstractMessageContent content, Boolean isTodo) {
        if (StringUtil.isEmpty(title)) {
            title = content.getTitle();
        }

        if (StringUtil.isEmpty(title)) {
            throw new RuntimeException("你怎么能不写标题呢~");
        }

        if (isTodo == null) {
            isTodo = event.isTodo();
        }

        MessageMysqlModel model = new MessageMysqlModel();
        model.setEvent(event);
        model.setProducer(ProducerType.board);
        model.setLevel(event.getLevel());
        model.setTitle(title);
        model.setContent(content.toString());
        model.setUnread(true);
        model.setTodo(isTodo);
        model.setTodoComplete(false);
        model.setTodoRelatedId1(content.getRelatedId1());
        model.setTodoRelatedId2(content.getRelatedId2());

        messageRepository.save(model);
    }

    public void completeApplyDataResourceTodo(ProjectDataSetMySqlModel projectDataSet) {
        String projectId = projectDataSet.getProjectId();
        String dataSetId = projectDataSet.getDataSetId();
        messageRepository.completeApplyDataResourceTodo(projectId, dataSetId);
    }

    public void completeApplyJoinProjectTodo(String projectId) {
        messageRepository.completeApplyJoinProjectTodo(projectId);
    }

    /**
     * 添加一条 event 为 ApplyDataResource 的消息     *
     */
    public void addApplyDataResourceMessage(String fromMemberId, ProjectMySqlModel project, ProjectDataSetMySqlModel projectDataSet) throws StatusCodeWithException {

        DataResourceOutputModel dataResource = dataResourceService.findDataResourceFromLocalOrUnion(projectDataSet);

        if (project == null || dataResource == null) {
            return;
        }

        ApplyDataResourceMessageContent content = new ApplyDataResourceMessageContent();
        content.fromMemberId = fromMemberId;
        content.projectId = project.getProjectId();
        content.projectName = project.getName();
        content.dataResourceName = dataResource.getName();
        content.dataResourceType = dataResource.getDataResourceType();
        content.dataResourceId = dataResource.getDataResourceId();
        content.sampleCount = dataResource.getTotalDataCount();
        add(MessageEvent.ApplyDataResource, content);
    }

    public void addAuditDataResourceMessage(String fromMemberId, ProjectMySqlModel project, ProjectDataSetMySqlModel projectDataResource, AuditStatus auditStatus, String auditComment) throws StatusCodeWithException {
        DataResourceOutputModel dataResource = dataResourceService.findDataResourceFromLocalOrUnion(projectDataResource);
        if (dataResource == null) {
            return;
        }
        AuditApplyDataResourceMessageContent content = new AuditApplyDataResourceMessageContent();
        content.fromMemberId = fromMemberId;
        content.projectId = project.getProjectId();
        content.projectName = project.getName();
        content.dataResourceName = dataResource.getName();
        content.dataResourceType = dataResource.getDataResourceType();
        content.dataResourceId = dataResource.getDataResourceId();
        content.sampleCount = dataResource.getTotalDataCount();
        content.auditStatus = auditStatus;
        content.auditComment = auditComment;

        MessageEvent event = auditStatus == AuditStatus.agree
                ? MessageEvent.AgreeApplyDataResource
                : MessageEvent.DisagreeApplyDataResource;
        add(event, content);

    }

    /**
     * 添加一条 event 为 ApplyJoinProject 的消息
     */
    public void addApplyJoinProjectMessage(String fromMemberId, String projectId, String projectName) {
        ApplyJoinProjectMessageContent content = new ApplyJoinProjectMessageContent();
        content.fromMemberId = fromMemberId;
        content.projectId = projectId;
        content.projectName = projectName;
        add(MessageEvent.ApplyJoinProject, content);
    }

    /**
     * 添加一条 event 为 AgreeJoinProject/DisagreeJoinProject 的消息
     */
    public void addAuditJoinProjectMessage(String fromMemberId, ProjectMySqlModel project, AuditStatus auditStatus, String auditComment) {

        /**
         * 【拒绝】
         * 1. 知会 promoter
         *
         * 【同意】
         * 1. 知会 promoter
         * 2. 提醒 provider 审核
         */
        AuditJoinProjectMessageContent content = null;
        boolean isTodo = false;

        // 如果被邀请人同意加入项目
        if (auditStatus == AuditStatus.agree) {
            // 如果需要我对其进行二次审核
            ProjectMemberAuditMySqlModel memberAudit = projectMemberAuditService.findOne(project.getProjectId(), fromMemberId, CacheObjects.getMemberId());
            if (memberAudit != null && memberAudit.getAuditResult() == AuditStatus.auditing) {
                content = new AuditJoinProjectMessageContent(memberAudit);
                isTodo = true;
            }
        }

        // 不需要我审核的情况
        if (content == null) {
            // 如果我是 promoter，知会。
            if (project.getMyRole() == JobMemberRole.promoter) {
                content = new AuditJoinProjectMessageContent();
                isTodo = false;
            }
            // 不需要知会
            else {
                return;
            }
        }

        content.fromMemberId = fromMemberId;
        content.projectId = project.getProjectId();
        content.projectName = project.getName();
        content.auditStatus = auditStatus;
        content.auditComment = auditComment;

        MessageEvent event = auditStatus == AuditStatus.agree
                ? MessageEvent.AgreeJoinProject
                : MessageEvent.DisagreeJoinProject;

        add(event, content, isTodo);
    }
}
