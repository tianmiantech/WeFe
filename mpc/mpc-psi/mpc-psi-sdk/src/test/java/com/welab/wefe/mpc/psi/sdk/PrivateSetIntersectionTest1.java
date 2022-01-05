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

package com.welab.wefe.mpc.psi.sdk;

import com.alibaba.fastjson.JSONObject;
import com.welab.wefe.mpc.config.CommunicationConfig;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

public class PrivateSetIntersectionTest1 {

    @Test
    public void test() {
        JSONObject data = new JSONObject();
        data.put("phone_number", "xxxxxx");
        data.put("nickname", "xxxxxx");
        data.put("email", "xxxxx@xxxxx");
        String clientId = MD5Util.getMD5String(data.getString("phone_number") + data.getString("nickname"))
                + SHA256Utils.getSHA256(data.getString("email"));
        List<String> clientIds = Arrays.asList(new String[]{clientId});
        int keySize = 1024;
        PrivateSetIntersection privateSetIntersection = new PrivateSetIntersection();
        CommunicationConfig config = new CommunicationConfig();
        String privateKey = "MIICdgIBADANBgkqhkiG9w0BAQEFAASCAmAwggJcAgEAAoGBAKUOtGj39LY9PABvGuFNwZu520TNsRBCQSt0XCqqNsD+TsfUg8OMdaJVbzASrXeI57W5Za322dVirSTAgEekJIRU04zHfDeKc/JhQvuR0+5vMqDrFZ89KzfsN9TUJxHESwwZ0CV0ZOywCEH5VzcOa4cBjJtEGfJWExUBDKdlqXH/AgMBAAECgYBOXzMOfF23gk/RLPAodpEtbgxNGRWv0KW9Cl0Q7Q1eieHCRIfj+/eHAuXuf4/aKClNQiCjf4hjJ51qy/SdD7U+U+dm+UiBcgHrOhYm2Co9KNEGJKF978vv+CSngXtmOF/ZUdofW6yfTez9ZpsbtQYxmXYGQYVnooLbHEe9tCCioQJBAOUCWy4H8BjluZDFADb5jbwrQfVvtBdHmOu4Uu+WR/EgGBLTVMu4kpwKK6go9631Q47FdO2dWfoIzNvv6ZACPQkCQQC4gs1Tf4sx4r/2ou3C/qnLJerM+mLaYpQG3EZtw4zEjndtaklldftAh5xu019P3HessoT3NQ/xuuWUQ947jADHAkAhhz3IOHtLed64Nk94vQKmSQMIJwmL2vyljj/+Oddgkx1TLEOe6+/zDn4jyZOxkVYJwhkDbOUueTlc/fwJDHrZAkEAqzbFbWv3MG1nEGiUFNPXn2kp/teBj4DWN5+DwysonuRMsj1kqj/WzESKxtRhp2u/qYNmmzaj+v4hN3na6Iq71QJATMZBjksGzILd9oSwzVN8iQREkdtHdZnvakT44pp9a1UrgPEHS6YGI3BqVoPjJkiJSWr3S3OwzbMo5EKkAZ6fqw==";
        config.setSignPrivateKey(privateKey);
        config.setCommercialId("tianmain");
        config.setServerUrl("localhost:8080/serving-service/api/query/black-list-match");
        List<String> result = privateSetIntersection.query(config,
                clientIds, keySize);
        System.out.println(clientId);
        System.out.println(result);
    }
}