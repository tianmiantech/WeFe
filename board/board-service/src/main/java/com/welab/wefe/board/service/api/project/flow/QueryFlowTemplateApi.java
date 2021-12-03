/**
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

package com.welab.wefe.board.service.api.project.flow;

import com.welab.wefe.board.service.api.project.flow.QueryFlowTemplateApi.TemplateListOutput;
import com.welab.wefe.board.service.database.entity.flow.FlowTemplateMySqlModel;
import com.welab.wefe.board.service.service.FlowTemplateService;
import com.welab.wefe.board.service.util.ModelMapper;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.web.api.base.AbstractNoneInputApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiOutput;
import com.welab.wefe.common.web.dto.ApiResult;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author winter.zou
 */
@Api(path = "project/flow/templates", name = "list flow templates")
public class QueryFlowTemplateApi extends AbstractNoneInputApi<TemplateListOutput> {

    @Autowired
    private FlowTemplateService flowTemplateService;

    @Override
    protected ApiResult<TemplateListOutput> handle() throws StatusCodeWithException {
        List<FlowTemplateMySqlModel> models = flowTemplateService.query();

        TemplateListOutput output = new TemplateListOutput();
        List<TemplateOutput> templates = models
                .stream()
                .map(t -> ModelMapper.map(t, TemplateOutput.class))
                .collect(Collectors.toList());

        output.setTemplates(templates);
        return success(output);
    }

    public static class TemplateListOutput extends AbstractApiOutput {
        private List<TemplateOutput> templates;

        public List<TemplateOutput> getTemplates() {
            return templates;
        }

        public void setTemplates(List<TemplateOutput> templates) {
            this.templates = templates;
        }

    }

    public static class TemplateOutput {

        private String id;
        /**
         * template name
         */
        private String name;

        /**
         * template name
         */
        private String description;

        private String enname;
        
        private String federatedLearningType;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getEnname() {
            return enname;
        }

        public void setEnname(String enname) {
            this.enname = enname;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }
    }
}
