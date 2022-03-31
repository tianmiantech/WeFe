package com.welab.wefe.serving.service.config;

import com.welab.wefe.common.web.config.CommonConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

@Component
@PropertySource(value = {"file:${config.path}"}, encoding = "utf-8")
@ConfigurationProperties
public class Config extends CommonConfig {

    @Value("${wefe.union.base-url}")
    private String UNION_BASE_URL;

    @Value("${wefe.serving.base-url}")
    private String SERVING_BASE_URL;

    @Value("${wefe.serving.file-path}")
    private String fileBasePath;
    
    @Value("${wefe.redis.host}")
    private String redisHost;
    
    @Value("${wefe.redis.port}")
    private String redisPort;
    
    @Value("${wefe.redis.password}")
    private String redisPassword;
    
    @Value("${wefe.service.cache.type}")
    private String serviceCacheType;

    @Value("${sm4.secret.key}")
    private String sm4SecretKey;

    @Value("${encrypt.phone.number.open:true}")
    private boolean encryptPhoneNumberOpen;

    public String getFileBasePath() {
        return fileBasePath;
    }

    public void setFileBasePath(String fileBasePath) {
        this.fileBasePath = fileBasePath;
    }

    public String getUNION_BASE_URL() {
        return UNION_BASE_URL;
    }

    public void setUNION_BASE_URL(String UNION_BASE_URL) {
        this.UNION_BASE_URL = UNION_BASE_URL;
    }

    public String getSERVING_BASE_URL() {
        return SERVING_BASE_URL;
    }

    public void setSERVING_BASE_URL(String sERVING_BASE_URL) {
        SERVING_BASE_URL = sERVING_BASE_URL;
    }

	public String getRedisHost() {
		return redisHost;
	}

	public void setRedisHost(String redisHost) {
		this.redisHost = redisHost;
	}

	public String getRedisPort() {
		return redisPort;
	}

	public void setRedisPort(String redisPort) {
		this.redisPort = redisPort;
	}

	public String getRedisPassword() {
		return redisPassword;
	}

	public void setRedisPassword(String redisPassword) {
		this.redisPassword = redisPassword;
	}

	public String getServiceCacheType() {
		return serviceCacheType;
	}

	public void setServiceCacheType(String serviceCacheType) {
		this.serviceCacheType = serviceCacheType;
	}

    public String getSm4SecretKey() {
        return sm4SecretKey;
    }

    public void setSm4SecretKey(String sm4SecretKey) {
        this.sm4SecretKey = sm4SecretKey;
    }

    public boolean isEncryptPhoneNumberOpen() {
        return encryptPhoneNumberOpen;
    }

    public void setEncryptPhoneNumberOpen(boolean encryptPhoneNumberOpen) {
        this.encryptPhoneNumberOpen = encryptPhoneNumberOpen;
    }
}
