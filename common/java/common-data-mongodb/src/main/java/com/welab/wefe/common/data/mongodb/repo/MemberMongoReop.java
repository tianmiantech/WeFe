/**
 * Copyright 2021 Tianmian Tech. All Rights Reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.welab.wefe.common.data.mongodb.repo;

import com.mongodb.client.result.UpdateResult;
import com.welab.wefe.common.data.mongodb.dto.PageOutput;
import com.welab.wefe.common.data.mongodb.entity.union.Member;
import com.welab.wefe.common.data.mongodb.entity.union.ext.MemberExtJSON;
import com.welab.wefe.common.data.mongodb.util.QueryBuilder;
import com.welab.wefe.common.data.mongodb.util.UpdateBuilder;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

/**
 * @author yuxin.zhang
 */
@Repository
public class MemberMongoReop extends AbstractMongoRepo {
    public boolean deleteMemberById(String memberId) {
        if (StringUtils.isEmpty(memberId)) {
            return false;
        }
        Query query = new QueryBuilder().append("memberId", memberId).build();
        Update update = new UpdateBuilder().append("status", "1").build();
        UpdateResult updateResult = mongoTemplate.updateFirst(query, update, Member.class);
        return updateResult.wasAcknowledged();
    }

    public boolean updateLastActivityTimeById(String lastActivityTime, String memberId) {
        if (StringUtils.isEmpty(memberId)) {
            return false;
        }
        Query query = new QueryBuilder().append("memberId", memberId).build();
        Update update = new UpdateBuilder().append("lastActivityTime", lastActivityTime).build();
        UpdateResult updateResult = mongoTemplate.updateFirst(query, update, Member.class);
        return updateResult.wasAcknowledged();
    }


    public boolean updateLogoById(String logo, String memberId) {
        if (StringUtils.isEmpty(memberId)) {
            return false;
        }
        Query query = new QueryBuilder().append("memberId", memberId).build();
        Update update = new UpdateBuilder().append("logo", logo).build();
        UpdateResult updateResult = mongoTemplate.updateFirst(query, update, Member.class);
        return updateResult.wasAcknowledged();
    }


    public boolean updateExcludeLogo(Member member) {
        if (member == null || StringUtils.isEmpty(member.getMemberId())) {
            return false;
        }
        Query query = new QueryBuilder().append("memberId", member.getMemberId()).build();
        Update update = new UpdateBuilder()
                .append("name", member.getName())
                .append("mobile", member.getMobile())
                .append("allowOpenDataSet", member.getAllowOpenDataSet())
                .append("hidden", member.getHidden())
                .append("freezed", member.getFreezed())
                .append("lostContact", member.getLostContact())
                .append("publicKey", member.getPublicKey())
                .append("email", member.getEmail())
                .append("gatewayUri", member.getGatewayUri())
                .append("updatedTime", member.getUpdatedTime())
                .append("lastActivityTime", member.getLastActivityTime())
                .append("logTime", member.getLogTime())
                .append("dataSyncTime", member.getDataSyncTime())
                .append("extJson", member.getExtJson())
                .build();
        UpdateResult updateResult = mongoTemplate.updateFirst(query, update, Member.class);
        return updateResult.wasAcknowledged();
    }


    public void upsert(Member member) {
        if (member != null && StringUtils.isNotEmpty(member.getMemberId())) {
            Query query = new QueryBuilder().append("memberId", member.getMemberId()).build();
            Member dbMember = mongoTemplate.findOne(query, Member.class);
            if (dbMember != null) {
                member.setId(dbMember.getId());
            }
            mongoTemplate.save(member);
        }
    }

    public boolean updateExcludePublicKey(Member member) {
        Query query = new QueryBuilder().append("memberId", member.getMemberId()).build();
        Update update = new UpdateBuilder()
                .append("name", member.getName())
                .append("mobile", member.getMobile())
                .append("allowOpenDataSet", member.getAllowOpenDataSet())
                .append("hidden", member.getHidden())
                .append("freezed", member.getFreezed())
                .append("lostContact", member.getLostContact())
                .append("logo", member.getLogo())
                .append("email", member.getEmail())
                .append("gatewayUri", member.getGatewayUri())
                .append("createdTime", member.getCreatedTime())
                .append("updatedTime", member.getUpdatedTime())
                .append("lastActivityTime", member.getLastActivityTime())
                .append("logTime", member.getLogTime())
                .append("dataSyncTime", member.getDataSyncTime())
                .append("extJson", member.getExtJson())
                .build();
        UpdateResult updateResult = mongoTemplate.updateFirst(query, update, Member.class);
        return updateResult.wasAcknowledged();
    }


    public boolean updatePulicKeyById(String publicKey, String memberId) {
        if (StringUtils.isEmpty(memberId)) {
            return false;
        }
        Query query = new QueryBuilder().append("memberId", memberId).build();
        Update update = new UpdateBuilder().append("publicKey", publicKey).build();
        UpdateResult updateResult = mongoTemplate.updateFirst(query, update, Member.class);
        return updateResult.wasAcknowledged();
    }

    public boolean existsByMemberId(String memberId) {
        if (StringUtils.isEmpty(memberId)) {
            return false;
        }
        Query query = new QueryBuilder().append("memberId", memberId).build();
        return mongoTemplate.exists(query, Member.class);
    }

    public Member findMemberId(String memberId) {
        if (StringUtils.isEmpty(memberId)) {
            return null;
        }
        Query query = new QueryBuilder().append("memberId", memberId).build();
        return mongoTemplate.findOne(query, Member.class);
    }


    public List<Member> find(String memberId) {
        List<Member> list = new ArrayList<>();
        if (StringUtils.isNotEmpty(memberId)) {
            Member member = findMemberId(memberId);
            list.add(member);
        } else {
            list = mongoTemplate.findAll(Member.class);
        }
        return list;
    }

    public PageOutput<Member> query(Integer pageIndex, Integer pageSize, String memberId, String name, Boolean hidden, Boolean freezed, Boolean lostContact, Boolean status) {
        String paramHidden = null == hidden ? null : String.valueOf(hidden ? 1 : 0);
        String paramFreezed = null == freezed ? null : String.valueOf(freezed ? 1 : 0);
        String paramLostContact = null == lostContact ? null : String.valueOf(lostContact ? 1 : 0);
        Query query = new QueryBuilder()
                .append("status", status != null ? (status ? 1 : 0) : null)
                .append("memberId", memberId)
                .like("name", name)
                .append("hidden", paramHidden)
                .append("freezed", paramFreezed)
                .append("lostContact", paramLostContact)
                .page(pageIndex, pageSize)
                .build();

        List<Member> list = mongoTemplate.find(query, Member.class);
        long total = mongoTemplate.count(query, Member.class);
        return new PageOutput<>(pageIndex, total, query.getLimit(), list);
    }

    public PageOutput<Member> query(Integer pageIndex, Integer pageSize, String memberId, String name, Boolean hidden, Boolean freezed, Boolean lostContact) {
        return query(pageIndex, pageSize, memberId, name, hidden, freezed, lostContact, null);
    }


    public boolean updateExtJSONById(String memberId, MemberExtJSON extJSON) {
        if (StringUtils.isEmpty(memberId)) {
            return false;
        }
        Query query = new QueryBuilder().append("memberId", memberId).build();
        Update update = new UpdateBuilder().append("extJson", extJSON).build();
        UpdateResult updateResult = mongoTemplate.updateFirst(query, update, Member.class);
        return updateResult.wasAcknowledged();
    }

}
