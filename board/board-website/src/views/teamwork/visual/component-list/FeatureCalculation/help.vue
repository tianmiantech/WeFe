<template>
    <CommonHelp>
        <template #intro>
            通过对分箱后的数据计算得到相关指标系数（iv值、cv值等）
        </template>
        <template #params />
        <template #desc>
            需要在“分箱并编码”后才能进行特征价值计算
        </template>

        <template #output />
        <template #error-tip />
    </CommonHelp>
</template>

<script>
    import CommonHelp from '../common/CommonHelp';

    export default {
        name:       'FeatureCalculation',
        components: {
            CommonHelp,
        },
    };
</script>
