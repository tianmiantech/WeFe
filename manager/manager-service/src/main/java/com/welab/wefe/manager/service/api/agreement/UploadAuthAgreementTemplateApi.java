package com.welab.wefe.manager.service.api.agreement;

import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.model.GridFSFile;
import com.welab.wefe.common.data.mongodb.entity.union.AuthAgreementTemplate;
import com.welab.wefe.common.data.mongodb.repo.AuthAgreementTemplateMongoRepo;
import com.welab.wefe.common.data.mongodb.util.QueryBuilder;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiOutput;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.manager.service.dto.agreement.UploadAuthAgreementTemplateInput;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;

import java.io.IOException;

/**
 * @Description:
 * @author: yuxin.zhang
 * @date: 2021/10/28
 */
@Api(path = "auth/agreement/template/upload", name = "auth_agreement_template_upload", login = false)
public class UploadAuthAgreementTemplateApi extends AbstractApi<UploadAuthAgreementTemplateInput, AbstractApiOutput> {
    @Autowired
    private AuthAgreementTemplateMongoRepo authAgreementTemplateMongoRepo;
    @Autowired
    private GridFSBucket gridFSBucket;
    @Autowired
    private GridFsTemplate gridFsTemplate;

    @Override
    protected ApiResult<AbstractApiOutput> handle(UploadAuthAgreementTemplateInput input) throws StatusCodeWithException, IOException {
        String fileId = gridFSBucket.uploadFromStream(input.getFilename(), input.getFirstFile().getInputStream()).toString();
        GridFSFile gridFSFile = gridFsTemplate.findOne(new QueryBuilder().append("_id",fileId).build());
        AuthAgreementTemplate authAgreementTemplate = new AuthAgreementTemplate();
        authAgreementTemplate.setAuthAgreementFileId(fileId);
        authAgreementTemplate.setAuthAgreementFileMd5(gridFSFile.getMD5());
        authAgreementTemplate.setFileName(input.getFilename());
        authAgreementTemplate.setVersion(input.getVersion());
        authAgreementTemplateMongoRepo.save(authAgreementTemplate);
        return success();
    }

}
