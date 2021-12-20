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

package com.welab.wefe.mpc.pir.server.thread;

import com.welab.wefe.mpc.MPCConfig;

import java.util.concurrent.*;

public class PrivateInformationRetrievalThreadPool {
    private static PrivateInformationRetrievalThreadPool ourInstance = new PrivateInformationRetrievalThreadPool();

    private ExecutorService mExecutorService;

    public static PrivateInformationRetrievalThreadPool getInstance() {
        return ourInstance;
    }

    private PrivateInformationRetrievalThreadPool() {
        init();
    }

    private void init() {
        ThreadFactory threadFactory = Executors.defaultThreadFactory();
        RejectedExecutionHandler handler = new ThreadPoolExecutor.CallerRunsPolicy();
        mExecutorService = new ThreadPoolExecutor(
                MPCConfig.getCorePoolSize(),
                MPCConfig.getMaxPoolSize(),
                MPCConfig.getKeepAliveTime(),
                TimeUnit.SECONDS,
                new LinkedBlockingQueue(1000),
                threadFactory,
                handler
        );
    }

    public void execute(Runnable command) {
        try {
            mExecutorService.execute(command);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
