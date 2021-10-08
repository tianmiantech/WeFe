/**
 * Copyright 2021 The WeFe Authors. All Rights Reserved.
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

package com.welab.wefe.gateway.cache;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

/**
 * Synchronous blocking counter cache for sending and receiving metadata
 *
 * @author aaron.li
 **/
public class RecvTransferMetaCountDownLatchCache {

    private static RecvTransferMetaCountDownLatchCache recvTransferMetaCountDownLatchCache = new RecvTransferMetaCountDownLatchCache();

    private static ConcurrentHashMap<String, CountDownLatch> dataMap = new ConcurrentHashMap<>();

    private RecvTransferMetaCountDownLatchCache() {
    }

    public static RecvTransferMetaCountDownLatchCache getInstance() {
        return recvTransferMetaCountDownLatchCache;
    }

    /**
     * Remove latch
     *
     * @param key Latch key
     * @return Deleted latch
     */
    public CountDownLatch removeCountDownLatch(String key) {
        return dataMap.remove(key);
    }


    /**
     * Close the latch
     *
     * @param key Latch key
     * @return Latch corresponding to key
     */
    public synchronized CountDownLatch closeCountDownLatch(String key) {
        CountDownLatch countDownLatch = dataMap.get(key);
        if (null == countDownLatch) {
            countDownLatch = new CountDownLatch(1);
            dataMap.put(key, countDownLatch);
        }
        return countDownLatch;
    }

    /**
     * Open the latch
     *
     * @param key Latch key
     */
    public void openCountDownLatch(String key) {
        closeCountDownLatch(key).countDown();
    }
}
