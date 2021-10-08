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

package com.welab.wefe.board.service.service.modelexport;

import com.welab.wefe.common.util.JObject;
import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Basic classes of xgboost language
 *
 * @author aaron.li
 **/
public class BaseXgboostLanguage {
    /**
     * Root node key value
     */
    public static final String ROOT_NODE_KEY = "0";
    /**
     * Method body placeholder
     */
    public static final String METHOD_BODY_PLACEHOLDER = "#body#";

    public static final String SPECIAL_NUMERIC = "#";
    /**
     * Maximum number of cycles
     */
    public static final int MAX_LOOP_COUNT = 1000;

    /**
     * Code indent unit character
     */
    public static final String INDENTATION_UNIT_CHAR = "    ";
    /**
     * Second classification
     */
    private static final int NUM_CLASSES_2_CLASSIFICATIONS = 2;

    /**
     * Generate complete code
     *
     * @param treeMapList One dimensional array structure of tree
     */
    public String buildWholeCode(List<Map<String, Node>> treeMapList, int treeDim, int numClasses, String initScore, JObject featureNameFidMappingObj) {
        // Preprocessing (that is, the code corresponding to each node of the spanning tree)
        preGenerateTreeNodeCode(treeMapList);

        // Number of trees
        int treeNum = treeMapList.size();

        // Pre generated method signature code
        String preMethodSignNameCode = null;
        // Second classification
        if (numClasses <= NUM_CLASSES_2_CLASSIFICATIONS) {
            // Method pre signature code
            preMethodSignNameCode = preBuild2ClassificationsMethodSignNameCode(treeNum, initScore);
        } else {
            // Multi classification
            // Classification quantity（Because the method signature of Haskell language is generated according to the result classification, only rewritable method signature methods can be defined）
            int treeMultipleClassificationsNum = treeMultipleClassificationsModMap(treeNum, treeDim).size();
            // Subclasses do not override this method, which is the same as that of the second category by default
            preMethodSignNameCode = preBuildMultipleClassificationsMethodSignNameCode(treeNum, treeMultipleClassificationsNum, initScore);
        }

        // Method body code
        String methodBodyCode = buildMethodBodyCode(treeMapList, treeDim, numClasses, initScore);
        // Method complete code
        return preMethodSignNameCode.replace(METHOD_BODY_PLACEHOLDER, methodBodyCode);
    }


    /**
     * Pre generated binary classification method signature code
     *
     * @param treeNum tree number
     */
    protected String preBuild2ClassificationsMethodSignNameCode(int treeNum, String initScore) {
        StringBuilder preMethodCode = new StringBuilder();
        preMethodCode.append("public class Model {")
                .append("\n")
                .append(INDENTATION_UNIT_CHAR)
                .append("public static double[] score(double[] input) {")
                .append("\n")
                .append(METHOD_BODY_PLACEHOLDER)
                .append("\n")
                .append(indentationByNodeLayer(1, false) + "}")
                .append("\n")
                .append("}");
        return preMethodCode.toString();
    }

    /**
     * Pre generated multi classification method signature code
     *
     * @param treeNum            tree number
     * @param classificationsNum classifications number
     * @param initScore          init score
     */
    protected String preBuildMultipleClassificationsMethodSignNameCode(int treeNum, int classificationsNum, String initScore) {
        return preBuild2ClassificationsMethodSignNameCode(treeNum, initScore);
    }

    /**
     * Generate method body code
     *
     * @param preTreeMapList Preprocessing list of all trees
     */
    protected String buildMethodBodyCode(List<Map<String, Node>> preTreeMapList, int treeDim, int numClasses, String initScore) {
        StringBuilder bodyCode = new StringBuilder();
        if (CollectionUtils.isEmpty(preTreeMapList)) {
            return bodyCode.toString();
        }
        for (int i = 0; i < preTreeMapList.size(); i++) {
            bodyCode.append(buildTreeCode(preTreeMapList.get(i), i));
            bodyCode.append("\n");
        }
        bodyCode.append(buildMethodResultLogicCode(preTreeMapList.size(), treeDim, numClasses, initScore));
        return bodyCode.toString();
    }

    /**
     * Generate the result logical code of the return value of the method
     *
     * @param treeNum tree number
     */
    protected String buildMethodResultLogicCode(int treeNum, int treeDim, int numClasses, String initScore) {
        StringBuilder methodSummaryCode = new StringBuilder();

        // Second classification
        if (numClasses <= NUM_CLASSES_2_CLASSIFICATIONS) {
            methodSummaryCode.append(build2ClassificationsResultLogicCode(treeNum, initScore));
        } else {
            // Multi classification
            methodSummaryCode.append(buildMultipleClassificationsResultLogicCode(treeNum, treeDim, initScore));
        }

        return methodSummaryCode.toString();
    }

    /**
     * Generate the result logic code of the second classification
     */
    protected String build2ClassificationsResultLogicCode(int treeNum, String initScore) {
        StringBuilder methodCalcCode = new StringBuilder();
        String summaryVar = "s1";
        methodCalcCode.append(indentationByNodeLayer(1, true));
        methodCalcCode.append(generateVarDef(summaryVar))
                .append("\n")
                .append(indentationByNodeLayer(1, true))
                .append(summaryVar)
                .append(" = 1 / (1 + Math.exp(0 - (")
                .append(generateTreeSum(treeNum, initScore))
                .append(")))")
                .append(lineEndSymbol())
                .append("\n")
                .append(build2ClassificationsReturnCode(summaryVar, initScore));

        return methodCalcCode.toString();
    }

    /**
     * Generate secondary classification return result code
     *
     * @param varName Variable name of classification
     */
    protected String build2ClassificationsReturnCode(String varName, String initScore) {
        StringBuilder methodResultCode = new StringBuilder();
        methodResultCode.append(indentationByNodeLayer(1, true))
                .append("return new double[] {1 - " + varName + ", " + varName + "}" + lineEndSymbol());

        return methodResultCode.toString();
    }


    /**
     * Generate multi classification result logic code
     */
    protected String buildMultipleClassificationsResultLogicCode(int treeNum, int treeDim, String initScore) {
        StringBuilder methodCalcCode = new StringBuilder();
        // Tree result variable classification map (key: module; value: variable name of each tree)
        Map<Integer, List<String>> treeClassificationMap = treeMultipleClassificationsModMap(treeNum, treeDim);

        // Generate classified variables and calculation logic
        int index = 0;
        for (Map.Entry<Integer, List<String>> entry : treeClassificationMap.entrySet()) {
            String rVarName = generateResultVarName(index);
            String rVarNameDef = generateVarDef(generateResultVarName(index));
            methodCalcCode.append(resultIndentationNum(2))
                    .append(rVarNameDef)
                    .append("\n")
                    .append(resultIndentationNum(2))
                    .append(rVarName)
                    .append(" = 1 / (1 + ")
                    .append(buildExpFunction(entry.getValue(), initScore))
                    .append(")")
                    .append(lineEndSymbol())
                    .append("\n");
            index++;
        }

        // Generate and return the results of each classification
        methodCalcCode.append(buildMultipleClassificationsReturnCode(treeClassificationMap.size()));
        return methodCalcCode.toString();
    }


    /**
     * Modular regression map of tree multi classification
     */
    protected Map<Integer, List<String>> treeMultipleClassificationsModMap(int treeNum, int treeDim) {
        // Tree result variable classification map (key: tree index module, value: result variable of each tree)
        Map<Integer, List<String>> treeClassificationMap = new LinkedHashMap<>(16);
        for (int i = 1; i <= treeNum; i++) {
            int mod = i % treeDim;
            List<String> treeClassificationList = treeClassificationMap.get(mod);
            treeClassificationList = CollectionUtils.isEmpty(treeClassificationList) ? new ArrayList<>() : treeClassificationList;
            treeClassificationList.add(generateVarName(i - 1));
            treeClassificationMap.put(mod, treeClassificationList);
        }

        return treeClassificationMap;
    }


    /**
     * Generate the sum of all trees
     *
     * @param treeNum   tree number
     * @param initScore init score
     */
    protected String generateTreeSum(int treeNum, String initScore) {
        List<String> treeVarNameList = buildTreeVarNameList(treeNum);
        return generateTreeSum(treeVarNameList, initScore);
    }


    /**
     * Generate the sum of all trees
     *
     * @param treeVarNameList List of variable names for all trees
     * @param initScore       init score
     */
    protected String generateTreeSum(List<String> treeVarNameList, String initScore) {
        StringBuilder sumSb = new StringBuilder();
        sumSb.append("(" + initScore + ")");
        if (CollectionUtils.isNotEmpty(treeVarNameList)) {
            sumSb.append(" + ");
            for (int i = 0; i < treeVarNameList.size(); i++) {
                sumSb.append("(")
                        .append(treeVarNameList.get(i))
                        .append(")");
                if (i < treeVarNameList.size() - 1) {
                    sumSb.append(" + ");
                }
            }
        }

        return sumSb.toString();
    }


    /**
     * Calculation of double power function generating Euler number e
     */
    protected String buildExpFunction(List<String> treeVarNameList, String initScore) {
        StringBuilder code = new StringBuilder();
        code.append("Math.exp(0 - (")
                .append(generateTreeSum(treeVarNameList, initScore))
                .append("))");

        return code.toString();
    }


    /**
     * Generate multi category return statements
     *
     * @param classificationsNum classifications number
     */
    protected String buildMultipleClassificationsReturnCode(int classificationsNum) {
        StringBuilder code = new StringBuilder();
        code.append(resultIndentationNum(2))
                .append("return new double[] {");
        code.append(generateCodeByClassificationsNum(classificationsNum));
        code.append("}").append(lineEndSymbol());
        return code.toString();
    }

    protected String generateCodeByClassificationsNum(int classificationsNum) {
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < classificationsNum; i++) {
            code.append(generateResultVarName(i))
                    .append(" / (")
                    // Generate the sum of categories
                    .append(generateTreeClassificationsResultSum(classificationsNum))
                    .append(")");
            if (i < classificationsNum - 1) {
                code.append(", ");
            }
        }
        return code.toString();
    }


    protected String generateTreeClassificationsResultSum(int classificationsNum) {
        StringBuilder sumCode = new StringBuilder();
        for (int i = 0; i < classificationsNum; i++) {
            sumCode.append(generateResultVarName(i));
            if (i < classificationsNum - 1) {
                sumCode.append(" + ");
            }
        }

        return sumCode.toString();
    }


    /**
     * Pre generated node code
     *
     * @param node      node
     * @param treeIndex The tree index of the tree to which the node belongs
     * @return Node pre code
     */
    protected String preGenerateNodeCode(Node node, int treeIndex) {
        StringBuilder codeSb = new StringBuilder();
        codeSb.append(indentationByNodeLayer(node, true));
        // Leaf node
        if (node.isLeaf()) {
            codeSb.append(generateVarName(treeIndex)).append(" = ").append(node.getWeight()).append(lineEndSymbol());
        } else {
            codeSb.append("if ((").append(generateCompareVarName(node.getFid())).append(") " + greaterThanSymbol() + " (").append(node.getBid()).append(")) {")
                    .append("\n")
                    .append(generateIdPlaceholder(node.getRightNodeId()))
                    .append("\n")
                    .append(indentationByNodeLayer(node, true))
                    .append(" } else {")
                    .append("\n")
                    .append(generateIdPlaceholder(node.getLeftNodeId()))
                    .append("\n")
                    .append(indentationByNodeLayer(node, true))
                    .append("}");
        }

        return codeSb.toString();

    }


    /**
     * Code to build the tree
     *
     * @param treeMap   tree node Map
     * @param treeIndex Tree index, starting at 0
     * @return tree code
     */
    private String buildTreeCode(Map<String, Node> treeMap, int treeIndex) {
        // root node
        Node rootNode = treeMap.get(ROOT_NODE_KEY);
        // code
        String code = indentationByNodeLayer(rootNode, true) + generateVarDef(generateVarName(treeIndex)) + "\n" + rootNode.getCode();
        // Current number of cycles
        int currentIndex = 1;
        // Add the maximum number of cycles, as long as it is to avoid dead cycles caused by incorrect tree structure
        while (code.contains(SPECIAL_NUMERIC) && currentIndex <= MAX_LOOP_COUNT) {
            // Intercept the variable ID
            List<String> ids = findReplaceIds(code);
            if (CollectionUtils.isNotEmpty(ids)) {
                for (String id : ids) {
                    Node subNode = treeMap.get(id.replace(SPECIAL_NUMERIC, ""));
                    code = code.replace(id, subNode.getCode());
                }
            }
            currentIndex++;
        }
        return code;
    }


    /**
     * Pre generated tree node code (that is, the code corresponding to each node of the tree)
     *
     * @param treeMapList One dimensional array of trees
     */
    private void preGenerateTreeNodeCode(List<Map<String, Node>> treeMapList) {
        for (int i = 0; i < treeMapList.size(); i++) {
            Map<String, Node> treeMap = treeMapList.get(i);
            for (Map.Entry<String, Node> entry : treeMap.entrySet()) {
                Node node = entry.getValue();
                node.setCode(preGenerateNodeCode(node, i));
            }
        }
    }


    /**
     * Extract the ID to replace from the string
     */
    private static List<String> findReplaceIds(String code) {
        List<String> result = new ArrayList<>();
        String regex = "#(\\d+)#";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(code);
        while (matcher.find()) {
            result.add(matcher.group());
        }

        return result;
    }

    /**
     * List of result variable names for the spanning tree
     *
     * @param treeNum tree number
     */
    protected List<String> buildTreeVarNameList(int treeNum) {
        List<String> treeVarNameList = new ArrayList<>();
        for (int i = 0; i < treeNum; i++) {
            treeVarNameList.add(generateVarName(i));
        }

        return treeVarNameList;
    }


    /**
     * Generate indented space characters based on node hierarchy
     *
     * @param node            node
     * @param initIndentation Need initial indent
     */
    protected String indentationByNodeLayer(Node node, boolean initIndentation) {
        int layer = node.getLayer();
        StringBuilder blankCharSb = new StringBuilder(initIndentation ? INDENTATION_UNIT_CHAR : "");
        for (int i = 1; i <= layer; i++) {
            blankCharSb.append(INDENTATION_UNIT_CHAR);
        }

        return blankCharSb.toString();
    }

    protected String indentationByNodeLayer(int layer, boolean initIndentation) {
        StringBuilder blankCharSb = new StringBuilder(initIndentation ? INDENTATION_UNIT_CHAR : "");
        for (int i = 1; i <= layer; i++) {
            blankCharSb.append(INDENTATION_UNIT_CHAR);
        }

        return blankCharSb.toString();
    }

    /**
     * Number of indent tab keys for multi category results
     *
     * @param num indented Number
     */
    protected String resultIndentationNum(int num) {
        return indentationNum(num);
    }


    protected String indentationNum(int num) {
        StringBuilder indentation = new StringBuilder();
        for (int i = 0; i < num; i++) {
            indentation.append(INDENTATION_UNIT_CHAR);
        }
        return indentation.toString();
    }


    /**
     * Line end symbol
     */
    protected String lineEndSymbol() {
        return ";";
    }

    /**
     * Generate greater than sign
     */
    protected String greaterThanSymbol() {
        return ">";
    }


    /**
     * Generate node ID placeholder
     *
     * @param nodeId node id
     */
    protected String generateIdPlaceholder(String nodeId) {
        return "#" + nodeId + "#";
    }

    /**
     * generate var name
     *
     * @param varSerialNo var serialNo
     */
    protected String generateVarName(int varSerialNo) {
        return "var" + varSerialNo;
    }

    /**
     * generate result var name
     *
     * @param varSerialNo var serial no
     */
    protected String generateResultVarName(int varSerialNo) {
        return "s" + varSerialNo;
    }

    /**
     * generate var def
     *
     * @param varName var name
     */
    protected String generateVarDef(String varName) {
        return "double " + varName + lineEndSymbol();
    }

    /**
     * generate compare var name
     *
     * @param index index
     */
    protected String generateCompareVarName(String index) {
        return "input[" + index + "]";
    }


}
