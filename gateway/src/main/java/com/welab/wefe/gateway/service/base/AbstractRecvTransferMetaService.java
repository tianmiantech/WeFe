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

import com.welab.wefe.gateway.api.meta.basic.BasicMetaProto;
import com.welab.wefe.gateway.api.meta.basic.GatewayMetaProto;

/**
 * Pull the data service base class submitted by the remote gateway
 *
 * @author aaron.li
 **/
public abstract class AbstractRecvTransferMetaService {


    /**
     * Pull the data submitted by the remote gateway from the local data cache
     * <p>
     * This method blocks until it times out
     * </p>
     */
    public abstract GatewayMetaProto.TransferMeta recv(GatewayMetaProto.TransferMeta transferMeta);

    /**
     * Query data status submitted by remote gateway
     *
     * <p>
     * This method is non blocking
     * </p>
     */
    public abstract GatewayMetaProto.TransferMeta checkStatusNow(GatewayMetaProto.TransferMeta transferMeta);

    /**
     * Process messages submitted by remote gateway
     *
     * @param transferMeta Message submitted by remote gateway
     * @return Processing results
     */
    public abstract BasicMetaProto.ReturnStatus doHandle(GatewayMetaProto.TransferMeta transferMeta);
}
