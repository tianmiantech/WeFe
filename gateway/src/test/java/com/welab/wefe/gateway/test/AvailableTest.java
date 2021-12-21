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
package com.welab.wefe.gateway.test;

import com.welab.wefe.common.wefe.enums.GatewayActionType;
import com.welab.wefe.common.wefe.enums.GatewayProcessorType;

/**
 * @author zane
 * @date 2021/12/20
 */
public class AvailableTest {
    public static void main(String[] args) throws Exception {
        String response = Client.send(
                "290007c2a71d470ba00f486b18875d31",
                "local_test",
                GatewayActionType.refresh_system_config_cache,
                "",
                GatewayProcessorType.gatewayAvailableProcessor
        );
        System.out.println(response);
    }
}
