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

package com.welab.wefe.board.service.dto.kernel.machine_learning;

import com.alibaba.fastjson.annotation.JSONField;
import com.welab.wefe.board.service.constant.Config;
import com.welab.wefe.common.web.Launcher;
import com.welab.wefe.common.wefe.enums.env.EnvName;


/**
 * @author zane.luo
 */
public class Env {

    private int workMode;
    private EnvName name;
    /**
     * 计算引擎相关配置
     */
    private CalculationEngineConfig calculationEngineConfig;
    /**
     * storage 相关配置
     */
    private StorageConfig storageConfig;

    @JSONField(serialize = false)
    public static Env get() {
        Env env = new Env();

        env.calculationEngineConfig = CalculationEngineConfig.get();
        env.storageConfig = StorageConfig.get();

        /** Working mode of modeling tasks
         * Use integer type definition: Cluster mode=1, stand-alone mode=0
         * If work_mode=1 is used, multi-party interaction needs to go through the gateway
         * work_mode=0 is only used in stand-alone mode，without gateway interaction, the transmitted data is directly written to mysql，often used with "wefe.job.backend=LOCAL"
         */
        // board 创建的任务全部为集群模式，即需要通过网关访问。
        env.setWorkMode(1);
        env.setName(Launcher.getBean(Config.class).getEnvName());
        return env;
    }

    //region getter/setter

    public int getWorkMode() {
        return workMode;
    }

    public void setWorkMode(int workMode) {
        this.workMode = workMode;
    }

    public EnvName getName() {
        return name;
    }

    public void setName(EnvName name) {
        this.name = name;
    }

    public CalculationEngineConfig getCalculationEngineConfig() {
        return calculationEngineConfig;
    }

    public void setCalculationEngineConfig(CalculationEngineConfig calculationEngineConfig) {
        this.calculationEngineConfig = calculationEngineConfig;
    }

    public StorageConfig getStorageConfig() {
        return storageConfig;
    }

    public void setStorageConfig(StorageConfig storageConfig) {
        this.storageConfig = storageConfig;
    }

    //endregion
}
