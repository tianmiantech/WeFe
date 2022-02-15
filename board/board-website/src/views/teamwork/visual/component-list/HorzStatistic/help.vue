<template>
    <CommonHelp>
        <template #intro>
            对数据集的特征进行统计，并输出统计结果。
        </template>
        <template #params>
            <code>选择特征</code>
            <br>
            该组件仅会对选择的特征的交集进行统计
            <br>
        </template>
        <template #desc />

        <template #output>
            数据集统计报告
        </template>
        <template #error-tip />
    </CommonHelp>
</template>

<script>
    import CommonHelp from '../common/CommonHelp';

    export default {
        name:       'HorzStatistic',
        components: {
            CommonHelp,
        },
    };
</script>
