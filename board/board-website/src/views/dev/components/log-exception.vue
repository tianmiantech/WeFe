<template>
    <div v-loading="loading">
        <el-button
            type="primary"
            @click="load()"
        >
            刷新
        </el-button>
        <el-button
            type="info"
            @click="copy()"
        >
            复制
        </el-button>
        <pre class="log-textarea">{{exception}}</pre>
    </div>

</template>

<script>

    export default {
        props: {

        },
        data() {
            return {
                loading:   true,
                exception: '',
            };
        },
        mounted() {
            this.load();
        },
        methods: {
            async load() {
                this.loading = true;
                const res = await this.$http.get({
                    url: '/log_file/find_exception',

                });

                this.loading = false;
                if(res.code === 0) {
                    this.exception = res.data.log;
                }
            },
            copy() {
                const input = document.createElement('input');

                input.value = this.exception.replace('\r\n','\n');
                document.body.appendChild(input);
                input.select();
                document.execCommand('Copy');
                document.body.removeChild(input);
                this.$message.success('复制成功！');
            },
        },
    };
</script>

<style lang="scss" scoped>
.log-textarea{
    font-size: 13px;
    font-family: serif,revert,monospace;
    overflow-x: scroll;
}
</style>
