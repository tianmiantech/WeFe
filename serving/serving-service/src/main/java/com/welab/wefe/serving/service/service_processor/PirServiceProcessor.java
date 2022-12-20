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

import com.alibaba.fastjson.JSONObject;
import com.welab.wefe.common.CommonThreadPool;
import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.util.JObject;
import com.welab.wefe.mpc.cache.intermediate.CacheOperation;
import com.welab.wefe.mpc.cache.intermediate.CacheOperationFactory;
import com.welab.wefe.mpc.commom.Constants;
import com.welab.wefe.mpc.pir.request.QueryKeysRequest;
import com.welab.wefe.mpc.pir.request.QueryKeysResponse;
import com.welab.wefe.mpc.pir.request.naor.QueryNaorPinkasRandomResponse;
import com.welab.wefe.mpc.pir.server.service.HuackKeyService;
import com.welab.wefe.mpc.pir.server.service.naor.NaorPinkasRandomService;
import com.welab.wefe.serving.service.database.entity.DataSourceMySqlModel;
import com.welab.wefe.serving.service.database.entity.TableServiceMySqlModel;
import com.welab.wefe.serving.service.utils.ServiceUtil;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

/**
 * @author hunter.zhao
 * @date 2022/5/19
 */
public class PirServiceProcessor extends AbstractServiceProcessor<TableServiceMySqlModel> {

    @Override
    public JObject process(JObject data, TableServiceMySqlModel model) throws StatusCodeWithException {

        List<String> ids = JObject.parseArray(data.getString("ids"), String.class);
        String otMethod = data.getString("otMethod");
        if (StringUtils.isBlank(otMethod)) {
            otMethod = data.getString("ot_method", Constants.PIR.NAORPINKAS_OT);
        }

        String uuid = UUID.randomUUID().toString().replace("-", "");
        JObject response = JObject.create();
        QueryKeysRequest request = new QueryKeysRequest();
        if (Constants.PIR.HUACK_OT.equalsIgnoreCase(otMethod)) {
            request.setIds((List) ids);
            request.setMethod("plain");
            request.setOtMethod(Constants.PIR.HUACK_OT);
            HuackKeyService service = new HuackKeyService();
            QueryKeysResponse resp = null;
            try {
                resp = service.handle(request);
                // 3 取出 QueryKeysResponse 的uuid 将uuid传入QueryResult
                response = JObject.create(resp);
            } catch (Exception e) {
                LOG.error("HUACK_OT handle error", e);
                throw new StatusCodeWithException(StatusCode.SYSTEM_ERROR, "系统异常，请联系管理员, " + e.getMessage());
            }
        } else {
            NaorPinkasRandomService service = new NaorPinkasRandomService();
            request.setIds((List) ids);
            request.setMethod("plain");
            request.setOtMethod(Constants.PIR.NAORPINKAS_OT);
            QueryNaorPinkasRandomResponse resp = null;
            try {
                LOG.info("begin NAORPINKAS_OT service handle");
                resp = service.handle(request, uuid);
                // 3 取出 QueryKeysResponse 的uuid
                // 将uuid传入QueryResult
                response = JObject.create(resp);
            } catch (Exception e) {
                LOG.error("NAORPINKAS_OT service handle error", e);
                throw StatusCodeWithException.of(StatusCode.SYSTEM_ERROR, "系统异常，请联系管理员");
            }
        }
        LOG.info("begin query data from datasource");
        CommonThreadPool.run(() -> {
            Map<String, String> result = new HashMap<>();
            // 0 根据ID查询对应的数据
            for (String id : ids) {// params
                JSONObject dataSource = JObject.parseObject(model.getDataSource());
                String dataSourceId = dataSource.getString("id");
                DataSourceMySqlModel dataSourceModel = dataSourceService.getDataSourceById(dataSourceId);
                String sql = ServiceUtil.generateSQL(id, dataSource, dataSourceModel.getDatabaseName());
                String resultfields = ServiceUtil.parseReturnFields(dataSource);
                try {
                    Map<String, String> resultMap = dataSourceService.queryOne(dataSourceModel, sql,
                            Arrays.asList(resultfields.split(",")));
                    if (resultMap == null || resultMap.isEmpty()) {
                        resultMap = new HashMap<>();
                        resultMap.put("rand", "thisisemptyresult");
                    }
                    String resultStr = JObject.toJSONString(resultMap);
                    LOG.info("pir datasource result : " + id + "\t " + resultStr);
                    result.put(id, resultStr);
                } catch (StatusCodeWithException e) {
                    LOG.error("pir query data error", e);
                }
            }
            // 将 0 步骤查询的数据 保存到 CacheOperation
            CacheOperation<Map<String, String>> queryResult = CacheOperationFactory.getCacheOperation();
            LOG.info("save service handle result");
            queryResult.save(uuid, Constants.RESULT, result);
        });
        LOG.info("finished query data from datasource");
        return response;
    }
}
