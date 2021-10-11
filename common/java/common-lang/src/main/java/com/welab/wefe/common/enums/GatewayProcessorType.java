/**
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

package com.welab.wefe.common.enums;

/**
 * Gateway processor name
 *
 * @author aaron.li
 **/
public enum GatewayProcessorType {

    /**
     * HTTP push to board message processor
     */
    boardHttpProcessor,
    /**
     * The message is saved to the flow table processor of MySQL
     */
    dbFlowTableProcessor,
    /**
     * The message is saved to the chat table processor of MySQL
     */
    dbChatTableProcessor,
    /**
     * Refresh member blacklist cache processor
     */
    refreshMemberBlacklistCacheProcessor,
    /**
     * Refresh member cache processor
     */
    refreshMemberCacheProcessor,
    /**
     * Refresh system configuration cache processor
     */
    refreshSystemConfigCacheProcessor,
    /**
     * Check gateway availability processor
     */
    gatewayAvailableProcessor,
    /**
     * Check gateway survival processor
     */
    gatewayAliveProcessor
}
