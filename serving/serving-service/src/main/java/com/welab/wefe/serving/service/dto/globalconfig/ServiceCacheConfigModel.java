/*
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
package com.welab.wefe.serving.service.dto.globalconfig;

import com.welab.wefe.common.fieldvalidate.secret.MaskStrategy;
import com.welab.wefe.common.fieldvalidate.secret.Secret;
import com.welab.wefe.serving.service.dto.globalconfig.base.AbstractConfigModel;
import com.welab.wefe.serving.service.dto.globalconfig.base.ConfigGroupConstant;
import com.welab.wefe.serving.service.dto.globalconfig.base.ConfigModel;

/**
 * @author hunter.zhao
 */
@ConfigModel(group = ConfigGroupConstant.SERVICE_CACHE_CONFIG)
public class ServiceCacheConfigModel extends AbstractConfigModel {

    public CacheType type;
    public String redisHost;
    private String redisPort;
    @Secret(maskStrategy = MaskStrategy.PASSWORD)
    private String redisPassword;


    public enum CacheType {
        mem,

        redis;
    }

    public CacheType getType() {
        return type;
    }

    public void setType(CacheType type) {
        this.type = type;
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
}
