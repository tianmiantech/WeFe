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

import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.model.GridFSFile;
import com.mongodb.client.gridfs.model.GridFSUploadOptions;
import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.data.mongodb.entity.union.MemberFileInfo;
import com.welab.wefe.common.data.mongodb.entity.union.UnionNode;
import com.welab.wefe.common.data.mongodb.repo.UnionNodeMongoRepo;
import com.welab.wefe.common.data.mongodb.util.QueryBuilder;
import com.welab.wefe.common.enums.FileRurpose;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.util.JObject;
import com.welab.wefe.common.util.Md5;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractWithFilesApiInput;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.common.web.dto.UploadFileApiOutput;
import com.welab.wefe.union.service.cache.UnionNodeConfigCache;
import com.welab.wefe.union.service.service.MemberFileInfoContractService;
import com.welab.wefe.union.service.task.UploadFileSyncToUnionTask;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;

import java.io.IOException;
import java.util.List;

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

        if (!FileRurpose.RealnameAuth.name().equals(input.getPurpose())) {
            throw new StatusCodeWithException(StatusCode.INVALID_PARAMETER, "purpose");
        }


        String fileName = input.getFilename();
        String sign = Md5.of(input.getFirstFile().getInputStream());
        String contentType = input.getFirstFile().getContentType();


        GridFSFile gridFSFile = gridFsTemplate.findOne(
                new QueryBuilder()
                        .append("metadata.sign", sign)
                        .append("metadata.memberId", input.getMemberId())
                        .build()
        );

        String fileId;
        if (gridFSFile == null) {
            GridFSUploadOptions options = new GridFSUploadOptions();
            Document metadata = new Document();
            metadata.append("contentType", contentType);
            metadata.append("sign", sign);
            metadata.append("memberId", input.getMemberId());

            options.metadata(metadata);

            fileId = gridFSBucket.uploadFromStream(fileName, input.getFirstFile().getInputStream(), options).toString();

            saveFileInfoToBlockchain(
                    input.memberId,
                    fileId,
                    fileName,
                    sign,
                    input.getFirstFile().getSize(),
                    input.purpose,
                    input.describe
            );

            syncDataToOtherUnionNode(input);

        } else {
            fileId = gridFSFile.getObjectId().toString();
            return success(new UploadFileApiOutput(fileId));
        }

        return success(new UploadFileApiOutput(fileId));
    }


    private void saveFileInfoToBlockchain(String memberId, String fileId, String fileName, String fileSign, long fileSize, String purpose, String describe) throws StatusCodeWithException {
        MemberFileInfo memberFileInfo = new MemberFileInfo();
        memberFileInfo.setFileId(fileId);
        memberFileInfo.setMemberId(memberId);
        memberFileInfo.setFileName(fileName);
        memberFileInfo.setFileSign(fileSign);
        memberFileInfo.setFileSize(String.valueOf(fileSize));
        memberFileInfo.setBlockchainNodeId(UnionNodeConfigCache.currentBlockchainNodeId);
        memberFileInfo.setPurpose(purpose);
        memberFileInfo.setEnable("1");
        memberFileInfo.setDescribe(describe);
        memberFileInfoContractService.add(memberFileInfo);
    }

    private void syncDataToOtherUnionNode(Input input) {
        List<UnionNode> unionNodeList = unionNodeMongoRepo.findExcludeCurrentNode(UnionNodeConfigCache.currentBlockchainNodeId);
        for (UnionNode unionNode :
                unionNodeList) {
            JObject params = JObject.create("filename", input.filename);
            params.append("memberId", input.memberId);
            params.append("purpose", input.purpose);


            new UploadFileSyncToUnionTask(
                    unionNode.getBaseUrl(),
                    "member/file/upload/sync",
                    params,
                    input.files
            ).start();
        }
    }

    public static class Input extends AbstractWithFilesApiInput {
        @Check(require = true)
        private String memberId;
        @Check(require = true)
        private String filename;
        @Check(require = true)
        private String purpose;
        private String describe;

        public String getFilename() {
            return filename;
        }

        public void setFilename(String filename) {
            this.filename = filename;
        }

        public String getMemberId() {
            return memberId;
        }

        public void setMemberId(String memberId) {
            this.memberId = memberId;
        }

        public String getPurpose() {
            return purpose;
        }

        public void setPurpose(String purpose) {
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
