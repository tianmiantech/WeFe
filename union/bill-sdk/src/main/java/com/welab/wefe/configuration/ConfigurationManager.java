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

/**
 * ConfigurationManager.java
 * <p>
 * Copyright 2016 WeLab Holdings, Inc. All rights reserved.
 * WELAB PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.welab.wefe.configuration;

import org.apache.commons.configuration.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * @author yuxin.zhang
 */
public class ConfigurationManager {
    private static final Logger LOG = LoggerFactory.getLogger(ConfigurationManager.class);

    private final static boolean THROW_EXCEPTION_ON_MISSING = true;
    private static ConfigurationManager configurationManager;
    private CompositeConfiguration config = new CompositeConfiguration();
    private final List<String> propertiesFiles = new ArrayList<>();

    private ConfigurationManager(String... files) {
        LOG.info("Start to initialize configuration");
        addPropertyFiles(files);
        config();
        LOG.info("Initialize configuration success");

        config.getKeys().forEachRemaining(key -> LOG.debug("{} = {}", key, config.getString(key)));
    }

    public static void init(String... files) {
        configurationManager = new ConfigurationManager(files);
    }

    public static void init(CompositeConfiguration config) {
        if (configurationManager == null && config != null) {
            configurationManager = new ConfigurationManager();
            configurationManager.config = config;
            LOG.info("Initialize configuration success");
        }
    }

    public static CompositeConfiguration getConfig() {
        String defaultConfigFile = "config.properties";
        if (configurationManager == null) {
            try {
                PropertiesConfiguration springBootProfile = new PropertiesConfiguration(ConfigurationManager.class.getClassLoader().getResource("application.properties"));

                // First get the spring.profiles.active variable of the java -jar -D parameter (used for Docker deployment), if not, select the value of the application.properties variable (used for local debugging)
                String envConfigFileSuffix = System.getProperty("spring.profiles.active");
                envConfigFileSuffix = StringUtils.isNotEmpty(envConfigFileSuffix) ? envConfigFileSuffix : springBootProfile.getString("spring.profiles.active");

                if (StringUtils.isNotEmpty(envConfigFileSuffix)) {
                    defaultConfigFile = String.format("config-%s.properties", envConfigFileSuffix);
                }
            } catch (ConfigurationException e) {
                // can be ignored
            }

            configurationManager = new ConfigurationManager(Objects.toString(System.getProperty("configFile"), defaultConfigFile));
        }
        return configurationManager.config;
    }

    private Configuration config() {
        try {
            AbstractConfiguration.setDefaultListDelimiter(';');
            config.addConfiguration(new SystemConfiguration());
            for (String file : propertiesFiles) {
                if (new File(file).exists()) {
                    config.addConfiguration(new PropertiesConfiguration(file));
                } else {
                    config.addConfiguration(new PropertiesConfiguration(this.getClass().getClassLoader().getResource(file)));
                }
            }
            config.setThrowExceptionOnMissing(THROW_EXCEPTION_ON_MISSING);
        } catch (ConfigurationException e) {
            LOG.error("Couldn't find config properties");
        }
        return config;
    }

    private void addPropertyFiles(String... files) {
        propertiesFiles.addAll(Arrays.asList(files));
    }
}
