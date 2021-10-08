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

package com.welab.wefe.board.service.api.member;

import com.welab.wefe.board.service.service.SystemInitializeService;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.web.api.base.AbstractNoneInputApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiOutput;
import com.welab.wefe.common.web.dto.ApiResult;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author Zane
 */
@Api(
        path = "member/is_initialized",
        name = "is the system initialized",
        desc = "The system cannot access any functional modules before initialization"
)
public class IsInitializedApi extends AbstractNoneInputApi<IsInitializedApi.Output> {

    @Autowired
    private SystemInitializeService systemInitializeService;

    @Override
    protected ApiResult<Output> handle() throws StatusCodeWithException {

        return success(new Output(systemInitializeService.isInitialized()));
    }

    public static class Output extends AbstractApiOutput {
        private boolean initialized;

        public Output(boolean initialized) {
            this.initialized = initialized;
        }

        //region getter/setter

        public boolean isInitialized() {
            return initialized;
        }

        public void setInitialized(boolean initialized) {
            this.initialized = initialized;
        }


        //endregion
    }
}
