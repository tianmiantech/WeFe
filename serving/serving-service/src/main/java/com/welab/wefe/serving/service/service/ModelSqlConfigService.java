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

import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.enums.DatabaseType;
import com.welab.wefe.common.enums.PredictFeatureDataSource;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.serving.service.database.serving.entity.ModelMySqlModel;
import com.welab.wefe.serving.service.database.serving.entity.ModelSqlConfigMySqlModel;
import com.welab.wefe.serving.service.database.serving.repository.ModelRepository;
import com.welab.wefe.serving.service.database.serving.repository.ModelSqlConfigRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author hunter.zhao
 */
@Service
public class ModelSqlConfigService {

    @Autowired
    private ModelRepository modelRepository;

    @Autowired
    private ModelSqlConfigRepository modelSqlConfigRepository;


    public ModelSqlConfigMySqlModel findOne(String modelId) {

        //Find SQL configuration
        return modelSqlConfigRepository.findOne("modelId", modelId, ModelSqlConfigMySqlModel.class);
    }


    @Transactional(rollbackFor = Exception.class)
    public void updateConfig(String modelId,
                             PredictFeatureDataSource featureSource,
                             DatabaseType type,
                             String url,
                             String username,
                             String password,
                             String sqlContext) throws StatusCodeWithException {


        ModelMySqlModel model = modelRepository.findOne("modelId", modelId, ModelMySqlModel.class);

        if (model == null) {
            throw new StatusCodeWithException("modelId error: " + modelId, StatusCode.PARAMETER_VALUE_INVALID);
        }

        model.setFeatureSource(featureSource);

        modelRepository.save(model);

        if (!featureSource.equals(PredictFeatureDataSource.sql)) {

            //Clear SQL configuration
            ModelSqlConfigMySqlModel config = modelSqlConfigRepository.findOne("modelId", modelId, ModelSqlConfigMySqlModel.class);
            if (config != null) {
                modelSqlConfigRepository.deleteById(config.getId());
            }

            return;
        }

        /**
         * Save SQL configuration
         */
        ModelSqlConfigMySqlModel config = modelSqlConfigRepository.findOne("modelId", modelId, ModelSqlConfigMySqlModel.class);

        if (config == null) {
            config = new ModelSqlConfigMySqlModel();
        }

        config.setModelId(modelId);
        config.setType(type);
        config.setUrl(url);
        config.setUsername(username);
        config.setPassword(password);
        config.setSqlContext(sqlContext);


        modelSqlConfigRepository.save(config);
    }

}
