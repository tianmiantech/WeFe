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

package com.welab.wefe.union.service.api.member;

import com.alibaba.fastjson.JSONObject;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.model.GridFSFile;
import com.mongodb.client.gridfs.model.GridFSUploadOptions;
import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.data.mongodb.entity.union.UnionNode;
import com.welab.wefe.common.data.mongodb.repo.UnionNodeMongoRepo;
import com.welab.wefe.common.data.mongodb.util.QueryBuilder;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.http.HttpContentType;
import com.welab.wefe.common.http.HttpRequest;
import com.welab.wefe.common.util.Md5;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractWithFilesApiInput;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.union.service.dto.member.RealNameAuthFileUploadOutput;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;

import java.io.IOException;
import java.util.List;

/**
 * @author yuxin.zhang
 **/
@Api(path = "member/file/upload", name = "member_file_upload", rsaVerify = true, login = false)
public class FileUploadApi extends AbstractApi<FileUploadApi.Input, RealNameAuthFileUploadOutput> {
    @Autowired
    private UnionNodeMongoRepo unionNodeMongoRepo;

    @Autowired
    private GridFsTemplate gridFsTemplate;

    @Autowired
    private GridFSBucket gridFSBucket;

    @Override
    protected ApiResult<RealNameAuthFileUploadOutput> handle(Input input) throws StatusCodeWithException, IOException {
        LOG.info("FileUploadApi handle..");
        String fileName = input.getFirstFile().getOriginalFilename();
        String sign = Md5.of(input.getFirstFile().getInputStream());
        //根据文件id查询文件
        GridFSFile gridFSFile = gridFsTemplate.findOne(new QueryBuilder().append("metadata.sign", sign).build());
        if (gridFSFile == null) {
            RealNameAuthFileUploadOutput realNameAuthFileUploadOutput = new RealNameAuthFileUploadOutput();
            GridFSUploadOptions options = new GridFSUploadOptions();
            Document metadata = new Document();
            metadata.append("sign", sign);
            metadata.append("memberId", input.getMemberId());

            options.metadata(metadata);

            String fileId = gridFSBucket.uploadFromStream(fileName, input.getFirstFile().getInputStream(), options).toString();
            realNameAuthFileUploadOutput.setFileId(fileId);

            return success(realNameAuthFileUploadOutput);
        } else {
            throw new StatusCodeWithException("Do not upload the same file repeatedly", StatusCode.DUPLICATE_RESOURCE_ERROR);
        }


    }

    private void syncDataToOtherUnionNode(Input input) {
        List<UnionNode> list = unionNodeMongoRepo.findAll();
        for (UnionNode unionNode :
                list) {
            JSONObject result = HttpRequest.create(unionNode.getUnionBaseUrl() + "member/file/upload")
                    .setContentType(HttpContentType.MULTIPART)
                    .appendParameter("file", input.getFirstFile())
                    .post()
                    .getBodyAsJson();
        }
    }


    public static class Input extends AbstractWithFilesApiInput {
        @Check(require = true)
        private String memberId;

        public String getMemberId() {
            return memberId;
        }

        public void setMemberId(String memberId) {
            this.memberId = memberId;
        }

    }
}
