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

package com.welab.wefe.board.service.service.modelexport;

import java.util.List;
import java.util.Map;

/**
 * Ruby
 *
 * @author aaron.li
 **/
public class XgboostRubyLanguage extends XgboostPythonLanguage {

    @Override
    protected String preBuild2ClassificationsMethodSignNameCode(int treeNum, String initScore) {
        StringBuilder preMethodCode = new StringBuilder();
        preMethodCode.append("def score(input)")
                .append("\n")
                .append(METHOD_BODY_PLACEHOLDER)
                .append("\n")
                .append("end");
        return preMethodCode.toString();
    }


    @Override
    protected String preGenerateNodeCode(Node node, int treeIndex) {
        StringBuilder codeSb = new StringBuilder();
        codeSb.append(indentationByNodeLayer(node, false));
        if (node.isLeaf()) {
            codeSb.append(generateVarName(treeIndex)).append(" = ").append(node.getWeight());
        } else {
            codeSb.append("if (").append(generateCompareVarName(node.getFid())).append(") " + greaterThanSymbol() + " (").append(node.getBid()).append(")")
                    .append("\n")
                    .append(generateIdPlaceholder(node.getRightNodeId()))
                    .append("\n")
                    .append(indentationByNodeLayer(node, false))
                    .append("else")
                    .append("\n")
                    .append(generateIdPlaceholder(node.getLeftNodeId()))
                    .append("\n")
                    .append(indentationByNodeLayer(node, false))
                    .append("end");

        }
        return codeSb.toString();
    }

    @Override
    protected String build2ClassificationsResultLogicCode(int treeNum, String initScore) {
        StringBuilder methodCalcCode = new StringBuilder();
        String summaryVar = "s1";
        methodCalcCode.append(indentationByNodeLayer(1, false));
        methodCalcCode.append(generateVarDef(summaryVar))
                .append("\n")
                .append(indentationByNodeLayer(1, false))
                .append(summaryVar)
                .append(" = 1.fdiv(1 + Math.exp(0 - (")
                .append(generateTreeSum(treeNum, initScore))
                .append(")))")
                .append(lineEndSymbol())
                .append("\n")
                .append(build2ClassificationsReturnCode(summaryVar, initScore));

        return methodCalcCode.toString();
    }


    @Override
    protected String build2ClassificationsReturnCode(String varName, String initScore) {
        StringBuilder methodResultCode = new StringBuilder();
        methodResultCode.append(indentationByNodeLayer(1, false))
                .append("[(1) - (" + varName + "), " + varName + "]" + lineEndSymbol());

        return methodResultCode.toString();
    }


    @Override
    protected String buildMultipleClassificationsResultLogicCode(int treeNum, int treeDim, String initScore) {
        StringBuilder methodCalcCode = new StringBuilder();
        Map<Integer, List<String>> treeClassificationMap = treeMultipleClassificationsModMap(treeNum, treeDim);

        // Generate the variable quantity of classification and calculation logic
        int index = 0;
        for (Map.Entry<Integer, List<String>> entry : treeClassificationMap.entrySet()) {
            String rVarName = generateResultVarName(index);
            String rVarNameDef = generateVarDef(generateResultVarName(index));
            methodCalcCode.append(resultIndentationNum(2))
                    .append(rVarNameDef)
                    .append("\n")
                    .append(resultIndentationNum(2))
                    .append(rVarName)
                    .append(" = 1.fdiv(1 + ")
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
        code.append("Math.exp(0 - (")
                .append(generateTreeSum(treeVarNameList, initScore))
                .append("))");

        return code.toString();
    }


    @Override
    protected String buildMultipleClassificationsReturnCode(int classificationsNum) {
        StringBuilder code = new StringBuilder();
        code.append(resultIndentationNum(2))
                .append("[");
        code.append(generateCodeByClassificationsNum(classificationsNum));
        code.append("]").append(lineEndSymbol());
        return code.toString();
    }


    @Override
    protected String lineEndSymbol() {
        return "";
    }
}
