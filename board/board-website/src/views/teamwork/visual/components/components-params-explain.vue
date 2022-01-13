<template>
    <div class="explain-wrap">
        参数说明 {{componentsName}}
        <el-button @click="closeExplain">关闭</el-button>
    </div>
</template>

<script>
    export default {
        props: {
            componentsName: String,
        },
        methods: {
            closeExplain() {
                this.$emit('close-explain-pop');
            },
        },
    };
</script>

<style lang="scss" scoped>
    .explain-wrap {height: 500px;}
</style>
