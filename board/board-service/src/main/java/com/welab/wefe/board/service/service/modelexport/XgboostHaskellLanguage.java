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

import java.util.List;
import java.util.Map;

/**
 * Haskell
 *
 * @author aaron.li
 **/
public class XgboostHaskellLanguage extends BaseXgboostLanguage {

    @Override
    protected String preBuild2ClassificationsMethodSignNameCode(int treeNum, String initScore) {
        StringBuilder preMethodCode = new StringBuilder();
        preMethodCode.append("module Model where")
                .append("\n")
                .append("score :: [Double] -> [Double]")
                .append("\n")
                .append("score input =")
                .append("\n")
                .append(INDENTATION_UNIT_CHAR + "[(1) - (s1), s1]")
                .append("\n")
                .append("    where")
                .append("\n")
                .append(METHOD_BODY_PLACEHOLDER);
        return preMethodCode.toString();
    }

    @Override
    protected String preBuildMultipleClassificationsMethodSignNameCode(int treeNum, int classificationsNum, String initScore) {
        StringBuilder preMethodCode = new StringBuilder();
        preMethodCode.append("module Model where")
                .append("\n")
                .append("score :: [Double] -> [Double]")
                .append("\n")
                .append("score input =")
                .append("\n")
                .append(INDENTATION_UNIT_CHAR)
                .append("[");
        for (int i = 0; i < classificationsNum; i++) {
            preMethodCode.append("r" + i);
            if (i < classificationsNum - 1) {
                preMethodCode.append(", ");
            }
        }
        preMethodCode.append("]")
                .append("\n")
                .append("    where")
                .append("\n")
                .append(METHOD_BODY_PLACEHOLDER);
        return preMethodCode.toString();
    }


    @Override
    protected String preGenerateNodeCode(Node node, int treeIndex) {
        StringBuilder codeSb = new StringBuilder();
        Node parentNode = node.getParentNode();
        int baseLayer = (null != parentNode ? (node.getLayer() * 2) : node.getLayer() + 1);
        codeSb.append(indentationByNodeLayer(baseLayer, true));
        if (node.isLeaf()) {
            codeSb.append(node.getWeight()).append(lineEndSymbol());
        } else {
            codeSb.append("if ((").append(generateCompareVarName(node.getFid())).append(") " + greaterThanSymbol() + " (").append(node.getBid()).append("))")
                    .append("\n")
                    .append(indentationByNodeLayer(baseLayer, true))
                    .append(INDENTATION_UNIT_CHAR)
                    .append("then")
                    .append("\n")
                    .append(generateIdPlaceholder(node.getRightNodeId()))
                    .append("\n")
                    .append(indentationByNodeLayer(baseLayer, true))
                    .append(INDENTATION_UNIT_CHAR)
                    .append("else")
                    .append("\n")
                    .append(generateIdPlaceholder(node.getLeftNodeId()));
        }

        return codeSb.toString();

    }

    @Override
    protected String build2ClassificationsResultLogicCode(int treeNum, String initScore) {
        StringBuilder methodCalcCode = new StringBuilder();
        String summaryVar = "s1";
        methodCalcCode.append(indentationByNodeLayer(1, true));
        methodCalcCode.append(generateVarDef(summaryVar))
                .append("\n")
                .append(indentationByNodeLayer(2, true))
                .append("1 / (1 + exp(0 - (")
                .append(generateTreeSum(treeNum, initScore))
                .append(")))")
                .append(lineEndSymbol())
                .append("\n")
                .append(build2ClassificationsReturnCode(summaryVar, initScore));

        return methodCalcCode.toString();
    }


    @Override
    protected String buildMultipleClassificationsResultLogicCode(int treeNum, int treeDim, String initScore) {
        StringBuilder methodCalcCode = new StringBuilder();
        // Tree result variable classification map (key: module; value: variable name of each tree)
        Map<Integer, List<String>> treeClassificationMap = treeMultipleClassificationsModMap(treeNum, treeDim);

        // Generate the variable quantity of classification and calculation logic
        int index = 0;
        for (Map.Entry<Integer, List<String>> entry : treeClassificationMap.entrySet()) {
            String rVarNameDef = generateVarDef(generateResultVarName(index));
            methodCalcCode.append(resultIndentationNum(2))
                    .append(rVarNameDef)
                    .append("\n")
                    .append(resultIndentationNum(3))
                    .append(" 1 / (1 + ")
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

    @Override
    protected String buildExpFunction(List<String> treeVarNameList, String initScore) {
        StringBuilder code = new StringBuilder();
        code.append("exp(0 - (")
                .append(generateTreeSum(treeVarNameList, initScore))
                .append("))");

        return code.toString();
    }

    @Override
    protected String buildMultipleClassificationsReturnCode(int classificationsNum) {
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < classificationsNum; i++) {
            code.append(resultIndentationNum(2))
                    .append("r" + i + " = ")
                    .append("\n")
                    .append(resultIndentationNum(3))
                    .append(generateResultVarName(i) + " / (" + generateTreeClassificationsResultSum(classificationsNum) + ")")
                    .append("\n");
        }


        return code.toString();
    }


    @Override
    protected String build2ClassificationsReturnCode(String varName, String initScore) {
        return "";
    }


    @Override
    protected String generateCompareVarName(String index) {
        return "(input) !! (" + index + ")";
    }

    @Override
    protected String generateVarName(int treeIndex) {
        return "func" + treeIndex;
    }

    @Override
    protected String generateVarDef(String varName) {
        return varName + " =";
    }

    @Override
    protected String lineEndSymbol() {
        return "";
    }
}
