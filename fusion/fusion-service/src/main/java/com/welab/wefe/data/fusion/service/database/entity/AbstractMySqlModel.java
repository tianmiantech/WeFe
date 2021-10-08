package com.welab.wefe.data.fusion.service.database.entity;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

/**
 * mysql The abstract parent class of a database entity
 *
 * @author Zane
 * @date 2020/5/13
 */
@MappedSuperclass
public abstract class AbstractMySqlModel implements Serializable {

    /**
     * Globally unique identifier
     */
    @Id
    @Column(name = "id", updatable = false)
    private String id = UUID.randomUUID().toString().replaceAll("-", "");
    /**
     * Creation time
     */
    private Date createdTime = new Date();
    /**
     * Update time
     */
    private Date updatedTime;

    //region getter/setter

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Date getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Date createdTime) {
        this.createdTime = createdTime;
    }

    public Date getUpdatedTime() {
        return updatedTime;
    }

    public void setUpdatedTime(Date updatedTime) {
        this.updatedTime = updatedTime;
    }


    //endregion
}
