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

package com.welab.wefe.mpc.pir.sdk;

import com.alibaba.fastjson.JSONObject;
import com.welab.wefe.mpc.config.CommunicationConfig;
import com.welab.wefe.mpc.pir.sdk.config.PrivateInformationRetrievalConfig;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @Author: eval
 * @Date: 2021-12-29
 **/
public class PrivateInformationRetrievalQueryTest {

    @Test
    public void query() throws Exception {
        CommunicationConfig communicationConfig = new CommunicationConfig();
        communicationConfig.setApiName("api/query/social_score");
        communicationConfig.setNeedSign(true);
        communicationConfig.setCommercialId("tianmain");
        String privateKey = "MIICdgIBADANBgkqhkiG9w0BAQEFAASCAmAwggJcAgEAAoGBAKUOtGj39LY9PABvGuFNwZu520TNsRBCQSt0XCqqNsD+TsfUg8OMdaJVbzASrXeI57W5Za322dVirSTAgEekJIRU04zHfDeKc/JhQvuR0+5vMqDrFZ89KzfsN9TUJxHESwwZ0CV0ZOywCEH5VzcOa4cBjJtEGfJWExUBDKdlqXH/AgMBAAECgYBOXzMOfF23gk/RLPAodpEtbgxNGRWv0KW9Cl0Q7Q1eieHCRIfj+/eHAuXuf4/aKClNQiCjf4hjJ51qy/SdD7U+U+dm+UiBcgHrOhYm2Co9KNEGJKF978vv+CSngXtmOF/ZUdofW6yfTez9ZpsbtQYxmXYGQYVnooLbHEe9tCCioQJBAOUCWy4H8BjluZDFADb5jbwrQfVvtBdHmOu4Uu+WR/EgGBLTVMu4kpwKK6go9631Q47FdO2dWfoIzNvv6ZACPQkCQQC4gs1Tf4sx4r/2ou3C/qnLJerM+mLaYpQG3EZtw4zEjndtaklldftAh5xu019P3HessoT3NQ/xuuWUQ947jADHAkAhhz3IOHtLed64Nk94vQKmSQMIJwmL2vyljj/+Oddgkx1TLEOe6+/zDn4jyZOxkVYJwhkDbOUueTlc/fwJDHrZAkEAqzbFbWv3MG1nEGiUFNPXn2kp/teBj4DWN5+DwysonuRMsj1kqj/WzESKxtRhp2u/qYNmmzaj+v4hN3na6Iq71QJATMZBjksGzILd9oSwzVN8iQREkdtHdZnvakT44pp9a1UrgPEHS6YGI3BqVoPjJkiJSWr3S3OwzbMo5EKkAZ6fqw==";
        communicationConfig.setSignPrivateKey(privateKey);
        communicationConfig.setServerUrl("http://172.29.20.150:8080/serving-service/");

        JSONObject object = new JSONObject();
        object.put("mobile", "13168730657");
        object.put("name", "Zane");
        List<JSONObject> ids = new ArrayList<>();
        ids.add(object);
        JSONObject object1 = new JSONObject();
        object1.put("mobile", "13800000000");
        object1.put("name", "aaaaa");
        ids.add(object1);
        JSONObject object2 = new JSONObject();
        object2.put("mobile", "13800003300");
        object2.put("name", "aaaaa");
        ids.add(object2);
        JSONObject object3 = new JSONObject();
        object3.put("mobile", "13800203300");
        object3.put("name", "bbb");
        ids.add(object3);


        PrivateInformationRetrievalConfig config = new PrivateInformationRetrievalConfig((List) ids, 0, 10, null);

        PrivateInformationRetrievalQuery privateInformationRetrievalQuery = new PrivateInformationRetrievalQuery();
        ExecutorService executorService = new ThreadPoolExecutor(4, 10, 60, TimeUnit.SECONDS, new LinkedBlockingQueue<>(2000));
        for (int i = 0; i < 100_000; i++) {
            executorService.execute(() -> {
                String result = null;
                try {
                    int target = new Random().nextInt(ids.size());
                    config.setTargetIndex(target);
                    result = privateInformationRetrievalQuery.query(config, communicationConfig);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                System.out.println("query result:" + result);
            });
            TimeUnit.SECONDS.sleep(20);
        }
    }
}