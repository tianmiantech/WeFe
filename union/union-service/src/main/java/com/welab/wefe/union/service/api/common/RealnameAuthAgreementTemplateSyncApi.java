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

package com.welab.wefe.union.service.api.common;

import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.model.GridFSFile;
import com.mongodb.client.gridfs.model.GridFSUploadOptions;
import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.data.mongodb.entity.union.RealnameAuthAgreementTemplate;
import com.welab.wefe.common.data.mongodb.repo.RealnameAuthAgreementTemplateMongoRepo;
import com.welab.wefe.common.data.mongodb.util.QueryBuilder;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.util.Md5;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractWithFilesApiInput;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.common.web.dto.UploadFileApiOutput;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;

import java.io.IOException;

/**
 * @author yuxin.zhang
 **/
@Api(path = "realname/auth/agreement/template/sync", name = "realname auth agreement template sync", login = false)
public class RealnameAuthAgreementTemplateSyncApi extends AbstractApi<RealnameAuthAgreementTemplateSyncApi.Input, UploadFileApiOutput> {
    @Autowired
    private RealnameAuthAgreementTemplateMongoRepo realnameAuthAgreementTemplateMongoRepo;
    @Autowired
    private GridFSBucket gridFSBucket;
    @Autowired
    private GridFsTemplate gridFsTemplate;

    @Override
    protected ApiResult<UploadFileApiOutput> handle(Input input) throws StatusCodeWithException, IOException {
        String fileName = input.getFilename();
        String sign = Md5.of(input.getFirstFile().getInputStream());
        String contentType = input.getFirstFile().getContentType();

        RealnameAuthAgreementTemplate realnameAuthAgreementTemplate = realnameAuthAgreementTemplateMongoRepo.findByTemplateFileSign(sign);
        if (realnameAuthAgreementTemplate == null) {
            throw new StatusCodeWithException(StatusCode.ILLEGAL_REQUEST);
        }
        GridFSFile gridFSFile = gridFsTemplate.findOne(
                new QueryBuilder()
                        .append("metadata.sign", sign)
                        .build()
        );
        String fileId;
        if (gridFSFile == null) {
            GridFSUploadOptions options = new GridFSUploadOptions();

            Document metadata = new Document();
            metadata.append("contentType", contentType);
            metadata.append("sign", sign);
            options.metadata(metadata);

            fileId = gridFSBucket.uploadFromStream(fileName, input.getFirstFile().getInputStream(), options).toString();

        } else {
            fileId = gridFSFile.getObjectId().toString();
        }

        return success(new UploadFileApiOutput(fileId));

    }


    public static class Input extends AbstractWithFilesApiInput {
        private String filename;

        public String getFilename() {
            return filename;
        }

        public void setFilename(String filename) {
            this.filename = filename;
        }

    }
}
