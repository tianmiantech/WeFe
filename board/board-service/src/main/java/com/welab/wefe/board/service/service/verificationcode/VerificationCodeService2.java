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

package com.welab.wefe.board.service.service.verificationcode;

import com.welab.wefe.board.service.database.entity.AccountMysqlModel;
import com.welab.wefe.board.service.database.repository.AccountRepository;
import com.welab.wefe.board.service.util.BoardSM4Util;
import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.util.StringUtil;
import com.welab.wefe.common.verification.code.AbstractResponse;
import com.welab.wefe.common.verification.code.service.AbstractVerificationCodeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Verification code service class
 *
 * @author aaron.li
 * @date 2022/1/19 11:29
 **/
@Service
public class VerificationCodeService2 extends AbstractVerificationCodeService {
    protected final Logger LOG = LoggerFactory.getLogger(VerificationCodeService2.class);

    @Autowired
    private AccountRepository accountRepository;

    @Override
    public void check(String mobile) throws StatusCodeWithException {
        if (StringUtil.isEmpty(mobile)) {
            throw new StatusCodeWithException("手机号不能为空", StatusCode.PARAMETER_CAN_NOT_BE_EMPTY);
        }

        if (!StringUtil.checkPhoneNumber(mobile)) {
            throw new StatusCodeWithException("非法的手机号", StatusCode.PARAMETER_VALUE_INVALID);
        }
        AccountMysqlModel model = accountRepository.findOne("phoneNumber", BoardSM4Util.encryptPhoneNumber(mobile), AccountMysqlModel.class);
        // phone number error
        if (model == null) {
            throw new StatusCodeWithException("手机号错误，该用户不存在", StatusCode.PARAMETER_VALUE_INVALID);
        }
        if (!model.getEnable()) {
            throw new StatusCodeWithException("用户被禁用，请联系管理员", StatusCode.PERMISSION_DENIED);
        }
    }

    @Override
    public Map<String, Object> buildExtendParams(String mobile) {

        return null;
    }

    @Override
    public void saveSendRecord(String mobile, String verificationCode, AbstractResponse response) {

    }
}
