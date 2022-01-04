<template>
    <CommonHelp>
        <template #intro>
            横向联邦逻辑回归建模，输出模型参数，预测结果数据资源。
        </template>
        <template #params>
            <ol>
                <li>最大迭代次数</li>
                <li>惩罚方式：l2、l1</li>
                <li>惩罚项系数</li>
                <li>优化算法：sgd、adm、adagrad、rmsprop、nesterov_momentum_sgd。</li>
                <li>学习率</li>
                <li>批量大小：随机梯度下降每次选取的数据资源大小。</li>
                <li>收敛容忍度：当两次迭代loss值小于此值，视为收敛。</li>
                <li>收敛方式：diff—根据loss值判断是否收敛，weight_diff—根据权重值判断是否收敛。</li>
                <li>多分类策略：ovr、ovo</li>
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
                <li>模型参数</li>
                <li>预测结果数据资源</li>
            </ol>
        </template>
        <template #error-tip />
    </CommonHelp>
</template>

<script>
    import CommonHelp from '../common/CommonHelp';

    export default {
        name:       'HorzLR',
        components: {
            CommonHelp,
        },
    };
</script>
