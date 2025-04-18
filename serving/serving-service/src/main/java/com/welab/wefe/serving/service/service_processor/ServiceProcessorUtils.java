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
package com.welab.wefe.serving.service.service_processor;

import com.welab.wefe.serving.service.enums.ServiceTypeEnum;

import java.util.HashMap;
import java.util.Map;

/**
 * @author hunter.zhao
 */
public class ServiceProcessorUtils {

    /**
     * Template generation method corresponding to each databaseType
     */
    private static Map<Integer, Class<? extends AbstractServiceProcessor>> SERVICE_PROCESSOR_MAPPING = new HashMap<>();

    static {
        SERVICE_PROCESSOR_MAPPING.put(ServiceTypeEnum.PSI.getCode(), PsiServiceProcessor.class);
        SERVICE_PROCESSOR_MAPPING.put(ServiceTypeEnum.PIR.getCode(), PirServiceProcessor.class);
        SERVICE_PROCESSOR_MAPPING.put(ServiceTypeEnum.SA.getCode(), SAServiceProcessor.class);
        SERVICE_PROCESSOR_MAPPING.put(ServiceTypeEnum.MULTI_SA.getCode(), SAQueryServiceProcessor.class);
        SERVICE_PROCESSOR_MAPPING.put(ServiceTypeEnum.MULTI_PSI.getCode(), MultiPsiServiceProcessor.class);
        SERVICE_PROCESSOR_MAPPING.put(ServiceTypeEnum.MULTI_PIR.getCode(), MultiPirServiceProcessor.class);
        SERVICE_PROCESSOR_MAPPING.put(ServiceTypeEnum.MachineLearning.getCode(), ModelServiceProcessor.class);
    }


    public static AbstractServiceProcessor get(int serviceType) {

        Class<? extends AbstractServiceProcessor> clazz = SERVICE_PROCESSOR_MAPPING.get(serviceType);

        try {
            return clazz.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}
