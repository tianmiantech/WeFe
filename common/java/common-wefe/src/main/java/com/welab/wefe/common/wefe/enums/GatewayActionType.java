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

package com.welab.wefe.common.wefe.enums;

/**
 * The type of instruction sent to the Gateway
 *
 * @author seven.zeng
 */
public enum GatewayActionType {

    /**
     * Updated the gateway system configuration cache Action
     */
    refresh_system_config_cache,
    /**
     * The gateway pushes an action to the board through HTTP
     */
    http_job,
    /**
     * Creating a Chat Message
     */
    create_chat_msg,
    /**
     * Placeholder action (no business meaning, just taking a place)
     * tips：In the ProBuffer, the value of the field must be filled in instead of null. Therefore, when the Processor field is added in the later gateway to replace the action, the action in the message should use this field
     */
    none
}
