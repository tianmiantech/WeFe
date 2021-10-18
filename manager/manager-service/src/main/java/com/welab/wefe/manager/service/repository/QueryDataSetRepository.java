package com.welab.wefe.manager.service.repository;

import com.welab.wefe.common.data.mysql.repository.MyJpaRepository;
import com.welab.wefe.manager.service.entity.QueryDataSet;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * @Author Jervis
 * @Date 2020-05-22
 **/
public interface QueryDataSetRepository extends MyJpaRepository<QueryDataSet, String> {
    @Query(value = "select ds.*, mem.NAME AS member_name from u_data_set ds,u_member mem where ds.member_id = mem.id " +
            "and ds.status = 0 " +
            "and if(:name != '', ds.name like concat('%',:name,'%'), 1 = 1) " +
            "and if(:tag != '', ds.tags like concat('%',:tag,'%'), 1 = 1) " +
            "and if(:member_name != '', mem.name like concat('%',:member_name,'%'), 1 = 1)" +
            "and if(:member_id != '', ds.member_id = :member_id, 1 = 1) " +
            "and if(:id != '', ds.id = :id, 1 = 1) " +
            "and if(:contains_y != '', ds.contains_y = :contains_y, 1 = 1) "
            , nativeQuery = true, countProjection = "1")
    Page<QueryDataSet> findAll(Pageable pageable,
                               @Param("name") String name,
                               @Param("member_name") String memberName,
                               @Param("contains_y") String containsY,
                               @Param("tag") String tag,
                               @Param("id") String id,
                               @Param("member_id") String memberId);

    @Query(value = "select ds.*, mem.NAME AS member_name from u_data_set ds,u_member mem where ds.member_id = mem.id " +
            "and ds.status = 0 " +
            "and mem.allow_open_data_set= 1 " +
            "and if(:name != '', ds.name like concat('%',:name,'%'), 1 = 1) " +
            "and if(:tag != '', ds.tags like concat('%',:tag,'%'), 1 = 1) " +
            "and if(:member_name != '', mem.name like concat('%',:member_name,'%'), 1 = 1)" +
            "and if(:member_id != '', ds.member_id = :member_id, 1 = 1) " +
            "and if(:id != '', ds.id = :id, 1 = 1) " +
            "and if(:contains_y != '', ds.contains_y = :contains_y, 1 = 1) " +
            "and (ds.public_level = 'Public' or ds.id in (select data_set_id from u_data_set_member_permission where member_id=:cur_member_id))"
            , nativeQuery = true, countProjection = "1")
    Page<QueryDataSet> find(Pageable pageable,
                            @Param("name") String name,
                            @Param("member_name") String memberName,
                            @Param("contains_y") String containsY,
                            @Param("tag") String tag,
                            @Param("cur_member_id") String curMemberId,
                            @Param("id") String id,
                            @Param("member_id") String memberId);
}
