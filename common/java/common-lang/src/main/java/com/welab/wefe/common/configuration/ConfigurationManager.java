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


package com.welab.wefe.common.configuration;

import com.welab.wefe.common.util.StringUtil;
import org.apache.commons.configuration.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * ConfigurationManager.java
 * <p>
 * Copyright 2016 WeLab Holdings, Inc. All rights reserved.
 * WELAB PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 * @author zane.luo
 */
public class ConfigurationManager {

    public static final String CONFIG_FILE = "configFile";
    private static final Logger LOG = LoggerFactory.getLogger(ConfigurationManager.class);

    private static boolean throwExceptionOnMissing = true;
    private static ConfigurationManager configurationManager;
    private CompositeConfiguration config = new CompositeConfiguration();
    private List<String> propertiesFiles = new ArrayList<>();

    private ConfigurationManager(String... files) {
        LOG.info("Start to initialize configuration");
        addPropertyFiles(files);
        config();
        LOG.info("Initialize configuration success");

        config.getKeys().forEachRemaining(key -> {
            if (key.startsWith("cfg")) {
                System.setProperty(key, config.getString(key));
            }
            LOG.debug("{} = {}", key, config.getString(key));
        });

    }

    public static void init(String... files) {
        LOG.info("files:" + StringUtils.join(files, ','));
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

                String envConfigFileSuffix = System.getProperty("spring.profiles.active");
                envConfigFileSuffix = StringUtils.isNotEmpty(envConfigFileSuffix) ? envConfigFileSuffix : springBootProfile.getString("spring.profiles.active");

                if (StringUtils.isNotEmpty(envConfigFileSuffix)) {
                    defaultConfigFile = String.format("config-%s.properties", envConfigFileSuffix);
                }
            } catch (ConfigurationException e) {
                // can be ignored
            }

            String configFile = System.getProperty(CONFIG_FILE);
            if (StringUtil.isEmpty(configFile) || "null".equals(configFile)) {
                /**
                 * Because the parameter value transfer of the configuration file path specified by Flink failed, write it here first.
                 * If there is no configuration file path in the startup parameters, find out whether this file exists.
                 * If so, read this file.
                 */
                if (Files.exists(Paths.get("/mnt/wdf/pangu-app-antifraud-flink-job/config.properties"))) {
                    configFile = "/mnt/wdf/pangu-app-antifraud-flink-job/config.properties";
                } else {
                    configFile = defaultConfigFile;
                }

            }

            LOG.info("configFile: " + configFile);
            configurationManager = new ConfigurationManager(configFile);
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
            config.setThrowExceptionOnMissing(throwExceptionOnMissing);
        } catch (ConfigurationException e) {
            LOG.error("Couldn't find config properties");
        }
        return config;
    }

    private void addPropertyFiles(String... files) {
        propertiesFiles.addAll(Arrays.asList(files));
    }
}
