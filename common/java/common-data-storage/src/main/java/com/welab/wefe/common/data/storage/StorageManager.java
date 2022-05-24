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

package com.welab.wefe.common.data.storage;

import com.alibaba.druid.pool.DruidDataSource;
import com.sun.istack.internal.NotNull;
import com.welab.wefe.common.data.storage.common.DBType;
import com.welab.wefe.common.data.storage.config.FcStorageConfig;
import com.welab.wefe.common.data.storage.config.JdbcConfig;
import com.welab.wefe.common.data.storage.config.LmdbConfig;
import com.welab.wefe.common.data.storage.config.StorageConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.util.Assert;

import javax.sql.DataSource;
import java.sql.SQLException;


/**
 * @author yuxin.zhang
 */
public class StorageManager {
    private static final Logger LOG = LoggerFactory.getLogger(StorageManager.class);

    private AnnotationConfigApplicationContext context;


    private volatile boolean inited = false;
    public volatile boolean restarting = false;

    private static class SingletonClassInstance {
        private static final StorageManager INSTANCE = new StorageManager();
    }

    public static StorageManager getInstance() {
        return StorageManager.SingletonClassInstance.INSTANCE;
    }

    private StorageManager() {
    }


    public synchronized void init(StorageConfig storageConfig) {
        if (!inited) {
            Assert.notNull(storageConfig, "storageConfig == null");
            LOG.info("start init storage...");
            context = new AnnotationConfigApplicationContext();
            registerBean(storageConfig);
            context.refresh();
            context.start();
            inited = true;
            LOG.info("end init storage...");
        }
    }

    private void registerBean(StorageConfig storageConfig) {
        DefaultListableBeanFactory defaultListableBeanFactory =
                (DefaultListableBeanFactory) context.getBeanFactory();
        defaultListableBeanFactory.registerSingleton("storageConfig", storageConfig);
        defaultListableBeanFactory.registerSingleton("storageDataSource", buildDruidDataSource(storageConfig.getJdbcConfig()));
    }


    public synchronized void refreshJdbcConfig(JdbcConfig jdbcConfig) {
        LOG.info("start refreshJdbcConfig...");

        Assert.notNull(jdbcConfig, "jdbcConfig == null");
        StorageConfig storageConfig = context.getBean(StorageConfig.class);
        storageConfig.setJdbcConfig(jdbcConfig);
        storageConfig.setDbType(jdbcConfig.getDbType());

        DruidDataSource dataSource = (DruidDataSource) context.getBean(DataSource.class);
        dataSource.setUrl(jdbcConfig.getUrl());
        dataSource.setDriverClassName(jdbcConfig.getDriverClassName());
        dataSource.setPassword(jdbcConfig.getPassword());
        dataSource.setUsername(jdbcConfig.getUsername());

        boolean restarted = false;
        restarting = true;
        do {
            try {
                dataSource.restart();
            } catch (SQLException e) {
                LOG.error(e.getMessage(), e);
                continue;
            }
            restarting = false;
            restarted = true;
        } while (!restarted);
        LOG.info("refreshJdbcConfig success...");
    }

    public synchronized void refreshLmdbConfig(LmdbConfig lmdbConfig) {
        LOG.info("start refreshLmdbConfig...");
        Assert.notNull(lmdbConfig, "lmdbConfig == null");
        StorageConfig storageConfig = context.getBean(StorageConfig.class);
        storageConfig.setLmdbConfig(lmdbConfig);
        storageConfig.setDbType(DBType.LMDB);
        LOG.info("refreshLmdbConfig success...");
    }

    public synchronized void refreshFcStorageConfig(FcStorageConfig fcStorageConfig) {
        LOG.info("start refreshFcStorageConfig...");
        Assert.notNull(fcStorageConfig, "fcStorageConfig == null");
        StorageConfig storageConfig = context.getBean(StorageConfig.class);
        storageConfig.setFcStorageConfig(fcStorageConfig);
        LOG.info("refreshFcStorageConfig success...");

    }

    private DataSource buildDruidDataSource(JdbcConfig jdbcConfig) {
        DruidDataSource datasource = new DruidDataSource();
        datasource.setUrl(jdbcConfig.getUrl());
        datasource.setDriverClassName(jdbcConfig.getDriverClassName());
        datasource.setInitialSize(jdbcConfig.getInitialSize());
        datasource.setMinIdle(jdbcConfig.getMinIdle());
        datasource.setMaxActive(jdbcConfig.getMaxActive());
        datasource.setMaxWait(jdbcConfig.getMaxWait());
        datasource.setPassword(jdbcConfig.getPassword());
        datasource.setUsername(jdbcConfig.getUsername());
        datasource.setTestWhileIdle(jdbcConfig.isTestWhileIdle());
        datasource.setValidationQuery(jdbcConfig.getValidationQuery());
        datasource.setTimeBetweenEvictionRunsMillis(jdbcConfig.getTimeBetweenEvictionRunsMillis());
        datasource.setMinEvictableIdleTimeMillis(jdbcConfig.getMinEvictableIdleTimeMillis());
        datasource.setRemoveAbandoned(jdbcConfig.isRemoveAbandoned());
        datasource.setRemoveAbandonedTimeout(jdbcConfig.getRemoveAbandonedTimeout());
        datasource.setLogAbandoned(jdbcConfig.isLogAbandoned());
        return datasource;

    }

    public <T> T getRepo(Class<T> t) {
        return context.getBean(t);
    }

    public <T> T getRepo(String beanName) {
        return (T) context.getBean(beanName);
    }

    public <T> T getBean(String beanName) {
        return getRepo(beanName);
    }

    public static <T> T getRepository(Class<T> t) {
        return getInstance().getRepo(t);
    }

}
