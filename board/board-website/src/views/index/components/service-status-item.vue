<template>
    <div v-loading="loading">
        <div :class="status.available ? 'tip tip-success' : 'tip tip-error'">
            <el-row class="item-name">
                <el-col :span="20">
                    <el-icon v-if="!status.available"><elicon-info-filled /></el-icon>
                    <el-icon v-else style="color: #67c23a"><elicon-select /></el-icon>
                    {{ service }} {{ desc }}
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

            <el-collapse class="mt0 pl5">
                <el-collapse-item title="明细情况">
                    <div
                        v-for="item in status.list"
                        :key="item.message"
                        class="f12"
                    >
                        <p>
                            <el-icon v-if="item.success" ><elicon-select /></el-icon>
                            <el-icon v-else><elicon-close /></el-icon>
                            {{item.desc}} <span v-if="item.value">({{item.value}})</span>

                        </p>
                        <p v-if="!item.success" style="color:red">ERROR: {{item.message}}</p>

                    </div>
                </el-collapse-item>
            </el-collapse>

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
        </div>
    </div>
</template>
<script>
    import { mapGetters } from 'vuex';

    export default {
        props: {
            service: String,
            desc:    String,
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
        created() {
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
            .el-icon{
                top:2px;
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

    .el-collapse{
        border:0;
        display: inline-block;
    }
    .el-collapse-item{
        :deep(.el-collapse-item__header) {
            height: 30px;
            line-height: 30px;
            display: inline-block;
            background: none;
            border:0;
        }
        :deep(.el-collapse-item__arrow) {
            margin-left: 10px;
            top: 2px;
        }
        :deep(.el-collapse-item__wrap) {
            background:none;
            border:0;
        }
        :deep(.el-collapse-item__content) {
            padding-bottom:0;
        }
    }
</style>
