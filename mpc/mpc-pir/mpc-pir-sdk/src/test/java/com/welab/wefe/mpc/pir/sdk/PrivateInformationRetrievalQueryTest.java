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
import com.welab.wefe.mpc.pir.sdk.config.CommunicationConfig;
import com.welab.wefe.mpc.pir.sdk.config.PrivateInformationRetrievalConfig;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

/**
 * @Author: eval
 * @Date: 2021-12-29
 **/
public class PrivateInformationRetrievalQueryTest {

    @Test
    public void query() throws Exception {
        CommunicationConfig communicationConfig = new CommunicationConfig();
        communicationConfig.setApiName("api/query/social_score");
        communicationConfig.setNeedSign(false);
        communicationConfig.setServerUrl("http://localhost:8080/serving-service/");

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