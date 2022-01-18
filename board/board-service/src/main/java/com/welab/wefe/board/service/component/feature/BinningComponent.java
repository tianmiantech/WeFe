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

package com.welab.wefe.board.service.component.feature;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.welab.wefe.board.service.component.DataIOComponent;
import com.welab.wefe.board.service.component.base.AbstractComponent;
import com.welab.wefe.board.service.component.base.io.IODataType;
import com.welab.wefe.board.service.component.base.io.InputMatcher;
import com.welab.wefe.board.service.component.base.io.Names;
import com.welab.wefe.board.service.component.base.io.OutputItem;
import com.welab.wefe.board.service.database.entity.job.TaskMySqlModel;
import com.welab.wefe.board.service.database.entity.job.TaskResultMySqlModel;
import com.welab.wefe.board.service.exception.FlowNodeException;
import com.welab.wefe.board.service.model.FlowGraph;
import com.welab.wefe.board.service.model.FlowGraphNode;
import com.welab.wefe.board.service.service.CacheObjects;
import com.welab.wefe.common.enums.ComponentType;
import com.welab.wefe.common.enums.JobMemberRole;
import com.welab.wefe.common.enums.TaskResultType;
import com.welab.wefe.common.fieldvalidate.AbstractCheckModel;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.util.JObject;

/**
 * @author lonnie
 */
@Service
public class BinningComponent extends AbstractComponent<BinningComponent.Params> {

    @Override
    protected void checkBeforeBuildTask(FlowGraph graph, List<TaskMySqlModel> preTasks, FlowGraphNode node, Params params) throws FlowNodeException {

        FlowGraphNode intersectionNode = graph.findOneNodeFromParent(node, ComponentType.Intersection);
        if (intersectionNode == null) {
            throw new FlowNodeException(node, "请在前面添加样本对齐组件。");
        }
        
        if (CollectionUtils.isEmpty(params.getMembers())) {
            throw new FlowNodeException(node, "请添加分箱策略");
        }

        DataIOComponent.Params dataIOParams = (DataIOComponent.Params) graph.findOneNodeFromParent(node, ComponentType.DataIO).getParamsModel();
        List<DataIOComponent.DataSetItem> dataSetItems = dataIOParams.getDataSetList();

        AtomicInteger count = new AtomicInteger();

        dataSetItems.forEach(x ->
                params.getMembers().forEach(y -> {
                    if (x.getMemberId().equals(y.getMemberId()) && x.getMemberRole() == y.getMemberRole()) {
                        count.addAndGet(1);
                    }
                })
        );

        if (count.get() != dataSetItems.size()) {
            throw new FlowNodeException(node, "请保证当前节点所有成员都参与。");
        }

    }


    @Override
    public ComponentType taskType() {
        return ComponentType.Binning;
    }

    @Override
    protected JSONObject createTaskParams(FlowGraph graph, List<TaskMySqlModel> preTasks, FlowGraphNode node, Params params) throws FlowNodeException {

        JSONObject taskParam = new JSONObject();

        // Reassemble front-end parameters
        JObject transformParam = JObject.create()
                .append("transform_cols", -1)
                .append("transform_names", new ArrayList<>())
                .append("transform_type", "bin_num");

        JObject optimalBinningParam = JObject.create()
                .append("metric_method", "chi_square")
                .append("min_bin_pct", 0.05)
                .append("max_bin_pct", 0.8)
                .append("init_bucket_method", "quantile")
                .append("init_bin_nums", 100)
                .append("mixture", true);

        List<Member> members = params.members;
        List<JObject> modesObj = new ArrayList<>();
        for (Member member : members) {

            List<Feature> features = member.features;

            // Integrate the features of the same binning strategy in the same member
            Map<String, List<String>> featureBinningMap = new HashMap<>();

            Map<String, List<Float>> featurePoints = new HashMap<>();
            for (Feature feature : features) {

                if (featureBinningMap.containsKey(feature.method.name() + "," + feature.count)) {
                    featureBinningMap.get(feature.method.name() + "," + feature.count).add(feature.name);
                } else {
                    List<String> featureList = new ArrayList<>();
                    featureList.add(feature.name);
                    featureBinningMap.put(feature.method.name() + "," + feature.count, featureList);
                }

                if (feature.method == BinningMethod.custom) {
                    String points = feature.getPoints();
                    if (!StringUtils.isBlank(points)) {
                        List<Float> pointList = new LinkedList<>();
                        if (points.startsWith("[") && points.endsWith("]")) {
                            points = points.substring(1, points.length() - 1);
                        }
                        String pointArr[] = points.split(",|，");
                        for (String p : pointArr) {
                            pointList.add(Float.parseFloat(p));
                        }
                        featurePoints.put(feature.name, pointList);
                    }
                }

            }

            // Build the array of modes required by the kernel
            for (Map.Entry<String, List<String>> entry : featureBinningMap.entrySet()) {
                String[] strArr = entry.getKey().split(",");

                JObject memberObj = JObject.create().append("role", member.memberRole)
                        .append("member_id", member.memberId).append("bin_feature_names", entry.getValue());
                if (BinningMethod.custom.name().equals(strArr[0])) {
                    Map<String, List<Float>> featurePointsTmp = new HashMap<>();
                    for (String s : entry.getValue()) {
                        featurePointsTmp.put(s, featurePoints.get(s));
                    }
                    memberObj.append("feature_split_points", featurePointsTmp);
                }

                if (modesObj.size() < 1) {
                    // Build the first mode node
                    JSONArray jsonArray = new JSONArray();
                    jsonArray.add(memberObj);
                    JObject modeObj = JObject.create()
                            .append("method", strArr[0])
                            .append("bin_num", Integer.parseInt(strArr[1]))
                            .append("members", jsonArray
                            );
                    modesObj.add(modeObj);
                } else {

                    // If there are the same binning strategies, put them together.
                    boolean insertFlag = false;
                    for (JObject obj : modesObj) {
                        if (obj.getString("method").equals(strArr[0]) && obj.getIntValue("bin_num") == Integer.parseInt(strArr[1])) {
                            JSONArray list = obj.getJSONArray("members");
                            list.add(memberObj);
                            obj.put("members", list);
                            insertFlag = true;
                            break;
                        }
                    }

                    // The current binning strategy cannot be found in the existing sharing strategies,
                    // save a new binning strategy node.
                    if (!insertFlag) {
                        JSONArray jsonArray = new JSONArray();
                        jsonArray.add(memberObj);
                        JObject modeObj = JObject.create()
                                .append("method", strArr[0])
                                .append("bin_num", strArr[1])
                                .append("members", jsonArray
                                );
                        modesObj.add(modeObj);
                    }
                }
            }
        }

        JObject binningParam = JObject.create()
                .append("method", "quantile")
                .append("compress_thres", 10000)
                .append("head_size", 10000)
                .append("error", 0.001)
                .append("adjustment_factor", 0.5)
                .append("bin_num", 10)
                .append("bin_indexes", -1)
                .append("bin_names", new ArrayList<>())
                .append("category_indexes", new ArrayList<>())
                .append("category_names", new ArrayList<>())
                .append("local_only", false)
                .append("need_run", true)
                .append("transform_param", transformParam)
                .append("optimal_binning_param", optimalBinningParam)
                .append("modes", modesObj);

        taskParam.put("params", binningParam);

        return taskParam;
    }

    @Override
    protected List<TaskResultMySqlModel> getAllResult(String taskId) {

        List<TaskResultMySqlModel> list = taskResultService.listAllResult(taskId)
                .stream()
                .filter(x -> x.getType().equals(TaskResultType.model_binning))
                .collect(Collectors.toList());

        // Put the reassembled data in
        list.add(getResult(taskId, TaskResultType.model_binning.name()));

        return list;
    }

    @Override
    protected TaskResultMySqlModel getResult(String taskId, String type) {

        TaskResultMySqlModel taskResult = taskResultService.findByTaskIdAndType(taskId, TaskResultType.model_binning.name());

        if (taskResult == null) {
            return null;
        }
        TaskResultMySqlModel taskResultMySqlModel = new TaskResultMySqlModel();
        BeanUtils.copyProperties(taskResult, taskResultMySqlModel);

        JObject obj = JObject.create(taskResult.getResult());
        List<JObject> resultList = new ArrayList<>();

        if (obj != null) {
            JObject modelParam = obj.getJObject("model_param");
            if (modelParam != null) {
                JObject binningResult = modelParam.getJObject("binningResult");

                if (binningResult != null) {

                    String memberName = CacheObjects.getMemberName(binningResult.getString("memberId"));
                    binningResult.append("member_name", memberName)
                            .append("member_id", binningResult.getString("memberId"))
                            .append("member_role", binningResult.getString("role"));

                    resultList.add(binningResult);
                }

                List<JObject> providerResults = modelParam.getJSONList("providerResults");
				Map<String, JObject> biningResultMap = new HashMap<>();
				if (CollectionUtils.isNotEmpty(providerResults)) {
					for (JObject providerResult : providerResults) {
						String memberName = CacheObjects.getMemberName(providerResult.getString("memberId"));
						String key = memberName + "_" + providerResult.getString("memberId") + "_"
								+ providerResult.getString("role");
						if (biningResultMap.containsKey(key)) {
							// merge
							JObject result = biningResultMap.get(key);
							JObject temp = result.getJObject("binningResult");
							temp.putAll(providerResult.getJObject("binningResult"));
							result.put("binningResult", temp);
							biningResultMap.put(key, result);
						} else {
							// add
							providerResult.append("member_name", memberName)
									.append("member_id", providerResult.getString("memberId"))
									.append("member_role", providerResult.getString("role"));
							biningResultMap.put(key, providerResult);
						}

					}
					for (Map.Entry<String, JObject> entry : biningResultMap.entrySet()) {
						resultList.add(entry.getValue());
					}
				}

                taskResultMySqlModel.setResult(JObject.create().append("result", resultList).toJSONString());
            }
        }

        return taskResultMySqlModel;

    }

    @Override
    protected List<InputMatcher> inputs(FlowGraph graph, FlowGraphNode node) {
        return Arrays.asList(
                InputMatcher.of(Names.Data.NORMAL_DATA_SET, IODataType.DataSetInstance)
        );
    }

    @Override
    public List<OutputItem> outputs(FlowGraph graph, FlowGraphNode node) {
        return Arrays.asList(
                OutputItem.of(Names.Model.BINNING_MODEL, IODataType.ModelFromBinning),
                OutputItem.of(Names.Data.NORMAL_DATA_SET, IODataType.DataSetInstance)
        );
    }

    @Override
    protected boolean needIntersectedDataSetBeforeMe() {
        return false;
    }

    public enum BinningMethod {
        /**
         * Equal frequency
         */
        quantile,
        /**
         * Equal width
         */
        bucket,
        /**
         * Bangla
         */
        optimal,

        custom
    }

    public static class Params extends AbstractCheckModel {

        @Check(require = true)
        private List<Member> members;

        //region getter/setter

        public List<Member> getMembers() {
            return members;
        }

        public void setMembers(List<Member> members) {
            this.members = members;
        }


        //endregion
    }


    public static class Member extends AbstractCheckModel {
        @Check(require = true)
        private String memberId;
        @Check(require = true)
        private JobMemberRole memberRole;
        @Check(require = true)
        private List<Feature> features;

        //region getter/setter

        public String getMemberId() {
            return memberId;
        }

        public void setMemberId(String memberId) {
            this.memberId = memberId;
        }

        public JobMemberRole getMemberRole() {
            return memberRole;
        }

        public void setMemberRole(JobMemberRole memberRole) {
            this.memberRole = memberRole;
        }

        public List<Feature> getFeatures() {
            return features;
        }

        public void setFeatures(List<Feature> features) {
            this.features = features;
        }


        //endregion
    }

    public static class Feature extends AbstractCheckModel {

        @Check(require = true)
        private String name;
        @Check(require = true)
        private BinningMethod method;
        @Check(require = true)
        private int count;
        private String points;

        //region getter/setter

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public BinningMethod getMethod() {
            return method;
        }

        public void setMethod(BinningMethod method) {
            this.method = method;
        }

        public int getCount() {
            return count;
        }

        public void setCount(int count) {
            this.count = count;
        }

        public String getPoints() {
            return points;
        }

        public void setPoints(String points) {
            this.points = points;
        }
        //endregion
    }

    @Override
    public boolean canSelectFeatures() {
        return true;
    }


}
