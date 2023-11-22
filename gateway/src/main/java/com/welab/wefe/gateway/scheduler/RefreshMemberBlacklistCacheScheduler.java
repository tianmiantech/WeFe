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

package com.welab.wefe.gateway.scheduler;

import com.welab.wefe.gateway.cache.MemberBlacklistCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Refresh member blacklist cache scheduler
 *
 * @author aaron.li
 **/
@Component
public class RefreshMemberBlacklistCacheScheduler {
    private final Logger LOG = LoggerFactory.getLogger(RefreshMemberBlacklistCacheScheduler.class);

    @Scheduled(cron = "0/10 * * * * ?")
    public void execute() {
        LOG.info("Start refreshing member blacklist cache....................");
        if (!MemberBlacklistCache.getInstance().refreshCache()) {
            LOG.error("Failed to refresh member blacklist cache.");
        } else {
            LOG.info("Refresh member blacklist cache completed.");
        }
    }
}
