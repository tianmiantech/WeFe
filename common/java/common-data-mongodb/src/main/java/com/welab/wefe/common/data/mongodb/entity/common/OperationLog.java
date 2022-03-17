package com.welab.wefe.common.data.mongodb.entity.common;

import com.welab.wefe.common.data.mongodb.constant.MongodbTable;
import com.welab.wefe.common.data.mongodb.entity.base.AbstractNormalMongoModel;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Document(collection = MongodbTable.Common.OPERATION_LOG)

public class OperationLog extends AbstractNormalMongoModel {
    /**
     * 请求接口
     */
    private String logInterface;

    /**
     * 请求接口名称
     */
    private String interfaceName;

    /**
     * 请求IP
     */
    private String requestIp;

    /**
     * 操作人员编号
     */
    private String operatorId;

    /**
     * 请求token
     */
    private String token;

    /**
     * 操作行为
     */
    private String logAction;

    /**
     * 请求结果编码
     */
    private int resultCode;

    /**
     * 请求结果
     */
    private String resultMessage;

    /**
     * 请求时间
     */
    private Date requestTime;

    /**
     * 耗时
     */
    private long spend;

    public String getLogInterface() {
        return logInterface;
    }

    public void setLogInterface(String logInterface) {
        this.logInterface = logInterface;
    }

    public String getInterfaceName() {
        return interfaceName;
    }

    public void setInterfaceName(String interfaceName) {
        this.interfaceName = interfaceName;
    }

    public String getRequestIp() {
        return requestIp;
    }

    public void setRequestIp(String requestIp) {
        this.requestIp = requestIp;
    }

    public String getOperatorId() {
        return operatorId;
    }

    public void setOperatorId(String operatorId) {
        this.operatorId = operatorId;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getLogAction() {
        return logAction;
    }

    public void setLogAction(String logAction) {
        this.logAction = logAction;
    }

    public int getResultCode() {
        return resultCode;
    }

    public void setResultCode(int resultCode) {
        this.resultCode = resultCode;
    }

    public String getResultMessage() {
        return resultMessage;
    }

    public void setResultMessage(String resultMessage) {
        this.resultMessage = resultMessage;
    }

    public Date getRequestTime() {
        return requestTime;
    }

    public void setRequestTime(Date requestTime) {
        this.requestTime = requestTime;
    }

    public long getSpend() {
        return spend;
    }

    public void setSpend(long spend) {
        this.spend = spend;
    }
}
