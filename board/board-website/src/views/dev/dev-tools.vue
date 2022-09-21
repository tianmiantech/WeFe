<template>
    <el-card v-loading="loading">
        <el-tabs
            class="mt20"
            type="border-card"
        >
            <el-tab-pane label="环境变量">
                <EnvInfo ref="EnvInfo" />
            </el-tab-pane>
            <el-tab-pane label="异常日志">
                <LogException ref="LogException" />
            </el-tab-pane>
        </el-tabs>


    </el-card>
</template>

<script>
    import EnvInfo from './components/env-info.vue';
    import LogException from './components/log-exception.vue';

    export default {
        components: {
            EnvInfo,
            LogException,
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
