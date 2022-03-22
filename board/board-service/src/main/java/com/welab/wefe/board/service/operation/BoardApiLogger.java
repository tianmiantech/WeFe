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

package com.welab.wefe.board.service.operation;

import com.welab.wefe.board.service.api.account.CaptchaApi;
import com.welab.wefe.board.service.api.data_resource.upload_task.DataResourceUploadTaskDetailApi;
import com.welab.wefe.board.service.api.data_resource.upload_task.DataResourceUploadTaskQueryApi;
import com.welab.wefe.board.service.api.file.FileUploadApi;
import com.welab.wefe.board.service.api.file.MergeApi;
import com.welab.wefe.board.service.api.member.MemberAvailableCheckApi;
import com.welab.wefe.board.service.api.project.flow.FlowQueryApi;
import com.welab.wefe.board.service.api.project.job.GetJobProgressApi;
import com.welab.wefe.board.service.api.project.job.task.TaskProgressDetailApi;
import com.welab.wefe.board.service.api.project.member.audit.ProjectMemberAuditListApi;
import com.welab.wefe.board.service.api.service.ServiceAvailableApi;
import com.welab.wefe.board.service.database.entity.OperationLogMysqlModel;
import com.welab.wefe.board.service.database.repository.AccountRepository;
import com.welab.wefe.board.service.database.repository.OperationLogRepository;
import com.welab.wefe.common.web.Launcher;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.delegate.api_log.AbstractApiLogger;
import com.welab.wefe.common.web.delegate.api_log.ApiLog;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * @author zane
 **/
@Component
public class BoardApiLogger extends AbstractApiLogger {

    @Override
    protected List<Class<? extends AbstractApi>> getIgnoreLogApiList() {
        return Arrays.asList(
                CaptchaApi.class,
                FlowQueryApi.class,
                ProjectMemberAuditListApi.class,
                GetJobProgressApi.class,
                ServiceAvailableApi.class,
                MemberAvailableCheckApi.class,
                TaskProgressDetailApi.class,
                DataResourceUploadTaskQueryApi.class,
                DataResourceUploadTaskDetailApi.class,
                FileUploadApi.class,
                MergeApi.class
        );
    }

    @Override
    protected void save(ApiLog apiLog) throws Exception {
        OperationLogMysqlModel model = new OperationLogMysqlModel();
        model.setRequestTime(apiLog.getRequestTime());
        model.setRequestIp(apiLog.getCallerIp());
        model.setOperatorId(apiLog.getCallerId());
        model.setSpend(apiLog.getSpend());
        model.setLogInterface(apiLog.getApiName());
        model.setResultCode(apiLog.getResponseCode());
        model.setResultMessage(apiLog.getResponseMessage());

        Launcher.getBean(OperationLogRepository.class).save(model);
    }

    @Override
    protected void updateAccountLastActionTime(String accountId) throws Exception {
        Launcher.getBean(AccountRepository.class).updateLastActionTime(accountId);
    }

}
