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
        unionNode.setBlockchainNodeId(StringUtil.strTrim2(params.getString(1)));
        unionNode.setBaseUrl(StringUtil.strTrim2(params.getString(2)));
        unionNode.setOrganizationName(StringUtil.strTrim2(params.getString(3)));
        unionNode.setLostContact(StringUtil.strTrim2(params.getString(4)));
        unionNode.setContactEmail(StringUtil.strTrim2(params.getString(5)));
        unionNode.setPriorityLevel(StringUtil.strTrim2(params.getString(6)));
        unionNode.setEnable(StringUtil.strTrim2(params.getString(7)));
        unionNode.setVersion(StringUtil.strTrim2(params.getString(8)));
        unionNode.setCreatedTime(StringUtil.strTrim2(params.getString(9)));
        unionNode.setUpdatedTime(StringUtil.strTrim2(params.getString(10)));
        unionNode.setExtJson(extJSON);

        unionNodeMongoRepo.save(unionNode);

    }

    private void parseUpdateEvent() {
        String nodeId = eventBO.getEntity().get("node_id").toString();
        String blockchainNodeId = StringUtil.strTrim2(params.getString(0));
        String baseUrl = StringUtil.strTrim2(params.getString(1));
        String organizationName = StringUtil.strTrim2(params.getString(2));
        String contactEmail = StringUtil.strTrim2(params.getString(3));
        String version = StringUtil.strTrim2(params.getString(4));
        String updatedTime = StringUtil.strTrim2(params.getString(5));
        unionNodeMongoRepo.update(nodeId,blockchainNodeId,baseUrl,organizationName,contactEmail,version,updatedTime);
    }

    private void parseUpdateEnableEvent() {
        String unionNodeId = eventBO.getEntity().get("node_id").toString();
        String enable = eventBO.getEntity().get("enable").toString();
        String updatedTime = eventBO.getEntity().get("updated_time").toString();
        unionNodeMongoRepo.updateEnable(unionNodeId, enable, updatedTime);
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
