/**
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

package com.welab.wefe.serving.service.service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.data.mysql.Where;
import com.welab.wefe.common.enums.DatabaseType;
import com.welab.wefe.common.enums.OrderBy;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.util.JObject;
import com.welab.wefe.common.web.CurrentAccount;
import com.welab.wefe.mpc.cache.result.QueryDataResult;
import com.welab.wefe.mpc.cache.result.QueryDataResultFactory;
import com.welab.wefe.mpc.config.CommunicationConfig;
import com.welab.wefe.mpc.pir.request.QueryKeysRequest;
import com.welab.wefe.mpc.pir.request.QueryKeysResponse;
import com.welab.wefe.mpc.pir.server.service.HuackKeyService;
import com.welab.wefe.mpc.psi.request.QueryPrivateSetIntersectionRequest;
import com.welab.wefe.mpc.psi.request.QueryPrivateSetIntersectionResponse;
import com.welab.wefe.mpc.sa.request.QueryDiffieHellmanKeyRequest;
import com.welab.wefe.mpc.sa.request.QueryDiffieHellmanKeyResponse;
import com.welab.wefe.mpc.sa.sdk.SecureAggregation;
import com.welab.wefe.mpc.sa.sdk.config.ServerConfig;
import com.welab.wefe.mpc.sa.sdk.transfer.SecureAggregationTransferVariable;
import com.welab.wefe.mpc.sa.sdk.transfer.impl.HttpTransferVariable;
import com.welab.wefe.mpc.sa.server.service.QueryDiffieHellmanKeyService;
import com.welab.wefe.mpc.util.DiffieHellmanUtil;
import com.welab.wefe.serving.service.api.service.AddApi;
import com.welab.wefe.serving.service.api.service.QueryApi;
import com.welab.wefe.serving.service.api.service.QueryOneApi;
import com.welab.wefe.serving.service.api.service.ServiceSQLTestApi.Output;
import com.welab.wefe.serving.service.api.service.UpdateApi.Input;
import com.welab.wefe.serving.service.config.Config;
import com.welab.wefe.serving.service.database.serving.entity.ClientMysqlModel;
import com.welab.wefe.serving.service.database.serving.entity.ClientServiceMysqlModel;
import com.welab.wefe.serving.service.database.serving.entity.DataSourceMySqlModel;
import com.welab.wefe.serving.service.database.serving.entity.ServiceMySqlModel;
import com.welab.wefe.serving.service.database.serving.repository.ClientRepository;
import com.welab.wefe.serving.service.database.serving.repository.ServiceRepository;
import com.welab.wefe.serving.service.dto.PagingOutput;
import com.welab.wefe.serving.service.enums.ServiceTypeEnum;
import com.welab.wefe.serving.service.utils.MD5Util;
import com.welab.wefe.serving.service.utils.ModelMapper;
import com.welab.wefe.serving.service.utils.SHA256Utils;
import com.welab.wefe.serving.service.utils.ServiceUtil;
import com.welab.wefe.serving.service.utils.ZipUtils;

/**
 * 服务 Service
 */
@Service
public class ServiceService {

	protected final Logger LOG = LoggerFactory.getLogger(this.getClass());

	public static final String SERVICE_PRE_URL = "api/";
	@Autowired
	private ServiceRepository serviceRepository;
	@Autowired
	private DataSourceService dataSourceService;
	@Autowired
	private ApiRequestRecordService apiRequestRecordService;

	@Autowired
	private UnionServiceService unionServiceService;

	@Autowired
	private ClientRepository clientRepository;

	@Autowired
	private ClientService clientService;

	@Autowired
	private Config config;

	@Transactional(rollbackFor = Exception.class)
	public com.welab.wefe.serving.service.api.service.AddApi.Output save(AddApi.Input input)
			throws StatusCodeWithException {
		ServiceMySqlModel model = serviceRepository.findOne("url", input.getUrl(), ServiceMySqlModel.class);
		if (model != null) {
			throw new StatusCodeWithException(StatusCode.PRIMARY_KEY_CONFLICT, input.getUrl(), "url");
		}
		model = ModelMapper.map(input, ServiceMySqlModel.class);
		model.setCreatedBy(CurrentAccount.id());
		model.setCreatedTime(new Date());
		model.setUpdatedBy(CurrentAccount.id());
		model.setUpdatedTime(new Date());
		if (model.getServiceType() != ServiceTypeEnum.PSI.getCode()) {// 对于 交集查询 需要额外生成对应的主键数据
			String idsTableName = generateIdsTable(model);
			model.setIdsTableName(idsTableName);
		}
		model.setQueryParams(StringUtils.join(input.getQueryParams(), ","));
		serviceRepository.save(model);
		com.welab.wefe.serving.service.api.service.AddApi.Output output = new com.welab.wefe.serving.service.api.service.AddApi.Output();
		output.setId(model.getId());
		output.setParams(model.getQueryParams());
		output.setUrl(SERVICE_PRE_URL + model.getUrl());
		return output;
	}

	private String generateIdsTable(ServiceMySqlModel model) {
		String keysTableName = "";
		SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
		if (model.getServiceType() != ServiceTypeEnum.PSI.getCode()) {// 对于 交集查询 需要额外生成对应的主键数据
			return keysTableName;
		}
		JSONObject dataSource = JObject.parseObject(model.getDataSource());
		DataSourceMySqlModel dataSourceModel = dataSourceService.getDataSourceById(dataSource.getString("id"));
		if (dataSourceModel == null) {
			return keysTableName;
		}
		keysTableName = dataSourceModel.getDatabaseName() + "_" + dataSource.getString("table");
		JSONArray keyCalcRules = dataSource.getJSONArray("key_calc_rules");
		List<String> needFields = new ArrayList<>();
		for (int i = 0; i < keyCalcRules.size(); i++) {
			JSONObject item = keyCalcRules.getJSONObject(i);
			String[] fields = item.getString("field").split(",");
			needFields.addAll(Arrays.asList(fields));
		}
		keysTableName += ("_" + format.format(new Date()));
		String sql = "SELECT " + StringUtils.join(needFields, ",") + " FROM " + dataSourceModel.getDatabaseName() + "."
				+ dataSource.getString("table");
		Set<String> ids = new HashSet<>();
		try {
			List<Map<String, String>> result = dataSourceService.queryList(dataSource.getString("id"), sql, needFields);
			for (Map<String, String> item : result) {
				String id = calcKey(keyCalcRules, item);
				ids.add(id);
			}
			String createTableSql = String.format(
					"CREATE TABLE `%s` (`id` varchar(100) NOT NULL ,PRIMARY KEY (`id`) USING BTREE ) ENGINE=InnoDB;",
					keysTableName);
			try {
				dataSourceService.createTable(createTableSql, DatabaseType.MySql, dataSourceModel.getHost(),
						dataSourceModel.getPort(), dataSourceModel.getUserName(), dataSourceModel.getPassword(),
						dataSourceModel.getDatabaseName());
			} catch (Exception e) {
				e.printStackTrace();
			}
			String insertSql = String.format("insert into %s values (?)", keysTableName);
			dataSourceService.batchInsert(insertSql, DatabaseType.MySql, dataSourceModel.getHost(),
					dataSourceModel.getPort(), dataSourceModel.getUserName(), dataSourceModel.getPassword(),
					dataSourceModel.getDatabaseName(), ids);

		} catch (StatusCodeWithException e) {
			e.printStackTrace();
		}
		return keysTableName;
	}

	private String calcKey(JSONArray keyCalcRules, Map<String, String> data) {
		int size = keyCalcRules.size();
		StringBuffer encodeValue = new StringBuffer("");
		for (int i = 0; i < size; i++) {
			JSONObject item = keyCalcRules.getJSONObject(i);
			String operator = item.getString("operator");
			String[] fields = item.getString("field").split(",");
			StringBuffer value = new StringBuffer();

			for (String field : fields) {
				value.append(data.get(field));
			}
			if ("md5".equalsIgnoreCase(operator)) {
				encodeValue.append(MD5Util.getMD5String(value.toString()));
			} else if ("sha256".equalsIgnoreCase(operator)) {
				encodeValue.append(SHA256Utils.getSHA256(value.toString()));
			}
		}
		return encodeValue.toString();

	}

	/**
	 * Paging query
	 */
	public PagingOutput<QueryApi.Output> query(QueryApi.Input input) {

		Where where = Where.create();
		if (StringUtils.isNotBlank(input.getName())) {
			where = where.contains("name", input.getName());
		}
		if (input.getServiceType() != -1) {
			where = where.equal("serviceType", input.getServiceType());
		}
		if (input.getStatus() != -1) {
			where = where.equal("status", input.getStatus());
		}
		Specification<ServiceMySqlModel> condition = where.orderBy("updatedTime", OrderBy.desc)
				.build(ServiceMySqlModel.class);

		PagingOutput<ServiceMySqlModel> page = serviceRepository.paging(condition, input);

		List<QueryApi.Output> list = page.getList().stream().map(x -> ModelMapper.map(x, QueryApi.Output.class))
				.collect(Collectors.toList());

		return PagingOutput.of(page.getTotal(), list);
	}

	public com.welab.wefe.serving.service.api.service.AddApi.Output update(Input input) throws StatusCodeWithException {
		ServiceMySqlModel model = serviceRepository.findOne("id", input.getId(), ServiceMySqlModel.class);
		if (model == null) {
			throw new StatusCodeWithException(StatusCode.DATA_NOT_FOUND);
		}
		if (StringUtils.isNotBlank(input.getName())) {
			model.setName(input.getName());
		}
		if (StringUtils.isNotBlank(input.getUrl())) {
			model.setUrl(input.getUrl());
		}
		if (!CollectionUtils.isEmpty(input.getQueryParams())) {
			model.setQueryParams(StringUtils.join(input.getQueryParams(), ","));
		}
		if (StringUtils.isNotBlank(input.getDataSource())) {
			model.setDataSource(input.getDataSource());
		}
		if (input.getServiceType() != -1) {
			model.setServiceType(input.getServiceType());
		}
		if (StringUtils.isNotBlank(input.getServiceConfig())) {
			model.setServiceConfig(input.getServiceConfig());
		}
		model.setUpdatedBy(CurrentAccount.id());
		model.setUpdatedTime(new Date());
		String idsTableName = generateIdsTable(model);
		model.setIdsTableName(idsTableName);
		serviceRepository.save(model);
		com.welab.wefe.serving.service.api.service.AddApi.Output output = new com.welab.wefe.serving.service.api.service.AddApi.Output();
		output.setId(model.getId());
		output.setParams(model.getQueryParams());
		output.setUrl(SERVICE_PRE_URL + model.getUrl());
		if (model.getStatus() == 1) {
			unionServiceService.add2Union(model);
		} else {
			unionServiceService.offline2Union(model);
		}
		return output;
	}

	public void offlineService(String id) throws StatusCodeWithException {
		ServiceMySqlModel model = serviceRepository.findOne("id", id, ServiceMySqlModel.class);
		if (model == null) {
			throw new StatusCodeWithException(StatusCode.DATA_NOT_FOUND);
		}
		if (model.getStatus() == 0) {
			throw new StatusCodeWithException(StatusCode.ILLEGAL_REQUEST);
		}
		model.setStatus(0);
		serviceRepository.save(model);
		unionServiceService.offline2Union(model);
	}

	public void onlineService(String id) throws StatusCodeWithException {
		ServiceMySqlModel model = serviceRepository.findOne("id", id, ServiceMySqlModel.class);
		if (model == null) {
			throw new StatusCodeWithException(StatusCode.DATA_NOT_FOUND);
		}
		if (model.getStatus() == 1) {
			throw new StatusCodeWithException(StatusCode.ILLEGAL_REQUEST);
		}
		model.setStatus(1);
		serviceRepository.save(model);
		unionServiceService.add2Union(model);
	}

	public Output sqlTest(com.welab.wefe.serving.service.api.service.ServiceSQLTestApi.Input input)
			throws StatusCodeWithException {
		JSONObject dataSource = JObject.parseObject(input.getDataSource());
		String resultfields = ServiceUtil.parseReturnFields(dataSource);
		String dataSourceId = dataSource.getString("id");
		DataSourceMySqlModel dataSourceModel = dataSourceService.getDataSourceById(dataSourceId);
		String sql = ServiceUtil.generateSQL(input.getParams(), dataSource, dataSourceModel.getDatabaseName());
		Map<String, String> result = dataSourceService.queryOne(dataSourceId, sql,
				Arrays.asList(resultfields.split(",")));
		Output out = new Output();
		out.setResult(JObject.create(result));
		return out;
	}

	public JObject executeService(com.welab.wefe.serving.service.api.service.RouteApi.Input input)
			throws StatusCodeWithException {
		long start = System.currentTimeMillis();
		String clientIp = ServiceUtil.getIpAddr(input.request);
		String uri = input.request.getRequestURI();
		String serviceUrl = uri.substring(uri.lastIndexOf("api/") + 4);
		JObject res = JObject.create();
		ServiceMySqlModel model = serviceRepository.findOne("url", serviceUrl, ServiceMySqlModel.class);
		JObject data = JObject.create(input.getData());
		if (model == null || model.getStatus() != 1) {
			return JObject.create("message", "invalid request: url = " + serviceUrl);
		} else {
			ClientMysqlModel client = clientService.queryByClientId(input.getCustomerId());
			ClientServiceMysqlModel clientMysqlModel = clientService.queryByServiceIdAndClientId(model.getId(),
					input.getCustomerId());
			if (clientMysqlModel == null || client == null || client.getStatus() != 1
					|| clientMysqlModel.getStatus() != 1) {// !Arrays.asList(client.getIpAdd().split(",|，")).contains(clientIp)
				return JObject.create("message", "invalid request: url = " + serviceUrl);
			}
			int serviceType = model.getServiceType();// 服务类型 1匿踪查询，2交集查询，3安全聚合
			if (serviceType == 1) {// 1匿踪查询
				List<String> ids = JObject.parseArray(data.getString("ids"), String.class);
				QueryKeysResponse result = pir(ids, model);
				res = JObject.create(result);
			} else if (serviceType == 2) {// 2交集查询（10W内）
				String p = data.getString("p");
				List<String> clientIds = JObject.parseArray(data.getString("clientIds"), String.class);
				QueryPrivateSetIntersectionResponse result = psi(p, clientIds, model);
				res = JObject.create(result);
			} else if (serviceType == 3) {// 3 安全聚合 被查询方
				QueryDiffieHellmanKeyRequest request = new QueryDiffieHellmanKeyRequest();
				request.setP(data.getString("p"));
				request.setG(data.getString("g"));
				request.setUuid(data.getString("uuid"));
				request.setQueryParams(data.getJSONObject("query_params"));
				QueryDiffieHellmanKeyResponse result = sa(request, model);
				res = JObject.create(result);
			} else if (serviceType == 4) {// 安全聚合（查询方）
				Double result = sa_query(data, model);
				res = JObject.create("result", result);
			}
		}
		long duration = System.currentTimeMillis() - start;
		try {
			ClientMysqlModel clientMysqlModel = clientRepository.findOne("id", input.getCustomerId(),
					ClientMysqlModel.class);
			apiRequestRecordService.save(model.getId(), model.getName(), model.getServiceType(),
					clientMysqlModel.getName(), clientMysqlModel.getId(), duration, clientIp, 1);
		} catch (Exception e) {
			LOG.error(e.toString());
		}
		return res;
	}

	/**
	 * 0.参考 SecureAggregation.query 返回结果
	 */
	private Double sa_query(JObject data, ServiceMySqlModel model) {
		JObject userParams = data.getJObject("query_params");
//		String queryParams = model.getQueryParams();
//		String operator = model.getOperator();
		JSONArray serviceConfigs = JObject.parseArray(model.getServiceConfig());
		int size = serviceConfigs.size();
		List<ServerConfig> serverConfigs = new LinkedList<>();
		List<SecureAggregationTransferVariable> transferVariables = new LinkedList<>();

		for (int i = 0; i < size; i++) {
			JSONObject serviceConfig = serviceConfigs.getJSONObject(i);
			String supplieId = serviceConfig.getString("member_id");
//			String supplierName = serviceConfig.getString("supplier_name");
			String name = serviceConfig.getString("name");
			String apiName = serviceConfig.getString("api_name");
//			String params = serviceConfig.getString("params");
			String base_url = serviceConfig.getString("base_url");
			ServerConfig config = new ServerConfig();
			config.setServerName(apiName);
			config.setServerUrl(base_url);
			config.setQueryParams(userParams);

			CommunicationConfig communicationConfig = new CommunicationConfig();
			communicationConfig.setApiName(apiName);
			communicationConfig.setServerUrl(base_url);
			communicationConfig.setCommercialId(supplieId);
			communicationConfig.setNeedSign(false);// TODO
			communicationConfig.setSignPrivateKey("");// TODO
			config.setCommunicationConfig(communicationConfig);
			HttpTransferVariable httpTransferVariable = new HttpTransferVariable(config);
			transferVariables.add(httpTransferVariable);
			serverConfigs.add(config);
		}

		SecureAggregation secureAggregation = new SecureAggregation();
		if (model.getOperator().equalsIgnoreCase("sum")) {
			return secureAggregation.query(serverConfigs, transferVariables);
		} else {
			return secureAggregation.query(serverConfigs, transferVariables) / size;
		}
	}

	/**
	 * 安全聚合（被查询方） 0.两次交互 1.根据用户参数，生成 QueryDiffieHellmanKeyRequest ，（根据 request 中的
	 * queryParams 去数据库中查询对应的【只能是一个数值类型】结果保存到内存中），然后调用
	 * QueryDiffieHellmanKeyService.handle 2.生成一个接口，参数为 QuerySAResultRequest ，然后去调用
	 * QueryResultService.handle ，然后返回结果
	 */
	private QueryDiffieHellmanKeyResponse sa(QueryDiffieHellmanKeyRequest request, ServiceMySqlModel model)
			throws StatusCodeWithException {
		QueryDiffieHellmanKeyService service = new QueryDiffieHellmanKeyService();
		JSONObject queryParams = request.getQueryParams();
		JSONObject dataSource = JObject.parseObject(model.getDataSource());
		String dataSourceId = dataSource.getString("id");
		DataSourceMySqlModel dataSourceModel = dataSourceService.getDataSourceById(dataSourceId);
		String sql = ServiceUtil.generateSQL(queryParams.toJSONString(), dataSource, dataSourceModel.getDatabaseName());
		String resultfields = ServiceUtil.parseReturnFields(dataSource);
		String resultStr = "";
		try {
			Map<String, String> resultMap = dataSourceService.queryOne(dataSourceId, sql,
					Arrays.asList(resultfields.split(",")));
			if (resultMap == null || resultMap.isEmpty()) {
				resultMap = new HashMap<>();
			}
			resultStr = resultMap.get(resultfields);// 目前只支持一个返回值
			LOG.info(queryParams.toJSONString() + "\t " + resultStr);
		} catch (StatusCodeWithException e) {
			throw e;
		}
		QueryDiffieHellmanKeyResponse response = service.handle(request);
		// 将 0 步骤查询的数据 保存到 QueryResult -> LocalResultCache
		QueryDataResult<Double> queryResult = QueryDataResultFactory.getQueryDataResult();
		queryResult.save(request.getUuid(), Double.valueOf(resultStr));
		return response;
	}

	private QueryPrivateSetIntersectionResponse psi(String p, List<String> clientIds, ServiceMySqlModel model)
			throws StatusCodeWithException {
		QueryPrivateSetIntersectionRequest request = new QueryPrivateSetIntersectionRequest();
		request.setClientIds(clientIds);
		request.setP(p);
		QueryPrivateSetIntersectionResponse response = new QueryPrivateSetIntersectionResponse();
		BigInteger mod = new BigInteger(request.getP(), 16);
		int keySize = 1024;
		BigInteger serverKey = new BigInteger(keySize, new Random());
		JSONObject dataSource = JObject.parseObject(model.getDataSource());
		String sql = "select id from " + model.getIdsTableName();
		List<String> needFields = new ArrayList<>();
		needFields.add("id");
		List<Map<String, String>> result = dataSourceService.queryList(dataSource.getString("id"), sql, needFields);
		List<String> serverIds = new ArrayList<>();
		for (Map<String, String> item : result) {
			serverIds.add(item.get("id"));
		}
		List<String> encryptServerIds = new ArrayList<>(serverIds.size());
		serverIds.forEach(
				serverId -> encryptServerIds.add(DiffieHellmanUtil.encrypt(serverId, serverKey, mod).toString(16)));
		response.setServerEncryptIds(encryptServerIds);

		List<String> encryptClientIds = new ArrayList<>(request.getClientIds().size());
		request.getClientIds()
				.forEach(id -> encryptClientIds.add(DiffieHellmanUtil.encrypt(id, serverKey, mod, false).toString(16)));
		response.setClientIdByServerKeys(encryptClientIds);
		return response;
	}

	private QueryKeysResponse pir(List<String> ids, ServiceMySqlModel model) throws StatusCodeWithException {
		Map<String, String> result = new HashMap<>();
		// 0 根据ID查询对应的数据
		for (String id : ids) {// params
			JSONObject dataSource = JObject.parseObject(model.getDataSource());
			String dataSourceId = dataSource.getString("id");
			DataSourceMySqlModel dataSourceModel = dataSourceService.getDataSourceById(dataSourceId);
			String sql = ServiceUtil.generateSQL(id, dataSource, dataSourceModel.getDatabaseName());
			String resultfields = ServiceUtil.parseReturnFields(dataSource);
			try {
				Map<String, String> resultMap = dataSourceService.queryOne(dataSourceId, sql,
						Arrays.asList(resultfields.split(",")));
				if (resultMap == null || resultMap.isEmpty()) {
					resultMap = new HashMap<>();
					resultMap.put("rand", "thisisrandomstring");
				}
				String resultStr = JObject.toJSONString(resultMap);
				LOG.info(id + "\t " + resultStr);
				result.put(id, resultStr);
			} catch (StatusCodeWithException e) {
				throw e;
			}
		}
		QueryKeysRequest request = new QueryKeysRequest();
		request.setIds((List) ids);
		request.setMethod("plain");
		HuackKeyService service = new HuackKeyService();
		String uuid = "";
		QueryKeysResponse response = null;
		try {
			response = service.handle(request);
			// 3 取出 QueryKeysResponse 的uuid
			// 将uuid传入QueryResult
			uuid = response.getUuid();
		} catch (Exception e) {
			e.printStackTrace();
			throw new StatusCodeWithException(StatusCode.SYSTEM_ERROR, "系统异常，请联系管理员");
		}
		// 将 0 步骤查询的数据 保存到 QueryResult -> LocalResultCache
		QueryDataResult<Map<String, String>> queryResult = QueryDataResultFactory.getQueryDataResult();
		queryResult.save(uuid, result);
		return response;
	}

	public ResponseEntity<byte[]> exportSdk(String serviceId) throws StatusCodeWithException, FileNotFoundException {
		ServiceMySqlModel model = serviceRepository.findOne("id", serviceId, ServiceMySqlModel.class);
		if (model == null) {
			throw new StatusCodeWithException(StatusCode.DATA_NOT_FOUND);
		}
		int serviceType = model.getServiceType();// 服务类型 1匿踪查询，2交集查询，3安全聚合
		String basePath = config.getFileBasePath();
		List<File> fileList = new ArrayList<>();
		if (serviceType == ServiceTypeEnum.PIR.getCode()) {
			// TODO 将需要提供的文件加到这个列表
			fileList.add(new File(basePath + "mpc-pir-sdk-1.0.0.jar"));
			fileList.add(new File(basePath + "readme.md"));
		} else if (serviceType == ServiceTypeEnum.PSI.getCode()) {
			// TODO 将需要提供的文件加到这个列表
			fileList.add(new File(basePath + "mpc-psi-sdk-1.0.0.jar"));
			fileList.add(new File(basePath + "readme.md"));
		} else if (serviceType == ServiceTypeEnum.SA.getCode() || serviceType == ServiceTypeEnum.SA.getCode()) {
			// TODO 将需要提供的文件加到这个列表
			fileList.add(new File(basePath + "mpc-sa-sdk-1.0.0.jar"));
			fileList.add(new File(basePath + "readme.md"));
		}
		String sdkZipName = "sdk.zip";
		String outputPath = basePath + sdkZipName;
		FileOutputStream fos2 = new FileOutputStream(new File(outputPath));
		ZipUtils.toZip(fileList, fos2);
		File file = new File(outputPath);
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
		headers.setContentDispositionFormData("attachment", sdkZipName);
		try {
			return new ResponseEntity<>(ServiceUtil.fileToBytes(file), headers, HttpStatus.CREATED);
		} catch (IOException e) {
			e.printStackTrace();
			throw new StatusCodeWithException(StatusCode.SYSTEM_ERROR, "系统异常，请联系管理员");
		}
	}

	public ServiceMySqlModel queryById(QueryOneApi.Input input) {

		Specification<ServiceMySqlModel> where = Where.create().equal("id", input.getId())
				.build(ServiceMySqlModel.class);

		Optional<ServiceMySqlModel> one = serviceRepository.findOne(where);
		return one.orElse(null);
	}
}
