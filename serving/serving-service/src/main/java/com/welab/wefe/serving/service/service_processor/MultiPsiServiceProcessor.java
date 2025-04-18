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

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.welab.wefe.common.util.JObject;
import com.welab.wefe.common.web.Launcher;
import com.welab.wefe.mpc.config.CommunicationConfig;
import com.welab.wefe.mpc.psi.sdk.Psi;
import com.welab.wefe.mpc.psi.sdk.PsiFactory;
import com.welab.wefe.serving.service.database.entity.ClientServiceMysqlModel;
import com.welab.wefe.serving.service.database.entity.TableServiceMySqlModel;
import com.welab.wefe.serving.service.service.ClientServiceService;

/**
 * @author hunter.zhao
 */
public class MultiPsiServiceProcessor extends AbstractServiceProcessor<TableServiceMySqlModel> {

    private final ClientServiceService clientServiceService = Launcher.getBean(ClientServiceService.class);

    @Override
    public JObject process(JObject data, TableServiceMySqlModel model) throws Exception {
        List<String> clientIds = JObject.parseArray(data.getString("client_ids"), String.class);
        if (CollectionUtils.isEmpty(clientIds)) {
            clientIds = JObject.parseArray(data.getString("clientIds"), String.class);
        }
        JSONArray serviceConfigs = JObject.parseArray(model.getServiceConfig());
        int size = serviceConfigs.size();
        List<CommunicationConfig> communicationConfigs = new LinkedList<>();
        for (int i = 0; i < size; i++) {
            JSONObject serviceConfig = serviceConfigs.getJSONObject(i);
//			String supplieId = serviceConfig.getString("member_id");
            String apiName = serviceConfig.getString("api_name");
            String baseUrl = serviceConfig.getString("base_url");

            String url = baseUrl + apiName;
            ClientServiceMysqlModel activateModel = clientServiceService.findActivateClientServiceByUrl(url);
            if (activateModel == null) {
                throw new Exception("尚未激活服务:" + url);
            }

            CommunicationConfig communicationConfig = new CommunicationConfig();
            communicationConfig.setApiName(apiName);
            communicationConfig.setServerUrl(baseUrl);
            communicationConfig.setCommercialId(activateModel.getCode());
            communicationConfig.setSignPrivateKey(activateModel.getPrivateKey());
            communicationConfig.setSecretKeyType(activateModel.getSecretKeyType().name());
            communicationConfigs.add(communicationConfig);
        }

        List<String> result = new ArrayList<>(clientIds);
        Psi psi = PsiFactory.generatePsi();
        for (CommunicationConfig config : communicationConfigs) {
            List<String> psiResult = psi.query(config, clientIds);
            if (result.isEmpty()) {
                break;
            }
            result = result.stream().filter(item -> psiResult.contains(item)).collect(Collectors.toList());
            // add calllog
            JSONObject request = new JSONObject();
            request.put("config", config);
            request.put("clientIds", clientIds);
            addCalllog(config.getRequestId(), config.getServerUrl() + config.getApiName(),
                    JSONObject.parseObject(JSONObject.toJSONString(request)),
                    JSONObject.parseArray(JSONObject.toJSONString(psi)));
        }
        return JObject.create("result", result);
    }
}
