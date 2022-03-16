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

package com.welab.wefe.manager.service.listener;

import com.welab.wefe.common.data.mongodb.entity.manager.User;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.util.Md5;
import com.welab.wefe.common.wefe.enums.AuditStatus;
import com.welab.wefe.manager.service.constant.UserConstant;
import com.welab.wefe.manager.service.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

/**
 * @Description:
 * @author: yuxin.zhang
 * @date: 2021/11/5
 */
@Component
public class AdminAccountInitListener implements ApplicationListener<ApplicationStartedEvent> {
    private static final Logger LOG = LoggerFactory.getLogger(AdminAccountInitListener.class);

    @Autowired
    private UserService userService;

    @Override
    public void onApplicationEvent(ApplicationStartedEvent applicationStartedEvent) {
        LOG.info("init admin account start");
        boolean isExist = userService.checkAdminAccountIsExist(UserConstant.ADMIN_ACCOUNT);
        if(!isExist) {
            User user = new User();
            user.setAccount(UserConstant.ADMIN_ACCOUNT);
            user.setPassword(Md5.of(UserConstant.DEFAULT_PASSWORD));
            user.setEnable(true);
            user.setAdminRole(true);
            user.setSuperAdminRole(true);
            user.setRealname(UserConstant.ADMIN_ACCOUNT);
            user.setAuditStatus(AuditStatus.agree);
            user.setNeedUpdatePassword(true);
            try {
                userService.register(user);
            } catch (StatusCodeWithException e) {
                LOG.info("init admin account fail:" + e);
                System.exit(1);
            }
        }
        LOG.info("init admin account end");
    }
}
