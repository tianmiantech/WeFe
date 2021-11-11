package com.welab.wefe.board.service.api.union;

import com.welab.wefe.board.service.sdk.UnionService;
import com.welab.wefe.common.enums.SmsBusinessType;
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
 * @author aaron.li
 * @date 2021/11/11 09:45
 **/
@Api(path = "union/send_forget_password_sms_code", name = "send sms verification code", login = false)
public class SendForgetPasswordSmsCodeApi extends AbstractApi<SendForgetPasswordSmsCodeApi.Input, NoneApiOutput> {
    @Autowired
    private UnionService unionService;

    @Override
    protected ApiResult<NoneApiOutput> handle(Input input) throws StatusCodeWithException, IOException {
        unionService.sendVerificationCode(input.phoneNumber, SmsBusinessType.AccountForgetPasswordVerificationCode);
        return success();
    }

    public static class Input extends AbstractApiInput {
        @Check(require = true)
        private String phoneNumber;

        public String getPhoneNumber() {
            return phoneNumber;
        }

        public void setPhoneNumber(String phoneNumber) {
            this.phoneNumber = phoneNumber;
        }
    }
}
