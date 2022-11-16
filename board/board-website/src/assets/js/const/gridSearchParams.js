const learningRate = {
    key:   'learningRate',
    label: '学习率',
    rule:  {
        message:  '请输入大于0小于1的数字。',
        checkFun: (val) => val > 0 && val <= 1,
    },
};

export default {
    xgboost: [
        learningRate,
        {
            key:   'numTrees',
            label: '最大树数量',
            rule:  {
                message:  '请输入大于0的整数。',
                checkFun: (val) =>
                    val > 0 && parseFloat(val) === parseInt(val, 10),
            },
        },
        {
            key:   'maxDepth',
            label: '树的最大深度',
            rule:  {
                message:  '请输入大于0的整数。',
                checkFun: (val) =>
                    val > 0 && parseFloat(val) === parseInt(val, 10),
            },
        },
        {
            key:   'subsampleFeatureRate',
            label: '特征随机采样比率',
            rule:  {
                message:  '请输入大于0小于1的数字。',
                checkFun: (val) => val > 0 && val <= 1,
            },
        },
        {
            key:   'binNum',
            label: '最大桶数量',
            rule:  {
                message:  '请输入大于0的整数。',
                checkFun: (val) =>
                    val > 0 && parseFloat(val) === parseInt(val, 10),
            },
        },
    ],
    lr: [
        {
            key:   'batchSize',
            label: '批量大小',
            rule:  {
                message:  '请输入大于0的整数。',
                checkFun: (val) =>
                    val > 0 && parseFloat(val) === parseInt(val, 10),
            },
        },
        {
            key:   'maxIter',
            label: '最大迭代次数',
            rule:  {
                message:  '请输入大于0的整数。',
                checkFun: (val) =>
                    val > 0 && parseFloat(val) === parseInt(val, 10),
            },
        },
        learningRate,
        {
            key:   'alpha',
            label: '惩罚项系数',
            rule:  {
                message:  '请输入大于0的数字。',
                checkFun: (val) => val > 0,
            },
        },
        {
            key:   'optimizer',
            label: '优化算法',
            items: [
                { value: 'sgd', text: 'sgd' },
                { value: 'rmsprop', text: 'rmsprop' },
                { value: 'adam', text: 'adam' },
                {
                    value: 'nesterov_momentum_sgd',
                    text:  'nesterov_momentum_sgd',
                },
                { value: 'sqn', text: 'sqn' },
                { value: 'adagrad', text: 'adagrad' },
            ],
        },
    ],
};
