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

package com.welab.wefe.board.service.service.fusion;

import com.welab.wefe.board.service.database.entity.fusion.FieldInfoMySqlModel;
import com.welab.wefe.board.service.database.repository.fusion.FieldInfoRepository;
import com.welab.wefe.board.service.service.AbstractService;
import com.welab.wefe.board.service.util.ModelMapper;
import com.welab.wefe.board.service.util.primarykey.FieldInfo;
import com.welab.wefe.common.data.mysql.Where;
import com.welab.wefe.common.enums.OrderBy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author hunter.zhao
 */
@Service
public class FieldInfoService extends AbstractService {

    @Autowired
    private FieldInfoRepository fieldInfoRepository;

    public List<String> columnList(String businessId) {

        List<FieldInfoMySqlModel> modelList = findByBusinessId(businessId);

        Set<String> fields = new HashSet<>();
        modelList.forEach(x -> {
            fields.addAll(Arrays.asList(x.getColumns().split(",")));
        });

        return fields.stream().collect(Collectors.toList());
    }


    public List<FieldInfo> fieldInfoList(String businessId) {


        List<FieldInfoMySqlModel> modelList = findByBusinessId(businessId);

        List<FieldInfo> models = modelList
                .stream()
                .map(x -> ModelMapper.map(x, FieldInfo.class))
                .collect(Collectors.toList());

        return models;
    }

    private List<FieldInfoMySqlModel> findByBusinessId(String businessId) {
        Specification<FieldInfoMySqlModel> where = Where.
                create().equal("businessId", businessId)
                .orderBy("position", OrderBy.asc)
                .build(FieldInfoMySqlModel.class);

        return fieldInfoRepository.findAll(where);
    }


    public void saveAll(String businessId, List<FieldInfo> fieldInfoList) {
        Specification<FieldInfoMySqlModel> where = Where.
                create().equal("businessId", businessId)
                .build(FieldInfoMySqlModel.class);

        List<FieldInfoMySqlModel> fieldInfoMySqlModels = fieldInfoRepository.findAll(where);
        fieldInfoRepository.deleteAll(fieldInfoMySqlModels);


        /**
         * Model member information
         */
        List<FieldInfoMySqlModel> list = new ArrayList<>();

        for (int i = 0; i < fieldInfoList.size(); i++) {
            FieldInfoMySqlModel fieldInfo = new FieldInfoMySqlModel();
            fieldInfo.setBusinessId(businessId);
            fieldInfo.setColumns(fieldInfoList.get(i).getColumns());
            fieldInfo.setOptions(fieldInfoList.get(i).getOptions());
            fieldInfo.setFristIndex(fieldInfoList.get(i).getFristIndex() == null ? 0 : fieldInfoList.get(i).getFristIndex());
            fieldInfo.setEndIndex(fieldInfoList.get(i).getEndIndex() == null ? 0 : fieldInfoList.get(i).getEndIndex());
            fieldInfo.setPosition(i);
            list.add(fieldInfo);
        }

        fieldInfoRepository.saveAll(list);
    }
}
