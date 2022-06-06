/**
 * Copyright 2021 Tianmian Tech. All Rights Reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.welab.wefe.serving.service.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.welab.wefe.common.CommonThreadPool;
import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.data.mysql.Where;
import com.welab.wefe.common.data.mysql.enums.OrderBy;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.http.HttpRequest;
import com.welab.wefe.common.http.HttpResponse;
import com.welab.wefe.common.util.JObject;
import com.welab.wefe.common.web.CurrentAccount;
import com.welab.wefe.common.web.util.ModelMapper;
import com.welab.wefe.common.wefe.enums.DatabaseType;
import com.welab.wefe.serving.service.api.service.AddApi;
import com.welab.wefe.serving.service.api.service.QueryApi;
import com.welab.wefe.serving.service.api.service.QueryOneApi;
import com.welab.wefe.serving.service.api.service.RouteApi;
import com.welab.wefe.serving.service.api.service.ServiceSQLTestApi.Output;
import com.welab.wefe.serving.service.api.service.UpdateApi.Input;
import com.welab.wefe.serving.service.config.Config;
import com.welab.wefe.serving.service.database.entity.*;
import com.welab.wefe.serving.service.database.repository.AccountRepository;
import com.welab.wefe.serving.service.database.repository.ServiceRepository;
import com.welab.wefe.serving.service.dto.PagingOutput;
import com.welab.wefe.serving.service.enums.ServiceResultEnum;
import com.welab.wefe.serving.service.enums.ServiceTypeEnum;
import com.welab.wefe.serving.service.service_processor.*;
import com.welab.wefe.serving.service.utils.*;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringSubstitutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

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
    private AccountRepository accountRepository;
    @Autowired
    private UnionServiceService unionServiceService;
    @Autowired
    private PartnerService partnerService;
    @Autowired
    private ClientServiceService clientServiceService;
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
        if (model.getServiceType() == ServiceTypeEnum.PSI.getCode()) {// 对于 交集查询 需要额外生成对应的主键数据
            String idsTableName = generateIdsTable(model);
            model.setIdsTableName(idsTableName);
        }
        model.setQueryParams(StringUtils.join(input.getQueryParams(), ","));
        model.setQueryParamsConfig(JSONObject.toJSONString(input.getQueryParamsConfig()));
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
            String tmpSql = "SELECT * FROM " + dataSourceModel.getDatabaseName() + "." + dataSource.getString("table");
            long count = dataSourceService.count(dataSourceModel, tmpSql);
            if (count <= 0) {
                throw new StatusCodeWithException("数据源数据为空", StatusCode.DATA_NOT_FOUND);
            }
            // 异步
            final String keysTableNameTmp = keysTableName;
            CommonThreadPool.run(() -> {
                try {
                    List<Map<String, String>> result = dataSourceService.queryList(dataSourceModel, sql, needFields);
                    if (result == null || result.isEmpty()) {
                        return;
                    }
                    LOG.info(dataSourceModel.getDatabaseName() + "." + dataSource.getString("table") + " count = " + result.size());
                    for (Map<String, String> item : result) {
                        String id = calcKey(keyCalcRules, item);
                        ids.add(id);
                    }
                    String createTableSql = String.format(
                            "CREATE TABLE `%s` (`id` varchar(100) NOT NULL ,PRIMARY KEY (`id`) USING BTREE ) ENGINE=InnoDB;",
                            keysTableNameTmp);
                    dataSourceService.createTable(createTableSql, DatabaseType.MySql, dataSourceModel.getHost(),
                            dataSourceModel.getPort(), dataSourceModel.getUserName(), dataSourceModel.getPassword(),
                            dataSourceModel.getDatabaseName());
                    String insertSql = String.format("insert into %s values (?)", keysTableNameTmp);
                    dataSourceService.batchInsert(insertSql, DatabaseType.MySql, dataSourceModel.getHost(),
                            dataSourceModel.getPort(), dataSourceModel.getUserName(), dataSourceModel.getPassword(),
                            dataSourceModel.getDatabaseName(), ids);
                } catch (StatusCodeWithException e1) {
                    e1.printStackTrace();
                }
            });
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
        if (StringUtils.isNotBlank(input.getCreatedBy())) {
            where = where.equal("createdBy", input.getCreatedBy());
        }
        Specification<ServiceMySqlModel> condition = where.orderBy("updatedTime", OrderBy.desc)
                .build(ServiceMySqlModel.class);

        PagingOutput<ServiceMySqlModel> page = serviceRepository.paging(condition, input);
        List<AccountMySqlModel> accounts = accountRepository.findAll();
        Map<String, String> accountMap = new HashMap<>();
        accounts.stream().forEach(s -> {
            accountMap.put(s.getId(), s.getNickname());
        });
        page.getList().stream().forEach(s -> {
            s.setCreatedBy(accountMap.get(s.getCreatedBy()));
            s.setUpdatedBy(accountMap.get(s.getUpdatedBy()));
        });
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
        if (!CollectionUtils.isEmpty(input.getQueryParamsConfig())) {
            model.setQueryParamsConfig(JSONObject.toJSONString(input.getQueryParamsConfig()));
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
        if (model.getServiceType() == ServiceTypeEnum.PSI.getCode()) {// 对于 交集查询 需要额外生成对应的主键数据
            String idsTableName = generateIdsTable(model);
            model.setIdsTableName(idsTableName);
        }
        serviceRepository.save(model);
        com.welab.wefe.serving.service.api.service.AddApi.Output output = new com.welab.wefe.serving.service.api.service.AddApi.Output();
        output.setId(model.getId());
        output.setParams(model.getQueryParams());
        output.setUrl(SERVICE_PRE_URL + model.getUrl());
        if (model.getStatus() == 1) {
            if (model.getServiceType() == ServiceTypeEnum.PSI.getCode()) {
                JSONObject dataSource = JObject.parseObject(model.getDataSource());
                String key_calc_rule = dataSource.getString("key_calc_rule");
                model.setQueryParams(key_calc_rule);
            }
            unionServiceService.add2Union(model);
        } else {
            unionServiceService.offline2Union(model);
        }
        clientServiceService.updateAllByServiceId(model.getId(), model.getName(), model.getUrl(),
                model.getServiceType());
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
        if (model.getServiceType() == ServiceTypeEnum.PSI.getCode()) {
            JSONObject dataSource = JObject.parseObject(model.getDataSource());
            String key_calc_rule = dataSource.getString("key_calc_rule");
            model.setQueryParams(key_calc_rule);
        }
        unionServiceService.add2Union(model);
    }

    public Output sqlTest(com.welab.wefe.serving.service.api.service.ServiceSQLTestApi.Input input)
            throws StatusCodeWithException {
        JSONObject dataSource = JObject.parseObject(input.getDataSource());
        String resultfields = ServiceUtil.parseReturnFields(dataSource);
        String dataSourceId = dataSource.getString("id");
        DataSourceMySqlModel dataSourceModel = dataSourceService.getDataSourceById(dataSourceId);
        String sql = ServiceUtil.generateSQL(input.getParams(), dataSource, dataSourceModel.getDatabaseName());
        Map<String, String> result = dataSourceService.queryOne(dataSourceModel, sql,
                Arrays.asList(resultfields.split(",")));
        Output out = new Output();
        out.setResult(JObject.create(result));
        return out;
    }

    public com.welab.wefe.serving.service.api.service.ServiceShowSQLApi.Output showSql(
            com.welab.wefe.serving.service.api.service.ServiceShowSQLApi.Input input) throws StatusCodeWithException {
        JSONObject dataSource = JObject.parseObject(input.getDataSource());
        String dataSourceId = dataSource.getString("id");
        DataSourceMySqlModel dataSourceModel = dataSourceService.getDataSourceById(dataSourceId);
        String sql = ServiceUtil.generateSQL(input.getParams(), dataSource, dataSourceModel.getDatabaseName());
        com.welab.wefe.serving.service.api.service.ServiceShowSQLApi.Output out = new com.welab.wefe.serving.service.api.service.ServiceShowSQLApi.Output();
        out.setResult(JObject.create("sql", sql));
        return out;
    }

    public JObject check(ServiceMySqlModel service, JObject res, String serviceUrl,
                         com.welab.wefe.serving.service.api.service.RouteApi.Input input, String clientIp) {
        long start = System.currentTimeMillis();
        if (service == null) {
            return JObject.create("message", "service not found: url = " + serviceUrl).append("code",
                    ServiceResultEnum.SERVICE_NOT_AVALIABLE.getCode());
        } else if (service.getStatus() != 1) {
            res.append("code", ServiceResultEnum.SERVICE_NOT_AVALIABLE.getCode());
            res.append("message", "invalid request: url = " + serviceUrl);
            return res;
        } else {
            PartnerMysqlModel client = partnerService.queryByCode(input.getCustomerId());
            if (client == null || client.getStatus() != 1) {
                res.append("code", ServiceResultEnum.CUSTOMER_NOT_AUTHORITY.getCode());
                res.append("message",
                        "invalid request: url = " + serviceUrl + ",customerId = " + input.getCustomerId());
                long duration = System.currentTimeMillis() - start;
                log(service, client, duration, clientIp, res.getIntValue("code"));
                return res;
            }
            ClientServiceMysqlModel clientServiceMysqlModel = partnerService.queryByServiceIdAndClientId(service.getId(),
                    client.getId());
            if (clientServiceMysqlModel == null || clientServiceMysqlModel.getStatus() != 1) {
                res.append("code", ServiceResultEnum.CUSTOMER_NOT_AUTHORITY.getCode());
                res.append("message", "invalid request: url = " + serviceUrl + ",customerId=" + client.getCode());
                long duration = System.currentTimeMillis() - start;
                log(service, client, duration, clientIp, res.getIntValue("code"));
                return res;
            }
            if (StringUtils.isNotBlank(clientServiceMysqlModel.getIpAdd()) && !"*".equalsIgnoreCase(clientServiceMysqlModel.getIpAdd())
                    && !Arrays.asList(clientServiceMysqlModel.getIpAdd().split(",|，")).contains(clientIp)) {
                res.append("code", ServiceResultEnum.IP_NOT_AUTHORITY.getCode());
                res.append("message", "invalid request: url = " + serviceUrl + ",clientIp=" + clientIp);
                long duration = System.currentTimeMillis() - start;
                log(service, client, duration, clientIp, res.getIntValue("code"));
                return res;
            }
        }
        return null;
    }

    public ServiceMySqlModel findById(String serviceId) {
        return serviceRepository.findOne("id", serviceId, ServiceMySqlModel.class);
    }

    public JObject executeService(RouteApi.Input input) {
        long start = System.currentTimeMillis();

        String clientIp = ServiceUtil.getIpAddr(input.request);
        ServiceMySqlModel service = serviceRepository.findOne("id", input.getServiceId(), ServiceMySqlModel.class);
        JObject data = JObject.create(input.getData());

        // check params
        JObject res = check(service, data, service.getUrl(), input, clientIp);
        if (res != null) {
            return res;
        }

        JObject result = JObject.create();
        try {
            AbstractServiceProcessor serviceProcessor = ServiceProcessorUtils.get(service.getServiceType());
            result = (JObject) serviceProcessor.process(data, service);
            return result.append("code", ServiceResultEnum.SUCCESS.getCode());
        } catch (Exception e) {
            result.append("code", ServiceResultEnum.SERVICE_FAIL.getCode());
            result.append("message", "服务调用失败: url = " + service.getUrl() + ", message= " + e.getMessage());
            return result;
        } finally {
            PartnerMysqlModel client = partnerService.queryByCode(input.getCustomerId());
            log(service, client, start, clientIp, result.getIntValue("code"));
        }
    }

    private void log(ServiceMySqlModel service, PartnerMysqlModel client, long start, String clientIp, int code) {
        CommonThreadPool.run(() -> apiRequestRecordService.save(service.getId(), service.getName(),
                service.getServiceType(), client.getName(), client.getId(), System.currentTimeMillis() - start, clientIp, code));
    }

    /**
     * 0.参考 SecureAggregation.query 返回结果
     *
     * @throws Exception
     */
    private JSONObject sa_query(JObject data, ServiceMySqlModel model) throws Exception {
        SAQueryServiceProcessor processor = new SAQueryServiceProcessor();
        return processor.process(data, model);
    }

    /**
     * 安全聚合（被查询方） 0.两次交互 1.根据用户参数，生成 QueryDiffieHellmanKeyRequest ，（根据 request 中的
     * queryParams 去数据库中查询对应的【只能是一个数值类型】结果保存到内存中），然后调用
     * QueryDiffieHellmanKeyService.handle 2.生成一个接口，参数为 QuerySAResultRequest ，然后去调用
     * QueryResultService.handle ，然后返回结果
     */
    private JObject sa(JObject data, ServiceMySqlModel model)
            throws StatusCodeWithException {
        SAServiceProcessor processor = new SAServiceProcessor();
        return processor.process(data, model);
    }

    private JObject psi(JObject data, ServiceMySqlModel model)
            throws StatusCodeWithException {
        PsiServiceProcessor psiServiceProcessor = new PsiServiceProcessor();
        return psiServiceProcessor.process(data, model);
    }

    private JObject multi_psi(JObject data, ServiceMySqlModel model) throws Exception {
        MultiPsiServiceProcessor processor = new MultiPsiServiceProcessor();
        return processor.process(data, model);
    }

    private JObject multi_pir(JObject data, ServiceMySqlModel model) throws Exception {
        MultiPirServiceProcessor processor = new MultiPirServiceProcessor();
        return processor.process(data, model);
    }

    private JObject pir(JObject data, ServiceMySqlModel model) throws StatusCodeWithException {
        PirServiceProcessor processor = new PirServiceProcessor();
        return processor.process(data, model);
    }

    public File exportSdk(String serviceId) throws StatusCodeWithException, IOException {
        ServiceMySqlModel model = serviceRepository.findOne("id", serviceId, ServiceMySqlModel.class);
        if (model == null) {
            throw new StatusCodeWithException(StatusCode.DATA_NOT_FOUND);
        }
        int serviceType = model.getServiceType();// 服务类型 1匿踪查询，2交集查询，3安全聚合
        Path basePath = Paths.get(config.getFileBasePath());
//		String basePath = config.getFileBasePath();
        List<File> fileList = new ArrayList<>();
        File readme = new File(basePath.resolve("readme.md").toString());
        if (serviceType == ServiceTypeEnum.PIR.getCode() || serviceType == ServiceTypeEnum.MULTI_PIR.getCode()) {
            // 将需要提供的文件加到这个列表
            fileList.add(new File(basePath.resolve("mpc-pir-sdk-1.0.0.jar").toString()));
            if (serviceType == ServiceTypeEnum.PIR.getCode()) {
                fileList.add(new File(basePath.resolve("PirClient.java").toString()));
            }
            if (serviceType == ServiceTypeEnum.MULTI_PIR.getCode()) {
                fileList.add(new File(basePath.resolve("MultiPir.java").toString()));
            }
            fillReadmeFile(model, readme);
        } else if (serviceType == ServiceTypeEnum.PSI.getCode() || serviceType == ServiceTypeEnum.MULTI_PSI.getCode()) {
            // 将需要提供的文件加到这个列表
            fileList.add(new File(basePath.resolve("mpc-psi-sdk-1.0.0.jar").toString()));
            if (serviceType == ServiceTypeEnum.PSI.getCode()) {
                fileList.add(new File(basePath.resolve("PsiClient.java").toString()));
            }
            if (serviceType == ServiceTypeEnum.MULTI_PSI.getCode()) {
                fileList.add(new File(basePath.resolve("MultiPsi.java").toString()));
            }
            fillReadmeFile(model, readme);
        } else if (serviceType == ServiceTypeEnum.SA.getCode() || serviceType == ServiceTypeEnum.MULTI_SA.getCode()) {
            // 将需要提供的文件加到这个列表
            fileList.add(new File(basePath.resolve("mpc-sa-sdk-1.0.0.jar").toString()));
            fileList.add(new File(basePath.resolve("SaClient.java").toString()));
            fillReadmeFile(model, readme);
        } else {
            fillReadmeFile(null, readme);
        }
        fileList.add(readme);
        String sdkZipName = "sdk.zip";
        String outputPath = basePath.resolve(sdkZipName).toString();
        FileOutputStream fos2 = new FileOutputStream(new File(outputPath));
        ZipUtils.toZip(fileList, fos2);
        File file = new File(outputPath);
        return file;
    }

    private void fillReadmeFile(ServiceMySqlModel model, File readme) throws IOException {
        Map<String, Object> valuesMap = new HashMap<>();
        if (model != null) {
            valuesMap.put("url", model.getUrl());
            valuesMap.put("params", model.getQueryParams() == null ? "" : model.getQueryParams());
            valuesMap.put("desc", model.getName());
            valuesMap.put("method", "POST");
            String templateString = "# url:\n" + "	${url}\n" + "	\n" + "# method:\n" + "	${method}\n" + "	\n"
                    + "# params:\n" + "	${params}\n" + "	\n" + "# desc\n" + "	${desc}";
            StringSubstitutor sub = new StringSubstitutor(valuesMap);
            String content = sub.replace(templateString);
            FileUtils.write(readme, content);
        } else {
            FileUtils.write(readme, "readme.md");
        }
    }

    public ServiceMySqlModel queryById(QueryOneApi.Input input) {

        Specification<ServiceMySqlModel> where = Where.create().equal("id", input.getId())
                .build(ServiceMySqlModel.class);

        Optional<ServiceMySqlModel> one = serviceRepository.findOne(where);
        return one.orElse(null);
    }


    public <T> T callOtherPartnerServing(String url, String api, TreeMap<String, Object> params, Class<T> entityClass) throws StatusCodeWithException {
        String uri = url + "/" + api;

        HttpResponse response = HttpRequest
                .create(uri)
                .setBody(SignUtils.parameterSign(params))
                .postJson();

        if (!response.success()) {
            throw new StatusCodeWithException("调用" + uri + "失败，" + response.getMessage(), StatusCode.REMOTE_SERVICE_ERROR);
        }

        return response
                .getBodyAsJson()
                .getJSONObject("data")
                .toJavaObject(entityClass);
    }
}
