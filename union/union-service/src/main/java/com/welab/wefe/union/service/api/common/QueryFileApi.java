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
import com.mongodb.client.gridfs.GridFSDownloadStream;
import com.mongodb.client.gridfs.model.GridFSFile;
import com.welab.wefe.common.data.mongodb.dto.QueryFileOutput;
import com.welab.wefe.common.data.mongodb.util.QueryBuilder;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.union.service.dto.base.BaseInput;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;

import java.io.IOException;

/**
 * @author yuxin.zhang
 */
@Api(path = "query/file", name = "query_file", login = false)
public class QueryFileApi extends AbstractApi<QueryFileApi.Input, QueryFileOutput> {

    @Autowired
    private GridFSBucket gridFSBucket;
    @Autowired
    private GridFsTemplate gridFsTemplate;

    @Override
    protected ApiResult<QueryFileOutput> handle(QueryFileApi.Input input) throws IOException {
        //根据文件id查询文件
        GridFSFile gridFSFile = gridFsTemplate.findOne(new QueryBuilder().append("_id", input.getFileId()).build());
        //使用GridFsBucket打开一个下载流对象
        GridFSDownloadStream gridFSDownloadStream = gridFSBucket.openDownloadStream(gridFSFile.getObjectId());
        //创建GridFsResource对象，获取流
        GridFsResource gridFsResource = new GridFsResource(gridFSFile, gridFSDownloadStream);
        QueryFileOutput queryFileOutput = new QueryFileOutput();
        queryFileOutput.setFileData(IOUtils.toByteArray(gridFsResource.getInputStream()));
        queryFileOutput.setFileName(gridFSFile.getFilename());
        queryFileOutput.setFileType(gridFSFile.getMetadata().getString("fileType"));
        return success(queryFileOutput);
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
