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
package com.welab.wefe.common;

import com.welab.wefe.common.constant.SecretKeyType;
import com.welab.wefe.common.util.SignUtil;
import com.welab.wefe.common.util.SignUtil.KeyPair;
import java.nio.charset.StandardCharsets;

/**
 * @author jensen
 * @date 2022/12/7
 */
public class SM2SignTest {
    public static void main(String[] args) throws Exception {

        KeyPair keyPair = SignUtil.generateKeyPair(SecretKeyType.sm2);
        System.out.println(keyPair.publicKey);
        System.out.println(keyPair.privateKey);

        String data = "abc123";
        String sign = SignUtil.sign(data, keyPair.privateKey, SecretKeyType.sm2);
        System.out.println(String.format("sign:%s", sign));
        boolean result = SignUtil.verify(data.getBytes(StandardCharsets.UTF_8), keyPair.publicKey, sign,
                SecretKeyType.sm2);
        System.out.println(result);

    }
}
