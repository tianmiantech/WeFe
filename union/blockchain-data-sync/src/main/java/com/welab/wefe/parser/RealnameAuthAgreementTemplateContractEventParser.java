package com.welab.wefe.parser;

import com.alibaba.fastjson.JSONObject;
import com.welab.wefe.App;
import com.welab.wefe.common.data.mongodb.entity.union.RealnameAuthAgreementTemplate;
import com.welab.wefe.common.data.mongodb.entity.union.ext.RealnameAuthAgreementTemplateExtJSON;
import com.welab.wefe.common.data.mongodb.repo.RealnameAuthAgreementTemplateMongoRepo;
import com.welab.wefe.common.util.StringUtil;
import com.welab.wefe.constant.EventConstant;
import com.welab.wefe.exception.BusinessException;
import org.apache.commons.lang3.StringUtils;

/**
 * @author yuxin.zhang
 */
public class RealnameAuthAgreementTemplateContractEventParser extends AbstractParser {
    protected RealnameAuthAgreementTemplateMongoRepo mongoRepo = App.CONTEXT.getBean(RealnameAuthAgreementTemplateMongoRepo.class);
    protected RealnameAuthAgreementTemplateExtJSON extJSON;

    @Override
    protected void parseContractEvent() throws BusinessException {
        extJSON = StringUtils.isNotEmpty(extJsonStr) ? JSONObject.parseObject(extJsonStr, RealnameAuthAgreementTemplateExtJSON.class) : new RealnameAuthAgreementTemplateExtJSON();
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
        RealnameAuthAgreementTemplate realnameAuthAgreementTemplate = new RealnameAuthAgreementTemplate();
        realnameAuthAgreementTemplate.setTemplateFileId(StringUtil.strTrim2(params.getString(0)));
        realnameAuthAgreementTemplate.setTemplateFileSign(StringUtil.strTrim2(params.getString(1)));
        realnameAuthAgreementTemplate.setFileName(StringUtil.strTrim2(params.getString(2)));
        realnameAuthAgreementTemplate.setEnable(StringUtil.strTrim2(params.getString(3)));
        realnameAuthAgreementTemplate.setCreatedTime(StringUtil.strTrim2(params.getString(4)));
        realnameAuthAgreementTemplate.setUpdatedTime(StringUtil.strTrim2(params.getString(5)));
        realnameAuthAgreementTemplate.setExtJson(extJSON);

        mongoRepo.save(realnameAuthAgreementTemplate);

    }


    private void parseUpdateEnableEvent() {
        String templateFileId = eventBO.getEntity().get("template_file_id").toString();
        String enable = eventBO.getEntity().get("enable").toString();
        String updatedTime = eventBO.getEntity().get("updated_time").toString();
        mongoRepo.updateEnable(templateFileId, enable, updatedTime);
    }

    private void parseUpdateExtJson() {
        String templateFileId = eventBO.getEntity().get("template_file_id").toString();
        String updatedTime = eventBO.getEntity().get("updated_time").toString();
        mongoRepo.updateExtJSONById(templateFileId, extJSON,updatedTime);
    }

}
