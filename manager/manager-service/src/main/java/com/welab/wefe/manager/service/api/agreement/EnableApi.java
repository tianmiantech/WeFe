package com.welab.wefe.manager.service.api.agreement;

import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.data.mongodb.entity.union.RealnameAuthAgreementTemplate;
import com.welab.wefe.common.data.mongodb.repo.RealnameAuthAgreementTemplateMongoRepo;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiOutput;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.manager.service.dto.agreement.RealnameAuthAgreementTemplateEnableInput;
import com.welab.wefe.manager.service.service.RealnameAuthAgreementTemplateContractService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author yuxin.zhang
 */
@Api(path = "realname/auth/agreement/template/enable", name = "realname_auth_agreement_template_enable", login = false)
public class EnableApi extends AbstractApi<RealnameAuthAgreementTemplateEnableInput, AbstractApiOutput> {

    @Autowired
    private RealnameAuthAgreementTemplateContractService contractService;
    @Autowired
    private RealnameAuthAgreementTemplateMongoRepo realnameAuthAgreementTemplateMongoRepo;

    @Override
    protected ApiResult<AbstractApiOutput> handle(RealnameAuthAgreementTemplateEnableInput input) throws StatusCodeWithException {
        LOG.info("RealnameAuthAgreementTemplate enable handle..");
        try {
            RealnameAuthAgreementTemplate realnameAuthAgreementTemplate = realnameAuthAgreementTemplateMongoRepo.findByEnable(true);
            contractService.enable(realnameAuthAgreementTemplate.getTemplateFileId(), false);
            contractService.enable(input.getTemplateFileId(), true);
        } catch (StatusCodeWithException e) {
            throw new StatusCodeWithException(e.getMessage(), StatusCode.SYSTEM_ERROR);
        }

        return success();
    }

}
