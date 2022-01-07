package com.welab.wefe.serving.service.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

@Component
@PropertySource(value = { "file:${config.path}" }, encoding = "utf-8")
@ConfigurationProperties
public class Config {

	@Value("${wefe.union.base-url}")
	private String UNION_BASE_URL;

	@Value("${wefe.serving.base-url}")
	private String SERVING_BASE_URL;

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

}
