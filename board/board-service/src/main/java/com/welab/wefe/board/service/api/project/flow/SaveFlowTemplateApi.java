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

package com.welab.wefe.board.service.api.project.flow;

import com.welab.wefe.board.service.service.FlowTemplateService;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.ApiResult;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author winter.zou
 */
@Api(path = "project/flow/template/save", name = "save flow template")
public class SaveFlowTemplateApi extends AbstractApi<SaveFlowTemplateApi.Input, SaveFlowTemplateApi.Output> {

    @Autowired
    private FlowTemplateService flowTemplateService;

    @Override
    protected ApiResult<Output> handle(Input input) throws StatusCodeWithException {
        String templateId = flowTemplateService.addTemplate(input);
        return success(new Output(templateId));
    }

    public static class Output {
        private String templateId;

        public Output(String templateId) {
            this.templateId = templateId;
        }

        public String getTemplateId() {
            return templateId;
        }

        public void setTemplateId(String templateId) {
            this.templateId = templateId;
        }

    }

    public static class Input extends AbstractApiInput {
        @Check(name = "模板名称")
        private String name;

        @Check(name = "模板描述")
        private String description;
        @Check(name = "流程图")
        private String graph;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getGraph() {
            return graph;
        }

        public void setGraph(String graph) {
            this.graph = graph;
        }

    }
}
