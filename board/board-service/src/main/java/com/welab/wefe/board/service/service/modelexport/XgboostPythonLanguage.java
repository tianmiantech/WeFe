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

import java.util.List;

/**
 * Python
 *
 * @author aaron.li
 **/
public class XgboostPythonLanguage extends BaseXgboostLanguage {

    @Override
    protected String preBuild2ClassificationsMethodSignNameCode(int treeNum, String initScore) {
        StringBuilder preMethodCode = new StringBuilder();
        preMethodCode.append("import math")
                .append("\n")
                .append("def score(input):")
                .append("\n")
                .append(METHOD_BODY_PLACEHOLDER)
                .append("\n");
        return preMethodCode.toString();
    }

    @Override
    protected String preGenerateNodeCode(Node node, int treeIndex) {
        StringBuilder codeSb = new StringBuilder();
        codeSb.append(indentationByNodeLayer(node, false));
        if (node.isLeaf()) {
            codeSb.append(generateVarName(treeIndex)).append(" = ").append(node.getWeight());
        } else {
            codeSb.append("if (").append(generateCompareVarName(node.getFid())).append(") " + greaterThanSymbol() + " (").append(node.getBid()).append("):")
                    .append("\n")
                    .append(generateIdPlaceholder(node.getRightNodeId()))
                    .append("\n")
                    .append(indentationByNodeLayer(node, false))
                    .append("else:")
                    .append("\n")
                    .append(generateIdPlaceholder(node.getLeftNodeId()));

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
                .append(" = 1 / (1 + math.exp(0 - (")
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
                .append("return [(1) - (" + varName + "), " + varName + "]" + lineEndSymbol());

        return methodResultCode.toString();
    }

    @Override
    protected String buildExpFunction(List<String> treeVarNameList, String initScore) {
        StringBuilder code = new StringBuilder();
        code.append("math.exp(0 - (")
                .append(generateTreeSum(treeVarNameList, initScore))
                .append("))");

        return code.toString();
    }

    @Override
    protected String buildMultipleClassificationsReturnCode(int classificationsNum) {
        StringBuilder code = new StringBuilder();
        code.append(resultIndentationNum(2))
                .append("return [");
        code.append(generateCodeByClassificationsNum(classificationsNum));
        code.append("]").append(lineEndSymbol());
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
        return varName + " = 0.0";
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

