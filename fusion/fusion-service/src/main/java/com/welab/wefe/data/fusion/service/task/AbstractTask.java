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

package com.welab.wefe.data.fusion.service.task;

import com.welab.wefe.common.CommonThreadPool;
import com.welab.wefe.common.TimeSpan;
import com.welab.wefe.data.fusion.service.actuator.AbstractActuator;
import com.welab.wefe.data.fusion.service.enums.PSIActuatorStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.welab.wefe.common.util.ThreadUtil.sleep;

/**
 * @author hunter.zhao
 */
public abstract class AbstractTask<T extends AbstractActuator> implements AutoCloseable {
    protected final Logger LOG = LoggerFactory.getLogger(getClass());

    public T actuator;

    protected String businessId;

    protected String name;

    /**
     * Task start time
     */
    public long startTime = System.currentTimeMillis();

    /**
     * Maximum execution time of a task
     */
    private TimeSpan maxExecuteTimeSpan = new TimeSpan(30 * 60 * 1000);

    public AbstractTask(String businessId, T actuator) {
        this.actuator = actuator;
        this.businessId = businessId;
    }

    /**
     * Maximum execution time
     *
     * @param minute
     * @return
     */
    public AbstractTask setMaxExecuteTimeSpan(int minute) {
        this.maxExecuteTimeSpan = new TimeSpan(minute * 60 * 1000);
        return this;
    }

    public String getBusinessId() {
        return this.businessId;
    }

    public Integer getDataCount() {
        return actuator.dataCount;
    }

    public Integer getFusionCount() {
        return actuator.fusionCount.intValue();
    }

    public Integer getProcessedCount() {
        return actuator.processedCount.intValue();
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
        if (actuator.processedCount.longValue() == 0) {
            return getSpend() * actuator.dataCount.longValue();
        }

        return getSpend() / actuator.processedCount.longValue()
                * (actuator.dataCount.longValue() - actuator.processedCount.longValue());
    }

    /**
     * The current progress
     *
     * @return
     */
    public Integer progress() {
        return actuator.processedCount.intValue() / actuator.dataCount.intValue();
    }


    protected void preprocess() throws Exception {
    }

    protected void postprocess() {
    }

    /**
     * Check whether the task is complete
     * @return
     */
    public abstract boolean isFinish();

    /**
     * Determine the status of the actuator
     * @return
     */
    protected abstract PSIActuatorStatus status();

    public void run() {
        CommonThreadPool.run(() -> execute());


        Thread thread = new Thread(() -> finish());
        thread.start();
    }


    private void execute() {
        try {

            LOG.info("task execute...");

            preprocess();

            actuator.init();

            actuator.handle();

            postprocess();

            LOG.info("execute() status ： {} ", status().name());
        } catch (Exception e) {
            e.printStackTrace();
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
                LOG.info("close actuator...");
                actuator.close();
            } catch (Exception e) {
                LOG.error(e.getClass().getSimpleName() + " close actuator error：" + e.getMessage());
            }

            try {
                LOG.info("close task...");
                close();
            } catch (Exception e) {
                LOG.error(e.getClass().getSimpleName() + " close task error：" + e.getMessage());
            }

            return;
        }
    }
}
