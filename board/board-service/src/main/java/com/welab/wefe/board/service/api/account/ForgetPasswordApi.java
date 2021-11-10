package com.welab.wefe.board.service.api.account;

import com.welab.wefe.board.service.service.account.AccountService;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.common.web.dto.NoneApiOutput;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;

/**
 * forget password
 *
 * @author aaron.li
 * @date 2021/11/10 14:08
 **/
@Api(path = "account/forget_password", name = "forgetPassword")
public class ForgetPasswordApi extends AbstractApi<ForgetPasswordApi.Input, NoneApiOutput> {
    @Autowired
    private AccountService accountService;

    @Override
    protected ApiResult<NoneApiOutput> handle(Input input) throws StatusCodeWithException, IOException {
        accountService.forgetPassword(input);
        return success();
    }

    public static class Input extends AbstractApiInput {
        @Check(require = true)
        private String phoneNumber;

        @Check(require = true, regex = "^.{6,128}$")
        private String password;

        @Check(require = true)
        private String smsVerificationCode;

        public String getPhoneNumber() {
            return phoneNumber;
        }

        public void setPhoneNumber(String phoneNumber) {
            this.phoneNumber = phoneNumber;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getSmsVerificationCode() {
            return smsVerificationCode;
        }

        public void setSmsVerificationCode(String smsVerificationCode) {
            this.smsVerificationCode = smsVerificationCode;
        }
    }


}
