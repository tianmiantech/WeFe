<template>
    <el-card>
        <template #header>
            <div style="line-height:32px;">
                服务状态
            </div>
        </template>
        <ServiceStatusItem service="union" />
        <ServiceStatusItem service="gateway" />
        <ServiceStatusItem service="storage" />
        <ServiceStatusItem service="flow" />
    </el-card>
</template>

<script>
    import ServiceStatusItem from './service-status-item.vue';

    export default {
        components: {
            ServiceStatusItem,
        },
        data() {
            return {
                loading: false,

                union: {
                    value:   '',
                    success: null,
                    message: '',
                },
            };
        },
        methods: {
            async checkAll() {
                this.loading = true;

                const { code, data } = await this.$http.post({
                    url:  '/member/service_status_check',
                    data: {
                        requestFromRefresh: true,
                        member_id:          this.userInfo.member_id,
                    },
                });

                if(code === 0) {
                    this.union = data.status.union;
                }
                this.loading = false;
            },
        },
    };
</script>

<style lang="scss" scoped>
.el-card{
    :deep(.el-card__body) {padding-top: 0px;}
}
</style>
