/**
 * Copyright 2021 The WeFe Authors. All Rights Reserved.
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

package com.welab.wefe.board.service.component;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.welab.wefe.board.service.api.project.flow.QueryDataIoTaskConfigApi;
import com.welab.wefe.board.service.api.project.member.ListApi;
import com.welab.wefe.board.service.component.base.AbstractComponent;
import com.welab.wefe.board.service.component.base.io.IODataType;
import com.welab.wefe.board.service.component.base.io.InputMatcher;
import com.welab.wefe.board.service.component.base.io.Names;
import com.welab.wefe.board.service.component.base.io.OutputItem;
import com.welab.wefe.board.service.constant.Config;
import com.welab.wefe.board.service.database.entity.data_set.DataSetMysqlModel;
import com.welab.wefe.board.service.database.entity.job.*;
import com.welab.wefe.board.service.dto.kernel.Env;
import com.welab.wefe.board.service.dto.kernel.KernelJob;
import com.welab.wefe.board.service.dto.kernel.TaskConfig;
import com.welab.wefe.board.service.exception.FlowNodeException;
import com.welab.wefe.board.service.model.FlowGraph;
import com.welab.wefe.board.service.model.FlowGraphNode;
import com.welab.wefe.board.service.service.*;
import com.welab.wefe.common.enums.*;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.fieldvalidate.AbstractCheckModel;
import com.welab.wefe.common.util.JObject;
import com.welab.wefe.common.util.StringUtil;
import com.welab.wefe.common.web.dto.ApiResult;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * OOT Component
 * <p>
 * OOT mode: flow chart with only [start] and [OOT] (the scene clicked in the model list)
 * </P>
 *
 * @author aaron.li
 **/
@Service
public class OotComponent extends AbstractComponent<OotComponent.Params> {

    /**
     * Exclude components to execute in OOT mode
     */
    private final static List<ComponentType> EXCLUDE_COMPONENT_TYPE_LIST = Arrays.asList(ComponentType.FeatureStatistic,
            ComponentType.FeatureCalculation, ComponentType.MixStatistic,
            ComponentType.Segment, ComponentType.VertPearson, ComponentType.Oot);
    @Autowired
    private Config config;

    @Autowired
    private DataSetService dataSetService;
    @Autowired
    private TaskService taskService;
    @Autowired
    private GatewayService gatewayService;
    @Autowired
    private ProjectFlowNodeService projectFlowNodeService;

    @Autowired
    private ProjectMemberService projectMemberService;


    @Override
    protected void checkBeforeBuildTask(FlowGraph graph, List<TaskMySqlModel> preTasks, FlowGraphNode node, Params params) throws FlowNodeException {
        if (graph.getJob().getMyRole() == JobMemberRole.arbiter) {
            return;
        }
        if (FederatedLearningType.mix.equals(graph.getFederatedLearningType())) {
            throw new FlowNodeException(node, "[打分验证]组件暂时不支持混合联邦");
        }

        DataIOComponent.DataSetItem myDataSetConfig = getMyDataSetConfig(graph, params);
        if (myDataSetConfig == null) {
            throw new FlowNodeException(node, "请保存成员[" + CacheObjects.getMemberName() + "]的数据集信息。");
        }

        DataSetMysqlModel dataSetMysqlModel = dataSetService.findOne(myDataSetConfig.getDataSetId());
        if (null == dataSetMysqlModel) {
            throw new FlowNodeException(node, "成员[" + CacheObjects.getMemberName() + "]选择的数据集信息不存在。");
        }

        // All characteristic columns of the dataset I selected
        List<String> myFeatureNameList = Arrays.asList(dataSetMysqlModel.getFeatureNameList().split(","));

        // Dataio task component
        TaskMySqlModel dataIoTaskMysqlModel = null;
        // If the jobid is not empty, it means an OOT process (a process containing only two components of [start] and [OOT]).
        // This jobid means the jobid of the old process
        if (isOotMode(params)) {
            if (preTasks.size() > 1) {
                throw new FlowNodeException(node, "只允许只有[打分验证]组件。");
            }
            // Find the dataio task from the task list
            dataIoTaskMysqlModel = taskService.findDataIoTask(params.jobId, graph.getJob().getMyRole());
            if (null == dataIoTaskMysqlModel) {
                throw new FlowNodeException(node, "未找到原流程中的[选择数据集]节点信息。");
            }

        } else {
            // Find modeling node
            FlowGraphNode modelingNode = graph.findModelingNodeFromParent(node);
            // Find evaluation node
            FlowGraphNode evaluationNode = graph.findOneNodeFromParent(node, ComponentType.Evaluation);
            if (null == evaluationNode && null == modelingNode) {
                throw new FlowNodeException(node, "在[打分验证]节点前必须有建模行为或评估行为");
            }

            dataIoTaskMysqlModel = findDataIoTask(preTasks);
        }

        // Feature list of original dataio selection
        List<String> needFeatures = JObject.parseArray(JObject.create(dataIoTaskMysqlModel.getTaskConf())
                .getStringByPath("params.need_features")).toJavaList(String.class);

        if (!myFeatureNameList.containsAll(needFeatures)) {
            throw new FlowNodeException(node, "成员[" + CacheObjects.getMemberName() + "]选择的数据集特征必须包含原流程的入模特征列。");
        }

        // Check the correctness of the feature column of the provider member selected by the OOT component on the promoter side.
        // Because the front end should be prompted directly on the initiator side, check it on the promoter side）
        if (graph.getJob().getMyRole() == JobMemberRole.promoter) {
            checkSelectedFeatures(graph, node, params, myDataSetConfig);
            checkSelectedMembersValid(graph, node, params);
        }
    }


    @Override
    protected JSONObject createTaskParams(FlowGraph graph, List<TaskMySqlModel> preTasks, FlowGraphNode node, Params params) throws FlowNodeException {
        if (graph.getJob().getMyRole() == JobMemberRole.arbiter) {
            return null;
        }

        // In OOT mode, you need to find out all the task information of the original process
        boolean isOotMode = isOotMode(params);

        DataIOComponent.DataSetItem myDataSetConfig = getMyDataSetConfig(graph, params);
        boolean isSelectedMyself = (null != myDataSetConfig);
        DataSetMysqlModel myDataSet = null;
        if (FederatedLearningType.vertical.equals(graph.getFederatedLearningType()) || isOotMode) {
            if (!isSelectedMyself) {
                throw new FlowNodeException(node, "请保存成员[" + CacheObjects.getMemberName() + "]的数据集信息。");
            }
        }
        if (isSelectedMyself) {
            myDataSet = dataSetService.findOne(myDataSetConfig.getDataSetId());
            if (myDataSet == null) {
                throw new FlowNodeException(node, "找不到成员[" + CacheObjects.getMemberName() + "]的数据集。");
            }
        }

        if (isOotMode) {
            // Find the original task list of the job
            preTasks = taskService.listByJobId(params.jobId, graph.getJob().getMyRole());
            if (CollectionUtils.isEmpty(preTasks)) {
                throw new FlowNodeException(node, "未找到原流程中任何节点信息。");
            }
        }

        // Find out the homologous branches with OOT components
        preTasks = findHomologousBranch(graph, preTasks, node, params);
        // Filter sub components that are not executed
        preTasks = preTasks.stream().filter(x -> !EXCLUDE_COMPONENT_TYPE_LIST.contains(x.getTaskType()))
                .collect(Collectors.toList());

        // Whether there is an evaluation component. If not, create a new evaluation component and add it to the OOT process
        if (!isExistEvaluationComponent(preTasks)) {
            // Find the dataio component of the original process
            TaskMySqlModel dataIoTaskMysqlModel = findDataIoTask(preTasks);
            preTasks.add(createEvaluationTaskMySqlModel(graph, node, dataIoTaskMysqlModel, params));
        }

        // Sub task name list
        List<String> subTaskNameList = new ArrayList<>();
        // Sub task configuration
        Map<String, JObject> subTaskConfigMap = new HashMap<>(16);
        // New input parameters for dataio component
        JObject newDataIoParam = JObject.create();

        for (TaskMySqlModel taskMySqlModel : preTasks) {
            String taskName = taskMySqlModel.getName();
            ComponentType taskType = taskMySqlModel.getTaskType();
            subTaskNameList.add(taskName);

            // Add new parameter
            JObject extendOotParams = JObject.create()
                    .append("need_run", isSelectedMyself)
                    .append("task_id", taskMySqlModel.getTaskId())
                    .append("task_type", taskType.name())
                    .append("flow_node_id", taskMySqlModel.getFlowNodeId())
                    .append("component_name", taskName)
                    .append("job_id", isOotMode ? params.jobId : graph.getJob().getJobId())
                    .append("is_model", false);

            // Add the OOT mode parameters and the required parameters in the corresponding OOT mode on the basis of the original task configuration
            TaskConfig taskConfig = JObject.parseObject(taskMySqlModel.getTaskConf(), TaskConfig.class);
            updateKernelJob(taskConfig, params);
            taskConfig.setTask(getTaskMembers(graph, node));
            JObject taskConfigObj = JObject.create(JObject.toJSONString(taskConfig));
            // If it is a dataio component, replace it with a new dataset
            if (DATA_IO_COMPONENT_TYPE_LIST.contains(taskType)) {
                newDataIoParam.append("with_label", isSelectedMyself ? myDataSet.getContainsY() : false)
                        .append("label_name", "y")
                        .append("namespace", isSelectedMyself ? myDataSet.getNamespace() : taskConfigObj.getStringByPath("params.namespace"))
                        .append("name", isSelectedMyself ? myDataSet.getTableName() : taskConfigObj.getStringByPath("params.name"))
                        .append("need_features", JObject.parseArray(taskConfigObj.getStringByPath("params.need_features")).toJavaList(String.class));
                taskConfigObj.put("params", newDataIoParam);
            } else if (MODEL_COMPONENT_TYPE_LIST.contains(taskType)) {
                // In order to match the prediction function of the kernel modeling component,
                // the modeling component needs to modify the input configuration (remove the train and add the model)
                JObject inputObj = taskConfigObj.getJObject("input");
                JObject dataObj = inputObj.getJObject("data");
                dataObj.remove(Names.Data.TRAIN_DATA_SET);
                inputObj.append("model", JObject.create().append(Names.Data.TRAIN_DATA_SET, Arrays.asList(taskName)));
                inputObj.append("data", dataObj);
                taskConfigObj.put("input", inputObj);

                // Mark as modeling component
                extendOotParams.put("is_model", true);

            }
            taskConfigObj.append("oot_params", extendOotParams);
            subTaskConfigMap.put(taskName, taskConfigObj);
        }

        // Create input parameters for OOT components
        JObject ootParam = JObject.create(newDataIoParam)
                .append("flow_node_id", node.getNodeId())
                .append("task_id", node.createTaskId(graph.getJob()))
                .append("sub_component_name_list", subTaskNameList)
                .append("sub_component_task_config_dick", subTaskConfigMap);

        // OotParam
        return JObject.create().append("params", ootParam);
    }

    @Override
    public ComponentType taskType() {
        return ComponentType.Oot;
    }

    @Override
    protected List<TaskResultMySqlModel> getAllResult(String taskId) {
        return null;
    }

    @Override
    protected TaskResultMySqlModel getResult(String taskId, String type) {
        TaskResultMySqlModel taskResultMySqlModel = taskResultService.findByTaskIdAndType(taskId, TaskResultType.metric_predict.name());
        if (null == taskResultMySqlModel) {
            return null;
        }

        JObject taskResultObj = JObject.create(taskResultMySqlModel.getResult());
        // Parameter configuration of modeling node
        JObject modelObj = taskResultObj.getJObject("model");

        // Evaluation results
        JObject evaluationObj = taskResultObj.getJObject("evaluation");
        String evaluationTaskId = findComponentTaskId(evaluationObj);

        // Final output
        JObject result = JObject.create("validate", evaluationObj.getJObject(evaluationTaskId));

        switch (type) {
            case "ks":
                JObject ksFpr = EvaluationComponent.parserCurveData(evaluationObj, "ks_fpr", evaluationTaskId, "");
                JObject ksTpr = EvaluationComponent.parserCurveData(evaluationObj, "ks_tpr", evaluationTaskId, "");
                JObject newKsFpr = JObject.create().append("validate_ks_fpr", ksFpr.getJObject("ks_fpr"));
                JObject newKsTpr = JObject.create().append("validate_ks_tpr", ksTpr.getJObject("ks_tpr"));

                result.putAll(newKsFpr);
                result.putAll(newKsTpr);
                break;
            case "lift":
                JObject lift = EvaluationComponent.parserCurveData(evaluationObj, "lift", evaluationTaskId, "");
                JObject newLift = JObject.create().append("validate_lift", lift.getJObject("lift"));
                result.putAll(newLift);
                break;
            case "gain":
                JObject gain = EvaluationComponent.parserCurveData(evaluationObj, "gain", evaluationTaskId, "");
                JObject newGain = JObject.create().append("validate_gain", gain.getJObject("gain"));
                result.putAll(newGain);
                break;
            case "accuracy":
                JObject accuracy = EvaluationComponent.parserCurveData(evaluationObj, "accuracy", evaluationTaskId, "");
                JObject newAccuracy = JObject.create().append("validate_accuracy", accuracy.getJObject("accuracy"));
                result.putAll(newAccuracy);
                break;
            case "precision_recall":
                JObject precision = EvaluationComponent.parserCurveData(evaluationObj, "precision", evaluationTaskId, "");
                JObject recall = EvaluationComponent.parserCurveData(evaluationObj, "recall", evaluationTaskId, "");
                JObject newPrecision = JObject.create().append("validate_precision", precision.getJObject("precision"));
                JObject newRecall = JObject.create().append("validate_recall", recall.getJObject("recall"));

                result.putAll(newPrecision);
                result.putAll(newRecall);
                break;
            case "roc":
                JObject roc = EvaluationComponent.parserCurveData(evaluationObj, "roc", evaluationTaskId, "");
                JObject newRoc = JObject.create().append("validate_roc", roc.getJObject("roc"));
                result.putAll(newRoc);
                break;
            case "topn":
                JObject topNData = evaluationObj.getJObject(evaluationTaskId + "_topn");
                if (null != topNData) {
                    result.putAll(JObject.create("validate_topn", topNData.getJSONList("data.topn")));
                }
                break;
            case "model":
                // Standardized output results of modeling nodes
                result.putAll(normalizerModel(modelObj));
                break;
            default:
        }
        taskResultMySqlModel.setResult(result.toJSONString());
        return taskResultMySqlModel;
    }

    @Override
    protected List<InputMatcher> inputs(FlowGraph graph, FlowGraphNode node) throws FlowNodeException {
        return null;
    }

    @Override
    public List<OutputItem> outputs(FlowGraph graph, FlowGraphNode node) throws FlowNodeException {
        return Arrays.asList(OutputItem.of(Names.JSON_RESULT, IODataType.Json));
    }

    /**
     * Judge whether the dataset feature column selected by the corresponding member of the OOT component
     * contains the feature column selected by the original dataio component
     */
    private void checkSelectedFeatures(FlowGraph graph, FlowGraphNode node, Params params, DataIOComponent.DataSetItem myDataSetConfig) throws FlowNodeException {
        // Relationship between member and selected dataset
        Map<String, DataIOComponent.DataSetItem> selectedDataSetItemMap = getSelectedDataSetItemMap(params);
        for (Map.Entry<String, DataIOComponent.DataSetItem> entry : selectedDataSetItemMap.entrySet()) {
            String memberId = entry.getKey();
            // This is mainly to check the provider, not the promoter
            if (memberId.equals(myDataSetConfig.getMemberId())) {
                continue;
            }
            DataIOComponent.DataSetItem dataSetItem = entry.getValue();
            JobMemberRole jobMemberRole = dataSetItem.getMemberRole();
            // List of features selected by OOT component
            List<String> ootFeatures = dataSetItem.getFeatures();

            String memberName = CacheObjects.getMemberName(memberId);
            // In OOT mode
            if (isOotMode(params)) {
                QueryDataIoTaskConfigApi.Input input = new QueryDataIoTaskConfigApi.Input();
                input.setJobId(params.jobId);
                input.setRole(jobMemberRole);
                try {
                    ApiResult<?> apiResult = gatewayService.sendToBoardRedirectApi(memberId, JobMemberRole.promoter, input, QueryDataIoTaskConfigApi.class);
                    if (0 != apiResult.code) {
                        throw new FlowNodeException(node, "获取成员[" + memberName + "]的原入模特征列失败,原因：" + apiResult.message);
                    }
                    JObject data = JObject.create(apiResult.data);
                    if (null == data || data.isEmpty()) {
                        throw new FlowNodeException(node, "获取成员[" + memberName + "]的原入模特征列为空。");
                    }

                    // Feature column selected for original dataio modeling
                    List<String> dataIoFeatures = JObject.parseArray(data.getStringByPath("params.need_features")).toJavaList(String.class);
                    if (!ootFeatures.containsAll(dataIoFeatures)) {
                        throw new FlowNodeException(node, "成员[" + memberName + "]数据集特征不全包含原入模特征列,请重新选择其他数据集再试");
                    }

                } catch (FlowNodeException e) {
                    LOG.error("Failed to get member features column, memberId: " + memberId + " ,exception: ", e);
                    throw e;
                } catch (Exception e) {
                    LOG.error("Failed to get member features column, memberId, memberId: " + memberId + ", exception: ", e);
                    throw new FlowNodeException(node, "获取成员[" + memberName + "]的原入模特征列失败，原因：" + e.getMessage());
                }
            } else {
                // Find the dataio component in the process
                FlowGraphNode dataIoFlowGraphNode = null;
                for (ComponentType componentType : DATA_IO_COMPONENT_TYPE_LIST) {
                    dataIoFlowGraphNode = graph.findOneNodeFromParent(node, componentType);
                    if (null != dataIoFlowGraphNode) {
                        break;
                    }
                }
                if (null == dataIoFlowGraphNode) {
                    throw new FlowNodeException(node, "请确保流程中存在[选择数据集]节点。");
                }
                ProjectFlowNodeMySqlModel projectFlowNodeMySqlModel = projectFlowNodeService.findOne(node.getFlowId(), dataIoFlowGraphNode.getNodeId());
                if (null == projectFlowNodeMySqlModel || StringUtil.isEmpty(projectFlowNodeMySqlModel.getParams())) {
                    throw new FlowNodeException(node, "请保存流程中的[选择数据集]节点参数。");
                }
                List<JObject> dataSetList = JObject.create(projectFlowNodeMySqlModel.getParams()).getJSONList("dataset_list");
                for (JObject dataSet : dataSetList) {
                    String dataSetMemberId = dataSet.getString("member_id");
                    String dataSetMemberRole = dataSet.getString("member_role");
                    String dataSetFeaturesStr = dataSet.getString("features");
                    if (JobMemberRole.arbiter.name().equals(dataSetMemberRole)) {
                        continue;
                    }

                    List<String> dataSetFeatures = JObject.parseArray(dataSetFeaturesStr).toJavaList(String.class);
                    if (memberId.equals(dataSetMemberId) && !ootFeatures.containsAll(dataSetFeatures)) {
                        throw new FlowNodeException(node, "请确保成员[" + memberName + "]在[打分验证]节点的数据集特征包含[选择数据集]节点相应成员已选择的特征列。");
                    }
                }
            }
        }
    }

    /**
     * Check whether the submitted dataset information is valid
     */
    private void checkSelectedMembersValid(FlowGraph graph, FlowGraphNode node, Params params) throws FlowNodeException {
        List<DataIOComponent.DataSetItem> dataSetItemList = params.getDataSetList();
        if (isOotMode(params)) {
            JobMySqlModel jobMySqlModel = jobService.findByJobId(params.jobId, JobMemberRole.promoter);
            if (null == jobMySqlModel) {
                throw new FlowNodeException(node, "找不到原流程任务信息");
            }
            ListApi.Input input = new ListApi.Input();
            input.setProjectId(jobMySqlModel.getProjectId());
            input.setOotJobId(params.jobId);
            try {
                List<ProjectMemberMySqlModel> projectMemberMySqlModelList = projectMemberService.findList(input);
                if (FederatedLearningType.vertical.equals(graph.getFederatedLearningType())) {
                    List<String> selectedMemberIdList = dataSetItemList.stream().map(DataIOComponent.DataSetItem::getMemberId).collect(Collectors.toList());
                    for (ProjectMemberMySqlModel projectMemberMySqlModel : projectMemberMySqlModelList) {
                        if (!selectedMemberIdList.contains(projectMemberMySqlModel.getMemberId())) {
                            throw new FlowNodeException(node, "请选择成员[" + CacheObjects.getMemberName(projectMemberMySqlModel.getMemberId()) + "]数据集信息");
                        }
                    }
                }
            } catch (StatusCodeWithException e) {
                throw new FlowNodeException(node, e.getMessage());
            }
        }
    }


    /**
     * Query all task lists of homologous branches
     */
    private List<TaskMySqlModel> findHomologousBranch(FlowGraph graph, List<TaskMySqlModel> preTasks, FlowGraphNode node, Params params) throws FlowNodeException {
        // Task ID to find
        String taskId = null;
        if (isOotMode(params)) {
            // Find out the modeling node in the original process
            TaskMySqlModel modelTaskMysqlModel = preTasks.stream().filter(x -> x.getFlowNodeId().equals(params.modelFlowNodeId)).findFirst().orElse(null);
            if (null == modelTaskMysqlModel) {
                throw new FlowNodeException(node, "找不到建模节点任务信息。");
            }
            taskId = modelTaskMysqlModel.getTaskId();
        } else {
            // Parent task ID of the current OOT component
            taskId = node.createParentTaskIds(graph.getJob());
        }
        return taskService.baseFindHomologousBranch(preTasks, taskId);
    }

    /**
     * Get my dataset configuration
     */
    private DataIOComponent.DataSetItem getMyDataSetConfig(FlowGraph graph, Params params) {
        return params.getDataSetList()
                .stream()
                .filter(x -> x.getMemberId().equals(CacheObjects.getMemberId()) && x.getMemberRole() == graph.getJob().getMyRole())
                .findFirst()
                .orElse(null);
    }

    /**
     * Query dataio components from task list
     */
    private TaskMySqlModel findDataIoTask(List<TaskMySqlModel> taskMySqlModelList) {
        return taskMySqlModelList.stream().filter(x -> DATA_IO_COMPONENT_TYPE_LIST.contains(x.getTaskType())).findFirst().orElse(null);
    }

    /**
     * Returns the mapping relationship between member and dataset
     *
     * @return KEY：Member ID, Value：Selected dataset
     */
    private Map<String, DataIOComponent.DataSetItem> getSelectedDataSetItemMap(Params params) {
        Map<String, DataIOComponent.DataSetItem> resultMap = new HashMap<>(16);
        List<DataIOComponent.DataSetItem> dataSetItemList = params.getDataSetList();
        for (DataIOComponent.DataSetItem dataSetItem : dataSetItemList) {
            resultMap.put(dataSetItem.getMemberId(), dataSetItem);
        }

        return resultMap;
    }


    /**
     * Is exist evaluation component in the task list
     *
     * @param taskMySqlModelList task list
     */
    private boolean isExistEvaluationComponent(List<TaskMySqlModel> taskMySqlModelList) {
        if (CollectionUtils.isEmpty(taskMySqlModelList)) {
            return false;
        }
        for (TaskMySqlModel taskMySqlModel : taskMySqlModelList) {
            if (ComponentType.Evaluation.equals(taskMySqlModel.getTaskType())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Create task configuration for evaluation component
     */
    private TaskMySqlModel createEvaluationTaskMySqlModel(FlowGraph graph, FlowGraphNode node, TaskMySqlModel dataIoTask, OotComponent.Params ootParams) throws FlowNodeException {
        if (StringUtil.isEmpty(ootParams.getEvalType()) || null == ootParams.posLabel) {
            throw new FlowNodeException(node, "请填写 打分验证 节点的评估类别或正标签类型字段。");
        }
        TaskMySqlModel evaluationTaskMySqlModel = new TaskMySqlModel();
        String evaluationTaskId = dataIoTask.getTaskId();
        for (ComponentType componentType : DATA_IO_COMPONENT_TYPE_LIST) {
            evaluationTaskId = evaluationTaskId.replace(componentType.name(), ComponentType.Evaluation.name());
        }
        evaluationTaskMySqlModel.setTaskId(evaluationTaskId);
        evaluationTaskMySqlModel.setTaskType(ComponentType.Evaluation);
        evaluationTaskMySqlModel.setJobId(dataIoTask.getJobId());
        evaluationTaskMySqlModel.setFlowNodeId(System.currentTimeMillis() + "");
        evaluationTaskMySqlModel.setName(ComponentType.Evaluation.name());

        // Create an evaluated task configuration based on the task configuration of the dataio component
        AbstractComponent evaluationComponent = Components.get(ComponentType.Evaluation);
        TaskConfig taskConfig = JObject.parseObject(dataIoTask.getTaskConf(), TaskConfig.class);
        taskConfig.setInput(JObject.create().append("data", JObject.create(Names.Data.NORMAL_DATA_SET, Arrays.asList(dataIoTask.getTaskId()))));
        taskConfig.setOutput(evaluationComponent.getOutputs(graph, node));
        taskConfig.setModule(ComponentType.Evaluation);
        // Reassemble evaluation parameters
        JObject evaluationParam = JObject.create();
        evaluationParam.append("eval_type", ootParams.evalType)
                .append("pos_label", ootParams.posLabel);
        taskConfig.setParams(evaluationParam);
        evaluationTaskMySqlModel.setTaskConf(JSON.toJSONString(taskConfig));
        return evaluationTaskMySqlModel;
    }


    /**
     * Standardized model configuration
     */
    private JObject normalizerModel(JObject modelObj) {
        JObject result = JObject.create();

        if (null == modelObj || modelObj.isEmpty()) {
            return result;
        }

        // Component name
        String moduleName = modelObj.getStringByPath("task_config.module");
        // Learning type
        String federatedLearningType = modelObj.getStringByPath("task_config.job.federated_learning_type");
        // Component parameters
        JObject modelParams = JObject.create(modelObj.getStringByPath("task_config.params"));
        // Data set y-value statistics
        JObject predictMetricData = modelObj.getJObject("predict_metric_data");

        return result.append("module_name", moduleName)
                .append("federated_learning_type", federatedLearningType)
                .append("model_params", modelParams)
                .append("predict_metric_data", predictMetricData);
    }


    /**
     * Find the task ID key from the evaluation results
     *
     * @param evaluationObj Evaluation result JSON
     */
    private String findComponentTaskId(JObject evaluationObj) {
        if (null == evaluationObj || evaluationObj.isEmpty()) {
            return "";
        }

        Set<String> keySet = evaluationObj.keySet();
        for (String key : keySet) {
            List<String> endsWithKeyList = Arrays.asList("_ks_fpr", "_lift", "_gain", "_roc", "_accuracy", "_precision",
                    "_recall", "_topn");
            for (String endsWithKey : endsWithKeyList) {
                if (key.endsWith(endsWithKey)) {
                    return key.substring(0, key.lastIndexOf(endsWithKey));
                }
            }

        }

        return "";
    }


    private void updateKernelJob(TaskConfig taskConfig, Params params) {
        KernelJob kernelJob = taskConfig.getJob();
        kernelJob.setEnv(createEvn());
        kernelJob.setFederatedLearningMode(FederatedLearningModel.oot);
    }

    /**
     * Create sub component running environment
     */
    private Env createEvn() {
        Env env = new Env();
        env.setBackend(config.getBackend());
        env.setDbType(config.getDbType());
        env.setWorkMode(config.getWorkMode());
        env.setName(config.getEnvName());
        return env;
    }


    /**
     * Is it OOT mode
     * <p>
     * If the jobid is not empty, it means an OOT process (a process containing only two components of [start] and [OOT]).
     * This jobid means the jobid of the old process
     * </p>
     */
    private boolean isOotMode(Params params) {
        return StringUtil.isNotEmpty(params.getJobId());
    }

    public static class Params extends AbstractCheckModel {
        private List<DataIOComponent.DataSetItem> dataSetList;
        /**
         * Specify jobid to create OOT component (used in OOT mode)
         */
        private String jobId;
        /**
         * Modeled node ID (used to distinguish which branch under the specified jobid is used for OOT) (used in OOT mode)
         */
        private String modelFlowNodeId;
        /**
         * Evaluation category (if there is no evaluation component in the original process, this parameter should be filled in for the input parameter of OOT component)
         */
        private String evalType;
        /**
         * Positive label type (if there is no evaluation component in the original process, this parameter should be filled in the OOT component input parameter)
         */
        private Integer posLabel;

        public List<DataIOComponent.DataSetItem> getDataSetList() {
            return dataSetList;
        }

        public void setDataSetList(List<DataIOComponent.DataSetItem> dataSetList) {
            this.dataSetList = dataSetList;
        }

        public String getJobId() {
            return jobId;
        }

        public void setJobId(String jobId) {
            this.jobId = jobId;
        }

        public String getEvalType() {
            return evalType;
        }

        public void setEvalType(String evalType) {
            this.evalType = evalType;
        }

        public Integer getPosLabel() {
            return posLabel;
        }

        public void setPosLabel(Integer posLabel) {
            this.posLabel = posLabel;
        }

        public String getModelFlowNodeId() {
            return modelFlowNodeId;
        }

        public void setModelFlowNodeId(String modelFlowNodeId) {
            this.modelFlowNodeId = modelFlowNodeId;
        }
    }
}
