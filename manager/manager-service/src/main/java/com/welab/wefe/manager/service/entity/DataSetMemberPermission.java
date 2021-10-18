package com.welab.wefe.manager.service.entity;

import com.welab.wefe.common.data.mysql.entity.AbstractBlockChainEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * @Author Jervis
 * @Date 2020-05-26
 **/
@Entity
@Table(name = "u_data_set_member_permission")
public class DataSetMemberPermission extends AbstractBlockChainEntity {

    @Column(name = "data_set_id")
    private String dataSetId;
    @Column(name = "member_id")
    private String memberId;

    public DataSetMemberPermission() {
    }

    public DataSetMemberPermission(String dataSetId, String memberId) {
        this.dataSetId = dataSetId;
        this.memberId = memberId;
    }

    public String getDataSetId() {
        return dataSetId;
    }

    public void setDataSetId(String dataSetId) {
        this.dataSetId = dataSetId;
    }

    public String getMemberId() {
        return memberId;
    }

    public void setMemberId(String memberId) {
        this.memberId = memberId;
    }
}
