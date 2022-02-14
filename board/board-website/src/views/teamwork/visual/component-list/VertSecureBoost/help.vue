<template>
    <CommonHelp>
        <template #intro>
            纵向联邦XGBoost建模，输出模型参数，预测结果数据资源。
        </template>
        <template #params>
            <ol>
                <li>同态加密算法：paillier</li>
                <li>最大树数量</li>
                <li>树的最大深度</li>
                <li>最大桶（箱）数量：直方图算法中指定每个特征最大分箱数量</li>
                <li>学习率</li>
                <li>特征随机采样比率：训练时可选择部分特征参与训练，当该值为1时，表示使用全部特征变量</li>
                <li>收敛阀值：当两次迭代loss值小于此值，视为收敛。</li>
                <li>验证频次：每迭代多少次跑一次评估验证。</li>
                <li>n次迭代没变化是否停止：与下方参数配合使用，该值为true时，下方参数才有效。</li>
                <li>提前结束的迭代次数：连续n次迭代模型无变化则结束迭代。</li>
            </ol>
        </template>
        <template #desc>
            <ol>
                <li>设置参数</li>
                <li>点击保存</li>
            </ol>
        </template>

        <template #output>
            <ol>
                <li>模型参数 （包含特征重要性，并且只存在于有y的一方）</li>
                <li>预测结果数据资源 (无y标签的一方没有预测结果数据资源)</li>
            </ol>
        </template>
        <template #error-tip />
    </CommonHelp>
</template>

<script>
    import CommonHelp from '../common/CommonHelp';

    export default {
        name:       'VertSecureBoost',
        components: {
            CommonHelp,
        },
    };
</script>
