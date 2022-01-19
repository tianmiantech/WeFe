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

import java.sql.SQLIntegrityConstraintViolationException;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.data.mysql.Where;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.serving.service.api.client.QueryClientApi;
import com.welab.wefe.serving.service.api.client.QueryClientListApi;
import com.welab.wefe.serving.service.api.client.SaveClientApi;
import com.welab.wefe.serving.service.database.serving.entity.ClientMysqlModel;
import com.welab.wefe.serving.service.database.serving.entity.ClientServiceMysqlModel;
import com.welab.wefe.serving.service.database.serving.repository.ClientRepository;
import com.welab.wefe.serving.service.database.serving.repository.ClientServiceRepository;
import com.welab.wefe.serving.service.dto.PagingOutput;
import com.welab.wefe.serving.service.enums.ClientStatusEnum;
import com.welab.wefe.serving.service.utils.ModelMapper;

@Service
public class ClientService {


    @Autowired
    private ClientRepository clientRepository;
    @Autowired
    private ClientServiceRepository clientServiceRepository;


    public void save(SaveClientApi.Input input) throws SQLIntegrityConstraintViolationException, StatusCodeWithException {

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
        model.setStatus(input.getStatus() == null ? ClientStatusEnum.NORMAL.getValue() : input.getStatus());

        String pattern = "(^,+)|(,+$)";
        Pattern r = Pattern.compile(pattern);
        Matcher m = r.matcher(input.getIpAdd());
        if (m.find()) {
            String ip = m.replaceAll("");
            model.setIpAdd(ip);
        } else {
            throw new StatusCodeWithException(StatusCode.IP_ADDRESS_FORMAT_ERROR);
        }

        clientRepository.save(model);

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
                .equal("status", ClientStatusEnum.NORMAL.getValue())
                .build(ClientMysqlModel.class);

        PagingOutput<ClientMysqlModel> page = clientRepository.paging(where, input);

        List<QueryClientListApi.Output> list = page
                .getList()
                .stream()
                // .filter(x -> member.contains(x.getModelId()))
                .map(x -> ModelMapper.map(x, QueryClientListApi.Output.class))
                .collect(Collectors.toList());

        return PagingOutput.of(
                page.getTotal(),
                list
        );
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
	
	public QueryClientApi.Output queryById(String id) {
		ClientMysqlModel model = clientRepository.findOne("id", id, ClientMysqlModel.class);
		return ModelMapper.map(model, QueryClientApi.Output.class);
	}

	public QueryClientApi.Output queryByName(String name) {
		ClientMysqlModel model = clientRepository.findOne("name", name, ClientMysqlModel.class);
		return ModelMapper.map(model, QueryClientApi.Output.class);
	}

	public void detele(String id) {
		ClientMysqlModel model = clientRepository.findOne("id", id, ClientMysqlModel.class);
		model.setStatus(ClientStatusEnum.DELETED.getValue());
		model.setUpdatedTime(new Date());
		clientRepository.save(model);
	}

}
