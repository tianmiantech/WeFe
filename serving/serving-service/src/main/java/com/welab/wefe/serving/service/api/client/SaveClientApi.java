/*
 * Copyright 2021 Tianmian Tech. All Rights Reserved.
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

package com.welab.wefe.serving.service.api.client;

import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.web.api.base.AbstractNoneOutputApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.serving.service.service.ClientService;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.SQLIntegrityConstraintViolationException;


@Api(path = "client/save", name = "save")
public class SaveClientApi extends AbstractNoneOutputApi<SaveClientApi.Input> {


    @Autowired
    private ClientService clientService;

    @Override
    protected ApiResult<?> handler(Input input) throws StatusCodeWithException {

        try {
            clientService.save(input);
        } catch (SQLIntegrityConstraintViolationException e) {
            e.printStackTrace();
            return fail(StatusCode.SQL_UNIQUE_IN_CODE.getCode(), StatusCode.SQL_UNIQUE_IN_CODE.getMessage());
        }
        return success();
    }


    public static class Input extends AbstractApiInput {

        @Check(name = "客户id")
        private String id;

        @Check(name = "客户名称", require = true)
        private String name;

        @Check(name = "客户邮箱")
        private String email;

        @Check(name = "客户调用端 IP 地址", require = true)
        private String ipAdd;

        @Check(name = "客户公钥", require = true)
        private String pubKey;

        @Check(name = "备注")
        private String remark;

        @Check(name = "状态")
        private Integer status;

        private String createdBy;

        @Check(name = "客户 code")
        private String code;

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getIpAdd() {
            return ipAdd;
        }

        public void setIpAdd(String ipAdd) {
            this.ipAdd = ipAdd;
        }

        public String getPubKey() {
            return pubKey;
        }

        public void setPubKey(String pubKey) {
            this.pubKey = pubKey;
        }

        public String getRemark() {
            return remark;
        }

        public void setRemark(String remark) {
            this.remark = remark;
        }

        public Integer getStatus() {
            return status;
        }

        public void setStatus(Integer status) {
            this.status = status;
        }

        public String getCreatedBy() {
            return createdBy;
        }

        public void setCreatedBy(String createdBy) {
            this.createdBy = createdBy;
        }
    }


}
