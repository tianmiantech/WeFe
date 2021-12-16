package com.welab.wefe.manager.service.mapper;

import com.welab.wefe.common.data.mongodb.entity.manager.User;
import com.welab.wefe.manager.service.dto.user.LoginOutput;
import com.welab.wefe.manager.service.dto.user.QueryUserOutput;
import com.welab.wefe.manager.service.dto.user.RegisterInput;
import org.mapstruct.Mapper;

/**
 * @Author Jervis
 * @Date 2020-06-09
 **/
@Mapper
public interface UserMapper {

    LoginOutput transfer(User user);

    User transfer(RegisterInput input);


    QueryUserOutput transferUserToQueryUserOutput(User user);
}
