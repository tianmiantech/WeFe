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
import com.welab.wefe.board.service.database.entity.data_resource.DataResourceMysqlModel;
import com.welab.wefe.board.service.database.entity.job.ProjectDataSetMySqlModel;
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
        add(event, null, content);
    }

    public void add(MessageEvent event, String title, AbstractMessageContent content) {
        if (StringUtil.isEmpty(title)) {
            title = content.getTitle();
        }

        if (StringUtil.isEmpty(title)) {
            throw new RuntimeException("你怎么能不写标题呢~");
        }

        MessageMysqlModel model = new MessageMysqlModel();
        model.setEvent(event);
        model.setProducer(ProducerType.board);
        model.setLevel(event.getLevel());
        model.setTitle(title);
        model.setContent(content.toString());
        model.setUnread(true);
        model.setTodo(event.isTodo());
        model.setTodoComplete(false);
        model.setTodoRelatedId1(content.getRelatedId1());
        model.setTodoRelatedId2(content.getRelatedId2());

        messageRepository.save(model);
    }

    /**
     * 添加一条 event 为 ApplyDataResource 的消息     *
     */
    public void addApplyDataResourceMessage(String fromMemberId, ProjectMySqlModel project, String dataResourceId) {

        DataResourceMysqlModel dataResource = dataResourceService.findOneById(dataResourceId);

        if (project == null || dataResource == null) {
            return;
        }

        ApplyDataResourceMessageContent content = new ApplyDataResourceMessageContent();
        content.fromMemberId = fromMemberId;
        content.projectId = project.getProjectId();
        content.projectName = project.getName();
        content.dataResourceName = dataResource.getName();
        content.dataResourceType = dataResource.getDataResourceType();
        content.dataResourceId = dataResourceId;
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

        content.auditStatus = auditStatus;
        content.auditComment = auditComment;

        MessageEvent event = auditStatus == AuditStatus.agree
                ? MessageEvent.AgreeApplyDataResource
                : MessageEvent.DisagreeApplyDataResource;
        add(event, content);

    }

    /**
     * 添加一条 event 为 CreateProject 的消息
     */
    public void addCreateProjectMessage(String fromMemberId, String projectId, String projectName) {
        CreateProjectMessageContent content = new CreateProjectMessageContent();
        content.fromMemberId = fromMemberId;
        content.projectId = projectId;
        content.projectName = projectName;
        add(MessageEvent.CreateProject, content);
    }

    /**
     * 添加一条 event 为 AgreeJoinProject/DisagreeJoinProject 的消息
     */
    public void addAuditJoinProjectMessage(String fromMemberId, ProjectMySqlModel project, AuditStatus auditStatus, String auditComment) {
        AuditJoinProjectMessageContent content = new AuditJoinProjectMessageContent();
        content.fromMemberId = fromMemberId;
        content.projectId = project.getProjectId();
        content.projectName = project.getName();
        content.auditStatus = auditStatus;
        content.auditComment = auditComment;

        MessageEvent event = auditStatus == AuditStatus.agree
                ? MessageEvent.AgreeJoinProject
                : MessageEvent.DisagreeJoinProject;

        add(event, content);
    }
}
