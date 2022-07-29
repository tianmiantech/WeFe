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
package com.welab.wefe.serving.service.utils;

import com.alibaba.fastjson.JSONObject;
import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.util.RSAUtil;
import com.welab.wefe.serving.service.service.CacheObjects;

import java.util.TreeMap;

/**
 * @author hunter.zhao
 * @date 2022/5/17
 */
public class SignUtils {

    /**
     * Set request body(single)
     */
    public static String parameterSign(TreeMap<String, Object> params) throws StatusCodeWithException {
        /**
         * Prevent map disorder, resulting in signature verification failure
         */
        String data = new JSONObject(params).toJSONString();

        /**
         * sign
         */
        String sign;
        try {
            sign = RSAUtil.sign(data, CacheObjects.getRsaPrivateKey());
        } catch (Exception e) {
            e.printStackTrace();
            throw new StatusCodeWithException(e.getMessage(), StatusCode.SYSTEM_ERROR);
        }

        JSONObject body = new JSONObject();
        body.put("memberId", CacheObjects.getMemberId());
        body.put("sign", sign);
        body.put("data", data);

        return body.toJSONString();
    }
}
