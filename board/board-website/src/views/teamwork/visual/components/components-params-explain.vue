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
        data() {
            return {
                
            };
        },
        created() {
            console.log(this.componentsName);
        },
        methods: {
            closeExplain() {
                this.$emit('close-explain-pop');
                console.log('closed');
            },
        },
    };
</script>

<style lang="scss" scoped>
.explain-wrap {
    height: 500px;
}
</style>
