package com.welab.wefe.common.data.storage.config;

import com.welab.wefe.common.data.storage.common.DBType;
import org.springframework.util.Assert;

public class StorageConfig {
    private DBType dbType;
    private JdbcConfig jdbcConfig;
    private LmdbConfig lmdbConfig;
    private FcStorageConfig fcStorageConfig;

    public StorageConfig(JdbcConfig jdbcConfig) {
        this(jdbcConfig, null);
    }

    public StorageConfig(JdbcConfig jdbcConfig, FcStorageConfig fcStorageConfig) {
        Assert.notNull(jdbcConfig, "jdbcConfig == null");

        this.dbType = jdbcConfig.getDbType();
        this.jdbcConfig = jdbcConfig;
        this.fcStorageConfig = fcStorageConfig;

    }

    public StorageConfig(LmdbConfig lmdbConfig) {
        this(lmdbConfig, null);
    }

    public StorageConfig(LmdbConfig lmdbConfig, FcStorageConfig fcStorageConfig) {
        Assert.notNull(lmdbConfig, "lmdbConfig == null");
        this.dbType = DBType.LMDB;
        this.lmdbConfig = lmdbConfig;
        this.fcStorageConfig = fcStorageConfig;
    }


    public DBType getDbType() {
        return dbType;
    }

    public void setDbType(DBType dbType) {
        this.dbType = dbType;
    }

    public JdbcConfig getJdbcConfig() {
        return jdbcConfig;
    }

    public void setJdbcConfig(JdbcConfig jdbcConfig) {
        this.jdbcConfig = jdbcConfig;
    }

    public LmdbConfig getLmdbConfig() {
        return lmdbConfig;
    }

    public void setLmdbConfig(LmdbConfig lmdbConfig) {
        this.lmdbConfig = lmdbConfig;
    }

    public FcStorageConfig getFcStorageConfig() {
        return fcStorageConfig;
    }

    public void setFcStorageConfig(FcStorageConfig fcStorageConfig) {
        this.fcStorageConfig = fcStorageConfig;
    }
}
