/**
 * Copyright 2021 The WeFe Authors. All Rights Reserved.
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

package com.welab.wefe.common.util;

import com.alibaba.fastjson.JSONObject;

import java.sql.*;

/**
 * @author Zane
 */
public class JdbcUtil {

    public static JSONObject readRowWithJson(ResultSet rs) throws SQLException {
        JSONObject row = new JSONObject();
        ResultSetMetaData metaData = rs.getMetaData();
        for (int i = 1; i <= metaData.getColumnCount(); i++) {
            String key = metaData.getColumnName(i);
            if (key.contains(".")) {
                key = StringUtil.substringAfter(metaData.getColumnName(i), ".");
            }

            row.put(key, rs.getObject(i));
        }
        return row;
    }

    /**
     * Release connection resources
     */
    public static void colseSession(Connection connection, Statement statement) {

        try {
            if (connection != null) {
                connection.close();
            }
            if (statement != null) {
                statement.close();
            }
        } catch (Exception e) {
            connection = null;
            statement = null;
        }
    }
}
