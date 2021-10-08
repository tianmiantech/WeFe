/*
 * Chart config
 */

// base config
const commonConfig = {
    show:       true,
    loading:    true,
    legend:     [],
    xAxis:      [],
    series:     [],
    thresholds: {},
    settings:   {
        xAxisType: 'value',
    },
    train: {
        auc: '',
        ks:  '',
    },
    validate: {
        auc: '',
        ks:  '',
    },
};
const chart = {
    roc: {
        type:   'line',
        config: {
            ...commonConfig,
            type:     'ks', // draw roc with ks
            settings: {
                area:      true,
                xAxisType: 'value',
            },
        },
    },
    accuracy: {
        type:   'line',
        config: {
            ...commonConfig,
            type:  'accuracy',
            xAxis: { axisLabel: {} },
        },
    },
    gain: {
        type:   'line',
        config: {
            ...commonConfig,
            type:          'gain',
            showThreshold: false,
            xAxis:         { axisLabel: {} },
        },
    },
    lift: {
        type:   'line',
        config: {
            ...commonConfig,
            type:          'lift',
            showThreshold: false,
            xAxis:         { axisLabel: {} },
        },
    },
    ks: {
        type:   'line',
        config: {
            ...commonConfig,
            type:       'ks',
            thresholds: [],
            markLine:   {
                name:      '差值线',
                symbol:    'none', // no arrow
                lineStyle: {
                    width: 2,
                },
                data: [
                    [
                        {
                            name:      'train 差值',
                            type:      'min',
                            coord:     ['0', '0'],
                            lineStyle: {
                                color: '#000',
                                width: 2,
                            },
                        },
                        {
                            type:      'max',
                            coord:     ['0', '0'],
                            lineStyle: {
                                color: '#000',
                                width: 2,
                            },
                        },
                    ],
                    [
                        {
                            name:  'validate 差值',
                            type:  'min',
                            coord: ['0', '0'],
                        },
                        {
                            type:  'max',
                            coord: ['0', '0'],
                        },
                    ],
                ],
            },
            formatter: ({
                seriesNames,
                train_ks_fpr,
                train_ks_tpr,
                validate_ks_fpr,
                validate_ks_tpr,
                thresholds,
                dataIndex,
            }) => {
                const series = {
                    train_ks_fpr:    `${train_ks_fpr[dataIndex][1]}<br>`,
                    train_ks_tpr:    `${train_ks_tpr[dataIndex][1]}<br>`,
                    validate_ks_fpr: `${validate_ks_fpr[dataIndex][1]}<br>`,
                    validate_ks_tpr: `${validate_ks_tpr[dataIndex][1]}<br>`,
                };
                const result = seriesNames.map(name => `${name}: ${series[name]}`).join('');

                return `${result}thresholds: ${thresholds[dataIndex]}<br>`;
            },
        },
    },
    precisionRecall: {
        type:   'line',
        config: {
            ...commonConfig,
            thresholds: [],
            type:       'precision_recall',
            formatter:  ({
                seriesNames,
                thresholds,
                dataIndex,
                train_precision,
                train_recall,
                validate_precision,
                validate_recall,
            }) => {
                const series = {
                    train_precision:    `${train_precision[dataIndex][1]}<br>`,
                    train_recall:       `${train_recall[dataIndex][1]}<br>`,
                    validate_precision: `${validate_precision[dataIndex][1]}<br>`,
                    validate_recall:    `${validate_recall[dataIndex][1]}<br>`,
                };
                const result = seriesNames.map(name => `${name}: ${series[name]}`).join('');

                return `${result}thresholds: ${thresholds[dataIndex]}<br>`;
            },
        },
    },
    train_loss: {
        type:   'line',
        config: {
            ...commonConfig,
            type:  'ks',
            xAxis: { axisLabel: {} },
        },
    },
};

export default {
    Evaluation: {
        tabs: [{
            label: 'Roc',
            name:  'roc',
            chart: chart.roc,
        }, {
            label: 'K-S',
            name:  'ks',
            chart: chart.ks,
        }, {
            label: 'Lift',
            name:  'lift',
            chart: chart.lift,
        }, {
            label: 'Gain',
            name:  'gain',
            chart: chart.gain,
        }, {
            label: 'Precision Recall',
            name:  'precisionRecall',
            chart: chart.precisionRecall,
        }, {
            label: 'Accuracy',
            name:  'accuracy',
            chart: chart.accuracy,
        }],
    },
    Oot: {
        tabs: [{
            label: 'Roc',
            name:  'roc',
            chart: chart.roc,
        }, {
            label: 'K-S',
            name:  'ks',
            chart: chart.ks,
        }, {
            label: 'Lift',
            name:  'lift',
            chart: chart.lift,
        }, {
            label: 'Gain',
            name:  'gain',
            chart: chart.gain,
        }, {
            label: 'Precision Recall',
            name:  'precisionRecall',
            chart: chart.precisionRecall,
        }, {
            label: 'Accuracy',
            name:  'accuracy',
            chart: chart.accuracy,
        }],
    },
    VertLR: {
        tabs: [{
            label: 'Train loss',
            name:  'train_loss',
            chart: chart.train_loss,
        }],
    },
    HorzLR: {
        tabs: [{
            label: 'Train loss',
            name:  'train_loss',
            chart: chart.train_loss,
        }],
    },
    VertSecureBoost: {
        tabs: [{
            label: 'Train loss',
            name:  'train_loss',
            chart: chart.train_loss,
        }],
    },
    HorzSecureBoost: {
        tabs: [{
            label: 'Train loss',
            name:  'train_loss',
            chart: chart.train_loss,
        }],
    },
};
