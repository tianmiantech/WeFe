<template>
    <CommonHelp>
        <template #intro>
            加载各参与方用于联合建模的数据集
        </template>
        <template #params>
            暂无
        </template>

        <template #desc>
            <ol>
                <li>点击各个参与方的选择数据集按钮，在弹出的搜索页中选择对应的数据集。</li>
                <li>选择数据集的特征列，默认情况下已全选。</li>
                <li>点击保存</li>
            </ol>
        </template>

        <template #output>
            根据指定的特征加载后的数据集
        </template>

        <template #error-tip>
            暂无
        </template>
    </CommonHelp>
</template>

<script>
    import CommonHelp from '../common/CommonHelp';

    export default {
        name:       'Oot',
        components: {
            CommonHelp,
        },
    };
</script>
