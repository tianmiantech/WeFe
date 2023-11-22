/*
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
package com.welab.wefe.serving.service.api.model;

import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.util.StringUtil;
import com.welab.wefe.common.web.api.base.AbstractNoneOutputApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.serving.service.enums.ModelTypeEnum;
import com.welab.wefe.serving.service.service.model.ModelImportService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author hunter.zhao
 * @date 2022/3/8
 */
@Api(path = "model/import", name = "导入模型文件", desc = "导入模型文件",login = false)
public class ImportApi extends AbstractNoneOutputApi<ImportApi.Input> {
    @Autowired
    private ModelImportService modelImportService;

    @Override
    protected ApiResult handler(Input input) throws StatusCodeWithException {
        switch (input.getModelType()) {
            case MachineLearning:
                modelImportService.saveMachineLearningModel(input.getFilename());
                break;
            case DeepLearning:
                modelImportService.saveDeepLearningModel(input.getName(), input.getFilename());
                break;
        }

        return success();
    }

    public static class Input extends AbstractApiInput {

        @Check(name = "主键id", require = true)
        private String filename;

        @Check(name = "主键id", require = true)
        private ModelTypeEnum modelType;

        @Check(name = "模型名称")
        private String name;

        @Override
        public void checkAndStandardize() throws StatusCodeWithException {
            super.checkAndStandardize();

            if (ModelTypeEnum.DeepLearning.equals(modelType) && StringUtil.isEmpty(name)) {
                throw new StatusCodeWithException("模型名称不能为空！", StatusCode.PARAMETER_VALUE_INVALID);
            }
        }

        public String getFilename() {
            return filename;
        }

        public void setFilename(String filename) {
            this.filename = filename;
        }

        public ModelTypeEnum getModelType() {
            return modelType;
        }

        public void setModelType(ModelTypeEnum modelType) {
            this.modelType = modelType;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}
