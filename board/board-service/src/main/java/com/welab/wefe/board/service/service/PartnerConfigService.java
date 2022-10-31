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
package com.welab.wefe.board.service.service;

import com.welab.wefe.board.service.api.partner_config.AddApi;
import com.welab.wefe.board.service.api.partner_config.QueryApi;
import com.welab.wefe.board.service.database.entity.PartnerConfigMysqlModel;
import com.welab.wefe.board.service.database.repository.PartnerConfigRepository;
import com.welab.wefe.board.service.dto.base.PagingOutput;
import com.welab.wefe.board.service.dto.entity.PartnerConfigOutputModel;
import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.data.mysql.Where;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.web.CurrentAccount;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author zane.luo
 */
@Service
public class PartnerConfigService extends AbstractService {
    @Autowired
    private PartnerConfigRepository partnerConfigRepository;

    @Autowired
    public GatewayService gatewayService;

    public String add(AddApi.Input input) throws StatusCodeWithException {
        if (!CurrentAccount.isAdmin()) {
            StatusCode.PERMISSION_DENIED.throwException("仅管理员可操作");
        }

        PartnerConfigMysqlModel byMemberId = partnerConfigRepository.findByMemberId(input.memberId);
        if (byMemberId != null) {
            StatusCode.DATA_EXISTED.throwException("该成员的配置项已存在，不能重复添加。");
        }
        gatewayService.isValidGatewayUri(input.gatewayAddress);

        PartnerConfigMysqlModel model = new PartnerConfigMysqlModel();
        model.setGatewayAddress(input.gatewayAddress);
        model.setMemberId(input.memberId);

        partnerConfigRepository.save(model);

        // 通知网关更新缓存
        gatewayService.refreshPartnerConfigCache();

        return model.getId();
    }

    public void delete(String id) throws StatusCodeWithException {
        if (!CurrentAccount.isAdmin()) {
            StatusCode.PERMISSION_DENIED.throwException("仅管理员可操作");
        }

        partnerConfigRepository.deleteById(id);

        // 通知网关更新缓存
        gatewayService.refreshPartnerConfigCache();
    }

    public PagingOutput<PartnerConfigOutputModel> query(QueryApi.Input input) {

        return partnerConfigRepository.paging(
                Where
                        .create()
                        .contains("gatewayAddress", input.gatewayAddress)
                        .in("memberId", input.memberIdList)
                        .build(),
                input,
                PartnerConfigOutputModel.class
        );
    }
}
