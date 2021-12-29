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

package com.welab.wefe.common.web.api.base;

import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.common.web.dto.NoneApiOutput;

/**
 * API with no arguments
 *
 * @author Zane
 */
public abstract class AbstractNoneOutputApi<In extends AbstractApiInput> extends AbstractApi<In, NoneApiOutput> {

    /**
     * Interface implementation
     */
    protected abstract ApiResult handler(In input) throws StatusCodeWithException;

    @Override
    protected ApiResult<NoneApiOutput> handle(In input) throws StatusCodeWithException {
        return handler(input);
    }
}
