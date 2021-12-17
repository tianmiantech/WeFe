
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

package com.welab.wefe.mpc;

import java.lang.reflect.Field;

/**
 * @Author eval
 * @Date 2021/12/14
 **/
public class MPCConfig {
    private static int corePoolSize = 10;
    private static int maxPoolSize = 500;
    private static int keepAliveTime = 30;

    public static int getCorePoolSize() {
        return corePoolSize;
    }

    public static int getMaxPoolSize() {
        return maxPoolSize;
    }

    public static int getKeepAliveTime() {
        return keepAliveTime;
    }

    public static boolean setConfig(String name, Object value) {
        try {
            Field field = MPCConfig.class.getDeclaredField(name);
            field.setAccessible(true);
            field.set(MPCConfig.class, value);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return true;
    }

}
