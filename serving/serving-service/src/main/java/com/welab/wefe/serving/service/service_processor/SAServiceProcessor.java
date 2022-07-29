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

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.util.JObject;
import com.welab.wefe.mpc.cache.intermediate.CacheOperation;
import com.welab.wefe.mpc.cache.intermediate.CacheOperationFactory;
import com.welab.wefe.mpc.commom.Constants;
import com.welab.wefe.mpc.sa.request.QueryDiffieHellmanKeyRequest;
import com.welab.wefe.mpc.sa.request.QueryDiffieHellmanKeyResponse;
import com.welab.wefe.mpc.sa.server.service.QueryDiffieHellmanKeyService;
import com.welab.wefe.serving.service.database.entity.DataSourceMySqlModel;
import com.welab.wefe.serving.service.database.entity.TableServiceMySqlModel;
import com.welab.wefe.serving.service.utils.ServiceUtil;

/**
 * @author hunter.zhao
 */
public class SAServiceProcessor extends AbstractServiceProcessor<TableServiceMySqlModel> {

    @Override
    public JObject process(JObject data, TableServiceMySqlModel model) throws StatusCodeWithException {

        QueryDiffieHellmanKeyRequest request = new QueryDiffieHellmanKeyRequest();
        request.setP(data.getString("p"));
        request.setG(data.getString("g"));
        request.setUuid(data.getString("uuid"));
        request.setQueryParams(data.getJSONObject("query_params"));

        QueryDiffieHellmanKeyService service = new QueryDiffieHellmanKeyService();
        JSONObject queryParams = request.getQueryParams();
        JSONObject dataSource = JObject.parseObject(model.getDataSource());
        String dataSourceId = dataSource.getString("id");
        DataSourceMySqlModel dataSourceModel = dataSourceService.getDataSourceById(dataSourceId);
        String sql = ServiceUtil.generateSQL(queryParams.toJSONString(), dataSource, dataSourceModel.getDatabaseName());
        String resultfields = ServiceUtil.parseReturnFields(dataSource);
        String resultStr = "";
        try {
            Map<String, String> resultMap = dataSourceService.queryOne(dataSourceModel, sql,
                    Arrays.asList(resultfields.split(",")));
            if (resultMap == null || resultMap.isEmpty()) {
                resultMap = new HashMap<>();
            }
            resultStr = resultMap.get(resultfields);// 目前只支持一个返回值
            LOG.info(queryParams.toJSONString() + "\t " + resultStr);
        } catch (StatusCodeWithException e) {
            throw e;
        }
        QueryDiffieHellmanKeyResponse response = service.handle(request);
        // 将 0 步骤查询的数据 保存到 CacheOperation
        CacheOperation<Double> queryResult = CacheOperationFactory.getCacheOperation();
        queryResult.save(request.getUuid(), Constants.RESULT, Double.valueOf(resultStr));

        return JObject.create(response);
    }
}
