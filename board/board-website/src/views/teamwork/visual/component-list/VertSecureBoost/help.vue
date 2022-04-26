<template>
    <CommonHelp>
        <template #intro>
            纵向联邦XGBoost建模，输出模型参数，预测结果数据资源。
        </template>
        <template #params>
            <ol>
                <li>最大树数量</li>
                <li>树的最大深度</li>
                <li>最大桶（箱）数量：直方图算法中指定每个特征最大分箱数量</li>
                <li>学习率</li>
                <li>特征随机采样比率：训练时可选择部分特征参与训练，当该值为1时，表示使用全部特征变量</li>
                <li>收敛阀值：当两次迭代loss值小于此值，视为收敛。</li>
                <li>验证频次：每迭代多少次跑一次评估验证。</li>
                <li>n次迭代没变化是否停止：与下方参数配合使用，该值为true时，下方参数才有效。</li>
                <li>提前结束的迭代次数：连续n次迭代模型无变化则结束迭代。</li>
                <li>工作模式：work_mode枚举值：normal、layered、skip。
                    <br>
                    三个枚举值说明如下：
                    <br>
                    ① normal：基于同态加密的联邦学习方案。promoter 跟 provider共同构建树结构，每一个节点的分割需要多方共同合作完成。
                    <br>
                    ② layered：基于同态加密的联邦学习方案。promoter 跟 provider交替构建树结构，一方构建完某一层或多层后，再交由另一方构建，交替进行。
                    <br>
                    ③ skip：基于同态加密的联邦学习方案。promoter 跟 provider交替构建树结构，一方构建完一棵或多棵树后，再交由另一方构建，交替进行。
                    <br>
                    ④ dp：基于差分隐私的联邦学习方案。对分箱的结果加入一定噪声，以混淆数据的分布。新增超参数epsilon，epsilon越小，隐私保护越好，但加入的噪声 （1/(e^epsilon + 箱数量 - 1)） 越大，数据可用性下降。
                </li>
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
