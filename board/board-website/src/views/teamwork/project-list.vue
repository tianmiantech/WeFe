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
                <!-- <div class="guide" @click="showGuideVideo">
                    新手指引
                    <i class="ml10 board-icon-video-play" />
                </div> -->
            </div>
            <template v-if="list.length">
                <router-link
                    v-for="(item, index) in list"
                    :key="item.project_id"
                    :to="{name: 'project-detail', query: { project_id: item.project_id, project_type: item.project_type }}"
                    class="li"
                >
                    <p v-if="item.project_type" class="project_type" :style="{color: item.project_type === 'DeepLearning' ? '#E89B00' : '#438BFF'}">{{item.project_type === 'DeepLearning' ? '视觉处理' : item.project_type === 'MachineLearning' ? '机器学习' : ''}} <span v-if="item.top" style="font-size: 12px;color: #f85564;">(已置顶)</span></p>
                    <p v-if="userInfo.admin_role" class="top_btn" @click.prevent="toTopClick(item)">
                        <el-tooltip v-if="item.top" effect="light" content="取消置顶" placement="bottom">
                            <el-icon style="color: #f85564;"><elicon-bottom /></el-icon>
                        </el-tooltip>
                        <el-tooltip v-if="(index !== 0 && !item.top) || (index === 0 && !item.top)" effect="light" content="置顶" placement="bottom">
                            <el-icon style="color: #438bff;"><elicon-top /></el-icon>
                        </el-tooltip>
                    </p>
                    <p class="p-name">
                        {{ item.name }}
                        <el-tooltip
                            :content="'项目简介：' + item.project_desc"
                            placement="top"
                            effect="light"
                        >
                            <el-icon class="board-icon-info desc-icon">
                                <elicon-info-filled />
                            </el-icon>
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
                            <el-icon :class="['parters-icon', {'parters-icon-promoter': member.member_role === 'promoter'}]">
                                <elicon-star-filled v-if="item.member_id === member.member_id" class="parters-icon-star" />
                                <elicon-star v-else />
                            </el-icon>
                            {{ member.member_name }}
                        </p>
                    </div>
                    <p
                        v-if="item.closed"
                        class="project-closed f12 color-danger"
                    >
                        该项目已由 {{ item.close_operator_nickname }} 于 {{ dateFormat(item.closed_time) }} 关闭
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
                        class="flow-list mt10"
                    >
                        <div class="flow-status">
                            <p class="status-num">{{ item.flow_status_statistics.editing || 0 }}</p>
                            流程配置中
                        </div>
                        <div class="flow-status">
                            <p class="status-num">{{ item.flow_status_statistics.running || 0 }}</p>
                            流程执行中
                        </div>
                        <div class="flow-status">
                            <p class="status-num">{{ item.flow_status_statistics.interrupted || 0 }}</p>
                            流程中断
                        </div>
                        <div class="flow-status">
                            <p class="status-num">{{ item.flow_status_statistics.success || 0 }}</p>
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
                    disagree: '已拒绝加入',
                    closed:   '已关闭',
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

            async toTopClick(item) {
                const { code } = await this.$http.post({
                    url:  '/project/top',
                    data: {
                        projectId: item.project_id,
                        top:       !item.top,
                    },
                });

                if(code === 0) {
                    this.getList();
                }
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
        position: relative;
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
            .board-icon-delete{display: block;}
        }
        .project_type {
            position: absolute;
            top: 0;
            right: 0;
            height: 26px;
            line-height: 26px;
            font-size: 14px;
            background: #f5f5f5;
            padding-right: 5px;
            border-radius: 0 3px 0 0;
            &::before {
                position: absolute;
                left: -16px;
                content: '';
                height: 0;
                width: 0;
                border-top: 13px solid transparent;
                border-right: 16px solid #f5f5f5;
                border-bottom: 13px solid transparent;
            }

        }
        .top_btn {
            position: absolute;
            top: 26px;
            right: 0;
            padding-right: 5px;
            font-size: 16px;
            color: #666;
        }
    }
    @media screen and (min-width: 1000px) and (max-width: 1387px) {
        .li{flex: 45%;
            min-width: 400px;
            max-width: 500px;
        }
    }
    .create-button{
        cursor: default;
        background: #f5f7fa;
        .guide{
            height: 62px;
            line-height: 62px;
            padding-left: 20px;
            display: inline-block;
            cursor: pointer;
        }
        .board-icon-video-play{
            color: $color-link-base;
            cursor: pointer;
        }
    }
    .add-wrap{
        display: block;
        color: #303133;
        // height: 160px;
        height: 100%;
        line-height: 30px;
        text-align: center;
        background: #F5F7FA;
        // border-bottom: 1px solid #DCDFE6;
        padding-top: 10px;
        .icon-add{
            display: block;
            position: relative;
            width: 60px;
            height:60px;
            // margin:20px auto 15px;
            margin:50px auto 20px;
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
        font-size: 13px;
        max-height: 50px;
        overflow: hidden;
        white-space: nowrap;
        word-break: break-all;
        text-overflow: ellipsis;
        vertical-align: middle;
    }
    .parters-icon{
        color: #3182bd;
        font-size: 14px;
        vertical-align: top;
        top:4px;
    }
    .parters-icon-promoter{
        color: #E89B00;
        font-size: 16px;
        top: 3px;
        left:-1px;
        .parters-icon-star{
            font-size: 18px;
            position: relative;
            top: -1px;
        }
    }
    .parters-item{
        width: 50%;
        float: left;
        line-height: 25px;
        overflow: hidden;
        text-overflow: ellipsis;
        -webkit-box-orient: vertical;
    }
    .project-closed{
        line-height: 18px;
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
    .board-icon-delete{
        display: none;
        position: absolute;
        right: 20px;
        bottom: 20px;
        color:$--color-danger;
    }
</style>
