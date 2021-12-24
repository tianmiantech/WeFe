package com.welab.wefe.serving.service.database.serving.entity;

import com.welab.wefe.serving.service.enums.PayTypeEnum;

import javax.persistence.Column;
import javax.persistence.Entity;
import java.math.BigDecimal;

@Entity(name = "fee_config")
public class FeeConfigMysqlModel extends AbstractBaseMySqlModel {


    @Column(name = "service_id")
    private String serviceId;

    @Column(name = "client_id")
    private String clientId;

    /**
     * unit price
     */
    @Column(name = "unit_price")
    private Double unitPrice;

    /**
     * pay type: 1 预付费、0 后付费
     */
    @Column(name = "pay_type")
    private int payType = PayTypeEnum.POSTPAID.getValue();

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

    public Double getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(Double unitPrice) {
        this.unitPrice = unitPrice;
    }

    public int getPayType() {
        return payType;
    }

    public void setPayType(int payType) {
        this.payType = payType;
    }
}
