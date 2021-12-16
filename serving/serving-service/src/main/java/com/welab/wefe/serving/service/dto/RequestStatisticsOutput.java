package com.welab.wefe.serving.service.dto;

import com.welab.wefe.common.util.DateUtil;

import java.util.Date;

/**
 * @author ivenn.zheng
 */
public class RequestStatisticsOutput {

    /**
     * 总调用次数
     */
    private Integer totalRequestTimes;

    /**
     * 总失败次数
     */
    private Integer totalFailTimes;

    /**
     * 总成功次数
     */
    private Integer totalSuccessTimes;

    /**
     * 服务名称
     */
    private String serviceName;

    /**
     * 客户名称
     */
    private String clientName;

    /**
     * 服务类型
     */
    private Integer serviceType;

    /**
     * 单价
     */
    private Double unitPrice;


    public Integer getTotalRequestTimes() {
        return totalRequestTimes;
    }

    public void setTotalRequestTimes(Integer totalRequestTimes) {
        this.totalRequestTimes = totalRequestTimes;
    }

    public Integer getTotalFailTimes() {
        return totalFailTimes;
    }

    public void setTotalFailTimes(Integer totalFailTimes) {
        this.totalFailTimes = totalFailTimes;
    }

    public Integer getTotalSuccessTimes() {
        return totalSuccessTimes;
    }

    public void setTotalSuccessTimes(Integer totalSuccessTimes) {
        this.totalSuccessTimes = totalSuccessTimes;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public Integer getServiceType() {
        return serviceType;
    }

    public void setServiceType(Integer serviceType) {
        this.serviceType = serviceType;
    }

    public Double getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(Double unitPrice) {
        this.unitPrice = unitPrice;
    }
}
