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

import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.serving.service.enums.ServiceTypeEnum;
import com.welab.wefe.serving.service.service.model.ModelImportService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author hunter.zhao
 * @date 2022/3/8
 */
@Api(path = "model/import", name = "导入模型文件", desc = "导入模型文件")
public class ImportApi extends AbstractApi<ImportApi.Input, ImportApi.Output> {
    @Autowired
    private ModelImportService modelImportService;


    @Override
    protected ApiResult<Output> handle(Input input) throws Exception {
        String id = "";
        ServiceTypeEnum type = ServiceTypeEnum.getType(input.getServiceType());
        switch (type) {
            case MachineLearning:
                id = modelImportService.saveMachineLearningModel(input.getName(), input.getFilename());
                break;
            case DeepLearning:
                id = modelImportService.saveDeepLearningModel(input.getName(), input.getFilename());
                break;
        }
        Output output = new Output();
        output.setId(id);
        return success(output);
    }

    public static class Output {
        private String id;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

    }

    public static class Input extends AbstractApiInput {

        @Check(name = "主键id", require = true)
        private String filename;

        @Check(name = "服务类型", require = true)
        private int serviceType;

        @Check(name = "模型名称 / 服务名称", require = true)
        private String name;


        public String getFilename() {
            return filename;
        }

        public void setFilename(String filename) {
            this.filename = filename;
        }

        public int getServiceType() {
            return serviceType;
        }

        public void setServiceType(int serviceType) {
            this.serviceType = serviceType;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }


    }

}
