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
import com.welab.wefe.common.data.mongodb.entity.union.MemberFileInfo;
import com.welab.wefe.common.data.mongodb.entity.union.ext.MemberFileInfoExtJSON;
import com.welab.wefe.common.data.mongodb.repo.MemberFileInfoMongoRepo;
import com.welab.wefe.common.util.StringUtil;
import com.welab.wefe.constant.EventConstant;
import com.welab.wefe.exception.BusinessException;
import org.apache.commons.lang3.StringUtils;

/**
 * @author yuxin.zhang
 */
public class MemberFileInfoContractEventParser extends AbstractParser {
    protected MemberFileInfoMongoRepo unionNodeMongoRepo = BlockchainDataSyncApp.CONTEXT.getBean(MemberFileInfoMongoRepo.class);
    protected MemberFileInfoExtJSON extJSON;

    @Override
    protected void parseContractEvent() throws BusinessException {
        extJSON = StringUtils.isNotEmpty(extJsonStr) ? JSONObject.parseObject(extJsonStr, MemberFileInfoExtJSON.class) : new MemberFileInfoExtJSON();
        switch (eventBO.getEventName().toUpperCase()) {
            case EventConstant.UnionNode.INSERT_EVENT:
                parseInsertEvent();
                break;
            case EventConstant.UnionNode.UPDATE_ENABLE_EVENT:
                parseUpdateEnableEvent();
                break;
            case EventConstant.UPDATE_EXTJSON_EVENT:
                parseUpdateExtJson();
                break;
            default:
                throw new BusinessException("event name valid:" + eventBO.getEventName());
        }
    }

    private void parseInsertEvent() {
        MemberFileInfo memberFileInfo = new MemberFileInfo();
        memberFileInfo.setFileId(StringUtil.strTrim2(params.getString(0)));
        memberFileInfo.setFileSign(StringUtil.strTrim2(params.getString(1)));
        memberFileInfo.setFileName(StringUtil.strTrim2(params.getString(2)));
        memberFileInfo.setFileSize(StringUtil.strTrim2(params.getString(3)));
        memberFileInfo.setMemberId(StringUtil.strTrim2(params.getString(4)));
        memberFileInfo.setBlockchainNodeId(StringUtil.strTrim2(params.getString(5)));
        memberFileInfo.setPurpose(StringUtil.strTrim2(params.getString(6)));
        memberFileInfo.setDescribe(StringUtil.strTrim2(params.getString(7)));
        memberFileInfo.setEnable(StringUtil.strTrim2(params.getString(8)));
        memberFileInfo.setCreatedTime(StringUtil.strTrim2(params.getString(9)));
        memberFileInfo.setUpdatedTime(StringUtil.strTrim2(params.getString(10)));
        memberFileInfo.setExtJson(extJSON);

        unionNodeMongoRepo.save(memberFileInfo);

    }


    private void parseUpdateEnableEvent() {
        String fileId = eventBO.getEntity().get("file_id").toString();
        String enable = eventBO.getEntity().get("enable").toString();
        String updatedTime = eventBO.getEntity().get("updated_time").toString();
        unionNodeMongoRepo.updateEnable(fileId, enable, updatedTime);
    }


    private void parseUpdateExtJson() {
        String unionNodeId = eventBO.getEntity().get("file_id").toString();
        String updatedTime = eventBO.getEntity().get("updated_time").toString();
        unionNodeMongoRepo.updateExtJSONById(unionNodeId, updatedTime, extJSON);
    }

}
