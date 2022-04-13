///*
// * Copyright 2021 Tianmian Tech. All Rights Reserved.
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// * http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//
//package com.welab.wefe.data.fusion.service.repo.config;
//
//import com.alibaba.druid.pool.DruidDataSource;
//import com.welab.wefe.data.fusion.service.enums.DBType;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
//import javax.sql.DataSource;
//
///**
// * @author jacky.jiang
// */
//@Configuration
//public class DruidConfig {
//    @Autowired
//    private MysqlConfig mysqlConfig;
//
//    @Value(value = "${db.storage.type}")
//    private DBType dbType;
//
////    @Bean("storageDataSource")
//    @Bean("fusion")
//    public DataSource dataSource() {
//        DruidDataSource datasource = new DruidDataSource();
//        if (dbType == DBType.MYSQL_FUSION) {
//            datasource.setUrl(mysqlConfig.getUrl());
//            datasource.setUsername(mysqlConfig.getUsername());
//            datasource.setPassword(mysqlConfig.getPassword());
//        }
//        return datasource;
//    }
//
//}
