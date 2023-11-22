/*
 * Copyright 2021 Tianmian Tech. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.welab.wefe.util;

import com.welab.wefe.bo.contract.ContractContextInfo;
import com.welab.wefe.bo.contract.ContractInfo;
import com.welab.wefe.common.util.StringUtil;
import com.welab.wefe.constant.ContractConstants;
import org.fisco.bcos.sdk.client.protocol.model.JsonTransactionResponse;
import org.fisco.bcos.sdk.transaction.model.exception.ContractException;

/**
 * @author aaron.li
 **/
public class TransactionUtil {

    public static ContractInfo getContractInfoByTransaction(JsonTransactionResponse transaction) throws ContractException {
        String contractAddress = transaction.getTo();
        if (StringUtil.isEmpty(contractAddress) || ContractConstants.EMPTY_ADDRESS.equals(contractAddress)) {
            return null;
        }

        return ContractContextInfo.getContractInfoByContractName(contractAddress);
    }
}
