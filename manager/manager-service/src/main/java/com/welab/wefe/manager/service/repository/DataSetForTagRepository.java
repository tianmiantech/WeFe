package com.welab.wefe.manager.service.repository;

import com.welab.wefe.common.data.mysql.repository.MyJpaRepository;
import com.welab.wefe.manager.service.entity.DataSetForTag;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * @Author Jervis
 * @Date 2020-05-22
 **/
public interface DataSetForTagRepository extends MyJpaRepository<DataSetForTag, String> {

    @Query(value = "select tags,count(tags) as use_count from u_data_set where 1=1 " +
            "and if(:tagName != '', tags like concat('%',:tagName,'%'), 1 = 1) " +
            "group by tags order by use_count DESC "
            , nativeQuery = true, countProjection = "1")
    List<DataSetForTag> findByTags(@Param("tagName") String tagName);

}
