<template>
    <div class="common-help">
        <h3>组件介绍</h3>
        <slot name="intro" />

        <h3>组件参数说明</h3>
        <slot name="params" />

        <h3>组件使用方法</h3>
        <slot name="desc" />

        <h3>组件输出</h3>
        <slot name="output" />

        <h3>常见错误操作</h3>
        <slot name="error-tip" />
    </div>
</template>

<style lang="scss" scoped>
    .common-help{
        font-size: 14px;
        color: #5e6d82;
        line-height: 1.5em;
        padding-bottom:30px;

        h3{
            color: rgb(15,123,108);
            font-weight: bold;
            padding-top: 30px;
            padding-bottom: 5px;
        }
        h3:first-of-type{
            padding-top: 0px;
        }
        :deep(ol,ul){
            margin-left: 26px;

            li{padding-bottom: 3px;}
        }
        code {
            color: #EB5757;
            background: rgba(135, 131, 120, 0.15);
            border-radius: 3px;
            padding: 0.2em 0.4em;
            border-radius: 3px;
            tab-size: 2;
        }
    }
</style>
