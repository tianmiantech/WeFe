package com.welab.wefe.union.service.service.sms;


import java.util.Map;

/**
 * @author aaron.li
 * @Date 2021/10/20
 **/
public abstract class AbstractSendSmsClient {

    /**
     * Send verification code sms
     *
     * @param mobile        target phone number
     * @param smsRequest sms request params
     * @return
     * @throws Exception
     */
    public abstract AbstractSmsResponse sendVerificationCode(String mobile, Map<String, Object> smsRequest) throws Exception;
}
