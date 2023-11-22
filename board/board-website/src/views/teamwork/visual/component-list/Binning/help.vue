<template>
    <CommonHelp>
        <template #intro>
            建立分类模型时，需要对连续变量离散化，特征离散化后，模型会更稳定，易于模型的快速迭代，降低了模型过拟合的风险。
        </template>
        <template #params />

        <template #desc>
            <ol>
                <li>分箱策略：分箱的方式：等频分箱、等宽分箱、卡方分箱</li>
                <li>分箱箱数：分箱箱数</li>
                <li>选择特征：此分箱策略将应用在选择的特征上</li>
                <li>添加分箱策略：增加一个新的分箱策略</li>
            </ol>
        </template>

        <template #output>
            输出分箱和woe编码后的数据资源
        </template>

        <template #error-tip>
            暂无
        </template>
    </CommonHelp>
</template>

<script>
    import CommonHelp from '../common/CommonHelp';

    export default {
        name:       'Binning',
        components: {
            CommonHelp,
        },
    };
</script>
