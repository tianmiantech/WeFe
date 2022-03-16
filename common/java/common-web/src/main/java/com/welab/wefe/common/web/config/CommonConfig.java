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

package com.welab.wefe.common.web.config;

import com.welab.wefe.common.wefe.enums.env.EnvBranch;
import com.welab.wefe.common.wefe.enums.env.EnvName;
import org.springframework.beans.factory.annotation.Value;

/**
 * config.properties
 *
 * @author lonnie
 */
public class CommonConfig {

    @Value("${wefe.union.base-url}")
    private String unionBaseUrl;

    @Value("${wefe.file.upload.dir}")
    private String fileUploadDir;

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

    public String getUnionBaseUrl() {
        return unionBaseUrl;
    }

    public void setUnionBaseUrl(String unionBaseUrl) {
        this.unionBaseUrl = unionBaseUrl;
    }

    public String getFileUploadDir() {
        return fileUploadDir;
    }

    public void setFileUploadDir(String fileUploadDir) {
        this.fileUploadDir = fileUploadDir;
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
