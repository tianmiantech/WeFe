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

package com.welab.wefe.serving.service.operation;

import java.util.Arrays;
import java.util.List;

import com.welab.wefe.common.web.Launcher;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.delegate.api_log.AbstractApiLogger;
import com.welab.wefe.common.web.delegate.api_log.ApiLog;
import com.welab.wefe.serving.service.api.account.CaptchaApi;
import com.welab.wefe.serving.service.api.pir.PrivateInformationRetrievalForNaorPinkasResultsApi;
import com.welab.wefe.serving.service.api.pir.PrivateInformationRetrievalForRandomApi;
import com.welab.wefe.serving.service.api.pir.PrivateInformationRetrievalForRandomLegalApi;
import com.welab.wefe.serving.service.api.pir.PrivateInformationRetrievalForResultsApi;
import com.welab.wefe.serving.service.api.sa.SecureAggregationForResultApi;
import com.welab.wefe.serving.service.database.serving.entity.OperationLogMysqlModel;
import com.welab.wefe.serving.service.database.serving.repository.AccountRepository;
import com.welab.wefe.serving.service.database.serving.repository.OperationLogRepository;

public class ServingApiLogger extends AbstractApiLogger {

    @Override
    protected List<Class<? extends AbstractApi>> getIgnoreLogApiList() {
        return Arrays.asList(
        		CaptchaApi.class,
        		PrivateInformationRetrievalForRandomApi.class,
        		PrivateInformationRetrievalForRandomLegalApi.class,
        		PrivateInformationRetrievalForResultsApi.class,
        		PrivateInformationRetrievalForNaorPinkasResultsApi.class,
        		SecureAggregationForResultApi.class
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
