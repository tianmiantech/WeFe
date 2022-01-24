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

package com.welab.wefe.serving.sdk.utils;

import java.util.concurrent.*;

/**
 * @author hunter.zhao
 */
public class AlgorithmThreadPool {
    private static ThreadPoolExecutor THREAD_POOL;

    static {

        THREAD_POOL = new ThreadPoolExecutor(
                Runtime.getRuntime().availableProcessors(),
                Runtime.getRuntime().availableProcessors() * 2,
                100L,
                TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>());
    }

    public static void run(Runnable someThing) {
        THREAD_POOL.execute(someThing);
    }

    public static <T> Future<T> submit(Callable<T> someThing) {
        return THREAD_POOL.submit(someThing);
    }

    /**
     * Add an asynchronous task. After the task is executed, the accounting number decreases
     */
    public static void run(Runnable someThing, CountDownLatch countDownLatch) {
        THREAD_POOL.execute(() -> {
            try {
                someThing.run();
            } finally {
                countDownLatch.countDown();
            }
        });
    }

    public static int actionThreadCount() {
        return THREAD_POOL.getActiveCount();
    }
}
