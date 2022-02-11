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

package com.welab.wefe.mpc.cache.intermediate;

import com.welab.wefe.mpc.cache.intermediate.impl.LocalIntermediateCache;

/**
 * @Author: eval
 * @Date: 2021-12-23
 **/
public class CacheOperationFactory {
    private static CacheOperation cacheOperation = null;
    private static boolean isInit = false;

    public synchronized static void init(CacheOperation cacheOperation) {
        if (CacheOperationFactory.cacheOperation != null) {
            isInit = true;
            return;
        }
        if (cacheOperation == null) {
            CacheOperationFactory.cacheOperation = new LocalIntermediateCache();
        } else {
            CacheOperationFactory.cacheOperation = cacheOperation;
        }
        isInit = true;
    }

    public static CacheOperation getCacheOperation() {
        if (!isInit) {
            init(null);
        }
        return cacheOperation;
    }
}
