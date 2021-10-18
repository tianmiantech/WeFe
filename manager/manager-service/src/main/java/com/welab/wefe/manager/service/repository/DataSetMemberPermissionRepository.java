package com.welab.wefe.manager.service.repository;

import com.welab.wefe.common.data.mysql.repository.MyJpaRepository;
import com.welab.wefe.manager.service.entity.DataSetMemberPermission;

/**
 * @Author Jervis
 * @Date 2020-05-29
 **/
public interface DataSetMemberPermissionRepository extends MyJpaRepository<DataSetMemberPermission, String> {

    void deleteByDataSetId(String dataSetId);


}
