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

package com.welab.wefe.tool;

import com.welab.wefe.bo.data.BlockInfoBO;
import com.welab.wefe.bo.data.EventBO;
import com.welab.wefe.exception.BusinessException;
import com.welab.wefe.parser.AbstractParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author yuxin.zhang
 **/
public class DataProcessor {

    private static final Logger log = LoggerFactory.getLogger(DataProcessor.class);

    private static final String ABSTRACT = "Abstract";
    private static final String PARSER_PKG = "com.welab.wefe.parser";
    private static final String EVENT_PARSER_SUFFIX = "EventParser";
    private static final Map<String, Class<?>> CLASS_MAP = new HashMap<>();

    static {
        getPackageAllClasses(PARSER_PKG);
    }


    /**
     * parse Block Data
     */
    public static Boolean parseBlockData(BlockInfoBO blockInfoBO) throws BusinessException {
        for (EventBO eventBO : blockInfoBO.getEventBOList()) {
            String contractName = eventBO.getContractName();
            String parserName = contractName + EVENT_PARSER_SUFFIX;
            AbstractParser parser = (AbstractParser) createInstance(parserName);
            if (parser == null) {
                log.warn("can't create parser: {}", parserName);
                return false;
            }

            parser.process(eventBO);
        }
        return true;
    }


    private static void getPackageAllClasses(String pkgname) {
        ClassPathScanHandler handler = new ClassPathScanHandler();
        Set<Class<?>> classList = handler.getPackageAllClasses(pkgname, true);
        for (Class<?> clazz : classList) {
            String simpleName = clazz.getSimpleName();
            if (!simpleName.startsWith(ABSTRACT)) {
                CLASS_MAP.put(simpleName, clazz);
                log.info(clazz.getName());
            }
        }
    }

    static Object createInstance(String key) {
        Class<?> clazz = CLASS_MAP.get(key);
        if (clazz != null) {
            try {
                return Class.forName(clazz.getName()).newInstance();
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
        return null;
    }

}
