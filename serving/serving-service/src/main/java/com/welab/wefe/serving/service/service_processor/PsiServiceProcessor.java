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

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.commons.collections.CollectionUtils;

import com.alibaba.fastjson.JSONObject;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.util.JObject;
import com.welab.wefe.mpc.psi.request.QueryPrivateSetIntersectionRequest;
import com.welab.wefe.mpc.psi.request.QueryPrivateSetIntersectionResponse;
import com.welab.wefe.mpc.util.DiffieHellmanUtil;
import com.welab.wefe.serving.service.database.entity.DataSourceMySqlModel;
import com.welab.wefe.serving.service.database.entity.TableServiceMySqlModel;

/**
 * @author hunter.zhao
 */
public class PsiServiceProcessor extends AbstractServiceProcessor<TableServiceMySqlModel, JObject> {

    @Override
    public JObject process(JObject data, TableServiceMySqlModel model) throws StatusCodeWithException {
        String p = data.getString("p");
        List<String> clientIds = JObject.parseArray(data.getString("client_ids"), String.class);
        if (CollectionUtils.isEmpty(clientIds)) {
            clientIds = JObject.parseArray(data.getString("clientIds"), String.class);
        }

        QueryPrivateSetIntersectionRequest request = new QueryPrivateSetIntersectionRequest();
        request.setClientIds(clientIds);
        request.setP(p);
        QueryPrivateSetIntersectionResponse response = new QueryPrivateSetIntersectionResponse();
        BigInteger mod = new BigInteger(request.getP(), 16);
        int keySize = 1024;
        BigInteger serverKey = new BigInteger(keySize, new Random());
        JSONObject dataSource = JObject.parseObject(model.getDataSource());
        String sql = "select id from " + model.getIdsTableName();
        List<String> needFields = new ArrayList<>();
        needFields.add("id");

        DataSourceMySqlModel dataSourceModel = dataSourceService.getDataSourceById(dataSource.getString("id"));
        if (dataSourceModel == null) {
            return JObject.create(response);
        }

        List<Map<String, String>> result = dataSourceService.queryList(dataSourceModel, sql, needFields);
        List<String> serverIds = new ArrayList<>();
        for (Map<String, String> item : result) {
            serverIds.add(item.get("id"));
        }
        List<String> encryptServerIds = new ArrayList<>(serverIds.size());
        serverIds.forEach(
                serverId -> encryptServerIds.add(DiffieHellmanUtil.encrypt(serverId, serverKey, mod).toString(16)));
        response.setServerEncryptIds(encryptServerIds);

        List<String> encryptClientIds = new ArrayList<>(request.getClientIds().size());
        request.getClientIds()
                .forEach(id -> encryptClientIds.add(DiffieHellmanUtil.encrypt(id, serverKey, mod, false).toString(16)));
        response.setClientIdByServerKeys(encryptClientIds);

        return JObject.create(response);
    }
}
