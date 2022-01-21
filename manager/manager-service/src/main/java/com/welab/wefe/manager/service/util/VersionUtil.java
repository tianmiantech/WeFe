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

package com.welab.wefe.manager.service.util;

import org.apache.commons.lang.StringUtils;

/**
 * @author yuxin.zhang
 */
public class VersionUtil {
    public static String generateVersion(String oldVersion) {
        if (StringUtils.isEmpty(oldVersion)) {
            return "v1.0";
        }
        oldVersion = oldVersion.replace("v", "");
        String[] split = oldVersion.split("\\.");
        int one = Integer.valueOf(split[0]);
        int two = Integer.valueOf(split[1]);
        // 1.2.9 -> 1.3.0
        if (two >= 9) {
            one = one + 1;
            two = 0;
        } else {
            two += 1;
        }
        return "v" + one + "." + two;
    }
}
