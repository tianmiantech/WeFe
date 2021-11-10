package com.welab.wefe.common.data.mongodb.entity.sms;

import com.welab.wefe.common.data.mongodb.constant.MongodbTable;
import com.welab.wefe.common.data.mongodb.constant.SmsBusinessTypeEnum;
import com.welab.wefe.common.data.mongodb.constant.SmsSupplierEnum;
import com.welab.wefe.common.data.mongodb.entity.AbstractNormalMongoModel;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * @author aaron.li
 * @date 2021/10/22 11:20
 */
@Document(collection = MongodbTable.Sms.DETAIL_INFO)
public class SmsDetailInfo extends AbstractNormalMongoModel {
    private String bizId;
    private String reqId;
    private String mobile;
    private String reqContent;
    private SmsSupplierEnum supplier;
    private boolean success;
    private String respContent;
    private SmsBusinessTypeEnum businessType;

    public String getBizId() {
        return bizId;
    }

    public void setBizId(String bizId) {
        this.bizId = bizId;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getReqContent() {
        return reqContent;
    }

    public void setReqContent(String reqContent) {
        this.reqContent = reqContent;
    }

    public String getReqId() {
        return reqId;
    }

    public void setReqId(String reqId) {
        this.reqId = reqId;
    }

    public SmsSupplierEnum getSupplier() {
        return supplier;
    }

    public void setSupplier(SmsSupplierEnum supplier) {
        this.supplier = supplier;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getRespContent() {
        return respContent;
    }

    public void setRespContent(String respContent) {
        this.respContent = respContent;
    }

    public SmsBusinessTypeEnum getBusinessType() {
        return businessType;
    }

    public void setBusinessType(SmsBusinessTypeEnum businessType) {
        this.businessType = businessType;
    }
}
