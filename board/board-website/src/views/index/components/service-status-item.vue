<template>
    <div v-loading="loading">
        <el-row :gutter="20">
            <el-col :span="24">
                <el-row
                    :class="status.available ? 'tip tip-success' : 'tip tip-error' "
                >
                    <el-col
                        :span="20"
                    >
                        <p class="item-name">
                            <i
                                v-if="status.available"
                                class="el-icon-success"
                                style="color:green"
                            />
                            {{ service }}
                            <el-tooltip
                                class="item"
                                effect="light"
                                placement="right"
                            >
                                <template #content>
                                    <ol v-if="status.list">
                                        <li v-for="item in status.list" :key="item.message" class="service_list">
                                            <p v-if="!item.success" style="color: #f56c6c;">
                                                <span>{{item.desc}}：</span>
                                                <br>
                                                <span>{{item.message}}</span>
                                            </p>
                                            <p v-else>{{item.desc}}</p>
                                        </li>
                                    </ol>
                                </template>
                                <el-icon v-if="!status.available"><elicon-info-filled /></el-icon>
                                <el-icon v-else style="color: #67c23a"><elicon-select /></el-icon>
                            </el-tooltip>
                        </p>
                        <p
                            v-if="status.value"
                            class="item-value"
                        >
                            {{ status.value }}
                        </p>
                        <p
                            v-if="!status.available"
                            :class="status.message ? 'item-message' : ''"
                        >
                            <span v-if="status.error_service_type" style="font-weight: bold">{{status.error_service_type}}:</span> {{ status.message }}
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
                    value:     '',
                    available: null,
                    message:   '',
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
                    url:  '/service/available',
                    data: {
                        member_id:    this.userInfo.member_id,
                        service_type: this.service,
                    },
                });

                if(code === 0) {
                    this.status = data;
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
            display: flex;
            align-items: center;
            i {
                margin-left: 4px;
                cursor: pointer;
                color: #f56c6c;
            }
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
        // align-items: center;
    }

    .tip-success{
        background-color: #f0f9eb;
        border-left: 5px solid #67c23a;
        align-items: center;
    }
    .service_list {
        margin-left: 15px;
    }
</style>
