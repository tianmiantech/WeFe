<template>
    <el-card
        name="流程列表"
        class="nav-title mb30"
        shadow="never"
    >
        <h3 class="mb10 card-title">
            流程列表
            <template v-if="form.isPromoter">
                <el-button
                    v-if="!form.closed && !form.is_exited"
                    class="ml10"
                    type="primary"
                    @click="addFlowMethod"
                >
                    新增流程
                </el-button>
            </template>
            <span v-else class="ml10 f12">(协作方无法添加流程)</span>
        </h3>

        <el-table
            ref="multipleTable"
            max-height="500px"
            :data="list"
            stripe
        >
            <el-table-column
                label="流程"
                min-width="220px"
            >
                <template v-slot="scope">
                    <FlowStatusTag
                        :key="scope.row.updated_time"
                        :status="scope.row.flow_status"
                        :disable-transitions="true"
                        class="mr5"
                    />
                    <router-link :to="{ name: form.project_type === 'DeepLearning' ? 'project-deeplearning-flow' : 'project-flow', query: { flow_id: scope.row.flow_id } }">
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
                v-if="form.isPromoter"
                label="创建者"
                prop="creator_nickname"
            />
            <el-table-column
                label="创建时间"
                width="160px"
            >
                <template v-slot="scope">
                    <p>{{ dateFormat(scope.row.created_time) }}</p>
                </template>
            </el-table-column>
            <el-table-column label="训练类型">
                <template v-slot="scope">
                    <p>{{ learningType(scope.row.federated_learning_type) }}</p>
                </template>
            </el-table-column>
            <el-table-column
                v-if="form.audit_status !== 'disagree'"
                label="操作"
                min-width="200px"
                fixed="right"
            >
                <template v-slot="scope">
                    <router-link
                        class="link mr10"
                        :to="{ name: 'project-flow', query: { flow_id: scope.row.flow_id } }"
                    >
                        查看
                    </router-link>
                    <router-link
                        class="link mr10"
                        :to="{ name: 'project-job-history', query: { project_id, flow_id: scope.row.flow_id }}"
                    >
                        执行记录
                    </router-link>
                    <el-dropdown v-if="scope.row.is_creator">
                        <el-button type="text">
                            更多<i class="el-icon-arrow-down el-icon--right" />
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

        <el-dialog
            title="选择模版:"
            v-model="addFlow"
            destroy-on-close
        >
            <div
                v-loading="loading"
                class="model-list"
            >
                <el-button
                    type="text"
                    class="li empty-flow"
                    @click="createFlow"
                >
                    <span class="model-img pt10">
                        <i class="el-icon-plus" />
                    </span>
                    空白流程
                </el-button>

                <template
                    v-for="item in templateList"
                    :key="item.id"
                >
                    <el-tooltip
                        v-if="item.enname !== 'oot'"
                        :content="item.description"
                        effect="dark"
                    >
                        <el-button
                            class="li"
                            type="text"
                            @click="createFlow($event, item)"
                        >
                            <span class="model-img">
                                <img :src="flowImgs[item.enname]">
                            </span>
                            {{ item.name }}
                            {{ item.desc }}
                        </el-button>
                    </el-tooltip>
                </template>
            </div>
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
                getListApi: '/project/flow/query',
                pagination: {
                    page_index: 1,
                    page_size:  10,
                    total:      0,
                },
                multipleFlow: [],
                templateList: [],
                flowImgs:     {
                    dq:       require('@assets/images/dq.png'),
                    lbsjdqqf: require('@assets/images/lbsjdqqf.png'),
                    ljhg:     require('@assets/images/ljhg.png'),
                    mxxl:     require('@assets/images/mxxl.png'),
                    sjqf:     require('@assets/images/sjqf.png'),
                    sjqg:     require('@assets/images/sjqg.png'),
                    tzsx:     require('@assets/images/tzsx.png'),
                    fx:       require('@assets/images/fx.png'),
                    horz_lr:  require('@assets/images/horz_lr.png'),
                    vert_lr:  require('@assets/images/vert_lr.png'),
                    vert_xgb: require('@assets/images/vert_xgb.png'),
                    horz_xgb: require('@assets/images/horz_xgb.png'),
                },
                flowTimer: null,
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
            this.getTemplateList();
        },
        beforeUnmount() {
            clearTimeout(this.timer);
            clearTimeout(this.flowTimer);
        },
        methods: {
            afterTableRender() {
                clearTimeout(this.timer);

                this.timer = setTimeout(() => {
                    this.getFlowList();
                }, 3000);
            },

            async getFlowList(opt = { resetPagination: false }) {
                if(opt.resetPagination) {
                    this.pagination.page_index = 1;
                }

                const { code, data } = await this.$http.get({
                    url:    this.getListApi,
                    params: {
                        project_id: this.project_id,
                        page_index: this.pagination.page_index - 1,
                        page_size:  this.pagination.page_size,
                    },
                });

                if(code === 0) {
                    this.pagination.total = data.total || 0;
                    if(data.list.length) {
                        this.list = data.list;
                        this.afterTableRender();
                    }
                }
                clearTimeout(this.flowTimer);
                this.flowTimer = setTimeout(() => {
                    this.getFlowList();
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

            async getTemplateList() {
                const { code, data } = await this.$http.get({
                    url: '/project/flow/templates',
                });

                if(code === 0) {
                    this.templateList = data.templates;
                }
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

            async createFlow(event, opt = { name: '', id: '', type: 'MachineLearning' }) {
                if(this.locker) return;
                this.locker = true;

                const now = new Date();
                const hours = now.getHours();
                const minutes = now.getMinutes();
                const seconds = now.getSeconds();
                const params = {
                    project_id:            this.project_id,
                    FederatedLearningType: this.form.project_type === 'DeepLearning' ? 'horizontal' : 'vertical',
                    name:                  `${opt.name || '新流程'}-${hours}:${minutes < 10 ? `0${minutes}` : minutes}:${seconds < 10 ? `0${seconds}` : seconds}`,
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
                this.copyFlowDialog.flowRename = row.flow_name;
                this.copyFlowDialog.sourceFlowId = row.flow_id;
                this.copyFlowDialog.targetProjectId = this.project_id;
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

            addFlowMethod() {
                if (this.form.project_type === 'MachineLearning') {
                    this.addFlow = true;
                } else {
                    // 创建深度学习流程
                    this.createFlow();
                }
            },
        },
    };
</script>

<style lang="scss" scoped>
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
