package com.welab.wefe.manager.service.repository;

import com.welab.wefe.common.data.mysql.repository.MyJpaRepository;
import com.welab.wefe.manager.service.entity.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * @Author Jervis
 * @Date 2020-05-22
 **/
public interface MemberRepository extends MyJpaRepository<Member, String> {

    Page<Member> findAllByNameLike(Pageable pageable, String name);

    @Query(value = "select * from u_member where 1=1 " +
            "and if(:id != '', id = :id, 1 = 1) " +
            "and if(:name != '', name like concat('%',:name,'%'), 1 = 1) " +
            "and if(:hidden != '', hidden = :hidden, 1 = 1) " +
            "and if(:freezed != '', freezed = :freezed, 1 = 1) " +
            "and if(:lostContact != '', lost_contact = :lostContact, 1 = 1) "
            , nativeQuery = true, countProjection = "1")
    Page<Member> paging(Pageable pageable,
                        @Param("id") String id,
                        @Param("name") String name,
                        @Param("hidden") String hidden,
                        @Param("freezed") String freezed,
                        @Param("lostContact") String lostContact);


//    @Query(value = "select m from Member m where " +
//            "m.id = ?1 and  m.name = ?2 and m.hidden = ?3 and m.freezed = ?4 and m.lostContact = ?5")
//    Page<Member> paging(Pageable pageable,
//                        String id,
//                        String name,
//                        String hidden,
//                        String freezed,
//                        String lostContact);

}
