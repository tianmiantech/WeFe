<template>
    <CommonHelp>
        <template #intro>
            评分卡组件介绍。
        </template>
        <template #params>
            <code>评分卡</code>
            <br>
            评分卡。
        </template>

        <template #desc>
            <ol>
                <li>评分卡。</li>
            </ol>
        </template>

        <template #output>
            评分卡。
        </template>

        <template #error-tip>
            暂无
        </template>
    </CommonHelp>
</template>

<script>
    import CommonHelp from '../common/CommonHelp';

    export default {
        name:       'ScoreCard',
        components: {
            CommonHelp,
        },
    };
</script>
