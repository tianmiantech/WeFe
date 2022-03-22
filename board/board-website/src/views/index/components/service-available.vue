<template>
    <el-card v-loading="loading">

        <template #header>
            <div
                class="test-btn"
            >
                {{ serviceType }} {{ available ? '可用' : '不可用' }}
            </div>
        </template>

        <p v-if="message">查询可用性失败：{{ message }}</p>
        <ul>
            <li
                v-for="(item, index) in list" :key="index"
                :class="item.success ? 'tip tip-success' : 'tip tip-error' "
            >
                <p class="item-name">
                    <el-icon
                        v-if="item.success"
                        class="el-icon-success"
                        style="color:green;"
                    >
                        <elicon-success-filled />
                    </el-icon>
                    <el-icon
                        v-else
                        class="el-icon-error"
                        style="color:red;"
                    >
                        <elicon-circle-close-filled />
                    </el-icon>

                    {{ item.desc }}
                </p>
                <p
                    v-if="item.value"
                    class="item-value"
                >
                    当前配置：{{ item.value }}
                </p>
                <p
                    v-if="!item.success"
                    class="item-message"
                >
                    {{ item.message }}
                </p>
            </li>
        </ul>
    </el-card>
</template>
<script>
    import { mapGetters } from 'vuex';

    export default {
        props: {
            serviceType: String,
        },
        data() {
            return {
                loading:   false,
                available: false,
                message:   '',
                list:      [],
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
                this.list = [];
                this.available = false;

                const { code, data } = await this.$http.post({
                    url:  '/server/available',
                    data: {
                        requestFromRefresh: true,
                        serviceType:        this.serviceType,
                    },
                });

                if(code === 0) {
                    this.available = data.available;
                    this.message = data.message;
                    this.list = data.list;
                }
                this.loading = false;
            },
        },
    };
</script>

<style lang="scss" scoped>
    .tip {
        font-size: 12px;
        padding: 3px 3px 3px 8px;
        border-radius: 4px;
        margin-top: 8px;

        .item-name{
            font-size: 14px;
            font-weight: bold;
        }

        .item-value{
            padding:3px 0;
        }
        .item-message{
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
