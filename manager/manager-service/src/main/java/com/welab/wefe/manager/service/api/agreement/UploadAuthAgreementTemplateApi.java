package com.welab.wefe.manager.service.api.agreement;

import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.model.GridFSFile;
import com.mongodb.client.gridfs.model.GridFSUploadOptions;
import com.welab.wefe.common.data.mongodb.entity.union.AuthAgreementTemplate;
import com.welab.wefe.common.data.mongodb.repo.AuthAgreementTemplateMongoRepo;
import com.welab.wefe.common.data.mongodb.util.QueryBuilder;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.util.Md5;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiOutput;
import com.welab.wefe.common.web.dto.AbstractWithFilesApiInput;
import com.welab.wefe.common.web.dto.ApiResult;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;

import java.io.IOException;

/**
 * @Description:
 * @author: yuxin.zhang
 * @date: 2021/10/28
 */
@Api(path = "auth/agreement/template/upload", name = "auth_agreement_template_upload", login = false)
public class UploadAuthAgreementTemplateApi extends AbstractApi<AbstractWithFilesApiInput, AbstractApiOutput> {
    @Autowired
    private AuthAgreementTemplateMongoRepo authAgreementTemplateMongoRepo;
    @Autowired
    private GridFSBucket gridFSBucket;
    @Autowired
    private GridFsTemplate gridFsTemplate;

    @Override
    protected ApiResult<AbstractApiOutput> handle(AbstractWithFilesApiInput input) throws StatusCodeWithException, IOException {
        GridFSUploadOptions options = new GridFSUploadOptions();
        String contentType = input.getFirstFile().getContentType();
        Document metadata = new Document();
        metadata.append("contentType", contentType);
        metadata.append("sign", Md5.of(input.getFirstFile().getInputStream()));
        options.metadata(metadata);
        String fileName = input.getFirstFile().getOriginalFilename();
        String fileId = gridFSBucket.uploadFromStream(fileName, input.getFirstFile().getInputStream(), options).toString();
        GridFSFile gridFSFile = gridFsTemplate.findOne(new QueryBuilder().append("_id", fileId).build());
        AuthAgreementTemplate authAgreementTemplate = new AuthAgreementTemplate();
        authAgreementTemplate.setAuthAgreementFileId(fileId);
        authAgreementTemplate.setAuthAgreementFileMd5(gridFSFile.getMetadata().getString("sign"));
        authAgreementTemplate.setFileName(fileName);
        authAgreementTemplateMongoRepo.save(authAgreementTemplate);
        return success();
    }

}
