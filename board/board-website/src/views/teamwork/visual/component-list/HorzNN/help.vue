<template>
    <CommonHelp>
        <template #intro>
            横向深度学习建模，输出模型参数，预测结果数据资源。
        </template>
        <template #params>
            <ol>
                <li>最大迭代次数</li>
                <li>学习率</li>
                <li>批量大小</li>
                <li>交互层学习率</li>
                <li>优化算法：Adadelta、Adagrad、Adam、Adamax、Nadam、RMSprop、SGD</li>
                <li>评估指标：Hinge、accuracy、AUC</li>
                <li>损失函数：binary_crossentropy、categorical_crossentropy</li>
                <li>
                    网络层级
                    <ul>
                        <li>输出维度</li>
                        <li>输入维度</li>
                        <li>激活函数：relu、sigmoid、tanh、softmax、selu</li>
                    </ul>
                </li>
            </ol>
        </template>
        <template #desc>
            <ol>
                <li>设置参数</li>
                <li>点击保存</li>
            </ol>
        </template>

        <template #output>
            <ol>
                <li>模型参数（包含特征重要性）</li>
                <li>预测结果数据资源</li>
            </ol>
        </template>
        <template #error-tip />
    </CommonHelp>
</template>

<script>
    import CommonHelp from '../common/CommonHelp';

    export default {
        name:       'HorzNN',
        components: {
            CommonHelp,
        },
    };
</script>
