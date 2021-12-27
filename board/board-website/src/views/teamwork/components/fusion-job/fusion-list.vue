<template>
    <el-card
        name="数据融合"
        class="nav-title mb30"
        shadow="never"
    >
        <h3 class="mb10 card-title">
            数据融合
            <template v-if="form.isPromoter">
                <router-link :to="{ name: 'fusion-edit', query: { project_id: form.project_id } }">
                    <el-button
                        v-if="!form.closed && !form.is_exited"
                        type="primary"
                        class="ml10"
                    >
                        新建数据融合任务
                    </el-button>
                </router-link>
            </template>
            <span v-else class="ml10 f12">(协作方无法创建任务)</span>
        </h3>

        <el-table
            max-height="500px"
            :data="list"
            stripe
        >
            <el-table-column
                label="训练"
                min-width="220px"
            >
                <template v-slot="scope">
                    <router-link :to="{ name: 'fusion-detail', query: { business_id: scope.row.business_id, project_id } }">
                        {{ scope.row.name }}
                    </router-link>
                </template>
            </el-table-column>
            <el-table-column
                label="进度"
                min-width="130px"
            >
                <template v-slot="scope">
                    <el-progress
                        v-if="scope.row.flow_status === 'running' || scope.row.flow_status === 'finished' || scope.row.flow_status === 'stop_on_running' || scope.row.flow_status === 'error_on_running' || scope.row.flow_status === 'success'"
                        :percentage="scope.row.job_progress || 0"
                        :color="customColorMethod"
                    />
                    <template v-else>
                        编辑中
                    </template>
                </template>
            </el-table-column>
            <el-table-column
                label="融合量"
                prop="creator_nickname"
            />
            <el-table-column
                label="创建者"
                prop="creator_nickname"
            />
            <el-table-column
                label="创建时间"
                max-width="160px"
            >
                <template v-slot="scope">
                    <p>{{ dateFormat(scope.row.created_time) }}</p>
                </template>
            </el-table-column>
            <el-table-column
                v-if="form.audit_status !== 'disagree'"
                min-width="200px"
                fixed="right"
                label="操作"
            >
                <template v-slot="scope">
                    <el-button
                        class="mr5"
                        type="text"
                        @click="checkDetail(scope.row.business_id)">
                        查看
                    </el-button>
                </template>
            </el-table-column>
        </el-table>
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
    </el-card>
</template>

<script>
    import table from '@src/mixins/table';

    export default {
        mixins: [table],
        inject: ['refresh'],
        props:  {
            form: Object,
        },
        data() {
            return {
                timer:      null,
                locker:     false,
                loading:    false,
                project_id: '',
                getListApi: '/fusion/task/paging',
                pagination: {
                    page_index: 1,
                    page_size:  10,
                    total:      0,
                },
                flowTimer: null,
            };
        },
        created() {
            this.project_id = this.$route.query.project_id;
            this.getTaskList();
        },
        beforeUnmount() {
            clearTimeout(this.timer);
            clearTimeout(this.flowTimer);
        },
        methods: {
            afterTableRender() {
                clearTimeout(this.timer);

                this.timer = setTimeout(() => {
                    this.getTaskList({
                        requestFromRefresh: true,
                    });
                }, 3000);
            },

            async getTaskList(opt = { resetPagination: false, requestFromRefresh: false }) {
                if(opt.resetPagination) {
                    this.pagination.page_index = 1;
                }

                const { code, data } = await this.$http.get({
                    url:    this.getListApi,
                    params: {
                        'request-from-refresh': opt.requestFromRefresh,
                        project_id:             this.project_id,
                        page_index:             this.pagination.page_index - 1,
                        page_size:              this.pagination.page_size,
                    },
                });

                if(code === 0) {
                    this.pagination.total = data.total || 0;
                    if(data.list.length) {
                        data.list.forEach(item => {
                            item.creator_nickname = item.creator_nickname || item.creator_member_name;
                        });
                        this.list = data.list;
                        this.afterTableRender();
                    }
                }
                clearTimeout(this.flowTimer);
                this.flowTimer = setTimeout(() => {
                    this.getTaskList({ requestFromRefresh: true });
                }, 5000);
            },

            currentPageChange (val) {
                this.pagination.page_index = val;
                this.getTaskList();
            },

            pageSizeChange (val) {
                this.pagination.page_size = val;
                this.getTaskList();
            },

            customColorMethod(percentage) {
                if (percentage < 30) {
                    return '#909399';
                } else if (percentage < 90) {
                    return '#e6a23c';
                } else {
                    return '#67c23a';
                }
            },

            deleteFlow(row, idx) {
                this.$confirm('确定要删除该流程吗? 此操作不可撤销!', '警告', {
                    type: 'warning',
                })
                    .then(async action => {
                        if(action === 'confirm') {
                            const { code } = await this.$http.post({
                                url:  '/project/flow/delete',
                                data: {
                                    flow_id: row.flow_id,
                                },
                            });

                            if(code === 0) {
                                this.list.splice(idx, 1);
                                this.getTaskList({ resetPagination: true });
                            }
                        }
                    });
            },

            checkDetail(business_id) {
                this.$router.replace({
                    name:  'fusion-detail',
                    query: {
                        business_id,
                        project_id: this.project_id,
                    },
                });
            },
        },
    };
</script>

<style lang="scss" scoped>
    .el-alert__description{
        color: $--color-danger;
    }
    h3{margin: 10px;}
    .model-list{
        display: flex;
        justify-content: center;
        flex-wrap: wrap;
    }
    .li{
        margin: 0 20px 10px;
        text-align: center;
        &:hover{
            .model-img{
                transform: scale(1.02);
            }
        }
    }
    .empty-flow{
        .model-img{background: #F5F7FA;}
        .el-icon-plus{
            font-size: 50px;
            color:#DCDFE6;
        }
    }
    .model-img{
        display: block;
        width: 120px;
        height: 120px;
        line-height: 120px;
        margin-bottom: 10px;
        border:1px solid #ebebeb;
    }
    .link{text-decoration: none;}
    .btn-danger{color: #F85564;}
    .el-switch{
        :deep(.el-switch__label){
            color: #999;
            &.is-active{color: $--color-primary;}
        }
    }
</style>
