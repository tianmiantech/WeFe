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
package com.welab.wefe.serving.service.utils.component;

import com.welab.wefe.common.util.JObject;
import com.welab.wefe.serving.service.database.entity.TableModelMySqlModel;
import org.apache.commons.compress.utils.Lists;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author hunter.zhao
 */
public class ScoreCardComponentUtil {

    public static JObject scoreCardInfo(TableModelMySqlModel model) {

        JObject scoreCardInfo = JObject.create(model.getScoreCardInfo());
        double bScore = extractBScore(scoreCardInfo);
        JObject binning = scoreCardInfo.getJObject("bin");
        JObject modelResult = getModelResult(model);

        JObject result = JObject.create();
        modelResult.entrySet().stream().forEach(x -> {
            double weight = modelResult.getDouble(x.getKey());
            List<Double> splitPoints = extractSplitPoints(binning.getJObject(x.getKey()));
            List<Double> woeArray = extractWoeArray(binning.getJObject(x.getKey()));

            List<Output> outputs = Lists.newArrayList();
            for (int i = 0; i < splitPoints.size(); i++) {
                Output output = new Output();
                output.setBinning(getBinningSplit(splitPoints, i));
                output.setWoe(woeArray.get(i));
                output.setScore(woeArray.get(i) * bScore * weight);
                output.setWeight(weight);
                outputs.add(output);
            }

            result.append(x.getKey(), outputs);
        });
        return result;
    }

    private static double extractBScore(JObject scoreCardInfo) {
        return scoreCardInfo.getJObject("score_card").getDouble("b_score");
    }


    private static String getBinningSplit(List<Double> list, int i) {
        String beforeKey = i == 0 ? "-∞" : precisionProcessByDouble(list.get(i - 1));
        return beforeKey + "," + precisionProcessByDouble(list.get(i));
    }

    private static String precisionProcessByDouble(double value) {
        BigDecimal bd = new BigDecimal(value);
        return bd.setScale(2, BigDecimal.ROUND_HALF_UP).toString();
    }

    private static List<Double> extractWoeArray(JObject obj) {
        return obj.getJSONList("woeArray", Double.class);
    }

    private static List<Double> extractSplitPoints(JObject obj) {
        return obj.getJSONList("splitPoints", Double.class);
    }

    private static JObject getModelResult(TableModelMySqlModel model) {
        JObject modelParam = JObject.create(model.getModelParam());
        return modelParam.getJObjectByPath("model_param.weight");
    }

    private static class Output {
        private String binning;

        private Double woe;

        private Double score;

        private Double weight;

        public String getBinning() {
            return binning;
        }

        public void setBinning(String binning) {
            this.binning = binning;
        }

        public Double getWoe() {
            return woe;
        }

        public void setWoe(Double woe) {
            this.woe = woe;
        }

        public Double getScore() {
            return score;
        }

        public void setScore(Double score) {
            this.score = score;
        }

        public Double getWeight() {
            return weight;
        }

        public void setWeight(Double weight) {
            this.weight = weight;
        }
    }
}
