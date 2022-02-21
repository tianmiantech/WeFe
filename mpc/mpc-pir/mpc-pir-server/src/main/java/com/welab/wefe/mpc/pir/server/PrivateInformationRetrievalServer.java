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

package com.welab.wefe.mpc.pir.server;

import com.welab.wefe.mpc.cache.CacheInit;
import com.welab.wefe.mpc.cache.intermediate.CacheOperation;
import com.welab.wefe.mpc.pir.server.thread.ProduceHauckTargetThread;

/**
 * @Author: eval
 * @Date: 2021-12-30
 **/
public class PrivateInformationRetrievalServer {

    public static void init(int cacheCount) {
        init(cacheCount, null);
    }

    public static void init(int cacheCount, CacheOperation operation) {
        // 预生成 Huack 对象
        ProduceHauckTargetThread thread = new ProduceHauckTargetThread(cacheCount);
        thread.start();

        CacheInit.init(operation);
    }
}
