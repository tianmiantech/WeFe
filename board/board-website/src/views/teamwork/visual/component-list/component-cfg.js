/*
* component configs
*/

const defaultCfg = {
    jsonParams: false,  // is json string
    autoSave:   false,
};

export default {
    DataIO: {
        ...defaultCfg,
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
    },
    /* 横向分箱编码 */
    HorzFeatureBinning: {
        ...defaultCfg,
    },
    /* 数据切割 */
    Segment: {
        ...defaultCfg,
        autoSave: true,
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
    },
    /* 横向特征统计 */
    HorzStatistic: {
        ...defaultCfg,
    },
    /* 特征价值计算 */
    FeatureCalculation: {
        ...defaultCfg,
        autoSave: true,
    },
    /* 特征选择 */
    FeatureSelection: {
        ...defaultCfg,
    },
    /* 样本对齐 */
    Intersection: {
        ...defaultCfg,
        autoSave: true,
    },
    /* 缺失值填充 */
    FillMissingValue: {
        ...defaultCfg,
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
    /* 纵向数据行过滤 */
    VertFilter: {
        ...defaultCfg,
    },
    /* 两方纵向PCA */
    VertPCA: {
        ...defaultCfg,
    },
    /* 特征转换组件 */
    FeatureTransform: {
        ...defaultCfg,
    },
    /* 横向热编码 */
    HorzOneHot: {
        ...defaultCfg,
    },
    /* 纵向热编码 */
    VertOneHot: {
        ...defaultCfg,
    },
};
