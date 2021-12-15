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

package com.welab.wefe.parser;

import com.alibaba.fastjson.JSONObject;
import com.welab.wefe.App;
import com.welab.wefe.common.data.mongodb.entity.union.ImageDataSet;
import com.welab.wefe.common.data.mongodb.entity.union.ext.ImageDataSetExtJSON;
import com.welab.wefe.common.data.mongodb.repo.ImageDataSetMongoReop;
import com.welab.wefe.common.util.StringUtil;
import com.welab.wefe.constant.EventConstant;
import com.welab.wefe.exception.BusinessException;
import org.apache.commons.lang3.StringUtils;

/**
 * ImageDataSetContract Event information interpreter
 *
 * @author yuxin.zhang
 */
public class ImageDataSetContractEventParser extends AbstractParser {
    protected ImageDataSetMongoReop imageDataSetMongoReop = App.CONTEXT.getBean(ImageDataSetMongoReop.class);
    protected ImageDataSetExtJSON extJSON;


    @Override
    protected void parseContractEvent() throws BusinessException {
        extJSON = StringUtils.isNotEmpty(extJsonStr) ? JSONObject.parseObject(extJsonStr, ImageDataSetExtJSON.class) : new ImageDataSetExtJSON();
        switch (eventBO.getEventName().toUpperCase()) {
            case EventConstant.ImageDataSetEvent.INSERT_EVENT:
                parseInsertEvent();
                break;
            case EventConstant.ImageDataSetEvent.UPDATE_EVENT:
                parseUpdateEvent();
                break;
            case EventConstant.UPDATE_EXTJSON_EVENT:
                parseUpdateExtJson();
                break;
            default:
                throw new BusinessException("event name valid:" + eventBO.getEventName());
        }
    }

    private void parseInsertEvent() {
        ImageDataSet imageDataSet = new ImageDataSet();
        imageDataSet.setDataResourceId(StringUtil.strTrim2(params.getString(0)));
        imageDataSet.setForJobType(params.getString(1));
        imageDataSet.setLabelList(StringUtil.strTrim2(params.getString(2)));
        imageDataSet.setLabeledCount(StringUtil.strTrim2(params.getString(3)));
        imageDataSet.setLabelCompleted(StringUtil.strTrim2(params.getString(4)));
        imageDataSet.setFileSize(StringUtil.strTrim2(params.getString(5)));
        imageDataSet.setCreatedTime(StringUtil.strTrim2(params.getString(6)));
        imageDataSet.setUpdatedTime(StringUtil.strTrim2(params.getString(7)));
        imageDataSet.setExtJson(extJSON);
        imageDataSetMongoReop.upsert(imageDataSet);
    }

    private void parseUpdateEvent() throws BusinessException {
        String dataResourceId = eventBO.getEntity().get("data_resource_id").toString();
        String updatedTime = eventBO.getEntity().get("updated_time").toString();

        ImageDataSet imageDataSet = getImageDataSet(dataResourceId);

        imageDataSet.setForJobType(params.getString(0));
        imageDataSet.setLabelList(StringUtil.strTrim2(params.getString(1)));
        imageDataSet.setLabeledCount(StringUtil.strTrim2(params.getString(2)));
        imageDataSet.setLabelCompleted(StringUtil.strTrim2(params.getString(3)));
        imageDataSet.setFileSize(StringUtil.strTrim2(params.getString(4)));
        imageDataSet.setUpdatedTime(updatedTime);
        imageDataSetMongoReop.upsert(imageDataSet);
    }


    private void parseUpdateExtJson() throws BusinessException {
        String dataResourceId = eventBO.getEntity().get("data_resource_id").toString();
        String updatedTime = eventBO.getEntity().get("updated_time").toString();
        ImageDataSet imageDataSet = getImageDataSet(dataResourceId);
        imageDataSet.setExtJson(extJSON);
        imageDataSet.setUpdatedTime(updatedTime);
        imageDataSetMongoReop.upsert(imageDataSet);
    }

    private ImageDataSet getImageDataSet(String dataResourceId) throws BusinessException {
        ImageDataSet imageDataSet = imageDataSetMongoReop.findDataResourceId(dataResourceId);
        if(imageDataSet == null) {
            throw new BusinessException("Data does not exist dataResourceId:" + dataResourceId);
        }
        return imageDataSet;
    }

}
