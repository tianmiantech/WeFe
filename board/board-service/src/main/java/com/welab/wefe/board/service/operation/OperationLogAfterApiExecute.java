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

import com.alibaba.fastjson.JSONObject;
import com.welab.wefe.board.service.api.data_resource.upload_task.DataResourceUploadTaskDetailApi;
import com.welab.wefe.board.service.api.data_resource.upload_task.DataResourceUploadTaskQueryApi;
import com.welab.wefe.board.service.api.file.FileUploadApi;
import com.welab.wefe.board.service.api.member.MemberAvailableCheckApi;
import com.welab.wefe.board.service.api.operation.LogQueryApi;
import com.welab.wefe.board.service.api.project.flow.FlowQueryApi;
import com.welab.wefe.board.service.api.project.job.GetJobProgressApi;
import com.welab.wefe.board.service.api.project.job.task.TaskProgressDetailApi;
import com.welab.wefe.board.service.api.project.member.audit.ProjectMemberAuditListApi;
import com.welab.wefe.board.service.api.service.ServiceAvailableApi;
import com.welab.wefe.board.service.database.entity.OperationLogMysqlModel;
import com.welab.wefe.board.service.database.repository.OperationLogRepository;
import com.welab.wefe.common.CommonThreadPool;
import com.welab.wefe.common.util.StringUtil;
import com.welab.wefe.common.web.CurrentAccount;
import com.welab.wefe.common.web.CurrentAccount.Info;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.common.web.function.AfterApiExecuteFunction;
import com.welab.wefe.common.web.util.HttpServletRequestUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Date;

/**
 * @author eval
 **/
@Component
public class OperationLogAfterApiExecute implements AfterApiExecuteFunction {

    @Autowired
    OperationLogRepository mOperationLogRepository;

    @Override
    public void action(HttpServletRequest httpServletRequest, long start, AbstractApi<?, ?> api, JSONObject params, ApiResult<?> result) {
        final Info info = CurrentAccount.get();
        CommonThreadPool.run(
                () -> log(httpServletRequest, start, api, result, info)
        );
    }

    private static final String[] IGNORE_LOG_APIS = {
            FlowQueryApi.class.getAnnotation(Api.class).path(),
            ProjectMemberAuditListApi.class.getAnnotation(Api.class).path(),
            GetJobProgressApi.class.getAnnotation(Api.class).path(),
            ServiceAvailableApi.class.getAnnotation(Api.class).path(),
            MemberAvailableCheckApi.class.getAnnotation(Api.class).path(),
            TaskProgressDetailApi.class.getAnnotation(Api.class).path(),
            DataResourceUploadTaskQueryApi.class.getAnnotation(Api.class).path(),
            DataResourceUploadTaskDetailApi.class.getAnnotation(Api.class).path(),
            FileUploadApi.class.getAnnotation(Api.class).path(),
            FileUploadApi.class.getAnnotation(Api.class).path(),
            LogQueryApi.class.getAnnotation(Api.class).path()
    };

    /**
     * Check whether the request is ignored
     */
    private boolean ignore(HttpServletRequest httpServletRequest, Api annotation) {
        // Automatically refresh from the front end without writing logs.
        if (httpServletRequest.getQueryString() != null) {
            if (httpServletRequest.getQueryString().toLowerCase().contains("request-from-refresh")) {
                return true;
            }
        }

        // Blacklist, do not write logs.
        String api = StringUtil.trim(annotation.path().toLowerCase(), '/', ' ');
        return Arrays.asList(IGNORE_LOG_APIS).contains(api);
    }

    private void log(HttpServletRequest httpServletRequest, long start, AbstractApi<?, ?> api, ApiResult<?> result, Info info) {
        if (info == null) {
            return;
        }
        Api annotation = api.getClass().getAnnotation(Api.class);

        if (ignore(httpServletRequest, annotation)) {
            return;
        }

        String token = httpServletRequest.getHeader("token");
        String ip = HttpServletRequestUtil.getClientIp(httpServletRequest);

        OperationLogMysqlModel model = new OperationLogMysqlModel();
        model.setRequestTime(new Date(start));
        model.setToken(token);
        model.setRequestIp(ip);
        model.setOperatorId(info.getId());
        model.setOperatorPhone(info.getPhoneNumber());
        model.setSpend(result.spend);


        String path = annotation.path();
        String name = annotation.name();
        model.setLogInterface(path);
        if (StringUtil.isEmpty(name)) {
            model.setInterfaceName(path);
        } else {
            model.setInterfaceName(name);
        }
        String action = path;
        if (path.lastIndexOf("/") >= 0) {
            action = path.substring(path.lastIndexOf("/") + 1);
        }
        model.setLogAction(action);
        model.setResultCode(result.code);
        model.setResultMessage(result.message);

        mOperationLogRepository.save(model);
    }
}
