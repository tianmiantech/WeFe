/*
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

package com.welab.wefe.serving.sdk.config;

import com.welab.wefe.common.constant.SecretKeyType;

/**
 * Global cache
 * It stores invariable or less variable data in the system to reduce database query and coding complexity.
 * <p>
 * Cache list：
 * - member info
 *
 * @author hunter.zhao
 */
public class Config {

    public static String MEMBER_ID;
    public static String RSA_PRIVATE_KEY;
    public static String RSA_PUBLIC_KEY;
    public static SecretKeyType SECRET_KEY_TYPE = SecretKeyType.rsa;
}
