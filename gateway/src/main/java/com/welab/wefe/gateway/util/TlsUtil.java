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


package com.welab.wefe.gateway.util;

import com.webank.cert.toolkit.utils.CertUtils;
import com.welab.wefe.gateway.cache.CaCertificateCache;
import org.apache.commons.collections4.CollectionUtils;

import java.security.cert.X509Certificate;
import java.util.List;

/**
 * TLS tool class
 */
public class TlsUtil {
    public static X509Certificate[] buildCertificates(List<CaCertificateCache.CaCertificate> caCertificateList) throws Exception {
        if (CollectionUtils.isEmpty(caCertificateList)) {
            return null;
        }
        X509Certificate[] certificates = new X509Certificate[caCertificateList.size()];
        for (int i = 0; i < caCertificateList.size(); i++) {
            CaCertificateCache.CaCertificate c = caCertificateList.get(i);
            X509Certificate cert = CertUtils.convertStrToCert(c.getContent());
            certificates[i] = cert;
        }
        return certificates;
    }


    public static X509Certificate[] getAllCertificates(boolean tlsEnable) throws Exception {
        return tlsEnable ? TlsUtil.buildCertificates(CaCertificateCache.getInstance().getAll()) : null;
    }
}
