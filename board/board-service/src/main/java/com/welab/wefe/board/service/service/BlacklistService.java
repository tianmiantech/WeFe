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

import com.alibaba.fastjson.JSONObject;
import com.welab.wefe.board.service.api.blacklist.AddApi;
import com.welab.wefe.board.service.api.blacklist.BlacklistApi;
import com.welab.wefe.board.service.api.blacklist.BlacklistMemberApi;
import com.welab.wefe.board.service.api.blacklist.DeleteApi;
import com.welab.wefe.board.service.database.entity.BlacklistMysqlModel;
import com.welab.wefe.board.service.database.repository.BlacklistRepository;
import com.welab.wefe.board.service.dto.base.PagingOutput;
import com.welab.wefe.board.service.dto.entity.BlacklistOutputModel;
import com.welab.wefe.board.service.dto.entity.MemberOutputModel;
import com.welab.wefe.common.data.mysql.Where;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.util.JObject;
import com.welab.wefe.common.web.CurrentAccount;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author lonnie
 */
@Service
public class BlacklistService extends AbstractService {

    @Autowired
    private BlacklistRepository blacklistRepository;

    public PagingOutput<BlacklistOutputModel> list(BlacklistApi.Input input) throws StatusCodeWithException {

        List<BlacklistOutputModel> resultList = new ArrayList<>();

        Specification<BlacklistMysqlModel> where = Where
                .create()
                .build(BlacklistMysqlModel.class);

        PagingOutput<BlacklistMysqlModel> page = blacklistRepository.paging(where, input);
        for (BlacklistMysqlModel model : page.getList()) {
            JSONObject memberObj = unionService.queryMemberById(model.getBlacklistMemberId());
            List<JObject> list = JObject.create(memberObj).getJSONList("data.list");

            for (JObject obj : list) {
                String name = obj.getString("name");
                String id = obj.getString("id");
                BlacklistOutputModel outModel = new BlacklistOutputModel();
                outModel.setId(model.getId());
                outModel.setMemberId(id);
                outModel.setMemberName(name);
                outModel.setRemark(model.getRemark());
                outModel.setCreatedTime(model.getCreatedTime());
                outModel.setCreatedBy(model.getMemberId());
                resultList.add(outModel);
            }
        }

        return PagingOutput.of(
                page.getTotal(),
                resultList
        );
    }

    /**
     * Add blacklist
     */
    public void add(AddApi.Input input) {

        List<BlacklistMysqlModel> list = new ArrayList<>();
        if (input.getMemberIds() != null) {
            for (String id : input.getMemberIds()) {
                BlacklistMysqlModel model = new BlacklistMysqlModel();
                model.setMemberId(CacheObjects.getMemberId());
                model.setBlacklistMemberId(id);
                model.setRemark(input.getRemark());
                model.setCreatedTime(new Date());
                model.setCreatedBy(CurrentAccount.id());
                list.add(model);
            }
        }

        blacklistRepository.saveAll(list);

        CacheObjects.refreshMemberBlacklist();
        // Notify gateway to update blacklist cache
        gatewayService.refreshMemberBlacklistCache();
    }

    public void deleteFromBlacklist(DeleteApi.Input input) {
        blacklistRepository.deleteById(input.getId());
        CacheObjects.refreshMemberBlacklist();
        // Notify gateway to update blacklist cache
        gatewayService.refreshMemberBlacklistCache();
    }

    public PagingOutput<MemberOutputModel> queryBlacklistMember(BlacklistMemberApi.Input input) throws StatusCodeWithException {

        JSONObject memberObj = unionService.queryMemberByPage(input.getPageIndex(), input.getPageSize(), input.getId(), input.getName());
        List<JObject> list = JObject.create(memberObj).getJSONList("data.list");

        List<MemberOutputModel> resultList = new ArrayList<>();
        for (JObject obj : list) {
            MemberOutputModel model = new MemberOutputModel();
            String memberId = obj.getStringByPath("id");
            model.setId(memberId);
            model.setName(obj.getString("name"));
            model.setEmail(obj.getString("email"));
            model.setMobile(obj.getString("mobile"));
            BlacklistMysqlModel blacklistMysqlModel = blacklistRepository.findByBlacklistMemberId(memberId);

            if (blacklistMysqlModel != null || CacheObjects.getMemberId().equals(memberId)) {
                model.setInBlacklist(true);
            }
            resultList.add(model);
        }

        return PagingOutput.of(
                JObject.create(memberObj).getIntegerByPath("data.total", 0),
                resultList
        );
    }

}
