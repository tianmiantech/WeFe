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

package com.welab.wefe.common;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;

/**
 * Code table
 *
 * @author Zane
 */
public class Stopwatch {
    private static final String EMPTY_LOG_NAME = "";
    private static final int DEFAULT_LABEL_QUEUE_CAPACITY = 100;

    private Logger logger = LoggerFactory.getLogger(Stopwatch.class);
    private Long lastTapTime;
    private Long startTime;
    private LinkedList<Label> labelQueue;
    private int maxLabelQueueLength;

    private Stopwatch(int maxLabelQueueLength) {
        this.labelQueue = new LinkedList();
        this.maxLabelQueueLength = maxLabelQueueLength;
        this.lastTapTime = System.currentTimeMillis();
        this.startTime = this.lastTapTime;
    }

    public long getTotalSpend() {
        return System.currentTimeMillis() - startTime;
    }

    /**
     * Create and start a code table.
     */
    public static Stopwatch startNew() {
        return new Stopwatch(DEFAULT_LABEL_QUEUE_CAPACITY);
    }

    /**
     * Create and start a code table.
     *
     * @param maxLabelQueueLength The maximum length of the queue used to save timestamp records in the code table
     */
    public static Stopwatch startNew(int maxLabelQueueLength) {
        return new Stopwatch(maxLabelQueueLength);
    }

    /**
     * Mark a timestamp in the code table
     */
    public Label tap() {
        return tap(EMPTY_LOG_NAME);
    }

    /**
     * Mark a timestamp in the code table
     *
     * @param name The name of the tag
     */
    public Label tap(String name) {
        Label item;
        synchronized (labelQueue) {
            long now = System.currentTimeMillis();
            long spend = now - lastTapTime;
            this.lastTapTime = now;
            item = new Label(name, spend);

            if (labelQueue.size() > maxLabelQueueLength) {
                labelQueue.poll();
            }
            labelQueue.offer(item);
        }

        return item;
    }

    /**
     * Mark the time once and print the time interval between the current time and the last tap.
     *
     * @param name The name of the tag
     */
    public Label tapAndPrint(String name) {
        Label item = tap(name);
        item.log();
        return item;
    }

    public void printAllTap() {
        for (Label label : labelQueue) {
            label.log();
        }
    }

    /**
     * The code table generates a label every tap
     */
    public class Label {
        public String name;
        public long spend;
        public long createTime;

        private Label(String name, long spend) {
            this.name = name;
            this.spend = spend;
            this.createTime = System.currentTimeMillis();
        }

        @Override
        public String toString() {
            StringBuilder str = new StringBuilder();
            str.append("[spend]");
            if (StringUtils.isNotEmpty(name)) {
                str.append(" " + name);
            }
            str.append(": ").append(spend).append(" ms");
            return str.toString();
        }

        /**
         * Recalculate time using current time
         * <p>
         * Used for scenarios where lasttaptime cannot be used to calculate time-consuming in a multithreaded environment
         *
         * @param args string format args
         */
        public void refreshAndPrint(String name, Object... args) {
            this.spend = System.currentTimeMillis() - this.createTime;
            this.name = String.format(name, args);
            log();
        }

        public void log() {
            logger.info(this.toString());
        }
    }
}
