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
package com.welab.wefe.serving.service.service.model;

import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.util.AESUtil;
import com.welab.wefe.common.util.FileUtil;
import com.welab.wefe.common.util.JObject;
import com.welab.wefe.common.util.RSAUtil;
import com.welab.wefe.common.web.CurrentAccount;
import com.welab.wefe.common.web.util.ModelMapper;
import com.welab.wefe.common.wefe.enums.Algorithm;
import com.welab.wefe.common.wefe.enums.FederatedLearningType;
import com.welab.wefe.common.wefe.enums.JobMemberRole;
import com.welab.wefe.serving.service.api.model.SaveModelApi;
import com.welab.wefe.serving.service.database.entity.TableModelMySqlModel;
import com.welab.wefe.serving.service.database.repository.TableModelRepository;
import com.welab.wefe.serving.service.dto.MemberParams;
import com.welab.wefe.serving.service.enums.ServiceTypeEnum;
import com.welab.wefe.serving.service.service.CacheObjects;
import com.welab.wefe.serving.service.service.ModelService;
import com.welab.wefe.serving.service.utils.ServingFileUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;

/**
 * @author hunter.zhao
 * @date 2022/3/8
 */
@Service
public class ModelImportService {


    @Autowired
    private ModelService modelService;

    @Autowired
    private TableModelRepository modelRepository;

    @Transactional(rollbackFor = Exception.class)
    public String saveMachineLearningModel(String name, String filename) throws StatusCodeWithException {
        try {
            List<String> jsonStr = parseFileToList(filename);
            String aesKeyCiphertext = jsonStr.get(0);
            String modelCiphertext = jsonStr.get(1);

            String aesKey = decryptAesKey(aesKeyCiphertext);
            JObject jObject = decryptModel(modelCiphertext, aesKey).append("name", name);

            SaveModelApi.Input modelContent = buildModelParam(jObject);
            return modelService.save(modelContent);
        } catch (Exception e) {
            e.printStackTrace();
            throw new StatusCodeWithException("导入模型失败,确认是否已加入联邦且未更改联邦密钥!", StatusCode.FILE_IO_ERROR);
        }
    }

    private SaveModelApi.Input buildModelParam(JObject jObject) {

        SaveModelApi.Input modelContent = new SaveModelApi.Input();
        modelContent.setServiceId(jObject.getString("modelId"));
        modelContent.setName(jObject.getString("name"));
        modelContent.setMyRole(extractMyRole(jObject));
        modelContent.setFlType(extractFlType(jObject));
        modelContent.setAlgorithm(extractAlgorithm(jObject));
        modelContent.setModelParam(jObject.getString("modelParam"));
        modelContent.setMemberParams(extractMemberParams(jObject));
        modelContent.setScoresDistribution(jObject.getString("scoresDistribution"));
        return modelContent;
    }

    private JobMemberRole extractMyRole(JObject jObject) {
        return JobMemberRole.promoter;
    }

    private JObject decryptModel(String modelCiphertext, String aesKey) {
        JObject jObject = JObject.create(AESUtil.decrypt(modelCiphertext, aesKey));
        return jObject;
    }

    private String decryptAesKey(String aesKeyCiphertext) throws Exception {
        return RSAUtil.decryptByPrivateKey(aesKeyCiphertext, CacheObjects.getRsaPrivateKey());
    }

    private List<String> parseFileToList(String filename) throws IOException {
        String path = ServingFileUtil
                .getBaseDir(ServingFileUtil.FileType.MachineLearningModelFile)
                .resolve(filename).toString();
        List<String> jsonStr = FileUtil.readAllForLine(path, "UTF-8");
        return jsonStr;
    }


    private Algorithm extractAlgorithm(JObject jobj) {
        return Algorithm.valueOf(jobj.getString("algorithm"));
    }

    private FederatedLearningType extractFlType(JObject jobj) {
        return FederatedLearningType.valueOf(jobj.getString("flType"));
    }


    private List<MemberParams> extractMemberParams(JObject jobj) {
        List<JObject> m = jobj.getJSONList("memberParams");
        return ModelMapper.maps(m, MemberParams.class);
    }


    public String saveDeepLearningModel(String name, String filename) throws StatusCodeWithException {

        TableModelMySqlModel model = modelRepository.findOne("name", name, TableModelMySqlModel.class);
        if (model != null) {
            throw new StatusCodeWithException("该模型名称已存在，请更改后再尝试提交！", StatusCode.PARAMETER_VALUE_INVALID);
        }
        String path = ServingFileUtil
                .getBaseDir(ServingFileUtil.FileType.DeepLearningModelFile).toString();

        model = new TableModelMySqlModel();
        ServingFileUtil.DeepLearningModelFile.renameZipFile(filename, model.getId());

        model.setSourcePath(path);
        model.setFilename(model.getId() + ".zip");
        model.setUseCount(0);
        model.setName(name);
        model.setServiceType(ServiceTypeEnum.DeepLearning.getCode());
        model.setUrl("predict/deep_learning/" + model.getId());
        model.setCreatedBy(CurrentAccount.get().getId());

        modelRepository.save(model);
        return model.getId();
    }

}
