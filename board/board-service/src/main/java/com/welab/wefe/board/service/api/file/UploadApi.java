/**
 * Copyright 2021 Tianmian Tech. All Rights Reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.welab.wefe.board.service.api.file;

import com.welab.wefe.board.service.constant.Config;
import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.util.FileUtil;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractWithFilesApiInput;
import com.welab.wefe.common.web.dto.ApiResult;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * The front end uses the simple-uploader component
 * doc：https://github.com/simple-uploader/Uploader/blob/develop/README_zh-CN.md#%E5%A4%84%E7%90%86-get-%E6%88%96%E8%80%85-test-%E8%AF%B7%E6%B1%82
 *
 * @author zane.luo
 */
@Api(path = "file/upload", name = "upload file")
public class UploadApi extends AbstractApi<UploadApi.Input, UploadApi.Output> {

    @Autowired
    private Config config;

    @Override
    protected ApiResult<Output> handle(Input input) throws StatusCodeWithException {
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
     * Check if the chunk already exists
     */
    private ApiResult<Output> checkChunk(Input input) {
        Integer chunkNumber = input.getChunkNumber();
        if (chunkNumber == null) {
            chunkNumber = 0;
        }

        File outFile = new File(config.getFileUploadDir() + File.separator + input.getIdentifier(), chunkNumber + ".part");
        if (outFile.exists()) {
            return success()
                    .setMessage("该分片已存在");
        } else {
            return success()
                    .setHttpCode(299)
                    .setMessage("该分片不存在");
        }
    }

    /**
     * save chunk
     */
    private ApiResult<Output> saveChunk(Input input) throws StatusCodeWithException {
        MultipartFile inputFile = input.getFirstFile();

        Integer chunkNumber = input.getChunkNumber();
        if (chunkNumber == null) {
            chunkNumber = 0;
        }

        Path outputDir = Paths.get(config.getFileUploadDir(), input.getIdentifier());
        FileUtil.createDir(outputDir.toString());
        LOG.info("创建目录 " + outputDir.toFile().exists() + " ：" + outputDir);

        File outFile = outputDir.resolve(chunkNumber + ".part").toFile();

        try {
            InputStream inputStream = inputFile.getInputStream();
            FileUtils.copyInputStreamToFile(inputStream, outFile);
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
            throw new StatusCodeWithException(e.getMessage(), StatusCode.SYSTEM_ERROR);
        }

        return success(new Output(inputFile.getSize()));
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
        @Check(name = "当前文件块，从1开始")
        private Integer chunkNumber;
        @Check(name = "分块大小")
        private Long chunkSize;
        @Check(name = "当前分块大小")
        private Long currentChunkSize;
        @Check(name = "总大小")
        private Long totalSize;
        @Check(name = "文件标识")
        private String identifier;
        @Check(name = "文件名")
        private String filename;
        @Check(name = "相对路径")
        private String relativePath;
        @Check(name = "总块数")
        private Integer totalChunks;
        @Check(name = "文件类型")
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
