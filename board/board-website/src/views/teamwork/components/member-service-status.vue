<template>
    <template v-if="onlyIcon">
        <span
            v-if="status.available === null"
            class="status_waiting"
        >
            <el-icon class="is-loading">
                <elicon-loading />
            </el-icon>
        </span>
        <i
            v-else-if="status.available"
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
            v-if="status.available === null"
            class="status_waiting"
        >
            <el-icon class="is-loading">
                <elicon-loading />
            </el-icon>
        </span>

        <el-tooltip
            v-else-if="status.available"
            placement="top"
        >
            <template #content>
                <div
                    v-for="item in status.details"
                    :key="item.service"
                    class="member-service-item"
                >
                    <el-tag
                        size="mini"
                        effect="dark"
                        :type="item.available ? 'success' : 'danger'"
                    >
                        {{ item.service }}
                    </el-tag> : {{ item.available ? "正常" : item.message }}
                    <ol v-if="item.list" class="service_list mt10">
                        <li v-for="sitem in item.list" :key="sitem.message">
                            <p v-if="!sitem.success" style="color: #f56c6c;">
                                <span>{{sitem.desc}}：</span>
                                <span>{{sitem.message}}</span>
                            </p>
                            <p v-else>{{sitem.desc}}</p>
                        </li>
                    </ol>
                </div>

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
                <div
                    v-for="item in status.details"
                    :key="item.service"
                    class="member-service-item"
                >
                    <el-tag
                        size="mini"
                        effect="dark"
                        :type="item.available ? 'success' : 'danger'"
                    >
                        {{ item.service }}
                    </el-tag> : {{ item.available ? "正常" : item.message }}
                    <ol v-if="item.list" class="service_list mt10">
                        <li v-for="sitem in item.list" :key="sitem.message">
                            <p v-if="!sitem.success" style="color: #f56c6c;">
                                <span>{{sitem.desc}}：</span>
                            </p>
                            <p v-else>{{sitem.desc}}</p>
                        </li>
                    </ol>
                </div>
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
        height:20px;
        font-size: 12px;
        display: inline-block;
        vertical-align:top;
        cursor: pointer;
    }
    .member-service-item{
        margin-top: 5px;
        .el-tag{
            width: 95px;
            text-align: center;
        }
    }
    .icon-service-error{color: $--color-danger;}
    .icon-service-ok{color: $--color-success;}
    .service_list {
        margin-left: 20px;
    }
</style>
