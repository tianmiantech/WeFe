/**
 * Copyright 2021 The WeFe Authors. All Rights Reserved.
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

package com.welab.wefe.gateway.service;

import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.util.FileUtil;
import com.welab.wefe.gateway.api.meta.basic.GatewayMetaProto;
import com.welab.wefe.gateway.config.ConfigProperties;
import com.welab.wefe.gateway.service.base.AbstractRecvTransferMetaCachePersistentService;
import com.welab.wefe.gateway.util.SerializeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Local file system persistent metadata service
 *
 * @author aaron.li
 **/
@ConditionalOnExpression("#{T(com.welab.wefe.gateway.common.TransferMetaCachePersistentTypeEnum).LOCAL_FILE_SYS.getType().equals(environment.getProperty('recv.transfer.meta.persistent.type', T(com.welab.wefe.gateway.common.TransferMetaCachePersistentTypeEnum).LOCAL_FILE_SYS.getType()))}")
@Service
public class RecvTransferMetaCachePersistentService extends AbstractRecvTransferMetaCachePersistentService {
    private final Logger LOG = LoggerFactory.getLogger(RecvTransferMetaCachePersistentService.class);

    @Autowired
    private ConfigProperties mConfigProperties;

    @Override
    public StatusCodeWithException save(GatewayMetaProto.TransferMeta transferMeta) {
        try {
            SerializeUtil.serialize(transferMeta, getPersistentFilePath(transferMeta));
            return new StatusCodeWithException(StatusCode.SUCCESS);
        } catch (Exception e) {
            LOG.error("received transfer meta file system persistent fail：", e);
            return new StatusCodeWithException("保存接收到的消息到本地文本系统失败, 请确保程序有自动创建目录的权限：" + e.getMessage(), StatusCode.SYSTEM_ERROR);
        }
    }

    @Override
    public GatewayMetaProto.TransferMeta get(GatewayMetaProto.TransferMeta transferMeta) {
        try {
            return SerializeUtil.deserializationTransferMeta(getPersistentFilePath(transferMeta));
        } catch (Exception e) {
            LOG.error("received transfer meta file system persistent from deserialization fail：", e);
            return transferMeta;
        }
    }

    @Override
    public boolean delete(GatewayMetaProto.TransferMeta transferMeta) {
        return new File(getPersistentFilePath(transferMeta)).delete();
    }

    @Override
    public List<GatewayMetaProto.TransferMeta> findAll() {
        List<GatewayMetaProto.TransferMeta> transferMetaJobList = new ArrayList<>();
        try {
            File persistentDirPathFile = new File(getBasePersistentDir());
            File[] fileList = persistentDirPathFile.listFiles();
            if (null != fileList) {
                for (File file : fileList) {
                    if (file.isDirectory()) {
                        continue;
                    }
                    transferMetaJobList.add(SerializeUtil.deserializationTransferMeta(file.getAbsolutePath()));
                }
            }
        } catch (Exception e) {
            LOG.error("deserialization received transfer meta data from file system fail：", e);
        }
        return transferMetaJobList;
    }


    /**
     * Get persistent file completion path
     */
    private String getPersistentFilePath(GatewayMetaProto.TransferMeta transferMeta) {
        return getBasePersistentDir() + SerializeUtil.generatePersistentId(transferMeta);
    }

    /**
     * Get persistent base directory
     */
    private String getBasePersistentDir() {
        String dirPath = mConfigProperties.getRecvTransferMetaPersistentTempDir();
        dirPath = ((dirPath.endsWith("\\") || dirPath.endsWith("/")) ? dirPath : dirPath + File.separator);
        FileUtil.createDir(dirPath);
        return dirPath;
    }
}
