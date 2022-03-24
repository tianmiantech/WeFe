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
import com.welab.wefe.common.data.mysql.Where;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.util.FileUtil;
import com.welab.wefe.common.util.JObject;
import com.welab.wefe.common.web.util.ModelMapper;
import com.welab.wefe.common.wefe.enums.Algorithm;
import com.welab.wefe.common.wefe.enums.FederatedLearningType;
import com.welab.wefe.common.wefe.enums.PredictFeatureDataSource;
import com.welab.wefe.serving.service.database.serving.entity.DeepLearningModelMySqlModel;
import com.welab.wefe.serving.service.database.serving.entity.MemberMySqlModel;
import com.welab.wefe.serving.service.database.serving.entity.ModelMemberMySqlModel;
import com.welab.wefe.serving.service.database.serving.entity.ModelMySqlModel;
import com.welab.wefe.serving.service.database.serving.repository.DeepLearningModelRepository;
import com.welab.wefe.serving.service.database.serving.repository.MemberRepository;
import com.welab.wefe.serving.service.database.serving.repository.ModelMemberRepository;
import com.welab.wefe.serving.service.database.serving.repository.ModelRepository;
import com.welab.wefe.serving.service.dto.MemberParams;
import com.welab.wefe.serving.service.utils.ServingFileUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author hunter.zhao
 * @date 2022/3/8
 */
@Service
public class ModelImportService {

    @Autowired
    private ModelRepository modelRepository;

    @Autowired
    private DeepLearningModelRepository deepLearningModelRepository;

    @Autowired
    private ModelMemberRepository modelMemberRepository;


    @Autowired
    private MemberRepository memberRepository;


    @Transactional(rollbackFor = Exception.class)
    public void saveMachineLearningModel(String filename) throws StatusCodeWithException {
        try {
            File jsonFile = ServingFileUtil
                    .getBaseDir(ServingFileUtil.FileType.MachineLearningModelFile)
                    .resolve(filename).toFile();
            String jsonStr = FileUtil.readAllText(jsonFile);
            JObject jObject = JObject.create(jsonStr);

            String modelId = getModelId(jObject);
            List<MemberParams> memberParams = getMemberParams(jObject);


            ModelMySqlModel model = modelRepository.findOne("modelId", modelId, ModelMySqlModel.class);

            if (model == null) {
                model = new ModelMySqlModel();
            }

            Specification<ModelMemberMySqlModel> where = Where.
                    create().equal("modelId", modelId)
                    .build(ModelMemberMySqlModel.class);

            List<ModelMemberMySqlModel> modelList = modelMemberRepository.findAll(where);
            modelMemberRepository.deleteAll(modelList);


            /**
             * Model member information
             */
            List<ModelMemberMySqlModel> list = new ArrayList<>();
            memberParams.forEach(x -> {
                ModelMemberMySqlModel member = new ModelMemberMySqlModel();
                member.setModelId(modelId);
                member.setMemberId(x.getMemberId());
                member.setRole(x.getRole());
                list.add(member);
            });

            modelMemberRepository.saveAll(list);

            /**
             * Member basic information
             */
            List<MemberMySqlModel> members = new ArrayList<>();
            for (MemberParams param : memberParams) {

                MemberMySqlModel member = memberRepository.findOne("memberId", param.getMemberId(), MemberMySqlModel.class);
                if (member == null) {
                    member = new MemberMySqlModel();
                }

                member.setMemberId(param.getMemberId());
                member.setName(param.getName());
                member.setPublicKey(param.getPublicKey());
                members.add(member);
            }

            memberRepository.saveAll(members);

            model.setModelId(modelId);
            model.setAlgorithm(getAlgorithm(jObject));
            model.setFlType(getFlType(jObject));
            model.setFeatureSource(PredictFeatureDataSource.api);
            model.setModelParam(getModelParam(jObject));
            model.setEnable(false);
            modelRepository.save(model);
        } catch (IOException e) {
            e.printStackTrace();
            throw new StatusCodeWithException("导入模型失败！error: " + e.getMessage(), StatusCode.FILE_IO_ERROR);
        }
    }


    private String getModelId(JObject jobj) {
        return jobj.getString("modelId");
    }

    private Algorithm getAlgorithm(JObject jobj) {
        return Algorithm.valueOf(jobj.getString("algorithm"));
    }


    private FederatedLearningType getFlType(JObject jobj) {
        return FederatedLearningType.valueOf(jobj.getString("flType"));
    }

    private String getModelParam(JObject jobj) {
        return jobj.getString("modelParam");
    }

    private List<MemberParams> getMemberParams(JObject jobj) {
        List<JObject> m = jobj.getJSONList("memberParams");
        return ModelMapper.maps(m, MemberParams.class);
    }


    public void saveDeepLearningModel(String name, String filename) throws StatusCodeWithException {

        DeepLearningModelMySqlModel model = deepLearningModelRepository.findOne("name", name, DeepLearningModelMySqlModel.class);
        if (model != null) {
            throw new StatusCodeWithException("该模型名称已存在，请更改后再尝试提交！", StatusCode.PARAMETER_VALUE_INVALID);
        }
        String path = ServingFileUtil
                .getBaseDir(ServingFileUtil.FileType.DeepLearningModelFile).toString();

        model = new DeepLearningModelMySqlModel();
        ServingFileUtil.DeepLearningModelFile.renameZipFile(filename, model.getId());

        model.setSourcePath(path);
        model.setFilename("test");
        model.setUseCount(0);
        model.setName(name);

        deepLearningModelRepository.save(model);
    }

}
