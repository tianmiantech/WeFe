package com.welab.wefe.manager.service.api.member;

import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.manager.service.dto.base.BaseInput;
import com.welab.wefe.manager.service.dto.member.MemberOutput;
import com.welab.wefe.manager.service.service.MemberContractService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 更新成员信息（不包括logo）
 *
 * @author aaron.li
 * @Date 2021/6/2
 **/
@Api(path = "member/update_exclude_logo", name = "更新成员信息（不包括logo）")
public class UpdateExcludeLogoApi extends AbstractApi<UpdateExcludeLogoApi.Input, MemberOutput> {
    @Autowired
    private MemberContractService memberContractService;

    @Override
    protected ApiResult<MemberOutput> handle(Input input) throws StatusCodeWithException {
        memberContractService.updateExcludeLogo(input);
        return success();
    }

    public static class Input extends BaseInput {
        @Check(require = true)
        private String id;
        private String name;
        private String mobile;
        private String email;
        private Boolean allowOpenDataSet;
        private Boolean hidden;
        private Boolean freezed;
        private Boolean lostContact;
        private String publicKey;
        private String gatewayUri;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getMobile() {
            return mobile;
        }

        public void setMobile(String mobile) {
            this.mobile = mobile;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public Boolean getAllowOpenDataSet() {
            return allowOpenDataSet;
        }

        public void setAllowOpenDataSet(Boolean allowOpenDataSet) {
            this.allowOpenDataSet = allowOpenDataSet;
        }

        public Boolean getHidden() {
            return hidden;
        }

        public void setHidden(Boolean hidden) {
            this.hidden = hidden;
        }

        public Boolean getFreezed() {
            return freezed;
        }

        public void setFreezed(Boolean freezed) {
            this.freezed = freezed;
        }

        public Boolean getLostContact() {
            return lostContact;
        }

        public void setLostContact(Boolean lostContact) {
            this.lostContact = lostContact;
        }

        public String getPublicKey() {
            return publicKey;
        }

        public void setPublicKey(String publicKey) {
            this.publicKey = publicKey;
        }

        public String getGatewayUri() {
            return gatewayUri;
        }

        public void setGatewayUri(String gatewayUri) {
            this.gatewayUri = gatewayUri;
        }
    }
}
