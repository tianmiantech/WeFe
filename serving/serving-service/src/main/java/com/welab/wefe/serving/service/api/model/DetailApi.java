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

package com.welab.wefe.serving.service.api.model;

import com.welab.wefe.common.enums.Algorithm;
import com.welab.wefe.common.enums.FederatedLearningType;
import com.welab.wefe.common.enums.JobMemberRole;
import com.welab.wefe.common.enums.PredictFeatureDataSource;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.util.JObject;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.serving.sdk.model.xgboost.XgboostDecisionTreeModel;
import com.welab.wefe.serving.sdk.model.xgboost.XgboostModel;
import com.welab.wefe.serving.sdk.model.xgboost.XgboostNodeModel;
import com.welab.wefe.serving.service.database.serving.entity.ModelMemberMySqlModel;
import com.welab.wefe.serving.service.database.serving.entity.ModelMySqlModel;
import com.welab.wefe.serving.service.database.serving.entity.ModelSqlConfigMySqlModel;
import com.welab.wefe.serving.service.database.serving.repository.ModelMemberRepository;
import com.welab.wefe.serving.service.database.serving.repository.ModelRepository;
import com.welab.wefe.serving.service.dto.ModelSqlConfigOutput;
import com.welab.wefe.serving.service.dto.PagingInput;
import com.welab.wefe.serving.service.dto.TreeNode;
import com.welab.wefe.serving.service.dto.TreeNodeData;
import com.welab.wefe.serving.service.manager.FeatureManager;
import com.welab.wefe.serving.service.service.CacheObjects;
import com.welab.wefe.serving.service.service.ModelService;
import com.welab.wefe.serving.service.service.ModelSqlConfigService;
import com.welab.wefe.serving.service.utils.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author hunter.zhao
 */
@Api(path = "model/detail", name = "Get model")
public class DetailApi extends AbstractApi<DetailApi.Input, DetailApi.Output> {
    @Autowired
    ModelService modelService;

    @Autowired
    private ModelRepository modelRepository;

    @Autowired
    private ModelMemberRepository modelMemberRepository;

    @Autowired
    private ModelSqlConfigService modelSqlConfigService;

    @Override
    protected ApiResult<DetailApi.Output> handle(Input input) {

        Optional<ModelMySqlModel> modelMySqlModel = modelRepository.findById(input.getId());
        if (modelMySqlModel == null) {
            return fail("No model was found");
        }
        ModelMySqlModel model = modelMySqlModel.get();

        DetailApi.Output output = ModelMapper.map(model, DetailApi.Output.class);

        output.setModelParam(JObject.create(model.getModelParam()).getJObject("model_param"));

        /**
         * Query role
         */
        List<ModelMemberMySqlModel> memberBaseInfo = modelMemberRepository.findByModelIdAndMemberId(model.getModelId(), CacheObjects.getMemberId());
        output.setMyRole(memberBaseInfo.stream().map(ModelMemberMySqlModel::getRole).collect(Collectors.toList()));

        /**
         * Query configuration
         */
        ModelSqlConfigMySqlModel sqlConfig = modelSqlConfigService.findOne(model.getModelId());
        if (sqlConfig != null) {
            output.setModelSqlConfig(ModelMapper.map(sqlConfig, ModelSqlConfigOutput.class));
        }

        /**
         * Query processor
         */
        output.setProcessor(FeatureManager.getProcessor(model.getModelId()));

        if (output.getAlgorithm() == Algorithm.XGBoost) {
            xgboost(output);
        }


        return success(output);
    }

    private void xgboost(Output output) {
        /**
         * feature
         */
        JObject feature = output.getModelParam().getJObject("featureNameFidMapping");

        XgboostModel model = output.getModelParam().toJavaObject(XgboostModel.class);

        /**
         * xgboost Tree structure settings
         * <p>
         * tree:[
         *    {
         *        "children":[
         *            Object{...},
         *            Object{...}
         *        ],
         *        "data":{
         *            "feature":"x15",
         *            "leaf":false,
         *            "left_node":1,
         *            "right_node":2,
         *            "sitename":"promoter:d3c9199e15154d9eac22690a55abc0f4",
         *            "split_maskdict":0.3127503322540728,
         *            "weight":-1.6183986372
         *        },
         *       "id":0
         *    }
         * ]
         * </p>
         */
        List<TreeNode> xgboost = new ArrayList<>();
        output.setXgboostTree(xgboost);

        List<XgboostDecisionTreeModel> trees = model.getTrees();
        for (int i = 0; i < trees.size(); i++) {

            Map<Integer, TreeNode> map = new HashMap<>(16);
            List<XgboostNodeModel> tree = trees.get(i).getTree();
            Map<Integer, Double> splitMaskdict = trees.get(i).getSplitMaskdict();

            //Composite node
            for (XgboostNodeModel xgboostNodeModel : tree) {
                //Find child nodes
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
                data.setThreshold(
                        output.flType == FederatedLearningType.vertical ?
                                splitMaskdict.get(xgboostNodeModel.getId()) : xgboostNodeModel.getBid());

                map.put(xgboostNodeModel.getId(), node);
            }

            //Traversing the processing node tree
            TreeNode root = map.get(0);
            recursive(map, root);

            xgboost.add(root);
        }
    }

    /**
     * Recursive fill tree
     */
    void recursive(Map<Integer, TreeNode> map, TreeNode root) {

        if (root.getData().isLeaf()) {
            return;
        }

        //Find left and right subtrees
        TreeNode leftNode = map.get(root.getData().getLeftNode());
        TreeNode rightNode = map.get(root.getData().getRightNode());

        //Set fill left and right subtrees
        recursive(map, leftNode);
        recursive(map, rightNode);

        //Add child node
        List<TreeNode> children = new ArrayList<>();
        children.add(leftNode);
        children.add(rightNode);
        root.setChildren(children);
    }

    public static class Input extends PagingInput {

        @Check(name = "主键id")
        private String id;

        //region getter/setter

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }


        //endregion
    }

    public static class Output extends AbstractApiInput {

        private String modelId;

        private Algorithm algorithm;

        private List<JobMemberRole> myRole;

        private FederatedLearningType flType;

        private String creator;

        private JObject modelParam;

        private Date createdTime;

        private PredictFeatureDataSource featureSource;

        private ModelSqlConfigOutput modelSqlConfig;

        private String processor;

        private List<TreeNode> xgboostTree;


        //region getter/setter

        public String getModelId() {
            return modelId;
        }

        public void setModelId(String modelId) {
            this.modelId = modelId;
        }

        public Algorithm getAlgorithm() {
            return algorithm;
        }

        public void setAlgorithm(Algorithm algorithm) {
            this.algorithm = algorithm;
        }

        public FederatedLearningType getFlType() {
            return flType;
        }

        public void setFlType(FederatedLearningType flType) {
            this.flType = flType;
        }

        public String getCreator() {
            return creator;
        }

        public void setCreator(String creator) {
            this.creator = creator;
        }

        public JObject getModelParam() {
            return modelParam;
        }

        public void setModelParam(JObject modelParam) {
            this.modelParam = modelParam;
        }

        public Date getCreatedTime() {
            return createdTime;
        }

        public void setCreatedTime(Date createdTime) {
            this.createdTime = createdTime;
        }

        public PredictFeatureDataSource getFeatureSource() {
            return featureSource;
        }

        public void setFeatureSource(PredictFeatureDataSource featureSource) {
            this.featureSource = featureSource;
        }

        public ModelSqlConfigOutput getModelSqlConfig() {
            return modelSqlConfig;
        }

        public void setModelSqlConfig(ModelSqlConfigOutput modelSqlConfig) {
            this.modelSqlConfig = modelSqlConfig;
        }

        public String getProcessor() {
            return processor;
        }

        public void setProcessor(String processor) {
            this.processor = processor;
        }

        public List<TreeNode> getXgboostTree() {
            return xgboostTree;
        }

        public void setXgboostTree(List<TreeNode> xgboostTree) {
            this.xgboostTree = xgboostTree;
        }

        public List<JobMemberRole> getMyRole() {
            return myRole;
        }

        public void setMyRole(List<JobMemberRole> myRole) {
            this.myRole = myRole;
        }

        //endregion
    }

}
