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

package com.welab.wefe.manager.service.api.common;

import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSDownloadStream;
import com.mongodb.client.gridfs.model.GridFSFile;
import com.welab.wefe.common.data.mongodb.util.QueryBuilder;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.manager.service.dto.common.QueryFileInput;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.net.URLEncoder;

/**
 * @author yuxin.zhang
 */
@Api(path = "download/file", name = "download_file")
public class DownloadFileApi extends AbstractApi<QueryFileInput, ResponseEntity<byte[]>> {

    @Autowired
    private GridFSBucket gridFSBucket;
    @Autowired
    private GridFsTemplate gridFsTemplate;


    @Override
    protected ApiResult<ResponseEntity<byte[]>> handle(QueryFileInput input) throws IOException {
        GridFSFile gridFSFile = gridFsTemplate.findOne(new QueryBuilder().append("_id", input.getFileId()).build());

        String contentType = gridFSFile.getMetadata().getString("contentType");
        GridFSDownloadStream gridFSDownloadStream = gridFSBucket.openDownloadStream(gridFSFile.getObjectId());
        GridFsResource gridFsResource = new GridFsResource(gridFSFile, gridFSDownloadStream);


        HttpHeaders headers = new HttpHeaders();
        headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
        headers.add("Content-Disposition", "attachment; filename=" + URLEncoder.encode(gridFSFile.getFilename(), "UTF-8"));
        headers.add("Pragma", "no-cache");
        headers.add("ETag", String.valueOf(System.currentTimeMillis()));
        headers.add("filename", URLEncoder.encode(gridFSFile.getFilename(), "UTF-8"));
        headers.add("Cache-Access-Control-Expose-Headers", "filename");

        ResponseEntity<byte[]> response = ResponseEntity
                .ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType(contentType))
                .body(IOUtils.toByteArray(gridFsResource.getInputStream()));

        return success(response);

    }


}
