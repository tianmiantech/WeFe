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

package com.welab.wefe.gateway.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author zane
 */
public class AbstractService {
    protected final Logger LOG = LoggerFactory.getLogger(this.getClass());

    protected void log(Exception e) {
        LOG.error(e.getClass() + " " + e.getMessage(), e);
    }
}
