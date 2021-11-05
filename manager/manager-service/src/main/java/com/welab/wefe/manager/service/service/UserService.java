package com.welab.wefe.manager.service.service;

import com.welab.wefe.common.data.mongodb.entity.manager.User;
import com.welab.wefe.common.data.mongodb.repo.UserMongoRepo;
import com.welab.wefe.common.util.Md5;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author yuxin.zhang
 */
@Service
@Transactional(transactionManager = "transactionManagerManager", rollbackFor = Exception.class)
public class UserService {
    @Value(value = "${password.salt}")
    private String passwordSalt;
    @Autowired
    private UserMongoRepo userMongoRepo;

    public boolean checkAdminAccountIsExist(String account) {
        boolean result = false;
        User user = userMongoRepo.findByAccount(account);
        if (user != null && user.isSuperAdminRole() && user.isAdminRole()) {
            result = true;
        }
        return result;
    }


    public void register(User user) {
        user.setPassword(Md5.of(user.getPassword() + passwordSalt));
        userMongoRepo.save(user);
    }
}
