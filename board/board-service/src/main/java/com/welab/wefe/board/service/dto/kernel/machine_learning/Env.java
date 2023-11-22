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
import com.welab.wefe.board.service.dto.globalconfig.CalculationEngineConfigModel;
import com.welab.wefe.board.service.service.globalconfig.GlobalConfigService;
import com.welab.wefe.common.data.storage.common.DBType;
import com.welab.wefe.common.util.StringUtil;
import com.welab.wefe.common.web.Launcher;
import com.welab.wefe.common.wefe.enums.JobBackendType;
import com.welab.wefe.common.wefe.enums.env.EnvName;


/**
 * @author zane.luo
 */
public class Env {
    private DBType dbType;
    private JobBackendType backend;
    private int workMode;
    private EnvName name;


    @JSONField(serialize = false)
    public static Env get() {
        Env env = new Env();
        CalculationEngineConfigModel calculationEngineConfig = Launcher.getBean(GlobalConfigService.class).getCalculationEngineConfig();
        if (StringUtil.isEmpty(calculationEngineConfig.backend)) {
            throw new RuntimeException("计算环境未选择，请在[全局设置][计算引擎设置]中指定计算环境。");
        }

        Config config = Launcher.getBean(Config.class);

        env.setBackend(JobBackendType.valueOf(calculationEngineConfig.backend));
        env.setDbType(config.getDbType());
        env.setWorkMode(config.getWorkMode());
        env.setName(config.getEnvName());
        return env;
    }

    //region getter/setter

    public DBType getDbType() {
        return dbType;
    }

    public void setDbType(DBType dbType) {
        this.dbType = dbType;
    }

    public JobBackendType getBackend() {
        return backend;
    }

    public void setBackend(JobBackendType backend) {
        this.backend = backend;
    }

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


    //endregion
}
