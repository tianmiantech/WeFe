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

package com.welab.wefe.data.fusion.service.operation;

import com.welab.wefe.common.web.Launcher;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.delegate.api_log.AbstractApiLogger;
import com.welab.wefe.common.web.delegate.api_log.ApiLog;
import com.welab.wefe.common.web.util.ModelMapper;
import com.welab.wefe.data.fusion.service.api.account.CaptchaApi;
import com.welab.wefe.data.fusion.service.api.bloomfilter.GetBloomFilterStateApi;
import com.welab.wefe.data.fusion.service.api.dataset.GetDataSetStateApi;
import com.welab.wefe.data.fusion.service.api.file.MergeApi;
import com.welab.wefe.data.fusion.service.api.file.UploadApi;
import com.welab.wefe.data.fusion.service.database.entity.OperationLogMysqlModel;
import com.welab.wefe.data.fusion.service.database.repository.AccountRepository;
import com.welab.wefe.data.fusion.service.database.repository.OperationLogRepository;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * @author zane
 **/
@Component
public class FusionApiLogger extends AbstractApiLogger {

    @Override
    protected List<Class<? extends AbstractApi>> getIgnoreLogApiList() {
        return Arrays.asList(
                CaptchaApi.class,
                GetBloomFilterStateApi.class,
                GetDataSetStateApi.class,
                UploadApi.class,
                MergeApi.class
        );
    }

    @Override
    protected void save(ApiLog apiLog) throws Exception {
        OperationLogMysqlModel model = ModelMapper.map(apiLog,OperationLogMysqlModel.class);
        Launcher.getBean(OperationLogRepository.class).save(model);
    }

    @Override
    protected void updateAccountLastActionTime(String accountId) throws Exception {
        Launcher.getBean(AccountRepository.class).updateLastActionTime(accountId);
    }

}
