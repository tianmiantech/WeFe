/*
 * Copyright 2021 Tianmian Tech. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.welab.wefe.serving.service.service;

import com.welab.wefe.serving.service.database.entity.ModelSqlConfigMySqlModel;
import com.welab.wefe.serving.service.database.repository.ModelSqlConfigRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author hunter.zhao
 */
@Service
public class ModelSqlConfigService {

    @Autowired
    private ModelSqlConfigRepository modelSqlConfigRepository;


    public ModelSqlConfigMySqlModel findById(String modelId) {
        return modelSqlConfigRepository.findOne("modelId", modelId, ModelSqlConfigMySqlModel.class);
    }

    public ModelSqlConfigMySqlModel save(ModelSqlConfigMySqlModel model) {
        return modelSqlConfigRepository.save(model);
    }

    public void saveSqlConfig(String modelId, String dataSourceId, String sqlContext) {
        ModelSqlConfigMySqlModel config = findById(modelId);
        if (config == null) {
            config = new ModelSqlConfigMySqlModel();
        }

        config.setModelId(modelId);
        config.setDataSourceId(dataSourceId);
        config.setSqlContext(sqlContext);
        save(config);
    }

    public void clearSqlConfig(String modelId) {
        ModelSqlConfigMySqlModel config = findById(modelId);
        if (config != null) {
            modelSqlConfigRepository.deleteById(config.getId());
        }
    }
}
