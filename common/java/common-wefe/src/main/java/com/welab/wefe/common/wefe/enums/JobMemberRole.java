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

package com.welab.wefe.common.wefe.enums;

/**
 * Role of member in the task
 *
 * @author Zane
 */
public enum JobMemberRole {
    /**
     * Task initiator
     */
    promoter(0),
    /**
     * Task collaborator
     */
    provider(1),
    /**
     * arbiter
     */
    arbiter(2);

    /**
     * Sort sequence number
     */
    private int index;

    JobMemberRole(int index) {
        this.index = index;
    }

    public int getIndex() {
        return index;
    }
}
