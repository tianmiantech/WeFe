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

package com.welab.wefe.manager.service.mapper;

import com.welab.wefe.common.data.mongodb.entity.manager.Account;
import com.welab.wefe.manager.service.dto.account.LoginOutput;
import com.welab.wefe.manager.service.dto.account.QueryAccountOutput;
import com.welab.wefe.manager.service.dto.account.RegisterInput;
import org.mapstruct.Mapper;

/**
 * @Author Jervis
 * @Date 2020-06-09
 **/
@Mapper
public interface AccountMapper {

    LoginOutput transfer(Account account);

    Account transfer(RegisterInput input);


    QueryAccountOutput transferAccountToQueryUserOutput(Account account);
}
