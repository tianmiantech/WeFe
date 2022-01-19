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

package com.welab.wefe.serving.service.database.serving.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

import com.welab.wefe.common.enums.DatabaseType;

/**
 * @author Johnny.lin
 * @date 2020/9/16
 */
@Entity(name = "data_source")
public class DataSourceMySqlModel extends AbstractBaseMySqlModel {
	
	public static final String PASSWORD_MASK = "*************";
	
	private static final long serialVersionUID = 4348703828245457696L;

	/**
     * Data source name
     */
    private String name;

    /**
     * Database type，The enumeration(hive、impala、mysql)
     */
    @Enumerated(EnumType.STRING)
    @Column(name="database_type")
    private DatabaseType databaseType;

    /**
     * database host
     */
    private String host;

    /**
     * database port
     */
    private Integer port;

    /**
     * The name of the database to connect to
     */
    @Column(name = "database_name")
    private String databaseName;

    /**
     * userName
     */
    @Column(name = "user_name")
    private String userName;

    /**
     * password
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
