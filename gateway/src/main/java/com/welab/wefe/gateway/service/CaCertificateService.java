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

package com.welab.wefe.gateway.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.welab.wefe.common.util.JObject;
import com.welab.wefe.gateway.cache.CaCertificateCache;
import com.welab.wefe.gateway.sdk.UnionHelper;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CaCertificateService {

    /**
     * Find all ca certificate from union service
     */
    public List<CaCertificateCache.CaCertificate> findAll() throws Exception {
        List<CaCertificateCache.CaCertificate> resultList = new ArrayList<>();
        JSONArray caCertificateDataArray = JObject.parseArray(UnionHelper.getCaCertificate());
        if (CollectionUtils.isEmpty(caCertificateDataArray)) {
            return resultList;
        }
        CaCertificateCache.CaCertificate caCertificate = null;
        for (int i = 0; i < caCertificateDataArray.size(); i++) {
            caCertificate = new CaCertificateCache.CaCertificate();
            JSONObject caCertificateDataObj = caCertificateDataArray.getJSONObject(i);
            caCertificate.setId(caCertificateDataObj.getString("serial_number"));
            caCertificate.setName(caCertificateDataObj.getString("subject_cn"));
            caCertificate.setContent(caCertificateDataObj.getString("cert_content"));

            resultList.add(caCertificate);
        }
        return resultList;
    }
}
