<template>
    <div v-loading="loading">
        <json-viewer
            :value="env_info"
            :expand-depth=5
            copyable
            sort
        ></json-viewer>
    </div>

</template>

<script>

    export default {
        props: {

        },
        data() {
            return {
                loading:  false,
                env_info: {},
            };
        },
        mounted() {
            this.load();
        },
        methods: {
            async load() {
                this.loading = true;
                const res = await this.$http.get({
                    url: '/env',
                });

                this.loading = false;
                if(res.code === 0) {
                    this.env_info = res.data;
                }
            },
        },
    };
</script>

<style lang="scss" scoped>
    .jv-container {
        min-height:500px
    }
</style>
