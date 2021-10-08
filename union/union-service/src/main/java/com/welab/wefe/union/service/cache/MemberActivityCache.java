/**
 * Copyright 2021 The WeFe Authors. All Rights Reserved.
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

package com.welab.wefe.union.service.cache;

import com.welab.wefe.common.data.mongodb.entity.contract.data.Member;
import com.welab.wefe.common.util.StringUtil;

import java.util.ArrayList;
import java.util.List;
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

    /**
     * Delete cache
     */
    public Member remove(String key) {
        if (StringUtil.isEmpty(key)) {
            return null;
        }

        return memberMap.remove(key);
    }

    /**
     * Get a list of cached data
     */
    public List<Member> getTotalList() {
        List<Member> list = new ArrayList<>();
        memberMap.forEach((key, value) -> list.add(value));
        return list;
    }
}
