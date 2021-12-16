<template>
    <CommonHelp>
        <template #intro>
            横向分箱编码：建立分类模型时，需要对连续变量离散化，特征离散化后，模型会更稳定，易于模型的快速迭代，降低了模型过拟合的风险。
        </template>
        <template #params />

        <template #desc>
            <ol>
                <li>分箱策略：分箱的方式：等频分箱</li>
                <li>分箱箱数：分箱箱数</li>
                <li>选择特征：此分箱策略将应用在选择的特征上</li>
            </ol>
        </template>

        <template #output>
            输出分箱结果和woe编码
        </template>

        <template #error-tip />
    </CommonHelp>
</template>

<script>
    import CommonHelp from '../common/CommonHelp';

    export default {
        name:       'HorzFeatureBinning',
        components: {
            CommonHelp,
        },
    };
</script>
