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
            case EventConstant.MemberAuthTypeEvent.INSERT_EVENT:
                parseInsertEvent();
                break;
            case EventConstant.MemberAuthTypeEvent.UPDATE_EVENT:
                parseUpdateEvent();
                break;
            case EventConstant.MemberAuthTypeEvent.DELETE_BY_TYPEID_EVENT:
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
