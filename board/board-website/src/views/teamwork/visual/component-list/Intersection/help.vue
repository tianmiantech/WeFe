<template>
    <CommonHelp>
        <template #intro>
            对多个数据集根据主键 ID 求交集，输出对齐后的样本集。
        </template>
        <template #params>
            <code>是否保存对齐后数据</code>
            <br>
            当选择是时，对齐后的数据集会作为衍生数据集被保存，衍生数据集可以在其他流程中被使用。
        </template>

        <template #desc>
            <ol>
                <li>选择对齐算法，现只支持dh算法</li>
                <li>选择是否保存对齐后的数据</li>
                <li>点击保存</li>
            </ol>
        </template>

        <template #output>
            对齐后的数据集
        </template>

        <template #error-tip>
            暂无
        </template>
    </CommonHelp>
</template>

<script>
    import CommonHelp from '../common/CommonHelp';

    export default {
        name:       'Intersection',
        components: {
            CommonHelp,
        },
    };
</script>
