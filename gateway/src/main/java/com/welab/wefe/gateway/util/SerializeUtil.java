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

package com.welab.wefe.gateway.util;

import com.google.protobuf.MessageLite;
import com.welab.wefe.gateway.api.meta.basic.GatewayMetaProto;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Serialization and deserialization tool classes
 *
 * @author aaron.li
 **/
public class SerializeUtil {

    /**
     * Serialize object to file
     *
     * @param object   Serialize object
     * @param filePath The full file path of the serialized object
     */
    public static <T extends MessageLite> void serialize(T object, String filePath) throws IOException {
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(filePath);
            object.writeTo(fos);
            fos.flush();
        } finally {
            if (null != fos) {
                fos.close();
            }
        }
    }

    /**
     * The full path of the specified file is deserialization into an object
     *
     * @param filePath The full path of the specified file
     * @return Deserialized object
     */
    public static GatewayMetaProto.TransferMeta deserializationTransferMeta(String filePath) throws IOException {
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(filePath);
            return GatewayMetaProto.TransferMeta.parseFrom(fis);
        } finally {
            if (null != fis) {
                fis.close();
            }
        }
    }

    /**
     * Generate the file name after object persistence
     */
    public static String generatePersistentId(GatewayMetaProto.TransferMeta transferMeta) {
        return transferMeta.getSrc().getMemberId() + "_" + transferMeta.getDst().getMemberId() + "_" + DigestUtils.md5Hex(transferMeta.getSessionId());
    }
}
