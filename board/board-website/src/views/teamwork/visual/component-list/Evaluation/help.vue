<template>
    <CommonHelp>
        <template #intro>
            对模型预测结果做评估，输出模型评估指标，如二分类有AUC、KS、ACCURACY、RECALL、PRECISION、ROC等。
        </template>
        <template #params>
            <code>评估类型</code><br>
            binary（二分类）、multi（多分类）、regression（回归）
        </template>
        <template #desc>
            <ol>
                <li>选择参数</li>
                <li>点击保存</li>
            </ol>
        </template>

        <template #output>
            模型评估指标
        </template>
        <template #error-tip />
    </CommonHelp>
</template>

<script>
    import CommonHelp from '../common/CommonHelp';

    export default {
        name:       'Evaluation',
        components: {
            CommonHelp,
        },
    };
</script>
