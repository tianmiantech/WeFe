/**
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

package com.welab.wefe.serving.service.feature;

import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.web.Launcher;
import com.welab.wefe.common.wefe.enums.DatabaseType;
import com.welab.wefe.serving.sdk.dto.PredictParams;
import com.welab.wefe.serving.service.database.serving.entity.ModelSqlConfigMySqlModel;
import com.welab.wefe.serving.service.feature.sql.AbstractTemplate;
import com.welab.wefe.serving.service.feature.sql.hive.HiveTemplate;
import com.welab.wefe.serving.service.feature.sql.impala.ImpalaTemplate;
import com.welab.wefe.serving.service.feature.sql.mysql.MySqlTemplate;
import com.welab.wefe.serving.service.feature.sql.pg.PgSqlTemplate;
import com.welab.wefe.serving.service.service.ModelSqlConfigService;

import java.util.HashMap;
import java.util.Map;

/**
 * @author hunter.zhao
 */
public class SqlFeatureDataHandle extends AbstractFeatureDataHandle {

    private static ModelSqlConfigService modelSqlConfigService;

    /**
     * Template generation method corresponding to each databaseType
     */
    private static Map<DatabaseType, GenerateTemplateFunction> SQL_TEMPLATE = new HashMap<>();


    static {
        SQL_TEMPLATE.put(
                DatabaseType.MySql,
                (String url, String username, String password, String sqlContext, String userId) ->
                        new MySqlTemplate(url, username, password, sqlContext, userId)

        );

        SQL_TEMPLATE.put(
                DatabaseType.PgSql,
                (String url, String username, String password, String sqlContext, String userId) ->
                        new PgSqlTemplate(url, username, password, sqlContext, userId)
        );

        SQL_TEMPLATE.put(
                DatabaseType.Hive,
                (String url, String username, String password, String sqlContext, String userId) ->
                        new HiveTemplate(url, username, password, sqlContext, userId)
        );

        SQL_TEMPLATE.put(
                DatabaseType.Impala,
                (String url, String username, String password, String sqlContext, String userId) ->
                        new ImpalaTemplate(url, username, password, sqlContext, userId)
        );

//        SQL_TEMPLATE.put(
//                DatabaseType.Cassandra,
//                (String url, String username, String password, String sqlContext, String userId) ->
//                        new CassandraTemplate(url, username, password, sqlContext, userId)
//        );
    }

    static {
        modelSqlConfigService = Launcher.CONTEXT.getBean(ModelSqlConfigService.class);
    }

    @Override
    public Map<String, Object> handle(String modelId, PredictParams predictParams) throws StatusCodeWithException {

        //Find SQL configuration
        ModelSqlConfigMySqlModel modelConfig = modelSqlConfigService.findOne(modelId);

        if (modelConfig == null) {
            throw new StatusCodeWithException("not find model config, modelId: " + modelId, StatusCode.PARAMETER_VALUE_INVALID);
        }

        GenerateTemplateFunction func = SQL_TEMPLATE.get(modelConfig.getType());

        if (func == null) {
            throw new StatusCodeWithException(StatusCode.PARAMETER_VALUE_INVALID, "DatabaseType", modelConfig.getType().name());
        }

        AbstractTemplate template = func.generate(
                modelConfig.getUrl(),
                modelConfig.getUsername(),
                modelConfig.getPassword(),
                modelConfig.getSqlContext(),
                predictParams.getUserId()
        );

        return template.handle();
    }

    @Override
    public Map<String, Map<String, Object>> batch(String modelId, PredictParams predictParams) {
        Map<String, Map<String, Object>> featureDataMap = new HashMap<>(16);
        predictParams.getUserIds().forEach(userId -> {
            try {
                featureDataMap.put(
                        userId,
                        handle(modelId, PredictParams.of(userId, null))
                );
            } catch (StatusCodeWithException e) {
                e.printStackTrace();
            }
        });

        return featureDataMap;
    }

    /**
     * Get feature data
     */
    public static Map<String, Object> get(DatabaseType type,
                                          String url,
                                          String username,
                                          String password,
                                          String sqlContext,
                                          String userId) throws StatusCodeWithException {


        GenerateTemplateFunction func = SQL_TEMPLATE.get(type);

        if (func == null) {
            throw new StatusCodeWithException(StatusCode.PARAMETER_VALUE_INVALID, "DatabaseType", type.name());
        }

        AbstractTemplate template = func.generate(
                url,
                username,
                password,
                sqlContext,
                userId
        );

        return template.handle();
    }


    @FunctionalInterface
    private interface GenerateTemplateFunction {
        /**
         * template generation method
         *
         * @param url        url
         * @param username   username
         * @param password   password
         * @param sqlContext sqlContext
         * @param userId     userId
         * @return AbstractTemplate
         */
        AbstractTemplate generate(String url, String username, String password, String sqlContext, String userId);
    }
}
