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

import org.apache.commons.lang3.math.NumberUtils;
import org.dmg.pmml.*;
import org.dmg.pmml.regression.NumericPredictor;
import org.dmg.pmml.regression.RegressionModel;
import org.dmg.pmml.regression.RegressionTable;
import org.jpmml.model.PMMLUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * PMML language
 *
 * @author aaron.li
 **/
public class LogisticRegressionPmmlLanguage extends BaseLogisticRegressionLanguage {
    protected final Logger LOG = LoggerFactory.getLogger(this.getClass());

    @Override
    public String generateMethodCode(List<String> headers, Map<String, String> weightMap, String intercept) {
        PMML pmml = new PMML();
        pmml.setVersion("4.3");
        pmml.setHeader(buildHeader());
        pmml.setDataDictionary(buildDataDictionary(headers));
        pmml.addModels(buildRegressionModel(headers, weightMap, intercept));
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            PMMLUtil.marshal(pmml, outputStream);
            return outputStream.toString();
        } catch (Exception e) {
            LOG.error("Model export logistic regression generates PMML language exception：", e);
        }
        return "";
    }


    /**
     * Header information
     */
    private Header buildHeader() {
        Header header = new Header();
        Application application = new Application("Logistic Regression Model");
        application.setVersion("1.0");
        header.setApplication(application);
        header.setTimestamp(new Timestamp());
        return header;
    }

    /**
     * Dictionary input parameter information
     */
    private DataDictionary buildDataDictionary(List<String> headers) {
        DataDictionary dataDictionary = new DataDictionary();
        List<DataField> dataFields = new ArrayList<>();
        // Initialize Y parameter
        dataFields.add(new DataField()
                .setName(new FieldName("y"))
                .setOpType(OpType.CONTINUOUS)
                .setDataType(DataType.DOUBLE));
        // Other input parameters
        for (String param : headers) {
            dataFields.add(new DataField()
                    .setName(new FieldName(param))
                    .setOpType(OpType.CONTINUOUS)
                    .setDataType(DataType.DOUBLE));
        }

        dataDictionary.addDataFields(dataFields.toArray(new DataField[0]));
        return dataDictionary;
    }

    /**
     * regression model
     */
    private RegressionModel buildRegressionModel(List<String> headers, Map<String, String> weightMap, String intercept) {
        RegressionModel regressionModel = new RegressionModel();
        regressionModel.setMiningFunction(MiningFunction.REGRESSION);
        regressionModel.setMiningSchema(buildMiningSchema(headers));
        regressionModel.addRegressionTables(buildRegressionTable(headers, weightMap, intercept));
        return regressionModel;
    }

    /**
     * Mining schema
     */
    private MiningSchema buildMiningSchema(List<String> headers) {
        MiningSchema miningSchema = new MiningSchema();
        // Y
        miningSchema.addMiningFields(new MiningField().setName(new FieldName("y"))
                .setUsageType(MiningField.UsageType.TARGET));
        for (String param : headers) {
            miningSchema.addMiningFields(new MiningField().setName(new FieldName(param)));
        }
        return miningSchema;
    }

    /**
     * Regression table
     */
    private RegressionTable buildRegressionTable(List<String> headers, Map<String, String> weightMap, String intercept) {
        RegressionTable regressionTable = new RegressionTable();
        regressionTable.setIntercept(NumberUtils.toDouble(intercept, 0));

        for (String param : headers) {
            regressionTable.addNumericPredictors(new NumericPredictor()
                    .setName(new FieldName(param))
                    .setCoefficient(NumberUtils.toDouble(weightMap.get(param))));
        }

        return regressionTable;
    }
}
