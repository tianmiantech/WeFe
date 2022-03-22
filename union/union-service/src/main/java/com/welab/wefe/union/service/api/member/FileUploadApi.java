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

package com.welab.wefe.union.service.api.member;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.welab.wefe.union.service.util.FileCheckerUtil;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.content.InputStreamBody;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;

import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.model.GridFSFile;
import com.mongodb.client.gridfs.model.GridFSUploadOptions;
import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.data.mongodb.entity.union.MemberFileInfo;
import com.welab.wefe.common.data.mongodb.entity.union.UnionNode;
import com.welab.wefe.common.data.mongodb.repo.UnionNodeMongoRepo;
import com.welab.wefe.common.data.mongodb.util.QueryBuilder;
import com.welab.wefe.common.enums.FilePublicLevel;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.util.JObject;
import com.welab.wefe.common.util.Md5;
import com.welab.wefe.common.util.StringUtil;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractWithFilesApiInput;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.common.web.dto.UploadFileApiOutput;
import com.welab.wefe.common.wefe.enums.FileRurpose;
import com.welab.wefe.union.service.cache.UnionNodeConfigCache;
import com.welab.wefe.union.service.service.MemberFileInfoContractService;
import com.welab.wefe.union.service.task.UploadFileSyncToUnionTask;

/**
 * @author yuxin.zhang
 **/
@Api(path = "member/file/upload", name = "member_file_upload", rsaVerify = true, login = false)
public class FileUploadApi extends AbstractApi<FileUploadApi.Input, UploadFileApiOutput> {
    @Autowired
    private UnionNodeMongoRepo unionNodeMongoRepo;

    @Autowired
    private GridFsTemplate gridFsTemplate;

    @Autowired
    private GridFSBucket gridFSBucket;

    @Autowired
    private MemberFileInfoContractService memberFileInfoContractService;


    @Override
    protected ApiResult<UploadFileApiOutput> handle(Input input) throws StatusCodeWithException, IOException {
        LOG.info("FileUploadApi handle..");

        if (FileRurpose.RealnameAuth != input.purpose) {
            throw new StatusCodeWithException(StatusCode.INVALID_PARAMETER, "purpose");
        }

        String fileName = input.getFilename();

        // 检查文件是否是支持的文件类型
        try {
            FileCheckerUtil.checkIsAllowFileType(fileName);
        } catch (Exception e) {
            return fail(e)
                    .setHttpCode(599);
        }

        String sign = Md5.of(input.getFirstFile().getInputStream());
        String contentType = input.getFirstFile().getContentType();

        Map<String, InputStreamBody> fileStreamBodyMap = buildFileStreamBodyMap(input.files);

        GridFSFile gridFSFile = gridFsTemplate.findOne(
                new QueryBuilder()
                        .append("metadata.sign", sign)
                        .append("metadata.memberId", input.getCurMemberId())
                        .build()
        );

        String fileId;
        if (gridFSFile == null) {
            GridFSUploadOptions options = new GridFSUploadOptions();
            Document metadata = new Document();
            metadata.append("contentType", contentType);
            metadata.append("sign", sign);
            metadata.append("memberId", input.curMemberId);

            options.metadata(metadata);

            fileId = gridFSBucket.uploadFromStream(fileName, input.getFirstFile().getInputStream(), options).toString();

            saveFileInfoToBlockchain(
                    input.curMemberId,
                    fileId,
                    fileName,
                    sign,
                    input.getFirstFile().getSize(),
                    input.purpose.name(),
                    input.filePublicLevel.name(),
                    input.describe
            );

            syncDataToOtherUnionNode(input.curMemberId, fileStreamBodyMap);

        } else {
            fileId = gridFSFile.getObjectId().toString();
            return success(new UploadFileApiOutput(fileId));
        }

        return success(new UploadFileApiOutput(fileId));
    }


    private void saveFileInfoToBlockchain(
            String memberId,
            String fileId,
            String fileName,
            String fileSign,
            long fileSize,
            String purpose,
            String filePublicLevel,
            String describe
    ) throws StatusCodeWithException {
        MemberFileInfo memberFileInfo = new MemberFileInfo();
        memberFileInfo.setFileId(fileId);
        memberFileInfo.setMemberId(memberId);
        memberFileInfo.setFileName(fileName);
        memberFileInfo.setFileSign(fileSign);
        memberFileInfo.setFileSize(String.valueOf(fileSize));
        memberFileInfo.setBlockchainNodeId(UnionNodeConfigCache.currentBlockchainNodeId);
        memberFileInfo.setRurpose(purpose);
        memberFileInfo.setFilePublicLevel(filePublicLevel);
        memberFileInfo.setDescribe(describe);
        memberFileInfoContractService.add(memberFileInfo);
    }

    private Map<String, InputStreamBody> buildFileStreamBodyMap(MultiValueMap<String, MultipartFile> files) throws StatusCodeWithException {
        Map<String, InputStreamBody> fileStreamBodyMap = new HashMap<>();
        for (Map.Entry<String, MultipartFile> item : files.toSingleValueMap().entrySet()) {
            try {
                MultipartFile file = item.getValue();
                ContentType contentType = StringUtil.isEmpty(file.getContentType())
                        ? ContentType.DEFAULT_BINARY
                        : ContentType.create(file.getContentType());

                InputStreamBody streamBody = new InputStreamBody(
                        file.getInputStream(),
                        contentType,
                        file.getOriginalFilename()
                );
                fileStreamBodyMap.put(item.getKey(), streamBody);
            } catch (IOException e) {
                LOG.error("File read / write failed", e);
                throw new StatusCodeWithException(StatusCode.FILE_IO_ERROR);
            }
        }
        return fileStreamBodyMap;
    }

    private void syncDataToOtherUnionNode(String memberId, Map<String, InputStreamBody> fileStreamBodyMap) {
        List<UnionNode> unionNodeList = unionNodeMongoRepo.findExcludeCurrentNode(UnionNodeConfigCache.currentBlockchainNodeId);
        for (UnionNode unionNode :
                unionNodeList) {

            new UploadFileSyncToUnionTask(
                    unionNode.getBaseUrl(),
                    "member/file/upload/sync",
                    JObject.create("memberId", memberId),
                    fileStreamBodyMap
            ).start();
        }
    }

    public static class Input extends AbstractWithFilesApiInput {
        @Check(require = true)
        private String curMemberId;
        @Check(require = true)
        private String filename;
        @Check(require = true)
        private FileRurpose purpose;
        private FilePublicLevel filePublicLevel = FilePublicLevel.Private;
        private String describe;

        public String getFilename() {
            return filename;
        }

        public void setFilename(String filename) {
            this.filename = filename;
        }

        public String getCurMemberId() {
            return curMemberId;
        }

        public void setCurMemberId(String curMemberId) {
            this.curMemberId = curMemberId;
        }

        public FileRurpose getPurpose() {
            return purpose;
        }

        public void setPurpose(FileRurpose purpose) {
            this.purpose = purpose;
        }

        public String getDescribe() {
            return describe;
        }

        public void setDescribe(String describe) {
            this.describe = describe;
        }
    }
}
