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

package com.welab.wefe.gateway.service.processors;

import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.gateway.base.ProcessorAnnotate;
import com.welab.wefe.gateway.util.ClassUtil;

import java.util.Map;

/**
 * Processor context
 *
 * @author aaron.li
 **/
public class ProcessorContext {
    private static Map<String, ProcessorAnnotate> PROCESSOR_MAP = null;

    static {
        // Load all processor classes
        PROCESSOR_MAP = ClassUtil.loadProcessorClass();
    }

    /**
     * get AbstractProcessor instance
     */
    public static AbstractProcessor getProcessor(String processorName) throws StatusCodeWithException {
        ProcessorAnnotate processorAnnotate = PROCESSOR_MAP.get(processorName);
        if (null == processorAnnotate) {
            StatusCode
                    .PARAMETER_VALUE_INVALID
                    .throwException("no suitable processor found, invalid processor name: " + processorName);
        }

        return processorAnnotate.getProcessor();
    }

}
