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
package com.welab.wefe.board.service.api.service;

import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.web.api.base.AbstractNoneInputApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.ApiResult;

import java.util.Date;

/**
 * @author zane
 * @date 2022/4/7
 */
@Api(path = "service/version", name = "版本信息", login = false)
public class VersionApi extends AbstractNoneInputApi<VersionApi.Output> {

    @Override
    protected ApiResult<Output> handle() throws StatusCodeWithException {
        return success(new Output());
    }

    public static class Output {
        @Check(name = "大版本号")
        public String version = "3.1.0";
        @Check(name = "小版本号")
        public long build = 20200426001L;
        @Check(name = "发布时间")
        public Date date = new Date(1650960596599L);
    }
}
