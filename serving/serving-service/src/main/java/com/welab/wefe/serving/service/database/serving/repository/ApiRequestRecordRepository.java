package com.welab.wefe.serving.service.database.serving.repository;

import com.welab.wefe.serving.service.database.serving.entity.ApiRequestRecordMysqlModel;
import com.welab.wefe.serving.service.database.serving.repository.base.BaseRepository;
import org.springframework.stereotype.Repository;

/**
 * @author ivenn.zheng
 */
@Repository
public interface ApiRequestRecordRepository extends BaseRepository<ApiRequestRecordMysqlModel, String> {
}
