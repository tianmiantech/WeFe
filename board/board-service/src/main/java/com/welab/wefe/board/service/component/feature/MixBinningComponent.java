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
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

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
 * @author Winter
 * @date 2020-12-24
 */
@Service
public class MixBinningComponent extends AbstractComponent<MixBinningComponent.Params> {

    @Override
    protected void checkBeforeBuildTask(FlowGraph graph, List<TaskMySqlModel> preTasks, FlowGraphNode node, Params params) throws FlowNodeException {

        if (CollectionUtils.isEmpty(params.getMembers())) {
            throw new FlowNodeException(node, "请添加分箱策略");
        }

        DataIOComponent.Params dataIOParams = (DataIOComponent.Params) graph.findOneNodeFromParent(node, ComponentType.DataIO).getParamsModel();
        List<DataIOComponent.DataSetItem> dataSetItems = dataIOParams.getDataSetList();

        AtomicInteger count = new AtomicInteger();

        dataSetItems.forEach(x -> {
            params.getMembers().forEach(y -> {
                if (x.getMemberId().equals(y.getMemberId()) && x.getMemberRole() == y.getMemberRole()) {
                    count.addAndGet(1);
                }
            });
        });

        if (count.get() != dataSetItems.size()) {
            throw new FlowNodeException(node, "请保证当前节点所有成员都参与。");
        }

    }


    @Override
    public ComponentType taskType() {
        return ComponentType.MixBinning;
    }

	@Override
	protected JSONObject createTaskParams(FlowGraph graph, List<TaskMySqlModel> preTasks, FlowGraphNode node,
			Params params) throws FlowNodeException {

		JSONObject taskParam = new JSONObject();
		List<Member> members = params.members;
		int bin_num = 10;
		List<String> bin_names = new ArrayList<>();
		for (Member member : members) {
			if (CacheObjects.getMemberId().equals(member.getMemberId())) {
				List<Feature> features = member.features;
				features.stream().forEach(x -> {
					bin_names.add(x.getName());
				});
				bin_num = features.get(0).getCount();
			}
		}
		JObject binningParam = JObject.create().append("bin_num", bin_num).append("bin_names", bin_names);

		taskParam.put("params", binningParam);

		return taskParam;
	}

    @Override
    protected List<TaskResultMySqlModel> getAllResult(String taskId) {

        List<TaskResultMySqlModel> list = taskResultService.listAllResult(taskId)
                .stream()
                .filter(x -> x.getType().equals(TaskResultType.model_binning))
                .collect(Collectors.toList());

        //将重新组装的数据放进去
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
                if (CollectionUtils.isNotEmpty(providerResults)) {

                    for (JObject providerResult : providerResults) {
                        String memberName = CacheObjects.getMemberName(providerResult.getString("memberId"));
                        providerResult.append("member_name", memberName)
                                .append("member_id", providerResult.getString("memberId"))
                                .append("member_role", providerResult.getString("role"));

                        resultList.add(providerResult);
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
         * 等频
         */
        quantile,
        /**
         * 等宽
         */
        bucket,
        /**
         * 卡方
         */
        optimal
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


        //endregion
    }

    @Override
    public boolean canSelectFeatures() {
        return true;
    }


}
