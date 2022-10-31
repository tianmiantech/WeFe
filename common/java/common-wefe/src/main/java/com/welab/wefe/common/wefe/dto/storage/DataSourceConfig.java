package com.welab.wefe.common.wefe.dto.storage;

import org.springframework.util.Assert;

/**
 * @author yuxin.zhang
 */
public abstract class DataSourceConfig {
    protected String host = "127.0.0.1";
    protected Integer port = 8123;
    protected String username;
    protected String password;
    protected String driverClassName;
    protected String url = "jdbc:clickhouse//127.0.0.1:8123";
    protected Integer initialSize = 1;
    protected Integer maxActive = 50;
    protected Integer minIdle = 1;
    protected Integer maxWait = 60000;
    private boolean testWhileIdle = false;
    protected String validationQuery = "SELECT 1";


    protected Integer timeBetweenEvictionRunsMillis = 15000;
    protected Integer minEvictableIdleTimeMillis = 60000;
    protected boolean removeAbandoned = true;
    protected Integer removeAbandonedTimeout = 60;
    protected boolean logAbandoned = true;
    /**
     * The optimal batch insertion byte size of clickhouse, unit: M (support decimal)
     */
    protected double optimalInsertByteSize = 1;


    public DataSourceConfig(String host, Integer port, String username, String password) {
        Assert.notNull(host, "host == null");
        Assert.notNull(port, "port == null");
        Assert.notNull(username, "username == null");
        Assert.notNull(password, "password == null");

        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
        buildUrl();
    }

    protected abstract void buildUrl();

    public abstract String getDriverClassName();


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

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Integer getInitialSize() {
        return initialSize;
    }

    public void setInitialSize(Integer initialSize) {
        this.initialSize = initialSize;
    }

    public Integer getMaxActive() {
        return maxActive;
    }

    public void setMaxActive(Integer maxActive) {
        this.maxActive = maxActive;
    }

    public Integer getMinIdle() {
        return minIdle;
    }

    public void setMinIdle(Integer minIdle) {
        this.minIdle = minIdle;
    }

    public Integer getMaxWait() {
        return maxWait;
    }

    public void setMaxWait(Integer maxWait) {
        this.maxWait = maxWait;
    }

    public boolean isTestWhileIdle() {
        return testWhileIdle;
    }

    public void setTestWhileIdle(boolean testWhileIdle) {
        this.testWhileIdle = testWhileIdle;
    }

    public String getValidationQuery() {
        return validationQuery;
    }

    public void setValidationQuery(String validationQuery) {
        this.validationQuery = validationQuery;
    }

    public Integer getTimeBetweenEvictionRunsMillis() {
        return timeBetweenEvictionRunsMillis;
    }

    public void setTimeBetweenEvictionRunsMillis(Integer timeBetweenEvictionRunsMillis) {
        this.timeBetweenEvictionRunsMillis = timeBetweenEvictionRunsMillis;
    }

    public Integer getMinEvictableIdleTimeMillis() {
        return minEvictableIdleTimeMillis;
    }

    public void setMinEvictableIdleTimeMillis(Integer minEvictableIdleTimeMillis) {
        this.minEvictableIdleTimeMillis = minEvictableIdleTimeMillis;
    }

    public boolean isRemoveAbandoned() {
        return removeAbandoned;
    }

    public void setRemoveAbandoned(boolean removeAbandoned) {
        this.removeAbandoned = removeAbandoned;
    }

    public Integer getRemoveAbandonedTimeout() {
        return removeAbandonedTimeout;
    }

    public void setRemoveAbandonedTimeout(Integer removeAbandonedTimeout) {
        this.removeAbandonedTimeout = removeAbandonedTimeout;
    }

    public boolean isLogAbandoned() {
        return logAbandoned;
    }

    public void setLogAbandoned(boolean logAbandoned) {
        this.logAbandoned = logAbandoned;
    }

    public double getOptimalInsertByteSize() {
        return optimalInsertByteSize;
    }

    public void setOptimalInsertByteSize(double optimalInsertByteSize) {
        this.optimalInsertByteSize = optimalInsertByteSize;
    }
}
