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
 * R
 *
 * @author aaron.li
 **/
public class XgboostRLanguage extends BaseXgboostLanguage {

    @Override
    protected String preBuild2ClassificationsMethodSignNameCode(int treeNum, String initScore) {
        StringBuilder preMethodCode = new StringBuilder();
        preMethodCode.append("score <- function(input) {")
                .append("\n")
                .append(INDENTATION_UNIT_CHAR)
                .append("s1 <- (1) / ((1) + (exp((0) - (subroutine(input)))))")
                .append("\n")
                .append(INDENTATION_UNIT_CHAR)
                .append("return(c((1) - (s1), s1))")
                .append("\n")
                .append("}")
                .append("\n")
                .append("subroutine <- function(input) {")
                .append("\n")
                .append(METHOD_BODY_PLACEHOLDER)
                .append("\n")
                .append("}");
        return preMethodCode.toString();
    }

    @Override
    protected String preBuildMultipleClassificationsMethodSignNameCode(int treeNum, int classificationsNum, String initScore) {
        StringBuilder preMethodCode = new StringBuilder();
        preMethodCode.append("score <- function(input) {")
                .append("\n")
                .append(INDENTATION_UNIT_CHAR)
                .append("return(subroutine(input))")
                .append("\n")
                .append("}")
                .append("\n")
                .append("subroutine <- function(input) {")
                .append("\n")
                .append(METHOD_BODY_PLACEHOLDER)
                .append("\n")
                .append("}");
        return preMethodCode.toString();
    }


    @Override
    protected String preGenerateNodeCode(Node node, int treeIndex) {
        StringBuilder codeSb = new StringBuilder();
        codeSb.append(indentationByNodeLayer(node, false));
        if (node.isLeaf()) {
            codeSb.append(generateVarName(treeIndex)).append(" <- ").append(node.getWeight()).append(lineEndSymbol());
        } else {
            codeSb.append("if ((").append(generateCompareVarName(node.getFid())).append(") " + greaterThanSymbol() + " (").append(node.getBid()).append(")) {")
                    .append("\n")
                    .append(generateIdPlaceholder(node.getRightNodeId()))
                    .append("\n")
                    .append(indentationByNodeLayer(node, false))
                    .append(" } else {")
                    .append("\n")
                    .append(generateIdPlaceholder(node.getLeftNodeId()))
                    .append("\n")
                    .append(indentationByNodeLayer(node, false))
                    .append("}");
        }

        return codeSb.toString();

    }

    @Override
    protected String build2ClassificationsResultLogicCode(int treeNum, String initScore) {
        StringBuilder methodCalcCode = new StringBuilder();
        methodCalcCode.append(INDENTATION_UNIT_CHAR)
                .append("return(")
                .append(generateTreeSum(treeNum, initScore))
                .append(")")
                .append(lineEndSymbol());

        return methodCalcCode.toString();
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
                    .append(" <- 1 / (1 + ")
                    .append(buildExpFunction(entry.getValue(), initScore))
                    .append(")")
                    .append(lineEndSymbol())
                    .append("\n");
            index++;
        }

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
        code.append(resultIndentationNum(2))
                .append("return c(");
        code.append(generateCodeByClassificationsNum(classificationsNum));
        code.append(")").append(lineEndSymbol());
        return code.toString();
    }


    @Override
    protected String indentationByNodeLayer(Node node, boolean initIndentation) {
        return super.indentationByNodeLayer(node, false);
    }

    @Override
    protected String indentationByNodeLayer(int layer, boolean initIndentation) {
        return super.indentationByNodeLayer(layer, false);
    }


    @Override
    protected String generateVarDef(String varName) {
        return varName + " <- 0.0" + lineEndSymbol();
    }


    @Override
    protected String lineEndSymbol() {
        return "";
    }

    @Override
    protected String resultIndentationNum(int num) {
        return super.resultIndentationNum(1);
    }
}
