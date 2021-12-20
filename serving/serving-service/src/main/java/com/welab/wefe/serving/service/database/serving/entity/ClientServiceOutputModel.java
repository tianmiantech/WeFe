package com.welab.wefe.serving.service.database.serving.entity;

import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.serving.sdk.model.BaseModel;

import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * @author ivenn.zheng
 */
@Entity
public class ClientServiceOutputModel {

    @Id
    private String id;

    /**
     * 服务名称
     */
    private String serviceName;

    /**
     * 客户名称
     */
    private String clientName;

    /**
     * 启用状态
     */
    private Integer status;

    /**
     * 服务类型
     */
    private Integer serviceType;

    /**
     * ip 地址
     */
    private String ipAdd;

    /**
     * 请求地址
     */
    private String url;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getServiceType() {
        return serviceType;
    }

    public void setServiceType(Integer serviceType) {
        this.serviceType = serviceType;
    }

    public String getIpAdd() {
        return ipAdd;
    }

    public void setIpAdd(String ipAdd) {
        this.ipAdd = ipAdd;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
