package com.welab.wefe.manager.service.entity;

import com.welab.wefe.common.data.mysql.entity.AbstractUniqueIDEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "data_set_default_tag")
public class DataSetDefaultTag extends AbstractUniqueIDEntity {

    @Column(name = "tag_name")
    private String tagName;

    public String getTagName() {
        return tagName;
    }

    public void setTagName(String tagName) {
        this.tagName = tagName;
    }
}
