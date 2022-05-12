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

import com.welab.wefe.common.util.Base64Util;

import java.security.SecureRandom;
import java.util.Random;

/**
 * 安全相关的常用方法
 *
 * @author zane
 * @date 2022/5/11
 */
public class SecurityUtil {
    /**
     * 生成随机盐
     */
    public static String createRandomSalt() {
        final Random r = new SecureRandom();
        byte[] salt = new byte[16];
        r.nextBytes(salt);

        return Base64Util.encode(salt);
    }

    public static void main(String[] args) {
        for (int i = 0; i < 10; i++) {
            System.out.println(createRandomSalt());
        }
    }
}
