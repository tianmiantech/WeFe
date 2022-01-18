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
import com.welab.wefe.common.data.mongodb.entity.union.MemberAuthType;
import com.welab.wefe.common.data.mongodb.entity.union.ext.MemberAuthTypeExtJSON;
import com.welab.wefe.common.data.mongodb.repo.MemberAuthTypeMongoRepo;
import com.welab.wefe.common.util.StringUtil;
import com.welab.wefe.constant.EventConstant;
import com.welab.wefe.exception.BusinessException;
import org.apache.commons.lang3.StringUtils;

/**
 * @author yuxin.zhang
 */
public class MemberAuthTypeContractEventParser extends AbstractParser {
    protected MemberAuthTypeMongoRepo memberAuthTypeMongoRepo = BlockchainDataSyncApp.CONTEXT.getBean(MemberAuthTypeMongoRepo.class);
    protected MemberAuthTypeExtJSON extJSON;

    @Override
    protected void parseContractEvent() throws BusinessException {
        extJSON = StringUtils.isNotEmpty(extJsonStr) ? JSONObject.parseObject(extJsonStr, MemberAuthTypeExtJSON.class) : new MemberAuthTypeExtJSON();
        switch (eventBO.getEventName().toUpperCase()) {
            case EventConstant.MemberAuthType.INSERT_EVENT:
                parseInsertEvent();
                break;
            case EventConstant.MemberAuthType.UPDATE_EVENT:
                parseUpdateEvent();
                break;
            case EventConstant.MemberAuthType.DELETE_BY_TYPEID_EVENT:
                parseDeleteByTypeIdEvent();
                break;
            case EventConstant.UPDATE_EXTJSON_EVENT:
                parseUpdateExtJson();
                break;
            default:
                throw new BusinessException("event name valid:" + eventBO.getEventName());
        }
    }

    private void parseInsertEvent() {
        MemberAuthType memberAuthType = new MemberAuthType();
        memberAuthType.setTypeId(StringUtil.strTrim2(params.getString(0)));
        memberAuthType.setTypeName(StringUtil.strTrim2(params.getString(1)));
        memberAuthType.setCreatedTime(StringUtil.strTrim2(params.getString(2)));
        memberAuthType.setUpdatedTime(StringUtil.strTrim2(params.getString(3)));
        memberAuthType.setExtJson(extJSON);

        memberAuthTypeMongoRepo.save(memberAuthType);

    }

    private void parseUpdateEvent() {
        String typeId = eventBO.getEntity().get("type_id").toString();
        String typeName = eventBO.getEntity().get("type_name").toString();
        String updatedTime = eventBO.getEntity().get("updated_time").toString();
        memberAuthTypeMongoRepo.update(typeId, typeName, updatedTime);
    }

    private void parseDeleteByTypeIdEvent() {
        String typeId = eventBO.getEntity().get("type_id").toString();
        memberAuthTypeMongoRepo.deleteByTypeId(typeId);
    }

    private void parseUpdateExtJson() {
        String typeId = eventBO.getEntity().get("type_id").toString();
        memberAuthTypeMongoRepo.updateExtJSONById(typeId, extJSON);
    }

}
