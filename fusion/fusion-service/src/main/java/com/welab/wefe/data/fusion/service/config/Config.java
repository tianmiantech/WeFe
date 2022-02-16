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

package com.welab.wefe.data.fusion.service.config;

import com.welab.wefe.common.wefe.enums.env.EnvBranch;
import com.welab.wefe.common.wefe.enums.env.EnvName;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

/**
 * config.properties
 *
 * @author lonnie
 */
@Component
@PropertySource(value = {"application.properties"}, encoding = "utf-8")
@ConfigurationProperties
public class Config {

    @Value("${wefe.job.work_mode}")
    private Integer workMode;

    @Value("${env.name}")
    private EnvName envName;

    /**
     * The branch of the environment, different branches will have different functions.
     * <p>
     * online_demo: You can only delete data created by yourself（eg:flow、member、data_set）
     */
    @Value("${env.branch:master}")
    private EnvBranch envBranch;

    public boolean isOnlineDemo() {
        return envBranch == EnvBranch.online_demo;
    }

    // region getter/setter

    public Integer getWorkMode() {
        return workMode;
    }

    public void setWorkMode(Integer workMode) {
        this.workMode = workMode;
    }

    public EnvName getEnvName() {
        return envName;
    }

    public void setEnvName(EnvName envName) {
        this.envName = envName;
    }

    public EnvBranch getEnvBranch() {
        return envBranch;
    }

    public void setEnvBranch(EnvBranch envBranch) {
        this.envBranch = envBranch;
    }


    // endregion

}
