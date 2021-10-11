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

package com.welab.wefe.gateway.util;

import java.util.HashMap;
import java.util.Map;

/**
 * In order to be compatible with the old and new versions, the mapping relationship between the original action field
 * and the new processor field
 *
 * @author aaron.li
 **/
public class ActionProcessorMappingUtil {

    /**
     * Mapping relationship between action and processor processor, key：Original action name, value：New version processor name
     */
    private static Map<String, String> MAPPING = new HashMap<>(16);

    static {
        // The old version of "http_job" (action) type is mapped to the new version of boardhttpprocessor (processor)
        MAPPING.put("http_job", "boardHttpProcessor");
        // The old version of "create_chat_msg" (action) type is mapped to the new version of dbChatTableProcessor (processor)
        MAPPING.put("create_chat_msg", "dbChatTableProcessor");
        // objectM mapping residentMemoryProcessor
        MAPPING.put("objectM", "residentMemoryProcessor");
        MAPPING.put("fcsource", "residentMemoryProcessor");
        MAPPING.put("dsource", "dSourceProcessor");
        MAPPING.put("run_job", "dbFlowTableProcessor");
        MAPPING.put("stop_job", "dbFlowTableProcessor");
        MAPPING.put("proceed_job", "dbFlowTableProcessor");
        MAPPING.put("build_task", "dbFlowTableProcessor");
    }

    /**
     * Return the processor name of the corresponding mapping of the new version according to the action name of the old version
     *
     * @param action Old version action name
     * @return The processor name of the corresponding mapping
     */
    public static String getProcessorByAction(String action) {
        return MAPPING.get(action);
    }
}
