package com.welab.wefe.manager.service.mapper;

import com.welab.wefe.common.data.mongodb.entity.manager.User;
import com.welab.wefe.manager.service.dto.account.LoginOutput;
import com.welab.wefe.manager.service.dto.account.RegisterInput;
import org.mapstruct.Mapper;

/**
 * @Author Jervis
 * @Date 2020-06-09
 **/
@Mapper
public interface UserMapper {

    LoginOutput transfer(User user);

    User transfer(RegisterInput input);
}
