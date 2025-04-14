<template>
    <CommonHelp>
        <template #intro>
            指根据客户的各种属性和行为数据，利用信用评分模型，对客户进行信用评分。
        </template>
        <template #params>
            <code>基准分：某个特定违约几率下的预期分值</code>
            <br/>
            <code>$PD0$ : 比率翻番时分数的变动值</code>
        </template>

        <template #desc>
            <ol>
                <li>输入参数信息，点击保存。</li>
                <li>在评估组件可以查看分数结果统计。</li>
            </ol>
        </template>

        <template #output>
            暂无
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
