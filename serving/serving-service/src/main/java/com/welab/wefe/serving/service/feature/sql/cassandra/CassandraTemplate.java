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

//package com.welab.wefe.serving.service.feature.sql.cassandra;
//
//import com.datastax.driver.core.*;
//import com.welab.wefe.common.util.StringUtil;
//import com.welab.wefe.serving.service.feature.sql.AbstractTemplate;
//
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.Set;
//import java.util.stream.Collectors;
//
///**
// * @author hunter.zhao
//// */
//public class CassandraTemplate extends AbstractTemplate {
//
//
//    public CassandraTemplate(String url, String username, String password, String sql, String userId) {
//        super(url, username, password, sql, userId);
//    }
//
//    /**
//     */
//    @Override
//    protected Map<String, Object> execute() {
//
//        String keySpace = url.substring(url.indexOf("/") + 1);
//        int port = Integer.parseInt(url.substring(url.indexOf(":") + 1, url.indexOf("/")));
//        url = url.substring(0, url.indexOf(":"));
//
//        Cluster cluster =
//                Cluster.builder().addContactPoint(url).withPort(port)
//                        .withCredentials(username, password)
//                        .build();
//        Session session = cluster.connect(keySpace);
//
//        sql = StringUtil.replace(sql, placeholder, userId);
//        Statement statement = new SimpleStatement(sql);
//        ResultSet resultSet = session.execute(statement);
//
//        Map<String, Object> featureData = new HashMap<>();
//
//        for (Row row : resultSet) {
//
//            //Get the node information of row
//            List<ColumnDefinitions.Definition> list = row.getColumnDefinitions().asList();
//            Set<String> rowColumn = list.stream().map(x -> x.getName().toLowerCase()).collect(Collectors.toSet());
//
//            for (int i = 0; i < rowColumn.size(); i++) {
//                String feature = "x" + i;
//                featureData.put(feature, row.getObject(i));
//            }
//        }
//
//        return featureData;
//    }
//
//}
