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

package com.welab.wefe.data.fusion.service.repo.impl;

import com.welab.wefe.common.util.StringUtil;
import com.welab.wefe.data.fusion.service.repo.AbstractStorage;
import com.welab.wefe.data.fusion.service.repo.config.MysqlConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author jacky.jiang
 */
@Component
public class MysqlStorage extends AbstractStorage {


    @Autowired
    private MysqlConfig mysqlConfig;

    @Override
    public void createTable(String dbName, String tbName, List<String> rows) throws Exception {
        Connection conn = null;
        PreparedStatement statement = null;
        try {
            conn = getConnection();
            String sql = String.format("CREATE TABLE %s (", tbName);
            StringBuilder s = new StringBuilder();
            for (String row : rows) {
                s.append("`").append(row).append("`");
                s.append(" VARCHAR(320) NOT NULL,");
            }
            if (s.length() > 0) {
                s = new StringBuilder(s.substring(0, s.length() - 1));
                s.append(")");
            }
            sql = sql + s;
            LOG.info("执行创建表sql语句:" + sql);
            statement = conn.prepareStatement(sql);
            statement.execute();
        } catch (Exception e) {
            LOG.error("创建表失败:" + e.getMessage());
        } finally {
            close(statement, conn);
        }

    }

    @Override
    public void dropTable(String dbName, String tbName) throws Exception {
        Connection conn = null;
        PreparedStatement statement = null;
        try {
            conn = getConnection();
            String sql = String.format("DROP TABLE %s ", tbName);

            LOG.info("执行创建表sql语句:" + sql);
            statement = conn.prepareStatement(sql);
            statement.execute();
        } catch (Exception e) {
            LOG.error("删除表失败:" + e.getMessage());
        } finally {
            close(statement, conn);
        }

    }


    @Override
    public void insert(String dbName, String tbName, Map<String, Object> data) throws Exception {
        Connection conn = null;
        PreparedStatement statement = null;
        try {
            List<String> fields = new ArrayList<>(data.keySet());
            List<Object> values = new ArrayList<>(data.values());
            conn = getConnection();
            conn.setAutoCommit(false);

            String sql = String.format("INSERT INTO %s (", tbName);
            StringBuilder s = new StringBuilder();
            for (String field : fields) {
                s.append("`").append(field).append("`").append(",");
            }
            if (s.length() > 0) {
                s = new StringBuilder(s.substring(0, s.length() - 1));
                s.append(") values(");
            }

            sql = sql + s;

            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < fields.size(); i++) {
                sb.append("?").append(",");
            }
            String keywordStr = sb.deleteCharAt(sb.length() - 1).toString();
            sql = sql + keywordStr + ")";

            statement = conn.prepareStatement(sql);

            for (int i = 0; i < values.size(); i++) {
                statement.setString(i + 1, values.get(i).toString());
                statement.addBatch();
            }
            statement.executeBatch();
            conn.commit();
        } finally {
            close(statement, conn);
        }
    }

    @Override
    public void putAll(String dbName, String tbName, List<Map<String, Object>> list) throws Exception {
        if (list == null || list.size() == 0) {
            return;
        }
        PreparedStatement statement = null;
        Connection conn = getConnection();
        conn.setAutoCommit(false);

        Map<String, Object> d = list.get(0);
        List<String> fields = new ArrayList<>(d.keySet());

        String sql = String.format("INSERT INTO %s (", tbName);
        StringBuilder s = new StringBuilder();
        for (String field : fields) {
            s.append("`").append(field).append("`").append(",");
        }
        if (s.length() > 0) {
            s = new StringBuilder(s.substring(0, s.length() - 1));
            s.append(") values(");
        }

        sql = sql + s;

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < fields.size(); i++) {
            sb.append("?").append(",");
        }
        String keywordStr = sb.deleteCharAt(sb.length() - 1).toString();
        sql = sql + keywordStr + ")";
        statement = conn.prepareStatement(sql);

        for (Map<String, Object> data : list) {
            try {
                List<Object> values = new ArrayList<>(data.values());
                StringBuilder check = new StringBuilder();
                for (int i = 0; i < values.size(); i++) {
                    statement.setString(i + 1, values.get(i).toString());
                    check.append(values.get(i).toString());
                }
                if (check.length() > 0) {
                    statement.addBatch();
                }

            } finally {

            }
        }
        statement.executeBatch();
        conn.commit();
        close(statement, conn);
    }


    @Override
    public int count(String dbName, String tbName) throws Exception {
        int result = 0;
        Connection conn = null;
        PreparedStatement statement = null;
        ResultSet rs = null;
        try {
            conn = getConnection();
            String sql = "select count(*) from " + formatTableName(dbName, tbName);
            statement = conn.prepareStatement(sql);
            rs = statement.executeQuery();
            if (rs.next()) {
                result = rs.getInt(1);
            }
        } finally {
            close(rs, statement, conn);
        }
        return result;
    }
}
