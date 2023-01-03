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
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Queue;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.SerializationUtils;
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
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.welab.wefe.common.CommonThreadPool;
import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.data.mysql.Where;
import com.welab.wefe.common.data.mysql.enums.OrderBy;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.http.HttpRequest;
import com.welab.wefe.common.http.HttpResponse;
import com.welab.wefe.common.jdbc.base.DatabaseType;
import com.welab.wefe.common.util.FileUtil;
import com.welab.wefe.common.util.JObject;
import com.welab.wefe.common.web.util.CurrentAccountUtil;
import com.welab.wefe.common.web.util.ModelMapper;
import com.welab.wefe.common.wefe.enums.Algorithm;
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
import com.welab.wefe.serving.service.enums.ServiceStatusEnum;
import com.welab.wefe.serving.service.enums.ServiceTypeEnum;
import com.welab.wefe.serving.service.manager.FeatureManager;
import com.welab.wefe.serving.service.manager.ModelManager;
import com.welab.wefe.serving.service.service_processor.AbstractServiceProcessor;
import com.welab.wefe.serving.service.service_processor.ServiceProcessorUtils;
import com.welab.wefe.serving.service.utils.ServiceUtil;
import com.welab.wefe.serving.service.utils.SignUtils;
import com.welab.wefe.serving.service.utils.ZipUtils;
import com.welab.wefe.serving.service.utils.component.ScoreCardComponentUtil;

/**
 * 服务 Service
 */
@Service
public class ServiceService {

    private final static Cache<String, Object> caches = CacheBuilder.newBuilder()
            .expireAfterAccess(10, TimeUnit.MINUTES).build();

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
    private int threads = Math.max(Runtime.getRuntime().availableProcessors(), 4);

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
        output.setScoreCardInfo(
                model.getScoreCardInfo() != null ? ScoreCardComponentUtil.scoreCardInfo(model) : JObject.create());

        JSONObject preview = new JSONObject();
        preview.put("id", model.getServiceId());
        preview.put("url", SERVICE_PRE_URL + model.getUrl());
        preview.put("method", "POST");
        output.setPreview(preview);

        return output;
    }

    private List<ModelStatusOutput> getModelStatus(TableModelMySqlModel model, DetailApi.Output output) {
        return output.getMyRole().contains(JobMemberRole.promoter)
                && !FederatedLearningType.horizontal.equals(output.getFlType()) ? checkModelStatus(model.getServiceId())
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
        preview.put("params", displayServiceQueryParams(entity));
        preview.put("url", SERVICE_PRE_URL + entity.getUrl());
        preview.put("method", "POST");
        output.setPreview(preview);
        return output;
    }

    public String displayServiceQueryParams(TableServiceMySqlModel entity) {
        String result = "";
        if (entity.getServiceType() == ServiceTypeEnum.PSI.getCode()) {
            JSONObject dataSource = JObject.parseObject(entity.getDataSource());
            JSONArray keyCalcRules = dataSource.getJSONArray("key_calc_rules");
            LOG.info("displayServiceQueryParams result = " + keyCalcRules.toJSONString());
            return keyCalcRules.toJSONString();
        } else {
            if (StringUtils.isNotBlank(entity.getQueryParamsConfig())) {
                List<JSONObject> params = new ArrayList<>();
                JSONArray arr = JSONObject.parseArray(entity.getQueryParamsConfig());
                for (int i = 0; i < arr.size(); i++) {
                    JSONObject jo = arr.getJSONObject(i);
                    String name = jo.getString("name");
                    String desc = jo.getString("desc");
                    JSONObject j = new JSONObject();
                    j.put("参数名:", name);
                    j.put("描述:", desc);
                    params.add(j);
                }
                result = JSONObject.toJSONString(params);
            } else {
                result = entity.getQueryParams();
            }
            if (StringUtils.isBlank(result)) {
                return "";
            }
        }
        return result;
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
                data.setThreshold(splitMaskdict.get(xgboostNodeModel.getId()) != null
                        ? splitMaskdict.get(xgboostNodeModel.getId())
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
            throw new StatusCodeWithException(StatusCode.PRIMARY_KEY_CONFLICT, "服务名称 【" + input.getName() + "】已经存在");
        }
        TableServiceMySqlModel model = serviceRepository.findOne("url", input.getUrl(), TableServiceMySqlModel.class);
        if (model != null) {
            throw new StatusCodeWithException(StatusCode.PRIMARY_KEY_CONFLICT, "服务英文名称 【" + input.getUrl() + "】已经存在");
        }
        model = ModelMapper.map(input, TableServiceMySqlModel.class);
        model.setCreatedBy(CurrentAccountUtil.get().getId());
        model.setCreatedTime(new Date());
        model.setUpdatedBy(CurrentAccountUtil.get().getId());
        model.setUpdatedTime(new Date());
        if (model.getServiceType() == ServiceTypeEnum.PSI.getCode()) {// 对于 交集查询 需要额外生成对应的主键数据
            String idsTableName = generateIdsTable(model);
            model.setIdsTableName(idsTableName);
        }
        model.setQueryParams(StringUtils.join(input.getQueryParams(), ","));
        if (input.getQueryParamsConfig() != null && !input.getQueryParamsConfig().isEmpty()) {
            model.setQueryParamsConfig(JSONObject.toJSONString(input.getQueryParamsConfig()));
        }
        serviceRepository.save(model);
        com.welab.wefe.serving.service.api.service.AddApi.Output output = new com.welab.wefe.serving.service.api.service.AddApi.Output();
        output.setId(model.getId());
        output.setParams(displayServiceQueryParams(model));
        output.setUrl(SERVICE_PRE_URL + model.getUrl());
        return output;
    }

    private String generateIdsTable(TableServiceMySqlModel model) throws StatusCodeWithException {
        String keysTableName = "";
        if (model.getServiceType() != ServiceTypeEnum.PSI.getCode()) {// 对于 交集查询 需要额外生成对应的主键数据
            return keysTableName;
        }
        JSONObject dataSource = JObject.parseObject(model.getDataSource());
        DataSourceMySqlModel dataSourceModel = dataSourceService.getDataSourceById(dataSource.getString("id"));
        if (dataSourceModel == null) {
            return keysTableName;
        }
        // 如果是mysql的数据源，则需要生成ID
        if (dataSourceModel.getDatabaseType().name().equalsIgnoreCase(DatabaseType.MySql.name())) {
            String oldIdsTableName = model.getIdsTableName();
            try {
                if (StringUtils.isNotBlank(oldIdsTableName)) {
                    dataSourceService.update(dataSourceModel, "drop table " + oldIdsTableName);
                }
            } catch (StatusCodeWithException e) {
                LOG.error("drop table error , tableName = " + oldIdsTableName);
            }
            keysTableName = generateMySqlIdsTable(dataSourceModel, dataSource);
        } else { // 如果不是mysql的，则直接使用原数据源
            keysTableName = dataSource.getString("table");
        }
        return keysTableName;
    }

    private String generateMySqlIdsTable(final DataSourceMySqlModel oldDataSourceModel, JSONObject dataSource)
            throws StatusCodeWithException {
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
        DataSourceMySqlModel newDataSourceMySqlModel = (DataSourceMySqlModel) SerializationUtils
                .clone(oldDataSourceModel);
        String keysTableName = newDataSourceMySqlModel.getDatabaseName() + "_" + dataSource.getString("table");
        JSONArray keyCalcRules = dataSource.getJSONArray("key_calc_rules");
        List<String> needFields = new ArrayList<>();
        for (int i = 0; i < keyCalcRules.size(); i++) {
            JSONObject item = keyCalcRules.getJSONObject(i);
            String[] fields = item.getString("field").split(",");
            needFields.addAll(Arrays.asList(fields));
        }
        keysTableName += ("_" + format.format(new Date()));
        String sql = "SELECT " + StringUtils.join(needFields, ",") + " FROM "
                + newDataSourceMySqlModel.getDatabaseName() + "." + dataSource.getString("table");
        String tmpSql = "SELECT * FROM " + newDataSourceMySqlModel.getDatabaseName() + "."
                + dataSource.getString("table");
        long count = dataSourceService.count(newDataSourceMySqlModel, tmpSql);
        if (count <= 0) {
            throw new StatusCodeWithException(StatusCode.DATA_NOT_FOUND, "数据源为空");
        }
        // 异步
        final String keysTableNameTmp = keysTableName;
        CommonThreadPool.run(() -> {
            try {
                String createTableSql = String.format(
                        "CREATE TABLE `%s` (`id` varchar(100) NOT NULL ,PRIMARY KEY (`id`) USING BTREE ) ENGINE=InnoDB;",
                        keysTableNameTmp);
                dataSourceService.createTable(createTableSql, DatabaseType.MySql, newDataSourceMySqlModel.getHost(),
                        newDataSourceMySqlModel.getPort(), newDataSourceMySqlModel.getUserName(),
                        newDataSourceMySqlModel.getPassword(), newDataSourceMySqlModel.getDatabaseName());
                List<Map<String, String>> result = dataSourceService.queryList(newDataSourceMySqlModel, sql,
                        needFields);
                if (result == null || result.isEmpty()) {
                    return;
                }
                int partitionSize = 500000;
                int taskNum = Math.max(result.size() / partitionSize, 1);
                LOG.info(newDataSourceMySqlModel.getDatabaseName() + "." + dataSource.getString("table") + " count = "
                        + result.size() + ", taskNum = " + taskNum + ", threads size = " + this.threads);
                List<Queue<Map<String, String>>> partitionList = ServiceUtil.partitionList(result, taskNum);
                result = null;
                ExecutorService executorService1 = Executors.newFixedThreadPool(this.threads);
                Map<String, BlockingQueue<String>> queues = new ConcurrentHashMap<>();
                for (int i = 0; i < partitionList.size(); i++) {
                    final int finalI = i;
                    Queue<Map<String, String>> partition = partitionList.get(i);
                    executorService1.submit(() -> {
                        LOG.info("calcKey begin index = " + finalI + ", partition size = " + partition.size());
                        BlockingQueue<String> queue = new LinkedBlockingQueue<>();
                        queues.put(finalI + "", queue);
                        String insertSql = String.format("insert into %s values (?)", keysTableNameTmp);
                        while (!partition.isEmpty()) {
                            String id = ServiceUtil.calcKey(keyCalcRules, partition.poll());
                            queue.add(id);
                            if (queue.size() > 250000) {
                                try {
                                    dataSourceService.batchInsert(insertSql, DatabaseType.MySql,
                                            newDataSourceMySqlModel.getHost(), newDataSourceMySqlModel.getPort(),
                                            newDataSourceMySqlModel.getUserName(),
                                            newDataSourceMySqlModel.getPassword(),
                                            newDataSourceMySqlModel.getDatabaseName(), new HashSet<>(queue));
                                    queue.clear();
                                } catch (StatusCodeWithException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                        if (!queue.isEmpty()) {
                            try {
                                dataSourceService.batchInsert(insertSql, DatabaseType.MySql,
                                        newDataSourceMySqlModel.getHost(), newDataSourceMySqlModel.getPort(),
                                        newDataSourceMySqlModel.getUserName(), newDataSourceMySqlModel.getPassword(),
                                        newDataSourceMySqlModel.getDatabaseName(), new HashSet<>(queue));
                                queue.clear();
                            } catch (StatusCodeWithException e) {
                                e.printStackTrace();
                            }
                        }
                        LOG.info("calcKey end index = " + finalI + ", queue size = " + queue.size());
                    });
                }
                executorService1.shutdown();
                try {
                    while (!executorService1.awaitTermination(10, TimeUnit.SECONDS)) {
                        int c = 0;
                        for (Entry<String, BlockingQueue<String>> queue : queues.entrySet()) {
                            LOG.info("index:" + (c++) + ", queue size =" + queue.getValue().size());
                        }
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    executorService1.shutdown();
                    executorService1 = null;
                }
                LOG.info("end batchInsert");
            } catch (StatusCodeWithException e1) {
                e1.printStackTrace();
            }
        });
        return keysTableName;
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
        Specification<BaseServiceMySqlModel> condition = where.orderBy("updatedTime", OrderBy.desc).build();

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
            StatusCode.DATA_NOT_FOUND.throwException();
        }
        if (model.getStatus() == ServiceStatusEnum.USED.getCode()) {
            throw new StatusCodeWithException(StatusCode.ILLEGAL_REQUEST, "上线的服务不允许更新");
        }
        if (!model.getName().equalsIgnoreCase(input.getName())) {
            List<BaseServiceMySqlModel> baseModels = baseServiceRepository
                    .findAll(Where.create().equal("name", input.getName()).build(BaseServiceMySqlModel.class));
            if (baseModels.size() > 0) {
                throw new StatusCodeWithException(
                        StatusCode.PRIMARY_KEY_CONFLICT,
                        "服务名称 【" + input.getName() + "】已经存在");
            }
        }
        if (!model.getUrl().equalsIgnoreCase(input.getUrl())) {
            List<BaseServiceMySqlModel> baseModels = baseServiceRepository
                    .findAll(Where.create().equal("url", input.getUrl()).build(BaseServiceMySqlModel.class));
            if (baseModels.size() > 0) {
                throw new StatusCodeWithException(
                        StatusCode.PRIMARY_KEY_CONFLICT,
                        "服务英文名称 【" + input.getUrl() + "】已经存在");
            }
        }

        if (StringUtils.isNotBlank(input.getName())) {
            model.setName(input.getName());
        }
        if (StringUtils.isNotBlank(input.getUrl())) {
            model.setUrl(input.getUrl());
        }
        if (StringUtils.isNotBlank(input.getOperator())) {
            model.setOperator(input.getOperator());
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
        model.setUpdatedBy(CurrentAccountUtil.get().getId());
        model.setUpdatedTime(new Date());
        if (model.getServiceType() == ServiceTypeEnum.PSI.getCode()) {// 对于 交集查询 需要额外生成对应的主键数据
            String idsTableName = generateIdsTable(model);
            model.setIdsTableName(idsTableName);
        }
        serviceRepository.save(model);
        com.welab.wefe.serving.service.api.service.AddApi.Output output = new com.welab.wefe.serving.service.api.service.AddApi.Output();
        output.setId(model.getId());
        output.setParams(displayServiceQueryParams(model));
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
            StatusCode.DATA_NOT_FOUND.throwException();
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
            StatusCode.DATA_NOT_FOUND.throwException();
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
        String sql = ServiceUtil.generateOneSQL(input.getParams(), dataSource, dataSourceModel.getDatabaseName());
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
        String sql = ServiceUtil.generateOneSQL(input.getParams(), dataSource, dataSourceModel.getDatabaseName());
        com.welab.wefe.serving.service.api.service.ServiceShowSQLApi.Output out = new com.welab.wefe.serving.service.api.service.ServiceShowSQLApi.Output();
        out.setResult(JObject.create("sql", sql));
        return out;
    }

    public JObject check(com.welab.wefe.serving.service.api.service.RouteApi.Input input,
            BaseServiceMySqlModel service) {
        if (service.getStatus() != 1) {
            return JObject.create().append("code", ServiceResultEnum.SERVICE_NOT_AVALIABLE.getCode()).append("message",
                    "invalid request: url = " + service.getUrl());
        }
        PartnerMysqlModel client = partnerService.queryByCode(input.getPartnerCode());
        if (client.getStatus() != 1) {
            return JObject.create().append("code", ServiceResultEnum.CUSTOMER_NOT_AUTHORITY.getCode()).append("message",
                    "invalid request: url = " + service.getUrl() + ",partnerCode = " + input.getPartnerCode());
        }

        ClientServiceMysqlModel clientServiceMysqlModel = clientServiceService
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

        return clientServiceMysqlModel.getIpAdd() != null
                && (Arrays.asList(clientServiceMysqlModel.getIpAdd().split(",|，")).contains(clientIp)
                        || Arrays.asList(clientServiceMysqlModel.getIpAdd().split(",|，")).contains("*"));
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
        AbstractServiceProcessor serviceProcessor = null;
        try {
            // check params
            LOG.info("check begin ... ");
            long start = System.currentTimeMillis();
            if (caches.getIfPresent(input.getRequestId()) == null) {
                JObject res = check(input, service);
                LOG.info("check end ..., duration =" + (System.currentTimeMillis() - start));
                if (MapUtils.isNotEmpty(res)) {
                    result.putAll(res);
                    result.putAll(JObject.create(input.getData()));
                    status = ServiceResultEnum.SERVICE_FAIL;
                    return result;
                }
                caches.put(input.getRequestId(), Boolean.TRUE);
            }
            serviceProcessor = ServiceProcessorUtils.get(service.getServiceType());
            JObject serviceResult = serviceProcessor.process(JObject.create(input.getData()), service);
            result.putAll(serviceResult);
            return result;
        } catch (Exception e) {
            LOG.error("executeService error, ", e);
            status = ServiceResultEnum.SERVICE_FAIL;
            result.append("message", "服务调用失败: url = " + service.getUrl() + ", message= " + e.getMessage());
            return result;
        } finally {
            String responseId = UUID.randomUUID().toString().replaceAll("-", "");
            result.append("responseId", responseId);
            result.append("code", status.getCode());
            JObject tmpResult = new JObject((JSONObject) result.clone());
            if (serviceProcessor != null) {
                tmpResult.put("subCalllogs", serviceProcessor.calllogs());
            }
            log(input, beginTime, service, tmpResult, status, responseId);
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

        ServiceOrderMysqlModel serviceOrderModel = serviceOrderService.add(service.getServiceId(), service.getName(),
                service.getServiceType(),
                input.getPartnerCode().equalsIgnoreCase(CacheObjects.getMemberId()) ? CallByMeEnum.YES.getCode()
                        : (partner.getIsMe() ? CallByMeEnum.YES.getCode() : CallByMeEnum.NO.getCode()),
                status.getValue(), partner.getCode(), partner.getName(), CacheObjects.getMemberId(),
                CacheObjects.getMemberName());
        return serviceOrderModel.getId();
    }

    private void callLog(RouteApi.Input input, String orderId, String responseId, JObject result, Integer responseCode,
            String responseStatus, long beginTime) {
        ServiceCallLogMysqlModel callLog = new ServiceCallLogMysqlModel();
        callLog.setServiceType(ServiceTypeEnum.MachineLearning.getCode());
        callLog.setOrderId(orderId);
        callLog.setServiceId(input.getServiceId());
        callLog.setServiceName(CacheObjects.getServiceName(input.getServiceId()));
        callLog.setRequestData(ServiceUtil.abbreviate(input.getData(), 12500));
        callLog.setRequestPartnerId(input.getPartnerCode());
        callLog.setRequestPartnerName(CacheObjects.getPartnerName(input.getPartnerCode()));
        callLog.setRequestId(input.getRequestId());
        callLog.setRequestIp(ServiceUtil.getIpAddr(input.request));
        callLog.setResponseCode(responseCode);
        callLog.setResponseId(responseId);
        callLog.setResponsePartnerId(CacheObjects.getMemberId());
        callLog.setResponsePartnerName(CacheObjects.getMemberName());
        callLog.setResponseData(ServiceUtil.abbreviate(JSON.toJSONString(result), 12500));
        callLog.setCallByMe(
                input.getPartnerCode().equalsIgnoreCase(CacheObjects.getMemberId()) ? CallByMeEnum.YES.getCode()
                        : CallByMeEnum.NO.getCode());
        callLog.setResponseStatus(responseStatus);
        callLog.setSpendTime(System.currentTimeMillis() - beginTime);
        serviceCallLogService.save(callLog);
    }

    public File exportSdk(String serviceId) throws StatusCodeWithException, IOException {
        BaseServiceMySqlModel model = baseServiceRepository.findOne("serviceId", serviceId,
                BaseServiceMySqlModel.class);

        if (model == null) {
            StatusCode.DATA_NOT_FOUND.throwException();
        }

        if (model.getServiceType() == 7) {
            return modelServiceSdk(serviceId);
        }

        return mpcServiceSdk(serviceId);
    }

    private File modelServiceSdk(String serviceId) throws StatusCodeWithException, IOException {
        Path outputPath = Paths.get(config.getFileBasePath()).resolve(serviceId + ".zip");
        if (outputPath.toFile().exists()) {
            return outputPath.toFile();
        }

        TableModelMySqlModel model = modelRepository.findOne("serviceId", serviceId, TableModelMySqlModel.class);
        if (model == null) {
            StatusCode.DATA_NOT_FOUND.throwException();
        }

        List<File> fileList = new ArrayList<>();

        Path path = Paths.get(config.getFileBasePath()).resolve(serviceId + ".java");
        File file = new File(path.toString());

        // 将需要提供的文件加到这个列表
        List<String> stringList = FileUtil
                .readAllForLine(Paths.get(config.getFileBasePath()).resolve("ApiExample.java").toString(), "UTF-8");
        stringList.stream().forEach(x -> {
            try {
                FileUtil.writeTextToFile(String.format(x, serviceId) + System.lineSeparator(), path, true);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        fileList.add(file);

        File outputFile = new File(outputPath.toString());
        FileOutputStream fos2 = new FileOutputStream(outputFile);
        ZipUtils.toZip(fileList, fos2);

        file.delete();
        return outputFile;
    }

    private File mpcServiceSdk(String serviceId) throws StatusCodeWithException, IOException {
        TableServiceMySqlModel model = serviceRepository.findOne("id", serviceId, TableServiceMySqlModel.class);
        if (model == null) {
            StatusCode.DATA_NOT_FOUND.throwException();
        }
        int serviceType = model.getServiceType();// 服务类型 1匿踪查询，2交集查询，3安全聚合
        Path basePath = Paths.get(config.getFileBasePath());
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
            valuesMap.put("serverUrl", CacheObjects.getServingBaseUrl());
            valuesMap.put("apiName", SERVICE_PRE_URL + model.getUrl());
            valuesMap.put("params", displayServiceQueryParams(model));
            valuesMap.put("desc", model.getName());
            valuesMap.put("method", "POST");
            String templateString = "# serverUrl:\n" + "	${serverUrl}\n" + "	\n" + "# apiName:\n"
                    + "	${apiName}\n" + "	\n" + "# method:\n" + " ${method}\n" + "    \n" + "# params:\n"
                    + "	${params}\n" + "	\n" + "# desc\n" + "	${desc}";
            LOG.info("displayServiceQueryParams result = " + valuesMap.get("params"));
            StringSubstitutor sub = new StringSubstitutor(valuesMap);
            String content = sub.replace(templateString);
            FileUtils.write(readme, content);
        } else {
            FileUtils.write(readme, "readme.md");
        }
    }

    public ServiceDetailOutput queryById(QueryOneApi.Input input) throws Exception {
        BaseServiceMySqlModel baseService = baseServiceRepository.findOne("serviceId", input.getId(),
                BaseServiceMySqlModel.class);
        if (baseService == null) {
            throw new Exception("data not found");
        }
        if (!baseService.isModelService()) {
            TableServiceMySqlModel service = serviceRepository.findOne("id", input.getId(),
                    TableServiceMySqlModel.class);
            return ServiceDetailOutput.convertByService(service);
        } else {
            Optional<TableModelMySqlModel> model = modelRepository.findById(baseService.getId());
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
