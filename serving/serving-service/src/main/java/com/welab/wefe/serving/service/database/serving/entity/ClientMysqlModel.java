package com.welab.wefe.serving.service.database.serving.entity;

import javax.persistence.Column;
import javax.persistence.Entity;

/**
 * client entity
 *
 * @author ivenn.zheng
 */
@Entity(name = "client")
public class ClientMysqlModel extends AbstractBaseMySqlModel{


    /**
     * name
     */
    private String name;

    /**
     * email
     */
    private String email;

    /**
     * ip_add
     */
    @Column(name = "ip_add")
    private String ipAdd;

    /**
     * ip_add
     */
    @Column(name = "pub_key")
    private String pubKey;

    /**
     * remark
     */
    private String remark;

    /**
     * status, 1 normal、 0 deleted
     */
    private Integer status;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getIpAdd() {
        return ipAdd;
    }

    public void setIpAdd(String ipAdd) {
        this.ipAdd = ipAdd;
    }

    public String getPubKey() {
        return pubKey;
    }

    public void setPubKey(String pubKey) {
        this.pubKey = pubKey;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}
