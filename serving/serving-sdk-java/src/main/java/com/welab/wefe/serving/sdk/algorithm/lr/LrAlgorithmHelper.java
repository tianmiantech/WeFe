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

package com.welab.wefe.serving.sdk.algorithm.lr;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.util.TypeUtils;
import com.welab.wefe.common.util.JObject;
import com.welab.wefe.serving.sdk.dto.BatchPredictParams;
import com.welab.wefe.serving.sdk.enums.StateCode;
import com.welab.wefe.serving.sdk.model.ScoreCardInfoModel;
import com.welab.wefe.serving.sdk.model.lr.LrModel;
import com.welab.wefe.serving.sdk.model.lr.LrPredictResultModel;
import com.welab.wefe.serving.sdk.model.lr.LrScoreCardModel;
import com.welab.wefe.serving.sdk.utils.AlgorithmThreadPool;
import org.apache.commons.compress.utils.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;

import static java.lang.Math.exp;

/**
 * @author hunter.zhao
 */
public class LrAlgorithmHelper {
    private static final Logger LOG = LoggerFactory.getLogger(LrAlgorithmHelper.class);


    /**
     * single sigmod function
     */
    public static Double sigmod(Double score) {
        return 1. / (1. + exp(-score));
    }

    /**
     * Calculate points based on features
     */
    public static LrPredictResultModel compute(LrModel model, String userId, Map<String, Object> featureData, ScoreCardInfoModel scoreCardInfo) {
        if (featureData == null) {
            return LrPredictResultModel.of(userId, 0.0);
        }

        return scoreCardInfo != null ? computeScoreCard(model, userId, featureData, scoreCardInfo) : computeProbability(model, userId, featureData);

    }

    private static LrPredictResultModel computeProbability(LrModel model, String userId, Map<String, Object> featureData) {
        double score = 0;
        int featureNum = 0;

        for (String key : featureData.keySet()) {
            if (model.getWeight().containsKey(key)) {
                Double x = TypeUtils.castToDouble(featureData.get(key));
                Double w = TypeUtils.castToDouble(model.getWeight().get(key));
                score += w * x;
                featureNum++;
            }
        }

        //Features do not match at all
        if (featureNum <= 0) {
            LOG.error("featureData error, userId : {}, featureData: {} ,weight: {}", userId, JSON.toJSONString(featureData), JSON.toJSONString(model.getWeight()));
            return LrPredictResultModel.fail(userId, StateCode.FEATURE_ERROR.getMessage());
        }

        return LrPredictResultModel.of(userId, score);
    }

    /**
     * Calculate points based on features
     */
    public static LrPredictResultModel computeScoreCard(LrModel model, String userId, Map<String, Object> featureData, ScoreCardInfoModel scoreCardInfo) {

        JObject bin = JObject.create(scoreCardInfo.getBin());
        double bScore = JObject.create(scoreCardInfo.getScoreCard()).getDouble("b_score");
        double score = 0;
        int featureNum = 0;

        List<LrScoreCardModel> scoreCard = Lists.newArrayList();
        for (String key : featureData.keySet()) {
            if (model.getWeight().containsKey(key)) {

                Double x = TypeUtils.castToDouble(featureData.get(key));
                Double w = TypeUtils.castToDouble(model.getWeight().get(key));
                List<Double> splitPoints = extractSplitPoints(bin.getJObject(key));
                List<Double> woeArray = extractWoeArray(bin.getJObject(key));

                LrScoreCardModel scoreCardModel = new LrScoreCardModel();
                scoreCardModel.setFeature(key);
                scoreCardModel.setValue(x);
                scoreCardModel.setBin(getBin(splitPoints, x));
                scoreCardModel.setWoe(getWoe(woeArray, x));
                scoreCardModel.setScore(w * getWoe(woeArray, x) * bScore);

                scoreCard.add(scoreCardModel);

                score += w * getWoe(woeArray, x) * bScore;
                featureNum++;
            }
        }

        //Features do not match at all
        if (featureNum <= 0) {
            LOG.error("featureData error, userId : {}, featureData: {} ,weight: {}", userId, JSON.toJSONString(featureData), JSON.toJSONString(model.getWeight()));
            return LrPredictResultModel.fail(userId, StateCode.FEATURE_ERROR.getMessage());
        }

        return LrPredictResultModel.of(userId, score, scoreCard);
    }

    public static List<LrPredictResultModel> batchCompute(LrModel lrModel, BatchPredictParams batchPredictParams, ScoreCardInfoModel scoreCardInfo) {
        CopyOnWriteArrayList<LrPredictResultModel> outputs = new CopyOnWriteArrayList<>();

        CountDownLatch latch = new CountDownLatch(batchPredictParams.getUserIds().size());

        batchPredictParams.getPredictParamsList().forEach(x ->
                AlgorithmThreadPool.run(() -> outputs.add(
                                LrAlgorithmHelper.compute(
                                        lrModel,
                                        x.getUserId(),
                                        x.getFeatureDataModel().getFeatureDataMap(),
                                        scoreCardInfo)
                        )
                )
        );

        try {
            latch.await();
        } catch (InterruptedException e) {
            LOG.error("Execution prediction error：{}", e.getMessage());
            e.printStackTrace();
        }

        return outputs;
    }

    public static Double intercept(double score, double intercept) {
        return score + intercept;
    }

    private static List<Double> extractSplitPoints(JObject obj) {
        return obj.getJSONList("splitPoints", Double.class);
    }


    private static List<Double> extractWoeArray(JObject obj) {
        return obj.getJSONList("woeArray", Double.class);
    }


    private static String getBin(List<Double> splits, Double value) {
        for (int i = 0; i < splits.size(); i++) {
            if (value <= splits.get(i)) {
                return getBinningSplit(splits, i);
            }
        }
        return null;
    }

    private static String getBinningSplit(List<Double> list, int i) {
        String beforeKey = i == 0 ? "-∞" : precisionProcessByDouble(list.get(i - 1));
        return beforeKey + "," + precisionProcessByDouble(list.get(i));
    }

    private static String precisionProcessByDouble(double value) {
        BigDecimal bd = new BigDecimal(value);
        return bd.setScale(2, BigDecimal.ROUND_HALF_UP).toString();
    }

    private static double getWoe(List<Double> woeList, Double value) {
        for (int i = 0; i < woeList.size(); i++) {
            if (value <= woeList.get(i)) {
                return woeList.get(i);
            }
        }
        return 0;
    }
}
