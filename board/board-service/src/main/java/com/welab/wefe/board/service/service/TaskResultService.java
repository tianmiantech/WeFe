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

package com.welab.wefe.board.service.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.welab.wefe.board.service.api.data_resource.table_data_set.DetailApi;
import com.welab.wefe.board.service.api.project.job.task.GetFeatureApi;
import com.welab.wefe.board.service.component.DataIOComponent;
import com.welab.wefe.board.service.component.base.io.Names;
import com.welab.wefe.board.service.component.base.io.NodeOutputItem;
import com.welab.wefe.board.service.component.feature.FeatureSelectionComponent;
import com.welab.wefe.board.service.component.feature.HorzOneHotComponent;
import com.welab.wefe.board.service.component.feature.HorzOneHotComponent.Params.MemberInfoModel;
import com.welab.wefe.board.service.database.entity.data_resource.TableDataSetMysqlModel;
import com.welab.wefe.board.service.database.entity.job.TaskMySqlModel;
import com.welab.wefe.board.service.database.entity.job.TaskResultMySqlModel;
import com.welab.wefe.board.service.database.repository.TaskRepository;
import com.welab.wefe.board.service.database.repository.TaskResultRepository;
import com.welab.wefe.board.service.dto.entity.MemberFeatureInfoModel;
import com.welab.wefe.board.service.dto.entity.data_resource.output.TableDataSetOutputModel;
import com.welab.wefe.board.service.exception.FlowNodeException;
import com.welab.wefe.board.service.exception.MemberGatewayException;
import com.welab.wefe.board.service.model.FlowGraph;
import com.welab.wefe.board.service.model.FlowGraphNode;
import com.welab.wefe.board.service.service.data_resource.table_data_set.TableDataSetService;
import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.data.mysql.Where;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.util.JObject;
import com.welab.wefe.common.web.util.CurrentAccountUtil;
import com.welab.wefe.common.wefe.enums.ComponentType;
import com.welab.wefe.common.wefe.enums.JobMemberRole;
import com.welab.wefe.common.wefe.enums.TaskResultType;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * @author zane.luo
 */
@Service
public class TaskResultService extends AbstractService {

    @Autowired
    TaskResultRepository taskResultRepository;

    @Autowired
    ProjectFlowNodeService projectFlowNodeService;

    @Autowired
    TaskRepository taskRepository;

    @Autowired
    ProjectMemberService projectMemberService;

    @Autowired
    private JobService jobService;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private TableDataSetService tableDataSetService;

    @Autowired
    private GatewayService gatewayService;
    @Autowired
    private TaskService taskService;

    /**
     * There are multiple results for mixed federations
     */
    public List<TaskResultMySqlModel> findList(String jobId, String nodeId, JobMemberRole role, String type) {

        Specification<TaskResultMySqlModel> where = Where
                .create()
                .equal("jobId", jobId)
                .equal("flowNodeId", nodeId)
                .equal("role", role).equal("type", type)
                .build(TaskResultMySqlModel.class);

        return taskResultRepository.findAll(where);
    }


    public TaskResultMySqlModel findOne(String jobId, String nodeId, JobMemberRole role, String type) {

        Specification<TaskResultMySqlModel> where = Where
                .create()
                .equal("jobId", jobId)
                .equal("flowNodeId", nodeId)
                .equal("role", role)
                .equal("type", type)
                .build(TaskResultMySqlModel.class);

        return taskResultRepository.findOne(where).orElse(null);
    }

    public List<TaskResultMySqlModel> listAllResult(String taskId) {
        Specification<TaskResultMySqlModel> where = Where
                .create()
                .equal("taskId", taskId)
                .build(TaskResultMySqlModel.class);

        return taskResultRepository.findAll(where);
    }

    /**
     * find task result by id and type
     */
    public TaskResultMySqlModel findByTaskIdAndType(String taskId, String type) {

        Specification<TaskResultMySqlModel> where = Where
                .create()
                .equal("taskId", taskId)
                .equal("type", type)
                .build(TaskResultMySqlModel.class);

        return taskResultRepository.findOne(where).orElse(null);
    }


    private List<JObject> parseBinningResult(TaskResultMySqlModel featureBinningTaskResult) {
        JObject binningResult = JObject.create(featureBinningTaskResult.getResult());
        JObject promoterBinningResult = binningResult.getJObject("model_param").getJObject("binningResult");
        List<JObject> providerBinningResults = binningResult.getJSONList("model_param.providerResults");
        List<JObject> binningResults = new ArrayList<>();
        binningResults.add(promoterBinningResult);
        binningResults.addAll(providerBinningResults);
        return binningResults;
    }

    /**
     * Get feature list
     * <p>
     * has_feature_calculation: true 表示支持CV/IV过滤 从计算特征价值 组件获取CV值/IV值
     * has_feature_statistic: true 表示支持缺失率 特征统计组件获取缺失率
     * <p>
     * 1.做了特征统计（不管横向还是纵向还是混合），那就有 缺失率和cv
     * <p>
     * 2.做了计算特征价值（只有纵向流程有），就有cv和iv。
     * <p>
     * 3.做了分箱（不管横向还是纵向还是混合），那就有iv
     */
    public GetFeatureApi.Output getResultFeature(GetFeatureApi.Input input) throws StatusCodeWithException {


        FlowGraph graph = jobService.createFlowGraph(input.getFlowId());
        FlowGraphNode node = graph.getNode(input.getFlowNodeId());

        // 获取当前节点能够使用的特征列表
        List<MemberFeatureInfoModel> members = getMemberFeatures(graph, node);

        GetFeatureApi.Output out = new GetFeatureApi.Output();
        out.setMembers(members);

        // 如果当前节点是特征选择组件，则需要告知是否可使用 cv、iv、缺失率 进行筛选，并填充各特征的cv、iv、缺失率。
        if (node.getComponentType() == ComponentType.FeatureSelection && graph.getLastJob() != null) {
            setCvIvMissingRate(out, graph, node);
        }


        return out;
    }

    /**
     * 缺失率：从特征统计获取
     * CV：从特征统计获取
     * IV：从WOE编码（分箱）获取
     */
    private void setCvIvMissingRate(GetFeatureApi.Output out, FlowGraph graph, FlowGraphNode node) {

        setInfoAboutStatistic(out, graph, node);

        setInfoAboutBinning(out, graph, node);
    }

    private void setInfoAboutBinning(GetFeatureApi.Output out, FlowGraph graph, FlowGraphNode node) {
        String jobId = graph.getLastJob().getJobId();

        FlowGraphNode featureBinningNode = graph.findOneNodeFromParent(node, x -> x.getComponentType().isBinning());
        if (featureBinningNode == null) {
            return;
        }
        TaskResultMySqlModel taskResult = findByJobIdAndComponentTypeAndType(
                jobId,
                featureBinningNode.getComponentType(),
                TaskResultType.model_binning,
                graph.getLastJob().getMyRole()
        );
        if (taskResult == null) {
            return;
        }
        out.setHasIV(true);

        List<JObject> featureBinningResults = parseBinningResult(taskResult);

        for (JObject memberObj : featureBinningResults) {

            String memberId = memberObj.getString("memberId");
            JobMemberRole role = JobMemberRole.valueOf(memberObj.getString("role"));

            JObject binningResult = memberObj.getJObject("binningResult");

            if (binningResult != null) {

                for (String feature : binningResult.keySet()) {
                    JObject binningData = binningResult.getJObject(feature);
                    double iv = binningData.getDouble("iv");
                    out.putIv(memberId, role, feature, iv);
                }
            }

        }
    }

    private void setInfoAboutStatistic(GetFeatureApi.Output out, FlowGraph graph, FlowGraphNode node) {
        String jobId = graph.getLastJob().getJobId();

        FlowGraphNode featureStatisticNode = graph.findOneNodeFromParent(node, x -> x.getComponentType().isStatistic());
        if (featureStatisticNode == null) {
            return;
        }
        TaskResultMySqlModel taskResult = findByJobIdAndComponentTypeAndType(
                jobId,
                featureStatisticNode.getComponentType(),
                TaskResultType.data_feature_statistic,
                graph.getLastJob().getMyRole()
        );
        if (taskResult == null) {
            return;
        }
        out.setHasFeatureStatistic(true);
        out.setHasLossRate(true);
        out.setHasCV(true);


        JObject result = JObject.create(taskResult.getResult());

        List<JObject> resultMembers = result.getJSONList("members");
        for (JObject memberObj : resultMembers) {
            String memberId = memberObj.getString("member_id");
            JobMemberRole role = JobMemberRole.valueOf(memberObj.getString("role"));

            JObject feature_statistic = memberObj.getJObject("feature_statistic");

            // Calculate the missing rate from the statistical data of the feature and store it in the map, key: "x1" value: 0.01
            // 遍历特征列表，计算缺失率。
            for (String feature : feature_statistic.keySet()) {
                JObject statisticData = feature_statistic.getJObject(feature);

                double not_null_count = statisticData.getDouble("not_null_count");
                double missing_count = statisticData.getDouble("missing_count");
                double missingValue = missing_count / (missing_count + not_null_count);
                out.putMissingRate(memberId, role, feature, missingValue);

                double cv = statisticData.getDouble("cv");
                out.putCv(memberId, role, feature, cv);
            }

        }

    }

    /**
     * Find the feature column in the training data set:
     * take the feature column from (DataIO/binning/feature filtering)
     *
     * @throws StatusCodeWithException
     */
    public List<MemberFeatureInfoModel> getMemberFeatures(FlowGraph graph, FlowGraphNode node) throws StatusCodeWithException {
        List<NodeOutputItem> nodeOutputItems = node.getComponent().findInputNodes(graph, node);

        // There is only one training data set by default,
        // and the situation where there are multiple training data sets will not be processed for the time being.
        NodeOutputItem trainDataSetNodeOutputItem = nodeOutputItems.stream().filter(x -> x.getName().equals(Names.Data.NORMAL_DATA_SET)).findFirst().orElse(null);
        if (trainDataSetNodeOutputItem == null) {
            for (NodeOutputItem item : nodeOutputItems) {
                // Going here shows that the training data set is not found,
                // and you need to continue to look for the parent node.
                return getMemberFeatures(graph, graph.getNode(item.getNodeId()));
            }
        }

        if (trainDataSetNodeOutputItem.getComponentType() == ComponentType.DataIO) {

            return getDataIOFeature(graph.getNode(trainDataSetNodeOutputItem.getNodeId()));
        } else if (trainDataSetNodeOutputItem.getComponentType() == ComponentType.FeatureSelection) {

            return getFeatureSelectFeature(graph.getNode(trainDataSetNodeOutputItem.getNodeId()), graph);
        } else if (trainDataSetNodeOutputItem.getComponentType() == ComponentType.HorzOneHot || trainDataSetNodeOutputItem.getComponentType() == ComponentType.VertOneHot) {
            return getOneHotFeature(graph.getNode(trainDataSetNodeOutputItem.getNodeId()), graph);
        } else {
            return getMemberFeatures(graph, graph.getNode(trainDataSetNodeOutputItem.getNodeId()));
        }
    }

    private List<MemberFeatureInfoModel> getOneHotFeature(FlowGraphNode node, FlowGraph flowGraph)
            throws StatusCodeWithException {
        List<MemberFeatureInfoModel> members = new ArrayList<>();

        FlowGraphNode dataIONode = flowGraph.findOneNodeFromParent(node, ComponentType.DataIO);
        DataIOComponent.Params dataIOParams = JObject.create(dataIONode.getParams())
                .toJavaObject(DataIOComponent.Params.class);

        List<DataIOComponent.DataSetItem> dataSetItems = dataIOParams.getDataSetList();

        // need filter
        HorzOneHotComponent.Params params = JObject.create(node.getParams())
                .toJavaObject(HorzOneHotComponent.Params.class);
        if (params == null || CollectionUtils.isEmpty(params.getMembers())) {
            return getMemberFeatures(flowGraph, flowGraph.getNode(node.getNodeId()));
        }
        for (MemberInfoModel memberInfoModel : params.getMembers()) {
            for (DataIOComponent.DataSetItem dataSetItem : dataSetItems) {
                if (memberInfoModel.getMemberRole() == dataSetItem.getMemberRole()
                        && memberInfoModel.getMemberId().equals(dataSetItem.getMemberId())) {
                    List<String> needPassFeatures = memberInfoModel.getFeatures();
                    MemberFeatureInfoModel member = new MemberFeatureInfoModel();
                    member.setMemberId(dataSetItem.getMemberId());
                    member.setMemberRole(dataSetItem.getMemberRole());
                    List<MemberFeatureInfoModel.Feature> features = new ArrayList<>();
                    for (String name : dataSetItem.getFeatures()) {
                        if (needPassFeatures != null && needPassFeatures.contains(name)) {
                            continue; // pass
                        }
                        MemberFeatureInfoModel.Feature feature = new MemberFeatureInfoModel.Feature();
                        feature.setName(name);
                        features.add(feature);
                    }
                    member.setFeatures(features);
                    member.setDataSetId(dataSetItem.getDataSetId());
                    member.setMemberName(CacheObjects.getMemberName(member.getMemberId()));
                    members.add(member);
                }
            }
        }
        if (flowGraph.getLastJob() != null) {
            TableDataSetMysqlModel myTmpDataSet = tableDataSetService.query(flowGraph.getLastJob().getJobId(),
                    node.getComponentType());
            if (myTmpDataSet != null) {
                for (MemberFeatureInfoModel member : members) {
                    if (!member.getMemberId().equalsIgnoreCase(CacheObjects.getMemberId())) {
                        DetailApi.Input input = new DetailApi.Input();
                        input.setId(myTmpDataSet.getId());
                        try {
                            TableDataSetOutputModel output = gatewayService.callOtherMemberBoard(member.getMemberId(),
                                    JobMemberRole.promoter, DetailApi.class, input, TableDataSetOutputModel.class);
                            if (output != null) {
                                LOG.info("getOneHotFeature request : " + JObject.toJSONString(input));
                                List<String> newColumnNameList = new ArrayList<>(
                                        Arrays.asList(output.getFeatureNameList().split(",")));
                                List<MemberFeatureInfoModel.Feature> oldFeatures = member.getFeatures();

                                List<MemberFeatureInfoModel.Feature> newFeatures = new ArrayList<>();
                                for (MemberFeatureInfoModel.Feature feature : oldFeatures) {
                                    if (newColumnNameList.contains(feature.getName())) {
                                        newFeatures.add(feature);
                                        newColumnNameList.remove(feature.getName());
                                    }
                                }
                                if (newColumnNameList != null && !newColumnNameList.isEmpty()) {
                                    for (String s : newColumnNameList) {
                                        MemberFeatureInfoModel.Feature f = new MemberFeatureInfoModel.Feature();
                                        f.setName(s);
                                        newFeatures.add(f);
                                    }
                                }
                                member.setFeatures(newFeatures);
                            }
                        } catch (MemberGatewayException e) {
                            throw new FlowNodeException(node, member.getMemberId());
                        }
                    } else {
                        List<String> newColumnNameList = new ArrayList<>(
                                Arrays.asList(myTmpDataSet.getFeatureNameList().split(",")));
                        List<MemberFeatureInfoModel.Feature> oldFeatures = member.getFeatures();

                        List<MemberFeatureInfoModel.Feature> newFeatures = new ArrayList<>();
                        for (MemberFeatureInfoModel.Feature feature : oldFeatures) {
                            if (newColumnNameList.contains(feature.getName())) {
                                newFeatures.add(feature);
                                newColumnNameList.remove(feature.getName());
                            }
                        }
                        if (newColumnNameList != null && !newColumnNameList.isEmpty()) {
                            for (String s : newColumnNameList) {
                                MemberFeatureInfoModel.Feature f = new MemberFeatureInfoModel.Feature();
                                f.setName(s);
                                newFeatures.add(f);
                            }
                        }
                        member.setFeatures(newFeatures);
                    }
                }
            }
        }
        return members;
    }


    /**
     * From the feature column in the DataIO node params
     */
    public List<MemberFeatureInfoModel> getDataIOFeature(FlowGraphNode node) {

        List<MemberFeatureInfoModel> members = new ArrayList<>();

        // Find the params of the DataIO node and assemble the feature column
        DataIOComponent.Params params = JObject
                .create(node.getParams())
                .toJavaObject(DataIOComponent.Params.class);

        List<DataIOComponent.DataSetItem> dataSetItems = params.getDataSetList();
        for (DataIOComponent.DataSetItem dataSetItem : dataSetItems) {
            MemberFeatureInfoModel member = new MemberFeatureInfoModel();
            member.setMemberId(dataSetItem.getMemberId());
            member.setMemberRole(dataSetItem.getMemberRole());

            List<MemberFeatureInfoModel.Feature> features = new ArrayList<>();
            for (String name : dataSetItem.getFeatures()) {
                MemberFeatureInfoModel.Feature feature = new MemberFeatureInfoModel.Feature();
                feature.setName(name);
                features.add(feature);
            }
            member.setFeatures(features);
            member.setDataSetId(dataSetItem.getDataSetId());
            member.setMemberName(CacheObjects.getMemberName(member.getMemberId()));
            members.add(member);
        }

        return members;
    }

    /**
     * Filter the feature column in the node params from the feature
     */
    public List<MemberFeatureInfoModel> getFeatureSelectFeature(FlowGraphNode node, FlowGraph flowGraph) {
        JObject featureSelectFeature = JObject.create();

        FeatureSelectionComponent.Params params = JObject
                .create(node.getParams())
                .toJavaObject(FeatureSelectionComponent.Params.class);

        FlowGraphNode dataIONode = flowGraph.findOneNodeFromParent(node, ComponentType.DataIO);
        DataIOComponent.Params dataIOParams = JObject
                .create(dataIONode.getParams())
                .toJavaObject(DataIOComponent.Params.class);

        List<DataIOComponent.DataSetItem> dataSetList = dataIOParams.getDataSetList();

        for (MemberFeatureInfoModel memberFeatureInfoModel : params.getMembers()) {
            for (DataIOComponent.DataSetItem dataSetItem : dataSetList) {
                if (memberFeatureInfoModel.getMemberRole() == dataSetItem.getMemberRole() && memberFeatureInfoModel.getMemberId().equals(dataSetItem.getMemberId())) {
                    memberFeatureInfoModel.setDataSetId(dataSetItem.getDataSetId());
                }
            }
        }

        featureSelectFeature.append("members", params.getMembers());

        return params.getMembers();
    }

    /**
     * Get the feature column in the binning component result
     */
    public List<MemberFeatureInfoModel> getBinningResultFeature(TaskMySqlModel task) {
        List<MemberFeatureInfoModel> members = new ArrayList<>();

        TaskResultMySqlModel taskResultMySqlModel = findByTaskIdAndType(task.getTaskId(), TaskResultType.model_binning.name());

        if (taskResultMySqlModel == null) {
            return members;
        }
        JObject resultObj = JObject.create(taskResultMySqlModel.getResult());
        JObject modelParam = resultObj.getJObject("model_param");
        JObject binningResult = modelParam.getJObject("binningResult");
        List<JObject> providerResults = modelParam.getJSONList("providerResults");

        // Analyze the feature column of the promoter side
        members.add(parserBinningResult(binningResult));

        // Analyze the characteristics of the provider
        for (JObject providerObj : providerResults) {
            members.add(parserBinningResult(providerObj));
        }

        return members;
    }


    public MemberFeatureInfoModel parserBinningResult(JObject result) {
        MemberFeatureInfoModel member = new MemberFeatureInfoModel();
        member.setMemberId(result.getString("memberId"));
        member.setMemberRole(JobMemberRole.valueOf(result.getString("role")));
        Set<String> featureNames = result.getJObject("binningResult").keySet();
        List<MemberFeatureInfoModel.Feature> features = new ArrayList<>();
        for (String name : featureNames) {
            MemberFeatureInfoModel.Feature feature = new MemberFeatureInfoModel.Feature();
            feature.setName(name);
            features.add(feature);
        }

        member.setFeatures(features);
        member.setMemberName(CacheObjects.getMemberName(member.getMemberId()));

        return member;
    }

    /**
     * Find out the task result by id, type and role.
     */
    public TaskResultMySqlModel findByTaskIdAndTypeAndRole(String taskId, String type, JobMemberRole role) {

        Specification<TaskResultMySqlModel> where = Where
                .create()
                .equal("taskId", taskId)
                .equal("type", type)
                .equal("role", role)
                .build(TaskResultMySqlModel.class);

        return taskResultRepository.findOne(where).orElse(null);
    }


    /**
     * Find out the task result by id, type and role.
     */
    public List<TaskResultMySqlModel> findByTaskIdAndRoleNotEqualType(String taskId, String type, JobMemberRole role) {

        Specification<TaskResultMySqlModel> where = Where
                .create()
                .equal("taskId", taskId)
                .notEqual("type", type)
                .equal("role", role)
                .equal("servingModel", true)
                .build(TaskResultMySqlModel.class);

        return taskResultRepository.findAll(where);
    }

    public TaskResultMySqlModel findModelByJobIdAndNodeIdAndRole(String jobId, String flowNodeId, JobMemberRole role) {
        Specification<TaskResultMySqlModel> where = Where
                .create()
                .equal("jobId", jobId)
                .equal("flowNodeId", flowNodeId)
                .equal("role", role)
                .equal("servingModel", true)
                .build(TaskResultMySqlModel.class);
        return taskResultRepository.findOne(where).orElse(null);
    }

    /**
     * 中断推理任务
     */
    public void stopDeepLearningInfer(String taskId) throws StatusCodeWithException {
        TaskMySqlModel task = taskService.findOne(taskId);
        if (task == null) {
            StatusCode.PARAMETER_VALUE_INVALID.throwException("此task不存在:" + taskId);
        }

        TaskResultMySqlModel taskResult = findByTaskIdAndTypeAndRole(taskId, "infer", task.getRole());
        if (taskResult == null) {
            StatusCode.PARAMETER_VALUE_INVALID.throwException("未找到推理任务");
        }

        JSONObject root = JSON.parseObject(taskResult.getResult());
        root.put("status", "stopped");

        taskResult.setResult(root.toJSONString());
        taskResult.setUpdatedBy(CurrentAccountUtil.get().getId());
        taskResultRepository.save(taskResult);
    }


    public TaskResultMySqlModel findByJobIdAndComponentTypeAndType(String jobId, ComponentType componentType, TaskResultType type, JobMemberRole role) {

        Specification<TaskResultMySqlModel> where = Where
                .create()
                .equal("jobId", jobId)
                .equal("componentType", componentType)
                .equal("type", type.name())
                .equal("role", role)
                .build(TaskResultMySqlModel.class);

        return taskResultRepository.findOne(where).orElse(null);
    }


    public List<TaskResultMySqlModel> findModelByJobIdAndTypeAndRole(String jobId, String type, JobMemberRole role) {
        Specification<TaskResultMySqlModel> where = Where
                .create()
                .equal("jobId", jobId)
                .equal("type", type)
                .equal("role", role)
                .build(TaskResultMySqlModel.class);
        return taskResultRepository.findAll(where);
    }
}
