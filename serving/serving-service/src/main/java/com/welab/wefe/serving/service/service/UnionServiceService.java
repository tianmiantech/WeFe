package com.welab.wefe.serving.service.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.springframework.stereotype.Service;

import com.welab.wefe.serving.service.api.service.UnionServiceApi;
import com.welab.wefe.serving.service.api.service.UnionServiceApi.Input;
import com.welab.wefe.serving.service.api.service.UnionServiceApi.Output;
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

}
