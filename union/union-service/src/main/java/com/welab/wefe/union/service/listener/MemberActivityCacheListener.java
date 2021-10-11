/**
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

package com.welab.wefe.union.service.listener;

import cn.hutool.core.thread.ThreadUtil;
import com.welab.wefe.common.data.mongodb.entity.contract.data.Member;
import com.welab.wefe.union.service.UnionService;
import com.welab.wefe.union.service.cache.MemberActivityCache;
import com.welab.wefe.union.service.service.MemberContractService;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Report the latest active time of members to the blockchain
 *
 * @author aaron.li
 **/
@Component
public class MemberActivityCacheListener implements ApplicationListener<ApplicationStartedEvent> {
    private static final Logger LOG = LoggerFactory.getLogger(MemberActivityCacheListener.class);

    @Override
    public void onApplicationEvent(ApplicationStartedEvent event) {
        LOG.info("MemberActivityCacheListener start");
        ThreadUtil.execAsync(() -> {
            MemberActivityCache cache = MemberActivityCache.getInstance();

            while (true) {
                try {
                    List<Member> list = cache.getTotalList();
                    //Sleep for 3 minutes if the data is empty
                    if (CollectionUtils.isEmpty(list)) {
                        ThreadUtil.sleep(3, TimeUnit.MINUTES);
                        continue;
                    }

                    for (Member member : list) {
                        updateMember(member);
                        cache.remove(member.getMemberId());
                        //Sleep for 3 seconds to prevent too frequent operation of the blockchain
                        ThreadUtil.sleep(3, TimeUnit.SECONDS);
                    }
                } catch (Exception e) {
                    LOG.error("MemberActivityCacheListener error", e);
                    ThreadUtil.sleep(10, TimeUnit.SECONDS);
                }
            }
        });
    }

    /**
     * Update member active time to the blockchain
     */
    private void updateMember(Member member) {
        try {
            MemberContractService memberContractService = UnionService.CONTEXT.getBean(MemberContractService.class);
            memberContractService.updateLastActivityTimeById(member.getMemberId(), member.getLastActivityTime());
        } catch (Exception e) {
            LOG.error("updateLastActivityTimeById error,member id: " + member.getId() + ", name: " + member.getName(), e);
        }
    }
}
