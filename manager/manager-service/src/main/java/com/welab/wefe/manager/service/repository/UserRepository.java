package com.welab.wefe.manager.service.repository;

import com.welab.wefe.common.data.mysql.repository.MyJpaRepository;
import com.welab.wefe.manager.service.entity.User;

/**
 * @Author Jervis
 * @Date 2020-06-09
 **/
public interface UserRepository extends MyJpaRepository<User, String> {

    User findByAccountAndPassword(String account, String password);
}
