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

package com.welab.wefe.union.service.operation;


import com.alibaba.fastjson.JSONObject;
import com.welab.wefe.common.data.mongodb.entity.common.OperationLog;
import com.welab.wefe.common.data.mongodb.repo.UnionOperationLogMongoRepo;
import com.welab.wefe.common.web.Launcher;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.delegate.api_log.AbstractApiLogger;
import com.welab.wefe.common.web.delegate.api_log.ApiLog;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.common.web.service.account.AccountInfo;
import com.welab.wefe.common.web.util.ModelMapper;
import com.welab.wefe.union.service.api.common.MemberFileUploadSyncApi;
import com.welab.wefe.union.service.api.common.RealnameAuthAgreementTemplateSyncApi;
import com.welab.wefe.union.service.api.member.FileUploadApi;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;

/**
 * @author zane
 **/
@Component
public class UnionApiLogger extends AbstractApiLogger {


    @Override
    protected List<Class<? extends AbstractApi>> getIgnoreLogApiList() {
        return Arrays.asList(
                FileUploadApi.class,
                MemberFileUploadSyncApi.class,
                RealnameAuthAgreementTemplateSyncApi.class
        );
    }

    @Override
    protected boolean ignoreWithoutLogin() {
        return false;
    }

    @Override
    protected JSONObject beforeSaveLog(HttpServletRequest httpServletRequest, long start, AbstractApi<?, ?> api, JSONObject params, ApiResult<?> result, AccountInfo accountInfo) {
        JSONObject requestParams = params;
        if(requestParams != null && requestParams.containsKey("data")) {
            JSONObject data = requestParams.getJSONObject("data");
            if(data.containsKey("public_key")) {
                String publicKey = data.getString("public_key");
                data.put("public_key",compress(publicKey));
            }
            if(data.containsKey("logo")) {
                String logo = data.getString("logo");
                data.put("logo",compress(logo));
            }

            String callerId = null;
            if(data.containsKey("cur_member_id")) {
                callerId = data.getString("cur_member_id");
            } else if (data.containsKey("cur_blockchain_id")){
                callerId = data.getString("cur_blockchain_id");
            }
            data.put("caller_id",callerId);
            requestParams.put("data",data);
        }
        return requestParams;
    }


    public String compress(String data) {
        StringBuffer result = new StringBuffer();
        result.append(data.substring(0,50));
        result.append("********************");
        result.append(data.substring(data.length() - 50));
        return result.toString();
    }

    @Override
    protected void save(ApiLog apiLog) {
        OperationLog model = ModelMapper.map(apiLog,OperationLog.class);
        Launcher.getBean(UnionOperationLogMongoRepo.class).save(model);
    }

    @Override
    protected void updateAccountLastActionTime(String accountId) {
    }

}
