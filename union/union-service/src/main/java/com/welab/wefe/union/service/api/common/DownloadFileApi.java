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

package com.welab.wefe.union.service.api.common;

import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSDownloadStream;
import com.mongodb.client.gridfs.model.GridFSFile;
import com.mongodb.client.gridfs.model.GridFSUploadOptions;
import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.data.mongodb.entity.union.MemberFileInfo;
import com.welab.wefe.common.data.mongodb.entity.union.RealnameAuthAgreementTemplate;
import com.welab.wefe.common.data.mongodb.entity.union.UnionNode;
import com.welab.wefe.common.data.mongodb.repo.MemberFileInfoMongoRepo;
import com.welab.wefe.common.data.mongodb.repo.RealnameAuthAgreementTemplateMongoRepo;
import com.welab.wefe.common.data.mongodb.repo.UnionNodeMongoRepo;
import com.welab.wefe.common.data.mongodb.util.QueryBuilder;
import com.welab.wefe.common.enums.FileRurpose;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.util.JObject;
import com.welab.wefe.common.util.UrlUtil;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.union.service.dto.base.BaseInput;
import org.apache.commons.io.IOUtils;
import org.bson.BsonObjectId;
import org.bson.BsonValue;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URLEncoder;

/**
 * @author yuxin.zhang
 */
@Api(path = "download/file", name = "download_file", rsaVerify = true, login = false)
public class DownloadFileApi extends AbstractApi<DownloadFileApi.Input, ResponseEntity<byte[]>> {

    @Autowired
    private GridFSBucket gridFSBucket;
    @Autowired
    private GridFsTemplate gridFsTemplate;
    @Autowired
    private UnionNodeMongoRepo unionNodeMongoRepo;
    @Autowired
    private MemberFileInfoMongoRepo memberFileInfoMongoRepo;
    @Autowired
    private RealnameAuthAgreementTemplateMongoRepo realnameAuthAgreementTemplateMongoRepo;

    @Override
    protected ApiResult<ResponseEntity<byte[]>> handle(DownloadFileApi.Input input) throws IOException, StatusCodeWithException {
        MemberFileInfo memberFileInfo = memberFileInfoMongoRepo.findByFileId(input.fileId);
        RealnameAuthAgreementTemplate realnameAuthAgreementTemplate = realnameAuthAgreementTemplateMongoRepo.findByTemplateFileId(input.fileId);

        String fileUploadCurrentBlockchainNodeId;
        if (memberFileInfo == null) {
            if (realnameAuthAgreementTemplate == null) {
                throw new StatusCodeWithException(StatusCode.FILE_DOES_NOT_EXIST, input.fileId);
            } else {
                fileUploadCurrentBlockchainNodeId = realnameAuthAgreementTemplate.getBlockchainNodeId();
            }
        } else {
            if (!memberFileInfo.getMemberId().equals(input.curMemberId) && FileRurpose.RealnameAuth.name().equals(memberFileInfo.getRurpose())) {
                throw new StatusCodeWithException("没有下载该文件的权限", StatusCode.ILLEGAL_REQUEST);
            } else {
                fileUploadCurrentBlockchainNodeId = memberFileInfo.getBlockchainNodeId();
            }
        }


        GridFSFile gridFSFile = gridFsTemplate.findOne(new QueryBuilder().append("_id", input.getFileId()).build());
        if (gridFSFile == null) {
            UnionNode unionNode = unionNodeMongoRepo.findByBlockchainNodeId(fileUploadCurrentBlockchainNodeId);
            String url = unionNode.getBaseUrl() + "/download/file";
            url = UrlUtil.appendQueryParameters(url, JObject.create(input));
            RequestEntity requestEntity = new RequestEntity<>(null, null, HttpMethod.GET, UrlUtil.createUri(url));
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<byte[]> response = restTemplate.exchange(requestEntity, byte[].class);
            new Thread(() -> {
                if (memberFileInfo != null) {
                    saveFileToCurrentNode(memberFileInfo, response);
                } else {
                    saveFileToCurrentNode(realnameAuthAgreementTemplate, response);
                }
            }).start();

            return success(response);

        }
        //Open a download stream object using gridfsbucket
        GridFSDownloadStream gridFSDownloadStream = gridFSBucket.openDownloadStream(gridFSFile.getObjectId());
        //Create a gridfsresource object and get the stream
        GridFsResource gridFsResource = new GridFsResource(gridFSFile, gridFSDownloadStream);
        String contentType = gridFSFile.getMetadata().getString("contentType");

        HttpHeaders headers = new HttpHeaders();
        headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
        headers.add("Content-Disposition", "attachment; filename=" + URLEncoder.encode(gridFSFile.getFilename(), "UTF-8"));
        headers.add("filename", URLEncoder.encode(gridFSFile.getFilename(), "UTF-8"));
        headers.add("Pragma", "no-cache");
        headers.add("ETag", String.valueOf(System.currentTimeMillis()));
        ResponseEntity<byte[]> response = ResponseEntity
                .ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType(contentType))
                .body(IOUtils.toByteArray(gridFsResource.getInputStream()));

        return success(response);
    }

    private void saveFileToCurrentNode(RealnameAuthAgreementTemplate realnameAuthAgreementTemplate, ResponseEntity<byte[]> response) {
        saveFileToCurrentNode(
                realnameAuthAgreementTemplate.getTemplateFileSign(),
                realnameAuthAgreementTemplate.getTemplateFileId(),
                realnameAuthAgreementTemplate.getFileName(),
                null,
                response
        );
    }

    private void saveFileToCurrentNode(MemberFileInfo memberFileInfo, ResponseEntity<byte[]> response) {
        saveFileToCurrentNode(
                memberFileInfo.getFileSign(),
                memberFileInfo.getFileId(),
                memberFileInfo.getFileName(),
                memberFileInfo.getMemberId(),
                response
        );
    }

    private void saveFileToCurrentNode(
            String fileSign,
            String fileId,
            String fileName,
            String memberId,
            ResponseEntity<byte[]> response
    ) {
        GridFSUploadOptions options = new GridFSUploadOptions();
        Document metadata = new Document();
        metadata.append("contentType", response.getHeaders().getFirst("Content-Type"));
        metadata.append("sign", fileSign);
        if (memberId != null) {
            metadata.append("memberId", memberId);
        }

        options.metadata(metadata);
        BsonValue fileObjectId = new BsonObjectId(new ObjectId(fileId));
        gridFSBucket.uploadFromStream(
                fileObjectId,
                fileName,
                new ByteArrayInputStream(response.getBody()),
                options);

    }


    public static class Input extends BaseInput {
        @Check(require = true)
        private String fileId;

        public String getFileId() {
            return fileId;
        }

        public void setFileId(String fileId) {
            this.fileId = fileId;
        }
    }

}
