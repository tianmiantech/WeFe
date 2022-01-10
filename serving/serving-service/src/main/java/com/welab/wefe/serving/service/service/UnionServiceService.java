/**
 * Copyright 2021 Tianmian Tech. All Rights Reserved.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.welab.wefe.serving.service.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.springframework.stereotype.Service;

import com.welab.wefe.serving.service.api.service.UnionServiceApi;
import com.welab.wefe.serving.service.api.service.UnionServiceApi.Input;
import com.welab.wefe.serving.service.api.service.UnionServiceApi.Output;
import com.welab.wefe.serving.service.database.serving.entity.ServiceMySqlModel;
import com.welab.wefe.serving.service.dto.PagingOutput;

@Service
public class UnionServiceService {

	public PagingOutput<Output> query(Input input) {
		if (input.getServiceType() != -1) {
			// TODO
		}

		List<UnionServiceApi.Output> list = new ArrayList<>();

		// mock
		int size = 2;
		for (int i = 1; i <= size; i++) {
			UnionServiceApi.Output output = new UnionServiceApi.Output();
			output.setId("" + i);
			output.setName("信用卡数量查询");
			output.setSupplierId("06198105b8c647289177cf057a15bdb" + i);
			output.setSupplierName("鹏元");
			output.setBaseUrl("http://xbd-dev.wolaidai.com/serving-service-0" + i + "/");
			output.setApiName("api/query/credit_card_count");
			output.setParams(Arrays.asList("member", "model"));
			output.setCreatedTime(new Date());
			output.setServiceType(3);
			list.add(output);
		}
		return PagingOutput.of(2, list);
	}

	public void publish2Union(ServiceMySqlModel model) {
		model.getId();
		model.getName();
		String supplierId = "";
		String supplierName = "";
		String baseUrl = "http://xbd-dev.wolaidai.com/serving-service-01";
		String apiName = "api/" + model.getUrl();
		List<String> params = Arrays.asList(model.getQueryParams().split(","));
		model.getCreatedTime();
		model.getServiceType();
		// TODO publish
	}

	public void offline2Union(ServiceMySqlModel model) {
		model.getId();
		model.getName();
		String supplierId = "";
		// TODO offline
	}

}
