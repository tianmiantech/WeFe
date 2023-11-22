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

package com.welab.wefe.bo.contract;

import com.welab.wefe.constant.BinConstant;
import com.welab.wefe.constant.SyncConstant;
import org.apache.commons.lang3.StringUtils;
import org.fisco.bcos.sdk.contract.precompiled.cns.CnsInfo;
import org.fisco.bcos.sdk.transaction.model.exception.ContractException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Contract context information
 *
 * @author aaron.li
 **/
public class ContractContextInfo {

    /**
     * Contract binary information Map (Key: contract binary, Value: contract information)
     */
    public static Map<String, ContractInfo> CONTRACT_BINARY_MAP = new HashMap<>(16);
    /**
     * Contract name and its corresponding contract information Map (Key: contract name, Value: contract information)
     */
    public static Map<String, ContractInfo> CONTRACT_INFO_MAP = new HashMap<>(16);


    /**
     * Query the contract information according to the contract address code
     *
     * @param code Contract address code
     * @return Contract information
     */
    public static ContractInfo getContractInfoByCode(String code) {
        for (Map.Entry<String, ContractInfo> entry : CONTRACT_BINARY_MAP.entrySet()) {
            String key = entry.getKey();
            ContractInfo contractInfo = entry.getValue();

            if (code.length() > BinConstant.META_DATA_HASH_LENGTH
                    && key.length() > BinConstant.META_DATA_HASH_LENGTH) {
                String hashLengthStr = code.substring(code.length() - 4);
                if ("0029".equals(hashLengthStr)) {
                    code = code.substring(2, code.length() - 86);
                }
                if ("0037".equals(hashLengthStr)) {
                    code = code.substring(2, code.length() - 114);
                }
                if (code.contains("a264697066735822")) {
                    code = code.substring(2, code.indexOf("a264697066735822"));
                }

            } else {
                continue;
            }

            if (StringUtils.containsIgnoreCase(key, code)) {
                return contractInfo;
            }
        }
        return null;
    }


    /**
     * Query the contract information according to the contract name
     *
     * @param contractAddress Contract address
     * @return Contract information
     */
    public static ContractInfo getContractInfoByContractName(String contractAddress) throws ContractException {
        for (String key : CONTRACT_INFO_MAP.keySet()) {
            List<CnsInfo> list = SyncConstant.getCurrentContext().getCnsService().selectByName(key);
            boolean isOk = list.stream().map(CnsInfo::getAddress).collect(Collectors.toList()).contains(contractAddress);
            if (isOk) {
                return CONTRACT_INFO_MAP.get(key);
            }
        }
        return null;
    }

}
