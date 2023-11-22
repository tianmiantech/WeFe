<template>
    <CommonHelp>
        <template #intro>
            把数据资源中缺失的数据进行填充，输出填充后数据资源。
        </template>
        <template #params>
            填充策略：最大值、最小值、均值、常量
        </template>

        <template #desc>
            <ol>
                <li>选择填充策略</li>
                <li>选择使用当前填充策略的特征</li>
                <li>点击保存</li>
            </ol>
        </template>

        <template #output>
            输出填充后的数据资源
        </template>

        <template #error-tip />
    </CommonHelp>
</template>

<script>
    import CommonHelp from '../common/CommonHelp';

    export default {
        name:       'FillMissingValue',
        components: {
            CommonHelp,
        },
    };
</script>
