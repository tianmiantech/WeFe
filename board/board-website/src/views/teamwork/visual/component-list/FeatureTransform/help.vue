<template>
    <CommonHelp>
        <template #intro>
            将字符串类型特征编码成 [数值类]。
        </template>
        <template #params>
            针对特征点击转换类型按钮添加转换类型, 可将枚举转换为数值
        </template>
        <template #desc>
            填写枚举与数值, 如:
            <br>
            特征为: x0
            <br>
            枚举为: '男', 数值为: 1
            <br>
            枚举为: '女', 数值为 0
        </template>

        <template #output />
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
