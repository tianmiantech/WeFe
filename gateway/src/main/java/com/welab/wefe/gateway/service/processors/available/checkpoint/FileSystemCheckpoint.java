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
package com.welab.wefe.gateway.service.processors.available.checkpoint;

import com.welab.wefe.common.util.FileUtil;
import com.welab.wefe.common.wefe.checkpoint.AbstractCheckpoint;
import com.welab.wefe.common.wefe.enums.ServiceType;
import com.welab.wefe.gateway.config.ConfigProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author zane
 * @date 2021/12/20
 */
@Service
public class FileSystemCheckpoint extends AbstractCheckpoint {

    @Autowired
    private ConfigProperties mConfigProperties;

    @Override
    protected ServiceType service() {
        return ServiceType.FileSystem;
    }

    @Override
    protected String desc() {
        return "检查对文件系统的访问是否正常";
    }

    @Override
    protected String value() {
        String sendDir = mConfigProperties.getSendTransferMetaPersistentTempDir();
        String recvDir = mConfigProperties.getRecvTransferMetaPersistentTempDir();
        return sendDir + " | " + recvDir;
    }

    @Override
    protected void doCheck(String value) throws Exception {
        String sendDir = mConfigProperties.getSendTransferMetaPersistentTempDir();
        String recvDir = mConfigProperties.getRecvTransferMetaPersistentTempDir();

        testCreateDir(sendDir);
        testCreateDir(recvDir);
    }

    private void testCreateDir(String dir) throws Exception {
        Path path = Paths.get(dir, "test_create");

        FileUtil.createDir(path.toString());
        File file = path.toFile();
        if (!file.exists()) {
            throw new Exception("创建文件夹失败：" + path);
        }

        FileUtil.deleteFileOrDir(file);
        if (file.exists()) {
            throw new Exception("删除文件夹失败：" + path);
        }
    }
}
