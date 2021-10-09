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

package com.welab.wefe.board.service.dto.vo;

/**
 * Save the arbiter information of the current member in a process
 *
 * @author winter.zou
 */
public class JobArbiterInfo {
    /**
     * Whether there is an arbiter
     */
    private boolean hasArbiter;

    /**
     * member_id of arbiter
     */
    private String arbiterMemberId;

    public boolean isHasArbiter() {
        return hasArbiter;
    }

    public void setHasArbiter(boolean hasArbiter) {
        this.hasArbiter = hasArbiter;
    }

    public String getArbiterMemberId() {
        return arbiterMemberId;
    }

    public void setArbiterMemberId(String arbiterMemberId) {
        this.arbiterMemberId = arbiterMemberId;
    }

}
