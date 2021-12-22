package com.welab.wefe.serving.service.database.serving.entity;

import com.welab.wefe.serving.service.enums.ServiceStatusEnum;

import javax.persistence.Column;
import javax.persistence.Entity;

@Entity(name = "client_service")
public class ClientServiceMysqlModel extends AbstractBaseMySqlModel {

    @Column(name = "service_id")
    private String serviceId;

    @Column(name = "client_id")
    private String clientId;

    @Column(name = "fee_config_id")
    private String feeConfigId;

    /**
     * status: false means unused, true means used, default = true
     */
    private Integer status = ServiceStatusEnum.USED.getValue();

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

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getFeeConfigId() {
        return feeConfigId;
    }

    public void setFeeConfigId(String feeConfigId) {
        this.feeConfigId = feeConfigId;
    }
}
