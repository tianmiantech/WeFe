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

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.welab.wefe.bo.contract.EventMetaInfo;
import com.welab.wefe.bo.contract.FieldInfo;
import com.welab.wefe.common.util.JObject;
import com.welab.wefe.enums.JavaTypeEnum;
import org.apache.commons.lang3.StringUtils;
import org.fisco.bcos.sdk.abi.wrapper.ABIDefinition;
import org.fisco.bcos.sdk.client.Client;
import org.fisco.bcos.sdk.client.protocol.response.BcosBlock;
import org.fisco.bcos.sdk.crypto.CryptoSuite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Some util
 *
 * @author aaron.li
 **/
public class BlockUtil {
    private static final Logger LOG = LoggerFactory.getLogger(BlockUtil.class);

    public static BcosBlock.Block getBlock(Client client, BigInteger blockHeightNumber) {
        return client.getBlockByNumber(blockHeightNumber, true).getBlock();
    }

    /**
     * Interpret event metadata list based on contract api and name
     */
    public static List<EventMetaInfo> parseToEventMetaInfoList(String abiStr, String contractName) {
        Map<String, List<ABIDefinition>> eventsAbis = AbiUtil.getEventsAbiDefs(abiStr, new CryptoSuite(0));
        List<EventMetaInfo> list = new ArrayList<>();
        for (Map.Entry<String, List<ABIDefinition>> entry : eventsAbis.entrySet()) {
            String eventName = entry.getKey();
            if (CollectionUtil.isEmpty(entry.getValue())) {
                LOG.error("Invalid parsed events, class {} event {} abi ls empty.", contractName, eventName);
                continue;
            }
            if (entry.getValue().size() > 1) {
                LOG.warn("Overload parsed events, class {} event {} abi ls empty.", contractName, eventName);
            }
            ABIDefinition abi = entry.getValue().get(0);
            EventMetaInfo eventMetaInfo = new EventMetaInfo();
            eventMetaInfo.setEventName(eventName);
            eventMetaInfo.setContractName(contractName);
            List<ABIDefinition.NamedType> fields = abi.getInputs();
            List<FieldInfo> fieldList = new ArrayList<>();
            FieldInfo vo = null;
            for (ABIDefinition.NamedType namedType : fields) {
                if (namedType.isIndexed()) {
                    continue;
                }
                vo = new FieldInfo();
                String fieldName = namedType.getName();
                String javaType = SolJavaTypeMappingUtil.fromSolBasicTypeToJavaType(namedType.getType());
                if (StringUtils.isEmpty(fieldName) || StringUtils.isEmpty(javaType)) {
                    continue;
                }
                vo.setSolidityName(namedType.getName());
                vo.setSolidityType(namedType.getType());
                vo.setJavaType(javaType);
                vo.setJavaName(fieldName);
                setSqlAttribute(vo);
                LOG.debug(JObject.create(vo).toJSONString());
                fieldList.add(vo);
            }
            eventMetaInfo.setFieldInfoList(fieldList);
            list.add(eventMetaInfo);
        }
        return list;
    }


    private static FieldInfo setSqlAttribute(FieldInfo vo) {
        String javaType = vo.getJavaType();
        vo.setSqlType(JavaTypeEnum.parse(javaType).getSqlType());
        String sqlName = StrUtil.toUnderlineCase(vo.getJavaName());
        vo.setSqlName(sqlName);
        return vo;
    }
}
