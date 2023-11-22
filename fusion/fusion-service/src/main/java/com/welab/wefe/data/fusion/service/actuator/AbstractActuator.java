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

package com.welab.wefe.data.fusion.service.actuator;

import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.util.JObject;
import com.welab.wefe.data.fusion.service.manager.TaskResultManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.atomic.LongAdder;

/**
 * Algorithm abstract class
 *
 * @author hunter.zhao
 */
public abstract class AbstractActuator implements AutoCloseable {
    protected final Logger LOG = LoggerFactory.getLogger(getClass());

    public String businessId;

    public Integer dataCount;

    public LongAdder processedCount = new LongAdder();

    public LongAdder fusionCount = new LongAdder();

    /**
     * Initializes the actuator
     *
     * @throws StatusCodeWithException
     */
    public abstract void init() throws StatusCodeWithException;

    /**
     * Executor execution method
     *
     * @throws StatusCodeWithException
     */
    public abstract void handle() throws StatusCodeWithException;

    /**
     * Alignment data into the library implementation method
     *
     * @param fruit
     */
    public abstract void dump(List<JObject> fruit);

    public AbstractActuator(String businessId, Integer dataCount) {
        this.businessId = businessId;
        this.dataCount = dataCount;
    }

    private boolean DUMP_TABLE_EXIST = false;

    public synchronized void createTable(String businessId, List<String> rows) {
        /*
         * Create a table if no table exists
         */
        if (!DUMP_TABLE_EXIST) {
            LOG.info("create fruit table...");
            TaskResultManager.createTaskResultTable(businessId, rows);
            DUMP_TABLE_EXIST = true;
        }
    }
}
