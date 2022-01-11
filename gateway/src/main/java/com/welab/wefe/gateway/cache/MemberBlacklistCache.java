/*
 * Copyright 2021 Tianmian Tech. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.welab.wefe.gateway.cache;

import com.welab.wefe.gateway.GatewayServer;
import com.welab.wefe.gateway.entity.BlacklistEntity;
import com.welab.wefe.gateway.service.BlacklistService;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * Member blacklist cache
 *
 * @author aaron.li
 **/
public class MemberBlacklistCache {
    private final static Logger LOG = LoggerFactory.getLogger(MemberBlacklistCache.class);

    private static MemberBlacklistCache MEMBER_BLACKLIST_CACHE = new MemberBlacklistCache();


    /**
     * Blacklist member ID list
     */
    private static ConcurrentSkipListSet<String> BLACKLIST_SET = new ConcurrentSkipListSet<>();

    private MemberBlacklistCache() {
    }

    public static MemberBlacklistCache getInstance() {
        return MEMBER_BLACKLIST_CACHE;
    }


    /**
     * Refresh cache
     */
    public boolean refreshCache() {
        try {
            BlacklistService blacklistService = GatewayServer.CONTEXT.getBean(BlacklistService.class);
            List<BlacklistEntity> blacklistEntityList = blacklistService.queryAll();
            if (CollectionUtils.isEmpty(blacklistEntityList)) {
                BLACKLIST_SET.clear();
                return true;
            }

            Set<String> dbBlacklistMemberIdSet = new HashSet<>(16);
            blacklistEntityList.forEach(blacklistEntity -> dbBlacklistMemberIdSet.add(blacklistEntity.getBlacklistMemberId()));
            // Update cache
            BLACKLIST_SET.addAll(dbBlacklistMemberIdSet);
            // Blacklist of deleted members member ID list
            List<String> delMemberIdList = new ArrayList<>();
            for (String cacheMemberId : BLACKLIST_SET) {
                if (!dbBlacklistMemberIdSet.contains(cacheMemberId)) {
                    delMemberIdList.add(cacheMemberId);
                }
            }

            // Remove the deleted member blacklist from the cache
            BLACKLIST_SET.removeAll(delMemberIdList);
            return true;
        } catch (Exception e) {
            LOG.error("Failed to refresh member blacklist cache: ", e);
            return false;
        }
    }

    /**
     * Does the member exist in the blacklist
     *
     * @param memberId member id
     * @return true: exist；false：non-existent
     */
    public boolean isExistBlacklist(String memberId) {
        return BLACKLIST_SET.contains(memberId);
    }
}
