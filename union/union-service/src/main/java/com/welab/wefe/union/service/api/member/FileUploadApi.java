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

import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.enums.FilePublicLevel;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractWithFilesApiInput;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.common.web.dto.UploadFileApiOutput;
import com.welab.wefe.common.wefe.enums.FileRurpose;
import com.welab.wefe.union.service.service.MemberService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author yuxin.zhang
 **/
@Api(path = "member/file/upload", name = "member_file_upload", allowAccessWithSign = true)
public class FileUploadApi extends AbstractApi<FileUploadApi.Input, UploadFileApiOutput> {
    @Autowired
    private MemberService memberService;


    @Override
    protected ApiResult<UploadFileApiOutput> handle(Input input) throws StatusCodeWithException {
        LOG.info("FileUploadApi handle..");
        try {
            return success(memberService.fileUpload(input));
        } catch (StatusCodeWithException e) {
            if (StatusCode.FILE_IO_ERROR.equals(e.getStatusCode())) {
                return fail(e).setHttpCode(599);
            }
            throw e;
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

        public FilePublicLevel getFilePublicLevel() {
            return filePublicLevel;
        }
    }
}
