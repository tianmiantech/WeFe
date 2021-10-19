package com.welab.wefe.manager.service.service;

import com.welab.wefe.common.data.mongodb.entity.manager.User;
import com.welab.wefe.common.data.mongodb.repo.UserMongoRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @Author Jervis
 * @Date 2020-06-09
 **/
@Service
@Transactional(rollbackFor = Exception.class)
public class UserService {

    @Autowired
    private UserMongoRepo userMongoRepo;

    public User find(String account, String password) {
        return userMongoRepo.find(account, password);
    }
}
