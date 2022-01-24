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

package com.welab.wefe.board.service.component;

import com.welab.wefe.board.service.component.base.AbstractComponent;
import com.welab.wefe.board.service.component.deep_learning.ImageDataIOComponent;
import com.welab.wefe.board.service.component.deep_learning.PaddleClassifyComponent;
import com.welab.wefe.board.service.component.deep_learning.PaddleDetectionComponent;
import com.welab.wefe.board.service.component.feature.*;
import com.welab.wefe.board.service.component.modeling.*;
import com.welab.wefe.common.web.Launcher;
import com.welab.wefe.common.wefe.enums.ComponentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author zane.luo
 */
@Service
public class Components {

    @Autowired
    private DataIOComponent dataIOComponent;
    @Autowired
    private IntersectionComponent intersectionComponent;
    @Autowired
    private EvaluationComponent evaluationComponent;
    @Autowired
    private HorzLRComponent horzLRComponent;
    @Autowired
    private VertLRComponent vertLRComponent;
    @Autowired
    private BinningComponent binningComponent;
    @Autowired
    private HorzSecureBoostComponent horzSecureBoostComponent;
    @Autowired
    private VertSecureBoostComponent vertSecureBoostComponent;
    @Autowired
    private FeatureSelectionComponent featureSelectionComponent;
    @Autowired
    private SegmentComponent segmentComponent;
    @Autowired
    private FeatureStatisticsComponent featureStatisticsComponent;
    @Autowired
    private FeatureCalculationComponent featureCalculationComponent;
    @Autowired
    private FillMissingValueComponent fillMissingValueComponent;
    @Autowired
    private FeatureStandardizedComponent featureStandardizedComponent;
    @Autowired
    private VertPearsonComponent vertPearsonComponent;
    @Autowired
    private MixLrComponent mixLrComponent;
    @Autowired
    private MixSecureBoostComponent mixSecureBoostComponent;
    @Autowired
    private MixStatisticComponent mixStatisticComponent;
    @Autowired
    private OotComponent ootComponent;
    @Autowired
    private FeatureTransformComponent featureTransformComponent;
    @Autowired
    private HorzOneHotComponent horzOneHotComponent;
    @Autowired
    private VertFilterComponent vertFilterComponent;
    @Autowired
    private VertOneHotComponent vertOneHotComponent;
    @Autowired
    private VertPCAComponent vertPCAComponent;
    @Autowired
    private HorzFeatureBinningComponent horzFeatureBinningComponent;
    @Autowired
    private HorzStatisticComponent horzStatisticComponent;
    @Autowired
    private VertNNComponent vertNNComponent;
    @Autowired
    private HorzNNComponent horzNNComponent;
    @Autowired
    private MixBinningComponent mixBinningComponent;

    public static AbstractComponent<?> getDataIOComponent() {
        return get(ComponentType.DataIO);
    }

    @Autowired
    private ImageDataIOComponent imageDataIOComponent;
    @Autowired
    private PaddleClassifyComponent paddleClassifyComponent;
    @Autowired
    private PaddleDetectionComponent paddleDetectionComponent;


    public static AbstractComponent<?> get(ComponentType componentType) {

        switch (componentType) {
            case HorzLRValidationDataSetLoader:
            case VertLRValidationDataSetLoader:
            case HorzXGBoostValidationDataSetLoader:
            case VertXGBoostValidationDataSetLoader:
            case DataIO:
                return Launcher.getBean(Components.class).dataIOComponent;
            case Intersection:
                return Launcher.getBean(Components.class).intersectionComponent;
            case Evaluation:
                return Launcher.getBean(Components.class).evaluationComponent;
            case HorzLR:
                return Launcher.getBean(Components.class).horzLRComponent;
            case VertLR:
                return Launcher.getBean(Components.class).vertLRComponent;
            case Binning:
                return Launcher.getBean(Components.class).binningComponent;
            case HorzSecureBoost:
                return Launcher.getBean(Components.class).horzSecureBoostComponent;
            case VertSecureBoost:
                return Launcher.getBean(Components.class).vertSecureBoostComponent;
            case FeatureSelection:
                return Launcher.getBean(Components.class).featureSelectionComponent;
            case Segment:
                return Launcher.getBean(Components.class).segmentComponent;
            case FeatureStatistic:
                return Launcher.getBean(Components.class).featureStatisticsComponent;
            case FeatureCalculation:
                return Launcher.getBean(Components.class).featureCalculationComponent;
            case FillMissingValue:
                return Launcher.getBean(Components.class).fillMissingValueComponent;
            case FeatureStandardized:
                return Launcher.getBean(Components.class).featureStandardizedComponent;
            case VertPearson:
                return Launcher.getBean(Components.class).vertPearsonComponent;
            case MixLR:
                return Launcher.getBean(Components.class).mixLrComponent;
            case MixSecureBoost:
                return Launcher.getBean(Components.class).mixSecureBoostComponent;
            case MixStatistic:
                return Launcher.getBean(Components.class).mixStatisticComponent;
            case Oot:
                return Launcher.CONTEXT.getBean(Components.class).ootComponent;
            case VertFilter:
                return Launcher.CONTEXT.getBean(Components.class).vertFilterComponent;
            case FeatureTransform:
                return Launcher.CONTEXT.getBean(Components.class).featureTransformComponent;
            case HorzOneHot:
                return Launcher.CONTEXT.getBean(Components.class).horzOneHotComponent;
            case VertOneHot:
                return Launcher.getBean(Components.class).vertOneHotComponent;
            case VertPCA:
                return Launcher.getBean(Components.class).vertPCAComponent;
            case HorzFeatureBinning:
                return Launcher.getBean(Components.class).horzFeatureBinningComponent;
            case HorzStatistic:
                return Launcher.getBean(Components.class).horzStatisticComponent;
            case HorzNN:
                return Launcher.getBean(Components.class).horzNNComponent;
            case VertNN:
                return Launcher.getBean(Components.class).vertNNComponent;
            case MixBinning:
                return Launcher.getBean(Components.class).mixBinningComponent;
            case ImageDataIO:
                return Launcher.getBean(Components.class).imageDataIOComponent;
            case PaddleClassify:
                return Launcher.getBean(Components.class).paddleClassifyComponent;
            case PaddleDetection:
                return Launcher.getBean(Components.class).paddleDetectionComponent;
            default:
                return null;
        }
    }

    public static boolean needArbiterTask(ComponentType type) {
        return type == ComponentType.HorzLR
                || type == ComponentType.HorzSecureBoost
                || type == ComponentType.MixLR
                || type == ComponentType.MixSecureBoost
                || type == ComponentType.MixStatistic
                || type == ComponentType.HorzStatistic
                || type == ComponentType.HorzFeatureBinning
                || type == ComponentType.HorzNN
                || type == ComponentType.MixBinning;
    }
}
