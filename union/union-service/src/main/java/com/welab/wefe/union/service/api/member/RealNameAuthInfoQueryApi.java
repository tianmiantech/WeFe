/**
 * Copyright 2021 Tianmian Tech. All Rights Reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.welab.wefe.union.service.api.member;

import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.data.mongodb.dto.member.RealNameAuthInfoQueryOutput;
import com.welab.wefe.common.data.mongodb.entity.union.Member;
import com.welab.wefe.common.data.mongodb.entity.union.ext.RealNameAuthFileInfo;
import com.welab.wefe.common.data.mongodb.repo.MemberMongoReop;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.union.service.dto.base.BaseInput;
import com.welab.wefe.union.service.mapper.MemberMapper;
import org.mapstruct.factory.Mappers;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author yuxin.zhang
 **/
@Api(path = "member/realname/authInfo/query", name = "member/realname/authInfo/query", rsaVerify = true, login = false)
public class RealNameAuthInfoQueryApi extends AbstractApi<RealNameAuthInfoQueryApi.Input, RealNameAuthInfoQueryOutput> {
    @Autowired
    protected MemberMongoReop memberMongoReop;

    protected MemberMapper mMapper = Mappers.getMapper(MemberMapper.class);

    @Override
    protected ApiResult<RealNameAuthInfoQueryOutput> handle(RealNameAuthInfoQueryApi.Input input) throws StatusCodeWithException {
        try {
            Member member = memberMongoReop.findMemberId(input.curMemberId);
            if (member == null) {
                throw new StatusCodeWithException("Invalid member_id: " + input.curMemberId, StatusCode.INVALID_MEMBER);
            }
            RealNameAuthInfoQueryOutput realNameAuthInfoQueryOutput = new RealNameAuthInfoQueryOutput();
            realNameAuthInfoQueryOutput.setAuthType(member.getExtJson().getAuthType());
            realNameAuthInfoQueryOutput.setAuditComment(member.getExtJson().getAuditComment());
            realNameAuthInfoQueryOutput.setDescription(member.getExtJson().getDescription());
            realNameAuthInfoQueryOutput.setPrincipalName(member.getExtJson().getPrincipalName());
            realNameAuthInfoQueryOutput.setRealNameAuthStatus(member.getExtJson().getRealNameAuthStatus());

            List<String> fileIdList = new ArrayList<>();
            List<RealNameAuthFileInfo> realNameAuthFileInfoList = member.getExtJson().getRealNameAuthFileInfoList();
            if(realNameAuthFileInfoList != null && !realNameAuthFileInfoList.isEmpty()){
                fileIdList = realNameAuthFileInfoList
                        .stream()
                        .map(RealNameAuthFileInfo::getFileId)
                        .collect(Collectors.toList());
            }

            realNameAuthInfoQueryOutput.setFileIdList(fileIdList);
            return success(realNameAuthInfoQueryOutput);
        } catch (Exception e) {
            LOG.error("Failed to query RealNameAuthInfo information in pagination:", e);
            throw new StatusCodeWithException(StatusCode.SYSTEM_ERROR, "Failed to query RealNameAuthInfo information in pagination");
        }
    }

    public static class Input extends BaseInput {

    }
}
