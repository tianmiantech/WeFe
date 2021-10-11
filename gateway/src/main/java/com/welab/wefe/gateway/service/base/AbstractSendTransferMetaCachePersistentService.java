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

package com.welab.wefe.gateway.service.base;

import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.gateway.api.meta.basic.GatewayMetaProto;

import java.util.List;

/**
 * Message persistence service base class
 *
 * @author aaron.li
 **/
public abstract class AbstractSendTransferMetaCachePersistentService {

    /**
     * Persistent message
     *
     * @param transferMeta message
     * @return true：Persistence success; false：Persistence failed
     */
    public abstract StatusCodeWithException save(GatewayMetaProto.TransferMeta transferMeta);

    /**
     * Delete persistent message
     *
     * @param transferMeta message
     * @return true：Delete success; false：Delete failed
     */
    public abstract boolean delete(GatewayMetaProto.TransferMeta transferMeta);

    /**
     * Load all persistent messages into memory
     *
     * @return all persistent messages
     */
    public abstract List<GatewayMetaProto.TransferMeta> findAll();
}
