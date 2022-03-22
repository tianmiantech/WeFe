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

package com.welab.wefe.parser;

import com.alibaba.fastjson.JSONObject;
import com.welab.wefe.BlockchainDataSyncApp;
import com.welab.wefe.common.data.mongodb.entity.union.UnionNode;
import com.welab.wefe.common.data.mongodb.entity.union.ext.UnionNodeExtJSON;
import com.welab.wefe.common.data.mongodb.repo.UnionNodeMongoRepo;
import com.welab.wefe.common.util.StringUtil;
import com.welab.wefe.constant.EventConstant;
import com.welab.wefe.exception.BusinessException;
import org.apache.commons.lang3.StringUtils;

/**
 * @author yuxin.zhang
 */
public class UnionNodeContractEventParser extends AbstractParser {
    protected UnionNodeMongoRepo unionNodeMongoRepo = BlockchainDataSyncApp.CONTEXT.getBean(UnionNodeMongoRepo.class);
    protected UnionNodeExtJSON extJSON;

    @Override
    protected void parseContractEvent() throws BusinessException {
        extJSON = StringUtils.isNotEmpty(extJsonStr) ? JSONObject.parseObject(extJsonStr, UnionNodeExtJSON.class) : new UnionNodeExtJSON();
        switch (eventBO.getEventName().toUpperCase()) {
            case EventConstant.UnionNodeEvent.INSERT_EVENT:
                parseInsertEvent();
                break;
            case EventConstant.UnionNodeEvent.UPDATE_EVENT:
                parseUpdateEvent();
                break;
            case EventConstant.UnionNodeEvent.UPDATE_ENABLE_EVENT:
                parseUpdateEnableEvent();
                break;
            case EventConstant.UnionNodeEvent.UPDATE_PUBLIC_KEY_EVENT:
                parseUpdatePublicKeyEvent();
                break;
            case EventConstant.UnionNodeEvent.DELETE_BY_UNIONNODEID_EVENT:
                parseDeleteByUnionNodeIdEvent();
                break;
            case EventConstant.UPDATE_EXTJSON_EVENT:
                parseUpdateExtJson();
                break;
            default:
                throw new BusinessException("event name valid:" + eventBO.getEventName());
        }
    }

    private void parseInsertEvent() {
        UnionNode unionNode = new UnionNode();
        unionNode.setNodeId(StringUtil.strTrim2(params.getString(0)));
        unionNode.setBlockchainNodeId(StringUtil.strTrim2(params.getString(1)));
        unionNode.setBaseUrl(StringUtil.strTrim2(params.getString(2)));
        unionNode.setOrganizationName(StringUtil.strTrim2(params.getString(3)));
        unionNode.setLostContact(StringUtil.strTrim2(params.getString(4)));
        unionNode.setContactEmail(StringUtil.strTrim2(params.getString(5)));
        unionNode.setPriorityLevel(StringUtil.strTrim2(params.getString(6)));
        unionNode.setEnable("0");
        unionNode.setVersion(StringUtil.strTrim2(params.getString(7)));
        unionNode.setPublicKey(StringUtil.strTrim2(params.getString(8)));
        unionNode.setCreatedTime(StringUtil.strTrim2(params.getString(9)));
        unionNode.setUpdatedTime(StringUtil.strTrim2(params.getString(10)));
        unionNode.setExtJson(extJSON);

        unionNodeMongoRepo.save(unionNode);

    }

    private void parseUpdateEvent() {
        String nodeId = eventBO.getEntity().get("node_id").toString();
        String baseUrl = StringUtil.strTrim2(params.getString(0));
        String organizationName = StringUtil.strTrim2(params.getString(1));
        String contactEmail = StringUtil.strTrim2(params.getString(2));
        String updatedTime = StringUtil.strTrim2(params.getString(3));
        unionNodeMongoRepo.update(nodeId,baseUrl,organizationName,contactEmail,updatedTime);
    }

    private void parseUpdateEnableEvent() {
        String unionNodeId = eventBO.getEntity().get("node_id").toString();
        String enable = eventBO.getEntity().get("enable").toString();
        String updatedTime = eventBO.getEntity().get("updated_time").toString();
        unionNodeMongoRepo.updateEnable(unionNodeId, enable, updatedTime);
    }

    private void parseUpdatePublicKeyEvent() {
        String unionNodeId = eventBO.getEntity().get("node_id").toString();
        String publicKey = eventBO.getEntity().get("public_key").toString();
        String updatedTime = eventBO.getEntity().get("updated_time").toString();
        unionNodeMongoRepo.updatePublicKey(unionNodeId, publicKey, updatedTime);
    }

    private void parseDeleteByUnionNodeIdEvent() {
        String unionNodeId = eventBO.getEntity().get("node_id").toString();
        unionNodeMongoRepo.deleteByUnionNodeId(unionNodeId);
    }

    private void parseUpdateExtJson() {
        String unionNodeId = eventBO.getEntity().get("node_id").toString();
        unionNodeMongoRepo.updateExtJSONById(unionNodeId, extJSON);
    }

}
