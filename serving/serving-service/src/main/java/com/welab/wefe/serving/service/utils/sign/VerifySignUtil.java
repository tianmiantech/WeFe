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
package com.welab.wefe.serving.service.utils.sign;

import com.alibaba.fastjson.JSONObject;
import com.welab.wefe.common.web.api.base.Caller;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * @author hunter.zhao
 * @date 2022/6/27
 */
public class VerifySignUtil {

    private static final Map<String, Class<? extends AbstractVerifySignFunction>> VERIFY_SIGN_FUNCTION_MAP = new HashMap<>();

    static {
        VERIFY_SIGN_FUNCTION_MAP.put(Caller.Member.name(), BoardVerifySignFunction.class);
        VERIFY_SIGN_FUNCTION_MAP.put(Caller.Board.name(), BoardVerifySignFunction.class);
        VERIFY_SIGN_FUNCTION_MAP.put(Caller.Customer.name(), PartnerVerifySignFunction.class);
    }


    private static AbstractVerifySignFunction getFunction(String serviceType) {

        Class<? extends AbstractVerifySignFunction> clazz = VERIFY_SIGN_FUNCTION_MAP.get(serviceType);

        try {
            return clazz.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static void rsaVerify(Caller caller, HttpServletRequest request, JSONObject params) throws Exception {
        AbstractVerifySignFunction signFunction = getFunction(caller.name());
        signFunction.rsaVerify(request, params);
    }
}
