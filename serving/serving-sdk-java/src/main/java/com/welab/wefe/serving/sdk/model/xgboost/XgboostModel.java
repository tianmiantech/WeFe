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

package com.welab.wefe.serving.sdk.model.xgboost;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author hunter.zhao
 */
public class XgboostModel {
    private Map<String, String> featureNameFidMapping = new HashMap<>();
    private int treeNum;
    private List<Double> initScore;
    private List<XgboostDecisionTreeModel> trees;
    private int numClasses;
    private List<String> classes;
    private int treeDim;
    private double learningRate;
    private boolean fastMode = true;

    public Map<String, String> getFeatureNameFidMapping() {
        return featureNameFidMapping;
    }

    public void setFeatureNameFidMapping(Map<String, String> featureNameFidMapping) {
        this.featureNameFidMapping = featureNameFidMapping;
    }

    public int getTreeNum() {
        return treeNum;
    }

    public void setTreeNum(int treeNum) {
        this.treeNum = treeNum;
    }

    public List<Double> getInitScore() {
        return initScore;
    }

    public void setInitScore(List<Double> initScore) {
        this.initScore = initScore;
    }

    public List<XgboostDecisionTreeModel> getTrees() {
        return trees;
    }

    public void setTrees(List<XgboostDecisionTreeModel> trees) {
        this.trees = trees;
    }

    public int getNumClasses() {
        return numClasses;
    }

    public void setNumClasses(int numClasses) {
        this.numClasses = numClasses;
    }

    public List<String> getClasses() {
        return classes;
    }

    public void setClasses(List<String> classes) {
        this.classes = classes;
    }

    public int getTreeDim() {
        return treeDim;
    }

    public void setTreeDim(int treeDim) {
        this.treeDim = treeDim;
    }

    public double getLearningRate() {
        return learningRate;
    }

    public void setLearningRate(double learningRate) {
        this.learningRate = learningRate;
    }

    public boolean isFastMode() {
        return fastMode;
    }

    public void setFastMode(boolean fastMode) {
        this.fastMode = fastMode;
    }
}
