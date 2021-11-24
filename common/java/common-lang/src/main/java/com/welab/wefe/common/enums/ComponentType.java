/**
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

package com.welab.wefe.common.enums;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Component type
 *
 * @author Zane
 */
public enum ComponentType {

    DataIO("选择数据集", null, "加载用于联邦建模的数据集"),
    Intersection("样本对齐", null, "将多个数据集使用 id 碰撞获取交集"),
    FeatureStatistic("特征统计", new ArrayList<>(Arrays.asList(FederatedLearningType.vertical)), "纵向统计组件的各项指标"),
    HorzStatistic("特征统计", new ArrayList<>(Arrays.asList(FederatedLearningType.horizontal)), "横向统计组件的各项指标"),
    MixStatistic("特征统计", new ArrayList<>(Arrays.asList(FederatedLearningType.mix)), "混合统计组件的各项指标"),
    MixBinning("分箱并编码", new ArrayList<>(Arrays.asList(FederatedLearningType.mix)), "对特征进行分箱，并进行 woe 编码。"),
    FillMissingValue("缺失值填充", null, "填充缺失值"),

    Binning("分箱并编码", new ArrayList<>(Arrays.asList(FederatedLearningType.vertical)), "对特征进行纵向分箱，并进行 woe 编码。"),
    HorzFeatureBinning("分箱并编码", new ArrayList<>(Arrays.asList(FederatedLearningType.horizontal)), "对特征进行横向分箱，并进行 woe 编码。"),
    FeatureCalculation("计算特征价值", new ArrayList<>(Arrays.asList(FederatedLearningType.vertical)), "计算特征的 CV/IV，需要在分箱之后。"),

    FeatureSelection("特征筛选", null, "挑选出需要入模的特征"),
    Segment("数据切割", null, "将数据集切割成训练集和测试集两部分"),
    VertPearson("皮尔逊相关系数", new ArrayList<>(Arrays.asList(FederatedLearningType.vertical)), "描述两个特征的相关性"),
    FeatureStandardized("特征标准化", new ArrayList<>(Arrays.asList(FederatedLearningType.vertical, FederatedLearningType.horizontal)), "对特征进行标准化"),

    HorzLR("横向逻辑回归", new ArrayList<>(Arrays.asList(FederatedLearningType.horizontal)), "横向逻辑回归建模"),
    VertLR("纵向逻辑回归", new ArrayList<>(Arrays.asList(FederatedLearningType.vertical)), "纵向逻辑回归建模"),

    HorzSecureBoost("横向XGBoost", new ArrayList<>(Arrays.asList(FederatedLearningType.horizontal)), "横向 XGBoost 建模"),
    VertSecureBoost("纵向XGBoost", new ArrayList<>(Arrays.asList(FederatedLearningType.vertical)), "纵向 XGBoost 建模"),

    MixLR("混合逻辑回归", new ArrayList<>(Arrays.asList(FederatedLearningType.mix)), "混合逻辑回归建模"),
    MixSecureBoost("混合XGBoost", new ArrayList<>(Arrays.asList(FederatedLearningType.mix)), "混合XGBoost 建模"),

    HorzNN("横向深度学习", new ArrayList<>(Arrays.asList(FederatedLearningType.horizontal)), "横向 深度学习 建模"),
    VertNN("纵向深度学习", new ArrayList<>(Arrays.asList(FederatedLearningType.vertical)), "纵向 深度学习 建模"),

    HorzLRValidationDataSetLoader("载入验证数据集·横向LR", new ArrayList<>(Arrays.asList(FederatedLearningType.horizontal)), "加载一个验证数据集用于模型评估"),
    VertLRValidationDataSetLoader("载入验证数据集·纵向LR", new ArrayList<>(Arrays.asList(FederatedLearningType.vertical)), "加载一个验证数据集用于模型评估"),
    HorzXGBoostValidationDataSetLoader("载入验证数据集·横向XGBoost", new ArrayList<>(Arrays.asList(FederatedLearningType.horizontal)), "加载一个验证数据集用于模型评估"),
    VertXGBoostValidationDataSetLoader("载入验证数据集·纵向XGBoost", new ArrayList<>(Arrays.asList(FederatedLearningType.vertical)), "加载一个验证数据集用于模型评估"),

    Evaluation("评估模型", null, "对建模结果进行效果评估"),
    Oot("打分验证", new ArrayList<>(Arrays.asList(FederatedLearningType.vertical, FederatedLearningType.horizontal)), "用新的数据集对模型进行打分验证");

    private static List<ComponentType> MODELING_TYPES = Arrays.asList(HorzLR, VertLR, HorzSecureBoost, VertSecureBoost, MixLR, MixSecureBoost, HorzNN, VertNN);


    /**
     * Chinese friendly name used to display on the interface
     */
    private final String label;
    /**
     * The applicable scope of the component. Null indicates common.
     */
    private final List<FederatedLearningType> federatedLearningTypes;
    /**
     * Description of the component
     */
    private final String desc;

    ComponentType(String label, List<FederatedLearningType> federatedLearningTypes, String desc) {
        this.label = label;
        this.federatedLearningTypes = federatedLearningTypes;
        this.desc = desc;
    }

    public String getLabel() {
        return label;
    }

    public String getDesc() {
        return desc;
    }

    public List<FederatedLearningType> getFederatedLearningTypes() {
        return federatedLearningTypes;
    }

    /**
     * Is it a modeling node
     */
    public boolean isModeling() {
        return MODELING_TYPES.contains(this);
    }
}
