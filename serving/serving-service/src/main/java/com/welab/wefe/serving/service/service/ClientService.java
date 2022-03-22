/*
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

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.data.mysql.Where;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.util.StringUtil;
import com.welab.wefe.common.web.util.ModelMapper;
import com.welab.wefe.serving.service.api.client.QueryClientApi;
import com.welab.wefe.serving.service.api.client.QueryClientListApi;
import com.welab.wefe.serving.service.api.client.SaveClientApi;
import com.welab.wefe.serving.service.api.client.UpdateApi;
import com.welab.wefe.serving.service.database.serving.entity.ClientMysqlModel;
import com.welab.wefe.serving.service.database.serving.entity.ClientServiceMysqlModel;
import com.welab.wefe.serving.service.database.serving.repository.ClientRepository;
import com.welab.wefe.serving.service.database.serving.repository.ClientServiceRepository;
import com.welab.wefe.serving.service.dto.PagingOutput;
import com.welab.wefe.serving.service.enums.ClientStatusEnum;

@Service
public class ClientService {


    @Autowired
    private ClientRepository clientRepository;
    @Autowired
    private ClientServiceRepository clientServiceRepository;


    public void save(SaveClientApi.Input input) throws StatusCodeWithException {

        ClientMysqlModel clientMysqlModel = queryByCode(input.getCode());
        if (clientMysqlModel != null) {
            throw new StatusCodeWithException(StatusCode.PRIMARY_KEY_CONFLICT, input.getCode(), "code");
        }

        ClientMysqlModel clientMysqlModel1 = queryByClientName(input.getName());
        if (null != clientMysqlModel1) {
            throw new StatusCodeWithException(StatusCode.CLIENT_NAME_EXIST);
        }


        ClientMysqlModel model = clientRepository.findOne("id", input.getId(), ClientMysqlModel.class);

        if (null == model) {
            model = new ClientMysqlModel();
        }
        model.setName(input.getName());
        model.setEmail(input.getEmail());
        model.setRemark(input.getRemark());
        model.setPubKey(input.getPubKey());
        model.setCreatedBy(input.getCreatedBy());
        model.setCode(input.getCode());
        model.setIpAdd(input.getIpAdd());

        clientRepository.save(model);
    }

    public void update(UpdateApi.Input input) throws StatusCodeWithException {
        ClientMysqlModel model = clientRepository.findOne("id", input.getId(), ClientMysqlModel.class);

        if (null == model) {
            throw new StatusCodeWithException(StatusCode.DATA_NOT_FOUND);
        }
        model.setName(input.getName());
        model.setEmail(input.getEmail());
        model.setRemark(input.getRemark());

        if (StringUtil.isNotEmpty(input.getPubKey()) && !"changeStatus".equals(input.getPubKey()) && !input.getPubKey().contains("****")) {
            model.setPubKey(input.getPubKey());
        }
        model.setIpAdd(input.getIpAdd());
        model.setUpdatedBy(input.getUpdatedBy());
        model.setStatus(input.getStatus());

        clientRepository.save(model);


        // 客户信息变动时，客户服务表中的字段也更新
        Specification<ClientServiceMysqlModel> where = Where.create()
                .equal("clientId", input.getId())
                .build(ClientServiceMysqlModel.class);

        List<ClientServiceMysqlModel> all = clientServiceRepository.findAll(where);
        List<ClientServiceMysqlModel> collect = all.stream().map(x -> {
            x.setIpAdd(input.getIpAdd());
            x.setClientName(input.getName());
            return x;
        }).collect(Collectors.toList());

        clientServiceRepository.saveAll(collect);

    }


    /**
     * Paging query
     */
    public PagingOutput<QueryClientListApi.Output> queryList(QueryClientListApi.Input input) {

        Specification<ClientMysqlModel> where = Where
                .create()
                .contains("name", input.getClientName())
                .betweenAndDate("createdTime", input.getStartTime() == null ? null : input.getStartTime(),
                        input.getEndTime() == null ? null : input.getEndTime())
//                .equal("status", ClientStatusEnum.NORMAL.getValue())
                .build(ClientMysqlModel.class);

        PagingOutput<ClientMysqlModel> page = clientRepository.paging(where, input);

        List<QueryClientListApi.Output> list = page
                .getList()
                .stream()
                .map(x -> ModelMapper.map(x, QueryClientListApi.Output.class))
                .peek(x -> x.setPubKey(x.getPubKey().substring(0, 4) + "*****" + x.getPubKey().substring(x.getPubKey().length() - 4)))
                .collect(Collectors.toList());

        return PagingOutput.of(
                page.getTotal(),
                list
        );
    }


    public ClientMysqlModel queryByCode(String code) {
        Specification<ClientMysqlModel> where = Where.create()
                .equal("code", code)
                .build(ClientMysqlModel.class);
        return clientRepository.findOne(where).orElse(null);
    }

    public ClientServiceMysqlModel queryByServiceIdAndClientId(String serviceId, String clientId) {
        Specification<ClientServiceMysqlModel> where = Where.create().equal("serviceId", serviceId).equal("clientId", clientId)
                .build(ClientServiceMysqlModel.class);
        return clientServiceRepository.findOne(where).orElse(null);
    }

    public ClientMysqlModel queryByClientId(String id) {
        ClientMysqlModel model = clientRepository.findOne("id", id, ClientMysqlModel.class);
        return model;
    }
    
    public ClientMysqlModel queryByClientName(String name) {
        return clientRepository.findOne("name", name, ClientMysqlModel.class);
    }

    public QueryClientApi.Output queryById(String id) {
        ClientMysqlModel model = clientRepository.findOne("id", id, ClientMysqlModel.class);
        model.setPubKey(model.getPubKey().substring(0, 4) + "*****" + model.getPubKey().substring(model.getPubKey().length() - 4));
        return ModelMapper.map(model, QueryClientApi.Output.class);
    }

    public QueryClientApi.Output queryByName(String name) {
        ClientMysqlModel model = clientRepository.findOne("name", name, ClientMysqlModel.class);
        model.setPubKey(model.getPubKey().substring(0, 4) + "*****" + model.getPubKey().substring(model.getPubKey().length() - 4));
        return ModelMapper.map(model, QueryClientApi.Output.class);
    }

    public void detele(String id) {
        ClientMysqlModel model = clientRepository.findOne("id", id, ClientMysqlModel.class);
        model.setStatus(ClientStatusEnum.DELETED.getValue());
        model.setUpdatedTime(new Date());
        clientRepository.save(model);
    }


}
