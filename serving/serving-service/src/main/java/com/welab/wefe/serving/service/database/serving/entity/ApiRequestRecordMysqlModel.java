package com.welab.wefe.serving.service.database.serving.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import java.math.BigDecimal;

/**
 * @author ivenn.zheng
 */
@Entity(name = "api_request_record")
public class ApiRequestRecordMysqlModel extends AbstractBaseMySqlModel {

    @Column(name = "service_id")
    private String serviceId;

    @Column(name = "client_id")
    private String clientId;

    /**
     * 请求地址
     */
    @Column(name = "ip_add")
    private String ipAdd;

    /**
     * 耗时
     */
    private Long spend;

    /**
     * 请求结果：1 成功、0 失败
     */
    @Column(name = "request_result")
    private Integer requestResult;

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getIpAdd() {
        return ipAdd;
    }

    public void setIpAdd(String ipAdd) {
        this.ipAdd = ipAdd;
    }

    public Long getSpend() {
        return spend;
    }

    public void setSpend(Long spend) {
        this.spend = spend;
    }

    public Integer getRequestResult() {
        return requestResult;
    }

    public void setRequestResult(Integer requestResult) {
        this.requestResult = requestResult;
    }
}
