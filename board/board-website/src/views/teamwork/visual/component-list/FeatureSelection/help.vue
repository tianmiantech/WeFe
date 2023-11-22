<template>
    <CommonHelp>
        <template #intro>
            使用多种方式对各数据资源的特征进行过滤
        </template>
        <template #params>
            无
        </template>
        <template #desc>
            无
        </template>

        <template #output>
            基于选择后的特征列表重新加载的数据资源
        </template>
        <template #error-tip>
            暂无
        </template>
    </CommonHelp>
</template>

<script>
    import CommonHelp from '../common/CommonHelp';

    export default {
        name:       'FeatureSelection',
        components: {
            CommonHelp,
        },
    };
</script>
