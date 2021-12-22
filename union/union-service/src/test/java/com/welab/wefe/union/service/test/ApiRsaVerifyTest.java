/**
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
package com.welab.wefe.union.service.test;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.util.RSAUtil;
import com.welab.wefe.common.web.dto.SignedApiInput;

import java.nio.charset.StandardCharsets;
import java.security.interfaces.RSAPublicKey;

/**
 * @author zane
 * @date 2021/11/4
 */
public class ApiRsaVerifyTest {
    public static void main(String[] args) throws Exception {
        String data = "{\"data\":\"{}\",\"member_id\":\"d0f47307804844898ecfc65b875abe87\",\"sign\":\"C5F207lYz8aYtv7Hp1lW+LAI8WZAeudJyDBbzks0MvthwcaG9KHHps8GekLEntXgpyZT9w1TebD8pEs2r35wL43qLax044efenDymHca5tY+ibIDInw43SEohpa5KGNFgK+TypUn/Tw3agSzmx3ke9TG4X3ZhsG3t1FyFdWMTagBdFZ+uFMp8JY0DCPX3cW7bs1l7PO7jzqsQ57y7EVotXx1Di7VK77GYAkbiou6ZWOdLSptX95NHz6qMrLpOirON65XWcdJxscgJ4c9u4aiVmHAKDh6xA/dHOvXcay948dtyHEfScydLg6i6AQsrFPaEfgMRrGSg1OUc2MDuhFWzg==\"}";
        JSONObject params = JSON.parseObject(data);
        rsaVerify(params);
    }

    private static void rsaVerify(JSONObject params) throws Exception {
        SignedApiInput signedApiInput = params.toJavaObject(SignedApiInput.class);
        String publicKeyStr = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAkmuhkL/3KZTEST7HZ+rm4yXuPrdzxEm2voRGbiQh1sANqP5d93EHBYiR+BxPDzyJGrKxq+VVDDsUjwiOmMHIbuliGAtFX428pXEDiIJYuzHMPxcpjNYl2OPukzMXJmCr6CpYyA/4K9MwgTtBuffQxGhZ7knS5tZ/OrS12mfyUgKY+VEqU/X3XbfIt/wg0j8L4GkhbR/l1NRdPxoAPbnbJrTnvEqz1QJjaYqmYy1JcAQy2lmnj4O23oFU94gJXoJ9e2VD/MF9G1uFG2aUhhxBhCEN9cWhXhntcUC8OC+m6CIqbjY0FiPUpSWqtWdLZZCDlW+2AhP8ce2w9qvo0ple7QIDAQAB";
        RSAPublicKey publicKey = RSAUtil.getPublicKey(publicKeyStr);
        boolean verified = RSAUtil.verify(signedApiInput.getData().getBytes(StandardCharsets.UTF_8), publicKey , signedApiInput.getSign());
        if (!verified) {
            throw new StatusCodeWithException("Wrong signature", StatusCode.PARAMETER_VALUE_INVALID);
        }

        params.putAll(JSONObject.parseObject(signedApiInput.getData()));
        params.put("cur_member_id", signedApiInput.getMemberId());
    }
}
