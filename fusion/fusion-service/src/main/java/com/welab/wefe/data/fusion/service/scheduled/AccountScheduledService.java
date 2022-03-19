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
package com.welab.wefe.data.fusion.service.scheduled;

import com.welab.wefe.data.fusion.service.database.repository.AccountRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 对 account 表的定时任务
 *
 * @author zane
 * @date 2022/03/16
 */
@Component
@Lazy(false)
public class AccountScheduledService {
    protected final Logger LOG = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private AccountRepository accountRepository;

    @Scheduled(fixedDelay = 600_000, initialDelay = 10_000)
    //@Scheduled(fixedDelay = 5_000, initialDelay = 1_000)
    public void run() {

        LOG.info("begin disableAccountWithoutAction90Days...");
        int count = accountRepository.disableAccountWithoutAction90Days();
        LOG.info("end disableAccountWithoutAction90Days:" + count);

        LOG.info("begin cancelAccountWithoutAction180Days...");
        count = accountRepository.cancelAccountWithoutAction180Days();
        LOG.info("end cancelAccountWithoutAction180Days:" + count);
    }
}
