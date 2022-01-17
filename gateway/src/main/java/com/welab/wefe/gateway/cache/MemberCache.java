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

package com.welab.wefe.gateway.cache;

import com.welab.wefe.common.constant.SecretKeyType;
import com.welab.wefe.common.util.ThreadUtil;
import com.welab.wefe.gateway.GatewayServer;
import com.welab.wefe.gateway.entity.MemberEntity;
import com.welab.wefe.gateway.service.base.AbstractMemberService;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Member information cache
 *
 * @author aaron.li
 **/
public class MemberCache {
    private final Logger LOG = LoggerFactory.getLogger(MemberCache.class);

    private static MemberCache memberCache = new MemberCache();
    /**
     * All member information（Key：Member ID of the member，Value：Member information object）
     */
    private static ConcurrentHashMap<String, MemberEntity> totalMember = new ConcurrentHashMap<>();
    /**
     * Own member information of gateway
     */
    private static MemberEntity selfMember = null;


    private MemberCache() {
    }

    public static MemberCache getInstance() {
        return memberCache;
    }

    /**
     * Refresh cache
     */
    public boolean refreshCache() {
        try {
            // Priority to load own member information
            if (!refreshSelfMemberCache()) {
                return false;
            }

            AbstractMemberService memberService = GatewayServer.CONTEXT.getBean(AbstractMemberService.class);
            List<MemberEntity> queryList = memberService.find(null);
            if (CollectionUtils.isEmpty(queryList)) {
                totalMember.clear();
                return false;
            }

            // Update cache
            List<String> queryMemberIds = new ArrayList<>();
            for (MemberEntity member : queryList) {
                queryMemberIds.add(member.getId());
                totalMember.put(member.getId(), member);
            }

            // Clear deleted member information
            List<String> delMemberIds = new ArrayList<>();
            totalMember.forEach((key, value) -> {
                if (!queryMemberIds.contains(key)) {
                    delMemberIds.add(key);
                }
            });
            for (String delMemberId : delMemberIds) {
                totalMember.remove(delMemberId);
            }

            return true;
        } catch (Exception e) {
            LOG.error("Refresh MemberCache exception: ", e);
            return false;
        }
    }

    /**
     * Refresh the specified member information
     */
    public MemberEntity refreshCacheById(String memberId) {
        try {
            AbstractMemberService memberService = GatewayServer.CONTEXT.getBean(AbstractMemberService.class);
            List<MemberEntity> queryList = memberService.find(memberId);
            if (CollectionUtils.isEmpty(queryList)) {
                totalMember.remove(memberId);
                LOG.info("Not exist memberId:" + memberId + " info.");
                return null;
            }

            MemberEntity member = queryList.get(0);
            totalMember.put(member.getId(), member);
            return member;

        } catch (Exception e) {
            LOG.error("Refresh MemberCache by memberId: " + memberId + " exception " + memberId, e);
            return null;
        }
    }

    /**
     * Refresh own member information
     */
    public boolean refreshSelfMemberCache() {
        try {
            AbstractMemberService memberService = GatewayServer.CONTEXT.getBean(AbstractMemberService.class);
            MemberEntity entity = memberService.findSelf();
            if (null == entity) {
                return false;
            }
            if (null == selfMember) {
                selfMember = entity;
            } else {
                selfMember.setId(entity.getId());
                selfMember.setName(entity.getName());
                selfMember.setPrivateKey(entity.getPrivateKey());
                selfMember.setBoardUri(entity.getBoardUri());
                selfMember.setPublicKey(entity.getPublicKey());
                selfMember.setIp(entity.getIp());
                selfMember.setPort(entity.getPort());
                selfMember.setSecretKeyType(null == entity.getSecretKeyType() ? SecretKeyType.rsa : entity.getSecretKeyType());
            }
            return true;
        } catch (Exception e) {
            LOG.error("Refresh self member cache exception ", e);
            return false;
        }
    }

    /**
     * Refresh its own member information until the database initialization is completed
     */
    public boolean refreshSelfMemberCacheUntilComplete() {
        while (true) {
            if (refreshSelfMemberCache()) {
                return true;
            } else {
                LOG.info("Its own member information has not been initialized. Please log in to the board system to improve its member information......");
                ThreadUtil.sleepSeconds(3);
            }
        }
    }

    /**
     * Refresh all member information until database initialization is complete
     */
    public boolean refreshTotalMemberCacheUntilComplete() {
        while (true) {
            if (refreshCache()) {
                return true;
            } else {
                LOG.info("All member information has not been initialized. Please report member information......");
                ThreadUtil.sleepSeconds(3);
            }
        }
    }


    public void put(String key, MemberEntity member) {
        totalMember.put(key, member);
    }

    public MemberEntity get(String key) {
        return totalMember.get(key);
    }

    public void putAll(Map<String, MemberEntity> all) {
        totalMember.putAll(all);
    }

    public MemberEntity getSelfMember() {
        return selfMember;
    }

    public void setSelfMember(MemberEntity selfMember) {
        MemberCache.selfMember = selfMember;
    }
}
