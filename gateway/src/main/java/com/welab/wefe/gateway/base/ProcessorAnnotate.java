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

package com.welab.wefe.gateway.base;

import com.welab.wefe.gateway.service.processors.AbstractProcessor;

import java.util.HashMap;
import java.util.Map;

/**
 * Processor annotation entity class
 *
 * @author aaron.li
 **/
public class ProcessorAnnotate {
    /**
     * Entity information annotated by all processors（Key：Annotation name, Value: Entity object marked with @Processor annotation）
     */
    public static Map<String, ProcessorAnnotate> PROCESSOR_MAP = new HashMap<>(16);
    /**
     * Annotation attribute name value
     */
    private String name;
    /**
     * Annotation attribute description value
     */
    private String desc;
    /**
     * Entity object marked by @Processor annotation
     */
    private AbstractProcessor processor;

    public static void addAnnotate(Object processorBean) {
        Processor processorAnnotation = processorBean.getClass().getAnnotation(Processor.class);
        ProcessorAnnotate processorAnnotate = new ProcessorAnnotate();
        processorAnnotate.setName(processorAnnotation.name());
        processorAnnotate.setDesc(processorAnnotation.desc());
        processorAnnotate.setProcessor((AbstractProcessor) processorBean);
        PROCESSOR_MAP.put(processorAnnotate.getName(), processorAnnotate);
    }


    /**
     * Returns an entity object based on the annotation name
     *
     * @param processorName Annotation attribute name value
     * @return Entity object marked by @Processor annotation
     */
    public static ProcessorAnnotate get(String processorName) {
        return PROCESSOR_MAP.get(processorName);
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public AbstractProcessor getProcessor() {
        return processor;
    }

    public void setProcessor(AbstractProcessor processor) {
        this.processor = processor;
    }
}
