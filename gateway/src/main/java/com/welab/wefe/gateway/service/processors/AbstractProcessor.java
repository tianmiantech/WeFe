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

package com.welab.wefe.gateway.service.processors;

import com.welab.wefe.gateway.api.meta.basic.BasicMetaProto;
import com.welab.wefe.gateway.api.meta.basic.GatewayMetaProto;
import com.welab.wefe.gateway.common.ReturnStatusBuilder;
import com.welab.wefe.gateway.service.base.AbstractSendTransferMetaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Processor base class
 *
 * @author aaron.li
 **/
public abstract class AbstractProcessor {
    protected Logger LOG = LoggerFactory.getLogger(getClass());

    @Autowired
    private AbstractSendTransferMetaService sendTransferMetaService;

    /**
     * This method is called before the message is pushed to the remote end.
     * <p>
     * If the subclass needs to push the message to the remote end after data processing, it can directly call the following toRemote method
     * </p>
     *
     * @param transferMeta Messages submitted by the client
     * @return Processing response status
     */
    public BasicMetaProto.ReturnStatus beforeSendToRemote(GatewayMetaProto.TransferMeta transferMeta) {
        return toRemote(transferMeta);
    }

    /**
     * Push message to remote
     *
     * <p>
     * Note: generally, this method does not need to be overridden for subclasses
     * </p>
     *
     * @param transferMeta Messages submitted by the client
     * @return Processing response status
     */
    protected BasicMetaProto.ReturnStatus toRemote(GatewayMetaProto.TransferMeta transferMeta) {
        return sendTransferMetaService.pushToRemote(transferMeta);
    }

    /**
     * After the remote end receives the message, the remote end calls this method to process the received message
     *
     * @param transferMeta Received message
     * @return Processing response status
     */
    public BasicMetaProto.ReturnStatus remoteProcess(GatewayMetaProto.TransferMeta transferMeta) {
        return ReturnStatusBuilder.ok(transferMeta.getSessionId());
    }
}
