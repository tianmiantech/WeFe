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

package com.welab.wefe.serving.service.feature;

import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.util.ReflectionsUtil;
import com.welab.wefe.common.web.Launcher;
import com.welab.wefe.serving.sdk.dto.PredictParams;
import com.welab.wefe.serving.service.feature.code.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author hunter.zhao
 */
public class CodeFeatureDataHandle extends AbstractFeatureDataHandle {

    private static Logger LOG = LoggerFactory.getLogger(CodeFeatureDataHandle.class);

    /**
     * modelId : Class
     */
    private static final Map<String, Class<? extends AbstractFeatureDataProcessor>> MAP = new HashMap<>();

    /**
     * modelId : Class
     */
    private static final Map<String, Class<? extends AbstractBatchFeatureDataProcessor>> BATCH_MAP = new HashMap<>();

    public static void init() {
        LOG.info("Initialization class processor.....");
    }

    static {
        // Fill map with reflection
        List<Class<?>> processor = ReflectionsUtil.getClassesWithAnnotation(Launcher.API_PACKAGE_PATH, FeatureProcessor.class);

        processor.forEach(x -> {
            FeatureProcessor featureProcessor = x.getAnnotation(FeatureProcessor.class);
            MAP.put(featureProcessor.id(), (Class<? extends AbstractFeatureDataProcessor>) x);
        });


        // Fill map with reflection
        List<Class<?>> batchProcessor = ReflectionsUtil.getClassesWithAnnotation(Launcher.API_PACKAGE_PATH, BatchFeatureProcessor.class);

        batchProcessor.forEach(x -> {
            BatchFeatureProcessor featureProcessor = x.getAnnotation(BatchFeatureProcessor.class);
            BATCH_MAP.put(featureProcessor.id(), (Class<? extends AbstractBatchFeatureDataProcessor>) x);
        });
    }


    private static AbstractFeatureDataProcessor get(String modelId) {
        Class<? extends AbstractFeatureDataProcessor> clazz = MAP.get(modelId);


        // If the current model does not specify a processor, an empty processor is returned
        if (clazz == null) {
            return new EmptyFeatureDataProcessor();
        }
        try {
            return clazz.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }


    private static AbstractBatchFeatureDataProcessor getBatch(String modelId) {
        Class<? extends AbstractBatchFeatureDataProcessor> clazz = BATCH_MAP.get(modelId);

        // If the current model does not specify a processor, an empty processor is returned
        if (clazz == null) {
            return null;
        }

        try {
            return clazz.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }


    /**
     * Feature processor list
     */
    public static List<String> getList() {

        List<String> processor = new ArrayList<>();
        MAP.forEach((k, v) -> processor.add(v.getSimpleName()));

        return processor;
    }

    /**
     * Get feature processor class name
     */
    public static String getSimpleName(String modelId) {
        Class<? extends AbstractFeatureDataProcessor> clazz = MAP.get(modelId);

        // If the current model does not specify a processor, an empty processor is returned
        if (clazz == null) {
            return EmptyFeatureDataProcessor.class.getSimpleName();
        }

        return clazz.getSimpleName();
    }

    @Override
    public Map<String, Object> handle(String modelId, PredictParams predictParams) throws StatusCodeWithException {
        AbstractFeatureDataProcessor processor = get(modelId);
        if (processor == null) {
            throw new StatusCodeWithException(StatusCode.SYSTEM_ERROR, "No corresponding processor was found");
        }
        return processor.process(predictParams.getUserId());
    }

    @Override
    public Map<String, Map<String, Object>> batch(String modelId, PredictParams predictParams) throws StatusCodeWithException {
        //Find batch processor
        AbstractBatchFeatureDataProcessor batchProcessor = getBatch(modelId);
        if (batchProcessor != null) {
            return batchProcessor.process(predictParams.getUserIds());
        }

        //If no batch processor is set, a single processor is used
        AbstractFeatureDataProcessor processor = get(modelId);
        if (processor == null) {
            throw new StatusCodeWithException(StatusCode.SYSTEM_ERROR, "No corresponding processor was found");
        }
        Map<String, Map<String, Object>> featureDataMap = new HashMap<>(16);
        predictParams.getUserIds().forEach(
                x -> featureDataMap.put(x, processor.process(x))
        );

        return featureDataMap;
    }

}
