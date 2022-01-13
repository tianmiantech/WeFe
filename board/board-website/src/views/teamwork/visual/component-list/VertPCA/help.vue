<template>
    <CommonHelp>
        <template #intro>
            主成分分析(Principal components analysis, PCA)是一种统计分析、简化数据集的方法。利用正交变换来对一 系列可能相关的变量的观测值进行线性变换，从而投影为一系列线性不相关变量的值，这些不相关变量称为主成 分。
        </template>
        <template #params>
            参与方数据集的特征名列表
        </template>
        <template #desc>
            选择参与方数据集的特征集合
        </template>

        <template #output>
            选择特征集合中每个特征的特征值和对应的特征向量。
        </template>
        <template #error-tip />
    </CommonHelp>
</template>

<script>
    import CommonHelp from '../common/CommonHelp';

    export default {
        name:       'VertPCA',
        components: {
            CommonHelp,
        },
    };
</script>
