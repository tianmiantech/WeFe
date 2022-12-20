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
import com.welab.wefe.common.jdbc.base.DatabaseType;
import com.welab.wefe.common.web.Launcher;
import com.welab.wefe.serving.sdk.model.FeatureDataModel;
import com.welab.wefe.serving.service.database.entity.DataSourceMySqlModel;
import com.welab.wefe.serving.service.database.entity.TableModelMySqlModel;
import com.welab.wefe.serving.service.feature.sql.AbstractTemplate;
import com.welab.wefe.serving.service.feature.sql.SqlRuleUtil;
import com.welab.wefe.serving.service.feature.sql.hive.HiveTemplate;
import com.welab.wefe.serving.service.feature.sql.impala.ImpalaTemplate;
import com.welab.wefe.serving.service.feature.sql.mysql.MySqlTemplate;
import com.welab.wefe.serving.service.feature.sql.pg.PgSqlTemplate;
import com.welab.wefe.serving.service.service.DataSourceService;
import com.welab.wefe.serving.service.service.ModelService;

import java.util.HashMap;
import java.util.Map;

/**
 * @author hunter.zhao
 */
public class SqlFeatureDataHandler extends AbstractFeatureDataHandler {

    private static ModelService modelService;

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
        modelService = Launcher.CONTEXT.getBean(ModelService.class);
        dataSourceService = Launcher.CONTEXT.getBean(DataSourceService.class);
    }

    @FunctionalInterface
    private interface GenerateTemplateFunction {
        AbstractTemplate generate(DatabaseType databaseType, String host, int port, String database, String username, String password);
    }


    @Override
    public FeatureDataModel handle(String modelId, String userId) throws StatusCodeWithException {

        AbstractTemplate template = generateTemplate(modelId);

        String sql = buildSqlContextByModelId(modelId, userId);

        return FeatureDataModel.of(template.handle(sql));
    }

    private String buildSqlContextByModelId(String modelId, String userId) throws StatusCodeWithException {
        TableModelMySqlModel modelConfig = modelService.findOne(modelId);
        return buildSqlContext(userId, modelConfig.getSqlScript(), modelConfig.getSqlConditionField());
    }

    private static String buildSqlContext(String userId, String sqlScript, String sqlConditionField) throws StatusCodeWithException {
        String sql = new StringBuilder(16)
                .append(sqlScript)
                .append(" where ")
                .append(sqlConditionField)
                .append("='")
                .append(userId)
                .append("'")
                .append(" limit 1")
                .toString();

        SqlRuleUtil.checkQueryContext(sql);

        return sql;
    }

    private AbstractTemplate generateTemplate(String modelId) throws StatusCodeWithException {

        DataSourceMySqlModel dataSource = findSqlConfig(modelId);

        GenerateTemplateFunction func = SQL_TEMPLATE.get(dataSource.getDatabaseType());

        if (func == null) {
            StatusCode
                    .PARAMETER_VALUE_INVALID
                    .throwExWithFormatMsg("DatabaseType", dataSource.getDatabaseType().name());
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
        TableModelMySqlModel modelConfig = modelService.findOne(modelId);
        return getDataSourceMySqlModel(modelConfig.getDataSourceId());
    }

    private static DataSourceMySqlModel getDataSourceMySqlModel(String dataSourceId) throws StatusCodeWithException {
        DataSourceMySqlModel dataSource = dataSourceService.findById(dataSourceId);
        if (dataSource == null) {
            throw new StatusCodeWithException("未查找到特征sql配置！" + dataSourceId, StatusCode.PARAMETER_VALUE_INVALID);
        }

        return dataSource;
    }

    /**
     * Get feature data
     */
    public static FeatureDataModel debug(String dataSourceId,
                                         String sqlScript,
                                         String sqlConditionField,
                                         String userId) throws StatusCodeWithException {

        DataSourceMySqlModel dataSource = getDataSourceMySqlModel(dataSourceId);

        GenerateTemplateFunction func = SQL_TEMPLATE.get(dataSource.getDatabaseType());
        if (func == null) {
            StatusCode.PARAMETER_VALUE_INVALID
                    .throwExWithFormatMsg("DatabaseType", dataSource.getDatabaseType().name());
        }

        AbstractTemplate template = func.generate(
                dataSource.getDatabaseType(),
                dataSource.getHost(),
                dataSource.getPort(),
                dataSource.getDatabaseName(),
                dataSource.getUserName(),
                dataSource.getPassword()
        );

        String sql = buildSqlContext(userId, sqlScript, sqlConditionField);

        return FeatureDataModel.of(template.handle(sql));
    }
}
