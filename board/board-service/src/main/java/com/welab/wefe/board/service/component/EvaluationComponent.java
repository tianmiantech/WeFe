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

package com.welab.wefe.board.service.component;

import com.alibaba.fastjson.JSONObject;
import com.welab.wefe.board.service.component.base.AbstractComponent;
import com.welab.wefe.board.service.component.base.io.*;
import com.welab.wefe.board.service.database.entity.job.TaskMySqlModel;
import com.welab.wefe.board.service.database.entity.job.TaskResultMySqlModel;
import com.welab.wefe.board.service.exception.FlowNodeException;
import com.welab.wefe.board.service.model.FlowGraph;
import com.welab.wefe.board.service.model.FlowGraphNode;
import com.welab.wefe.board.service.model.JobBuilder;
import com.welab.wefe.board.service.service.TaskService;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.fieldvalidate.AbstractCheckModel;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.util.JObject;
import com.welab.wefe.common.web.util.ModelMapper;
import com.welab.wefe.common.wefe.enums.ComponentType;
import com.welab.wefe.common.wefe.enums.JobMemberRole;
import com.welab.wefe.common.wefe.enums.TaskResultType;
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
    protected JSONObject createTaskParams(JobBuilder jobBuilder, FlowGraph graph, List<TaskMySqlModel> preTasks, FlowGraphNode node, Params params) throws FlowNodeException {

        if (graph.getJob().getMyRole() == JobMemberRole.arbiter) {
            return null;
        }

        // Reassembly parameters
        return JObject.create()
                .append("eval_type", params.getEvalType())
                .append("pos_label", params.getPosLabel())
                .append("prob_need_to_bin", params.isProbNeedToBin())
                .append("bin_method", params.getBinMethod())
                .append("bin_num", params.getBinNum());
    }

    @Override
    protected List<TaskResultMySqlModel> getAllResult(String taskId) {
        return taskResultService.listAllResult(taskId);
    }

    @Override
    protected TaskResultMySqlModel getResult(String taskId, String type) throws StatusCodeWithException {

        TaskResultMySqlModel taskResultMySqlModel = findEvaluationTaskResultByTaskId(taskId);
        if (taskResultMySqlModel == null) {
            return null;
        }

        JObject result = JObject.create()
                .append("validate", getValidateJObject(taskId, taskResultMySqlModel))
                .append("train", getTrainJObject(taskId, taskResultMySqlModel));

        // Start parsing the required result data
        result.putAll(getResultByType(taskId, type, extractNormalName(taskResultMySqlModel)));

        taskResultMySqlModel.setResult(result.toJSONString());

        return taskResultMySqlModel;
    }

    private JObject getTrainJObject(String taskId, TaskResultMySqlModel taskResultMySqlModel) throws StatusCodeWithException {
        return getTrainObjByTaskId(taskId).getJObject(extractPreTrainName(taskResultMySqlModel));
    }

    private JObject getValidateJObject(String taskId, TaskResultMySqlModel taskResultMySqlModel) throws StatusCodeWithException {
        return getValidateObjByTaskId(taskId).getJObject(extractPreValidateName(taskResultMySqlModel));
    }

    private JObject getResultByType(String taskId, String type, String normalName) throws StatusCodeWithException {

        final JObject trainObj = getTrainObjByTaskId(taskId);
        final JObject validateObj = getValidateObjByTaskId(taskId);


        switch (type) {
            case "ks":
                JObject ks = JObject.create();
                ks.putAll(parserTrainCurveData(trainObj, "ks_fpr", normalName));
                ks.putAll(parserValidateCurveData(validateObj, "ks_fpr", normalName));
                ks.putAll(parserTrainCurveData(trainObj, "ks_tpr", normalName));
                ks.putAll(parserValidateCurveData(validateObj, "ks_tpr", normalName));
                return ks;
            case "lift":
                JObject lift = JObject.create();
                lift.putAll(parserTrainCurveData(trainObj, "lift", normalName));
                lift.putAll(parserValidateCurveData(validateObj, "lift", normalName));
                return lift;
            case "gain":
                JObject gain = JObject.create();
                gain.putAll(parserTrainCurveData(trainObj, "gain", normalName));
                gain.putAll(parserValidateCurveData(validateObj, "gain", normalName));
                return gain;
            case "accuracy":
                JObject accuracy = JObject.create();
                accuracy.putAll(parserTrainCurveData(trainObj, "accuracy", normalName));
                accuracy.putAll(parserValidateCurveData(validateObj, "accuracy", normalName));
                return accuracy;
            case "precision_recall":
                JObject precision_recall = JObject.create();
                precision_recall.putAll(parserTrainCurveData(trainObj, "precision", normalName));
                precision_recall.putAll(parserValidateCurveData(validateObj, "precision", normalName));
                precision_recall.putAll(parserTrainCurveData(trainObj, "recall", normalName));
                precision_recall.putAll(parserValidateCurveData(validateObj, "recall", normalName));
                break;
            case "roc":
                JObject roc = JObject.create();
                roc.putAll(parserTrainCurveData(trainObj, "roc", normalName));
                roc.putAll(parserValidateCurveData(validateObj, "roc", normalName));
                break;
            case "topn":
                JObject topn = JObject.create();
                topn.putAll(parserTopN(trainObj, normalName, "train"));
                topn.putAll(parserTopN(validateObj, normalName, "validate"));
                return topn;
            case "scores_distribution":
                final JObject distributionObj = getDistributionObjByTaskId(taskId);
                JObject scores_distribution = JObject.create();
//                scores_distribution.putAll(parserTrainCurveData(trainObj, "gain", normalName));
                scores_distribution.putAll(parserScoresDistributionCurveData(distributionObj, normalName));
                return scores_distribution;
            default:
                return JObject.create();
        }
        return JObject.create();
    }

    private String extractModelComponentType(TaskResultMySqlModel taskResultMySqlModel) throws StatusCodeWithException {
        TaskMySqlModel taskMySqlModel = findEvaluationTaskByTaskResult(taskResultMySqlModel);
        return taskMySqlModel.getTaskType().toString();
    }

    private String extractFlowNodeId(TaskResultMySqlModel taskResultMySqlModel) throws StatusCodeWithException {
        TaskMySqlModel taskMySqlModel = findEvaluationTaskByTaskResult(taskResultMySqlModel);
        return taskMySqlModel.getFlowNodeId();
    }

    private String extractPreTrainName(TaskResultMySqlModel taskResultMySqlModel) throws StatusCodeWithException {
        return "train_" + extractModelComponentType(taskResultMySqlModel) + "_" + extractFlowNodeId(taskResultMySqlModel) + extractSuffix(taskResultMySqlModel);
    }

    private String extractPreValidateName(TaskResultMySqlModel taskResultMySqlModel) throws StatusCodeWithException {
        return "validate_" + extractModelComponentType(taskResultMySqlModel) + "_" + extractFlowNodeId(taskResultMySqlModel) + extractSuffix(taskResultMySqlModel);
    }

    private String extractNormalName(TaskResultMySqlModel taskResultMySqlModel) throws StatusCodeWithException {
        return extractModelComponentType(taskResultMySqlModel) + "_" + extractFlowNodeId(taskResultMySqlModel) + extractSuffix(taskResultMySqlModel);
    }

    private String extractSuffix(TaskResultMySqlModel taskResultMySqlModel) {
        return !taskResultMySqlModel.getTaskId().endsWith(taskResultMySqlModel.getFlowNodeId()) ?
                "_" + taskResultMySqlModel.getTaskId().split("_")[taskResultMySqlModel.getTaskId().split("_").length - 1] : "";
    }

    /**
     * <p>
     * Find out all the same branch nodes with the evaluation node
     * and find the modeling node from them
     * (this method solves the problem of null pointer when the evaluation node is deleted in the original editing process again)
     *
     * </p>
     *
     * @param taskResultMySqlModel
     * @return
     * @throws StatusCodeWithException
     */
    private TaskMySqlModel findEvaluationTaskByTaskResult(TaskResultMySqlModel taskResultMySqlModel) throws StatusCodeWithException {
        List<TaskMySqlModel> homologousBranchTaskList = taskService.findHomologousBranchByJobId(taskResultMySqlModel.getJobId(), taskResultMySqlModel.getRole(), taskResultMySqlModel.getTaskId());
        return homologousBranchTaskList
                .stream()
                .filter(x -> MODEL_COMPONENT_TYPE_LIST.contains(x.getTaskType()))
                .findFirst()
                .orElse(null);
    }

    private JObject getTrainObjByTaskId(String taskId) {
        TaskResultMySqlModel trainTaskResult = findEvaluationTrainTaskResultByTaskId(taskId);
        return trainTaskResult != null ? JObject.create(trainTaskResult.getResult()) : JObject.create("");
    }

    private JObject getValidateObjByTaskId(String taskId) {
        TaskResultMySqlModel validateTaskResult = findEvaluationValidateTaskResultByTaskId(taskId);
        return validateTaskResult != null ? JObject.create(validateTaskResult.getResult()) : JObject.create("");
    }

    private JObject getDistributionObjByTaskId(String taskId) {
        TaskResultMySqlModel result = findEvaluationDistributionTaskResultByTaskId(taskId);
        return result != null ? JObject.create(result.getResult()) : JObject.create("");
    }

    private TaskResultMySqlModel findEvaluationTaskResultByTaskId(String taskId) {
        TaskResultMySqlModel trainTaskResult = findEvaluationTrainTaskResultByTaskId(taskId);
        // Training and validation evaluation task_result only has different types,
        // and finally the results are merged and returned.
        return trainTaskResult != null ?
                ModelMapper.map(trainTaskResult, TaskResultMySqlModel.class) :
                ModelMapper.map(findEvaluationValidateTaskResultByTaskId(taskId), TaskResultMySqlModel.class);

    }

    private TaskResultMySqlModel findEvaluationTrainTaskResultByTaskId(String taskId) {
        return taskResultService.findByTaskIdAndType(taskId, TaskResultType.metric_train.name());
    }

    private TaskResultMySqlModel findEvaluationValidateTaskResultByTaskId(String taskId) {
        return taskResultService.findByTaskIdAndType(taskId, TaskResultType.metric_validate.name());
    }

    private TaskResultMySqlModel findEvaluationDistributionTaskResultByTaskId(String taskId) {
        return taskResultService.findByTaskIdAndType(taskId, TaskResultType.distribution_train_validate.name());
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

    private JObject parserScoresDistributionCurveData(JObject obj, String normalName) {
        JObject result = extractScoreDistributionData(obj, normalName);
        List<String> dataKey = result.keySet().stream().sorted()
                .collect(Collectors.toList());

        List<List<Object>> dataList = new ArrayList<>();

        for (int i = 0; i < dataKey.size(); i++) {
            String key = dataKey.get(i);
            String beforeKey = i == 0 ? "0" : dataKey.get(i - 1);
            String xAxis = beforeKey + "~" + key;

            int yAxis = result.getJObject(key).getIntValue("count");
            double yAxis2 = result.getJObject(key).getDoubleValue("count_rate");

            dataList.add(Arrays.asList(xAxis, yAxis, yAxis2));
        }

        return JObject.create().append("scores_distribution", dataList);
    }

    private JObject extractScoreDistributionData(JObject obj, String normalName) {
        String curveKey = "train_validate_" + normalName + "_scores_distribution";
        JObject scoresDistributionData = obj.getJObject(curveKey);
        JObject data = scoresDistributionData.getJObject("data");
        JObject result = data.getJObject("bin_result");
        return result;
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

        @Check(require = true)
        private boolean probNeedToBin;

        @Check
        private String binMethod;

        @Check
        private int binNum;


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

        public boolean isProbNeedToBin() {
            return probNeedToBin;
        }

        public void setProbNeedToBin(boolean probNeedToBin) {
            this.probNeedToBin = probNeedToBin;
        }

        public String getBinMethod() {
            return binMethod;
        }

        public void setBinMethod(String binMethod) {
            this.binMethod = binMethod;
        }

        public int getBinNum() {
            return binNum;
        }

        public void setBinNum(int binNum) {
            this.binNum = binNum;
        }
    }
}
