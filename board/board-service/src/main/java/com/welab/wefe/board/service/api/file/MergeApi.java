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

package com.welab.wefe.board.service.api.file;

import com.welab.wefe.board.service.api.file.security.FileSecurityChecker;
import com.welab.wefe.board.service.base.file_system.WeFeFileSystem;
import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.ApiResult;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;

/**
 * @author zane.luo
 */
@Api(path = "file/merge", name = "Merge the chunks after the file is uploaded")
public class MergeApi extends AbstractApi<MergeApi.Input, MergeApi.Output> {

    @Override
    protected ApiResult<Output> handle(Input input) throws Exception {

        String mergedFileName = UUID.randomUUID() + "-" + input.filename;

        File dir = WeFeFileSystem.getBaseDir(input.uploadFileUseType)
                .resolve(input.uniqueIdentifier)
                .toFile();

        File[] parts = dir.listFiles();

        File mergedFile = WeFeFileSystem.getBaseDir(input.uploadFileUseType)
                .resolve(mergedFileName)
                .toFile();

        if (mergedFile.exists()) {
            return success(new Output(mergedFileName));
        }

        try {
            for (int i = 1; i <= parts.length; i++) {
                File part = WeFeFileSystem.getBaseDir(input.uploadFileUseType)
                        .resolve(input.uniqueIdentifier)
                        .resolve(i + ".part")
                        .toFile();

                // append chunk to the target file
                FileOutputStream stream = new FileOutputStream(mergedFile, true);
                FileUtils.copyFile(part, stream);
                stream.close();
            }

            // delete chunk
            FileUtils.deleteDirectory(dir);
        } catch (IOException e) {
            throw new StatusCodeWithException(StatusCode.SYSTEM_ERROR, e.getMessage());
        }

        // 检查上传的文件是否安全
        FileSecurityChecker.check(mergedFile);

        return success(new Output(mergedFileName));

    }

    public static class Output {
        private String filename;

        public Output(String filename) {
            this.filename = filename;
        }

        public String getFilename() {
            return filename;
        }

        public void setFilename(String filename) {
            this.filename = filename;
        }
    }

    public static class Input extends AbstractApiInput {
        private String filename;
        private String uniqueIdentifier;
        @Check(name = "文件用途", require = true)
        private WeFeFileSystem.UseType uploadFileUseType;

        public String getFilename() {
            return filename;
        }

        public void setFilename(String filename) {
            this.filename = filename;
        }

        public String getUniqueIdentifier() {
            return uniqueIdentifier;
        }

        public void setUniqueIdentifier(String uniqueIdentifier) {
            this.uniqueIdentifier = uniqueIdentifier;
        }

        public WeFeFileSystem.UseType getUploadFileUseType() {
            return uploadFileUseType;
        }

        public void setUploadFileUseType(WeFeFileSystem.UseType uploadFileUseType) {
            this.uploadFileUseType = uploadFileUseType;
        }
    }
}
