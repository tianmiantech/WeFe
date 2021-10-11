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

package com.welab.wefe.serving.service.service;

import com.welab.wefe.serving.service.database.serving.entity.ModelMemberBaseModel;
import com.welab.wefe.serving.service.database.serving.repository.ModelMemberBaseRepository;
import com.welab.wefe.serving.service.database.serving.repository.ModelMemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author hunter.zhao
 */
@Service
public class ModelMemberService {
    @Autowired
    private ModelMemberRepository modelMemberRepository;

    @Autowired
    private ModelMemberBaseRepository modelMemberBaseRepository;

    public List<ModelMemberBaseModel> findModelMemberBase(String modelId, String role) {
        return modelMemberBaseRepository.findAllByModelIdAndRole(modelId, role);
    }
}
