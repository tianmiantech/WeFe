package com.welab.wefe.manager.service.mapper;

import com.welab.wefe.manager.service.dto.account.LoginOutput;
import com.welab.wefe.manager.service.entity.User;
import org.mapstruct.Mapper;

/**
 * @Author Jervis
 * @Date 2020-06-09
 **/
@Mapper
public interface UserMapper {

    LoginOutput transfer(User user);
}
