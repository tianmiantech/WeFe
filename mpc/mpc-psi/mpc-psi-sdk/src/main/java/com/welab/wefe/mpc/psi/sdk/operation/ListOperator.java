/*
 * Copyright 2022 Tianmian Tech. All Rights Reserved.
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

package com.welab.wefe.mpc.psi.sdk.operation;

import java.util.List;

/**
 * @Author: eval
 * @Date: 2022-01-04
 **/
public interface ListOperator<K, T> {
    /**
     * 返回两个列表的计算结果
     *
     * @param originIds        原始id列表
     * @param encryptOriginIds 加密原始id列表，大小与原始id列表相同
     * @param encryptServerIds 加密服务器id列表
     * @return
     */
    List<K> operator(List<T> originIds, List<T> encryptOriginIds, List<T> encryptServerIds);
}
