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

package com.welab.wefe.union.service.service;

import com.alibaba.fastjson.JSON;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.model.GridFSFile;
import com.mongodb.client.gridfs.model.GridFSUploadOptions;
import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.constant.SecretKeyType;
import com.welab.wefe.common.data.mongodb.dto.PageOutput;
import com.welab.wefe.common.data.mongodb.dto.member.MemberAuthQueryOutput;
import com.welab.wefe.common.data.mongodb.dto.member.RealnameAuthInfoQueryOutput;
import com.welab.wefe.common.data.mongodb.entity.union.MemberFileInfo;
import com.welab.wefe.common.data.mongodb.entity.union.UnionNode;
import com.welab.wefe.common.data.mongodb.entity.union.ext.MemberExtJSON;
import com.welab.wefe.common.data.mongodb.entity.union.ext.RealnameAuthFileInfo;
import com.welab.wefe.common.data.mongodb.repo.MemberAuthTypeMongoRepo;
import com.welab.wefe.common.data.mongodb.repo.MemberMongoReop;
import com.welab.wefe.common.data.mongodb.repo.UnionNodeMongoRepo;
import com.welab.wefe.common.data.mongodb.util.QueryBuilder;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.util.DateUtil;
import com.welab.wefe.common.util.JObject;
import com.welab.wefe.common.util.Md5;
import com.welab.wefe.common.util.StringUtil;
import com.welab.wefe.common.web.dto.UploadFileApiOutput;
import com.welab.wefe.common.wefe.enums.FileRurpose;
import com.welab.wefe.union.service.api.member.*;
import com.welab.wefe.union.service.cache.UnionNodeConfigCache;
import com.welab.wefe.union.service.constant.CertStatusEnums;
import com.welab.wefe.union.service.dto.base.BaseInput;
import com.welab.wefe.union.service.dto.member.MemberQueryOutput;
import com.welab.wefe.union.service.entity.Member;
import com.welab.wefe.union.service.service.contract.MemberContractService;
import com.welab.wefe.union.service.service.contract.MemberFileInfoContractService;
import com.welab.wefe.union.service.task.UploadFileSyncToUnionTask;
import com.welab.wefe.union.service.util.FileCheckerUtil;
import com.welab.wefe.union.service.util.MapperUtil;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.content.InputStreamBody;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class MemberService {
    protected final Logger LOG = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private MemberContractService memberContractService;
    @Autowired
    private UnionNodeMongoRepo unionNodeMongoRepo;
    @Autowired
    private GridFsTemplate gridFsTemplate;
    @Autowired
    private GridFSBucket gridFSBucket;
    @Autowired
    private MemberFileInfoContractService memberFileInfoContractService;
    @Autowired
    private MemberMongoReop memberMongoReop;
    @Autowired
    protected MemberAuthTypeMongoRepo memberAuthTypeMongoRepo;

    public void add(AddApi.Input input) throws StatusCodeWithException {
        try {
            Member member = new Member();
            member.setId(input.getId());
            member.setName(input.getName());
            member.setMobile(input.getMobile());
            member.setHidden(input.isHidden() ? 1 : 0);
            member.setFreezed(input.isFreezed() ? 1 : 0);
            member.setLostContact(input.isLostContact() ? 1 : 0);
            member.setEmail(input.getEmail());
            member.setAllowOpenDataSet(input.isAllowOpenDataSet() ? 1 : 0);
            member.setPublicKey(input.getPublicKey());
            member.setGatewayUri(input.getGatewayUri());
            member.setLastActivityTime(System.currentTimeMillis());
            member.setLogo(input.getLogo());
            SecretKeyType secretKeyType = (null == input.getSecretKeyType() ? SecretKeyType.rsa : input.getSecretKeyType());
            MemberExtJSON extJson = new MemberExtJSON();
            extJson.setRealNameAuthStatus(0);
            extJson.setSecretKeyType(secretKeyType);
            extJson.setServingBaseUrl(input.getServingBaseUrl());
            member.setExtJson(JSON.toJSONString(extJson));

            memberContractService.add(member);
        } catch (StatusCodeWithException e) {
            LOG.error("Add member info exception: ", e);
            throw new StatusCodeWithException(e.getMessage(), StatusCode.SYSTEM_ERROR);
        }
    }

    /**
     * 查询所有成员信息
     */
    public List<MemberQueryOutput> queryAll(QueryAllApi.Input input) {
        List<com.welab.wefe.common.data.mongodb.entity.union.Member> memberList = memberMongoReop.find(input.getId());

        return memberList.stream().map(member -> {
            // does not contain logo
            if (!input.getIncludeLogo()) {
                member.setLogo(null);
            }
            return MapperUtil.transferMember(member);
        }).collect(Collectors.toList());
    }

    /**
     * 分页查询
     */
    public PageOutput<MemberQueryOutput> query(QueryApi.Input input) throws StatusCodeWithException {
        try {
            PageOutput<com.welab.wefe.common.data.mongodb.entity.union.Member> page = memberMongoReop.query(
                    input.getPageIndex(),
                    input.getPageSize(),
                    input.getId(),
                    input.getName()
            );

            List<MemberQueryOutput> list = page.getList().stream()
                    .map(MapperUtil::transferMember)
                    .collect(Collectors.toList());

            return new PageOutput<>(
                    page.getPageIndex(),
                    page.getTotal(),
                    page.getPageSize(),
                    page.getTotalPage(),
                    list
            );
        } catch (Exception e) {
            LOG.error("Failed to query member information in pagination:", e);
            throw new StatusCodeWithException(StatusCode.SYSTEM_ERROR, "Failed to query member information in pagination");
        }
    }

    /**
     * 查询成员特定字段
     */
    public Map<String, JObject> queryMap(BaseInput input) {
        List<com.welab.wefe.common.data.mongodb.entity.union.Member> memberList = memberMongoReop.find(null);
        return memberList.stream().collect(
                Collectors.toMap(
                        com.welab.wefe.common.data.mongodb.entity.union.Member::getMemberId,
                        MemberService::apply
                ));
    }

    public List<MemberAuthQueryOutput> queryAllAuthType(BaseInput input) {
        return memberAuthTypeMongoRepo.findList().stream().map(memberAuthType -> {
            MemberAuthQueryOutput memberAuthQueryOutput = new MemberAuthQueryOutput();
            memberAuthQueryOutput.setTypeId(memberAuthType.getTypeId());
            memberAuthQueryOutput.setTypeName(memberAuthType.getTypeName());
            memberAuthQueryOutput.setStatus(memberAuthType.getStatus());
            return memberAuthQueryOutput;
        }).collect(Collectors.toList());
    }

    /**
     * 实名认证
     */
    public void realNameAuth(RealnameAuthApi.Input input) throws StatusCodeWithException {
        MemberExtJSON extJSON = new MemberExtJSON();
        extJSON.setPrincipalName(input.getPrincipalName());
        extJSON.setAuthType(input.getAuthType());
        extJSON.setDescription(input.getDescription());
        extJSON.setCertRequestContent(input.getCertRequestContent());
        extJSON.setCertRequestId(input.getCertRequestId());
        extJSON.setCertStatus(CertStatusEnums.WAIT_VERIFY.name());
        List<RealnameAuthFileInfo> realnameAuthFileInfoList = new ArrayList<>();
        for (String fileId : input.getFileIdList()) {
            RealnameAuthFileInfo realNameAuthFileInfo = new RealnameAuthFileInfo();
            GridFSFile gridFSFile = gridFsTemplate.findOne(new QueryBuilder().append("_id", fileId).build());
            if (gridFSFile == null) {
                throw new StatusCodeWithException(StatusCode.FILE_DOES_NOT_EXIST, fileId);
            }

            realNameAuthFileInfo.setFilename(gridFSFile.getFilename());
            realNameAuthFileInfo.setFileId(fileId);
            realNameAuthFileInfo.setSign(gridFSFile.getMetadata().getString("sign"));
            realnameAuthFileInfoList.add(realNameAuthFileInfo);
        }
        extJSON.setRealnameAuthFileInfoList(realnameAuthFileInfoList);
        extJSON.setRealNameAuthStatus(1);
        memberContractService.updateExtJson(input.curMemberId, extJSON);
    }


    /**
     * 文件上传
     */
    public UploadFileApiOutput fileUpload(FileUploadApi.Input input) throws StatusCodeWithException {
        try {
            if (FileRurpose.RealnameAuth != input.getPurpose()) {
                throw new StatusCodeWithException(StatusCode.INVALID_PARAMETER, "purpose");
            }

            String fileName = input.getFilename();

            // 检查文件是否是支持的文件类型
            try {
                FileCheckerUtil.checkIsAllowFileType(fileName);
            } catch (Exception e) {
                throw new StatusCodeWithException(e.getMessage(), StatusCode.FILE_IO_ERROR);
            }

            String sign = Md5.of(input.getFirstFile().getInputStream());
            String contentType = input.getFirstFile().getContentType();

            Map<String, InputStreamBody> fileStreamBodyMap = buildFileStreamBodyMap(input.files);

            GridFSFile gridFSFile = gridFsTemplate.findOne(new QueryBuilder()
                    .append("metadata.sign", sign)
                    .append("metadata.memberId", input.getCurMemberId())
                    .build()
            );

            String fileId;
            if (gridFSFile == null) {
                GridFSUploadOptions options = new GridFSUploadOptions();
                Document metadata = new Document();
                metadata.append("contentType", contentType);
                metadata.append("sign", sign);
                metadata.append("memberId", input.getCurMemberId());

                options.metadata(metadata);

                fileId = gridFSBucket.uploadFromStream(fileName, input.getFirstFile().getInputStream(), options).toString();

                saveFileInfoToBlockchain(
                        input.getCurMemberId(),
                        fileId,
                        fileName,
                        sign,
                        input.getFirstFile().getSize(),
                        input.getPurpose().name(),
                        input.getFilePublicLevel().name(),
                        input.getDescribe()
                );

                syncDataToOtherUnionNode(input.getCurMemberId(), fileStreamBodyMap);

            } else {
                fileId = gridFSFile.getObjectId().toString();
                return new UploadFileApiOutput(fileId);
            }

            return new UploadFileApiOutput(fileId);
        } catch (StatusCodeWithException e) {
            throw e;
        } catch (Exception e) {
            throw new StatusCodeWithException(e.getMessage(), StatusCode.SYSTEM_ERROR);
        }
    }

    public RealnameAuthInfoQueryOutput queryRealNameAuthInfo(RealnameAuthInfoQueryApi.Input input) throws StatusCodeWithException {
        try {
            com.welab.wefe.common.data.mongodb.entity.union.Member member = memberMongoReop.findMemberId(input.curMemberId);
            if (member == null) {
                throw new StatusCodeWithException("Invalid member_id: " + input.curMemberId, StatusCode.INVALID_MEMBER);
            }
            RealnameAuthInfoQueryOutput realNameAuthInfoQueryOutput = new RealnameAuthInfoQueryOutput();
            realNameAuthInfoQueryOutput.setAuthType(member.getExtJson().getAuthType());
            realNameAuthInfoQueryOutput.setAuditComment(member.getExtJson().getAuditComment());
            realNameAuthInfoQueryOutput.setDescription(member.getExtJson().getDescription());
            realNameAuthInfoQueryOutput.setPrincipalName(member.getExtJson().getPrincipalName());
            realNameAuthInfoQueryOutput.setRealNameAuthStatus(member.getExtJson().getRealNameAuthStatus());
            // 证书相关内容
            realNameAuthInfoQueryOutput.setCertPemContent(member.getExtJson().getCertPemContent());
            realNameAuthInfoQueryOutput.setCertRequestContent(member.getExtJson().getCertRequestContent());
            realNameAuthInfoQueryOutput.setCertRequestId(member.getExtJson().getCertRequestId());
            realNameAuthInfoQueryOutput.setCertSerialNumber(member.getExtJson().getCertSerialNumber());
            realNameAuthInfoQueryOutput.setCertStatus(member.getExtJson().getCertStatus());
            realNameAuthInfoQueryOutput.setMemberGatewayTlsEnable(member.getExtJson().getMemberGatewayTlsEnable());
            if (member.getExtJson().getRealNameAuthStatus() == 2) {
                long realNameAuthTime = member.getExtJson().getRealNameAuthTime();
                String realNameAuthUsefulLife = DateUtil.toStringYYYY_MM_DD(DateUtil.addYears(DateUtil.getDate(realNameAuthTime), 1));
                realNameAuthInfoQueryOutput.setRealNameAuthUsefulLife(realNameAuthUsefulLife);
            }
            List<RealnameAuthInfoQueryOutput.FileInfo> fileInfoList = new ArrayList<>();
            List<RealnameAuthFileInfo> realnameAuthFileInfoList = member.getExtJson().getRealnameAuthFileInfoList();
            if (realnameAuthFileInfoList != null && !realnameAuthFileInfoList.isEmpty()) {
                fileInfoList = realnameAuthFileInfoList
                        .stream()
                        .map(realnameAuthFileInfo -> {
                            RealnameAuthInfoQueryOutput.FileInfo fileInfo = new RealnameAuthInfoQueryOutput.FileInfo();
                            fileInfo.setFilename(realnameAuthFileInfo.getFilename());
                            fileInfo.setFileId(realnameAuthFileInfo.getFileId());
                            return fileInfo;
                        })
                        .collect(Collectors.toList());
            }
            realNameAuthInfoQueryOutput.setFileInfoList(fileInfoList);
            return realNameAuthInfoQueryOutput;
        } catch (Exception e) {
            LOG.error("Failed to query RealNameAuthInfo information in pagination:", e);
            throw new StatusCodeWithException(StatusCode.SYSTEM_ERROR, "Failed to query RealNameAuthInfo information in pagination");
        }
    }

    public void update(UpdateApi.Input input) throws StatusCodeWithException {
        try {
            Member member = putUpdateField(input);
            memberContractService.upsert(member);
        } catch (Exception e) {
            LOG.error("Failed to update member: ", e);
            throw new StatusCodeWithException(e.getMessage(), StatusCode.SYSTEM_ERROR);
        }
    }

    private Member putUpdateField(UpdateApi.Input input) throws StatusCodeWithException {
        List<Member> memberList = memberContractService.queryAll(input.getId());
        Member member = memberList.get(0);

        member.setId(input.getId());
        if (StringUtil.isNotEmpty(input.getName())) {
            member.setName(input.getName());
        }
        if (StringUtil.isNotEmpty(input.getMobile())) {
            member.setMobile(input.getMobile());
        }
        if (null != input.getHidden()) {
            member.setHidden(input.getHidden() ? 1 : 0);
        }
        if (null != input.getFreezed()) {
            member.setFreezed(input.getFreezed() ? 1 : 0);
        }
        if (null != input.getLostContact()) {
            member.setLostContact(input.getLostContact() ? 1 : 0);
        }
        if (StringUtil.isNotEmpty(input.getEmail())) {
            member.setEmail(input.getEmail());
        }
        if (null != input.getAllowOpenDataSet()) {
            member.setAllowOpenDataSet(input.getAllowOpenDataSet() ? 1 : 0);
        }
        if (StringUtil.isNotEmpty(input.getGatewayUri())) {
            member.setGatewayUri(input.getGatewayUri());
        }
        if (StringUtil.isNotEmpty(input.getLogo())) {
            member.setLogo(input.getLogo());
        }

        if (StringUtil.isNotEmpty(input.getServingBaseUrl())) {
            MemberExtJSON memberExtJSON = JObject.parseObject(member.getExtJson(), MemberExtJSON.class);
            memberExtJSON.setServingBaseUrl(input.getServingBaseUrl());
            member.setExtJson(JObject.toJSONString(memberExtJSON));
        }

        member.setUpdatedTime(new Date());
        member.setLastActivityTime(System.currentTimeMillis());
        return member;
    }


    private void saveFileInfoToBlockchain(String memberId, String fileId, String fileName, String fileSign, long fileSize, String purpose, String filePublicLevel, String describe) throws StatusCodeWithException {
        MemberFileInfo memberFileInfo = new MemberFileInfo();
        memberFileInfo.setFileId(fileId);
        memberFileInfo.setMemberId(memberId);
        memberFileInfo.setFileName(fileName);
        memberFileInfo.setFileSign(fileSign);
        memberFileInfo.setFileSize(String.valueOf(fileSize));
        memberFileInfo.setBlockchainNodeId(UnionNodeConfigCache.currentBlockchainNodeId);
        memberFileInfo.setRurpose(purpose);
        memberFileInfo.setFilePublicLevel(filePublicLevel);
        memberFileInfo.setDescribe(describe);
        memberFileInfoContractService.add(memberFileInfo);
    }

    private Map<String, InputStreamBody> buildFileStreamBodyMap(MultiValueMap<String, MultipartFile> files) throws StatusCodeWithException {
        Map<String, InputStreamBody> fileStreamBodyMap = new HashMap<>();
        for (Map.Entry<String, MultipartFile> item : files.toSingleValueMap().entrySet()) {
            try {
                MultipartFile file = item.getValue();
                ContentType contentType = StringUtil.isEmpty(file.getContentType())
                        ? ContentType.DEFAULT_BINARY
                        : ContentType.create(file.getContentType());

                InputStreamBody streamBody = new InputStreamBody(
                        file.getInputStream(),
                        contentType,
                        file.getOriginalFilename()
                );
                fileStreamBodyMap.put(item.getKey(), streamBody);
            } catch (IOException e) {
                LOG.error("File read / write failed", e);
                throw new StatusCodeWithException(StatusCode.FILE_IO_ERROR);
            }
        }
        return fileStreamBodyMap;
    }

    private void syncDataToOtherUnionNode(String memberId, Map<String, InputStreamBody> fileStreamBodyMap) {
        List<UnionNode> unionNodeList = unionNodeMongoRepo.findExcludeCurrentNode(UnionNodeConfigCache.currentBlockchainNodeId);
        for (UnionNode unionNode :
                unionNodeList) {

            new UploadFileSyncToUnionTask(
                    unionNode.getBaseUrl(),
                    "member/file/upload/sync",
                    JObject.create("memberId", memberId),
                    fileStreamBodyMap
            ).start();
        }
    }


    private static JObject apply(com.welab.wefe.common.data.mongodb.entity.union.Member member) {
        return JObject.create()
                .put("name", member.getName())
                .put("hidden", Integer.parseInt(member.getHidden()))
                .put("freezed", Integer.parseInt(member.getFreezed()))
                .put("lostContact", Integer.parseInt(member.getLostContact()));
    }
}
