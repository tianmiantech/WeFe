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

package com.welab.wefe.union.service.service;

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
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.util.JObject;
import com.welab.wefe.common.util.Md5;
import com.welab.wefe.common.util.ThreadUtil;
import com.welab.wefe.common.util.UrlUtil;
import com.welab.wefe.common.web.dto.AbstractWithFilesApiInput;
import com.welab.wefe.common.web.dto.UploadFileApiOutput;
import com.welab.wefe.common.wefe.enums.FileRurpose;
import com.welab.wefe.union.service.api.common.DownloadFileApi;
import com.welab.wefe.union.service.api.common.MemberFileUploadSyncApi;
import com.welab.wefe.union.service.dto.base.BaseInput;
import com.welab.wefe.union.service.dto.common.RealnameAuthAgreementTemplateOutput;
import org.apache.commons.io.IOUtils;
import org.bson.BsonObjectId;
import org.bson.BsonValue;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.ByteArrayInputStream;
import java.net.URLEncoder;

@Service
public class CommonService {
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

    public ResponseEntity<byte[]> downloadFile(DownloadFileApi.Input input) throws StatusCodeWithException {
        try {
            MemberFileInfo memberFileInfo = memberFileInfoMongoRepo.findByFileId(input.getFileId());
            RealnameAuthAgreementTemplate realnameAuthAgreementTemplate = realnameAuthAgreementTemplateMongoRepo.findByTemplateFileId(input.getFileId());

            String fileUploadCurrentBlockchainNodeId = null;
            if (memberFileInfo == null) {
                if (realnameAuthAgreementTemplate == null) {
                    StatusCode.FILE_DOES_NOT_EXIST.throwExWithFormatMsg(input.getFileId());
                } else {
                    fileUploadCurrentBlockchainNodeId = realnameAuthAgreementTemplate.getBlockchainNodeId();
                }
            } else {
                if (!memberFileInfo.getMemberId().equals(input.curMemberId) && FileRurpose.RealnameAuth.name().equals(memberFileInfo.getRurpose())) {
                    throw new StatusCodeWithException(StatusCode.ILLEGAL_REQUEST, "没有下载该文件的权限");
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

                return response;

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
            headers.add("Cache-Access-Control-Expose-Headers", "filename");
            ResponseEntity<byte[]> response = ResponseEntity
                    .ok()
                    .headers(headers)
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(IOUtils.toByteArray(gridFsResource.getInputStream()));

            return response;
        } catch (StatusCodeWithException e) {
            throw e;
        } catch (Exception e) {
            StatusCode.SYSTEM_ERROR.throwException(e);
            return null;
        }
    }


    public UploadFileApiOutput memberFileUpload(MemberFileUploadSyncApi.Input input) throws StatusCodeWithException {
        try {
            String sign = Md5.of(input.getFirstFile().getInputStream());
            String contentType = input.getFirstFile().getContentType();

            MemberFileInfo memberFileInfo = null;
            for (int i = 0; i < 3; i++) {
                memberFileInfo = memberFileInfoMongoRepo.findByFileSign(sign);
                if (memberFileInfo == null) {
                    ThreadUtil.sleep(2000);
                    continue;
                }
                break;
            }

            if (memberFileInfo == null) {
                throw new StatusCodeWithException(StatusCode.ILLEGAL_REQUEST);
            }

            GridFSFile gridFSFile = gridFsTemplate.findOne(
                    new QueryBuilder()
                            .append("metadata.sign", sign)
                            .append("metadata.memberId", input.getMemberId())
                            .build()
            );

            String fileId = memberFileInfo.getFileId();
            if (gridFSFile == null) {
                GridFSUploadOptions options = new GridFSUploadOptions();
                Document metadata = new Document();
                metadata.append("contentType", contentType);
                metadata.append("sign", sign);
                metadata.append("memberId", input.getMemberId());

                options.metadata(metadata);

                gridFSBucket.uploadFromStream(
                        new BsonObjectId(new ObjectId(fileId)),
                        memberFileInfo.getFileName(),
                        input.getFirstFile().getInputStream(),
                        options);
            } else {
                fileId = gridFSFile.getObjectId().toString();
            }

            return new UploadFileApiOutput(fileId);
        } catch (StatusCodeWithException e) {
            throw e;
        } catch (Exception e) {
            StatusCode.SYSTEM_ERROR.throwException(e);
        }
        return null;
    }


    public RealnameAuthAgreementTemplateOutput queryRealNameAuthAgreementTemplate(BaseInput input) {
        RealnameAuthAgreementTemplate realnameAuthAgreementTemplate = realnameAuthAgreementTemplateMongoRepo.findByEnable(true);
        RealnameAuthAgreementTemplateOutput realnameAuthAgreementTemplateOutput = new RealnameAuthAgreementTemplateOutput();
        if (realnameAuthAgreementTemplate != null) {
            realnameAuthAgreementTemplateOutput.setTemplateFileId(realnameAuthAgreementTemplate.getTemplateFileId());
            realnameAuthAgreementTemplateOutput.setFileName(realnameAuthAgreementTemplate.getFileName());
        }
        return realnameAuthAgreementTemplateOutput;
    }

    public UploadFileApiOutput queryRealNameAuthAgreementTemplateUploadFile(AbstractWithFilesApiInput input) throws StatusCodeWithException {
        try {

            String sign = Md5.of(input.getFirstFile().getInputStream());
            String contentType = input.getFirstFile().getContentType();

            RealnameAuthAgreementTemplate realnameAuthAgreementTemplate = null;
            for (int i = 0; i < 3; i++) {
                realnameAuthAgreementTemplate = realnameAuthAgreementTemplateMongoRepo.findByTemplateFileSign(sign);
                if (realnameAuthAgreementTemplate == null) {
                    continue;
                }
                break;
            }

            if (realnameAuthAgreementTemplate == null) {
                throw new StatusCodeWithException(StatusCode.ILLEGAL_REQUEST);
            }

            GridFSFile gridFSFile = gridFsTemplate.findOne(
                    new QueryBuilder()
                            .append("metadata.sign", sign)
                            .build()
            );
            String fileId = realnameAuthAgreementTemplate.getTemplateFileId();
            if (gridFSFile == null) {
                GridFSUploadOptions options = new GridFSUploadOptions();

                Document metadata = new Document();
                metadata.append("contentType", contentType);
                metadata.append("sign", sign);
                options.metadata(metadata);


                gridFSBucket.uploadFromStream(
                        new BsonObjectId(new ObjectId(fileId)),
                        realnameAuthAgreementTemplate.getFileName(),
                        input.getFirstFile().getInputStream(),
                        options);
            } else {
                fileId = gridFSFile.getObjectId().toString();
            }

            return new UploadFileApiOutput(fileId);

        } catch (StatusCodeWithException e) {
            throw e;
        } catch (Exception e) {
            StatusCode.SYSTEM_ERROR.throwException(e);
            return null;
        }
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

    private void saveFileToCurrentNode(String fileSign, String fileId, String fileName, String memberId, ResponseEntity<byte[]> response) {
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
}
