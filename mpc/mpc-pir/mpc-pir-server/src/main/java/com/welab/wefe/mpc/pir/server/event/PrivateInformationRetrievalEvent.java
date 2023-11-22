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

package com.welab.wefe.mpc.pir.server.event;

import com.welab.wefe.mpc.pir.server.flow.PrivateInformationRetrievalFlowServer;

import java.util.List;

/**
 * @author eval
 */
public class PrivateInformationRetrievalEvent {
    String uuid;
    List<Object> keys;

    PrivateInformationRetrievalFlowServer mPrivateInformationRetrieval;

    public PrivateInformationRetrievalEvent(String uuid, List<Object> keys) {
        this.uuid = uuid;
        this.keys = keys;
    }

    public PrivateInformationRetrievalEvent(String uuid, List<Object> keys, PrivateInformationRetrievalFlowServer privateInformationRetrieval) {
        this.uuid = uuid;
        this.keys = keys;
        mPrivateInformationRetrieval = privateInformationRetrieval;
    }

    public PrivateInformationRetrievalFlowServer getPrivateInformationRetrieval() {
        return mPrivateInformationRetrieval;
    }

    public String getUuid() {
        return uuid;
    }

    public List<Object> getKeys() {
        return keys;
    }

}
