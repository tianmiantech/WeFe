package com.welab.wefe.parser;

import com.alibaba.fastjson.JSONObject;
import com.welab.wefe.App;
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
    protected UnionNodeMongoRepo unionNodeMongoRepo = App.CONTEXT.getBean(UnionNodeMongoRepo.class);
    protected UnionNodeExtJSON extJSON;

    @Override
    protected void parseContractEvent() throws BusinessException {
        extJSON = StringUtils.isNotEmpty(extJsonStr) ? JSONObject.parseObject(extJsonStr, UnionNodeExtJSON.class) : new UnionNodeExtJSON();
        switch (eventBO.getEventName().toUpperCase()) {
            case EventConstant.UnionNode.INSERT_EVENT:
                parseInsertEvent();
                break;
            case EventConstant.UnionNode.UPDATE_EVENT:
                parseUpdateEvent();
                break;
            case EventConstant.UnionNode.UPDATE_ENABLE_EVENT:
                parseUpdateEnableEvent();
                break;
            case EventConstant.UnionNode.DELETE_BY_UNIONNODEID_EVENT:
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
        unionNode.setBaseUrl(StringUtil.strTrim2(params.getString(1)));
        unionNode.setOrganizationName(StringUtil.strTrim2(params.getString(2)));
        unionNode.setEnable(StringUtil.strTrim2(params.getString(3)));
        unionNode.setCreatedTime(StringUtil.strTrim2(params.getString(4)));
        unionNode.setUpdatedTime(StringUtil.strTrim2(params.getString(5)));
        unionNode.setExtJson(extJSON);

        unionNodeMongoRepo.save(unionNode);

    }

    private void parseUpdateEvent() {
        String unionNodeId = eventBO.getEntity().get("union_node_id").toString();
        String unionBaseUrl = StringUtil.strTrim2(params.getString(0));
        String organizationName = StringUtil.strTrim2(params.getString(1));
        String updatedTime = StringUtil.strTrim2(params.getString(2));
        unionNodeMongoRepo.update(unionNodeId, unionBaseUrl, organizationName, updatedTime);
    }

    private void parseUpdateEnableEvent() {
        String unionNodeId = eventBO.getEntity().get("union_node_id").toString();
        String enable = eventBO.getEntity().get("enable").toString();
        String updatedTime = eventBO.getEntity().get("updated_time").toString();
        unionNodeMongoRepo.updateEnable(unionNodeId, enable, updatedTime);
    }

    private void parseDeleteByUnionNodeIdEvent() {
        String unionNodeId = eventBO.getEntity().get("union_node_id").toString();
        unionNodeMongoRepo.deleteByUnionNodeId(unionNodeId);
    }

    private void parseUpdateExtJson() {
        String unionNodeId = eventBO.getEntity().get("unionNodeId").toString();
        unionNodeMongoRepo.updateExtJSONById(unionNodeId, extJSON);
    }

}
