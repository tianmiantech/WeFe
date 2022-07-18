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
package com.welab.wefe.board.service.dto.vo.message;

import com.welab.wefe.board.service.database.entity.job.ProjectMemberAuditMySqlModel;
import com.welab.wefe.common.wefe.enums.AuditStatus;

/**
 * 消息体：审核加入项目
 *
 * @author zane
 * @date 2022/6/7
 */
public class AuditJoinProjectMessageContent extends ApplyJoinProjectMessageContent {
    public AuditStatus auditStatus;
    public String auditComment;
    /**
     * 我对被邀请成员的审核记录
     * <p>
     * 1. 如果被邀请的成员是创建项目时的初创成员，则不会有此记录。
     * 2. 如果是项目已经创建后邀请的成员，则在被邀请成员自己同意后，还需要其他正式成员的审核。
     */
    private ProjectMemberAuditMySqlModel needMeAuditRecord;

    public AuditJoinProjectMessageContent() {
    }

    public AuditJoinProjectMessageContent(ProjectMemberAuditMySqlModel needMeAuditRecord) {
        this.needMeAuditRecord = needMeAuditRecord;
    }

    @Override
    public String getTitle() {
        String status = auditStatus == AuditStatus.agree
                ? "同意"
                : "拒绝";

        // 如果需要我进行二次审核。
        if (needMeAuditRecord != null) {
            return "请审核成员【" + getFromMemberName() +
                    "】加入项目【" + projectName + "】的请求";
        } else {
            return "成员【" + getFromMemberName() +
                    "】已" + status + "加入项目【" + projectName + "】";
        }


    }
}
