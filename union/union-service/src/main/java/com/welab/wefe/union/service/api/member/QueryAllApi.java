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

import com.welab.wefe.common.util.JObject;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.union.service.service.MemberService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author aaron.li
 **/
@Api(path = "member/query_all", name = "member_query_all", allowAccessWithSign = true, logSaplingInterval = 60_000)
public class QueryAllApi extends AbstractApi<QueryAllApi.Input, JObject> {

    @Autowired
    private MemberService memberService;

    @Override
    protected ApiResult<JObject> handle(QueryAllApi.Input input) {
        return success(JObject.create("list", JObject.toJSON(memberService.queryAll(input))));
    }

    public static class Input extends AbstractApiInput {
        /**
         * Primary key ID, if it is empty, query all
         */
        private String id;

        /**
         * Whether to include logo
         */
        private Boolean includeLogo = true;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public Boolean getIncludeLogo() {
            return includeLogo;
        }

        public void setIncludeLogo(Boolean includeLogo) {
            this.includeLogo = includeLogo;
        }
    }
}
