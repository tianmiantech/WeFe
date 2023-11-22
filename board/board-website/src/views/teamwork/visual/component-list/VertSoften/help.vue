<template>
    <CommonHelp>
        <template #intro>
            对数据资源的特征进行统计，并输出统计结果。
        </template>
        <template #params>
            <code>选择特征</code>
            <br>
            该组件仅会对选择的特征进行统计
            <br>
        </template>
        <template #desc>
            tips：默认数量 ≤ 20 的特征为离散型特征，否则为连续型特征。
        </template>

        <template #output>
            数据资源统计报告
        </template>
        <template #error-tip />
    </CommonHelp>
</template>

<script>
    import CommonHelp from '../common/CommonHelp';

    export default {
        name:       'VertSoften',
        components: {
            CommonHelp,
        },
    };
</script>
