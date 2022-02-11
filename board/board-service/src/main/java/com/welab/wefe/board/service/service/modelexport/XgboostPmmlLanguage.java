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

import com.welab.wefe.common.util.JObject;
import org.dmg.pmml.*;
import org.dmg.pmml.mining.MiningModel;
import org.dmg.pmml.mining.Segment;
import org.dmg.pmml.mining.Segmentation;
import org.dmg.pmml.regression.NumericPredictor;
import org.dmg.pmml.regression.RegressionModel;
import org.dmg.pmml.regression.RegressionTable;
import org.dmg.pmml.tree.TreeModel;
import org.jpmml.model.PMMLUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * PMML
 * <p>
 * TODO At present, it only supports the export of secondary classification, and multi classification has not been implemented
 *
 * @author aaron.li
 **/
public class XgboostPmmlLanguage extends BaseXgboostLanguage {
    protected final Logger LOG = LoggerFactory.getLogger(this.getClass());

    @Override
    public String buildWholeCode(List<Map<String, Node>> treeMapList, int treeDim, int numClasses, String initScore, JObject featureNameFidMappingObj) {
        PMML pmml = new PMML();
        pmml.setVersion("4.3");
        pmml.setHeader(buildHeader());
        pmml.setMiningBuildTask(buildMiningBuildTask());
        pmml.setDataDictionary(buildDataDictionary(featureNameFidMappingObj));
        pmml.addModels(buildTopMiningModel(treeMapList, featureNameFidMappingObj, initScore));
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            PMMLUtil.marshal(pmml, outputStream);
            return outputStream.toString();
        } catch (Exception e) {
            LOG.error("Model export xgboost generates PMML language exception：", e);
        }
        return "";
    }


    /**
     * Build header node
     */
    private Header buildHeader() {
        Header header = new Header();
        header.setApplication(new Application("Xgboost Regression Model")
                .setVersion("1.0"))
                .setTimestamp(new Timestamp());
        return header;
    }


    /**
     * Build mining task node
     */
    private MiningBuildTask buildMiningBuildTask() {
        MiningBuildTask miningBuildTask = new MiningBuildTask();

        return miningBuildTask;
    }


    /**
     * Build in reference dictionary
     */
    private DataDictionary buildDataDictionary(JObject featureNameFidMappingObj) {
        DataDictionary dataDictionary = new DataDictionary();
        dataDictionary.addDataFields(new DataField()
                .setName(new FieldName("flag"))
                .setOpType(OpType.CATEGORICAL)
                .setDataType(DataType.INTEGER)
                .addValues(new Value("0"), new Value("1")));

        for (Map.Entry<String, Object> entity : featureNameFidMappingObj.getInnerMap().entrySet()) {
            String value = String.valueOf(entity.getValue());
            dataDictionary.addDataFields(new DataField()
                    .setName(new FieldName(value))
                    .setOpType(OpType.CONTINUOUS)
                    .setDataType(DataType.DOUBLE));
        }

        return dataDictionary;
    }


    /**
     * Build the top-level mining model
     */
    private MiningModel buildTopMiningModel(List<Map<String, Node>> treeMapList, JObject featureNameFidMappingObj, String initScore) {
        MiningModel miningModel = new MiningModel();
        miningModel.setMiningFunction(MiningFunction.CLASSIFICATION)
                .setAlgorithmName("XGBoost (GBTree)")
                .setMathContext(MathContext.DOUBLE);

        MiningSchema miningSchema = new MiningSchema();
        miningSchema.addMiningFields(new MiningField()
                .setName(new FieldName("flag"))
                .setUsageType(MiningField.UsageType.TARGET));
        for (Map.Entry<String, Object> entity : featureNameFidMappingObj.getInnerMap().entrySet()) {
            miningSchema.addMiningFields(new MiningField()
                    .setName(new FieldName(String.valueOf(entity.getValue()))));
        }

        miningModel.setMiningSchema(miningSchema)
                .setSegmentation(buildTopSegmentation(treeMapList, featureNameFidMappingObj, initScore));

        return miningModel;
    }


    /**
     * Build the top-level segment structure
     *
     * @param treeMapList              All tree information
     * @param featureNameFidMappingObj feature name fid mapping
     */
    private Segmentation buildTopSegmentation(List<Map<String, Node>> treeMapList, JObject featureNameFidMappingObj, String initScore) {
        Segmentation segmentation = new Segmentation();
        segmentation.setMultipleModelMethod(Segmentation.MultipleModelMethod.MODEL_CHAIN)
                .addSegments(buildTreeLogicSegment(treeMapList, featureNameFidMappingObj, initScore))
                .addSegments(buildClassificationSegmen());

        return segmentation;
    }


    /**
     * Construct tree logical segment structure
     *
     * @param treeMapList              All tree information
     * @param featureNameFidMappingObj feature name fid mapping
     */
    private Segment buildTreeLogicSegment(List<Map<String, Node>> treeMapList, JObject featureNameFidMappingObj, String initScore) {
        Segment segment = new Segment();
        segment.setId("1")
                .setPredicate(new True())
                .setModel(buildMiningModel(treeMapList, featureNameFidMappingObj, initScore));
        return segment;
    }


    /**
     * Construction of classification segment structure
     */
    private Segment buildClassificationSegmen() {
        Segment segment = new Segment();
        segment.setPredicate(new True())
                .setId("2");

        RegressionModel regressionModel = new RegressionModel();
        regressionModel.setMiningFunction(MiningFunction.CLASSIFICATION)
                .setNormalizationMethod(RegressionModel.NormalizationMethod.LOGIT)
                .setMathContext(MathContext.DOUBLE);

        MiningSchema miningSchema = new MiningSchema()
                .addMiningFields(new MiningField()
                        .setName(new FieldName("flag"))
                        .setUsageType(MiningField.UsageType.TARGET))
                .addMiningFields(new MiningField()
                        .setName(new FieldName("xgbValue")));

        regressionModel.setMiningSchema(miningSchema);

        Output output = new Output()
                .addOutputFields(new OutputField()
                        .setName(new FieldName("probability(0)"))
                        .setOpType(OpType.CONTINUOUS)
                        .setDataType(DataType.DOUBLE)
                        .setResultFeature(ResultFeature.PROBABILITY)
                        .setValue("0"))
                .addOutputFields(new OutputField()
                        .setName(new FieldName("probability(1)"))
                        .setOpType(OpType.CONTINUOUS)
                        .setDataType(DataType.DOUBLE)
                        .setResultFeature(ResultFeature.PROBABILITY)
                        .setValue("1"));

        regressionModel.setOutput(output);

        RegressionTable regressionTable = new RegressionTable()
                .setIntercept(0)
                .setTargetCategory("1")
                .addNumericPredictors(new NumericPredictor()
                        .setName(new FieldName("xgbValue"))
                        .setCoefficient(1.0));


        regressionModel.addRegressionTables(regressionTable)
                .addRegressionTables(new RegressionTable()
                        .setIntercept(0)
                        .setTargetCategory("0"));

        segment.setModel(regressionModel);
        return segment;
    }


    /**
     * Build mining model
     *
     * @param treeMapList              All tree information
     * @param featureNameFidMappingObj feature name fid mapping
     */
    private MiningModel buildMiningModel(List<Map<String, Node>> treeMapList, JObject featureNameFidMappingObj, String initScore) {
        MiningModel miningModel = new MiningModel();
        miningModel.setMiningFunction(MiningFunction.REGRESSION)
                .setMathContext(MathContext.DOUBLE)
                .setMiningSchema(buildMiningSchema(featureNameFidMappingObj))
                .setOutput(buildOutput())
                .setLocalTransformations(buildLocalTransformations(featureNameFidMappingObj))
                .setSegmentation(buildSegmentation(treeMapList, initScore));

        return miningModel;
    }


    /**
     * Build mining parameters
     *
     * @param featureNameFidMappingObj feature name fid mapping
     */
    private MiningSchema buildMiningSchema(JObject featureNameFidMappingObj) {
        MiningSchema miningSchema = new MiningSchema();
        Map<String, Object> featureNameFidMapping = featureNameFidMappingObj.getInnerMap();
        MiningField miningField = null;
        for (Map.Entry<String, Object> entity : featureNameFidMapping.entrySet()) {
            miningField = new MiningField()
                    .setName(new FieldName(String.valueOf(entity.getValue())));

            miningSchema.addMiningFields(miningField);
        }
        return miningSchema;
    }

    /**
     * Output of the build tree
     */
    private Output buildOutput() {
        Output output = new Output();
        output.addOutputFields(new OutputField().setName(new FieldName("xgbValue"))
                .setOpType(OpType.CONTINUOUS)
                .setDataType(DataType.DOUBLE)
                .setFinalResult(false));

        return output;
    }


    /**
     * Build local variable transformation node information
     *
     * @param featureNameFidMappingObj feature name fid mapping
     */
    private LocalTransformations buildLocalTransformations(JObject featureNameFidMappingObj) {
        LocalTransformations localTransformations = new LocalTransformations();
        Map<String, Object> featureNameFidMapping = featureNameFidMappingObj.getInnerMap();
        DerivedField derivedField = null;
        for (Map.Entry<String, Object> entity : featureNameFidMapping.entrySet()) {
            String value = String.valueOf(entity.getValue());
            derivedField = new DerivedField();
            derivedField.setName(new FieldName("double(" + value + ")"))
                    .setOpType(OpType.CONTINUOUS)
                    .setDataType(DataType.DOUBLE)
                    .setExpression(new FieldRef().setField(new FieldName(value)));

            localTransformations.addDerivedFields(derivedField);
        }
        return localTransformations;
    }


    /**
     * Build segmentation node information
     *
     * @param treeMapList All tree information
     */
    private Segmentation buildSegmentation(List<Map<String, Node>> treeMapList, String initScore) {
        Segmentation segmentation = new Segmentation();
        segmentation.setMultipleModelMethod(Segmentation.MultipleModelMethod.SUM);
        segmentation.addSegments(buildInitScoreSegment(initScore));
        for (int i = 0; i < treeMapList.size(); i++) {
            segmentation.addSegments(buildTreeSegment(treeMapList.get(i), i + 1));
        }
        return segmentation;
    }


    /**
     * Build the segment node of the tree
     *
     * @param treeMap   All node information of the tree
     * @param treeIndex tree index
     */
    private Segment buildTreeSegment(Map<String, Node> treeMap, int treeIndex) {
        Segment segment = new Segment();
        segment.setPredicate(new True())
                .setId(treeIndex + "")
                .setModel(buildTreeModel(treeMap));

        return segment;
    }

    /**
     * Build the segment node of the initial score
     *
     * @param initScore init score
     */
    private Segment buildInitScoreSegment(String initScore) {
        Segment segment = new Segment();
        segment.setPredicate(new True())
                .setId("0")
                .setModel(new TreeModel()
                        .setMiningFunction(MiningFunction.REGRESSION)
                        .setNoTrueChildStrategy(TreeModel.NoTrueChildStrategy.RETURN_LAST_PREDICTION)
                        .setMathContext(MathContext.DOUBLE)
                        .setMiningSchema(new MiningSchema())
                        .setNode(new org.dmg.pmml.tree.Node()
                                .setPredicate(new True())
                                .setScore(initScore)));
        return segment;
    }


    /**
     * PMML structure of treemodel of spanning tree
     *
     * @param treeMap All node information of the tree
     */
    private TreeModel buildTreeModel(Map<String, Node> treeMap) {
        // root node
        Node root = treeMap.get("0");
        org.dmg.pmml.tree.Node rootPmmlNode = new org.dmg.pmml.tree.Node();
        rootPmmlNode.setScore("0.0");
        rootPmmlNode.setPredicate(new True());
        buildNode(root, rootPmmlNode);

        TreeModel treeModel = new TreeModel();
        treeModel.setMiningFunction(MiningFunction.REGRESSION)
                .setNoTrueChildStrategy(TreeModel.NoTrueChildStrategy.RETURN_LAST_PREDICTION)
                .setMathContext(MathContext.DOUBLE)
                .setMiningSchema(buildTreeModelMiningSchema(treeMap))
                .setNode(rootPmmlNode);

        return treeModel;
    }


    private MiningSchema buildTreeModelMiningSchema(Map<String, Node> treeMap) {
        MiningSchema miningSchema = new MiningSchema();
        Set<String> fidNameSet = new HashSet<>(16);
        for (Map.Entry<String, Node> entry : treeMap.entrySet()) {
            Node node = entry.getValue();
            if (!node.isLeaf()) {
                fidNameSet.add(node.getFidName());
            }
        }
        for (String fidName : fidNameSet) {
            MiningField miningField = new MiningField();
            miningField.setName(new FieldName("double(" + fidName + ")"));
            miningSchema.addMiningFields(miningField);
        }

        return miningSchema;
    }


    /**
     * XML of node structure of spanning tree
     */
    private void buildNode(Node treeNode, org.dmg.pmml.tree.Node parentPmmlNode) {
        // Leaf node
        if (treeNode.isLeaf()) {
            org.dmg.pmml.tree.Node pmmlNode = new org.dmg.pmml.tree.Node();
            pmmlNode.setScore(treeNode.getWeight());
            pmmlNode.setPredicate(new True());
            parentPmmlNode.addNodes(pmmlNode);
        } else {
            // Generate left node
            org.dmg.pmml.tree.Node leftPmmlNode = new org.dmg.pmml.tree.Node();
            leftPmmlNode.setScore(treeNode.getWeight());
            leftPmmlNode.setPredicate(new SimplePredicate().setField(new FieldName("double(" + treeNode.getFidName() + ")")).setOperator(SimplePredicate.Operator.LESS_OR_EQUAL).setValue(treeNode.getBid()));
            parentPmmlNode.addNodes(leftPmmlNode);

            // Generate right node
            org.dmg.pmml.tree.Node rightPmmlNode = new org.dmg.pmml.tree.Node();
            rightPmmlNode.setScore(treeNode.getWeight());
            rightPmmlNode.setPredicate(new SimplePredicate().setField(new FieldName("double(" + treeNode.getFidName() + ")")).setOperator(SimplePredicate.Operator.GREATER_THAN).setValue(treeNode.getBid()));
            parentPmmlNode.addNodes(rightPmmlNode);


            // Regenerate the XML of tree child nodes
            Node leftTreeNode = treeNode.getLeftNode();
            Node rightTreeNode = treeNode.getRigthNode();

            if (null != leftTreeNode) {
                buildNode(leftTreeNode, leftPmmlNode);
            }
            if (null != rightTreeNode) {
                buildNode(rightTreeNode, rightPmmlNode);
            }
        }
    }

}
