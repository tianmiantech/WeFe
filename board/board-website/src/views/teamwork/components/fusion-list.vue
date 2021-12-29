<template>
    <el-card
        name="数据融合"
        class="nav-title mb30"
        shadow="never"
    >
        <h3 class="mb10 card-title">
            数据融合
            <template v-if="form.isPromoter">
                <router-link :to="{ name: 'fusion-add' }">
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
                    <FlowStatusTag
                        v-if="form.project_type === 'MachineLearning'"
                        :key="scope.row.updated_time"
                        :status="scope.row.flow_status"
                        :disable-transitions="true"
                        class="mr5"
                    />
                    <router-link :to="{ name: 'project-flow', query: { flow_id: scope.row.flow_id } }">
                        {{ scope.row.flow_name }}
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
            <el-table-column v-if="form.project_type === 'MachineLearning'" label="训练类型">
                <template v-slot="scope">
                    <p>{{ learningType(scope.row.federated_learning_type) }}</p>
                </template>
            </el-table-column>
            <el-table-column
                v-if="form.audit_status !== 'disagree'"
                min-width="200px"
                fixed="right"
                label="操作"
            >
                <template v-slot="scope">
                    <el-button type="text" @click="checkDetail(scope.row.flow_id)" style="margin-right: 4px;">
                        查看
                    </el-button>
                    <router-link
                        v-if="form.project_type !== 'DeepLearning'"
                        class="link mr10"
                        :to="{ name: 'project-job-history', query: { project_id, flow_id: scope.row.flow_id }}"
                    >
                        执行记录
                    </router-link>
                    <el-dropdown v-if="scope.row.is_creator">
                        <el-button type="text">
                            更多
                            <el-icon>
                                <elicon-arrow-down />
                            </el-icon>
                        </el-button>
                        <template #dropdown>
                            <el-dropdown-menu>
                                <el-dropdown-item>
                                    <el-button
                                        type="text"
                                        @click="copyFlow(scope.row)"
                                    >
                                        复制流程
                                    </el-button>
                                </el-dropdown-item>
                                <el-dropdown-item divided>
                                    <el-button
                                        type="text"
                                        class="btn-danger"
                                        @click="deleteFlow(scope.row, scope.$index)"
                                    >
                                        删除流程
                                    </el-button>
                                </el-dropdown-item>
                            </el-dropdown-menu>
                        </template>
                    </el-dropdown>
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

        <el-dialog
            title="复制流程:"
            v-model="copyFlowDialog.visible"
            destroy-on-close
            width="400px"
        >
            <el-form>
                <el-form-item
                    label="选择目标项目："
                    label-width="100px"
                >
                    <el-switch
                        v-model="thisProject"
                        active-text="当前项目"
                        inactive-text="其他项目"
                        @change="changeProject"
                    />
                </el-form-item>
                <el-form-item
                    v-if="!thisProject"
                    label="选择目标项目："
                    label-width="100px"
                >
                    <el-autocomplete
                        v-model="copyFlowDialog.targetProject"
                        :fetch-suggestions="searchProject"
                        placeholder="请输入关键词查询"
                        style="width:260px;"
                        @select="selectCheck"
                    >
                        <template v-slot="scope">
                            <strong>{{ scope.item.name }}</strong> <span class="f12">{{ scope.item.project_id }}</span>
                        </template>
                    </el-autocomplete>
                </el-form-item>
                <el-form-item
                    v-if="!thisProject"
                    label="目标项目 ID："
                    label-width="100px"
                >
                    <el-input
                        v-model="copyFlowDialog.targetProjectId"
                        disabled
                    />
                </el-form-item>
                <el-form-item
                    label="新流程名称："
                    label-width="100px"
                >
                    <el-input v-model="copyFlowDialog.flowRename" />
                </el-form-item>
                <div class="mt20 text-r">
                    <el-button @click="copyFlowDialog.visible=false">
                        取消
                    </el-button>
                    <el-button
                        type="primary"
                        @click="copyConfirm"
                    >
                        确定
                    </el-button>
                </div>
            </el-form>
        </el-dialog>
    </el-card>
</template>

<script>
    import table from '@src/mixins/table';
    import FlowStatusTag from '@src/components/views/flow-status-tag';

    export default {
        components: {
            FlowStatusTag,
        },
        mixins: [table],
        inject: ['refresh'],
        props:  {
            form: Object,
        },
        data() {
            return {
                timer:          null,
                locker:         false,
                loading:        false,
                project_id:     '',
                thisProject:    true,
                disabled:       true,
                addFlow:        false,
                copyFlowDialog: {
                    visible:         false,
                    sourceFlowId:    '',
                    targetProject:   '',
                    targetProjectId: '',
                    flowRename:      '',
                },
                getListApi: '/project/fusion/query',
                pagination: {
                    page_index: 1,
                    page_size:  10,
                    total:      0,
                },
                multipleFlow: [],
                flowTimer:    null,
            };
        },
        computed: {
            learningType() {
                return function (val) {
                    const types = {
                        vertical:   '纵向',
                        horizontal: '横向',
                        mix:        '混合',
                    };

                    return types[val] || '-';
                };
            },
        },
        created() {
            this.project_id = this.$route.query.project_id;
            this.getFlowList();
        },
        beforeUnmount() {
            clearTimeout(this.timer);
            clearTimeout(this.flowTimer);
        },
        methods: {
            afterTableRender() {
                clearTimeout(this.timer);

                this.timer = setTimeout(() => {
                    this.getFlowList({
                        requestFromRefresh: true,
                    });
                }, 3000);
            },

            async getFlowList(opt = { resetPagination: false, requestFromRefresh: false }) {
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
                    this.getFlowList({ requestFromRefresh: true });
                }, 5000);
            },

            currentPageChange (val) {
                this.pagination.page_index = val;
                this.getFlowList();
            },

            pageSizeChange (val) {
                this.pagination.page_size = val;
                this.getFlowList();
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

            getDateTime() {
                const now = new Date();
                const hours = now.getHours();
                const minutes = now.getMinutes();
                const seconds = now.getSeconds();

                return `${hours}:${minutes < 10 ? `0${minutes}` : minutes}:${seconds < 10 ? `0${seconds}` : seconds}`;
            },

            async createFlow(event, opt = { federated_learning_type: '', name: '', id: '' }) {
                if(this.locker) return;
                this.locker = true;

                const params = {
                    project_id:            this.project_id,
                    federatedLearningType: this.form.project_type === 'DeepLearning' ? 'horizontal' : opt.federated_learning_type,
                    name:                  `${opt.name || '新流程'}-${this.getDateTime()}`,
                    desc:                  '',
                };

                if(opt.id) {
                    params.templateId = opt.id;
                }

                // add loading after 1s
                setTimeout(() => {
                    if(this.locker) {
                        this.loading = true;
                    }
                }, 1000);

                const { code, data } = await this.$http.post({
                    url:  '/project/flow/add',
                    data: params,
                });

                this.locker = false;
                this.loading = false;
                if(code === 0) {
                    this.$router.push({
                        name:  this.form.project_type === 'DeepLearning' ? 'project-deeplearning-flow' : 'project-flow',
                        query: {
                            flow_id: data.flow_id,
                        },
                    });
                }
            },

            changeProject(value) {
                this.thisProject = value;

                if(value) {
                    this.copyFlowDialog.targetProjectId = this.project_id;
                } else {
                    this.copyFlowDialog.targetProject = '';
                    this.copyFlowDialog.targetProjectId = '';
                }
            },

            async searchProject(name, cb) {
                this.copyFlowDialog.targetProjectId = '';
                const { code, data } = await this.$http.get({
                    url:    '/project/query',
                    params: {
                        name,
                        myRole: 'promoter',
                    },
                });

                if(code === 0) {
                    cb(data.list);
                }
            },

            selectCheck(item) {
                this.copyFlowDialog.targetProject = item.name;
                this.copyFlowDialog.targetProjectId = item.project_id;
            },

            copyFlow(row) {
                this.thisProject = true;
                this.copyFlowDialog.visible = true;
                this.copyFlowDialog.sourceFlowId = row.flow_id;
                this.copyFlowDialog.targetProjectId = this.project_id;
                this.copyFlowDialog.flowRename = `${row.flow_name}-${this.getDateTime()}`;
                this.copyFlowDialog.targetProject = '';
            },

            async copyConfirm(event) {
                if(!this.copyFlowDialog.targetProjectId) {
                    return this.$message.error('项目不存在! 请重新选择');
                }

                const { code } = await this.$http.post({
                    url:  '/project/flow/copy',
                    data: {
                        targetProjectId: this.copyFlowDialog.targetProjectId,
                        sourceFlowId:    this.copyFlowDialog.sourceFlowId,
                        flowRename:      this.copyFlowDialog.flowRename,
                    },
                    btnState: {
                        target: event,
                    },
                });

                if(code === 0) {
                    this.$message.success('复制成功!');
                    this.copyFlowDialog.visible = false;
                    this.refresh();
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
                                this.getFlowList({ resetPagination: true });
                            }
                        }
                    });
            },

            checkDetail(flow_id) {
                this.$router.replace({
                    name:  'project-flow',
                    query: { flow_id },
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
