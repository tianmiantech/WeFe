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

import com.welab.wefe.gateway.cache.CaCertificateCache;
import org.apache.commons.collections4.CollectionUtils;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.List;

/**
 * TLS tool class
 */
public class TlsUtil {
    /**
     * Build the list of certificate objects according to the address list of CRT file
     */
    public static X509Certificate[] buildCertificates(String[] crtPaths) throws Exception {
        if (null == crtPaths) {
            return null;
        }
        X509Certificate[] certificates = new X509Certificate[crtPaths.length];
        for (int i = 0; i < crtPaths.length; i++) {
            try (InputStream inStream = new FileInputStream(crtPaths[i])) {
                CertificateFactory cf = CertificateFactory.getInstance("X.509");
                X509Certificate cert = (X509Certificate) cf.generateCertificate(inStream);
                certificates[i] = cert;
            }
        }
        return certificates;
    }


    public static X509Certificate[] buildCertificates(List<CaCertificateCache.CaCertificate> caCertificateList) throws Exception {
        if (CollectionUtils.isEmpty(caCertificateList)) {
            return null;
        }
        X509Certificate[] certificates = new X509Certificate[caCertificateList.size()];
        for (int i = 0; i < caCertificateList.size(); i++) {
            try (InputStream inputStream = new ByteArrayInputStream(caCertificateList.get(i).getContent().getBytes(StandardCharsets.UTF_8))) {
                CertificateFactory cf = CertificateFactory.getInstance("X.509");
                X509Certificate cert = (X509Certificate) cf.generateCertificate(inputStream);
                certificates[i] = cert;
            }
        }
        return certificates;
    }

}
