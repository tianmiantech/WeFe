package com.welab.wefe.common.data.mongodb.entity.common;

import com.welab.wefe.common.data.mongodb.constant.MongodbTable;
import com.welab.wefe.common.data.mongodb.entity.base.AbstractNormalMongoModel;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;


@Document(collection = MongodbTable.Common.OPERATION_LOG)

public class OperationLog extends AbstractNormalMongoModel {

    /**
     * 请求接口名称
     */
    public String apiName;
    /**
     * api 调用者类型
     */
    public String callerType;
    /**
     * api 调用者 id
     */
    public String callerId;
    /**
     * api 调用者名称
     */
    public String callerName;
    /**
     * 请求来源IP
     */
    public String callerIp;
    /**
     * 请求参数
     */
    public String requestData;
    /**
     * 响应 code
     */
    public int responseCode;
    /**
     * 响应 message
     */
    public String responseMessage;
    /**
     * 响应内容
     */
    public String responseData;
    /**
     * 请求时间
     */
    public Date requestTime;
    /**
     * 响应时间
     */
    public Date responseTime;
    /**
     * 耗时
     */
    public long spend;

    // region getter/setter

    public String getApiName() {
        return apiName;
    }

    public void setApiName(String apiName) {
        this.apiName = apiName;
    }

    public String getCallerType() {
        return callerType;
    }

    public void setCallerType(String callerType) {
        this.callerType = callerType;
    }

    public String getCallerId() {
        return callerId;
    }

    public void setCallerId(String callerId) {
        this.callerId = callerId;
    }

    public String getCallerName() {
        return callerName;
    }

    public void setCallerName(String callerName) {
        this.callerName = callerName;
    }

    public String getCallerIp() {
        return callerIp;
    }

    public void setCallerIp(String callerIp) {
        this.callerIp = callerIp;
    }

    public String getRequestData() {
        return requestData;
    }

    public void setRequestData(String requestData) {
        this.requestData = requestData;
    }

    public int getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(int responseCode) {
        this.responseCode = responseCode;
    }

    public String getResponseMessage() {
        return responseMessage;
    }

    public void setResponseMessage(String responseMessage) {
        this.responseMessage = responseMessage;
    }

    public String getResponseData() {
        return responseData;
    }

    public void setResponseData(String responseData) {
        this.responseData = responseData;
    }

    public Date getRequestTime() {
        return requestTime;
    }

    public void setRequestTime(Date requestTime) {
        this.requestTime = requestTime;
    }

    public Date getResponseTime() {
        return responseTime;
    }

    public void setResponseTime(Date responseTime) {
        this.responseTime = responseTime;
    }

    public long getSpend() {
        return spend;
    }

    public void setSpend(long spend) {
        this.spend = spend;
    }

    // endregion
}
