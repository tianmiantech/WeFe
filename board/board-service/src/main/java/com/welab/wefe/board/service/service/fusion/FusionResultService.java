package com.welab.wefe.board.service.service.fusion;

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


import com.google.common.collect.Lists;
import com.welab.wefe.board.service.api.project.fusion.result.ResultExportApi;
import com.welab.wefe.board.service.database.entity.fusion.FusionTaskMySqlModel;
import com.welab.wefe.board.service.dto.fusion.FusionResultExportProgress;
import com.welab.wefe.board.service.fusion.enums.ExportStatus;
import com.welab.wefe.board.service.fusion.manager.ExportManager;
import com.welab.wefe.board.service.service.AbstractService;
import com.welab.wefe.board.service.util.JdbcManager;
import com.welab.wefe.common.CommonThreadPool;
import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.data.storage.common.Constant;
import com.welab.wefe.common.data.storage.model.DataItemModel;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.util.DateUtil;
import com.welab.wefe.common.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

/**
 * @author hunter.zhao
 */
@Service
public class FusionResultService extends AbstractService {

    @Autowired
    FusionTaskService fusionTaskService;

    @Autowired
    FusionResultStorageService fusionResultStorageService;

    public String export(ResultExportApi.Input input) throws StatusCodeWithException {

        FusionTaskMySqlModel taskMySqlModel = fusionTaskService.findByBusinessId(input.getBusinessId());
        if (taskMySqlModel == null) {
            throw new StatusCodeWithException(StatusCode.DATA_NOT_FOUND);
        }

        //table header
        DataItemModel headerModel = fusionResultStorageService.getByKey(
                Constant.DBName.WEFE_DATA,
                fusionResultStorageService.createRawDataSetTableName(input.getBusinessId()) + ".meta",
                "header"
        );
        List<String> columns = StringUtil.splitWithoutEmptyItem(headerModel.getV().toString().replace("\"", ""), ",");


        List<DataItemModel> allList = fusionResultStorageService.getList(fusionResultStorageService.createRawDataSetTableName(input.getBusinessId()));

        Connection conn = JdbcManager.getConnection(
                input.getDatabaseType(),
                input.getHost(),
                input.getPort(),
                input.getUserName(),
                input.getPassword(),
                input.getDatabaseName()
        );

        String tableName = "fusion_result_" + input.getBusinessId() + "_" + DateUtil.toString(new Date(), DateUtil.Y4_M2_D2_H2_M2_S2);
        create(columns, conn, tableName);

        FusionResultExportProgress progress = new FusionResultExportProgress(input.getBusinessId(), tableName, allList.size());
        ExportManager.set(input.getBusinessId(), progress);

        allList.forEach(x -> {
            CommonThreadPool.run(
                    () -> {
                            try {
                                writer(columns, x, conn, tableName);
                                progress.increment();
                            } catch (SQLException e) {
                                progress.setStatus(ExportStatus.failure);
                                e.printStackTrace();
                                return;
                            }
                    }
            );
        });

        return tableName;
    }

    private void create(List<String> headers, Connection conn, String tableName) throws StatusCodeWithException {
        String sql = String.format("CREATE TABLE %s (", tableName);
        StringBuilder s = new StringBuilder(sql);
        for (String row : headers) {
            s.append(row).append(" VARCHAR(32) NOT NULL,");
        }

        if (s.length() > 0) {
            s.deleteCharAt(s.length() - 1).append(")");
        }

        try {
            PreparedStatement statement = conn.prepareStatement(s.toString());
            statement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new StatusCodeWithException(StatusCode.SQL_ERROR, "create table error:" + e.getMessage());
        }
    }


    private void writer(List<String> headers, DataItemModel model, Connection conn, String tableName) throws SQLException {
        StringBuilder sql = new StringBuilder().append("INSERT INTO  " + tableName + "(");
        headers.forEach(
                x -> {
                    sql.append(x + ",");
                }
        );
        sql.deleteCharAt(sql.length() - 1).append(") value (");

        List<String> list = Lists.newArrayList();
        list.add(String.valueOf(model.getK()));
        list.addAll(StringUtil.splitWithoutEmptyItem(String.valueOf(model.getV()), ","));
        list.forEach(x -> sql.append(x + ","));

        sql.deleteCharAt(sql.length() - 1).append(")");

        PreparedStatement statement = conn.prepareStatement(sql.toString());
        statement.execute();
    }
}
