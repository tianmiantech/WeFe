package com.welab.wefe.manager.service.listener;

import com.welab.wefe.common.data.mongodb.entity.manager.User;
import com.welab.wefe.common.data.mongodb.repo.UserMongoRepo;
import com.welab.wefe.common.util.Md5;
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

    public static final String ADMIN_ACCOUNT = "admin";
    @Autowired
    private UserService userService;

    @Override
    public void onApplicationEvent(ApplicationStartedEvent applicationStartedEvent) {
        LOG.info("init admin account start");
        boolean isExist = userService.checkAdminAccountIsExist(ADMIN_ACCOUNT);
        if(!isExist) {
            User user = new User();
            user.setAccount(ADMIN_ACCOUNT);
            user.setPassword(Md5.of("admin123456"));
            user.setEnable(true);
            user.setAdminRole(true);
            user.setSuperAdminRole(true);
            user.setNickname(ADMIN_ACCOUNT);
            userService.register(user);
        }
        LOG.info("init admin account end");
    }
}
