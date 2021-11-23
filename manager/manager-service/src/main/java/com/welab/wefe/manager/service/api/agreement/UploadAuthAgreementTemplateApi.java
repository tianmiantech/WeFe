package com.welab.wefe.manager.service.api.agreement;

import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.model.GridFSFile;
import com.mongodb.client.gridfs.model.GridFSUploadOptions;
import com.welab.wefe.common.StatusCode;
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
import com.welab.wefe.manager.service.dto.common.UploadFileInput;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;

import java.io.IOException;

/**
 * @Description:
 * @author: yuxin.zhang
 * @date: 2021/10/28
 */
@Api(path = "auth/agreement/template/upload", name = "auth_agreement_template_upload")
public class UploadAuthAgreementTemplateApi extends AbstractApi<UploadFileInput, AbstractApiOutput> {
    @Autowired
    private AuthAgreementTemplateMongoRepo authAgreementTemplateMongoRepo;
    @Autowired
    private GridFSBucket gridFSBucket;
    @Autowired
    private GridFsTemplate gridFsTemplate;

    @Override
    protected ApiResult<AbstractApiOutput> handle(UploadFileInput input) throws StatusCodeWithException, IOException {
        String fileName = input.getFilename();
        String sign = Md5.of(input.getFirstFile().getInputStream());
        String contentType = input.getFirstFile().getContentType();
        //根据文件id查询文件
        GridFSFile gridFSFile = gridFsTemplate.findOne(
                new QueryBuilder()
                        .append("metadata.sign", sign)
                        .build()
        );
        if(gridFSFile == null){
            GridFSUploadOptions options = new GridFSUploadOptions();

            Document metadata = new Document();
            metadata.append("contentType", contentType);
            metadata.append("sign", sign);
            options.metadata(metadata);

            String fileId = gridFSBucket.uploadFromStream(fileName, input.getFirstFile().getInputStream(), options).toString();

            AuthAgreementTemplate authAgreementTemplate = new AuthAgreementTemplate();
            authAgreementTemplate.setAuthAgreementFileId(fileId);
            authAgreementTemplate.setAuthAgreementFileMd5(sign);
            authAgreementTemplate.setFileName(fileName);
            authAgreementTemplateMongoRepo.save(authAgreementTemplate);
        } else {
            throw new StatusCodeWithException("Do not upload the same file repeatedly", StatusCode.DUPLICATE_RESOURCE_ERROR);
        }
        return success();
    }
}
