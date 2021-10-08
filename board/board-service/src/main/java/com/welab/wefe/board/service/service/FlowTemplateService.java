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

package com.welab.wefe.board.service.service;

import com.welab.wefe.board.service.api.project.flow.SaveFlowTemplateApi.Input;
import com.welab.wefe.board.service.database.entity.flow.FlowTemplateMySqlModel;
import com.welab.wefe.board.service.database.repository.FlowTemplateRepository;
import com.welab.wefe.common.data.mysql.Where;
import com.welab.wefe.common.web.CurrentAccount;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author winter.zou
 */
@Service
public class FlowTemplateService extends AbstractService {

    @Autowired
    FlowTemplateRepository flowTemplateRepository;

    public FlowTemplateMySqlModel save(FlowTemplateMySqlModel entity) {
        return flowTemplateRepository.save(entity);
    }

    public List<FlowTemplateMySqlModel> query() {
        return flowTemplateRepository.findAll();
    }

    public FlowTemplateMySqlModel findById(String templateId) {
        Specification<FlowTemplateMySqlModel> where = Where.create().equal("id", templateId)
                .build(FlowTemplateMySqlModel.class);
        return flowTemplateRepository.findOne(where).orElse(null);
    }

    public String addTemplate(Input input) {
        FlowTemplateMySqlModel model = new FlowTemplateMySqlModel();
        model.setDescription(input.getDescription());
        model.setGraph(input.getGraph());
        model.setName(input.getName());
        model.setCreatedBy(CurrentAccount.id());
        flowTemplateRepository.save(model);
        return model.getId();
    }
}
