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

package com.welab.wefe.serving.sdk.model.xgboost;

import java.util.List;
import java.util.Map;

/**
 * Decision tree
 *
 * @author hunter.zhao
 */
public class XgboostDecisionTreeModel {
    private List<XgboostNodeModel> tree;

    private Map<Integer, Double> splitMaskdict;

    private Map<String, Integer> missingDirMaskdict;

    public List<XgboostNodeModel> getTree() {
        return tree;
    }

    public XgboostNodeModel getTree(int treeNodeId) {
        return tree.get(treeNodeId);
    }

    public void setTree(List<XgboostNodeModel> tree) {
        this.tree = tree;
    }

    public Map<Integer, Double> getSplitMaskdict() {
        return splitMaskdict;
    }

    public void setSplitMaskdict(Map<Integer, Double> splitMaskdict) {
        this.splitMaskdict = splitMaskdict;
    }

    public Map<String, Integer> getMissingDirMaskdict() {
        return missingDirMaskdict;
    }

    public void setMissingDirMaskdict(Map<String, Integer> missingDirMaskdict) {
        this.missingDirMaskdict = missingDirMaskdict;
    }
}
