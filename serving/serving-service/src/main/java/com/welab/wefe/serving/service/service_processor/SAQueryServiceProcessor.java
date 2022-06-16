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
package com.welab.wefe.serving.service.service_processor;

import java.util.LinkedList;
import java.util.List;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.util.JObject;
import com.welab.wefe.common.web.Launcher;
import com.welab.wefe.mpc.config.CommunicationConfig;
import com.welab.wefe.mpc.sa.sdk.SecureAggregation;
import com.welab.wefe.mpc.sa.sdk.config.ServerConfig;
import com.welab.wefe.mpc.sa.sdk.transfer.SecureAggregationTransferVariable;
import com.welab.wefe.mpc.sa.sdk.transfer.impl.HttpTransferVariable;
import com.welab.wefe.serving.service.database.entity.ClientServiceMysqlModel;
import com.welab.wefe.serving.service.database.entity.TableServiceMySqlModel;
import com.welab.wefe.serving.service.service.ClientServiceService;

/**
 * @author hunter.zhao
 */
public class SAQueryServiceProcessor extends AbstractServiceProcessor<TableServiceMySqlModel, JObject> {

    private final ClientServiceService clientServiceService = Launcher.getBean(ClientServiceService.class);

    @Override
    public JObject process(JObject data, TableServiceMySqlModel model) throws Exception {
        Double result = -999.0;

        JObject userParams = data.getJObject("query_params");
        JSONArray serviceConfigs = JObject.parseArray(model.getServiceConfig());
        int size = serviceConfigs.size();
        List<ServerConfig> serverConfigs = new LinkedList<>();
        List<SecureAggregationTransferVariable> transferVariables = new LinkedList<>();

        for (int i = 0; i < size; i++) {
            JSONObject serviceConfig = serviceConfigs.getJSONObject(i);
//			String supplieId = serviceConfig.getString("member_id");
            String apiName = serviceConfig.getString("api_name");
            String baseUrl = serviceConfig.getString("base_url");
            String url = baseUrl + apiName;
            ClientServiceMysqlModel activateModel = clientServiceService.findActivateClientServiceByUrl(url);
            if (activateModel == null) {
                throw new StatusCodeWithException("尚未激活服务:" + url, StatusCode.PERMISSION_DENIED);
            }
            ServerConfig config = new ServerConfig();
            config.setServerName(apiName);
            config.setServerUrl(baseUrl);
            config.setQueryParams(userParams);
            CommunicationConfig communicationConfig = new CommunicationConfig();
            communicationConfig.setApiName(apiName);
            communicationConfig.setServerUrl(baseUrl);
            communicationConfig.setCommercialId(activateModel.getCode());
            communicationConfig.setNeedSign(true);
            communicationConfig.setSignPrivateKey(activateModel.getPrivateKey());
            config.setCommunicationConfig(communicationConfig);
            HttpTransferVariable httpTransferVariable = new HttpTransferVariable(config);
            transferVariables.add(httpTransferVariable);
            serverConfigs.add(config);
        }

        SecureAggregation secureAggregation = new SecureAggregation();
        if (model.getOperator().equalsIgnoreCase("sum")) {
            result = secureAggregation.query(serverConfigs, transferVariables);
        } else {
            result = secureAggregation.query(serverConfigs, transferVariables) / size;
        }

        return JObject.create("result", result);
    }
}
