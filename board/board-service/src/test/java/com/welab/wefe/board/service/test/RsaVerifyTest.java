/*
 * Copyright 2021 Tianmian Tech. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.welab.wefe.board.service.test;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.util.RSAUtil;
import com.welab.wefe.common.web.CurrentAccount;
import com.welab.wefe.common.web.dto.SignedApiInput;

import java.nio.charset.StandardCharsets;

/**
 * @author zane
 * @date 2022/5/5
 */
public class RsaVerifyTest {
    public static void main(String[] args) throws Exception {

        String jsonStr = "{\n" +
                "    \"data\":\n" +
                "    {\n" +
                "        \"job_id\": \"dbf8b10fc1d64c6ab959dadfe2aa76eb\"\n" +
                "    },\n" +
                "    \"sign\": \"RSVQq1/b9nIQlsuH2h3b9s00rJQd7+W1Rssrq8Pg+i9V8pKu6lDTAgrMR/al6pIZkbD5t2/uq8zAhLkw8O0aLxoIxMwKROwscKtPwzBos6tuSJvo8Me5nq7mJgd2nob79+hf16IDtwyNJo6eJnFb3ZGoOn8IzrvQCWQztJuKlkgl+sZK/K1TPqZDc5YfFxb2YdELnTDu5ibq4KXhvwdWnhb0Myede4AkTjgiDvSjg0UnNQ/APAqifiBhEyO7xvMOWS3ty9CnCzAWrmVGmgRTD5/2QWO2tkJ8P7N+hNCU65k1Q9SgPzUvXyGLgdg+gxSTkqZ6pVEMwYbiJOIW5/Ne1g==\"\n" +
                "}";
        JSONObject json = JSON.parseObject(jsonStr);
        rsaVerify(json);
        System.out.println(json);
    }

    private static void rsaVerify(JSONObject params) throws Exception {

        // 如果是登录状态，不验签。
        if (CurrentAccount.get() != null) {
            return;
        }

        SignedApiInput signedApiInput = params.toJavaObject(SignedApiInput.class);

        // At present, the board service only serves the application services of its own wefe system,
        // such as gateway and flow, so the same set of public and private keys are used for rsa signatures.
        String publicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAoAm1UxQ1HzJu6Ezf3poIl76q43pqe3DPyMI4fCrKHwGCOxcu8cCKQOKOWPvePu2/+tymSqgsF7dNHYYz82RGUTTLCvq3tvTnrIDeptOAAxr2FBfR1M/sToe6faHjEEpogE4gtagjr/KbcAW8EPUTZUJSGfk+97FEnxtXfIB622oiC3FatDap0mrf/XV0HLG7b2QcihQD68IpGdxMp47zI/zzZATqDweab1Gvdk0NPSpZQaX5twQUKn9e6hmc9Y43HeKHc3UNaPC+fLCxGozwBIbinWuzE3fXveiyjGj5sZKJN7hVhaKUh3HFIr9TB70j70shJfYy8r7yg0ezhC7gcwIDAQAB";

        boolean verified = RSAUtil.verify(
                signedApiInput.getData().getBytes(StandardCharsets.UTF_8),
                RSAUtil.getPublicKey(publicKey),
                signedApiInput.getSign()
        );
        if (!verified) {
            throw new StatusCodeWithException("错误的签名", StatusCode.PARAMETER_VALUE_INVALID);
        }

        params.clear();

        JSONObject data = JSONObject.parseObject(signedApiInput.getData());

        params.putAll(data);
    }
}
