/**
 * Copyright 2021 Tianmian Tech. All Rights Reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.welab.wefe.common.enums;


import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.welab.wefe.common.enums.FederatedLearningType.*;

/**
 * Component type
 *
 * @author Zane
 */
public enum ComponentType {
    /**
     *
     */
    DataIO("选择数据集", null, "加载用于联邦建模的数据集"),
    Intersection("样本对齐", null, "将多个数据集使用 id 碰撞获取交集"),
    FeatureStatistic("特征统计", Arrays.asList(vertical, horizontal), "统计组件的各项指标"),
    //    HorzStatistic("特征统计", Arrays.asList(horizontal), "横向统计组件的各项指标"),
    MixStatistic("特征统计", Collections.singletonList(mix), "混合统计组件的各项指标"),
    FillMissingValue("缺失值填充", Arrays.asList(vertical, horizontal), "填充缺失值"),

    Binning("分箱并编码", Collections.singletonList(vertical), "对特征进行分箱，并进行 woe 编码。"),
    //    HorzFeatureBinning("分箱并编码", Arrays.asList(mix), "对特征进行横向分箱，并进行 woe 编码。"),
    FeatureCalculation("计算特征价值", Collections.singletonList(vertical), "计算特征的 CV/IV，需要在分箱之后。"),

    FeatureSelection("特征筛选", Arrays.asList(vertical, horizontal), "挑选出需要入模的特征"),
    Segment("数据切割", null, "将数据集切割成训练集和测试集两部分"),
    VertPearson("皮尔逊相关系数", Collections.singletonList(vertical), "描述两个特征的相关性"),
    FeatureStandardized("特征标准化", Arrays.asList(vertical, horizontal), "对特征进行标准化"),

    HorzLR("横向逻辑回归", Collections.singletonList(horizontal), "横向逻辑回归建模"),
    VertLR("纵向逻辑回归", Collections.singletonList(vertical), "纵向逻辑回归建模"),

    HorzSecureBoost("横向XGBoost", Collections.singletonList(horizontal), "横向 XGBoost 建模"),
    VertSecureBoost("纵向XGBoost", Collections.singletonList(vertical), "纵向 XGBoost 建模"),

    MixLR("混合逻辑回归", Collections.singletonList(mix), "混合逻辑回归建模"),
    MixSecureBoost("混合XGBoost", Collections.singletonList(mix), "混合XGBoost 建模"),

    HorzLRValidationDataSetLoader("载入验证数据集·横向LR", Collections.singletonList(horizontal), "加载一个验证数据集用于模型评估"),
    VertLRValidationDataSetLoader("载入验证数据集·纵向LR", Collections.singletonList(vertical), "加载一个验证数据集用于模型评估"),
    HorzXGBoostValidationDataSetLoader("载入验证数据集·横向XGBoost", Collections.singletonList(horizontal), "加载一个验证数据集用于模型评估"),
    VertXGBoostValidationDataSetLoader("载入验证数据集·纵向XGBoost", Collections.singletonList(vertical), "加载一个验证数据集用于模型评估"),

    Evaluation("评估模型", null, "对建模结果进行效果评估"),
    Oot("打分验证", Arrays.asList(vertical, horizontal), "用新的数据集对模型进行打分验证"),

    /**
     * **************** 深度学习相关组件 ****************
     */
    ImageDataIO("选择图片数据集", Collections.singletonList(horizontal), "加载用于联邦建模的图片数据集"),
    DeepLearning("深度学习", Collections.singletonList(horizontal), "深度学习");

    private static List<ComponentType> MODELING_TYPES = Arrays.asList(HorzLR, VertLR, HorzSecureBoost, VertSecureBoost, MixLR, MixSecureBoost);


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
