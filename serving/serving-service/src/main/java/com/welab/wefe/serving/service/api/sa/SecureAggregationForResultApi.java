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

package com.welab.wefe.serving.service.api.sa;

import java.io.IOException;
import java.util.List;

import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.util.JObject;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.common.web.util.ModelMapper;
import com.welab.wefe.mpc.commom.Operator;
import com.welab.wefe.mpc.sa.SecureAggregationApiName;
import com.welab.wefe.mpc.sa.request.QuerySAResultRequest;
import com.welab.wefe.mpc.sa.request.QuerySAResultResponse;
import com.welab.wefe.mpc.sa.server.service.QueryResultService;

@Api(path = SecureAggregationApiName.SA_RESULT, name = "sa_result", login = false)
public class SecureAggregationForResultApi
		extends AbstractApi<SecureAggregationForResultApi.Input, QuerySAResultResponse> {

	@Override
	protected ApiResult<QuerySAResultResponse> handle(SecureAggregationForResultApi.Input input)
			throws StatusCodeWithException, IOException {
		QueryResultService service = new QueryResultService();
		LOG.info("request path = " + SecureAggregationApiName.SA_RESULT + "\t request =" + JObject.toJSONString(input));
		QuerySAResultRequest request = ModelMapper.map(input.getData(), QuerySAResultRequest.class);
		QuerySAResultResponse response = service.handle(request);
		LOG.info("request path = " + SecureAggregationApiName.SA_RESULT + "\t response ="
				+ JObject.toJSONString(response));
		return success(response);
	}

	public static class Input extends AbstractApiInput {
		private InputData data;

		public InputData getData() {
			return data;
		}

		public void setData(InputData data) {
			this.data = data;
		}

	}

	public static class InputData extends AbstractApiInput {
		/**
		 * 请求标识
		 */
		private String uuid;
		/**
		 * 所有参与方的DH公钥
		 */
		private List<String> diffieHellmanValues;
		/**
		 * 操作符，+ or -
		 */
		private Operator operator = Operator.ADD;
		/**
		 * 权重
		 */
		private float weight = 1.0f;
		/**
		 * 当前参与方序号 [0, diffieHellmanValues.size() - 1]
		 */
		private int index;
		/**
		 * DH的mode
		 */
		private String p;

		public String getUuid() {
			return uuid;
		}

		public void setUuid(String uuid) {
			this.uuid = uuid;
		}

		public List<String> getDiffieHellmanValues() {
			return diffieHellmanValues;
		}

		public void setDiffieHellmanValues(List<String> diffieHellmanValues) {
			this.diffieHellmanValues = diffieHellmanValues;
		}

		public int getIndex() {
			return index;
		}

		public void setIndex(int index) {
			this.index = index;
		}

		public String getP() {
			return p;
		}

		public void setP(String p) {
			this.p = p;
		}

		public Operator getOperator() {
			return operator;
		}

		public void setOperator(Operator operator) {
			this.operator = operator;
		}

		public float getWeight() {
			return weight;
		}

		public void setWeight(float weight) {
			this.weight = weight;
		}
	}

}
