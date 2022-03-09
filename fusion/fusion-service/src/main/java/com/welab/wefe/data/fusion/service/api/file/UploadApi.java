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

package com.welab.wefe.data.fusion.service.api.file;

import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractWithFilesApiInput;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.data.fusion.service.utils.FileSecurityChecker;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * The front-end uses simple- Uploader component
 * The document：https://github.com/simple-uploader/Uploader/blob/develop/README_zh-CN.md#%E5%A4%84%E7%90%86-get-%E6%88%96%E8%80%85-test-%E8%AF%B7%E6%B1%82
 *
 * @author jacky.jiang
 */
@Api(path = "file/upload", name = "Upload a file")
public class UploadApi extends AbstractApi<UploadApi.Input, UploadApi.Output> {

    @Value("${file.upload.dir}")
    String fileUploadDir;

    @Override
    protected ApiResult<Output> handle(Input input) throws StatusCodeWithException {
        Boolean CanUploaded = FileSecurityChecker.isValid(input.filename);
        if (!CanUploaded) {
            throw new StatusCodeWithException("该文件不为.csv,.xls,xlsx之一，禁止上传！",StatusCode.PARAMETER_VALUE_INVALID);
        }

        switch (input.method) {
            case "POST":
                return saveChunk(input);

            case "GET":
                return checkChunk(input);

            default:
                throw new StatusCodeWithException(StatusCode.UNEXPECTED_ENUM_CASE);
        }


    }

    /**
     * Check whether a fragment already exists
     */
    private ApiResult<Output> checkChunk(Input input) {
        Integer chunkNumber = input.getChunkNumber();
        if (chunkNumber == null) {
            chunkNumber = 0;
        }

        File outFile = new File(fileUploadDir + File.separator + input.getIdentifier(), chunkNumber + ".part");
        if (outFile.exists()) {
            return success()
                    .setMessage("The shard already exists");
        } else {
            return success()
                    .setHttpCode(299)
                    .setMessage("The shard does not exist");
        }
    }

    /**
     * Save the shard
     */
    private ApiResult<Output> saveChunk(Input input) throws StatusCodeWithException {
        MultipartFile file = input.getFirstFile();

        Integer chunkNumber = input.getChunkNumber();
        if (chunkNumber == null) {
            chunkNumber = 0;
        }

        File outFile = new File(fileUploadDir + File.separator + input.getIdentifier(), chunkNumber + ".part");


        try {
            InputStream inputStream = file.getInputStream();
            FileUtils.copyInputStreamToFile(inputStream, outFile);
        } catch (IOException e) {
            throw new StatusCodeWithException(e.getMessage(), StatusCode.SYSTEM_ERROR);
        }

        return success(new Output(file.getSize()));
    }

    public static class Output {
        private long length;

        public Output(long length) {
            this.length = length;
        }

        public long getLength() {
            return length;
        }

        public void setLength(long length) {
            this.length = length;
        }
    }

    public static class Input extends AbstractWithFilesApiInput {
        private Long id;
        /**
         * Current file block, starting at 1
         */
        private Integer chunkNumber;
        /**
         * Block size
         */
        private Long chunkSize;
        /**
         * Current block size
         */
        private Long currentChunkSize;
        /**
         * Total size
         */
        private Long totalSize;
        /**
         * File identifier
         */
        private String identifier;
        /**
         * The file name
         */
        private String filename;
        /**
         * Relative paths
         */
        private String relativePath;
        /**
         * Total blocks
         */
        private Integer totalChunks;
        /**
         * The file type
         */
        private String type;

        //region getter/setter

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public Integer getChunkNumber() {
            return chunkNumber;
        }

        public void setChunkNumber(Integer chunkNumber) {
            this.chunkNumber = chunkNumber;
        }

        public Long getChunkSize() {
            return chunkSize;
        }

        public void setChunkSize(Long chunkSize) {
            this.chunkSize = chunkSize;
        }

        public Long getCurrentChunkSize() {
            return currentChunkSize;
        }

        public void setCurrentChunkSize(Long currentChunkSize) {
            this.currentChunkSize = currentChunkSize;
        }

        public Long getTotalSize() {
            return totalSize;
        }

        public void setTotalSize(Long totalSize) {
            this.totalSize = totalSize;
        }

        public String getIdentifier() {
            return identifier;
        }

        public void setIdentifier(String identifier) {
            this.identifier = identifier;
        }

        public String getFilename() {
            return filename;
        }

        public void setFilename(String filename) {
            this.filename = filename;
        }

        public String getRelativePath() {
            return relativePath;
        }

        public void setRelativePath(String relativePath) {
            this.relativePath = relativePath;
        }

        public Integer getTotalChunks() {
            return totalChunks;
        }

        public void setTotalChunks(Integer totalChunks) {
            this.totalChunks = totalChunks;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }


        //endregion
    }
}
