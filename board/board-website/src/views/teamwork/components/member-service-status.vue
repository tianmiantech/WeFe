<template>
    <template v-if="onlyIcon">
        <span
            v-if="status.all_status_is_success === null"
            class="status_waiting"
        >
            <i class="el-icon-loading" />
        </span>
        <i
            v-else-if="status.all_status_is_success"
            class="iconfont icon-service-ok"
            title="服务正常"
            @click="recheck"
        />
        <i
            v-else
            title="服务异常"
            class="iconfont icon-service-error"
            @click="recheck"
        />
    </template>
    <template v-else>
        <span
            v-if="status.all_status_is_success === null"
            class="status_waiting"
        >
            <i class="el-icon-loading" />
        </span>

        <el-tooltip
            v-else-if="status.all_status_is_success"
            placement="top"
        >
            <template #content>
                <p
                    v-for="item in status.status"
                    :key="item.service"
                    class="member-service-item"
                >
                    <el-tag
                        size="mini"
                        effect="dark"
                        :type="item.success ? 'success' : 'danger'"
                    >
                        {{ item.service }}
                    </el-tag>
                    : {{ item.success ? "正常" : item.message }}
                </p>
            </template>
            <i
                class="iconfont icon-service-ok"
                title="服务正常"
                @click="recheck"
            >
                &nbsp;服务正常
            </i>
        </el-tooltip>
        <el-tooltip
            v-else
            placement="top"
        >
            <template #content>
                <p
                    v-for="item in status.status"
                    :key="item.service"
                    class="member-service-item"
                >
                    <el-tag
                        size="mini"
                        effect="dark"
                        :type="item.success ? 'success' : 'danger'"
                    >
                        {{ item.service }}
                    </el-tag>
                    : {{ item.success ? "正常" : item.message }}
                </p>
            </template>
            <i
                title="服务异常"
                class="iconfont icon-service-error"
                @click="recheck"
            >
                &nbsp;服务异常
            </i>
        </el-tooltip>
    </template>
</template>

<script>
    export default {
        props: {
            status:   Object,
            onlyIcon: Boolean,
        },
        methods: {
            recheck() {
                this.$bus.$emit('check-service-status');
            },
        },
    };
</script>

<style lang="scss" scoped>
    .status_waiting,
    .icon-service-error,
    .icon-service-ok{
        height:32px;
        font-size: 12px;
        display: inline-block;
        vertical-align:top;
        cursor: pointer;
    }
    .status_waiting{
        position: relative;
        top:2px;
    }
    .member-service-item{
        margin-top: 5px;
        .el-tag{
            width: 60px;
            text-align: center;
        }
    }
    .icon-service-error{color: $--color-danger;}
    .icon-service-ok{color: $--color-success;}
</style>
