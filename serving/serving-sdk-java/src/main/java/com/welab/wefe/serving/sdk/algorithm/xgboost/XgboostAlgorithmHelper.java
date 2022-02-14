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

package com.welab.wefe.serving.sdk.algorithm.xgboost;

import com.alibaba.fastjson.util.TypeUtils;
import com.welab.wefe.serving.sdk.model.PredictModel;
import com.welab.wefe.serving.sdk.model.xgboost.XgboostDecisionTreeModel;
import com.welab.wefe.serving.sdk.model.xgboost.XgboostModel;
import com.welab.wefe.serving.sdk.model.xgboost.XgboostNodeModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static java.lang.Math.exp;

/**
 * @author hunter.zhao
 */
public class XgboostAlgorithmHelper {
    /**
     * sigmod function
     */
    private static double sigmod(double x) {
        return 1. / (1. + exp(-x));
    }


    /**
     * Determine whether leaf
     *
     * @param model      Model structure
     * @param treeId     Tree number
     * @param treeNodeId Tree Node Number
     */
    private static boolean isLeaf(XgboostModel model, int treeId, int treeNodeId) {
        return model.getTrees().get(treeId).getTree().get(treeNodeId).isLeaf();
    }

    /**
     * Get federated Roles
     *
     * @param model      Model structure
     * @param treeId     Tree number
     * @param treeNodeId Tree Node Number
     */
    private static String getSite(XgboostModel model, int treeId, int treeNodeId) {
        return model.getTrees().get(treeId).getTree().get(treeNodeId).getSitename().split(":", -1)[0];
    }


    /**
     * To get the weight
     *
     * @param model      Model structure
     * @param treeId     Tree number
     * @param treeNodeId Tree Node Number
     */
    private static double getTreeLeafWeight(XgboostModel model, int treeId, int treeNodeId) {
        return model.getTrees().get(treeId).getTree().get(treeNodeId).getWeight();
    }


    private static final int CATEGORY = 2;

    /**
     * Merge final estimates
     *
     * @param model   Model structure
     * @param weights The weight of each tree
     */
    private static Object finalPredict(XgboostModel model, double[] weights) {

        if (model.getNumClasses() == CATEGORY) {

            double sum = 0;
            for (int i = 0; i < model.getTreeNum(); i++) {
                sum += weights[i] * model.getLearningRate();
            }

//            return new Double[]{sigmod(sum) , 1 - sigmod(sum) };
            return sigmod(sum);
        } else if (model.getNumClasses() > CATEGORY) {

            double[] sumWeights = new double[model.getTreeDim()];
            for (int i = 0; i < model.getTreeNum(); i++) {
                sumWeights[i % model.getTreeDim()] += weights[i] * model.getLearningRate();
            }

            for (int i = 0; i < model.getTreeDim(); i++) {
                sumWeights[i] += model.getInitScore().get(i);
            }

            return softmax(model, sumWeights);

        } else {

            double sum = model.getInitScore().get(0);
            for (int i = 0; i < model.getTreeNum(); i++) {
                sum += weights[i] * model.getLearningRate();
            }

            return sum;
        }
    }


    private static Map<String, Object> softmax(XgboostModel model, double[] weights) {

        int n = weights.length, maxIndex = 0;
        double max = weights[0], denominator = 0.0;

        for (int i = 0; i < n; i++) {
            if (weights[i] > weights[maxIndex]) {
                maxIndex = i;
                max = weights[i];
            }
        }

        for (int i = 0; i < n; i++) {
            weights[i] = Math.exp(weights[i] - max);
            denominator += weights[i];
        }

        ArrayList<Double> scores = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            scores.add(weights[i] / denominator);
        }

        Map<String, Object> ret = new HashMap<>(16);
        ret.put("label", model.getClasses().get(maxIndex));
        ret.put("score", scores);

        return ret;
    }


    /**
     * Get the next node (horizontal federation)
     *
     * @param model          Model structure
     * @param treeId         Tree number
     * @param treeNodeId     Tree Node Number
     * @param featureDataMap Processed feature maps
     */
    private static int nextTreeNodeIdByHorz(XgboostModel model, int treeId, int treeNodeId, Map<String, Object> featureDataMap) {

        int fid = model.getTrees().get(treeId).getTree(treeNodeId).getFid();
        double bidValue = model.getTrees().get(treeId).getTree(treeNodeId).getBid();
        String fidStr = String.valueOf(fid);

        return getNextNodeId(model, treeId, treeNodeId, featureDataMap, bidValue, fidStr);

    }


    /**
     * Get the next node (vertical federation)
     *
     * @param model          Model structure
     * @param treeId         Tree number
     * @param treeNodeId     Tree Node Number
     * @param featureDataMap Processed feature maps
     */
    private static int nextTreeNodeIdByVert(XgboostModel model, int treeId, int treeNodeId, Map<String, Object> featureDataMap) {

        int fid = model.getTrees().get(treeId).getTree(treeNodeId).getFid();
        double splitValue = model.getTrees().get(treeId).getSplitMaskdict().get(treeNodeId);
        String fidStr = String.valueOf(fid);

        return getNextNodeId(model, treeId, treeNodeId, featureDataMap, splitValue, fidStr);
    }

    private static final double COMPARING_VALUES = 1e-20;

    /**
     * Gets the next node
     *
     * @param model          Model structure
     * @param treeId         Tree number
     * @param treeNodeId     Tree Node Number
     * @param featureDataMap Processed feature maps
     * @param value          Node threshold
     * @param fidStr         Characteristics of the serial number
     */
    private static int getNextNodeId(XgboostModel model, int treeId, int treeNodeId, Map<String, Object> featureDataMap, double value, String fidStr) {
        if (featureDataMap.containsKey(fidStr)) {
            //Find the threshold by the number
            if (TypeUtils.castToDouble(featureDataMap.get(fidStr)) <= value + COMPARING_VALUES) {
                return model.getTrees().get(treeId).getTree().get(treeNodeId).getLeftNodeId();
            } else {
                return model.getTrees().get(treeId).getTree().get(treeNodeId).getRightNodeId();
            }
        } else {
            //Find the default path
            if (model.getTrees().get(treeId).getTree(treeNodeId).getMissingDir() != null) {
                int missingDir = model.getTrees().get(treeId).getTree(treeNodeId).getMissingDir();
                if (missingDir == 1) {
                    return model.getTrees().get(treeId).getTree(treeNodeId).getRightNodeId();
                } else {
                    return model.getTrees().get(treeId).getTree(treeNodeId).getLeftNodeId();
                }
            } else {
                return model.getTrees().get(treeId).getTree(treeNodeId).getRightNodeId();
            }
        }
    }


    /**
     * Prediction by sponsor (horizontal)
     */
    public static PredictModel promoterPredictByHorz(XgboostModel model, String userId, Map<String, Object> featureDataMap) {

        //Traverse to get the root node
        int[] treeNodeIds = new int[model.getTreeNum()];
        double[] weights = new double[model.getTreeNum()];

        /**
         * Local decision making
         **/
        for (int i = 0; i < model.getTreeNum(); i++) {

            /**
             * Traverse to get the leaf node
             */
            int treeNodeId = treeNodeIds[i];
            while (!isLeaf(model, i, treeNodeId)) {
                treeNodeId = nextTreeNodeIdByHorz(model, i, treeNodeId, featureDataMap);
            }
            treeNodeIds[i] = treeNodeId;
        }

        for (int i = 0; i < model.getTreeNum(); i++) {

            /**
             * Get weight value
             */
            weights[i] = getTreeLeafWeight(model, i, treeNodeIds[i]);
        }

        return PredictModel.ofScores(userId, finalPredict(model, weights));
    }

    /**
     * Prediction of sponsor (longitudinal)
     *
     * @param model           model
     * @param userId          userId
     * @param featureDataMap  featureDataMap
     * @param decisionTreeMap decisionTreeMap
     * @return PredictModel
     */
    public static PredictModel promoterPredictByVert(XgboostModel model, String userId, Map<String, Object> featureDataMap, Map<String, Map<String, Boolean>> decisionTreeMap) {
        int[] treeNodeIds = new int[model.getTreeNum()];
        double[] weights = new double[model.getTreeNum()];

        while (true) {

            /**
             * Tree to be processed
             * <p>
             * - Determine whether leaf
             * - Local decision, find child nodes
             * - The loop is broken when the tree to be processed is empty
             * - Federated decision, find child nodes
             * </>
             */
            Map<String, Object> pendingTree = new HashMap<>(16);

            /**
             * Local decision making
             **/
            for (int i = 0; i < model.getTreeNum(); i++) {

                if (isLeaf(model, i, treeNodeIds[i])) {
                    continue;
                }

                treeNodeIds[i] = decision(model, i, treeNodeIds[i], featureDataMap);

                if (!isLeaf(model, i, treeNodeIds[i])) {
                    pendingTree.put(String.valueOf(i), treeNodeIds[i]);
                }

            }

            if (pendingTree.size() == 0) {
                break;
            }

            //The federal decision
            for (String treeIdx : pendingTree.keySet()) {

                int idx = Integer.parseInt(treeIdx);
                int curNodeId = (Integer) pendingTree.get(treeIdx);
                int finalNodeId = federatedDecision(model, idx, curNodeId, featureDataMap, decisionTreeMap);
                treeNodeIds[idx] = finalNodeId;
            }
        }

        for (int i = 0; i < model.getTreeNum(); i++) {

            /**
             * Get weight value
             */
            weights[i] = getTreeLeafWeight(model, i, treeNodeIds[i]);
        }

        return PredictModel.ofScores(
                userId,
                finalPredict(model, weights)
        );
    }


    private static final String PROMOTER = "promoter";

    /**
     * The initiator decides to take the child node
     *
     * @param model      model
     * @param treeId     treeId
     * @param treeNodeId treeNodeId
     * @param input      input
     * @return treeNodeId
     */
    private static int decision(XgboostModel model, int treeId, int treeNodeId, Map<String, Object> input) {

        while (!isLeaf(model, treeId, treeNodeId) && getSite(model, treeId, treeNodeId).equals(PROMOTER)) {
            treeNodeId = nextTreeNodeIdByVert(model, treeId, treeNodeId, input);
        }

        return treeNodeId;
    }

    /**
     * Federated decision trees take leaf nodes
     *
     * @param model           model
     * @param treeId          treeId
     * @param treeNodeId      treeNodeId
     * @param featureDataMap  featureDataMap
     * @param decisionTreeMap decisionTreeMap
     * @return treeNodeId
     */
    private static int federatedDecision(XgboostModel model, int treeId, int treeNodeId, Map<String, Object> featureDataMap, Map<String, Map<String, Boolean>> decisionTreeMap) {

        while (!isLeaf(model, treeId, treeNodeId)) {
            if (getSite(model, treeId, treeNodeId).equals(PROMOTER)) {
                treeNodeId = nextTreeNodeIdByVert(model, treeId, treeNodeId, featureDataMap);
            } else {
                Map<String, Boolean> decision = decisionTreeMap.get(String.valueOf(treeId));
                if (decision.get(String.valueOf(treeNodeId))) {
                    treeNodeId = model.getTrees().get(treeId).getTree().get(treeNodeId).getLeftNodeId();
                } else {
                    treeNodeId = model.getTrees().get(treeId).getTree().get(treeNodeId).getRightNodeId();
                }
            }
        }

        return treeNodeId;
    }


    private static final String PROVIDER = "provider";

    /**
     * Cooperative side prediction method
     *
     * @param model          model
     * @param userId         userId
     * @param featureDataMap featureDataMap
     * @return PredictModel
     */
    public static PredictModel providerPredict(XgboostModel model, String userId, Map<String, Object> featureDataMap) {
        Map<String, Map<String, Boolean>> result = new HashMap<>(16);

        //Traverse the tree
        for (int i = 0; i < model.getTrees().size(); i++) {

            XgboostDecisionTreeModel decisionTree = model.getTrees().get(i);
            Map<String, Boolean> treeRoute = new HashMap<>(16);

            for (int j = 0; j < decisionTree.getTree().size(); j++) {

                XgboostNodeModel tree = decisionTree.getTree().get(j);

                if (!PROVIDER.equals(getSite(model, i, j))) {
                    continue;
                }

                int fid = tree.getFid();

                /**
                 * False Select the right node
                 * true Select the left node
                 */
                boolean direction = false;

                if (featureDataMap.containsKey(Integer.toString(fid)) && decisionTree.getSplitMaskdict().containsKey(j)) {
                    Object featVal = featureDataMap.get(Integer.toString(fid));
                    double splitValue = decisionTree.getSplitMaskdict().get(j);
                    direction = TypeUtils.castToDouble(featVal) <= splitValue + 1e-20;
                } else {
                    if (decisionTree.getMissingDirMaskdict().containsKey(Integer.toString(j))) {
                        int missingDir = decisionTree.getMissingDirMaskdict().get(Integer.toString(j));
                        direction = (missingDir != 1);
                    }
                }

                treeRoute.put(Integer.toString(j), direction);
            }

            result.put(Integer.toString(i), treeRoute);
        }

        return PredictModel.ofObject(userId, result);
    }
}
