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

package com.welab.wefe.data.fusion.service.service;

import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.data.mysql.Where;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.data.fusion.service.api.partner.AddApi;
import com.welab.wefe.data.fusion.service.api.partner.PagingApi;
import com.welab.wefe.data.fusion.service.api.partner.UpdateApi;
import com.welab.wefe.data.fusion.service.database.entity.PartnerMySqlModel;
import com.welab.wefe.data.fusion.service.database.repository.PartnerRepository;
import com.welab.wefe.data.fusion.service.dto.base.PagingOutput;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author hunter.zhao
 */
@Service
public class PartnerService extends AbstractService {


    @Autowired
    PartnerRepository partnerRepository;


    public PartnerMySqlModel findByPartnerId(String partnerId) {
        return partnerRepository.findOne("partnerId", partnerId, PartnerMySqlModel.class);
    }


    public boolean existByPartnerIdNotEqId(String partnerId, String id) {
        Specification<PartnerMySqlModel> where = Where.create()
                .equal("partnerId", partnerId)
                .notEqual("id", id)
                .build(PartnerMySqlModel.class);
        return partnerRepository.count(where) > 0;
    }

    public void add(AddApi.Input input) throws StatusCodeWithException {
        if (findByPartnerId(input.getPartnerId()) != null) {
            LOG.error("该合作伙伴已存在，请检查后提交！");
            throw new StatusCodeWithException("该合作伙伴已存在，请检查后提交！", StatusCode.DATA_EXISTED);

        }

        PartnerMySqlModel partnerMySqlModel = new PartnerMySqlModel();
        BeanUtils.copyProperties(input, partnerMySqlModel);

        partnerRepository.save(partnerMySqlModel);
    }

    public void update(UpdateApi.Input input) throws StatusCodeWithException {

        PartnerMySqlModel partnerMySqlModel = partnerRepository.findOne("id", input.getId(), PartnerMySqlModel.class);
        if (partnerMySqlModel == null) {
            LOG.error("未找到对应合作伙伴");
            throw new StatusCodeWithException(StatusCode.PARAMETER_VALUE_INVALID, input.getId());
        }

        if (existByPartnerIdNotEqId(input.getPartnerId(), input.getId())) {
            LOG.error("partnerId已存在");
            throw new StatusCodeWithException("该合作伙伴已存在，请检查后提交！", StatusCode.DATA_EXISTED);
        }

        PartnerMySqlModel partner = new PartnerMySqlModel();
        BeanUtils.copyProperties(input, partner);

        partnerRepository.save(partner);
    }

    /**
     * paging query
     */
    public PagingOutput<PartnerMySqlModel> paging(PagingApi.Input input) {
        Specification<PartnerMySqlModel> where = Where.create()
                .equal("partnerId", input.getPartnerId())
                .equal("name", input.getName())
                .build(PartnerMySqlModel.class);


        return partnerRepository.paging(where, input);
    }

    public void delete(String id) {
        partnerRepository.deleteById(id);
    }



    public List<PartnerMySqlModel> list() {
       return  partnerRepository.findAll();
    }
}
