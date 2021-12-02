<template>
    <div v-loading="loading">
        <el-row :gutter="20">
            <el-col :span="24">
                <el-row
                    :class="status.success ? 'tip tip-success' : 'tip tip-error' "
                >
                    <el-col
                        :span="20"
                    >
                        <p class="item-name">
                            <i
                                v-if="status.success"
                                class="el-icon-success"
                                style="color:green"
                            />
                            <i
                                v-else
                                class="el-icon-error"
                                style="color:red"
                            />

                            {{ status.service }}
                        </p>
                        <p
                            v-if="status.value"
                            class="item-value"
                        >
                            {{ status.value }}
                        </p>
                        <p
                            v-if="!status.success"
                            class="item-message"
                        >
                            {{ status.message }}
                        </p>
                    </el-col>
                    <el-col
                        :span="4"
                        style="text-align:right"
                    >
                        <el-button
                            class="test-btn"
                            @click="check"
                        >
                            Check
                        </el-button>
                    </el-col>
                </el-row>
            </el-col>
        </el-row>
    </div>
</template>
<script>
    import { mapGetters } from 'vuex';

    export default {
        props: {
            service: String,
        },
        data() {
            return {
                loading: false,

                status: {
                    value:   '',
                    success: null,
                    message: '',
                },
            };
        },
        computed: {
            ...mapGetters(['userInfo']),
        },
        async created() {
            this.check();
        },
        methods: {
            async check() {
                this.loading = true;

                // ensure refresh state
                this.status.value = '';
                this.status.message = '';

                const { code, data } = await this.$http.post({
                    url:  '/member/service_status_check',
                    data: {
                        member_id: this.userInfo.member_id,
                        service:   this.service,
                    },
                });

                if(code === 0) {
                    this.status = data.status[this.service];
                }
                this.loading = false;
            },
        },
    };
</script>

<style lang="scss" scoped>
    .tip {
        padding: 8px 8px 8px 16px;
        border-radius: 4px;
        margin-top: 15px;

        .item-name{
            font-size: 18px;
            font-weight: bold;
        }

        .item-value{
            font-size: 14px;
            padding:8px 0;
        }
        .item-message{
            font-size: 14px;
            padding:8px 0;
            color: #f56c6c;
        }
    }

    .tip-error{
        background-color: #fef0f0;
        border-left: 5px solid #f56c6c;
    }

    .tip-success{
        background-color: #f0f9eb;
        border-left: 5px solid #67c23a;
    }
</style>
