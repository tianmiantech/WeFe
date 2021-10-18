package com.welab.wefe.manager.service.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * 联邦成员同步的数据集
 *
 * @Date 2020-05-22
 **/
@Entity(name = "data_set_for_tag")
public class DataSetForTag {

    @Id
    private String tags;
    @Column(name = "use_count")
    private Long useCount;
    public DataSetForTag(){};
    public DataSetForTag(String tags,Long useCount){
        this.tags = tags;
        this.useCount = useCount;
    }

    public Long getUseCount() {
        return useCount;
    }

    public void setUseCount(Long useCount) {
        this.useCount = useCount;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }
}
