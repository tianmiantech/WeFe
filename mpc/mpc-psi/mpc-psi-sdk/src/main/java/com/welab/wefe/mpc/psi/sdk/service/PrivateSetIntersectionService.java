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

package com.welab.wefe.mpc.psi.sdk.service;

import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.welab.wefe.mpc.psi.request.QueryPrivateSetIntersectionRequest;
import com.welab.wefe.mpc.psi.request.QueryPrivateSetIntersectionResponse;

/**
 * @Author: eval
 * @Date: 2021-12-24
 **/
public class PrivateSetIntersectionService {

    public static QueryPrivateSetIntersectionResponse handle(String url, QueryPrivateSetIntersectionRequest request) {
        try {
            String res = HttpUtil.post(url, JSONObject.toJSONString(request));
            return JSON.parseObject(res, QueryPrivateSetIntersectionResponse.class);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
