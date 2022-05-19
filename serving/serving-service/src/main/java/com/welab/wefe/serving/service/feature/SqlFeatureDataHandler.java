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

package com.welab.wefe.serving.service.feature;

import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.util.StringUtil;
import com.welab.wefe.common.web.Launcher;
import com.welab.wefe.common.wefe.enums.DatabaseType;
import com.welab.wefe.serving.sdk.dto.PredictParams;
import com.welab.wefe.serving.service.database.entity.DataSourceMySqlModel;
import com.welab.wefe.serving.service.database.entity.ModelSqlConfigMySqlModel;
import com.welab.wefe.serving.service.feature.sql.AbstractTemplate;
import com.welab.wefe.serving.service.feature.sql.hive.HiveTemplate;
import com.welab.wefe.serving.service.feature.sql.impala.ImpalaTemplate;
import com.welab.wefe.serving.service.feature.sql.mysql.MySqlTemplate;
import com.welab.wefe.serving.service.feature.sql.pg.PgSqlTemplate;
import com.welab.wefe.serving.service.service.DataSourceService;
import com.welab.wefe.serving.service.service.ModelSqlConfigService;

import java.util.HashMap;
import java.util.Map;

/**
 * @author hunter.zhao
 */
public class SqlFeatureDataHandler extends AbstractFeatureDataHandler {

    private static ModelSqlConfigService modelSqlConfigService;

    private static DataSourceService dataSourceService;

    /**
     * Template generation method corresponding to each databaseType
     */
    private static Map<DatabaseType, GenerateTemplateFunction> SQL_TEMPLATE = new HashMap<>();


    static {
        SQL_TEMPLATE.put(
                DatabaseType.MySql,
                (DatabaseType databaseType, String host, int port, String database, String username, String password) ->
                        new MySqlTemplate(databaseType, host, port, database, username, password)

        );

        SQL_TEMPLATE.put(
                DatabaseType.PgSql,
                (DatabaseType databaseType, String host, int port, String database, String username, String password) ->
                        new PgSqlTemplate(databaseType, host, port, database, username, password)
        );

        SQL_TEMPLATE.put(
                DatabaseType.Hive,
                (DatabaseType databaseType, String host, int port, String database, String username, String password) ->
                        new HiveTemplate(databaseType, host, port, database, username, password)
        );

        SQL_TEMPLATE.put(
                DatabaseType.Impala,
                (DatabaseType databaseType, String host, int port, String database, String username, String password) ->
                        new ImpalaTemplate(databaseType, host, port, database, username, password)
        );
    }

    static {
        modelSqlConfigService = Launcher.CONTEXT.getBean(ModelSqlConfigService.class);
        dataSourceService = Launcher.CONTEXT.getBean(DataSourceService.class);
    }

    @FunctionalInterface
    private interface GenerateTemplateFunction {
        AbstractTemplate generate(DatabaseType databaseType, String host, int port, String database, String username, String password);
    }


    @Override
    public Map<String, Object> handle(String modelId, PredictParams predictParams) throws StatusCodeWithException {

        AbstractTemplate template = generateTemplate(modelId);

        String sql = buildSqlContext(modelId, predictParams.getUserId());

        return template.handle(sql);
    }

    private String buildSqlContext(String modelId, String userId) {
        ModelSqlConfigMySqlModel modelConfig = modelSqlConfigService.findById(modelId);
        return StringUtil.replace(
                modelConfig.getSqlContext(), "?", "'" + userId + "'"
        );
    }

    private AbstractTemplate generateTemplate(String modelId) throws StatusCodeWithException {

        DataSourceMySqlModel dataSource = findSqlConfig(modelId);

        GenerateTemplateFunction func = SQL_TEMPLATE.get(dataSource.getDatabaseType());

        if (func == null) {
            throw new StatusCodeWithException(StatusCode.PARAMETER_VALUE_INVALID, "DatabaseType", dataSource.getDatabaseType().name());
        }

        AbstractTemplate template = func.generate(
                dataSource.getDatabaseType(),
                dataSource.getHost(),
                dataSource.getPort(),
                dataSource.getDatabaseName(),
                dataSource.getUserName(),
                dataSource.getPassword()
        );
        return template;
    }

    private DataSourceMySqlModel findSqlConfig(String modelId) throws StatusCodeWithException {
        ModelSqlConfigMySqlModel modelConfig = modelSqlConfigService.findById(modelId);
        DataSourceMySqlModel dataSource = dataSourceService.findById(modelConfig.getDataSourceId());
        if (dataSource == null) {
            throw new StatusCodeWithException("模型 {} 未查找到特征sql配置！" + modelId, StatusCode.PARAMETER_VALUE_INVALID);
        }

        return dataSource;
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

//
//        GenerateTemplateFunction func = SQL_TEMPLATE.get(type);
//        if (func == null) {
//            throw new StatusCodeWithException(StatusCode.PARAMETER_VALUE_INVALID, "DatabaseType", type.name());
//        }
//
//        AbstractTemplate template = func.generate(url, username, password);
//
//        return template.handle(sqlContext);
        return null;
    }

}
