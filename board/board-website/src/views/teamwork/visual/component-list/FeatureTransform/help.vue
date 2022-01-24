<template>
    <CommonHelp>
        <template #intro>
            特征转换把数据集中字符串类型数据转换成数值类型。
        </template>
        <template #params>
            数据集特征的某个值转换成指定值。
        </template>
        <template #desc>
            选择数据集特征，给特征的可能值转换成指定数值。
            <br>
            填写枚举与数值, 如:
            <br>
            特征为: x0
            <br>
            枚举为: '男', 数值为: 1
            <br>
            枚举为: '女', 数值为 0
        </template>

        <template #output>
            输出转换后的数据集。
        </template>
        <template #error-tip />
    </CommonHelp>
</template>

<script>
    import CommonHelp from '../common/CommonHelp';

    export default {
        name:       'FeatureTransform',
        components: {
            CommonHelp,
        },
    };
</script>
