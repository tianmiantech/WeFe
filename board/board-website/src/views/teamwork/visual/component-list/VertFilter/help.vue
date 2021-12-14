<template>
    <CommonHelp>
        <template #intro>
            根据条件筛选样本。
        </template>

        <template #params>
            该组件会对成员的特征按照<span class="strong">指定过滤规则</span>进行筛选
        </template>

        <template #desc>
            <el-form class="flex-form">
                <el-form-item label="案例:">
                    <p class="f12"><span class="color-danger">x1</span>>2<span class="strong">&</span><span class="color-danger">x1</span>&lt;50<span class="strong">&</span><span class="color-danger">x3</span>=100<span class="strong">&</span><span class="color-danger">x5</span>!=30</p>
                </el-form-item>
                <el-form-item label="含义:">
                    <p class="f12">其中 <span class="color-danger">x1, x3, x5</span> 为特征名称, 满足 x1>2 <span class="strong">并且</span> x1&lt;50 <span class="strong">并且</span> x3 = 100 <span class="strong">并且</span> x5!=30 的数据样本保留，其他的样本将被删除</p>
                </el-form-item>
                <el-form-item label="支持的操作符:">
                    <p class="f12">>, &lt;, >=, &lt;=, =, !=</p>
                </el-form-item>
                <el-form-item label="支持的操作符:">
                    <p class="f12">&</p>
                </el-form-item>
                <el-form-item label="注意:">
                    <p class="f12 color-danger">对于同一个特征的操作符 >, &lt; 和 !=, = 不能同时存在</p>
                </el-form-item>
            </el-form>
        </template>

        <template #output>
            数据集过滤报告
        </template>

        <template #error-tip />
    </CommonHelp>
</template>

<script>
    import CommonHelp from '../common/CommonHelp';

    export default {
        name:       'VertFilter',
        components: {
            CommonHelp,
        },
    };
</script>

<style lang="scss" scoped>
    .strong{color:$--color-success;}
    .el-form-item{
        margin-bottom: 0;
        :deep(.el-form-item__label),
        :deep(.el-form-item__content) {
            line-height: 22px;
        }
    }
</style>
