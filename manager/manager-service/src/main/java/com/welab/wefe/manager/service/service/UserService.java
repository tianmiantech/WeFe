package com.welab.wefe.manager.service.service;

import com.welab.wefe.manager.service.entity.User;
import com.welab.wefe.manager.service.repository.UserRepository;
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
    private UserRepository mRepository;

    public User find(String account, String password) {
        return mRepository.findByAccountAndPassword(account, password);
    }
}
