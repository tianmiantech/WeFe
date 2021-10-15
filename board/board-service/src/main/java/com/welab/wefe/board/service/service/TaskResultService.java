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

package com.welab.wefe.board.service.service;

import com.alibaba.fastjson.JSONArray;
import com.welab.wefe.board.service.api.project.job.task.GetFeatureApi;
import com.welab.wefe.board.service.api.project.job.task.SelectFeatureApi;
import com.welab.wefe.board.service.api.project.job.task.SelectFeatureApi.Input.MemberModel;
import com.welab.wefe.board.service.component.DataIOComponent;
import com.welab.wefe.board.service.component.base.io.Names;
import com.welab.wefe.board.service.component.base.io.NodeOutputItem;
import com.welab.wefe.board.service.component.feature.FeatureSelectionComponent;
import com.welab.wefe.board.service.database.entity.job.ProjectMySqlModel;
import com.welab.wefe.board.service.database.entity.job.TaskMySqlModel;
import com.welab.wefe.board.service.database.entity.job.TaskResultMySqlModel;
import com.welab.wefe.board.service.database.repository.TaskRepository;
import com.welab.wefe.board.service.database.repository.TaskResultRepository;
import com.welab.wefe.board.service.dto.entity.MemberFeatureInfoModel;
import com.welab.wefe.board.service.exception.FlowNodeException;
import com.welab.wefe.board.service.model.FlowGraph;
import com.welab.wefe.board.service.model.FlowGraphNode;
import com.welab.wefe.common.data.mysql.Where;
import com.welab.wefe.common.enums.ComponentType;
import com.welab.wefe.common.enums.FederatedLearningType;
import com.welab.wefe.common.enums.JobMemberRole;
import com.welab.wefe.common.enums.TaskResultType;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.util.JObject;
import com.welab.wefe.common.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

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

    /**
     * filter the features
     * (1).cv/iv
     * (2). Missing rate
     * (3). Manually select to
     */
    public JObject selectFeature(SelectFeatureApi.Input input) throws StatusCodeWithException {

        FlowGraph flowGraph = jobService.createFlowGraph(input.getFlowId());
        FlowGraphNode node = flowGraph.getNode(input.getFlowNodeId());

        if (input.getSelectType() == SelectFeatureApi.SelectType.cv_iv) {
            return selectByCvIv(flowGraph, node, input);
        } else if (input.getSelectType() == SelectFeatureApi.SelectType.miss_rate) {
            return selectByMissRate(flowGraph, node, input);
        } else {

            List<MemberModel> members = input.getMembers();

            return JObject.create().append("members", members)
                    .append("featureNum", members.size());
        }

    }

    /**
     * filter the features by cv/iv
     */
    private JObject selectByCvIv(FlowGraph flowGraph, FlowGraphNode node, SelectFeatureApi.Input input) throws FlowNodeException {
        
        JObject result = JObject.create();
        List<MemberModel> selectMembers = new ArrayList<>();
        if (flowGraph.getFederatedLearningType() == FederatedLearningType.mix) {
            FlowGraphNode featureStatisticNode = flowGraph.findOneNodeFromParent(node, ComponentType.MixStatistic);
            if (featureStatisticNode == null) {
                throw new FlowNodeException(node, "请添加特征统计组件。");
            }

            FlowGraphNode featureBinningNode = flowGraph.findOneNodeFromParent(node, ComponentType.MixBinning);
            if (featureBinningNode == null) {
                throw new FlowNodeException(node, "请添加特征分箱组件。");
            }
            
            // Find the task corresponding to the FeatureCalculation node
            ProjectMySqlModel project = projectService.findProjectByJobId(input.getJobId());
            TaskMySqlModel featureStatisticTask = taskRepository.findOne(input.getJobId(), featureStatisticNode.getNodeId(), project.getMyRole().name());
            if (featureStatisticTask == null) {
                throw new FlowNodeException(node, "找不到对应的特征统计任务。");
            }
            
            // Find the task result of FeatureStatistic
            TaskResultMySqlModel featureStatisticTaskResult = findByTaskIdAndType(featureStatisticTask.getTaskId(), TaskResultType.data_feature_statistic.name());

            if (featureStatisticTaskResult == null) {
                return JObject.create();
            }

            JObject statisticResult = JObject.create(featureStatisticTaskResult.getResult());
            
            TaskMySqlModel featureBinningTask = taskRepository.findOne(input.getJobId(), featureBinningNode.getNodeId(), project.getMyRole().name());
            if (featureBinningTask == null) {
                throw new FlowNodeException(node, "找不到对应的特征分箱任务。");
            }
            // Find the task result of FeatureStatistic
            TaskResultMySqlModel featureBinningTaskResult = findByTaskIdAndType(featureBinningTask.getTaskId(), TaskResultType.model_binning.name());

            if (featureBinningTaskResult == null) {
                return JObject.create();
            }
            
            List<JObject> featureBinningResults = parseBinningResult(featureBinningTaskResult);
            List<JObject> statisticResultMembers = statisticResult.getJSONList("members");
            for (JObject memberObj : statisticResultMembers) {
                Map<String, Double> cvMap = new HashMap<>();
                Map<String, Double> ivMap = new HashMap<>();
                
                String memberId = memberObj.getString("member_id");
                String role = memberObj.getString("role");
                
                JObject featureBinningResult = featureBinningResults.stream()
                        .filter(s -> role.equalsIgnoreCase(s.getString("role"))
                                && memberId.equalsIgnoreCase(s.getString("memberId")))
                        .findFirst().orElse(null);
                if (featureBinningResult != null) {
                    featureBinningResult = featureBinningResult.getJObject("binningResult");
                }
                JObject feature_statistic = memberObj.getJObject("feature_statistic");
                Set<String> featuresKey = feature_statistic.keySet();
                for (String feature : featuresKey) {
                    JObject statisticData = feature_statistic.getJObject(feature);
                    double cv = statisticData.getDouble("cv");
                    BigDecimal bg = new BigDecimal(cv);
                    cvMap.put(feature, bg.setScale(4, RoundingMode.HALF_UP).doubleValue());
                }
                if (featureBinningResult != null) {
                    featuresKey = featureBinningResult.keySet();
                    for (String feature : featuresKey) {
                        JObject binningData = featureBinningResult.getJObject(feature);
                        double iv = binningData.getDouble("iv");
                        BigDecimal bgiv = new BigDecimal(iv);
                        ivMap.put(feature, bgiv.setScale(4, RoundingMode.HALF_UP).doubleValue());
                    }
                }

                // Get the feature column of the current member
                List<MemberModel> currentMembers = input.getMembers().stream().filter(
                        x -> x.getMemberId().equals(memberId) && x.getMemberRole() == JobMemberRole.valueOf(role))
                        .collect(Collectors.toList());

                for (MemberModel model : currentMembers) {
                    if (cvMap.get(model.getName()) != null) {
                        model.setCv(cvMap.get(model.getName()));
                    }
                    if (ivMap.get(model.getName()) != null) {
                        model.setIv(ivMap.get(model.getName()));
                    }
                }

                // Perform cv filtering
                for (MemberModel model : currentMembers) {
                    if (model.getIv() >= input.getIv() && model.getCv() >= input.getCv()) {
                        selectMembers.add(model);
                    }
                }
            }
            
        }
        else {
            // Find the FeatureCalculation node in the parent node
            FlowGraphNode featureCalculationNode = flowGraph.findOneNodeFromParent(node, ComponentType.FeatureCalculation);

            if (featureCalculationNode == null) {
                throw new FlowNodeException(node, "请添加特征计算组件。");
            }

            // Find the task corresponding to the FeatureCalculation node
            ProjectMySqlModel project = projectService.findProjectByJobId(input.getJobId());
            TaskMySqlModel featureCalculationTask = taskRepository.findOne(input.getJobId(), featureCalculationNode.getNodeId(), project.getMyRole().name());
            if (featureCalculationTask == null) {
                throw new FlowNodeException(node, "找不到对应的特征计算任务。");
            }

            // Find the task result of FeatureCalculation
            TaskResultMySqlModel featureCalculationTaskResult = findByTaskIdAndType(featureCalculationTask.getTaskId(), TaskResultType.model_result.name());
            if (featureCalculationTaskResult == null) {
                return JObject.create();
            }
            
            result = JObject.create(featureCalculationTaskResult.getResult());
            List<JObject> calculateResults = result.getJSONList("model_param.calculateResults");

            for (JObject obj : calculateResults) {
                String role = obj.getString("role");
                String memberId = obj.getString("memberId");

                List<JObject> results = obj.getJSONList("results");

                JSONArray ivValue = new JSONArray();
                JSONArray ivCols = new JSONArray();
                JSONArray cvValue = new JSONArray();
                JSONArray cvCols = new JSONArray();
                Map<String, Double> cvMap = new HashMap<>();
                Map<String, Double> ivMap = new HashMap<>();

                // Store the cv/iv value in the map in the form of key: "x1" value: 0.123
                for (JObject resultObj : results) {

                    if ("iv_value_thres".equals(resultObj.getString("filterName"))) {
                        ivValue = resultObj.getJSONArray("values");
                        ivCols = resultObj.getJSONArray("cols");
                    }
                    if ("coefficient_of_variation_value_thres".equals(resultObj.getString("filterName"))) {
                        cvValue = resultObj.getJSONArray("values");
                        cvCols = resultObj.getJSONArray("cols");
                    }

                    for (int i = 0; i < ivCols.size(); i++) {
                        ivMap.put(ivCols.getString(i), ivValue.getDoubleValue(i));
                    }

                    for (int i = 0; i < cvCols.size(); i++) {
                        cvMap.put(cvCols.getString(i), cvValue.getDoubleValue(i));
                    }
                }

                // Get the feature column of the current member
                List<MemberModel> currentMembers = input.getMembers().stream().filter(x -> x.getMemberId().equals(memberId) && x.getMemberRole() == JobMemberRole.valueOf(role))
                        .collect(Collectors.toList());

                // Assign cv/iv values to features
                for (MemberModel model : currentMembers) {
                    if (cvMap.get(model.getName()) != null) {
                        model.setCv(cvMap.get(model.getName()));
                    }
                    if (ivMap.get(model.getName()) != null) {
                        model.setIv(ivMap.get(model.getName()));
                    }
                }

                // Filter
                for (MemberModel model : currentMembers) {
                    if (model.getIv() >= input.getIv() && model.getCv() >= input.getCv()) {
                        selectMembers.add(model);
                    }
                }
            }

        }

        return JObject
                .create()
                .append("members", selectMembers)
                .append("featureNum", selectMembers.size());
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
     * filter the features by missing rate
     */
    private JObject selectByMissRate(FlowGraph flowGraph, FlowGraphNode node, SelectFeatureApi.Input input) throws FlowNodeException {
        // Find the FeatureStatistic node in the parent node
        FlowGraphNode featureStatisticNode = flowGraph.findOneNodeFromParent(node,
                x -> x.getComponentType() == ComponentType.FeatureStatistic
                        || x.getComponentType() == ComponentType.MixStatistic);

        if (featureStatisticNode == null) {
            throw new FlowNodeException(node, "请添加特征统计组件。");
        }

        // Find the task corresponding to the FeatureStatistic node
        ProjectMySqlModel project = projectService.findProjectByJobId(input.getJobId());
        TaskMySqlModel featureStatisticTask = taskRepository.findOne(input.getJobId(), featureStatisticNode.getNodeId(), project.getMyRole().name());
        if (featureStatisticTask == null) {
            throw new FlowNodeException(node, "找不到特征统计节点对应的任务。");
        }

        // Find the task result of FeatureStatistic
        TaskResultMySqlModel featureStatisticTaskResult = findByTaskIdAndType(featureStatisticTask.getTaskId(), TaskResultType.data_feature_statistic.name());

        if (featureStatisticTaskResult == null) {
            return JObject.create();
        }

        JObject result = JObject.create(featureStatisticTaskResult.getResult());

        List<JObject> resultMembers = result.getJSONList("members");
        List<MemberModel> selectMembers = new ArrayList<>();

        for (JObject memberObj : resultMembers) {
            Map<String, Double> missingValueMap = new HashMap<>();

            String memberId = memberObj.getString("member_id");
            String role = memberObj.getString("role");

            JObject feature_statistic = memberObj.getJObject("feature_statistic");

            Set<String> featuresKey = feature_statistic.keySet();

            // Calculate the missing rate from the statistical data of the feature and store it in the map, key: "x1" value: 0.01
            for (String feature : featuresKey) {
                JObject statisticData = feature_statistic.getJObject(feature);

                double not_null_count = statisticData.getDouble("not_null_count");
                double missing_count = statisticData.getDouble("missing_count");
                double missingValue = missing_count / (missing_count + not_null_count);
                BigDecimal bg = new BigDecimal(missingValue);

                missingValueMap.put(feature, bg.setScale(4, RoundingMode.HALF_UP).doubleValue());
            }

            // Get the feature column of the current member
            List<MemberModel> currentMembers = input.getMembers().stream().filter(x -> x.getMemberId().equals(memberId) && x.getMemberRole() == JobMemberRole.valueOf(role))
                    .collect(Collectors.toList());

            // Assign values to features with missing values
            for (MemberModel model : currentMembers) {
                if (missingValueMap.get(model.getName()) != null) {
                    model.setMissRate(missingValueMap.get(model.getName()));
                }
            }

            // Perform missingValue filtering
            for (MemberModel model : currentMembers) {
                if (model.getMissRate() >= input.getMissRate()) {
                    selectMembers.add(model);
                }
            }
        }

        return JObject
                .create()
                .append("members", selectMembers)
                .append("featureNum", selectMembers.size());
    }

    /**
     * Get feature list
     */
    public GetFeatureApi.Output getResultFeature(GetFeatureApi.Input input) throws StatusCodeWithException {

        JObject result = JObject.create();
        GetFeatureApi.Output out = new GetFeatureApi.Output();
        FlowGraph graph = jobService.createFlowGraph(input.getFlowId());

        FlowGraphNode node = graph.getNode(input.getFlowNodeId());

        // If the current node is a feature screening node,
        // it is necessary to determine whether the previous component has feature calculation (the result has cv/iv)/feature statistics (the result has a missing rate)
        if (node.getComponentType() == ComponentType.FeatureSelection) {
            FlowGraphNode featureStatisticNode = graph.findOneNodeFromParent(node,
                    x -> x.getComponentType() == ComponentType.MixStatistic
                            || x.getComponentType() == ComponentType.FeatureStatistic);
            out.setHasFeatureStatistic(false);
            out.setHasFeatureCalculation(false);
            if (featureStatisticNode != null && StringUtil.isNotEmpty(input.getJobId())) {
                ProjectMySqlModel project = projectService.findProjectByJobId(input.getJobId());
                TaskMySqlModel featureStatisticTask = taskRepository.findOne(input.getJobId(), featureStatisticNode.getNodeId(), project.getMyRole().name());
                if (featureStatisticTask != null) {

                    TaskResultMySqlModel featureStatisticResult = findByTaskIdAndTypeAndRole(featureStatisticTask.getTaskId(), TaskResultType.data_feature_statistic.name(), project.getMyRole());
                    if (featureStatisticResult != null) {
                        out.setHasFeatureStatistic(true);
                    }
                }
            }

            FlowGraphNode featureCalculationNode = graph.findOneNodeFromParent(node, ComponentType.FeatureCalculation);
            if (featureCalculationNode != null && StringUtil.isNotEmpty(input.getJobId())) {
                ProjectMySqlModel project = projectService.findProjectByJobId(input.getJobId());
                TaskMySqlModel featureCalculationTask = taskRepository.findOne(input.getJobId(), featureCalculationNode.getNodeId(), project.getMyRole().name());
                if (featureCalculationTask != null) {

                    TaskResultMySqlModel featureCalculationResult = findByTaskIdAndTypeAndRole(featureCalculationTask.getTaskId(), TaskResultType.model_result.name(), project.getMyRole());
                    if (featureCalculationResult != null) {
                        out.setHasFeatureCalculation(true);
                    }
                }
            }
            
            FlowGraphNode featureBinningNode = graph.findOneNodeFromParent(node,
                    x -> x.getComponentType() == ComponentType.MixBinning
                            || x.getComponentType() == ComponentType.Binning);
            if (featureBinningNode != null && StringUtil.isNotEmpty(input.getJobId())) {
                ProjectMySqlModel project = projectService.findProjectByJobId(input.getJobId());
                TaskMySqlModel featureBinningTask = taskRepository.findOne(input.getJobId(),
                        featureBinningNode.getNodeId(), project.getMyRole().name());
                if (featureBinningTask != null) {
                    TaskResultMySqlModel featureBinningResult = findByTaskIdAndTypeAndRole(
                            featureBinningTask.getTaskId(), TaskResultType.model_binning.name(), project.getMyRole());
                    if (featureBinningResult != null) {
                        out.setHasFeatureCalculation(true && out.isHasFeatureStatistic());
                    }
                }
            }
        }

        List<MemberFeatureInfoModel> members = getMemberFeatures(graph, node);
        out.setMembers(members);
        return out;
    }

    /**
     * Find the feature column in the training data set:
     * take the feature column from (DataIO/binning/feature filtering)
     */
    public List<MemberFeatureInfoModel> getMemberFeatures(FlowGraph graph, FlowGraphNode node) throws FlowNodeException {
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
        } else {
            return getMemberFeatures(graph, graph.getNode(trainDataSetNodeOutputItem.getNodeId()));
        }
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

    public List<TaskResultMySqlModel> findTaskResult(String jobId, String taskId, ComponentType componentType) {

        Specification<TaskResultMySqlModel> where = Where
                .create()
                .equal("jobId", jobId)
                .equal("componentType", componentType)
                .equal("taskId", taskId)
                .notEqual("role", JobMemberRole.arbiter)
                .build(TaskResultMySqlModel.class);

        return taskResultRepository.findAll(where);

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
}
