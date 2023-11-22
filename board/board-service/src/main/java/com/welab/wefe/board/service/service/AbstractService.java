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

package com.welab.wefe.board.service.service;

import com.welab.wefe.board.service.constant.Config;
import com.welab.wefe.board.service.sdk.union.UnionService;
import com.welab.wefe.board.service.service.globalconfig.GlobalConfigService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author Zane
 */
public class AbstractService {
    protected final Logger LOG = LoggerFactory.getLogger(this.getClass());

    @Autowired
    protected GatewayService gatewayService;
    @Autowired
    protected UnionService unionService;
    @Autowired
    protected Config config;
    @Autowired
    protected GlobalConfigService globalConfigService;

    protected void log(Exception e) {
        LOG.error(e.getClass() + " " + e.getMessage(), e);
    }
}
