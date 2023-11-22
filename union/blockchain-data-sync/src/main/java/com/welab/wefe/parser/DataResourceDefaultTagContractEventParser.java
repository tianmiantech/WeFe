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
import com.welab.wefe.common.data.mongodb.entity.union.DataResource;
import com.welab.wefe.common.data.mongodb.entity.union.DataResourceDefaultTag;
import com.welab.wefe.common.data.mongodb.entity.union.DataSetDefaultTag;
import com.welab.wefe.common.data.mongodb.entity.union.ext.DataResourceDefaultTagExtJSON;
import com.welab.wefe.common.data.mongodb.entity.union.ext.DataSetDefaultTagExtJSON;
import com.welab.wefe.common.data.mongodb.repo.DataResourceDefaultTagMongoRepo;
import com.welab.wefe.common.data.mongodb.repo.DataSetDefaultTagMongoRepo;
import com.welab.wefe.common.util.StringUtil;
import com.welab.wefe.common.wefe.enums.DataResourceType;
import com.welab.wefe.constant.EventConstant;
import com.welab.wefe.exception.BusinessException;
import org.apache.commons.lang3.StringUtils;

/**
 * @author yuxin.zhang
 */
public class DataResourceDefaultTagContractEventParser extends AbstractParser {
    protected DataResourceDefaultTagMongoRepo dataSetDefaultTagMongoRepo = BlockchainDataSyncApp.CONTEXT.getBean(DataResourceDefaultTagMongoRepo.class);
    protected DataResourceDefaultTagExtJSON extJSON;

    @Override
    protected void parseContractEvent() throws BusinessException {
        extJSON = StringUtils.isNotEmpty(extJsonStr) ? JSONObject.parseObject(extJsonStr, DataResourceDefaultTagExtJSON.class) : new DataResourceDefaultTagExtJSON();
        switch (eventBO.getEventName().toUpperCase()) {
            case EventConstant.DataResourceDefaultTagEvent.INSERT_EVENT:
                parseInsertEvent();
                break;
            case EventConstant.DataResourceDefaultTagEvent.UPDATE_EVENT:
                parseUpdateEvent();
                break;
            case EventConstant.DataResourceDefaultTagEvent.DELETE_BY_TAGID_EVENT:
                parseDeleteByTagIdEvent();
                break;
            case EventConstant.UPDATE_EXTJSON_EVENT:
                parseUpdateExtJson();
                break;
            default:
                throw new BusinessException("event name valid:" + eventBO.getEventName());
        }
    }

    private void parseInsertEvent() {
        DataResourceDefaultTag dataSetDefaultTag = new DataResourceDefaultTag();
        dataSetDefaultTag.setTagId(StringUtil.strTrim2(params.getString(0)));
        dataSetDefaultTag.setTagName(StringUtil.strTrim2(params.getString(1)));
        dataSetDefaultTag.setDataResourceType(DataResourceType.valueOf(StringUtil.strTrim2(params.getString(2))));
        dataSetDefaultTag.setCreatedTime(StringUtil.strTrim2(params.getString(3)));
        dataSetDefaultTag.setUpdatedTime(StringUtil.strTrim2(params.getString(4)));
        dataSetDefaultTag.setExtJson(extJSON);

        dataSetDefaultTagMongoRepo.save(dataSetDefaultTag);

    }

    private void parseUpdateEvent() {
        String tagId = eventBO.getEntity().get("tag_id").toString();
        String tagName = eventBO.getEntity().get("tag_name").toString();
        String updatedTime = eventBO.getEntity().get("updated_time").toString();
        dataSetDefaultTagMongoRepo.update(tagId, tagName, extJSON, updatedTime);
    }

    private void parseDeleteByTagIdEvent() {
        String tagId = eventBO.getEntity().get("tag_id").toString();
        dataSetDefaultTagMongoRepo.deleteByTagId(tagId);
    }

    private void parseUpdateExtJson() {
        String tagId = eventBO.getEntity().get("tag_id").toString();
        dataSetDefaultTagMongoRepo.updateExtJSONById(tagId, extJSON);
    }

}
