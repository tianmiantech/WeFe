package com.welab.wefe.manager.service.service;

import com.webank.cert.mgr.model.vo.CertVO;
import com.webank.cert.mgr.service.CertOperationService;
import com.webank.cert.toolkit.enums.CertStatusEnums;
import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.data.mongodb.dto.PageOutput;
import com.welab.wefe.common.data.mongodb.entity.manager.CertInfo;
import com.welab.wefe.common.data.mongodb.entity.union.Member;
import com.welab.wefe.common.data.mongodb.entity.union.ext.MemberExtJSON;
import com.welab.wefe.common.data.mongodb.repo.MemberMongoReop;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.manager.service.dto.member.RealNameAuthInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.transaction.Transactional;
import java.util.List;

@Service
public class RealNameAuthAuditService {

    protected static final Logger LOG = LoggerFactory.getLogger(RealNameAuthAuditService.class);

    @Autowired
    protected MemberContractService memberContractService;

    @Autowired
    private CertOperationService certOperationService;

    @Autowired
    private MemberMongoReop memberMongoReop;

    @Transactional
    public void audit(RealNameAuthInput input) throws StatusCodeWithException {
        List<CertInfo> certInfos = certOperationService.findCertList(input.getId(), null, null, null);
        if (certInfos != null && !certInfos.isEmpty()) {
            for (CertInfo certInfo : certInfos) {
                if (certInfo.getStatus() == CertStatusEnums.VALID.getCode()) {
                    // 将之前的证书状态置为无效
                    certOperationService.updateStatusBySerialNumber(certInfo.getSerialNumber(),
                            CertStatusEnums.INVALID.getCode(), "realname auditing,实名认证二次审核");
                }
            }
        }
        MemberExtJSON memberExtJSON = new MemberExtJSON();
        memberExtJSON.setRealNameAuthStatus(input.getRealNameAuthStatus());
        memberExtJSON.setAuditComment(input.getAuditComment());
        if (input.getRealNameAuthStatus() == 2) {
            Member member = memberMongoReop.findMemberId(input.getId());
            if (member == null) {
                throw new StatusCodeWithException(StatusCode.DATA_NOT_FOUND, "成员不存在");
            }
            memberExtJSON = member.getExtJson();
            memberExtJSON.setRealNameAuthStatus(input.getRealNameAuthStatus());
            memberExtJSON.setAuditComment(input.getAuditComment());
            memberExtJSON.setRealNameAuthTime(System.currentTimeMillis());
            // 用户ID
            String memberId = member.getMemberId();
            // 证书请求内容
            String certRequestContent = memberExtJSON.getCertRequestContent();
            // 签发机构的证书ID
            PageOutput<CertInfo> results = certOperationService.findCertList(null, null, true, false, 2, 0, 10);
            if (CollectionUtils.isEmpty(results.getList())) {
                throw new StatusCodeWithException(StatusCode.DATA_NOT_FOUND, "没有签发证书");
            }
            CertInfo issuerCert = results.getList().get(0);
            try {
                // 签发证书
                CertVO cert = certOperationService.createUserCert(issuerCert.getPkId(), memberId, certRequestContent);
                memberExtJSON.setCertStatus(CertStatusEnums.VALID.name());
                // 将证书内容写入
                memberExtJSON.setCertPemContent(cert.getCertContent());
                memberExtJSON.setCertSerialNumber(cert.getSerialNumber());
            } catch (Exception e) {
                throw new StatusCodeWithException(StatusCode.SYSTEM_ERROR, e.getMessage());
            }
        } else if (input.getRealNameAuthStatus() == -1) { // -1认证失败 /0未认证 /1认证中 /2已认证
            memberExtJSON.setUpdatedTime(System.currentTimeMillis());
            memberExtJSON.setCertStatus(CertStatusEnums.INVALID.name());
        }
        memberContractService.updateExtJson(input.getId(), memberExtJSON);

    }

}
