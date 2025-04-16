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
package com.welab.wefe.common.wefe.dto.global_config.storage;

import com.welab.wefe.common.wefe.dto.global_config.base.AbstractConfigModel;
import com.welab.wefe.common.wefe.dto.global_config.base.ConfigGroupConstant;
import com.welab.wefe.common.wefe.dto.global_config.base.ConfigModel;
import com.welab.wefe.common.fieldvalidate.secret.MaskStrategy;
import com.welab.wefe.common.fieldvalidate.secret.Secret;
import com.welab.wefe.common.util.StringUtil;
import com.welab.wefe.common.wefe.dto.storage.ClickhouseConfig;

/**
 * @author zane
 * @date 2022/5/6
 */
@ConfigModel(group = ConfigGroupConstant.CLICKHOUSE_STORAGE)
public class ClickHouseStorageConfigModel extends AbstractConfigModel {
    public String host;
    public int http_port = 8123;
    public int tcpPort = 9000;
    public String username;
    @Secret(maskStrategy = MaskStrategy.PASSWORD)
    public String password;

    public ClickhouseConfig toStorageConfig() {
        if (StringUtil.isEmpty(host) || StringUtil.isEmpty(password) || StringUtil.isEmpty(username)) {
            return null;
        }

        return new ClickhouseConfig(host, http_port, username, password);
    }
}
