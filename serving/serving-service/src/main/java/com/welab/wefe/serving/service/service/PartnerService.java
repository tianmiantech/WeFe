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

import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.data.mysql.Where;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.web.util.ModelMapper;
import com.welab.wefe.serving.service.api.member.QueryApi;
import com.welab.wefe.serving.service.api.partner.DetailPartnerApi;
import com.welab.wefe.serving.service.api.partner.QueryPartnerListApi;
import com.welab.wefe.serving.service.api.partner.QueryPartnerListApi.Input;
import com.welab.wefe.serving.service.api.partner.QueryPartnerListApi.Output;
import com.welab.wefe.serving.service.api.partner.SavePartnerApi;
import com.welab.wefe.serving.service.database.entity.ClientMysqlModel;
import com.welab.wefe.serving.service.database.entity.ClientServiceMysqlModel;
import com.welab.wefe.serving.service.database.entity.MemberMySqlModel;
import com.welab.wefe.serving.service.database.entity.PartnerMysqlModel;
import com.welab.wefe.serving.service.database.repository.ClientRepository;
import com.welab.wefe.serving.service.database.repository.ClientServiceRepository;
import com.welab.wefe.serving.service.database.repository.MemberRepository;
import com.welab.wefe.serving.service.database.repository.PartnerRepository;
import com.welab.wefe.serving.service.dto.MemberParams;
import com.welab.wefe.serving.service.dto.PagingOutput;
import com.welab.wefe.serving.service.enums.ClientStatusEnum;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PartnerService {
    @Autowired
    private PartnerRepository partnerRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private ClientServiceRepository clientServiceRepository;

    @Transactional(rollbackFor = Exception.class)
    public void init() {
        partnerRepository.deleteAll();
        List<MemberMySqlModel> members = memberRepository.findAll();
        List<ClientMysqlModel> clients = clientRepository.findAll();

        for (MemberMySqlModel m : members) {
            PartnerMysqlModel model = ModelMapper.map(m, PartnerMysqlModel.class);
            model.setServingBaseUrl(m.getApi());
            model.setId(m.getMemberId());
            model.setIsUnionMember(true);
            model.setStatus(ClientStatusEnum.NORMAL.getValue());
            partnerRepository.save(model);
        }

        for (ClientMysqlModel c : clients) {
            PartnerMysqlModel model = ModelMapper.map(c, PartnerMysqlModel.class);
            model.setStatus(c.getStatus());
//            model.setServingBaseUrl(config.getSERVING_BASE_URL());
            model.setId(UUID.randomUUID().toString().replaceAll("-", ""));
            partnerRepository.save(model);
        }
    }

    public void save(SavePartnerApi.Input input) throws StatusCodeWithException {
        PartnerMysqlModel partnerMysqlModel = null;

        if (StringUtils.isNotBlank(input.getCode())) {
            partnerMysqlModel = queryByCode(input.getCode());
            if (partnerMysqlModel != null) {
                throw new StatusCodeWithException(StatusCode.PRIMARY_KEY_CONFLICT, input.getCode(), "code");
            }
        }

        partnerMysqlModel = queryByPartnerName(input.getName());
        if (null != partnerMysqlModel) {
            throw new StatusCodeWithException(StatusCode.CLIENT_NAME_EXIST);
        }
        
        if (StringUtils.isNotBlank(input.getId())) {
            partnerMysqlModel = partnerRepository.findOne("id", input.getId(), PartnerMysqlModel.class);
        }
        else {
            input.setId(UUID.randomUUID().toString().replaceAll("-", ""));
        }
        if (null == partnerMysqlModel) {
            partnerMysqlModel = new PartnerMysqlModel();
        }
        partnerMysqlModel.setName(input.getName());
        partnerMysqlModel.setEmail(input.getEmail());
        partnerMysqlModel.setRemark(input.getRemark());
        partnerMysqlModel.setServingBaseUrl(input.getServingBaseUrl());
        partnerMysqlModel.setCreatedBy(input.getCreatedBy());
        partnerMysqlModel.setCode(input.getCode());
        partnerMysqlModel.setId(input.getId());
        partnerRepository.save(partnerMysqlModel);
    }

    public PartnerMysqlModel findOne(String partnerId) {
        return partnerRepository.findOne("partnerId", partnerId, PartnerMysqlModel.class);
    }

    /**
     * Paging query
     */
    public PagingOutput<QueryApi.Output> query(QueryApi.Input input) {

        Specification<PartnerMysqlModel> where = Where.create().equal("partnerId", input.getMemberId())
                .contains("name", input.getName()).build(PartnerMysqlModel.class);

        PagingOutput<PartnerMysqlModel> page = partnerRepository.paging(where, input);

        List<QueryApi.Output> list = page.getList().stream().map(x -> ModelMapper.map(x, QueryApi.Output.class))
                .collect(Collectors.toList());

        return PagingOutput.of(page.getTotal(), list);
    }

    public PartnerMysqlModel queryByPartnerName(String name) {
        return partnerRepository.findOne("name", name, PartnerMysqlModel.class);
    }

    public DetailPartnerApi.Output queryById(String id) {
        PartnerMysqlModel model = partnerRepository.findOne("id", id, PartnerMysqlModel.class);
        return ModelMapper.map(model, DetailPartnerApi.Output.class);
    }

    public DetailPartnerApi.Output queryByName(String name) {
        PartnerMysqlModel model = queryByPartnerName(name);
        return ModelMapper.map(model, DetailPartnerApi.Output.class);
    }

    public void detele(String id) {
        PartnerMysqlModel model = partnerRepository.findOne("id", id, PartnerMysqlModel.class);
        model.setStatus(ClientStatusEnum.DELETED.getValue());
        model.setUpdatedTime(new Date());
        partnerRepository.save(model);
    }

    public PartnerMysqlModel queryByCode(String code) {
        Specification<PartnerMysqlModel> where = Where.create().equal("code", code).build(PartnerMysqlModel.class);
        return partnerRepository.findOne(where).orElse(null);
    }

    public PagingOutput<Output> queryList(Input input) {
        Specification<PartnerMysqlModel> where = Where.create().contains("name", input.getPartnerName())
                .build(PartnerMysqlModel.class);

        PagingOutput<PartnerMysqlModel> page = partnerRepository.paging(where, input);

        List<QueryPartnerListApi.Output> list = page.getList().stream()
                .map(x -> ModelMapper.map(x, QueryPartnerListApi.Output.class)).collect(Collectors.toList());

        return PagingOutput.of(page.getTotal(), list);
    }

    public void update(com.welab.wefe.serving.service.api.partner.UpdateApi.Input input)
            throws StatusCodeWithException {
        PartnerMysqlModel model = partnerRepository.findOne("id", input.getId(), PartnerMysqlModel.class);
        if (null == model) {
            throw new StatusCodeWithException(StatusCode.DATA_NOT_FOUND);
        }
        model.setName(input.getName());
        model.setEmail(input.getEmail());
        model.setUpdatedBy(input.getUpdatedBy());
        model.setUpdatedTime(new Date());
        model.setStatus(input.getStatus());
        model.setIsUnionMember(input.getIsUnionMember());
        model.setServingBaseUrl(input.getServingBaseUrl());
        model.setRemark(input.getRemark());
        partnerRepository.save(model);
        // 客户信息变动时，客户服务表中的字段也更新
        Specification<ClientServiceMysqlModel> where = Where.create().equal("clientId", input.getId())
                .build(ClientServiceMysqlModel.class);

        List<ClientServiceMysqlModel> all = clientServiceRepository.findAll(where);
        if (CollectionUtils.isNotEmpty(all)) {
            List<ClientServiceMysqlModel> collect = all.stream().map(x -> {
                x.setClientName(input.getName());
                return x;
            }).collect(Collectors.toList());

            clientServiceRepository.saveAll(collect);
        }
    }

    public ClientServiceMysqlModel queryByServiceIdAndClientId(String serviceId, String clientId) {
        Specification<ClientServiceMysqlModel> where = Where.create().equal("serviceId", serviceId)
                .equal("clientId", clientId).build(ClientServiceMysqlModel.class);
        return clientServiceRepository.findOne(where).orElse(null);
    }

    public ClientMysqlModel queryByClientName(String name) {
        return clientRepository.findOne("name", name, ClientMysqlModel.class);
    }


    public void save(List<MemberParams> memberParams) {
        memberParams.forEach(x -> {
            SavePartnerApi.Input partner = new SavePartnerApi.Input();
            partner.setIsUnionMember(true);
            partner.setId(x.getMemberId());
            partner.setName(x.getName());
            partner.setCode(x.getMemberId());
            partner.setServingBaseUrl("");
            partner.setEmail("");
            try {
                save(partner);
            } catch (StatusCodeWithException e) {
                e.printStackTrace();
            }
        });
    }
}
