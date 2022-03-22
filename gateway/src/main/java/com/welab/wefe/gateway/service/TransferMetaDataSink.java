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

package com.welab.wefe.gateway.service;

import com.welab.wefe.common.util.FileUtil;
import com.welab.wefe.common.util.StringUtil;
import com.welab.wefe.common.util.ThreadUtil;
import com.welab.wefe.gateway.api.meta.basic.GatewayMetaProto;
import com.welab.wefe.gateway.config.ConfigProperties;
import com.welab.wefe.gateway.service.base.AbstractTransferMetaDataSink;
import com.welab.wefe.gateway.util.SerializeUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * @author aaron.li
 **/
@Service
public class TransferMetaDataSink extends AbstractTransferMetaDataSink {
    private final Logger LOG = LoggerFactory.getLogger(TransferMetaDataSink.class);

    private static final String SINK_TEMP_DIR_NAME = "sink";

    /**
     * Processing status: processing
     */
    public final static int PROCESS_STATUS_ING = 0;
    /**
     * Processing status: success
     */
    public final static int PROCESS_STATUS_SUCCESS = 1;
    /**
     * Processing status: fail
     */
    public final static int PROCESS_STATUS_FAIL = 2;

    /**
     * Metadata list in process（KEY：sessionId；VALUE：Serialization path of metadata）
     */
    private static ConcurrentHashMap<String, ConcurrentSkipListSet<ProcessingTransferMetaData>> processingTransferMetaDataCache = new ConcurrentHashMap<>();
    /**
     * Commit completion ID cache（KEY：sessionId; complete Block）
     */
    private static ConcurrentHashMap<String, GatewayMetaProto.TransferMeta> completeBlockFlagCache = new ConcurrentHashMap<>();


    @Autowired
    private ConfigProperties configProperties;


    @Autowired
    private TransferMetaDataAsyncSaveService transferMetaDataAsyncSaveService;

    @Autowired
    private MessageService messageService;

    @Autowired
    private void startProcessTransferMetaCacheThread() {
        // Load serialized data
        if (!loadSerializeTransferMetaData()) {
            LOG.error("load data sink serialize transferMeta data fail, system exit");
            System.exit(-1);
        }

        // Start to check whether the metadata cache has been received thread
        new CheckTransferMetaDataCacheCompleteThread().start();
    }


    @Override
    public void sink(GatewayMetaProto.TransferMeta transferMeta) throws Exception {
        // Temporary storage
        tempSaveTransferMetaData(transferMeta);
    }


    /**
     * check whether the metadata cache has been received thread
     */
    private class CheckTransferMetaDataCacheCompleteThread extends Thread {
        @Override
        public void run() {
            while (true) {
                try {
                    // List of completed session ID
                    List<GatewayMetaProto.TransferMeta> completeTransferMetaList = new ArrayList<>();
                    for (Map.Entry<String, GatewayMetaProto.TransferMeta> entry : completeBlockFlagCache.entrySet()) {
                        String sessionId = entry.getKey();
                        GatewayMetaProto.TransferMeta transferMeta = entry.getValue();
                        // All fragment data under the session ID
                        ConcurrentSkipListSet<ProcessingTransferMetaData> processingTransferMetaDataList = processingTransferMetaDataCache.get(sessionId);

                        // Has all processing been completed
                        boolean isTotalProcessComplete = true;
                        boolean isExistProcessFail = false;
                        if (CollectionUtils.isNotEmpty(processingTransferMetaDataList)) {
                            for (ProcessingTransferMetaData processingTransferMetaData : processingTransferMetaDataList) {
                                if (PROCESS_STATUS_ING == processingTransferMetaData.status) {
                                    isTotalProcessComplete = false;
                                    break;
                                }
                                isExistProcessFail = (PROCESS_STATUS_FAIL == processingTransferMetaData.status || isExistProcessFail);
                            }
                        }

                        // Unprocessed completion
                        if (!isTotalProcessComplete) {
                            continue;
                        }

                        if (isExistProcessFail) {
                            transferMeta = transferMeta.toBuilder().setTransferStatus(GatewayMetaProto.TransferStatus.ERROR).build();
                        }

                        // Notify the client that data warehousing has been completed
                        updateCache(transferMeta);
                        // Add to completed list
                        completeTransferMetaList.add(transferMeta);
                    }
                    // Delete processed cached data
                    completeTransferMetaList.forEach(TransferMetaDataSink.this::clearTransferMetaDataCache);
                } catch (Exception e) {
                    LOG.error("Check whether the metadata cache has been received thread exception：", e);
                }
                ThreadUtil.sleep(100);
            }
        }
    }


    /**
     * Load serialization metadata
     */
    private boolean loadSerializeTransferMetaData() {
        File sinkBaseDirFile = new File(sinkTransferMetaDataBaseDir());
        File[] dirList = sinkBaseDirFile.listFiles();
        if (null == dirList) {
            return true;
        }
        for (File dir : dirList) {
            if (!dir.isDirectory()) {
                continue;
            }

            File[] transferMetaDataFileList = dir.listFiles();
            if (null == transferMetaDataFileList || transferMetaDataFileList.length == 0) {
                continue;
            }
            String sessionId = null;
            try {
                sessionId = SerializeUtil.deserializationTransferMeta(transferMetaDataFileList[0].getAbsolutePath()).getSessionId();
            } catch (Exception e) {
                LOG.error("load data sink serialize transferMeta data fail: ", e);
                continue;
            }

            String completeFilePath = null;
            for (File transferMetaDataFile : transferMetaDataFileList) {
                String filePath = transferMetaDataFile.getAbsolutePath();
                // Submit completion identification block
                if ("complete".equalsIgnoreCase(transferMetaDataFile.getName())) {
                    completeFilePath = filePath;
                } else {
                    // Data files: add to cache and asynchronously store
                    putProcessingTransferMetaDataCache(sessionId, filePath);
                }
            }

            // The end tag is added to the cache at the end to prevent the timer from misjudging that the data has been warehoused
            if (StringUtil.isNotEmpty(completeFilePath)) {
                try {
                    GatewayMetaProto.TransferMeta transferMeta = SerializeUtil.deserializationTransferMeta(completeFilePath);
                    completeBlockFlagCache.put(transferMeta.getSessionId(), transferMeta);
                } catch (Exception e) {
                    messageService.saveError("加载序列化元数据数据失败", "反序列化元数据文件失败，路径：" + completeFilePath + ", 异常：" + e.getMessage());
                    LOG.error("load data sink serialize transferMeta data fail: ", e);
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * Delete all metadata caches and serialization files
     */
    private void clearTransferMetaDataCache(GatewayMetaProto.TransferMeta transferMeta) {
        completeBlockFlagCache.remove(transferMeta.getSessionId());
        processingTransferMetaDataCache.remove(transferMeta.getSessionId());
        FileUtil.deleteFileOrDir(sinkTransferMetaDataBaseDir() + File.separator + SerializeUtil.generatePersistentId(transferMeta));
    }


    private boolean tempSaveTransferMetaData(GatewayMetaProto.TransferMeta transferMeta) {
        String saveBaseDir = sinkTransferMetaDataBaseDir() + File.separator + SerializeUtil.generatePersistentId(transferMeta);
        try {
            FileUtil.createDir(saveBaseDir);
            String filePath = saveBaseDir + File.separator;
            // Completion identification block submitted by the client (i.e. the last block)
            if (transferMeta.getTransferStatus().equals(GatewayMetaProto.TransferStatus.COMPLETE)) {
                filePath += "complete";
                completeBlockFlagCache.put(transferMeta.getSessionId(), transferMeta);
                SerializeUtil.serialize(transferMeta, filePath);
            } else {
                //Business data block
                filePath += transferMeta.getSequenceNo();
                SerializeUtil.serialize(transferMeta, filePath);

                // Add to cache and save database asynchronously
                putProcessingTransferMetaDataCache(transferMeta.getSessionId(), filePath);
            }
        } catch (Exception e) {
            LOG.error("Temp save transfer meta data fail: ", e);
            messageService.saveError("临时保存元数据失败", e.getMessage(), transferMeta);
            return false;
        }
        return true;
    }


    /**
     * Add data to cache and process
     */
    private synchronized void putProcessingTransferMetaDataCache(String sessionId, String serializePath) {
        ProcessingTransferMetaData processingTransferMetaData = new ProcessingTransferMetaData();
        processingTransferMetaData.serializePath = serializePath;
        // Add to cache
        ConcurrentSkipListSet<ProcessingTransferMetaData> dataSet = processingTransferMetaDataCache.get(sessionId);
        dataSet = (null == dataSet ? new ConcurrentSkipListSet() : dataSet);
        dataSet.add(processingTransferMetaData);
        processingTransferMetaDataCache.put(sessionId, dataSet);
        // Save to database asynchronously
        transferMetaDataAsyncSaveService.save(sessionId, processingTransferMetaData);
    }


    /**
     * Base directory for saving metadata
     */
    private String sinkTransferMetaDataBaseDir() {
        String sinkBaseDir = configProperties.getRecvTransferMetaPersistentTempDir();
        return ((sinkBaseDir.endsWith("\\") || sinkBaseDir.endsWith("/")) ? sinkBaseDir : sinkBaseDir + File.separator) + SINK_TEMP_DIR_NAME;
    }

    /**
     * Metadata structure in processing
     */
    public static class ProcessingTransferMetaData implements Comparable {
        /**
         * Serialized full path of metadata entity
         */
        public String serializePath;
        /**
         * Processing status
         */
        public int status = PROCESS_STATUS_ING;

        @Override
        public int hashCode() {
            return 0;
        }

        @Override
        public boolean equals(Object obj) {
            ProcessingTransferMetaData ptf = (ProcessingTransferMetaData) obj;
            return ptf.serializePath.equals(serializePath);
        }

        @Override
        public int compareTo(Object o) {
            ProcessingTransferMetaData ptf = (ProcessingTransferMetaData) o;
            return ptf.serializePath.compareTo(serializePath);
        }
    }

}
