<template>
    <CommonHelp>
        <template #intro>
            Min-max 标准化和Z-score标准化，输出标准化后的数据
        </template>
        <template #params>
            <code>标准化方法</code>
            <br>
            min-max标准化：0-1标准化<br>
            z-score标准化：正态标准化<br>
        </template>
        <template #desc>
            <ol>
                <li>选择标准化方法</li>
                <li>选择特征变量</li>
                <li>点击保存</li>
            </ol>
        </template>

        <template #output>
            输出标准化后的数据集
        </template>
        <template #error-tip />
    </CommonHelp>
</template>

<script>
    import CommonHelp from '../common/CommonHelp';

    export default {
        name:       'FeatureStandardized',
        components: {
            CommonHelp,
        },
    };
</script>
