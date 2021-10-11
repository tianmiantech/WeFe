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

package com.welab.wefe.configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.Objects;

/**
 * @author yuxin.zhang
 */
public class Config {

    private static final Logger LOG = LoggerFactory.getLogger(Config.class);

    private static boolean inited = false;

    public static void init() {
        if (inited) {
            return;
        }
        inited = true;
    }

    public static final String BLOCK_CHAIN_TOML_FILE_PATH = Configurations.getString("block.chain.toml.file.path");
    public static final String BLOCK_CHAIN_BILL_CONTRACT_NAME = Configurations.getString("block.chain.bill.contract.name");

    public static void print() {
        Field[] fields = Config.class.getFields();
        for (Field field : fields) {
            try {
                if (field.getType().isPrimitive() || String.class.isAssignableFrom(field.getType()) || Number.class.isAssignableFrom(field.getType())) {
                    LOG.info(field.getName() + " = " + Objects.toString(field.get(Config.class)));
                } else if (field.getType().isArray()) {
                    LOG.info(field.getName() + " = " + Objects.toString(field.get(Config.class)));
                }
            } catch (Exception e) {
                LOG.error("print field error", e);
            }
        }
    }
}
