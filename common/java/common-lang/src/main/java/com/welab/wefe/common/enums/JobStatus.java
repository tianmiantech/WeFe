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

package com.welab.wefe.common.enums;

/**
 * Task status
 *
 * @author Zane
 */
public enum JobStatus {
    /**
     * Waiting for running
     */
    wait_run,
    /**
     * In the operation of the
     */
    running,
    /**
     * Waiting for the end
     */
    wait_stop,
    /**
     * Manual shutdown (end task)
     */
    stop_on_running,
    /**
     * Program closed abnormally
     */
    error_on_running,
    /**
     * Successful (normal end)
     */
    success,
    /**
     * Wait for success
     */
    wait_success;

    public boolean finished() {
        return this == stop_on_running ||
                this == error_on_running ||
                this == success;
    }

    public boolean onStoping() {
        return this == wait_stop || this == wait_success;
    }
}
