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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringSubstitutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSON;
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
import com.welab.wefe.common.wefe.enums.Algorithm;
import com.welab.wefe.common.wefe.enums.DatabaseType;
import com.welab.wefe.common.wefe.enums.FederatedLearningType;
import com.welab.wefe.common.wefe.enums.JobMemberRole;
import com.welab.wefe.serving.sdk.model.xgboost.XgboostDecisionTreeModel;
import com.welab.wefe.serving.sdk.model.xgboost.XgboostModel;
import com.welab.wefe.serving.sdk.model.xgboost.XgboostNodeModel;
import com.welab.wefe.serving.service.api.service.AddApi;
import com.welab.wefe.serving.service.api.service.DetailApi;
import com.welab.wefe.serving.service.api.service.QueryApi;
import com.welab.wefe.serving.service.api.service.QueryOneApi;
import com.welab.wefe.serving.service.api.service.RouteApi;
import com.welab.wefe.serving.service.api.service.ServiceSQLTestApi.Output;
import com.welab.wefe.serving.service.api.service.UpdateApi.Input;
import com.welab.wefe.serving.service.config.Config;
import com.welab.wefe.serving.service.database.entity.AccountMySqlModel;
import com.welab.wefe.serving.service.database.entity.BaseServiceMySqlModel;
import com.welab.wefe.serving.service.database.entity.ClientServiceMysqlModel;
import com.welab.wefe.serving.service.database.entity.DataSourceMySqlModel;
import com.welab.wefe.serving.service.database.entity.ModelMemberMySqlModel;
import com.welab.wefe.serving.service.database.entity.PartnerMysqlModel;
import com.welab.wefe.serving.service.database.entity.ServiceCallLogMysqlModel;
import com.welab.wefe.serving.service.database.entity.ServiceOrderMysqlModel;
import com.welab.wefe.serving.service.database.entity.TableModelMySqlModel;
import com.welab.wefe.serving.service.database.entity.TableServiceMySqlModel;
import com.welab.wefe.serving.service.database.repository.AccountRepository;
import com.welab.wefe.serving.service.database.repository.BaseServiceRepository;
import com.welab.wefe.serving.service.database.repository.ModelMemberRepository;
import com.welab.wefe.serving.service.database.repository.TableModelRepository;
import com.welab.wefe.serving.service.database.repository.TableServiceRepository;
import com.welab.wefe.serving.service.dto.ModelSqlConfigOutput;
import com.welab.wefe.serving.service.dto.ModelStatusOutput;
import com.welab.wefe.serving.service.dto.PagingOutput;
import com.welab.wefe.serving.service.dto.ServiceDetailOutput;
import com.welab.wefe.serving.service.dto.TreeNode;
import com.welab.wefe.serving.service.dto.TreeNodeData;
import com.welab.wefe.serving.service.enums.CallByMeEnum;
import com.welab.wefe.serving.service.enums.ServiceOrderEnum;
import com.welab.wefe.serving.service.enums.ServiceResultEnum;
import com.welab.wefe.serving.service.enums.ServiceTypeEnum;
import com.welab.wefe.serving.service.manager.FeatureManager;
import com.welab.wefe.serving.service.manager.ModelManager;
import com.welab.wefe.serving.service.service_processor.AbstractServiceProcessor;
import com.welab.wefe.serving.service.service_processor.ServiceProcessorUtils;
import com.welab.wefe.serving.service.utils.MD5Util;
import com.welab.wefe.serving.service.utils.SHA256Utils;
import com.welab.wefe.serving.service.utils.ServiceUtil;
import com.welab.wefe.serving.service.utils.SignUtils;
import com.welab.wefe.serving.service.utils.ZipUtils;

/**
 * 服务 Service
 */
@Service
public class ServiceService {

    protected final Logger LOG = LoggerFactory.getLogger(this.getClass());

    public static final String SERVICE_PRE_URL = "api/";
    @Autowired
    private BaseServiceRepository<BaseServiceMySqlModel> baseServiceRepository;
    @Autowired
    private TableServiceRepository serviceRepository;
    @Autowired
    private TableModelRepository modelRepository;
    @Autowired
    private DataSourceService dataSourceService;
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private UnionServiceService unionServiceService;
    @Autowired
    private PartnerService partnerService;
    @Autowired
    private ClientServiceService clientServiceService;
    @Autowired
    private ServiceOrderService serviceOrderService;
    @Autowired
    private ServiceCallLogService serviceCallLogService;
    @Autowired
    private Config config;
    @Autowired
    private ModelMemberRepository modelMemberRepository;
    @Autowired
    private ModelMemberService modelMemberService;
    @Autowired
    private ModelService modelService;

    public com.welab.wefe.serving.service.api.service.DetailApi.Output detail(
            com.welab.wefe.serving.service.api.service.DetailApi.Input input) throws Exception {
        if (input.getServiceType() < 7) {
            Optional<TableServiceMySqlModel> serviceMySqlModel = serviceRepository.findById(input.getId());
            return detailService(serviceMySqlModel);
        } else {
            Optional<TableModelMySqlModel> modelServiceModelOptional = modelRepository.findById(input.getId());
            if (modelServiceModelOptional != null && modelServiceModelOptional.isPresent()) {
                return detailModel(modelServiceModelOptional);
            } else {
                throw new Exception("data not found");
            }
        }
    }

    private com.welab.wefe.serving.service.api.service.DetailApi.Output detailModel(
            Optional<TableModelMySqlModel> modelServiceModelOptional) {
        TableModelMySqlModel model = modelServiceModelOptional.get();
        DetailApi.Output output = ModelMapper.map(model, DetailApi.Output.class);

        output.setModelParam(JObject.create(model.getModelParam()).getJObject("model_param"));
        output.setMyRole(findMyRoles(model.getServiceId()));
        output.setModelSqlConfig(
                ModelSqlConfigOutput.of(model.getDataSourceId(), model.getSqlScript(), model.getSqlConditionField()));
        output.setProcessor(FeatureManager.getProcessor(model.getServiceId()));
        output.setXgboostTree(
                output.getAlgorithm() == Algorithm.XGBoost ? xgboost(output.getModelParam(), output.getFlType())
                        : null);
        output.setModelStatus(getModelStatus(model, output));

        return output;
    }

    private List<ModelStatusOutput> getModelStatus(TableModelMySqlModel model, DetailApi.Output output) {
        return output.getMyRole().contains(JobMemberRole.promoter)
                && FederatedLearningType.vertical.equals(output.getFlType()) ? checkModelStatus(model.getServiceId())
                        : null;
    }

    private com.welab.wefe.serving.service.api.service.DetailApi.Output detailService(
            Optional<TableServiceMySqlModel> serviceMySqlModel) {
        TableServiceMySqlModel entity = serviceMySqlModel.get();

        DetailApi.Output output = ModelMapper.map(entity, DetailApi.Output.class);
        if (StringUtils.isNotBlank(entity.getDataSource())) {
            output.setDataSource(JSONObject.parseObject(entity.getDataSource()));
        }
        if (StringUtils.isNotBlank(entity.getQueryParams())) {
            output.setQueryParams(Arrays.asList(entity.getQueryParams().split(",")));
        }
        if (StringUtils.isNotBlank(entity.getServiceConfig())) {
            output.setServiceConfig(JSONObject.parseArray(entity.getServiceConfig()));
        }
        if (StringUtils.isNotBlank(entity.getQueryParamsConfig())) {
            output.setQueryParamsConfig(JSONObject.parseArray(entity.getQueryParamsConfig()));
        }
        JSONObject preview = new JSONObject();
        preview.put("id", entity.getId());
        preview.put("params", displayServiceQueryParams(entity.getQueryParams(), entity.getQueryParamsConfig()));
        preview.put("url", SERVICE_PRE_URL + entity.getUrl());
        preview.put("method", "POST");
        output.setPreview(preview);
        return output;
    }

    public String displayServiceQueryParams(String queryParams, String queryParamsConfig) {
        if (StringUtils.isNotBlank(queryParamsConfig)) {
            StringBuilder sb = new StringBuilder();
            JSONArray arr = JSONObject.parseArray(queryParamsConfig);
            for (int i = 0; i < arr.size(); i++) {
                JSONObject jo = arr.getJSONObject(i);
                String name = jo.getString("name");
                String desc = jo.getString("desc");
                sb.append("参数" + (i + 1) + ":" + name + ",描述:" + desc + ";");
            }
            return sb.toString();
        }
        return queryParams;
    }

    private List<JobMemberRole> findMyRoles(String modelId) {
        List<ModelMemberMySqlModel> memberBaseInfo = modelMemberRepository.findByModelIdAndMemberId(modelId,
                CacheObjects.getMemberId());

        return memberBaseInfo.stream().map(ModelMemberMySqlModel::getRole).collect(Collectors.toList());
    }

    private List<ModelStatusOutput> checkModelStatus(String modelId) {
        return modelMemberService.checkAvailableByModelIdAndMemberId(modelId, null);
    }

    private List<TreeNode> xgboost(JObject modelParam, FederatedLearningType flType) {

        JObject feature = modelParam.getJObject("featureNameFidMapping");

        XgboostModel model = modelParam.toJavaObject(XgboostModel.class);

        /**
         * xgboost Tree structure settings
         * <p>
         * tree:[ { "children":[ Object{...}, Object{...} ], "data":{ "feature":"x15",
         * "leaf":false, "left_node":1, "right_node":2,
         * "sitename":"promoter:d3c9199e15154d9eac22690a55abc0f4",
         * "split_maskdict":0.3127503322540728, "weight":-1.6183986372 }, "id":0 } ]
         * </p>
         */
        List<TreeNode> xgboost = new ArrayList<>();
        List<XgboostDecisionTreeModel> trees = model.getTrees();

        for (int i = 0; i < trees.size(); i++) {

            Map<Integer, TreeNode> map = new HashMap<>(16);
            List<XgboostNodeModel> tree = trees.get(i).getTree();
            Map<Integer, Double> splitMaskdict = trees.get(i).getSplitMaskdict();

            // When the tree is on each other
            if (CollectionUtils.isEmpty(tree)) {
                TreeNode node = new TreeNode();
                node.setId(i + "-" + 0);
                xgboost.add(node);
                continue;
            }

            // Composite node
            for (XgboostNodeModel xgboostNodeModel : tree) {
                // Find child nodes
                TreeNode node = new TreeNode();
                TreeNodeData data = new TreeNodeData();
                node.setId(i + "-" + xgboostNodeModel.getId().toString());
                node.setData(data);

                data.setFeature(feature.getString(xgboostNodeModel.getFid().toString()));
                data.setLeaf(xgboostNodeModel.isLeaf());
                data.setLeftNode(xgboostNodeModel.getLeftNodeId());
                data.setRightNode(xgboostNodeModel.getRightNodeId());
                data.setSitename(xgboostNodeModel.getSitename().split(":", -1)[0]);
                data.setWeight(xgboostNodeModel.getWeight());
                data.setThreshold(flType == FederatedLearningType.vertical ? splitMaskdict.get(xgboostNodeModel.getId())
                        : xgboostNodeModel.getBid());

                map.put(xgboostNodeModel.getId(), node);
            }

            // Traversing the processing node tree
            TreeNode root = map.get(0);
            if (root.getData().getLeftNode() != -1 && root.getData().getRightNode() != -1) {
                recursive(map, root);
            }
            xgboost.add(root);
        }

        return xgboost;
    }

    /**
     * Recursive fill tree
     */
    void recursive(Map<Integer, TreeNode> map, TreeNode root) {

        if (root.getData().isLeaf()) {
            return;
        }

        // Find left and right subtrees
        TreeNode leftNode = map.get(root.getData().getLeftNode());
        TreeNode rightNode = map.get(root.getData().getRightNode());

        // Set fill left and right subtrees
        recursive(map, leftNode);
        recursive(map, rightNode);

        // Add child node
        List<TreeNode> children = new ArrayList<>();
        children.add(leftNode);
        children.add(rightNode);
        root.setChildren(children);
    }

    @Transactional(rollbackFor = Exception.class)
    public com.welab.wefe.serving.service.api.service.AddApi.Output saveService(AddApi.Input input)
            throws StatusCodeWithException {
        BaseServiceMySqlModel baseModel = baseServiceRepository.findOne("name", input.getName(),
                BaseServiceMySqlModel.class);
        if (baseModel != null) {
            throw new StatusCodeWithException("服务名称 【" + input.getName() + "】已经存在", StatusCode.PRIMARY_KEY_CONFLICT);
        }
        TableServiceMySqlModel model = serviceRepository.findOne("url", input.getUrl(), TableServiceMySqlModel.class);
        if (model != null) {
            throw new StatusCodeWithException("服务英文名称 【" + input.getUrl() + "】已经存在", StatusCode.PRIMARY_KEY_CONFLICT);
        }
        model = ModelMapper.map(input, TableServiceMySqlModel.class);
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
        output.setParams(displayServiceQueryParams(model.getQueryParams(), model.getQueryParamsConfig()));
        output.setUrl(SERVICE_PRE_URL + model.getUrl());
        return output;
    }

    private String generateIdsTable(TableServiceMySqlModel model) {
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
                    LOG.info(dataSourceModel.getDatabaseName() + "." + dataSource.getString("table") + " count = "
                            + result.size());
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
        if (StringUtils.isNotBlank(input.getServiceId())) {
            where = where.contains("serviceId", input.getServiceId());
        }
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
        Specification<BaseServiceMySqlModel> condition = where.orderBy("updatedTime", OrderBy.desc)
                .build(BaseServiceMySqlModel.class);

        PagingOutput<BaseServiceMySqlModel> page = baseServiceRepository.paging(condition, input);
        List<AccountMySqlModel> accounts = accountRepository.findAll();
        Map<String, String> accountMap = new HashMap<>();
        accounts.stream().forEach(s -> {
            accountMap.put(s.getId(), s.getNickname());
        });
        page.getList().stream().forEach(s -> {
            s.setCreatedBy(StringUtils.isNotBlank(accountMap.get(s.getCreatedBy())) ? accountMap.get(s.getCreatedBy())
                    : s.getCreatedBy());
            s.setUpdatedBy(StringUtils.isNotBlank(accountMap.get(s.getUpdatedBy())) ? accountMap.get(s.getUpdatedBy())
                    : s.getUpdatedBy());
            s.setUrl(SERVICE_PRE_URL + s.getUrl());
        });
        List<QueryApi.Output> list = page.getList().stream().map(x -> ModelMapper.map(x, QueryApi.Output.class))
                .collect(Collectors.toList());

        return PagingOutput.of(page.getTotal(), list);
    }

    public com.welab.wefe.serving.service.api.service.AddApi.Output updateService(Input input)
            throws StatusCodeWithException {
        TableServiceMySqlModel model = serviceRepository.findOne("id", input.getId(), TableServiceMySqlModel.class);
        if (model == null) {
            throw new StatusCodeWithException(StatusCode.DATA_NOT_FOUND);
        }
        if (!model.getName().equalsIgnoreCase(input.getName())) {
            List<BaseServiceMySqlModel> baseModels = baseServiceRepository
                    .findAll(Where.create().equal("name", input.getName()).build(BaseServiceMySqlModel.class));
            if (baseModels.size() > 0) {
                throw new StatusCodeWithException("服务名称 【" + input.getName() + "】已经存在",
                        StatusCode.PRIMARY_KEY_CONFLICT);
            }
        }
        if (!model.getUrl().equalsIgnoreCase(input.getUrl())) {
            List<BaseServiceMySqlModel> baseModels = baseServiceRepository
                    .findAll(Where.create().equal("url", input.getUrl()).build(BaseServiceMySqlModel.class));
            if (baseModels.size() > 0) {
                throw new StatusCodeWithException("服务英文名称 【" + input.getUrl() + "】已经存在",
                        StatusCode.PRIMARY_KEY_CONFLICT);
            }
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
        output.setParams(displayServiceQueryParams(model.getQueryParams(), model.getQueryParamsConfig()));
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
        BaseServiceMySqlModel model = baseServiceRepository.findOne("id", id, BaseServiceMySqlModel.class);
        if (model == null) {
            throw new StatusCodeWithException(StatusCode.DATA_NOT_FOUND);
        }
        if (model.getStatus() == 0) {
            throw new StatusCodeWithException(StatusCode.ILLEGAL_REQUEST);
        }
        model.setStatus(0);
        baseServiceRepository.save(model);
        if (!model.isModelService()) {
            TableServiceMySqlModel m = serviceRepository.findOne("id", id, TableServiceMySqlModel.class);
            unionServiceService.offline2Union(m);
        } else {
            ModelManager.refreshModelEnable(model.getServiceId(), false);
        }
    }

    public void onlineService(String id) throws StatusCodeWithException {
        BaseServiceMySqlModel model = baseServiceRepository.findOne("id", id, BaseServiceMySqlModel.class);
        if (model == null) {
            throw new StatusCodeWithException(StatusCode.DATA_NOT_FOUND);
        }
        if (model.getStatus() == 1) {
            throw new StatusCodeWithException(StatusCode.ILLEGAL_REQUEST);
        }
        model.setStatus(1);
        baseServiceRepository.save(model);
        if (!model.isModelService()) {
            TableServiceMySqlModel m = serviceRepository.findOne("id", id, TableServiceMySqlModel.class);
            if (m.getServiceType() == ServiceTypeEnum.PSI.getCode()) {
                JSONObject dataSource = JObject.parseObject(m.getDataSource());
                String key_calc_rule = dataSource.getString("key_calc_rule");
                m.setQueryParams(key_calc_rule);
            }
            unionServiceService.add2Union(m);
        } else {
            ModelManager.refreshModelEnable(model.getServiceId(), true);
        }
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

    public JObject check(com.welab.wefe.serving.service.api.service.RouteApi.Input input) {

        BaseServiceMySqlModel service = baseServiceRepository.findOne("serviceId", input.getServiceId(),
                BaseServiceMySqlModel.class);
        if (service.getStatus() != 1) {
            return JObject.create().append("code", ServiceResultEnum.SERVICE_NOT_AVALIABLE.getCode()).append("message",
                    "invalid request: url = " + service.getUrl());
        }

        PartnerMysqlModel client = partnerService.queryByCode(input.getPartnerCode());
        if (client.getStatus() != 1) {
            return JObject.create().append("code", ServiceResultEnum.CUSTOMER_NOT_AUTHORITY.getCode()).append("message",
                    "invalid request: url = " + service.getUrl() + ",partnerCode = " + input.getPartnerCode());
        }

        ClientServiceMysqlModel clientServiceMysqlModel = partnerService
                .queryByServiceIdAndClientId(service.getServiceId(), client.getId());
        if (clientServiceMysqlModel.getStatus() != 1) {
            return JObject.create().append("code", ServiceResultEnum.CUSTOMER_NOT_AUTHORITY.getCode()).append("message",
                    "invalid request: url = " + service.getUrl() + ",customerId=" + client.getCode());
        }

        if (!isIpWhiteList(input, clientServiceMysqlModel)) {
            return JObject.create().append("code", ServiceResultEnum.IP_NOT_AUTHORITY.getCode()).append("message",
                    "invalid request: url = " + service.getUrl() + ",clientIp=" + ServiceUtil.getIpAddr(input.request));
        }

        return null;
    }

    private boolean isIpWhiteList(RouteApi.Input input, ClientServiceMysqlModel clientServiceMysqlModel) {
        String clientIp = ServiceUtil.getIpAddr(input.request);

        return input.isModelService()
                || Arrays.asList(clientServiceMysqlModel.getIpAdd().split(",|，")).contains(clientIp);
    }

    public TableServiceMySqlModel findById(String serviceId) {
        return serviceRepository.findOne("id", serviceId, TableServiceMySqlModel.class);
    }

    public JObject executeService(RouteApi.Input input) throws StatusCodeWithException {

        long beginTime = System.currentTimeMillis();

        BaseServiceMySqlModel service = baseServiceRepository.findOne("serviceId", input.getServiceId(),
                BaseServiceMySqlModel.class);

        JObject result = JObject.create();
        ServiceResultEnum status = ServiceResultEnum.SUCCESS;
        try {
            // check params
            JObject res = check(input);
            if (MapUtils.isNotEmpty(res)) {
                result.putAll(res);
                result.putAll(JObject.create(input.getData()));
                status = ServiceResultEnum.SERVICE_FAIL;
                return result;
            }

            AbstractServiceProcessor serviceProcessor = ServiceProcessorUtils.get(service.getServiceType());
            JObject serviceResult = serviceProcessor.process(JObject.create(input.getData()), service);
            result.putAll(serviceResult);
            return result;

        } catch (Exception e) {
            status = ServiceResultEnum.SERVICE_FAIL;
            result.append("message", "服务调用失败: url = " + service.getUrl() + ", message= " + e.getMessage());
            return result;
        } finally {
            String responseId = UUID.randomUUID().toString().replaceAll("-", "");
            result.append("responseId", responseId);
            result.append("code", status.getCode());
            log(input, beginTime, service, result, status, responseId);
        }
    }

    private void log(RouteApi.Input input, long beginTime, BaseServiceMySqlModel service, JObject result,
            ServiceResultEnum status, String responseId) {
        ServiceOrderEnum orderStatus = ServiceResultEnum.SUCCESS.equals(status) ? ServiceOrderEnum.SUCCESS
                : ServiceOrderEnum.FAILED;

        String serviceOrderId = createOrder(service, input, orderStatus);
        callLog(input, serviceOrderId, responseId, result, status.getCode(), status.getMessage(), beginTime);
    }

    private String createOrder(BaseServiceMySqlModel service, RouteApi.Input input, ServiceOrderEnum status) {
        PartnerMysqlModel partner = partnerService.queryByCode(input.getPartnerCode());

        ServiceOrderMysqlModel serviceOrderModel = serviceOrderService.add(service.getId(), service.getName(),
                service.getServiceType(), CallByMeEnum.NO.getValue(), status.getValue(), partner.getId(),
                partner.getName(), CacheObjects.getMemberId(), CacheObjects.getMemberName());
        return serviceOrderModel.getId();
    }

    private void callLog(RouteApi.Input input, String orderId, String responseId, JObject result, Integer responseCode,
            String responseStatus, long beginTime) {
        ServiceCallLogMysqlModel callLog = new ServiceCallLogMysqlModel();
        callLog.setServiceType(ServiceTypeEnum.MachineLearning.getCode());
        callLog.setOrderId(orderId);
        callLog.setServiceId(input.getServiceId());
        callLog.setServiceName(CacheObjects.getServiceName(input.getServiceId()));
        callLog.setRequestData(input.getData());
        callLog.setRequestPartnerId(input.getPartnerCode());
        callLog.setRequestPartnerName(CacheObjects.getPartnerName(input.getPartnerCode()));
        callLog.setRequestId(input.getRequestId());
        callLog.setRequestIp(ServiceUtil.getIpAddr(input.request));
        callLog.setResponseCode(responseCode);
        callLog.setResponseId(responseId);
        callLog.setResponsePartnerId(CacheObjects.getMemberId());
        callLog.setResponsePartnerName(CacheObjects.getMemberName());
        callLog.setResponseData(JSON.toJSONString(result));
        callLog.setCallByMe(CallByMeEnum.NO.getValue());
        callLog.setResponseStatus(responseStatus);
        callLog.setSpendTime(System.currentTimeMillis() - beginTime);
        serviceCallLogService.save(callLog);
    }

    private String preExecuteCallLog(String serviceOrderId, BaseServiceMySqlModel service, PartnerMysqlModel client,
            RouteApi.Input input, String clientIp) {
        ServiceCallLogMysqlModel serviceCallLogMysqlModel = serviceCallLogService.add(serviceOrderId, 0, client.getId(),
                client.getName(), service.getId(), service.getName(), service.getServiceType(), input.getRequestId(),
                JSONObject.toJSONString(input), clientIp);
        return serviceCallLogMysqlModel.getId();
    }

    private void afterExecute(String serviceOrderId, String callLogId, String status, JObject res, long beginTime) {
        serviceOrderService.update(serviceOrderId, status);
//        serviceCallLogService.add(serviceOrderId, CallByMeEnum.NO.getValue(), client.getId(),
//                client.getName(), service.getId(), service.getName(), service.getServiceType(), input.getRequestId(),
//                JSONObject.toJSONString(input), clientIp);
        serviceCallLogService.update(callLogId, CacheObjects.getMemberId(), CacheObjects.getMemberName(),
                res.getString("responseId"), res.toJSONString(), res.getInteger("code"),
                ServiceResultEnum.getValueByCode(res.getInteger("code")), System.currentTimeMillis() - beginTime);
    }

    public File exportSdk(String serviceId) throws StatusCodeWithException, IOException {
        TableServiceMySqlModel model = serviceRepository.findOne("id", serviceId, TableServiceMySqlModel.class);
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

    private void fillReadmeFile(TableServiceMySqlModel model, File readme) throws IOException {
        Map<String, Object> valuesMap = new HashMap<>();
        if (model != null) {
            valuesMap.put("url", CacheObjects.getServingBaseUrl() + SERVICE_PRE_URL + model.getUrl());
            valuesMap.put("params", displayServiceQueryParams(model.getQueryParams(), model.getQueryParamsConfig()));
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

    public ServiceDetailOutput queryById(QueryOneApi.Input input) throws Exception {
        BaseServiceMySqlModel baseService = baseServiceRepository.getOne(input.getId());
        if (baseService == null) {
            throw new Exception("data not found");
        }
        if (!baseService.isModelService()) {
            TableServiceMySqlModel service = serviceRepository.findOne("id", input.getId(),
                    TableServiceMySqlModel.class);
            return ServiceDetailOutput.convertByService(service);
        } else {
            Optional<TableModelMySqlModel> model = modelRepository.findById(input.getId());
            return ServiceDetailOutput.convertByModel(model.get());
        }
    }

    public <T> T callOtherPartnerServing(String url, String api, TreeMap<String, Object> params, Class<T> entityClass)
            throws StatusCodeWithException {
        if (StringUtils.isEmpty(url)) {
            StatusCode.PARAMETER_CAN_NOT_BE_EMPTY.throwException("未配置合作者地址，请先配置地址");
        }

        String uri = url + "/" + api;

        HttpResponse response = HttpRequest.create(uri).setBody(SignUtils.parameterSign(params)).postJson();

        if (!response.success() || response.getCode() != 200 || response.getBodyAsJson().getInteger("code") != 0) {
            StatusCode.REMOTE_SERVICE_ERROR.throwException("调用" + uri + "失败，" + getErrorMessage(response));
        }

        return response.getBodyAsJson().getJSONObject("data").toJavaObject(entityClass);
    }

    private String getErrorMessage(HttpResponse response) {
        return StringUtils.isEmpty(response.getMessage()) ? response.getBodyAsJson().getString("message")
                : response.getMessage();
    }
}
