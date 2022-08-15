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

package com.welab.wefe.board.service.database.entity;

import com.welab.wefe.board.service.database.entity.base.AbstractBaseMySqlModel;
import com.welab.wefe.board.service.database.listener.DataSourceMysqlModelListener;
import com.welab.wefe.common.wefe.enums.DatabaseType;

import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

/**
 * Data source, purpose: read data from the specified database and upload it to CK as the original data set
 *
 * @author Johnny.lin
 */
@Entity(name = "data_source")
@EntityListeners(DataSourceMysqlModelListener.class)
public class DataSourceMysqlModel extends AbstractBaseMySqlModel {
    /**
     * Data source name
     */
    private String name;

    /**
     * Database types, enumerations(hive、impala、mysql)
     */
    @Enumerated(EnumType.STRING)
    private DatabaseType databaseType;

    /**
     * Database IP address
     */
    private String host;

    /**
     * port
     */
    private Integer port;

    /**
     * Name of the database to connect to
     */
    private String databaseName;

    /**
     * User name
     */
    private String userName;

    /**
     * Password
     */
    private String password;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public DatabaseType getDatabaseType() {
        return databaseType;
    }

    public void setDatabaseType(DatabaseType databaseType) {
        this.databaseType = databaseType;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
