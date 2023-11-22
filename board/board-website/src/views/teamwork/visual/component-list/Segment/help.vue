<template>
    <CommonHelp>
        <template #intro>
            将选择后的数据资源按照一定比例进行划分得到 train set、test set，作为后续的算法组件的输入。
        </template>
        <template #params>
            <code>数据切分随机数</code>
            <br>
            切分数据时随机种子
            <br>
            <br>
            <code>训练与验证数据比例</code>
            <br>
            设置范围支持  0% - 100%，但不包含 0% 或100%
        </template>

        <template #desc>
            <ol>
                <li>手动点击按钮，生成数据切分随机数（可选）</li>
                <li>调整训练与验证数据比例</li>
                <li>点击保存</li>
            </ol>
        </template>

        <template #output>
            输出训练集和测试集
        </template>

        <template #error-tip />
    </CommonHelp>
</template>

<script>
    import CommonHelp from '../common/CommonHelp';

    export default {
        name:       'Segment',
        components: {
            CommonHelp,
        },
    };
</script>
