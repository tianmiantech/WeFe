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

package com.welab.wefe.manager.service.operation;


import com.welab.wefe.common.data.mongodb.entity.common.OperationLog;
import com.welab.wefe.common.data.mongodb.repo.ManagerOperationLogMongoRepo;
import com.welab.wefe.common.data.mongodb.repo.AccountMongoRepo;
import com.welab.wefe.common.web.Launcher;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.delegate.api_log.AbstractApiLogger;
import com.welab.wefe.common.web.delegate.api_log.ApiLog;
import com.welab.wefe.common.web.util.ModelMapper;
import com.welab.wefe.manager.service.api.agreement.UploadRealnameAuthAgreementTemplateApi;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * @author zane
 **/
@Component
public class ManagerApiLogger extends AbstractApiLogger {


    @Override
    protected List<Class<? extends AbstractApi>> getIgnoreLogApiList() {
        return Arrays.asList(
                UploadRealnameAuthAgreementTemplateApi.class
        );
    }

    @Override
    protected void save(ApiLog apiLog) throws Exception {
        OperationLog model = ModelMapper.map(apiLog,OperationLog.class);
        Launcher.getBean(ManagerOperationLogMongoRepo.class).save(model);
    }

    @Override
    protected void updateAccountLastActionTime(String accountId) {
        Launcher.getBean(AccountMongoRepo.class).updateLastActionTime(accountId);
    }

}
