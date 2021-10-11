package com.welab.wefe.parser;

import com.alibaba.fastjson.JSONObject;
import com.welab.wefe.App;
import com.welab.wefe.common.data.mongodb.entity.contract.data.DataSetDefaultTag;
import com.welab.wefe.common.data.mongodb.repo.DataSetDefaultTagMongoRepo;
import com.welab.wefe.common.util.StringUtil;
import com.welab.wefe.constant.EventConstant;
import com.welab.wefe.exception.BusinessException;
import org.apache.commons.lang3.StringUtils;

/**
 * @author yuxin.zhang
 */
public class DataSetDefaultTagContractEventParser extends AbstractParser {
    protected DataSetDefaultTagMongoRepo dataSetDefaultTagMongoRepo = App.CONTEXT.getBean(DataSetDefaultTagMongoRepo.class);
    protected DataSetDefaultTag.ExtJSON extJSON;

    @Override
    protected void parseContractEvent() throws BusinessException {
        extJSON = StringUtils.isNotEmpty(extJsonStr) ? JSONObject.parseObject(extJsonStr, DataSetDefaultTag.ExtJSON.class) : new DataSetDefaultTag.ExtJSON();
        switch (eventBO.getEventName().toUpperCase()) {
            case EventConstant.DataSetDefaultTag.INSERT_EVENT:
                parseInsertEvent();
                break;
            case EventConstant.DataSetDefaultTag.UPDATE_EVENT:
                parseUpdateEvent();
                break;
            case EventConstant.DataSetDefaultTag.DELETE_BY_TAGID_EVENT:
                parseDeleteByTagIdEvent();
                break;
            default:
                throw new BusinessException("event name valid:" + eventBO.getEventName());
        }
    }

    private void parseInsertEvent() {
        DataSetDefaultTag dataSetDefaultTag = new DataSetDefaultTag();
        dataSetDefaultTag.setTagId(StringUtil.strTrim2(params.getString(0)));
        dataSetDefaultTag.setTagName(StringUtil.strTrim2(params.getString(1)));
        dataSetDefaultTag.setCreatedTime(StringUtil.strTrim2(params.getString(2)));
        dataSetDefaultTag.setUpdatedTime(StringUtil.strTrim2(params.getString(3)));
        dataSetDefaultTag.setExtJson(extJSON);

        dataSetDefaultTagMongoRepo.save(dataSetDefaultTag);

    }

    private void parseUpdateEvent() {
        String tagId = eventBO.getEntity().get("tag_id").toString();
        String tagName = eventBO.getEntity().get("tag_name").toString();
        dataSetDefaultTagMongoRepo.update(tagId,tagName);
    }
    private void parseDeleteByTagIdEvent() {
        String tagId = eventBO.getEntity().get("tag_id").toString();
        dataSetDefaultTagMongoRepo.deleteByTagId(tagId);
    }

}
