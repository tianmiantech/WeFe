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

package com.welab.wefe.mpc.cache;

import com.welab.wefe.mpc.cache.intermediate.CacheOperation;
import com.welab.wefe.mpc.cache.intermediate.CacheOperationFactory;
import com.welab.wefe.mpc.cache.result.QueryDataResult;
import com.welab.wefe.mpc.cache.result.QueryDataResultFactory;

/**
 * @Author: eval
 * @Date: 2021-12-30
 **/
public class CacheInit {

    public static void init(QueryDataResult queryDataResult) {
        init(null, queryDataResult);
    }

    public static void init(CacheOperation operation) {
        init(operation, null);
    }

    public static void init(CacheOperation operation, QueryDataResult queryDataResult) {
        // 同一个请求，中间数据缓存操作实现
        CacheOperationFactory.init(operation);

        // 请求查询结果数据的缓存实现,
        QueryDataResultFactory.init(queryDataResult);
    }
}
