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

package com.welab.wefe.mpc.psi.sdk.operation.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.welab.wefe.mpc.psi.sdk.operation.ListOperator;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author: eval
 * @Date: 2022-01-04
 **/
public class IntersectionOperator implements ListOperator<String, String> {

    @Override
    public List<String> operator(List<String> originIds, List<String> encryptOriginIds, List<String> encryptServerIds) {
        if (CollectionUtil.isEmpty(originIds) || CollectionUtil.isEmpty(encryptOriginIds) || CollectionUtil.isEmpty(encryptServerIds)) {
            return new ArrayList<>();
        }
        List<String> result =
                encryptOriginIds.stream().filter(item -> encryptServerIds.contains(item))
                        .map(item -> originIds.get(encryptOriginIds.indexOf(item)))
                        .collect(Collectors.toList());
        return result;
    }
}
