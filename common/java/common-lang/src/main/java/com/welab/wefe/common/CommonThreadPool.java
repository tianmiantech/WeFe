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

import java.util.concurrent.*;

/**
 * Common thread pool
 *
 * @author Zane
 */
public class CommonThreadPool {
    private static ThreadPoolExecutor THREAD_POOL;

    public static boolean TASK_SWITCH = true;

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

    public static void stop() {
//        THREAD_POOL.shutdownNow();
        TASK_SWITCH = false;
//        THREAD_POOL = new ThreadPoolExecutor(
//                50,
////                Runtime.getRuntime().availableProcessors(),
//                60,
////                8 * 16,
//                100L,
//                TimeUnit.MILLISECONDS,
//                new LinkedBlockingQueue<>());
    }


    public static <T> Future<T> submit(Callable<T> someThing) {
        return THREAD_POOL.submit(someThing);
    }

    /**
     * Add an asynchronous task, and the accounting amount will be reduced after the task is executed
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
