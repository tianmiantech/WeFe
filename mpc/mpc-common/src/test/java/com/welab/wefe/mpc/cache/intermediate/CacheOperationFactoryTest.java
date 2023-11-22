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

import org.junit.Test;

import java.util.concurrent.*;

import static org.junit.Assert.*;

/**
 * @Author: eval
 * @Date: 2021-12-29
 **/
public class CacheOperationFactoryTest {

    @Test
    public void getCacheOperation() throws InterruptedException {
        ExecutorService executorService = new ThreadPoolExecutor(
                5,
                10,
                60,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(100));
        for (int i = 0; i < 106; i++) {
            final int num = i;
            executorService.submit(() -> {
                CacheOperation<String> cacheOperation = CacheOperationFactory.getCacheOperation();
                System.out.println(num + ":" + ",threadId:" + Thread.currentThread().getName() + "," + cacheOperation);
            });
        }

        TimeUnit.SECONDS.sleep(60);
    }
}