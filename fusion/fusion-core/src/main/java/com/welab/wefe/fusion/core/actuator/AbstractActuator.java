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

package com.welab.wefe.fusion.core.actuator;

import com.welab.wefe.common.TimeSpan;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.util.JObject;
import com.welab.wefe.fusion.core.actuator.psi.AbstractPsiClientActuator;
import com.welab.wefe.fusion.core.utils.FusionThreadPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.atomic.LongAdder;

import static com.welab.wefe.common.util.ThreadUtil.sleep;

/**
 * @author hunter.zhao
 */
public abstract class AbstractActuator implements AutoCloseable {
    protected final Logger LOG = LoggerFactory.getLogger(getClass());

    public AbstractActuator(String businessId) {
        this.businessId = businessId;
    }

    protected String businessId;

    public Integer dataCount;

    public LongAdder processedCount = new LongAdder();

    public LongAdder fusionCount = new LongAdder();


    /**
     * Task start time
     */
    public long startTime = System.currentTimeMillis();

    /**
     * Maximum execution time of a task
     */
    private TimeSpan maxExecuteTimeSpan = new TimeSpan(30 * 60 * 1000);

    /**
     * Maximum execution time
     *
     * @param minute
     * @return
     */
    public AbstractActuator setMaxExecuteTimeSpan(int minute) {
        this.maxExecuteTimeSpan = new TimeSpan(minute * 60 * 1000L);
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
    public Integer progress() {
        return processedCount.intValue() / dataCount.intValue();
    }

    protected void preprocess() {
    }

    protected void postprocess() {
    }

    /**
     * Check whether the task is complete
     *
     * @return
     */
    public abstract boolean isFinish();


    /**
     * Initializes the task
     *
     * @throws StatusCodeWithException
     */
    public abstract void init() throws StatusCodeWithException;

    /**
     * Executor execution method
     *
     * @throws StatusCodeWithException
     */
    public abstract void fusion() throws StatusCodeWithException;

    /**
     * Alignment data into the library implementation method
     *
     * @param fruit
     */
    public abstract void dump(List<JObject> fruit);

    public void run() {
        FusionThreadPool.run(() -> execute());
        FusionThreadPool.run(() -> finish());
    }

    private void execute() {
        try {

            LOG.info("task execute...");

            preprocess();

            init();

            fusion();

            postprocess();

            LOG.info("task execute end!");

        } catch (Exception e) {
            e.printStackTrace();
            LOG.info("error: ", e);
            LOG.error(e.getClass().getSimpleName() + " " + e.getMessage());
        }
    }

    public void finish() {
        LOG.info("finish waiting...");

        while (true) {
            sleep(1000);

            if (System.currentTimeMillis() - startTime < maxExecuteTimeSpan.toMs() && !isFinish()) {
                continue;
            }

            try {
                if (this instanceof AbstractPsiClientActuator) {
                    LOG.info("notify the server that the task has ended...");
                    ((AbstractPsiClientActuator) this).notifyServerClose();
                }
            } catch (Exception e) {
                LOG.error(e.getClass().getSimpleName() + " notify the server error：" + e.getMessage());
            }

            try {
                LOG.info("close task...");
                close();
            } catch (Exception e) {
                LOG.error(e.getClass().getSimpleName() + " close task error：" + e.getMessage());
            }

            LOG.info("{} spend: {} ms", businessId, System.currentTimeMillis() - startTime);
            return;
        }
    }
}
