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

package com.welab.wefe.board.service.component;

import com.welab.wefe.board.service.component.base.AbstractComponent;
import com.welab.wefe.board.service.component.feature.*;
import com.welab.wefe.board.service.component.modeling.*;
import com.welab.wefe.common.enums.ComponentType;
import com.welab.wefe.common.web.Launcher;
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
    private HorzFeatureBinningComponent horzFeatureBinningComponent;
    @Autowired
    private HorzStatisticComponent horzStatisticComponent;

    public static AbstractComponent<?> getDataIOComponent() {
        return get(ComponentType.DataIO);
    }

    public static AbstractComponent<?> get(ComponentType componentType) {

        switch (componentType) {
            case HorzLRValidationDataSetLoader:
            case VertLRValidationDataSetLoader:
            case HorzXGBoostValidationDataSetLoader:
            case VertXGBoostValidationDataSetLoader:
            case DataIO:
                return Launcher.CONTEXT.getBean(Components.class).dataIOComponent;
            case Intersection:
                return Launcher.CONTEXT.getBean(Components.class).intersectionComponent;
            case Evaluation:
                return Launcher.CONTEXT.getBean(Components.class).evaluationComponent;
            case HorzLR:
                return Launcher.CONTEXT.getBean(Components.class).horzLRComponent;
            case VertLR:
                return Launcher.CONTEXT.getBean(Components.class).vertLRComponent;
            case Binning:
                return Launcher.CONTEXT.getBean(Components.class).binningComponent;
            case HorzSecureBoost:
                return Launcher.CONTEXT.getBean(Components.class).horzSecureBoostComponent;
            case VertSecureBoost:
                return Launcher.CONTEXT.getBean(Components.class).vertSecureBoostComponent;
            case FeatureSelection:
                return Launcher.CONTEXT.getBean(Components.class).featureSelectionComponent;
            case Segment:
                return Launcher.CONTEXT.getBean(Components.class).segmentComponent;
            case FeatureStatistic:
                return Launcher.CONTEXT.getBean(Components.class).featureStatisticsComponent;
            case FeatureCalculation:
                return Launcher.CONTEXT.getBean(Components.class).featureCalculationComponent;
            case FillMissingValue:
                return Launcher.CONTEXT.getBean(Components.class).fillMissingValueComponent;
            case FeatureStandardized:
                return Launcher.CONTEXT.getBean(Components.class).featureStandardizedComponent;
            case VertPearson:
                return Launcher.CONTEXT.getBean(Components.class).vertPearsonComponent;
            case MixLR:
                return Launcher.CONTEXT.getBean(Components.class).mixLrComponent;
            case MixSecureBoost:
                return Launcher.CONTEXT.getBean(Components.class).mixSecureBoostComponent;
            case MixStatistic:
                return Launcher.CONTEXT.getBean(Components.class).mixStatisticComponent;
            case Oot:
                return Launcher.CONTEXT.getBean(Components.class).ootComponent;
            case HorzFeatureBinning:
                return Launcher.CONTEXT.getBean(Components.class).horzFeatureBinningComponent;
            case HorzStatistic:
                return Launcher.CONTEXT.getBean(Components.class).horzStatisticComponent;
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
                || type == ComponentType.HorzStatistic;
    }
}
