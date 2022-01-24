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

/**
 * Visual Basic
 *
 * @author aaron.li
 **/
public class XgboostVisualBasicLanguage extends XgboostPythonLanguage {

    @Override
    protected String preBuild2ClassificationsMethodSignNameCode(int treeNum, String initScore) {
        StringBuilder preMethodCode = new StringBuilder();
        preMethodCode.append("Module Model")
                .append("\n")
                .append("Function Score(ByRef inputVector() As Double) As Double()")
                .append("\n")
                .append(METHOD_BODY_PLACEHOLDER)
                .append("\n")
                .append("End Function")
                .append("\n")
                .append("End Module");
        return preMethodCode.toString();
    }

    @Override
    protected String preGenerateNodeCode(Node node, int treeIndex) {
        StringBuilder codeSb = new StringBuilder();
        codeSb.append(indentationByNodeLayer(node, false));
        if (node.isLeaf()) {
            codeSb.append(generateVarName(treeIndex)).append(" = ").append(node.getWeight());
        } else {
            codeSb.append("If (").append(generateCompareVarName(node.getFid())).append(") " + greaterThanSymbol() + " (").append(node.getBid()).append(") Then")
                    .append("\n")
                    .append(generateIdPlaceholder(node.getRightNodeId()))
                    .append("\n")
                    .append(indentationByNodeLayer(node, false))
                    .append("Else")
                    .append("\n")
                    .append(generateIdPlaceholder(node.getLeftNodeId()))
                    .append("\n")
                    .append(indentationByNodeLayer(node, false))
                    .append("End If");

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
                .append(" = 1 / (1 + Math.Exp(0 - (")
                .append(generateTreeSum(treeNum, initScore));

        String summaryVar2 = "s2";
        methodCalcCode.append(")))")
                .append(lineEndSymbol())
                .append("\n")
                .append(indentationByNodeLayer(1, false))
                .append("Dim " + summaryVar2 + "(1) As Double")
                .append("\n")
                .append(indentationByNodeLayer(1, false))
                .append(summaryVar2 + "(0) = (1) - (" + summaryVar + ")")
                .append("\n")
                .append(indentationByNodeLayer(1, false))
                .append(summaryVar2 + "(1) = ")
                .append(summaryVar)
                .append("\n")
                .append(indentationByNodeLayer(1, false))
                .append("Score = ")
                .append(summaryVar2);

        return methodCalcCode.toString();
    }

    @Override
    protected String buildExpFunction(List<String> treeVarNameList, String initScore) {
        StringBuilder code = new StringBuilder();
        code.append("Math.Exp(0 - (")
                .append(generateTreeSum(treeVarNameList, initScore))
                .append("))");

        return code.toString();
    }


    @Override
    protected String buildMultipleClassificationsReturnCode(int classificationsNum) {
        StringBuilder code = new StringBuilder();
        code.append(resultIndentationNum(1))
                .append(generateVarDef(generateResultVarName(classificationsNum) + "(" + (classificationsNum - 1) + ")"))
                .append("\n");

        for (int i = 0; i < classificationsNum; i++) {
            code.append(resultIndentationNum(1))
                    .append(generateResultVarName(classificationsNum) + "(" + i + ") = " + generateResultVarName(i) + " / (" + generateTreeClassificationsResultSum(classificationsNum) + ")")
                    .append("\n");
        }
        code.append(resultIndentationNum(1))
                .append("Score = " + generateResultVarName(classificationsNum));
        return code.toString();
    }


    @Override
    protected String generateCompareVarName(String index) {
        return "inputVector(" + index + ")";
    }


    @Override
    protected String generateVarDef(String varName) {
        return "Dim " + varName + " As Double";
    }

    @Override
    protected String lineEndSymbol() {
        return "";
    }
}
