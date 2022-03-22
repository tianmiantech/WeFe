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

package com.welab.wefe.union.service.listener;

import com.welab.wefe.common.data.mongodb.entity.union.UnionNode;
import com.welab.wefe.common.data.mongodb.entity.union.UnionNodeSm2Config;
import com.welab.wefe.common.data.mongodb.repo.UnionNodeConfigMongoRepo;
import com.welab.wefe.common.data.mongodb.repo.UnionNodeMongoRepo;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.util.SM2Util;
import com.welab.wefe.common.util.StringUtil;
import com.welab.wefe.union.service.cache.UnionNodeConfigCache;
import com.welab.wefe.union.service.constant.UnionNodeConfigType;
import com.welab.wefe.union.service.service.UnionNodeContractService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
    private String currentBlockchainNodeId;
    @Autowired
    private UnionNodeContractService unionNodeContractService;
    @Autowired
    private UnionNodeMongoRepo unionNodeMongoRepo;
    @Autowired
    private UnionNodeConfigMongoRepo unionNodeConfigMongoRepo;
    @Value("${organization.name}")
    private String organizationName;

    @Override
    public void onApplicationEvent(ApplicationStartedEvent event) {
        registerUnionNode();
    }


    private void registerUnionNode() {
        try {
            if (StringUtil.isEmpty(organizationName)) {
                LOG.error("registerUnionNode to blockchain failed,Please configure organizationname");
                System.exit(1);
            }
            UnionNodeSm2Config unionNodeSm2Config = unionNodeConfigMongoRepo.find();

            UnionNode unionNode = unionNodeMongoRepo.findByBlockchainNodeId(currentBlockchainNodeId);
            if (unionNode == null) {
                if (unionNodeSm2Config == null) {
                    unionNodeSm2Config = new UnionNodeSm2Config(UnionNodeConfigType.SM2.name());
                    SM2Util.Sm2KeyPair sm2KeyPair = SM2Util.generateKeyPair();
                    unionNodeSm2Config.setPrivateKey(sm2KeyPair.privateKey);
                    unionNodeSm2Config.setPublicKey(sm2KeyPair.publicKey);
                    unionNodeConfigMongoRepo.save(unionNodeSm2Config);
                }
                unionNode = new UnionNode();
                unionNode.setNodeId(unionNodeSm2Config.getNodeId());
                unionNode.setPublicKey(unionNodeSm2Config.getPublicKey());
                unionNode.setBlockchainNodeId(currentBlockchainNodeId);
                unionNode.setOrganizationName(organizationName);
                unionNode.setLostContact("0");
                unionNodeContractService.add(unionNode);
            } else {
                if (unionNodeSm2Config == null) {
                    unionNodeSm2Config = new UnionNodeSm2Config(UnionNodeConfigType.SM2.name());
                    SM2Util.Sm2KeyPair sm2KeyPair = SM2Util.generateKeyPair();
                    unionNodeSm2Config.setNodeId(unionNode.getNodeId());
                    unionNodeSm2Config.setPrivateKey(sm2KeyPair.privateKey);
                    unionNodeSm2Config.setPublicKey(sm2KeyPair.publicKey);
                    unionNodeConfigMongoRepo.save(unionNodeSm2Config);
                    unionNodeContractService.updatePublicKey(unionNode.getNodeId(), unionNodeSm2Config.getPublicKey());
                } else {
                    if (!unionNodeSm2Config.getPublicKey().equals(unionNode.getPublicKey())) {
                        unionNodeContractService.updatePublicKey(unionNode.getNodeId(), unionNodeSm2Config.getPublicKey());
                    }
                }

            }

            UnionNodeConfigCache.currentBlockchainNodeId = currentBlockchainNodeId;
            UnionNodeConfigCache.setUnionNodeSm2Config(unionNodeSm2Config);
        } catch (StatusCodeWithException e) {
            LOG.error("registerUnionNode to blockchain failed", e);
            System.exit(1);
        }
    }
}
