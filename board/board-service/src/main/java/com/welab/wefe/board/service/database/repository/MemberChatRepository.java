/*
 * Copyright 2021 Tianmian Tech. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.welab.wefe.board.service.database.repository;

import com.welab.wefe.board.service.database.entity.chat.MemberChatMySqlModel;
import com.welab.wefe.board.service.database.repository.base.BaseRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author Johnny.lin
 */
@Repository
public interface MemberChatRepository extends BaseRepository<MemberChatMySqlModel, String> {

    @Query(value = "select * from #{#entityName} where (from_member_id = :fromMemberId and to_member_id = :toMemberId) or (from_member_id = :toMemberId and to_member_id = :fromMemberId)", nativeQuery = true)
    List<MemberChatMySqlModel> queryAllChatDetail(@Param("fromMemberId") String fromMemberId, @Param("toMemberId") String toMemberId);

    @Query(value = "select member_id, max(max_time) from ("
            + "    select from_member_id as member_id, max(created_time) as max_time from member_chat mc "
            + "        where from_member_id <> :selfMemberId "
            + "        group by from_member_id "
            + "     union "
            + "    select to_member_id as member_id, max(created_time) as max_time from member_chat mc "
            + "        where to_member_id <> :selfMemberId "
            + "        group by to_member_id "
            + ") a "
            + " group by member_id order by max_time desc", nativeQuery = true)
    List<Object[]> queryChatList(@Param("selfMemberId") String selfMemberId);


    @Modifying(clearAutomatically = true)
    @Query(value = "update member_chat set status = :newStatus where from_account_id = :fromAccountId " +
            "and to_account_id = :toAccountId and status = :status", nativeQuery = true)
    void updateMessageStatus(@Param("fromAccountId") String fromAccountId, @Param("toAccountId") String toAccountId, @Param("status") int status, @Param("newStatus") int newStatus);
}
