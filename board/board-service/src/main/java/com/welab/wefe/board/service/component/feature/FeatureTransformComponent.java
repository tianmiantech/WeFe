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

import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.welab.wefe.board.service.component.base.AbstractComponent;
import com.welab.wefe.board.service.component.base.io.IODataType;
import com.welab.wefe.board.service.component.base.io.InputMatcher;
import com.welab.wefe.board.service.component.base.io.Names;
import com.welab.wefe.board.service.component.base.io.OutputItem;
import com.welab.wefe.board.service.database.entity.job.TaskMySqlModel;
import com.welab.wefe.board.service.database.entity.job.TaskResultMySqlModel;
import com.welab.wefe.board.service.dto.kernel.Member;
import com.welab.wefe.board.service.exception.FlowNodeException;
import com.welab.wefe.board.service.model.FlowGraph;
import com.welab.wefe.board.service.model.FlowGraphNode;
import com.welab.wefe.board.service.service.CacheObjects;
import com.welab.wefe.common.enums.ComponentType;
import com.welab.wefe.common.fieldvalidate.AbstractCheckModel;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.util.JObject;

/**
 * @author lonnie
 * @date 2021-04-25
 */
@Service
public class FeatureTransformComponent extends AbstractComponent<FeatureTransformComponent.Params> {

    @Override
    protected void checkBeforeBuildTask(FlowGraph graph, List<TaskMySqlModel> preTasks, FlowGraphNode node,
            FeatureTransformComponent.Params params) throws FlowNodeException {
        FlowGraphNode intersectionNode = graph.findOneNodeFromParent(node, ComponentType.Intersection);
        if (intersectionNode == null) {
            throw new FlowNodeException(node, "请在前面添加样本对齐组件。");
        }
    }

    @Override
    protected JSONObject createTaskParams(FlowGraph graph, List<TaskMySqlModel> preTasks, FlowGraphNode node,
            FeatureTransformComponent.Params params) throws FlowNodeException {

        JSONObject taskParam = new JSONObject();

        List<Params.TransformMember> members = params.getMembers();

        JObject transformRules = JObject.create();
        members.forEach(x -> {
            if (CacheObjects.getMemberId().equals(x.getMemberId()) && graph.getJob().getMyRole() == x.getMemberRole()) {
                List<Params.Feature> features = x.getFeatures();

                features.forEach(feature -> {
                    List<List<String>> maps = feature.getTransforms();
                    JObject rulesObj = JObject.create();
                    for (List<String> map : maps) {
                        rulesObj.append(map.get(0), map.get(1));
                    }
                    if(!rulesObj.isEmpty()) {
                        transformRules.append(feature.getName(), rulesObj);    
                    }
                });

            }

        });

        taskParam.put("params", JObject.create().append("transform_rules", transformRules.toJSONString()));

        return taskParam;
    }

    @Override
    public ComponentType taskType() {
        return ComponentType.FeatureTransform;
    }

    @Override
    protected List<TaskResultMySqlModel> getAllResult(String taskId) {
        return null;
    }

    @Override
    protected TaskResultMySqlModel getResult(String taskId, String type) {
        return null;
    }

    @Override
    public boolean canSelectFeatures() {
        return true;
    }
    
    @Override
    protected List<InputMatcher> inputs(FlowGraph graph, FlowGraphNode node) throws FlowNodeException {
        return Arrays.asList(InputMatcher.of(Names.Data.NORMAL_DATA_SET, IODataType.DataSetInstance));
    }

    @Override
    public List<OutputItem> outputs(FlowGraph graph, FlowGraphNode node) throws FlowNodeException {
        return Arrays.asList(OutputItem.of(Names.Data.NORMAL_DATA_SET, IODataType.DataSetInstance));
    }

    public static class Params extends AbstractCheckModel {

        @Check(name = "转换成员信息", require = true)
        private List<TransformMember> members;

        public List<TransformMember> getMembers() {
            return members;
        }

        public void setMembers(List<TransformMember> members) {
            this.members = members;
        }

        public static class TransformMember extends Member {

            @Check(name = "特征列", require = true)
            private List<Feature> features;

            public List<Feature> getFeatures() {
                return features;
            }

            public void setFeatures(List<Feature> features) {
                this.features = features;
            }
        }

        public static class Feature extends AbstractCheckModel {
            @Check(name = "特征名", require = true)
            private String name;

            @Check(name = "转换规则")
            private List<List<String>> transforms;

            public String getName() {
                return name;
            }

            public void setName(String name) {
                this.name = name;
            }

            public List<List<String>> getTransforms() {
                return transforms;
            }

            public void setTransforms(List<List<String>> transforms) {
                this.transforms = transforms;
            }

        }
    }
}
