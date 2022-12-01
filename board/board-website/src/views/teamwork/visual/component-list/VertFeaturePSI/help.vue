<template>
    <CommonHelp>
        <template #intro>
            根据训练集和测试集数据计算特征的PSI。
        </template>
        <template #params>
            <code>分箱方法</code>
            <br>
            将特征以选择的方法进行分箱，支持等宽，等频分箱方式
            <br>
            <code>分箱数目</code>
            <br>
            将特征分为指定的箱数
            <br>
        </template>
        <template #desc>
            <ol>
                <li>在特征下手动选择需要计算PSI的特征。</li>
                <li>在分箱方式下选择分箱方法和分箱数。</li>
                <li>点击保存</li>
            </ol>       
        </template>

        <template #output>
            输出选择特征的PSI计算结果
        </template>
        <template #error-tip />
    </CommonHelp>
</template>

<script>
    import CommonHelp from '../common/CommonHelp';

    export default {
        name:       'VertFeaturePSI',
        components: {
            CommonHelp,
        },
    };
</script>
