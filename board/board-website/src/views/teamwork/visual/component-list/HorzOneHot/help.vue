<template>
    <CommonHelp>
        <template #intro>
            独热编码(One-Hot Encoding)，又称一位有效编码，其方法是使用N位状态寄存器来对N个状态进行编码，每个状态都有它独立的寄存器位，并且在任意时候，其中只有一位有效，即只有一位是1，其余都是零值。
        </template>
        <template #params>
            参与方数据资源的特征名列表(要求选择的特征类型是整型或字符串，并且要求特征的值是离散的并小于20个类 别)
        </template>
        <template #desc>
            选择参与方数据资源的特征
        </template>

        <template #output>
            生成新数据资源
        </template>
        <template #error-tip />
    </CommonHelp>
</template>

<script>
    import CommonHelp from '../common/CommonHelp';

    export default {
        name:       'HorzOneHot',
        components: {
            CommonHelp,
        },
    };
</script>
