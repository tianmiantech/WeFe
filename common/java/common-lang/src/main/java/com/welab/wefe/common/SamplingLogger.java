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

package com.welab.wefe.common;

import org.slf4j.Logger;

import java.util.concurrent.atomic.LongAdder;

/**
 * Log sampling component
 * <p>
 * Output the logs after sampling to avoid outputting too many logs.
 *
 * @author Zane
 */
public class SamplingLogger {
    private Logger log;
    private long maxCount;
    private TimeSpan maxInterval;
    private Runnable lastAction;
    private LongAdder counter = new LongAdder();
    private long lastPrintTime = 0;

    public SamplingLogger(Logger log, long maxCount, TimeSpan maxInterval) {
        this.log = log;
        this.maxCount = maxCount;
        this.maxInterval = maxInterval;
    }

    /**
     * Sampling logger with quantity interval only
     */
    public static SamplingLogger of(Logger log, long maxCount) {
        return new SamplingLogger(log, maxCount, TimeSpan.fromDays(1));
    }

    /**
     * Set sampling logger for time interval only
     */
    public static SamplingLogger of(Logger log, TimeSpan maxInterval) {
        return new SamplingLogger(log, Long.MAX_VALUE, maxInterval);
    }

    /**
     * Set the sampling logger of quantity interval and time interval at the same time
     */
    public static SamplingLogger of(Logger log, long maxCount, TimeSpan maxInterval) {
        return new SamplingLogger(log, maxCount, maxInterval);
    }

    /**
     * Execute log printing
     */
    private void emit() {
        boolean onTime = System.currentTimeMillis() - lastPrintTime >= maxInterval.toMs();
        boolean onCount = counter.longValue() >= maxCount;
        if (onTime || onCount) {
            lastAction.run();
            lastPrintTime = System.currentTimeMillis();
            counter.reset();
        }
    }

    public void info(String format, Object... arguments) {
        lastAction = () -> log.info(format, arguments);
        counter.increment();
        emit();
    }

    public void error(String format, Object... arguments) {
        lastAction = () -> log.error(format, arguments);
        counter.increment();
        emit();
    }


    public void error(final Exception ex) {
        lastAction = () -> log.error(ex.getClass().getSimpleName() + " " + ex.getMessage(), ex);
        counter.increment();
        emit();
    }

}
