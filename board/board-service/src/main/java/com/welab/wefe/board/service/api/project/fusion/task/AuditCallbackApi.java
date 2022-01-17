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

package com.welab.wefe.board.service.api.project.fusion.task;

import com.welab.wefe.board.service.service.fusion.CallbackService;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.web.api.base.AbstractNoneOutputApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.common.wefe.enums.AuditStatus;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author hunter.zhao
 */
@Api(path = "fusion/audit/callback", name = "接收消息接口", login = false, rsaVerify = true)
public class AuditCallbackApi extends AbstractNoneOutputApi<AuditCallbackApi.Input> {
    @Autowired
    CallbackService callbackService;

    @Override
    protected ApiResult handler(Input input) throws StatusCodeWithException {
        callbackService.audit(input);
        return success();
    }


    public static class Input extends AbstractApiInput {

        @Check(name = "指定操作的businessId", require = true)
        private String businessId;

        @Check(name = "审核字段", require = true)
        private AuditStatus auditStatus;

        @Check(name = "审核评论")
        private String auditComment;

        @Check(name = "审核评论")
        private String partnerHashFunction;

        public String getBusinessId() {
            return businessId;
        }

        public void setBusinessId(String businessId) {
            this.businessId = businessId;
        }

        public AuditStatus getAuditStatus() {
            return auditStatus;
        }

        public void setAuditStatus(AuditStatus auditStatus) {
            this.auditStatus = auditStatus;
        }

        public String getAuditComment() {
            return auditComment;
        }

        public void setAuditComment(String auditComment) {
            this.auditComment = auditComment;
        }

        public String getPartnerHashFunction() {
            return partnerHashFunction;
        }

        public void setPartnerHashFunction(String partnerHashFunction) {
            this.partnerHashFunction = partnerHashFunction;
        }
    }
}
