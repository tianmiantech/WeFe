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
package com.welab.wefe.board.service.test;

/**
 * @author zane
 * @date 2022/3/11
 */
public class LockTest {
    public static void main(String[] args) {
        new Thread(() -> func()).start();
    }

    private synchronized static void func() {
        System.out.println("in to func()" + Thread.currentThread().getName());

        try {
            Thread.sleep(10_000);
            new Thread(() -> func()).start();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
