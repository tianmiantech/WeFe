<template>
    <div
        v-loading="loading"
        class="project-wrap"
    >
        <div :class="['project-list', { space: list.length }]">
            <div
                v-if="tabName === 'allProjects' && pagination.page_index === 1 && list.length"
                class="li create-button"
            >
                <router-link
                    class="add-wrap"
                    :to="{ name: 'project-create' }"
                >
                    <i class="icon-add" />
                    创建项目
                </router-link>
                <div class="guide" @click="showGuideVideo">
                    新手指引
                    <i class="ml10 el-icon-video-play" />
                </div>
            </div>
            <template v-if="list.length">
                <router-link
                    v-for="item in list"
                    :key="item.project_id"
                    :to="{name: 'project-detail', query: { project_id: item.project_id }}"
                    class="li"
                >
                    <p class="p-name">
                        {{ item.name }}
                        <el-tooltip
                            :content="'项目简介：' + item.project_desc"
                            placement="top"
                            effect="light"
                        >
                            <i
                                class="el-icon-info desc-icon"
                            />
                        </el-tooltip>
                    </p>
                    <p class="p-id">
                        {{ item.project_id }}
                    </p>
                    <div class="parters">
                        <p
                            v-for="member in item.member_list"
                            :key="member.member_id + member.member_role"
                            class="parters-item"
                        >
                            <i
                                :class="['parters-icon', item.member_id === member.member_id ? 'el-icon-star-on' : 'el-icon-star-off', {'parters-icon-promoter': member.member_role === 'promoter'}]"
                            />
                            {{ member.member_name }}
                        </p>
                    </div>
                    <p
                        v-if="item.closed"
                        class="f14 color-danger"
                    >
                        该项目已由 {{ item.close_operator_nickname }} ({{ item.closed_by }}) 于 {{ dateFormat(item.closed_time) }} 关闭
                    </p>
                    <div
                        v-else-if="item.audit_status !== 'agree'"
                        class="data-status"
                    >
                        {{ status[item.audit_status] }}
                    </div>
                    <p
                        v-else
                        class="p-time"
                    >
                        创建于 {{ dateFormat(item.created_time) }}
                    </p>
                    <div
                        v-if="item.flow_status_statistics"
                        class="flow-list"
                    >
                        <div class="flow-status">
                            <p class="status-num">{{ item.flow_status_statistics.editing }}</p>
                            流程配置中
                        </div>
                        <div class="flow-status">
                            <p class="status-num">{{ item.flow_status_statistics.running }}</p>
                            流程执行中
                        </div>
                        <div class="flow-status">
                            <p class="status-num">{{ item.flow_status_statistics.finished }}</p>
                            流程已完成
                        </div>
                    </div>
                </router-link>
            </template>
            <EmptyData v-else />
        </div>
        <div
            v-if="pagination.total"
            class="mt20 text-r"
        >
            <el-pagination
                :total="pagination.total"
                :page-size="pagination.page_size"
                :page-sizes="[10, 20, 30, 40, 50]"
                :current-page="pagination.page_index"
                layout="total, sizes, prev, pager, next, jumper"
                @current-change="currentPageChange"
                @size-change="pageSizeChange"
            />
        </div>
    </div>
</template>

<script>
    import { mapGetters } from 'vuex';
    import table from '@src/mixins/table.js';

    export default {
        mixins: [table],
        props:  {
            tabName: {
                type:    String,
                default: '',
            },
            searchKey: Object,
            myRole:    String,
        },
        data() {
            return {
                getListApi: '/project/query',
                status:     {
                    created:  '已创建',
                    auditing: '等待审核中',
                },
                watchRoute: false, // When there are multiple instances, multiple requests will be issued at the same time, set to false, let the parent component listen for routing changes
            };
        },
        computed: {
            ...mapGetters(['userInfo']),
        },
        methods: {
            showGuideVideo() {
                this.$bus.$emit('show-guide-video');
            },

            async searchList(opt = { to: true, resetPagination: true }) {
                // get url params first
                this.pagination.page_index = +this.$route.query.page_index || 1;
                this.pagination.page_size = +this.$route.query.page_size || 20;
                this.search = {
                    ...this.searchKey,
                    my_role: this.myRole,
                };
                // reset pagination
                if(opt.resetPagination) {
                    this.pagination.page_index = 1;
                    this.pagination.page_size = 20;
                }
                await this.getList(opt);
                this.list.forEach(item => {
                    item.$promoter_list = [];
                    item.member_list.map(i => {
                        if (i.member_role === 'promoter') {
                            item.$promoter_list.push(i);
                        }
                    });
                });
            },
        },
    };
</script>

<style lang="scss">
    .side-collapsed{
        .project-list{
            @media screen and (min-width: 1200px) and (max-width: 1387px) {
                .li{flex: 45%;
                    min-width: initial;
                    max-width: initial;
                }
            }
        }
    }
</style>

<style lang="scss" scoped>
    .empty-data{flex:1;}
    .project-list{
        display: flex;
        flex-wrap: wrap;
        min-height:200px;
        justify-content: space-between;
    }
    .space {
        &:after {
            content: '';
            width: 32.2%;
        }
    }
    .li{
        flex: 1;
        margin-left: 20px;
        min-height: 220px;
        min-width: 350px;
        max-width: 400px;
        line-height: 30px;
        padding: 20px;
        font-size: 16px;
        color: #303133;
        margin-bottom: 20px;
        position: relative;
        border-radius: 4px;
        text-decoration: none;
        transition-duration:.3s;
        border:1px solid #DCDFE6;
        box-shadow: 0 6px 10px rgba(0, 0, 0, 0.15);
        &:hover{
            box-shadow: 0 6px 10px -6px rgba(0, 0, 0, 0.1);
            .el-icon-delete{display: block;}
        }
    }
    @media screen and (min-width: 1000px) and (max-width: 1387px) {
        .li{flex: 45%;
            min-width: 400px;
            max-width: 500px;
        }
    }
    .create-button{
        padding:0;
        cursor: default;
        .guide{
            height: 62px;
            line-height: 62px;
            padding-left: 20px;
            display: inline-block;
            cursor: pointer;
        }
        .el-icon-video-play{
            color: $color-link-base;
            cursor: pointer;
        }
    }
    .add-wrap{
        display: block;
        color: #303133;
        height: 160px;
        line-height: 30px;
        text-align: center;
        background: #F5F7FA;
        border-bottom: 1px solid #DCDFE6;
        padding-top: 10px;
        .icon-add{
            display: block;
            position: relative;
            width: 60px;
            height:60px;
            margin:20px auto 15px;
            cursor: pointer;
            &:before,
            &:after{
                content:'';
                position: absolute;
                background: #DCDFE6;
            }
            &:before{
                top:0;
                left: 28px;
                width: 3px;
                height:100%;
            }
            &:after{
                left:0;
                top: 28px;
                width:100%;
                height: 3px;
            }
        }
    }
    .flow-list{
        display: flex;
        justify-content: space-between;
        border-top: 1px solid #F2F6FC;
        padding-top: 8px;
        text-align: center;
        line-height: 20px;
        font-size: 28px;
    }
    .flow-status{
        font-size: 12px;
        color:#909399;
    }
    .status-num{
        font-weight: bold;
        color: #7ab8cc;
        font-size: 20px;
    }
    .p-name, .parters{
        overflow: hidden;
        white-space: nowrap;
        text-overflow: ellipsis;
    }
    .p-name{
        height:30px;
        font-size: 16px;
        margin-bottom: 5px;
        font-weight: bold;
        color:#438BFF;
        .desc-icon{
            font-size: 12px;
            color: #999;
        }
    }
    .parters{
        color:#333;
        height: 55px;
        font-size: 13px;
        overflow: hidden;
        white-space: nowrap;
        word-break: break-all;
        text-overflow: ellipsis;
        vertical-align: middle;
    }
    .parters-icon{color: #3182bd;}
    .parters-icon-promoter{color: #E89B00;}
    .parters-item{
        width: 50%;
        float: left;
        line-height: 25px;
        overflow: hidden;
        text-overflow: ellipsis;
        -webkit-box-orient: vertical;
    }
    .data-status{flex:1;
        text-align: center;
        color: #E6A23C;
        font-size: 14px;
    }
    .p-time{
        color:#C0C4CC;
        font-size: 12px;
        line-height: 25px;
    }
    .el-icon-delete{
        display: none;
        position: absolute;
        right: 20px;
        bottom: 20px;
        color:$--color-danger;
    }
</style>
