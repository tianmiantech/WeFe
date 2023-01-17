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

package com.welab.wefe.data.fusion.service.task;

import static com.welab.wefe.common.util.ThreadUtil.sleep;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.welab.wefe.common.CommonThreadPool;
import com.welab.wefe.common.TimeSpan;
import com.welab.wefe.common.util.StringUtil;
import com.welab.wefe.data.fusion.service.actuator.AbstractActuator;
import com.welab.wefe.data.fusion.service.actuator.rsapsi.AbstractPsiActuator;
import com.welab.wefe.data.fusion.service.enums.PSIActuatorStatus;

/**
 * @author hunter.zhao
 */
public abstract class AbstractTask<T extends AbstractActuator> implements AutoCloseable {
    protected final Logger LOG = LoggerFactory.getLogger(getClass());

    public T actuator;

    protected String businessId;

    protected String name;

    private volatile String error;

    /**
     * Task start time
     */
    public long startTime = System.currentTimeMillis();

    /**
     * Maximum execution time of a task
     */
    private TimeSpan maxExecuteTimeSpan = new TimeSpan(100 * 60 * 1000);

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
        return Double.valueOf(
                actuator.processedCount.doubleValue() / actuator.dataCount.doubleValue() * 100
        ).intValue();
    }


    protected void preprocess() throws Exception {
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
     * Determine the status of the actuator
     *
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
            LOG.info("fusion task log , task execute, begin");
            preprocess();
            LOG.info("fusion task log , task execute, preprocess finished");
            actuator.init();
            LOG.info("fusion task log , task execute, init finished");
            actuator.handle();
            LOG.info("fusion task log , task execute, handle finished");
            postprocess();
            LOG.info("fusion task log , execute() status ： {} ", status().name());
        } catch (Exception e) {
            LOG.error("execute error ", e);
            error = e.getMessage();
        }
    }

    public void finish() {
        LOG.info("fusion task log , finish waiting... begin");
        while (true) {
            sleep(1000);
            LOG.info("fusion task log , finish waiting...");
            if (System.currentTimeMillis() - startTime < maxExecuteTimeSpan.toMs() && !isFinish() && StringUtil.isEmpty(error)) {
                continue;
            }
            LOG.info("fusion task log , actuator.status = " + ((AbstractPsiActuator)actuator).status);
            try {
                LOG.info("fusion task log , close actuator...");
                actuator.close();
            } catch (Exception e) {
                LOG.error(e.getClass().getSimpleName() + " close actuator error：" , e);
            }

            try {
                LOG.info("fusion task log , close task...");
                close();
            } catch (Exception e) {
                LOG.error(e.getClass().getSimpleName() + " close task error：", e);
            }

            return;
        }
    }
}
