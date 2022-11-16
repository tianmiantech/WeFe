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
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.welab.wefe.common.util.JObject;
import com.welab.wefe.common.web.Launcher;
import com.welab.wefe.mpc.commom.Constants;
import com.welab.wefe.mpc.config.CommunicationConfig;
import com.welab.wefe.mpc.pir.sdk.PrivateInformationRetrievalQuery;
import com.welab.wefe.mpc.pir.sdk.config.PrivateInformationRetrievalConfig;
import com.welab.wefe.serving.service.database.entity.ClientServiceMysqlModel;
import com.welab.wefe.serving.service.database.entity.TableServiceMySqlModel;
import com.welab.wefe.serving.service.service.ClientServiceService;

import cn.hutool.core.lang.UUID;

/**
 * @author hunter.zhao
 */
public class MultiPirServiceProcessor extends AbstractServiceProcessor<TableServiceMySqlModel> {

    private final ClientServiceService clientServiceService = Launcher.getBean(ClientServiceService.class);

    @Override
    public JObject process(JObject data, TableServiceMySqlModel model) throws Exception {
        List<String> ids = JObject.parseArray(data.getString("ids"), String.class);
        int idx = data.getIntValue("index");
        String otMethod = data.getString("otMethod");
        if (StringUtils.isBlank(otMethod)) {
            otMethod = data.getString("ot_method", Constants.PIR.NAORPINKAS_OT);
        }
        JSONArray serviceConfigs = JObject.parseArray(model.getServiceConfig());
        int size = serviceConfigs.size();
        List<JObject> results = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            JSONObject serviceConfig = serviceConfigs.getJSONObject(i);
            CommunicationConfig communicationConfig = new CommunicationConfig();
            String memberId = serviceConfig.getString("member_id");
            String memberName = serviceConfig.getString("member_name");
            String apiName = serviceConfig.getString("api_name");
            String baseUrl = serviceConfig.getString("base_url");

            String url = baseUrl + apiName;
            ClientServiceMysqlModel activateModel = clientServiceService.findActivateClientServiceByUrl(url);
            if (activateModel == null) {
                throw new Exception("尚未激活服务:" + url);
            }

            communicationConfig.setApiName(apiName);
            communicationConfig.setNeedSign(true);
            communicationConfig.setCommercialId(activateModel.getCode());
            communicationConfig.setSignPrivateKey(activateModel.getPrivateKey());
            communicationConfig.setServerUrl(baseUrl);

            PrivateInformationRetrievalConfig config = new PrivateInformationRetrievalConfig((List) ids, 0, 10, null);
            config.setRequestId(UUID.randomUUID().toString().replaceAll("-", ""));
            PrivateInformationRetrievalQuery privateInformationRetrievalQuery = new PrivateInformationRetrievalQuery();
            String result = null;
            try {
                config.setTargetIndex(idx); // right index
                result = privateInformationRetrievalQuery.query(config, communicationConfig, otMethod);
                JSONObject request = new JSONObject();
                request.put("config", config);
                request.put("communicationConfig", communicationConfig);
                request.put("otMethod", otMethod);
                // add calllog
                addCalllog(config.getRequestId(), url, JSONObject.parseObject(JSONObject.toJSONString(request)),
                        JSONObject.parseObject(result));
                JObject tmp = JObject.create("memberId", memberId).append("memberName", memberName)
                        .append("index", idx).append("result", result);
                results.add(tmp);
            } catch (Exception e) {
                throw e;
            }
        }
        return JObject.create("result", results);
    }
}
