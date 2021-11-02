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

import com.mongodb.client.gridfs.model.GridFSFile;
import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.data.mongodb.entity.union.ext.MemberExtJSON;
import com.welab.wefe.common.data.mongodb.entity.union.ext.RealNameAuthFileInfo;
import com.welab.wefe.common.data.mongodb.util.QueryBuilder;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiOutput;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.union.service.dto.base.BaseInput;
import com.welab.wefe.union.service.service.MemberContractService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;

import java.util.ArrayList;
import java.util.List;

/**
 * @author yuxin.zhang
 **/
@Api(path = "member/realname/auth", name = "member_realname_auth", rsaVerify = true, login = false)
public class RealNameAuthApi extends AbstractApi<RealNameAuthApi.Input, AbstractApiOutput> {

    @Autowired
    private MemberContractService memberContractService;

    @Autowired
    private GridFsTemplate gridFsTemplate;


    @Override
    protected ApiResult<AbstractApiOutput> handle(Input input) throws StatusCodeWithException {
        LOG.info("RealNameAuthApi handle..");
        MemberExtJSON extJSON = new MemberExtJSON();
        extJSON.setPrincipalName(input.principalName);
        extJSON.setAuthType(input.authType);
        extJSON.setDescription(input.description);


        List<RealNameAuthFileInfo> realNameAuthFileInfoList = new ArrayList<>();
        for (String fileId :
                input.fileIdList) {
            RealNameAuthFileInfo realNameAuthFileInfo = new RealNameAuthFileInfo();
            GridFSFile gridFSFile = gridFsTemplate.findOne(new QueryBuilder().append("_id", fileId).build());
            if (gridFSFile == null) {
                throw new StatusCodeWithException(StatusCode.FILE_DOES_NOT_EXIST, fileId);
            }
            realNameAuthFileInfo.setFileId(fileId);
            realNameAuthFileInfo.setSign(gridFSFile.getMetadata().getString("sign"));
        }
        extJSON.setRealNameAuthFileInfoList(realNameAuthFileInfoList);
        memberContractService.updateExtJson(input.curMemberId, extJSON);
        return success();
    }


    public static class Input extends BaseInput {
        @Check(require = true)
        private String principalName;
        @Check(require = true)
        private String authType;
        private String description;
        @Check(require = true)
        private List<String> fileIdList;


        public String getPrincipalName() {
            return principalName;
        }

        public void setPrincipalName(String principalName) {
            this.principalName = principalName;
        }

        public String getAuthType() {
            return authType;
        }

        public void setAuthType(String authType) {
            this.authType = authType;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public List<String> getFileIdList() {
            return fileIdList;
        }

        public void setFileIdList(List<String> fileIdList) {
            this.fileIdList = fileIdList;
        }
    }
}
