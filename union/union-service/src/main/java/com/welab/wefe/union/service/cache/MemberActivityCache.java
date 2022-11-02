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

package com.welab.wefe.union.service.cache;

import com.welab.wefe.common.data.mongodb.entity.union.Member;
import com.welab.wefe.common.util.DateUtil;
import com.welab.wefe.common.util.StringUtil;

import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Member active cache
 *
 * @author aaron.li
 **/
public class MemberActivityCache {
    private static MemberActivityCache memberActivityCache = new MemberActivityCache();

    /**
     * KEY: member id; VALUE: member information
     */
    private static ConcurrentHashMap<String, Member> memberMap = new ConcurrentHashMap<>();

    private MemberActivityCache() {
    }

    public static MemberActivityCache getInstance() {
        return memberActivityCache;
    }

    /**
     * Add to cache
     */
    public void add(Member member) {
        if (null == member || StringUtil.isEmpty(member.getMemberId())) {
            return;
        }

        memberMap.put(member.getMemberId(), member);
    }

    public boolean isActivePeriod(Member member) {
        if (null == member || StringUtil.isEmpty(member.getMemberId()) || !memberMap.containsKey(member.getMemberId())) {
            return false;
        }
        Member cacheMember = memberMap.get(member.getMemberId());
        if (StringUtil.isEmpty(cacheMember.getLastActivityTime()) || StringUtil.isEmpty(member.getLastActivityTime())) {
            return false;
        }

        try {
            long activityTime = DateUtil.dateMillis(new Date(Long.parseLong(member.getLastActivityTime())));
            long cacheActivityTime = DateUtil.dateMillis(new Date(Long.parseLong(cacheMember.getLastActivityTime())));
            return activityTime <= cacheActivityTime;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
