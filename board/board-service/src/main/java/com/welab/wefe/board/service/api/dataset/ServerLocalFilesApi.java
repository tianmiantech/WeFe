/**
 * Copyright 2021 The WeFe Authors. All Rights Reserved.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.welab.wefe.board.service.api.dataset;

import com.welab.wefe.board.service.constant.Config;
import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiOutput;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.common.web.dto.NoneApiInput;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Johnny.lin
 */
@Api(path = "data_set/list_local_data_set_files", name = "query the files in the specified directory on the server")
public class ServerLocalFilesApi extends AbstractApi<NoneApiInput, ServerLocalFilesApi.Output> {

    @Autowired
    private Config config;

    private static final List<String> SUPPORT_SUFFIX = new ArrayList();

    static {
        SUPPORT_SUFFIX.add("xls");
        SUPPORT_SUFFIX.add("xlsx");
        SUPPORT_SUFFIX.add("csv");
    }

    @Override
    protected ApiResult<ServerLocalFilesApi.Output> handle(NoneApiInput input) throws StatusCodeWithException {
        List<String> files = new ArrayList<>();
        File file = new File(config.getFileUploadDir());
        LOG.info("file.exists(): " + file.exists());
        if (!file.exists() || !file.isDirectory()) {
            throw new StatusCodeWithException(StatusCode.DIRECTORY_NOT_FOUND, config.getFileUploadDir());
        }

        File[] tempList = file.listFiles();
        for (File fileObj : tempList) {
            if (fileObj.isFile()) {
                LOG.info("file: " + fileObj);

                //File name, excluding path
                String fileName = fileObj.getName();
                String suffix = fileName.substring(fileName.lastIndexOf(".") + 1);

                //Only XLS, xlsx, and CSV files are displayed
                if (!SUPPORT_SUFFIX.contains(suffix.toLowerCase())) {
                    continue;
                }

                files.add(fileName);
            }
        }

        Output output = new Output();
        output.setFiles(files);
        return success(output);
    }

    public static class Output extends AbstractApiOutput {
        private List<String> files;

        public List<String> getFiles() {
            return files;
        }

        public void setFiles(List<String> files) {
            this.files = files;
        }

        public Output() {

        }
    }
}
