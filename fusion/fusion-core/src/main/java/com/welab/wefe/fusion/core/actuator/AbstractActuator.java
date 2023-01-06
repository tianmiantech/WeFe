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

package com.welab.wefe.fusion.core.actuator;

import com.welab.wefe.common.TimeSpan;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.util.StringUtil;
import com.welab.wefe.fusion.core.utils.FusionThreadPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.LongAdder;

import static com.welab.wefe.common.util.ThreadUtil.sleep;

/**
 * @author hunter.zhao
 */
public abstract class AbstractActuator implements AutoCloseable {
    protected final Logger LOG = LoggerFactory.getLogger(getClass());

    public AbstractActuator(String businessId) {
        this.businessId = businessId;
        ActuatorCache.set(this);
    }

    protected String businessId;

    protected Long dataCount;

    public volatile String error;

    protected LongAdder processedCount = new LongAdder();

    protected LongAdder fusionCount = new LongAdder();

    /**
     * Task start time
     */
    protected final long startTime = System.currentTimeMillis();

    /**
     * Maximum execution time
     *
     * @param minute
     * @return
     */
    public AbstractActuator setMaxExecuteTimeSpan(int minute) {
        return this;
    }

    /**
     * get businessId
     *
     * @return
     */
    public String getBusinessId() {
        return businessId;
    }


    /**
     * Time consuming
     *
     * @return
     */
    public long getSpend() {
        return System.currentTimeMillis() - startTime;
    }

    /**
     * get processedCount
     *
     * @return
     */
    public long getProcessedCount() {
        return processedCount.longValue();
    }

    /**
     * get fusionCount
     *
     * @return
     */
    public long getFusionCount() {
        return fusionCount.longValue();
    }

    /**
     * get dataCount
     *
     * @return
     */
    public long getDataCount() {
        return dataCount.longValue();
    }

    /**
     * Estimated remaining time
     *
     * @return
     */
    public long getEstimatedSpend() {
        if (processedCount.longValue() == 0) {
            return getSpend() * dataCount.longValue();
        }

        return getSpend() / processedCount.longValue()
                * (dataCount.longValue() - processedCount.longValue());
    }

    /**
     * The current progress
     *
     * @return
     */
    public int progress() {
        return Double.valueOf(
                processedCount.doubleValue() / dataCount.doubleValue() * 100
        ).intValue();
    }

    /**
     * Check whether the task is complete
     *
     * @return
     */
    public abstract boolean isFinish();


    /**
     * Executor execution method
     *
     * @throws StatusCodeWithException
     */
    public abstract void fusion() throws Exception;

    public void run() {
        FusionThreadPool.run(() -> execute());
        FusionThreadPool.run(() -> heartbeat());
    }

    private void execute() {
        try {

            LOG.info("task execute...");

            fusion();

            LOG.info("task execute end!");

        } catch (Exception e) {
            e.printStackTrace();
            LOG.info("error: ", e);
            this.error = e.getMessage();
            LOG.info("error message: {}", e.getMessage());
        }
    }

    private void heartbeat() {
        LOG.info("finish waiting...");

        while (true) {
            sleep(1000);

            if (!isFinish() && StringUtil.isEmpty(error)) {
                continue;
            }

            try {
                LOG.info("close task...");
                close();
            } catch (Exception e) {
                LOG.error(e.getClass().getSimpleName() + " close task error：" + e.getMessage());
            }

            LOG.info("{} spend: {} ms", businessId, System.currentTimeMillis() - startTime);
            //remove Actuator
            ActuatorCache.remove(businessId);
            return;
        }
    }
}
