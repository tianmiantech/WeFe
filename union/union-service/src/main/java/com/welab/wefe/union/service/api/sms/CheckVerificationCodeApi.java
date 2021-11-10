package com.welab.wefe.union.service.api.sms;

import com.welab.wefe.common.data.mongodb.constant.SmsBusinessTypeEnum;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.common.web.dto.NoneApiOutput;
import com.welab.wefe.union.service.service.sms.SmsService;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;

/**
 * @author aaron.li
 * @date 2021/11/9 11:44
 **/
@Api(path = "sms/check_verification_code", name = "check sms verification code")
public class CheckVerificationCodeApi extends AbstractApi<CheckVerificationCodeApi.Input, NoneApiOutput> {
    @Autowired
    private SmsService smsService;

    @Override
    protected ApiResult<NoneApiOutput> handle(Input input) throws StatusCodeWithException, IOException {
        smsService.checkVerificationCodeValid(input.mobile, input.code, input.businessType);
        return success();
    }

    public static class Input extends AbstractApiInput {
        @Check(name = "Mobile phone number check verification code", require = true)
        private String mobile;

        @Check(name = "verification code", require = true)
        private String code;

        @Check(name = "business type", require = true)
        private SmsBusinessTypeEnum businessType;

        public String getMobile() {
            return mobile;
        }

        public void setMobile(String mobile) {
            this.mobile = mobile;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public SmsBusinessTypeEnum getBusinessType() {
            return businessType;
        }

        public void setBusinessType(SmsBusinessTypeEnum businessType) {
            this.businessType = businessType;
        }
    }
}
