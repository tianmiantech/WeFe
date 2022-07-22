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
package com.welab.wefe.manager.service.api.cert;

import java.net.URLEncoder;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import com.webank.cert.mgr.enums.MgrExceptionCodeEnums;
import com.webank.cert.mgr.exception.CertMgrException;
import com.webank.cert.mgr.model.vo.CertVO;
import com.webank.cert.mgr.service.CertOperationService;
import com.webank.cert.toolkit.utils.CertUtils;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.manager.service.api.cert.DownloadApi.DownloadApiInput;

@Api(path = "cert/download", name = "download cert")
public class DownloadApi extends AbstractApi<DownloadApiInput, ResponseEntity<byte[]>> {

    @Autowired
    private CertOperationService certOperationService;

    @Override
    protected ApiResult<ResponseEntity<byte[]>> handle(DownloadApiInput input) throws Exception {

        CertVO certVO = certOperationService.queryCertInfoByCertId(input.getCertId());
        String fileName = certVO.getIssuerCN() + ".crt";
        if (certVO != null && !StringUtils.isEmpty(certVO.getCertContent())) {
            byte[] bytes = CertUtils.toBytes(CertUtils.convertStrToCert(certVO.getCertContent()));

            HttpHeaders headers = new HttpHeaders();
            headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
            headers.add("Content-Disposition", "attachment; filename=" + URLEncoder.encode(fileName, "UTF-8"));
            headers.add("Pragma", "no-cache");
            headers.add("ETag", String.valueOf(System.currentTimeMillis()));
            headers.add("filename", URLEncoder.encode(fileName, "UTF-8"));
            headers.add("Cache-Access-Control-Expose-Headers", "filename");

            ResponseEntity<byte[]> response = ResponseEntity.ok().headers(headers)
                    .contentType(MediaType.APPLICATION_OCTET_STREAM).body(bytes);

            return success(response);
        } else {
            throw new CertMgrException(MgrExceptionCodeEnums.PKEY_MGR_CERT_NOT_EXIST);
        }

    }

    public class DownloadApiInput extends AbstractApiInput {
        @Check(require = true)
        private String certId;

        public String getCertId() {
            return certId;
        }

        public void setCertId(String certId) {
            this.certId = certId;
        }
    }

}
