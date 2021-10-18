package com.welab.wefe.manager.service.repository;

import com.welab.wefe.common.data.mysql.repository.MyJpaRepository;
import com.welab.wefe.manager.service.entity.DataSet;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * @Author Jervis
 * @Date 2020-05-22
 **/
public interface DataSetRepository extends MyJpaRepository<DataSet, String> {

    @Query(value = "select * from u_data_set where if(:name != '', name like concat('%',:name,'%'), 1 = 1) and if(:containsY != '', contains_y = :containsY, 1 = 1) and if(:tag != '', tags like concat('%',:tag,'%'), 1 = 1)"
            , nativeQuery = true, countProjection = "1")
    Page<DataSet> findAll(Pageable pageable, String name, Boolean containsY, String tag);

    @Query(value = "select * from u_data_set ds left JOIN u_member as m ON ds.member_id = m.id " +
            "where m.allow_open_data_set = 1 " +
            "and (ds.public_level = 'Public' or ds.id in (select data_set_id from u_data_set_member_permission as p where p.member_id = :currentMemberId)) " +
            "and if(:queryMemberId != '', ds.member_id = :queryMemberId, 1 = 1)" +
            "and if(:name != '', ds.name like concat('%', :name, '%'), 1 = 1)" +
            "and if(:containsY != '', ds.contains_y = :containsY, 1 = 1)" +
            "and if(:tag != '', ds.tags like concat('%', :tag, '%'), 1 = 1)", nativeQuery = true, countProjection = "1")
    Page<DataSet> findAllWithPermission(Pageable pageable, @Param("currentMemberId") String currentMemberId, @Param("queryMemberId") String queryMemberId
            , @Param("name") String name, @Param("containsY") String containsY, @Param("tag") String tag);
//
//    @Query(value = "select * from u_data_set ds left JOIN u_member as m ON ds.member_id = m.id " +
//            "where m.allow_open_data_set = TRUE " +
//            "and (ds.open_for_all = TRUE or ds.id in (select data_set_id from u_data_set_member_permission as p where p.member_id = :currentMemberId)) " +
//            "and ds.id = :dataSetId", nativeQuery = true, countProjection = "1")
//    Optional<DataSet> findByIdWithPermission(@Param("currentMemberId") String currentMemberId, @Param("dataSetId") String dataSetId);
}
