/*
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
package com.welab.wefe.gateway.config;

import com.google.api.client.util.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

/**
 * 读取公共配置 common.properties
 * @author ivenn.zheng
 * @date 2022/7/5
 */
@Component
@PropertySource(value = {"file:${common.path}"}, encoding = "utf-8")
@ConfigurationProperties
public class CommonConfig {


    @Value("${privacy.database.encrypt.enable:false}")
    private String isDatabaseEncryptEnable;

    @Value("${privacy.database.encrypt.secret.key}")
    private String privacyDatabaseEncryptSecretKey;

    @Value("${wefe.union.base-url}")
    private String unionBaseUrl;

    public String getIsDatabaseEncryptEnable() {
        return isDatabaseEncryptEnable;
    }

    public void setIsDatabaseEncryptEnable(String isDatabaseEncryptEnable) {
        this.isDatabaseEncryptEnable = isDatabaseEncryptEnable;
    }

    public String getPrivacyDatabaseEncryptSecretKey() {
        return privacyDatabaseEncryptSecretKey;
    }

    public void setPrivacyDatabaseEncryptSecretKey(String privacyDatabaseEncryptSecretKey) {
        this.privacyDatabaseEncryptSecretKey = privacyDatabaseEncryptSecretKey;
    }

    public String getUnionBaseUrl() {
        return unionBaseUrl;
    }

    public void setUnionBaseUrl(String unionBaseUrl) {
        this.unionBaseUrl = unionBaseUrl;
    }
}
