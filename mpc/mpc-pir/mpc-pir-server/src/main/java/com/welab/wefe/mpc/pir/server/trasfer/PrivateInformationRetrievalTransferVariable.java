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

package com.welab.wefe.mpc.pir.server.trasfer;

/**
 * @Author eval
 * @Date 2021/12/15
 **/
public interface PrivateInformationRetrievalTransferVariable {

    /***
     * 发送 PIR 随机数
     * @param key 请求id
     * @param count 次数
     * @param value 数量
     */
    void processHauckRandom(String key, int count, String value);

    /**
     * 发送加密的结果
     *
     * @param key   请求 id
     * @param value 结果字符串
     */
    void processResult(String key, String value);

    /**
     * 获取客户端 PIR 随机数是否符要求
     *
     * @param key
     * @param count
     * @return
     */
    boolean processHauckRandomLegal(String key, int count);

    /**
     * 获取客户端对 PIR 随机数的转换结果
     *
     * @param key
     * @return
     */
    String processClientRandom(String key);

}
