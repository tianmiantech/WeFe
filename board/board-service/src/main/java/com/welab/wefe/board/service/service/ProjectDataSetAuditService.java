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

import com.welab.wefe.board.service.api.project.dataset.AuditDataSetApi;
import com.welab.wefe.board.service.api.project.dataset.AuditDataSetApi.Input;
import com.welab.wefe.board.service.database.entity.job.ProjectDataSetMySqlModel;
import com.welab.wefe.board.service.database.entity.job.ProjectMySqlModel;
import com.welab.wefe.board.service.service.data_resource.table_data_set.TableDataSetService;
import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.wefe.enums.AuditStatus;
import com.welab.wefe.common.wefe.enums.JobMemberRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author zane.luo
 */
@Service
public class ProjectDataSetAuditService extends AbstractService {

    @Autowired
    ProjectService projectService;

    @Autowired
    ProjectMemberService projectMemberService;

    @Autowired
    ProjectDataSetService projectDataSetService;
    @Autowired
    private MessageService messageService;


    /**
     * audit data set
     */
    @Transactional(rollbackFor = Exception.class)
    public synchronized void auditDataSet(Input input) throws StatusCodeWithException {

        ProjectMySqlModel project = projectService.findByProjectId(input.getProjectId());
        if (project == null) {
            throw new StatusCodeWithException(StatusCode.ILLEGAL_REQUEST, "未找到相应的项目！");
        }

        if (!input.fromGateway()) {
            if (project.getAuditStatus() != AuditStatus.agree || project.isExited()) {
                throw new StatusCodeWithException(StatusCode.ILLEGAL_REQUEST, "请在成为该项目的正式成员后再进行相关操作");
            }
        }

        List<ProjectDataSetMySqlModel> dataSets = projectDataSetService.findAll(input.getProjectId(),
                input.getDataSetId());
        if (dataSets == null || dataSets.isEmpty()) {
            return;
        }
        ProjectDataSetMySqlModel dataSet = dataSets.stream().filter(d -> d.getAuditStatus() == AuditStatus.auditing)
                .findFirst().orElse(null);

        if (dataSet == null || dataSet.getAuditStatus() != AuditStatus.auditing) {
            throw new StatusCodeWithException(StatusCode.ILLEGAL_REQUEST, "请勿重复审核！");
        }

        if (!input.fromGateway()) {
            if (!CacheObjects.getMemberId().equals(dataSet.getMemberId())) {
                throw new StatusCodeWithException(StatusCode.ILLEGAL_REQUEST, "你不能审核别人的数据集");
            }

            messageService.completeApplyDataResourceTodo(dataSet);
        }

        projectDataSetService.update(dataSet, (x) -> x.setAuditStatus(input.getAuditStatus()));

        // Update the number of data sets used in the project
        tableDataSetService.updateUsageCountInProject(dataSet.getDataSetId());

        // 如果我方是 promoter，添加一条提醒消息。
        if (input.fromGateway() && project.getMyRole() == JobMemberRole.promoter) {
            messageService.addAuditDataResourceMessage(
                    input.callerMemberInfo.getMemberId(),
                    project,
                    dataSet,
                    input.getAuditStatus(),
                    input.getAuditComment()
            );
        }

        gatewayService.syncToNotExistedMembers(input.getProjectId(), input, AuditDataSetApi.class);

    }

    @Autowired
    private TableDataSetService tableDataSetService;

}
