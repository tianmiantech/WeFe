/**
 * Copyright 2021 The WeFe Authors. All Rights Reserved.
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

package com.welab.wefe.serving.sdk.manager;


import com.welab.wefe.common.util.ReflectionsUtil;
import com.welab.wefe.common.web.Launcher;
import com.welab.wefe.serving.sdk.processor.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Zane
 */
public class ModelProcessorManager {
    /**
     * modelId : Class
     */
    private static final Map<String, Class<? extends AbstractModelProcessor>> MAP = new HashMap<>();

    /**
     * modelId : Class
     */
    private static final Map<String, Class<? extends AbstractBatchModelProcessor>> MAP_BATCH_PROCESSOR = new HashMap<>();

    public static void init() {
        Logger log = LoggerFactory.getLogger(ModelProcessorManager.class);
        log.info("Initialize model processor.....");
    }

    static {
        // Populate the map with reflection
        List<Class<?>> processor = ReflectionsUtil.getClassesWithAnnotation(Launcher.API_PACKAGE_PATH, ModelProcessor.class);

        processor.forEach(x -> {
            ModelProcessor modelProcessor = x.getAnnotation(ModelProcessor.class);
            MAP.put(modelProcessor.id(), (Class<? extends AbstractModelProcessor>) x);
        });
    }

    static {
        // Populate the map with reflection
        List<Class<?>> processor = ReflectionsUtil.getClassesWithAnnotation(Launcher.API_PACKAGE_PATH, BatchModelProcessor.class);

        processor.forEach(x -> {
            BatchModelProcessor modelProcessor = x.getAnnotation(BatchModelProcessor.class);
            MAP_BATCH_PROCESSOR.put(modelProcessor.id(), (Class<? extends AbstractBatchModelProcessor>) x);
        });
    }

    /**
     * model processor
     *
     * @return AbstractModelProcessor
     */
    public static AbstractModelProcessor getProcessor(String modelId) {
        Class<? extends AbstractModelProcessor> clazz = MAP.get(modelId);

        // If the current model does not specify a processor, an empty processor is returned
        if (clazz == null) {
            return new EmptyModelProcessor();
        }
        try {
            return clazz.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Batch model processor
     *
     * @return AbstractBatchModelProcessor
     */
    public static AbstractBatchModelProcessor getBatchProcessor(String modelId) {
        Class<? extends AbstractBatchModelProcessor> clazz = MAP_BATCH_PROCESSOR.get(modelId);

        // If the current model does not specify a processor, an empty processor is returned
        if (clazz == null) {
            return new EmptyBatchModelProcessor();
        }
        try {
            return clazz.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

}
