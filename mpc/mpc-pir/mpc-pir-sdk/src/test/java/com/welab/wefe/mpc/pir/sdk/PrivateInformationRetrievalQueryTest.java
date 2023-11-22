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

/**
 * @Author: eval
 * @Date: 2021-12-29
 **/
public class PrivateInformationRetrievalQueryTest {

    @Test
    public void query() throws Exception {
        CommunicationConfig communicationConfig = new CommunicationConfig();
        communicationConfig.setApiName("api/query/social_score"); // API name
        communicationConfig.setNeedSign(true); // 是否需要签名
        communicationConfig.setCommercialId("tianmain"); // 商户id
        String privateKey = "xxxx";
        communicationConfig.setSignPrivateKey(privateKey);// 签名RSA私钥
        communicationConfig.setServerUrl("http://xxxx.com/serving-service-01/"); // url

        // 混淆查询的用户
        List<JSONObject> ids = new ArrayList<>();
        JSONObject object1 = new JSONObject();
        object1.put("mobile", "18032642070");
        object1.put("name", "李四");
        ids.add(object1);
        JSONObject object2 = new JSONObject();
        object2.put("mobile", "18132609320");
        object2.put("name", "王五");
        ids.add(object2);
        JSONObject object3 = new JSONObject();
        object3.put("mobile", "13132840320");
        object3.put("name", "赵四");
        ids.add(object3);
        System.out.println("ids= " + JSONObject.toJSONString(ids));

        // 实际要查询的用户
        JSONObject object = new JSONObject();
        object.put("mobile", "13032419870");
        object.put("name", "张三");
        int targetIndex = new Random().nextInt(3); //把真实用户放到混淆集中的位置
        ids.add(targetIndex, object);

        PrivateInformationRetrievalConfig config = new PrivateInformationRetrievalConfig((List) ids, targetIndex, 10, null);
        PrivateInformationRetrievalQuery privateInformationRetrievalQuery = new PrivateInformationRetrievalQuery();
        String result = privateInformationRetrievalQuery.query(config, communicationConfig);
        System.out.println("index = " + targetIndex);
        System.out.println("query result:" + result);
    }
}