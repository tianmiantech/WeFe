/**
 * Copyright 2021 Tianmian Tech. All Rights Reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.welab.wefe.union.service.listener;

import com.welab.wefe.common.data.mongodb.entity.union.UnionNode;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.union.service.service.UnionNodeContractService;
import org.fisco.bcos.sdk.client.protocol.response.NodeInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

/**
 * @author yuxin.zhang
 **/
@Component
public class RegisterNodeInfoListener implements ApplicationListener<ApplicationStartedEvent> {
    private static final Logger LOG = LoggerFactory.getLogger(RegisterNodeInfoListener.class);

    @Autowired
    private String currentNodeId;
    @Autowired
    private UnionNodeContractService unionNodeContractService;

    @Override
    public void onApplicationEvent(ApplicationStartedEvent event) {
        registerUnionNode();
    }


    private void registerUnionNode() {
        UnionNode unionNode = new UnionNode();
        unionNode.setBlockchainNodeId(currentNodeId);
        try {
            unionNodeContractService.add(unionNode);
        } catch (StatusCodeWithException e) {
            LOG.error("registerUnionNode to blockchain failed", e);
            System.exit(1);
        }
    }
}
