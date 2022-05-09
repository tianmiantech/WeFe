<template>
    <el-card v-loading="loading">
        <el-tabs
            class="mt20"
            type="border-card"
        >
            <el-tab-pane label="环境变量">
                <EnvInfo ref="EnvInfo" />
            </el-tab-pane>
        </el-tabs>


    </el-card>
</template>

<script>
    import EnvInfo from './components/env-info.vue';

    export default {
        components: {
            EnvInfo,
        },
        data() {
            return {
                loading: false,

            };
        },
        computed: {

        },
        created() {

        },
        mounted() {

        },
        methods: {

        },
    };
</script>

<style lang="scss" scoped>

</style>
