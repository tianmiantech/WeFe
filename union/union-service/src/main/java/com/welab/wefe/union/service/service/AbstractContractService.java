/**
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

package com.welab.wefe.union.service.service;

import com.alibaba.fastjson.JSONArray;
import com.welab.wefe.common.util.JObject;

/**
 * @author yuxin.zhang
 */
public class AbstractContractService {

    /**
     * Whether the transaction was executed successfully
     *
     * @param responseValues Receipt response result
     * @return true: the transaction was successful; false: the transaction failed
     */
    protected boolean transactionIsSuccess(String responseValues) {
        JSONArray values = JObject.parseArray(responseValues);
        if (null == values || values.isEmpty() || values.getIntValue(0) < 0) {
            return false;
        }
        return true;
    }

    protected boolean transactionException(String responseValues) {
        JSONArray values = JObject.parseArray(responseValues);
        return null == values || values.isEmpty();
    }

    /**
     * Data not found
     *
     * @param responseValues Receipt response result
     * @return true：the transaction was successful; false: the transaction failed
     */
    protected boolean transactionDataNotFound(String responseValues) {
        JSONArray values = JObject.parseArray(responseValues);
        return !transactionException(responseValues) && (values.getIntValue(0) < 0);
    }

    /**
     * Data already exists
     */
    protected boolean transactionDataIsExist(String responseValues) {
        JSONArray values = JObject.parseArray(responseValues);
        return !transactionException(responseValues) && (values.getIntValue(0) == -1);
    }

    protected boolean transactionInsertFail(String responseValues) {
        JSONArray values = JObject.parseArray(responseValues);
        return !transactionException(responseValues) && (values.getIntValue(0) == -2);
    }
}
