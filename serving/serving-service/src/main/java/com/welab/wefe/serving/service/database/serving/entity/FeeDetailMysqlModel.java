package com.welab.wefe.serving.service.database.serving.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import java.math.BigDecimal;

@Entity(name = "fee_detail")
public class FeeDetailMysqlModel extends AbstractBaseMySqlModel {

    @Column(name = "service_id")
    private String serviceId;

    @Column(name = "client_id")
    private String clientId;

    /**
     * total fee
     */
    @Column(name = "total_fee")
    private BigDecimal totalFee;

    @Column(name = "total_request_times")
    private Long totalRequestTimes;


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

    public BigDecimal getTotalFee() {
        return totalFee;
    }

    public void setTotalFee(BigDecimal totalFee) {
        this.totalFee = totalFee;
    }

    public Long getTotalRequestTimes() {
        return totalRequestTimes;
    }

    public void setTotalRequestTimes(Long totalRequestTimes) {
        this.totalRequestTimes = totalRequestTimes;
    }
}
