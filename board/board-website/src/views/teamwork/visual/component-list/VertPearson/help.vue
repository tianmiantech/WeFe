<template>
    <CommonHelp>
        <template #intro>
            计算各特征之间的相关系数，并使用热力图展示，只支持两方。
        </template>
        <template #params />
        <template #desc>
            <ol>
                <li>设置参数</li>
                <li>选择特征变量</li>
                <li>点击保存</li>
            </ol>
        </template>

        <template #output>
            <ol>
                <li>本地特征变量的相关系数</li>
                <li>本方与其它方特征变量的相关系数</li>
            </ol>
        </template>
        <template #error-tip />
    </CommonHelp>
</template>

<script>
    import CommonHelp from '../common/CommonHelp';

    export default {
        name:       'VertPearson',
        components: {
            CommonHelp,
        },
    };
</script>
