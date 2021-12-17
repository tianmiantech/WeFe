
/*
 * *
 *  * Copyright 2021 Tianmian Tech. All Rights Reserved.
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package com.welab.wefe.mpc.pir.server.cache;

import com.welab.wefe.mpc.pir.protocol.ot.hauck.HauckTarget;
import org.springframework.stereotype.Component;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * @Author eval
 * @Date 2021/12/14
 **/
@Component
public class HauckTargetCache {

    private static HauckTargetCache sHauckTargetCache = new HauckTargetCache();

    private static BlockingQueue<HauckTarget> sHauckTargetCaches = new ArrayBlockingQueue<>(500);

    private HauckTargetCache() {
    }

    public static HauckTargetCache getInstance(){
        return sHauckTargetCache;
    }

    public boolean put(HauckTarget hauckTarget) {
        return sHauckTargetCaches.offer(hauckTarget);
    }

    public HauckTarget get() {
        return sHauckTargetCaches.poll();
    }

    public int size(){
        return sHauckTargetCaches.size();
    }
}
