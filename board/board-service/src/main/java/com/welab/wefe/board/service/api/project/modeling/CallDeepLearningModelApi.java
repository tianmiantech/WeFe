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
package com.welab.wefe.board.service.api.project.modeling;

import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.util.FileUtil;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.ApiResult;

/**
 * @author zane
 * @date 2022/2/14
 */
@Api(path = "project/modeling/deep_learning/call", name = "调用深度学习模型")
public class CallDeepLearningModelApi extends AbstractApi<CallDeepLearningModelApi.Input, CallDeepLearningModelApi.Output> {

    @Override
    protected ApiResult<Output> handle(CallDeepLearningModelApi.Input input) throws Exception {
        return null;
    }

    public static class Output {
    }

    public static class Input extends AbstractApiInput {
        @Check(require = true)
        public String taskId;

        @Check(require = true, messageOnEmpty = "请指定数据集文件")
        public String filename;

        @Override
        public void checkAndStandardize() throws StatusCodeWithException {
            super.checkAndStandardize();

            // 如果是单张图片，要打包为 zip。
            if (FileUtil.isImage(filename)) {

            } else {
                String suffix = FileUtil.getFileSuffix(filename);
                if (!"zip".equalsIgnoreCase(suffix)) {
                    // TODO: Zane 删除文件
                    StatusCode.PARAMETER_VALUE_INVALID.throwException("不支持的文件类型：" + suffix);
                }
            }

        }
    }
}
