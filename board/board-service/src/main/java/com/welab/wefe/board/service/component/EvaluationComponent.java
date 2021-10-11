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

package com.welab.wefe.board.service.component;

import com.alibaba.fastjson.JSONObject;
import com.welab.wefe.board.service.component.base.AbstractComponent;
import com.welab.wefe.board.service.component.base.io.*;
import com.welab.wefe.board.service.database.entity.job.TaskMySqlModel;
import com.welab.wefe.board.service.database.entity.job.TaskResultMySqlModel;
import com.welab.wefe.board.service.exception.FlowNodeException;
import com.welab.wefe.board.service.model.FlowGraph;
import com.welab.wefe.board.service.model.FlowGraphNode;
import com.welab.wefe.board.service.service.TaskService;
import com.welab.wefe.common.enums.ComponentType;
import com.welab.wefe.common.enums.JobMemberRole;
import com.welab.wefe.common.enums.TaskResultType;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.fieldvalidate.AbstractCheckModel;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.util.JObject;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author lonnie
 */
@Service
class EvaluationComponent extends AbstractComponent<EvaluationComponent.Params> {

    @Autowired
    private TaskService taskService;

    @Override
    protected void checkBeforeBuildTask(FlowGraph graph, List<TaskMySqlModel> preTasks, FlowGraphNode node, Params params) throws FlowNodeException {
        // Find the modeling node
        FlowGraphNode modelingNode = graph.findModelingNodeFromParent(node);

        if (modelingNode == null) {
            throw new FlowNodeException(node, "评估之前必须有建模行为");
        }
    }


    @Override
    public ComponentType taskType() {
        return ComponentType.Evaluation;
    }

    @Override
    protected JSONObject createTaskParams(FlowGraph graph, List<TaskMySqlModel> preTasks, FlowGraphNode node, Params params) throws FlowNodeException {

        if (graph.getJob().getMyRole() == JobMemberRole.arbiter) {
            return null;
        }

        JSONObject taskParam = new JSONObject();

        // Reassembly parameters
        JObject evaluationParam = JObject.create();
        evaluationParam.append("eval_type", params.getEvalType())
                .append("pos_label", params.getPosLabel());

        taskParam.put("params", evaluationParam);

        return taskParam;
    }

    @Override
    protected List<TaskResultMySqlModel> getAllResult(String taskId) {
        return taskResultService.listAllResult(taskId);
    }

    @Override
    protected TaskResultMySqlModel getResult(String taskId, String type) {

        TaskResultMySqlModel trainTaskResult = taskResultService.findByTaskIdAndType(taskId, TaskResultType.metric_train.name());
        TaskResultMySqlModel validateTaskResult = taskResultService.findByTaskIdAndType(taskId, TaskResultType.metric_validate.name());

        if (trainTaskResult == null && validateTaskResult == null) {
            return null;
        }

        // Training and validation evaluation task_result only has different types,
        // and finally the results are merged and returned.
        TaskResultMySqlModel taskResultMySqlModel = new TaskResultMySqlModel();
        if (trainTaskResult != null) {
            BeanUtils.copyProperties(trainTaskResult, taskResultMySqlModel);
        } else {
            BeanUtils.copyProperties(validateTaskResult, taskResultMySqlModel);
        }

        JObject trainObj = JObject.create(trainTaskResult != null ? trainTaskResult.getResult() : "");
        JObject validateObj = JObject.create(validateTaskResult != null ? validateTaskResult.getResult() : "");

        JObject result = JObject.create();

        try {
            // Find out all the same branch nodes with the evaluation node
            // and find the modeling node from them
            // (this method solves the problem of null pointer when the evaluation node is deleted in the original editing process again)
            List<TaskMySqlModel> homologousBranchTaskList = taskService.findHomologousBranchByJobId(taskResultMySqlModel.getJobId(), trainTaskResult.getRole(), taskResultMySqlModel.getTaskId());
            TaskMySqlModel modelingTask = homologousBranchTaskList.stream().filter(x -> MODEL_COMPONENT_TYPE_LIST.contains(x.getTaskType())).findFirst().orElse(null);

            String modelComponentType = modelingTask.getTaskType().toString();
            String modelNodeId = modelingTask.getFlowNodeId();
            String suffix = "";
            if (!taskId.endsWith(taskResultMySqlModel.getFlowNodeId())) {
                suffix = "_" + taskId.split("_")[taskId.split("_").length - 1];
            }
            // Start parsing the required result data
            String normalName = modelComponentType + "_" + modelNodeId + suffix;
            String preValidateName = "validate_" + modelComponentType + "_" + modelNodeId + suffix;
            String preTrainName = "train_" + modelComponentType + "_" + modelNodeId + suffix;

            JObject validate = validateObj.getJObject(preValidateName);
            JObject train = trainObj.getJObject(preTrainName);

            result.append("validate", validate)
                    .append("train", train);

            switch (type) {
                case "ks":
                    result.putAll(parserTrainCurveData(trainObj, "ks_fpr", normalName));
                    result.putAll(parserValidateCurveData(validateObj, "ks_fpr", normalName));
                    result.putAll(parserTrainCurveData(trainObj, "ks_tpr", normalName));
                    result.putAll(parserValidateCurveData(validateObj, "ks_tpr", normalName));
                    break;
                case "lift":
                    result.putAll(parserTrainCurveData(trainObj, "lift", normalName));
                    result.putAll(parserValidateCurveData(validateObj, "lift", normalName));
                    break;
                case "gain":
                    result.putAll(parserTrainCurveData(trainObj, "gain", normalName));
                    result.putAll(parserValidateCurveData(validateObj, "gain", normalName));
                    break;
                case "accuracy":
                    result.putAll(parserTrainCurveData(trainObj, "accuracy", normalName));
                    result.putAll(parserValidateCurveData(validateObj, "accuracy", normalName));
                    break;
                case "precision_recall":
                    result.putAll(parserTrainCurveData(trainObj, "precision", normalName));
                    result.putAll(parserValidateCurveData(validateObj, "precision", normalName));
                    result.putAll(parserTrainCurveData(trainObj, "recall", normalName));
                    result.putAll(parserValidateCurveData(validateObj, "recall", normalName));
                    break;
                case "roc":
                    result.putAll(parserTrainCurveData(trainObj, "roc", normalName));
                    result.putAll(parserValidateCurveData(validateObj, "roc", normalName));
                    break;
                case "topn":
                    result.putAll(parserTopN(trainObj, normalName, "train"));
                    result.putAll(parserTopN(validateObj, normalName, "validate"));
                default:
                    break;

            }
        } catch (StatusCodeWithException e) {
            e.printStackTrace();
        }

        taskResultMySqlModel.setResult(result.toJSONString());

        return taskResultMySqlModel;
    }

    /**
     * Parse topN
     */
    private JObject parserTopN(JObject trainObj, String normalName, String dataType) {
        String curveKey = dataType + "_" + normalName + "_topn";
        JObject topNData = trainObj.getJObject(curveKey);
        JObject data = topNData.getJObject("data");

        List<JObject> topN = data.getJSONList("topn");

        return JObject.create().append(dataType + "_topn", topN);
    }

    private JObject parserTrainCurveData(JObject obj, String type, String normalName) {
        return parserCurveData(obj, type, normalName, "train_");
    }

    private JObject parserValidateCurveData(JObject obj, String type, String normalName) {
        return parserCurveData(obj, type, normalName, "validate_");
    }

    /**
     * Get the corresponding curve data node information, dataType: validate_/train_
     * <p>
     * Since the oot component wants to reuse this method, it can only be changed to public and static.
     * </p>
     */
    public static JObject parserCurveData(JObject obj, String type, String normalName, String dataType) {
        String curveKey = dataType + normalName + "_" + type;

        JObject curveObj = obj.getJObject(curveKey);
        if (curveObj == null) {
            return JObject.create();
        }
        JObject curveData = normalizerData(curveObj);
        JObject parserResult = JObject.create().append(dataType + type, curveData);

        return parserResult;
    }

    /**
     * Reassemble the data structure
     * <p>
     * Since the oot component wants to reuse this method, it can only be changed to static
     * </p>
     */
    private static JObject normalizerData(JObject obj) {
        JObject data = obj.getJObject("data");
        List<String> dataKey = data.keySet().stream().sorted().collect(Collectors.toList());

        List<List<Double>> dataList = new ArrayList<>();
        for (String key : dataKey) {
            List<Double> coordinate = new ArrayList<>();
            Double value = data.getJObject(key).getDoubleValue("value");
            // X-axis
            coordinate.add(Double.valueOf(key));
            // Y-axis
            coordinate.add(value);
            dataList.add(coordinate);
        }
        obj.append("data", dataList);
        return obj;
    }

    @Override
    protected List<InputMatcher> inputs(FlowGraph flowGraph, FlowGraphNode flowGraphNode) throws FlowNodeException {

        InputSupplier modelSupplier = (graph, node) -> {
            // Find the modeling node
            FlowGraphNode modelingNode = flowGraph.findModelingNodeFromParent(flowGraphNode);
            return new NodeOutputItem(modelingNode, OutputItem.of(Names.Data.NORMAL_DATA_SET, IODataType.DataSetInstance));
        };

        return Arrays.asList(
                InputMatcher.of(Names.Data.NORMAL_DATA_SET, modelSupplier)
        );
    }

    @Override
    public List<OutputItem> outputs(FlowGraph graph, FlowGraphNode node) {
        return Arrays.asList(
                OutputItem.of(Names.JSON_RESULT, IODataType.Json)
        );
    }

    public static class Params extends AbstractCheckModel {
        @Check(require = true)
        private String evalType;

        @Check(require = true)
        private int posLabel;

        public String getEvalType() {
            return evalType;
        }

        public void setEvalType(String evalType) {
            this.evalType = evalType;
        }

        public int getPosLabel() {
            return posLabel;
        }

        public void setPosLabel(int posLabel) {
            this.posLabel = posLabel;
        }
    }
}
