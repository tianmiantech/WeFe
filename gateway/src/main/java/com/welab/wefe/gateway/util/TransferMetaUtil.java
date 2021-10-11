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

package com.welab.wefe.gateway.util;

import com.welab.wefe.common.util.JObject;
import com.welab.wefe.common.util.StringUtil;
import com.welab.wefe.gateway.api.meta.basic.GatewayMetaProto;
import com.welab.wefe.gateway.common.StorageConstant;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * @author aaron.li
 **/
public class TransferMetaUtil {

    /**
     * Get database name from message
     */
    public static String getDbName(GatewayMetaProto.TransferMeta transferMeta) {
        JObject dbConfig = JObject.create(transferMeta.getContent().getObjectData());
        return dbConfig.getString(StorageConstant.NAMESPACE_KEY);
    }

    /**
     * Get table name from message
     */
    public static String getTableName(GatewayMetaProto.TransferMeta transferMeta) {
        JObject dbConfig = JObject.create(transferMeta.getContent().getObjectData());
        return dbConfig.getString(StorageConstant.NAME_KEY);
    }

    public static String getDstDbName(GatewayMetaProto.TransferMeta transferMeta) {
        JObject dbConfig = JObject.create(transferMeta.getContent().getObjectData());
        String dstDbName = dbConfig.getString(StorageConstant.DST_NAMESPACE_KEY);
        return StringUtil.isNotEmpty(dstDbName) ? dstDbName : getDbName(transferMeta);
    }

    public static String getDstTableName(GatewayMetaProto.TransferMeta transferMeta) {
        JObject dbConfig = JObject.create(transferMeta.getContent().getObjectData());
        String dstTableName = dbConfig.getString(StorageConstant.DST_NAME_KEY);
        return StringUtil.isNotEmpty(dstTableName) ? dstTableName : getTableName(transferMeta);
    }

    public static String getFCNamespace(GatewayMetaProto.TransferMeta transferMeta) {
        JObject dbConfig = JObject.create(transferMeta.getContent().getObjectData());
        return dbConfig.getString(StorageConstant.FC_NAMESPACE);
    }

    public static String getFCName(GatewayMetaProto.TransferMeta transferMeta) {
        JObject dbConfig = JObject.create(transferMeta.getContent().getObjectData());
        return dbConfig.getString(StorageConstant.FC_NAME);
    }

    /**
     * Get the number of slices
     */
    public static Integer getPartitions(GatewayMetaProto.TransferMeta transferMeta) {
        JObject dbConfig = JObject.create(transferMeta.getContent().getObjectData());
        String partitions = dbConfig.getString(StorageConstant.PARTITIONS);
        return Integer.parseInt(partitions);
    }

    /**
     * Get the number of slices
     */
    public static Integer getFCPartitions(GatewayMetaProto.TransferMeta transferMeta) {
        JObject dbConfig = JObject.create(transferMeta.getContent().getObjectData());
        String partitions = dbConfig.getString(StorageConstant.FC_PARTITIONS);
        return Integer.parseInt(partitions);
    }

    /**
     * Convert to log message format
     */
    public static String toMessageString(GatewayMetaProto.TransferMeta transferMeta) {
        GatewayMetaProto.Member src = transferMeta.getSrc();
        GatewayMetaProto.Member dst = transferMeta.getDst();

        StringBuilder messageStrSb = new StringBuilder();
        messageStrSb.append("session_id:[")
                .append(transferMeta.getSessionId())
                .append("],")
                .append("action:[")
                .append(transferMeta.getAction())
                .append("],")
                .append("src:[")
                .append(src.getMemberId())
                .append(":")
                .append(src.getMemberName())
                .append("]");

        if (null != dst) {
            messageStrSb.append(",dst:[")
                    .append(dst.getMemberId())
                    .append(":")
                    .append(dst.getMemberName())
                    .append("]");
        }

        return messageStrSb.toString();
    }

    /**
     * Gets the size of metadata in KB
     */
    public static int getKbSize(GatewayMetaProto.TransferMeta transferMeta) {
        if (null == transferMeta) {
            return 0;
        }
        int bSize = transferMeta.toByteArray().length;
        return BigDecimal.valueOf(bSize)
                .divide(BigDecimal.valueOf(1024), 0, RoundingMode.HALF_UP)
                .intValue();
    }

    /**
     * Generate traffic log
     */
    public static String toFlowLogJSONString(GatewayMetaProto.TransferMeta transferMeta) {
        int kbSize = getKbSize(transferMeta);
        String memberId = transferMeta.getSrc().getMemberId();

        JObject logObj = JObject.create()
                .append("sizeKB", kbSize)
                .append("memberId", memberId);

        return logObj.toString();
    }
}
