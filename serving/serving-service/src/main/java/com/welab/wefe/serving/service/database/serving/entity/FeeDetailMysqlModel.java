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

    @Column(name = "fee_config_id")
    private String feeConfigId;

    @Column(name = "api_call_record_id")
    private String apiCallRecordId;

    /**
     * total fee
     */
    @Column(name = "total_fee")
    private BigDecimal totalFee;


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

    public String getFeeConfigId() {
        return feeConfigId;
    }

    public void setFeeConfigId(String feeConfigId) {
        this.feeConfigId = feeConfigId;
    }

    public String getApiCallRecordId() {
        return apiCallRecordId;
    }

    public void setApiCallRecordId(String apiCallRecordId) {
        this.apiCallRecordId = apiCallRecordId;
    }

    public BigDecimal getTotalFee() {
        return totalFee;
    }

    public void setTotalFee(BigDecimal totalFee) {
        this.totalFee = totalFee;
    }
}
