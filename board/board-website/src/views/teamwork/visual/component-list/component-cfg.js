/*
* component configs
*/

const defaultCfg = {
    jsonParams: false,  // is json string
    autoSave:   true,
};

export default {
    DataIO: {
        ...defaultCfg,
        autoSave: false,
    },
    /* 横向LR */
    HorzLR: {
        ...defaultCfg,
    },
    /* 纵向LR */
    VertLR: {
        ...defaultCfg,
    },
    /* 混合LR */
    MixLR: {
        ...defaultCfg,
    },
    /* 分箱 */
    Binning: {
        ...defaultCfg,
        autoSave: false,
    },
    /* 横向分箱编码 */
    HorzFeatureBinning: {
        ...defaultCfg,
        autoSave: false,
    },
    /* 数据切割 */
    Segment: {
        ...defaultCfg,
    },
    /* 模型评估 */
    Evaluation: {
        ...defaultCfg,
    },
    /* 数据分布 */
    Distribution: {
        ...defaultCfg,
    },
    /* 特征统计 */
    FeatureStatistic: {
        ...defaultCfg,
        autoSave: false,
    },
    /* 横向特征统计 */
    HorzStatistic: {
        ...defaultCfg,
        autoSave: false,
    },
    /* 特征价值计算 */
    FeatureCalculation: {
        ...defaultCfg,
        autoSave: false,
    },
    /* 特征选择 */
    FeatureSelection: {
        ...defaultCfg,
        autoSave: false,
    },
    /* 样本对齐 */
    Intersection: {
        ...defaultCfg,
    },
    /* 缺失值填充 */
    FillMissingValue: {
        ...defaultCfg,
        autoSave: false,
    },
    /* 横向XGBoost */
    HorzSecureBoost: {
        ...defaultCfg,
    },
    /* 纵向XGBoost */
    VertSecureBoost: {
        ...defaultCfg,
    },
    /* 混合XGBoost */
    MixSecureBoost: {
        ...defaultCfg,
    },
};
