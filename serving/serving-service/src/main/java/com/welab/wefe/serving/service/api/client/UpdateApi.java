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
import com.welab.wefe.common.util.StringUtil;
import com.welab.wefe.common.web.api.base.AbstractNoneOutputApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.serving.service.service.ClientService;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.SQLIntegrityConstraintViolationException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@Api(path = "client/update", name = "update")
public class UpdateApi extends AbstractNoneOutputApi<UpdateApi.Input> {


    private static final Pattern R = Pattern.compile("((2(5[0-5]|[0-4]\\d))|[0-1]?\\d{1,2})(\\.((2(5[0-5]|[0-4]\\d))|[0-1]?\\d{1,2})){3}");

    @Autowired
    private ClientService clientService;


    @Override
    protected ApiResult<?> handler(Input input) throws StatusCodeWithException {
        clientService.update(input);
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

        @Check(name = "修改人")
        private String updateBy;


        @Override
        public void checkAndStandardize() throws StatusCodeWithException {
            super.checkAndStandardize();
            List<String> ipArray = StringUtil.splitWithoutEmptyItem(ipAdd, ",");
            for (String ip : ipArray) {
                Matcher m = R.matcher(ip);
                if (!m.matches()) {
                    StatusCode.PARAMETER_VALUE_INVALID.throwException("错误的IP：" + ip);
                }
            }
            ipAdd = StringUtil.join(ipArray, ",");


        }


        public String getUpdateBy() {
            return updateBy;
        }

        public void setUpdateBy(String updateBy) {
            this.updateBy = updateBy;
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


    }


}
