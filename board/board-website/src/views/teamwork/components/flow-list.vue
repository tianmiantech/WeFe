<template>
    <el-card
        name="训练列表"
        class="nav-title mb30"
        shadow="never"
        :idx="sortIndex"
    >
        <template #header>
            <div class="clearfix mb10" style="display: flex; justify-content: space-between;">
                <div style="display: flex; align-items: center;">
                    <h3 class="mb10 card-title">
                        训练列表
                        <template v-if="form.isPromoter">
                            <el-button
                                v-if="!form.closed && !form.is_exited && form.is_project_admin"
                                class="ml10"
                                size="small"
                                type="primary"
                                @click="addFlowMethod"
                            >
                                新建训练
                            </el-button>
                        </template>
                        <span v-else class="ml10 f12">(协作方无法添加训练)</span>
                    </h3>
                </div>
                <div v-if="form.is_project_admin" class="right-sort-area">
                    <div class="right-sort-area">
                        <el-icon v-if="sortIndex !== 0" :sidx="sortIndex" :midx="maxIndex" :class="['el-icon-top', {'mr10': maxIndex === sortIndex}]" @click="moveUp"><elicon-top /></el-icon>
                        <el-icon v-if="maxIndex !== sortIndex" :class="['el-icon-bottom', 'ml10', 'mr10']" @click="moveDown"><elicon-bottom /></el-icon>
                        <span v-if="sortIndex !== 0 && sortIndex !== 1" :class="['f12', {'mr10': sortIndex === 2}]" @click="toTop">置顶</span>
                        <span v-if="sortIndex !== maxIndex && sortIndex !== maxIndex -1" class="f12" @click="toBottom">置底</span>
                    </div>
                </div>
            </div>
        </template>

        <el-table
            ref="multipleTable"
            max-height="500px"
            :data="list"
            stripe
        >
            <el-table-column
                label="训练"
                min-width="200"
            >
                <template v-slot="scope">
                    <FlowStatusTag
                        :key="scope.row.updated_time"
                        :status="scope.row.flow_status"
                        :disable-transitions="true"
                        class="mr5"
                    />
                    <el-link
                        type="primary"
                        :underline="false"
                        @click="linkTo(form.project_type === 'DeepLearning' ? 'teamwork/detail/deep-learning/flow' : 'teamwork/detail/flow', { flow_id: scope.row.flow_id, project_id: project_id, training_type: form.project_type === 'DeepLearning' ? scope.row.deep_learning_job_type : '', is_project_admin: form.is_project_admin })"
                    >
                        {{ scope.row.flow_name }}
                    </el-link>
                </template>
            </el-table-column>
            <el-table-column
                v-if="form.project_type === 'MachineLearning'"
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
            <el-table-column label="训练类型">
                <template v-slot="scope">
                    <p>{{ form.project_type === 'MachineLearning' ? learningType(scope.row.federated_learning_type) : learningType(scope.row.deep_learning_job_type) }}</p>
                </template>
            </el-table-column>
            <el-table-column
                label="创建者"
                prop="creator_nickname"
            />
            <el-table-column
                label="创建时间"
                min-width="160px"
            >
                <template v-slot="scope">
                    <p>{{ dateFormat(scope.row.created_time) }}</p>
                </template>
            </el-table-column>
            <el-table-column
                v-if="form.audit_status !== 'disagree'"
                label="操作"
                min-width="200px"
                fixed="right"
            >
                <template v-slot="scope">
                    <template v-if="form.project_type === 'DeepLearning'">
                        <el-link
                            type="primary"
                            class="link mr10"
                            :underline="false"
                            @click="linkTo('teamwork/detail/deep-learning/flow', { flow_id: scope.row.flow_id, project_id: project_id, training_type: scope.row.deep_learning_job_type, is_project_admin: form.is_project_admin })"
                        >
                            查看
                        </el-link>
                        <el-link
                            v-if="scope.row.flow_status === 'success'"
                            type="primary"
                            class="mr10"
                            :underline="false"
                            @click="linkTo('teamwork/detail/deep-learning/check-flow', { flow_id: scope.row.flow_id, flow_name: scope.row.flow_name, project_id: project_id, project_name: form.name })"
                        >
                            校验
                        </el-link>
                    </template>
                    <el-link
                        v-else
                        class="mr10"
                        type="primary"
                        :underline="false"
                        @click="linkTo('teamwork/detail/job/history', { project_id, flow_id: scope.row.flow_id })"
                    >
                        执行记录
                    </el-link>
                    <el-dropdown v-if="scope.row.is_creator && form.is_project_admin" size="small">
                        <el-button type="text" size="small">
                            更多
                            <el-icon>
                                <elicon-arrow-down />
                            </el-icon>
                        </el-button>
                        <template #dropdown>
                            <el-dropdown-menu>
                                <el-dropdown-item v-if="form.project_type !== 'DeepLearning'">
                                    <el-button
                                        type="text"
                                        size="small"
                                        @click="copyFlow(scope.row)"
                                    >
                                        复制训练
                                    </el-button>
                                </el-dropdown-item>
                                <el-dropdown-item divided>
                                    <el-button
                                        type="text"
                                        size="small"
                                        class="color-danger"
                                        @click="deleteFlow(scope.row, scope.$index)"
                                    >
                                        删除训练
                                    </el-button>
                                </el-dropdown-item>
                            </el-dropdown-menu>
                        </template>
                    </el-dropdown>
                    <p v-if="list.length > 1 && userInfo.admin_role" class="ml10 totop_btn" @click="flowToTopClick(scope.row)">
                        <!-- <span :style="{'color': scope.row.top ? '#e6a23c' : '#438bff'}">{{scope.row.top ? '取消置顶' : '置顶'}}</span> -->
                        <el-tooltip v-if="scope.row.top" effect="light" content="取消置顶" placement="bottom">
                            <el-icon class="f14" style="color: #f85564; font-weight: 500;">
                                <elicon-bottom />
                            </el-icon>
                        </el-tooltip>
                        <el-tooltip v-if="(scope.$index !== 0 && !scope.row.top) || (scope.$index === 0 && !scope.row.top && this.pagination.page_index !== 1)" effect="light" content="置顶" placement="bottom">
                            <el-icon class="f14" style="color: #438bff; font-weight: 500;">
                                <elicon-top />
                            </el-icon>
                        </el-tooltip>
                    </p>
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
            title="复制训练:"
            v-model="copyFlowDialog.visible"
            destroy-on-close
            width="400px"
        >
            <el-form @submit.prevent>
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
                    label="新训练名称："
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
            v-model="addFlow"
            destroy-on-close
        >
            <template #title>
                选择模版:
                <span class="ml10 f14 el-alert__description">(训练创建后将无法更改训练类型)</span>
            </template>

            <div
                v-loading="loading"
                class="model-list"
            >
                <div
                    class="li empty-flow"
                    @click="createFlow($event, { federated_learning_type: 'vertical' })"
                >
                    <span class="model-img f30">
                        纵向
                    </span>
                    空白训练
                </div>
                <div
                    class="li empty-flow"
                    @click="createFlow($event, { federated_learning_type: 'horizontal' })"
                >
                    <span class="model-img f30">
                        横向
                    </span>
                    空白训练
                </div>
                <div
                    class="li empty-flow"
                    @click="createFlow($event, { federated_learning_type: 'mix' })"
                >
                    <span class="model-img f30">
                        混合
                    </span>
                    空白训练
                </div>

                <template
                    v-for="item in templateList"
                    :key="item.id"
                >
                    <el-tooltip
                        v-if="item.enname !== 'oot'"
                        :content="item.description"
                        effect="dark"
                    >
                        <div
                            class="li"
                            @click="createFlow($event, item)"
                        >
                            <span class="model-img">
                                <img :src="flowImgs[item.enname]">
                            </span>
                            {{ item.name }}
                            {{ item.desc }}
                        </div>
                    </el-tooltip>
                </template>
            </div>
        </el-dialog>
        <!-- 深度学习训练 -->
        <el-dialog
            v-model="addDeeplearningFlow"
            destroy-on-close
        >
            <template #title>
                选择模版:
                <span class="ml10 f14 el-alert__description">(训练创建后将无法更改训练类型)</span>
            </template>

            <div
                v-loading="loading"
                class="model-list"
            >
                <div
                    class="li empty-flow"
                    @click="createFlow($event, { federated_learning_type: 'detection' })"
                >
                    <span class="model-img f20">
                        目标检测
                    </span>
                </div>
                <div
                    class="li empty-flow"
                    @click="createFlow($event, { federated_learning_type: 'classify' })"
                >
                    <span class="model-img f20">
                        图像分类
                    </span>
                </div>
            </div>
        </el-dialog>
    </el-card>
</template>

<script>
    import table from '@src/mixins/table';
    import FlowStatusTag from '@src/components/views/flow-status-tag';
    import { mapGetters } from 'vuex';

    const prefixPath = process.env.NODE_ENV === 'development' ? '/' : `${process.env.CONTEXT_ENV ? `/${process.env.CONTEXT_ENV}/` : '/'}`;

    export default {
        components: {
            FlowStatusTag,
        },
        mixins: [table],
        inject: ['refresh'],
        props:  {
            form:      Object,
            sortIndex: Number,
            maxIndex:  Number,
        },
        emits: ['move-up', 'move-down', 'to-top', 'to-bottom'],
        data() {
            return {
                timer:               null,
                locker:              false,
                loading:             false,
                project_id:          '',
                thisProject:         true,
                disabled:            true,
                addFlow:             false,
                addDeeplearningFlow: false,
                copyFlowDialog:      {
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
                    mix_lr:   require('@assets/images/mix_lr.png'),
                    mix_xgb:  require('@assets/images/mix_xgb.png'),
                },
                flowTimer: null,
                config:    {}, // deeplearning config
            };
        },
        computed: {
            learningType() {
                return function (val) {
                    const types = {
                        vertical:   '纵向',
                        horizontal: '横向',
                        mix:        '混合',
                        classify:   '图像分类',
                        detection:  '目标检测',
                    };

                    return types[val] || '-';
                };
            },
            ...mapGetters(['userInfo']),
        },
        created() {
            this.project_id = this.$route.query.project_id;
            this.getFlowList();
            this.getTemplateList();
            this.getConfigInfo();
        },
        beforeUnmount() {
            clearTimeout(this.timer);
            clearTimeout(this.flowTimer);
        },
        methods: {
            afterTableRender() {
                clearTimeout(this.timer);

                if(this.userInfo && this.userInfo.id) {
                    this.timer = setTimeout(() => {
                        this.getFlowList({
                            requestFromRefresh: true,
                        });
                    }, 3000);
                }
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
                        clearTimeout(this.flowTimer);
                        this.flowTimer = setTimeout(() => {
                            this.getFlowList({ requestFromRefresh: true });
                        }, 5000);
                    }
                    clearTimeout(this.flowTimer);
                    this.flowTimer = setTimeout(() => {
                        this.getFlowList({ requestFromRefresh: true });
                    }, 5000);
                }
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

                const deeplearning = this.form.project_type === 'DeepLearning';
                const params = {
                    project_id:            this.project_id,
                    federatedLearningType: deeplearning ? 'horizontal' : opt.federated_learning_type,
                    name:                  `${opt.name || '新训练'}-${this.getDateTime()}`,
                    desc:                  '',
                };

                if(opt.id) {
                    params.templateId = opt.id;
                }
                if(deeplearning) {
                    params.deepLearningJobType = opt.federated_learning_type;
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
                    const query = {
                        flow_id:          data.flow_id,
                        training_type:    deeplearning ? opt.federated_learning_type : '',
                        project_id:       this.project_id,
                        is_project_admin: this.form.is_project_admin,
                    };

                    this.linkTo(deeplearning ? 'teamwork/detail/deep-learning/flow' : 'teamwork/detail/flow', query);
                }
            },

            linkTo(path, query) {
                let href = `${prefixPath}${path}?`;

                for(const key in query) {
                    const val = query[key];

                    href += `${key}=${val}&`;
                }

                window.location.href = href;
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
                this.$confirm('确定要删除该训练吗? 此操作不可撤销!', '警告', {
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
                    // 创建深度学习训练
                    this.addDeeplearningFlow = true;
                }
            },

            async getConfigInfo() {
                const { code, data } = await this.$http.post({
                    url:  '/global_config/get',
                    data: { groups: ['deep_learning_config'] },
                });

                if (code === 0) {
                    this.config = data;
                }
            },
            moveUp() {
                this.$emit('move-up', this.sortIndex);
            },
            moveDown() {
                this.$emit('move-down', this.sortIndex);
            },
            toTop() {
                this.$emit('to-top', this.sortIndex);
            },
            toBottom() {
                this.$emit('to-bottom', this.sortIndex);
            },
            async flowToTopClick(item) {
                const { code } = await this.$http.post({
                    url:  '/project/flow/top',
                    data: {
                        flowId: item.flow_id,
                        top:    !item.top,
                    },
                });

                if(code === 0) {
                    this.getFlowList();
                }
            },
        },
    };
</script>

<style lang="scss" scoped>
    .el-alert__description{
        color: $--color-danger;
    }
    h3{margin: 10px;}
    .el-dropdown{top: -1px;}
    .model-list{
        display: flex;
        justify-content: center;
        flex-wrap: wrap;
    }
    .li{
        cursor: pointer;
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
    .el-switch{
        :deep(.el-switch__label){
            color: #999;
            &.is-active{color: $--color-primary;}
        }
    }
    .totop_btn {
        display: inline-block;
        cursor: pointer;
    }
</style>
