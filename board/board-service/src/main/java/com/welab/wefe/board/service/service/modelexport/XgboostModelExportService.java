/*
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

package com.welab.wefe.board.service.service.modelexport;

import com.alibaba.fastjson.JSONArray;
import com.welab.wefe.common.util.JObject;
import com.welab.wefe.common.util.StringUtil;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * XGBoots model export service
 *
 * @author aaron.li
 **/
@Service
public class XgboostModelExportService {

    /**
     * export model
     *
     * @param modelParam model param
     * @param language   language
     */
    public String export(JObject modelParam, String language) {
        // Get the corresponding language interpreter
        BaseXgboostLanguage baseXgboostLanguage = getXgboostLanguage(language);

        // feature name mapping
        JObject featureNameFidMappingObj = modelParam.getJObject("featureNameFidMapping");
        // Tree data preprocessing list
        List<Map<String, Node>> treeMapList = treesPreHandle(modelParam.getJSONArray("trees"), featureNameFidMappingObj);
        int treeDim = modelParam.getInteger("treeDim");
        int numClasses = modelParam.getInteger("numClasses");
        // Initialization score
        JSONArray initScoreArray = modelParam.getJSONArray("initScore");
        String initScore = (null != initScoreArray && initScoreArray.size() > 0) ? initScoreArray.getString(0) : "0";

        return baseXgboostLanguage.buildWholeCode(treeMapList, treeDim, numClasses, initScore, featureNameFidMappingObj);
    }


    /**
     * Tree list preprocessing
     *
     * @param trees List of all trees
     * @parm featureNameFidMappingObj Parameter name mapping
     * @return List of all trees; Map structure description: key: node ID, value: node information
     */
    private List<Map<String, Node>> treesPreHandle(JSONArray trees, JObject featureNameFidMappingObj) {
        List<Map<String, Node>> treeMapList = new ArrayList<>();
        for (int i = 0; i < trees.size(); i++) {
            JSONArray tree = trees.getJSONObject(i).getJSONArray("tree");
            Map<String, Node> treeMap = new HashMap<>(16);
            Node node = null;
            for (int index = 0; index < tree.size(); index++) {
                node = new Node();
                JObject obj = JObject.create(tree.getJSONObject(index));
                String id = obj.getString("id");
                node.setId(StringUtil.isEmpty(id) ? "0" : id);
                node.setSitename(obj.getString("sitename"));
                node.setFid(obj.getString("fid"));
                node.setFidName(featureNameFidMappingObj.getString(node.getFid()));
                node.setBid(obj.getString("bid"));
                node.setWeight(obj.getString("weight"));
                node.setLeftNodeId(obj.getString("leftNodeid"));
                node.setRightNodeId(obj.getString("rightNodeid"));
                node.setMissingDir(obj.getString("missingDir"));
                node.setLeaf(obj.getBooleanValue("isLeaf"));

                treeMap.put(node.getId(), node);
            }
            // Set the height of each node of the tree
            setTreeLayer(treeMap);
            treeMapList.add(treeMap);
        }

        return treeMapList;
    }

    /**
     * Set the height of the node name tree (the root node is 1)
     */
    private Node setTreeLayer(Map<String, Node> treeMap) {
        // get root node
        Node root = treeMap.get("0");
        root.setLayer(1);
        List<Node> list = new ArrayList<>();
        list.add(root);

        Node node = null;
        while ((node = getNonLeaf(list)) != null) {
            // Non leaf node
            Node leftNode = treeMap.get(node.getLeftNodeId());
            Node rightNode = treeMap.get(node.getRightNodeId());

            leftNode.setLayer(node.getLayer() + 1);
            rightNode.setLayer(node.getLayer() + 1);

            node.setLeftNode(leftNode);
            node.setRigthNode(rightNode);

            leftNode.setParentNode(node);
            rightNode.setParentNode(node);

            list.add(leftNode);
            list.add(rightNode);
        }

        return root;
    }

    /**
     * Get non leaf node
     */
    private Node getNonLeaf(List<Node> nodeList) {
        for (Node node : nodeList) {
            if (null != node && !node.isLeaf()) {
                Node leftNode = node.getLeftNode();
                Node rightNode = node.getRigthNode();

                if (null == leftNode && null == rightNode) {
                    return node;
                }

            }
        }
        return null;
    }


    /**
     * Get the corresponding language interpreter
     */
    private BaseXgboostLanguage getXgboostLanguage(String language) {
        return new XgboostLanguageSelector(language).getSelector();
    }
}
