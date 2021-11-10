package com.welab.wefe.union.service.service.sms;

import com.aliyun.dysmsapi20170525.Client;
import com.aliyun.dysmsapi20170525.models.SendSmsRequest;
import com.aliyun.dysmsapi20170525.models.SendSmsResponse;
import com.aliyun.teaopenapi.models.Config;
import com.welab.wefe.common.util.JObject;

import java.util.Map;


/**
 * @author aaron.li
 * @Date 2021/10/20
 **/
public class AliyunSendSmsClient extends AbstractSendSmsClient {
    private Client client;

    public static AliyunSendSmsClient createClient(String accessKeyId, String accessKeySecret) throws Exception {
        Config config = new Config().setAccessKeyId(accessKeyId).setAccessKeySecret(accessKeySecret);
        config.endpoint = "dysmsapi.aliyuncs.com";
        AliyunSendSmsClient aliyunSmsClient = new AliyunSendSmsClient();
        aliyunSmsClient.client = new Client(config);
        return aliyunSmsClient;
    }

    @Override
    public AbstractSmsResponse sendVerificationCode(String mobile, Map<String, Object> smsRequest) throws Exception {
        SendSmsRequest sendSmsRequest = new SendSmsRequest();
        sendSmsRequest.setPhoneNumbers(mobile);
        sendSmsRequest.setSignName(String.valueOf(smsRequest.get("SignName")));
        sendSmsRequest.setTemplateCode(String.valueOf(smsRequest.get("templateCode")));
        sendSmsRequest.setTemplateParam(JObject.create("code", smsRequest.get("code")).toString());
        SendSmsResponse sendSmsResponse = this.client.sendSms(sendSmsRequest);
        return new AliyunSmsResponse(sendSmsResponse);
    }
}
