package com.welab.wefe.manager.service.api.agreement;

import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.model.GridFSUploadOptions;
import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.data.mongodb.entity.union.RealnameAuthAgreementTemplate;
import com.welab.wefe.common.data.mongodb.entity.union.UnionNode;
import com.welab.wefe.common.data.mongodb.repo.RealnameAuthAgreementTemplateMongoRepo;
import com.welab.wefe.common.data.mongodb.repo.UnionNodeMongoRepo;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.util.JObject;
import com.welab.wefe.common.util.Md5;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.common.web.dto.UploadFileApiOutput;
import com.welab.wefe.manager.service.dto.common.UploadFileInput;
import com.welab.wefe.manager.service.service.RealnameAuthAgreementTemplateContractService;
import com.welab.wefe.manager.service.task.UploadFileSyncToUnionTask;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.List;

/**
 * @Description:
 * @author: yuxin.zhang
 * @date: 2021/10/28
 */
@Api(path = "realname/auth/agreement/template/upload", name = "realname_auth_agreement_template_upload")
public class UploadRealnameAuthAgreementTemplateApi extends AbstractApi<UploadFileInput, UploadFileApiOutput> {
    @Autowired
    private RealnameAuthAgreementTemplateContractService contractService;
    @Autowired
    private RealnameAuthAgreementTemplateMongoRepo realnameAuthAgreementTemplateMongoRepo;
    @Autowired
    private GridFSBucket gridFSBucket;
    @Autowired
    private UnionNodeMongoRepo unionNodeMongoRepo;

    @Override
    protected ApiResult<UploadFileApiOutput> handle(UploadFileInput input) throws StatusCodeWithException, IOException {
        String fileName = input.getFilename();
        String sign = Md5.of(input.getFirstFile().getInputStream());
        String contentType = input.getFirstFile().getContentType();

        RealnameAuthAgreementTemplate realnameAuthAgreementTemplate = realnameAuthAgreementTemplateMongoRepo.findByTemplateFileSign(sign);
        if (realnameAuthAgreementTemplate == null) {
            GridFSUploadOptions options = new GridFSUploadOptions();

            Document metadata = new Document();
            metadata.append("contentType", contentType);
            metadata.append("sign", sign);
            options.metadata(metadata);

            String fileId = gridFSBucket.uploadFromStream(fileName, input.getFirstFile().getInputStream(), options).toString();


            realnameAuthAgreementTemplate = new RealnameAuthAgreementTemplate();
            realnameAuthAgreementTemplate.setTemplateFileId(fileId);
            realnameAuthAgreementTemplate.setTemplateFileSign(sign);
            realnameAuthAgreementTemplate.setFileName(fileName);
            realnameAuthAgreementTemplate.setEnable("0");
            contractService.add(realnameAuthAgreementTemplate);

            syncFileToUnion(input);

            return success(new UploadFileApiOutput(fileId));
        } else {
            throw new StatusCodeWithException("请勿重复上传相同文件", StatusCode.DUPLICATE_RESOURCE_ERROR);
        }
    }

    private void syncFileToUnion(UploadFileInput input) {
        List<UnionNode> unionNodeList = unionNodeMongoRepo.findAll(true);
        for (UnionNode unionNode :
                unionNodeList) {
            new UploadFileSyncToUnionTask(
                    unionNode.getBaseUrl(),
                    "realname/auth/agreement/template/sync",
                    JObject.create("filename", input.getFilename()),
                    input.files
            ).start();
        }
    }
}
