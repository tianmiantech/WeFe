package com.welab.wefe.serving.service.database.serving.entity;

import com.welab.wefe.serving.service.enums.PayTypeEnum;

import javax.persistence.Column;
import javax.persistence.Entity;
import java.math.BigDecimal;

@Entity(name = "fee_config")
public class FeeConfigMysqlModel extends AbstractBaseMySqlModel {


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
