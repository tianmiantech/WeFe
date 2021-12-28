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

package com.welab.wefe.serving.service.api.feeconfig;

import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.serving.service.database.serving.entity.FeeConfigMysqlModel;
import com.welab.wefe.serving.service.service.FeeConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import java.io.IOException;


@Api(path = "feeconfig/save", name = "save fee config")
public class SaveApi extends AbstractApi<SaveApi.Input, FeeConfigMysqlModel> {

    @Autowired
    private FeeConfigService feeConfigService;

    @Override
    protected ApiResult<FeeConfigMysqlModel> handle(Input input) throws StatusCodeWithException, IOException {
        return success(feeConfigService.save(input));
    }

    public static class Input extends AbstractApiInput {

        @Check(name = "计费配置 id")
        private String id;

        @Check(name = "计费单价", require = true)
        private Double unitPrice;

        @Check(name = "付费方式")
        private Integer payType;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public Double getUnitPrice() {
            return unitPrice;
        }

        public void setUnitPrice(Double unitPrice) {
            this.unitPrice = unitPrice;
        }

        public Integer getPayType() {
            return payType;
        }

        public void setPayType(Integer payType) {
            this.payType = payType;
        }
    }

}
