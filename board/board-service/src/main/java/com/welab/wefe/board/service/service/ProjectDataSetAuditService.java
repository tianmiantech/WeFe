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

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.welab.wefe.board.service.api.project.dataset.AuditDataSetApi;
import com.welab.wefe.board.service.api.project.dataset.AuditDataSetApi.Input;
import com.welab.wefe.board.service.database.entity.job.ProjectDataSetMySqlModel;
import com.welab.wefe.board.service.database.entity.job.ProjectMySqlModel;
import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.enums.AuditStatus;
import com.welab.wefe.common.exception.StatusCodeWithException;

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


    /**
     * audit data set
     */
    @Transactional(rollbackFor = Exception.class)
    public synchronized void auditDataSet(Input input) throws StatusCodeWithException {

        ProjectMySqlModel project = projectService.findByProjectId(input.getProjectId());
        if (project == null) {
            throw new StatusCodeWithException("未找到相应的项目！", StatusCode.ILLEGAL_REQUEST);
        }

        if (!input.fromGateway()) {
            if (project.getAuditStatus() != AuditStatus.agree || project.isExited()) {
                throw new StatusCodeWithException("请在成为该项目的正式成员后再进行相关操作", StatusCode.ILLEGAL_REQUEST);
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
            throw new StatusCodeWithException("请勿重复审核！", StatusCode.ILLEGAL_REQUEST);
        }

        if (!input.fromGateway()) {
            if (!CacheObjects.getMemberId().equals(dataSet.getMemberId())) {
                throw new StatusCodeWithException("你不能审核别人的数据集", StatusCode.ILLEGAL_REQUEST);
            }
        }

        projectDataSetService.update(dataSet, (x) -> x.setAuditStatus(input.getAuditStatus()));

        // Update the number of data sets used in the project
        dataSetService.updateUsageCountInProject(dataSet.getDataSetId());


        gatewayService.syncToNotExistedMembers(input.getProjectId(), input, AuditDataSetApi.class);

    }

    @Autowired
    private DataSetService dataSetService;

}
