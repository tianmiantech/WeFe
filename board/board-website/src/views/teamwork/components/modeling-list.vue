<template>
    <el-card
        name="模型列表"
        shadow="never"
        class="nav-title mb30"
        :show="project_type !== 'DeepLearning'"
        :idx="sortIndex"
    >
        <template #header>
            <div class="clearfix mb10 flex-row">
                <h3 class="mb10">模型列表</h3>
                <div class="right-sort-area">
                    <el-icon v-if="sortIndex !== 0" :sidx="sortIndex" :midx="maxIndex" :class="['el-icon-top', {'mr10': maxIndex === sortIndex}]" @click="moveUp"><elicon-top /></el-icon>
                    <el-icon v-if="maxIndex !== sortIndex" :class="['el-icon-bottom', 'ml10', 'mr10']" @click="moveDown"><elicon-bottom /></el-icon>
                    <span v-if="sortIndex !== 0 && sortIndex !== 1" :class="['f12', {'mr10': sortIndex === 2}]" @click="toTop">置顶</span>
                    <span v-if="sortIndex !== maxIndex && sortIndex !== maxIndex -1" class="f12" @click="toBottom">置底</span>
                </div>
            </div>
        </template>
        <el-form inline @submit.prevent>
            <el-form-item label="来源组件：">
                <el-select v-model="search.component_type">
                    <el-option
                        v-for="item in component_types"
                        :key="item.value"
                        :value="item.value"
                        :label="item.label"
                    />
                </el-select>
            </el-form-item>
            <el-form-item label="来源流程 id：">
                <el-input
                    v-model="search.flow_id"
                    placeholder="选填"
                />
            </el-form-item>
            <el-form-item label="任务 id：">
                <el-input
                    v-model="search.job_id"
                    placeholder="选填"
                />
            </el-form-item>
            <el-form-item>
                <el-button
                    type="primary"
                    @click="getList"
                >
                    搜索
                </el-button>
                <el-button
                    type="primary"
                    @click="modelCompare"
                >
                    模型对比
                </el-button>
            </el-form-item>
        </el-form>

        <div :class="['flex-layout', { show_result_panel }]">
            <div
                v-if="form.isPromoter"
                class="result-panel"
            >
                <el-icon
                    class="el-icon-close close-result-panel-icon"
                    @click="hiddenResultPanel"
                >
                    <elicon-close />
                </el-icon>
                <ChartsWithTabs
                    v-if="show_result_panel"
                    ref="ChartsWithTabs"
                    result-api="/project/modeling/detail"
                    :flow-node-id="chartIds.flow_node_id"
                    :flow-id="chartIds.flow_id"
                    :job-id="chartIds.job_id"
                    :show-topn="isShowTopN"
                />
            </div>

            <div v-loading="loading" class="modeling-table">
                <el-table
                    :data="list"
                    stripe
                >
                    <el-table-column
                        label="名称"
                        min-width="200px"
                    >
                        <template v-slot="scope">
                            <p
                                v-if="form.isPromoter"
                                :class="['result-name', { 'current-panel': result_panel_idx === +scope.$index }]"
                                @click="showResult(scope.row, scope.$index)"
                            >
                                {{ scope.row.role }}: {{ scope.row.flow_name }} - {{ scope.row.name }}
                            </p>
                            <p
                                v-else
                                :class="[{ 'current-panel': result_panel_idx === +scope.$index }]"
                            >
                                {{ scope.row.role }}: {{ scope.row.flow_name }} - {{ scope.row.name }}
                            </p>
                        </template>
                    </el-table-column>
                    <el-table-column
                        v-if="!show_result_panel"
                        label="模型来源"
                        min-width="120px"
                    >
                        <template v-slot="scope">
                            {{ scope.row.component_name }}
                        </template>
                    </el-table-column>
                    <el-table-column
                        v-if="!show_result_panel"
                        label="我方角色"
                        width="120px"
                    >
                        <template v-slot="scope">
                            <RoleTag :role="scope.row.role" />
                        </template>
                    </el-table-column>
                    <el-table-column
                        v-if="!show_result_panel"
                        label="相关流程"
                        min-width="120px"
                    >
                        <template v-slot="scope">
                            <router-link
                                class="link mr10"
                                :to="{
                                    name: 'project-flow',
                                    query: { flow_id: scope.row.flow_id, job_id: scope.row.job_id }
                                }"
                            >
                                查看流程
                            </router-link>
                        </template>
                    </el-table-column>
                    <el-table-column
                        v-if="!show_result_panel"
                        label="相关任务"
                        min-width="120px"
                    >
                        <template v-slot="scope">
                            <router-link
                                class="link mr10"
                                :to="{
                                    name: 'project-job-detail',
                                    query: { job_id: scope.row.job_id, project_id, member_role: scope.row.role }
                                }"
                            >
                                查看结果
                            </router-link>
                        </template>
                    </el-table-column>
                    <el-table-column
                        label="创建时间"
                        width="120px"
                    >
                        <template v-slot="scope">
                            <p>{{ dateFormat(scope.row.created_time) }}</p>
                        </template>
                    </el-table-column>
                    <el-table-column
                        width="100px"
                        fixed="right"
                        label="操作"
                    >
                        <template v-slot="scope">
                            <template v-if="scope.row.serving_model">
                                <el-button
                                    type="primary"
                                    size="small"
                                    class="mb5"
                                    @click="modelExportChange(scope.row)"
                                >
                                    模型导出
                                </el-button>
                            </template>
                            <el-button
                                v-if="scope.row.role === 'promoter' && scope.row.component_type !== 'HorzNN' && scope.row.component_type !== 'VertNN'"
                                size="small"
                                @click="addOotFlew($event, scope.row)"
                            >打分验证</el-button>
                        </template>
                    </el-table-column>
                </el-table>
            </div>
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

        <el-dialog
            title="导出"
            v-model="modelExportDialog"
            destroy-on-close
            width="420px"
        >
            <el-tabs v-model="modelExportType">
                <el-tab-pane v-if="selectedRow.component_type === 'HorzSecureBoost' || selectedRow.component_type === 'HorzLR'" label="导出为代码" :name="0">
                    <p class="mb10 f14">点击任意语言可下载对应的模型:</p>
                    <p class="color-danger mb10 f12">请使用浏览器默认下载器, 否则下载的文件格式可能有误</p>
                    <div v-loading="loading" class="select-lang">
                        <el-tag
                            v-for="item in languages"
                            :key="item"
                            @click="modelExport($event, item)"
                        >
                            {{ item }}
                        </el-tag>
                    </div>
                </el-tab-pane>
                <el-tab-pane label="同步到serving" :name="1">
                    <p>模型推送到 serving</p>
                    <div class="text-c mt30">
                        <el-button
                            type="primary"
                            @click="syncModel"
                        >
                            同步模型
                        </el-button>
                    </div>
                </el-tab-pane>
                <el-tab-pane label="下载模型" :name="2">
                    <p>下载模型文件到本地</p>
                    <div class="text-c mt30">
                        <el-button
                            type="primary"
                            @click="downloadModel"
                        >
                            下载模型
                        </el-button>
                    </div>
                </el-tab-pane>
            </el-tabs>
        </el-dialog>
    </el-card>
</template>

<script>
    import { mapGetters } from 'vuex';
    import table from '@src/mixins/table';
    import RoleTag from '@src/components/views/role-tag';

    export default {
        components: {
            RoleTag,
        },
        mixins: [table],
        props:  {
            form:      Object,
            sortIndex: Number,
            maxIndex:  Number,
        },
        emits:    ['move-up', 'move-down', 'to-top', 'to-bottom'],
        computed: {
            ...mapGetters(['userInfo']),
        },
        data() {
            return {
                loading:           false,
                show_result_panel: false,
                result_panel_idx:  0,

                chartIds: {
                    flow_id:      '',
                    flow_node_id: '',
                    job_id:       '',
                },
                search: {
                    name:           '',
                    flow_id:        '',
                    component_type: '',
                    project_id:     '',
                    job_id:         '',
                },

                component_types: [{
                    label: '纵向 XGBoost',
                    value: 'VertSecureBoost',
                }, {
                    label: '纵向 LR',
                    value: 'VertLR',
                }, {
                    label: '横向 XGBoost',
                    value: 'HorzSecureBoost',
                }, {
                    label: '横向 LR',
                    value: 'HorzLR',
                }, {
                    label: '纵向深度学习',
                    value: 'VertNN',
                }, {
                    label: '横向深度学习',
                    value: 'HorzNN',
                }, {
                    label: '混合 XGBoost',
                    value: 'MixSecureBoost',
                }, {
                    label: '混合 LR',
                    value: 'MixLR',
                }],
                list:          [],
                watchRoute:    false,
                turnPageRoute: false,
                getListApi:    '/project/modeling/query',
                pagination:    {
                    page_index: 1,
                    page_size:  10,
                    total:      0,
                },
                isShowTopN: true, // show topn
                languages:  [
                    'c',
                    'cSharp',
                    'dart',
                    'go',
                    'haskell',
                    'java',
                    'javaScript',
                    'php',
                    'powerShell',
                    'python',
                    'r',
                    'ruby',
                    'visualBasic',
                    'pmml',
                ],
                selectedRow:       {},
                modelExportDialog: false,
                modelExportType:   0,
            };
        },
        created() {
            this.project_id = this.$route.query.project_id;
            this.project_type = this.$route.query.project_type;
            this.search.project_id = this.project_id;
        },
        methods: {
            afterTableRender() {
                if(this.form.isPromoter) {
                    if (this.list && this.list.length > 0) {
                        this.showResult(this.list[0], 0);
                        // force rerender
                        setTimeout(() => {
                            this.showResult(this.list[0], 0);
                        });
                    } else {
                        this.show_result_panel = false;
                    }
                }
            },

            async showResult(item, index) {
                this.show_result_panel = true;
                this.result_panel_idx = +index;
                this.chartIds = item;
                setTimeout(_ => {
                    this.$refs['ChartsWithTabs'] && this.$refs['ChartsWithTabs'].readResult();
                }, 300);
            },

            hiddenResultPanel() {
                this.show_result_panel = false;
            },

            modelExportChange(row) {
                this.modelExportDialog = true;
                this.modelExportType = (row.component_type === 'HorzSecureBoost' || row.component_type === 'HorzLR') ? 0 : 1;
                this.selectedRow = row;
            },

            async syncModel($event) {
                this.loading = true;
                const { code } = await this.$http.post({
                    url:  '/data_output_info/sync_model_to_serving',
                    data: {
                        task_id: this.selectedRow.task_id,
                        role:    this.selectedRow.role,
                    },
                    btnState: {
                        target: $event,
                    },
                });

                this.loading = false;
                if(code === 0) {
                    this.$message.success('同步成功!');
                }
            },

            async addOotFlew($event, row) {
                this.loading = true;
                const { code, data } = await this.$http.post({
                    url:  '/project/flow/add_oot',
                    data: {
                        ootJobId:           row.job_id,
                        ootModelFlowNodeId: row.flow_node_id,
                    },
                });

                this.loading = false;
                if(code === 0) {
                    this.$router.push({
                        name:  'project-flow',
                        query: {
                            flow_id: data.flow_id,
                        },
                    });
                }
            },

            async modelExport(event, language) {
                const href = `${window.api.baseUrl}/data_output_info/model_export?jobId=${this.selectedRow.job_id}&modelFlowNodeId=${this.selectedRow.flow_node_id}&role=${this.selectedRow.role}&language=${language}&token=${this.userInfo.token}`;
                const link = document.createElement('a');

                link.href = href;
                link.target = '_blank';
                link.style.display = 'none';
                document.body.appendChild(link);
                link.click();
            },

            downloadModel(e) {
                const href = `${window.api.baseUrl}/data_output_info/model_export_to_file?taskId=${this.selectedRow.task_id}&role=${this.selectedRow.role}&token=${this.userInfo.token}`;
                const link = document.createElement('a');

                link.href = href;
                link.target = '_blank';
                link.style.display = 'none';
                document.body.appendChild(link);
                link.click();
            },

            modelCompare() {
                const { href } = this.$router.resolve({
                    name:  'modeling-list',
                    query: {
                        project_id: this.project_id,
                        isPromoter: this.form.isPromoter,
                    },
                });

                window.open(href, '_blank');
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
        },
    };
</script>

<style lang="scss" scoped>
    .result-panel,
    .modeling-table {
        max-height: 502px;
        border: solid 1px #eee;
    }
    .modeling-table{
        overflow-y: auto;
        min-width: 300px;
        width: 100%;
        .el-button {margin-left:0;}
    }
    .result-panel {
        transition-duration: 0.2s;
        position: relative;
        opacity:0;
        width:0;
    }
    .result-name{cursor: pointer;}
    .close-result-panel-icon {
        cursor: pointer;
        position: absolute;
        right: 15px;
        top: 15px;
        z-index: 1024;
    }
    .flex-layout{
        display: flex;
        overflow: auto;
        &.show_result_panel{
            .result-panel{
                flex: 1;
                padding: 10px;
                opacity: 1;
            }
            .modeling-table{max-width: 352px;}
        }
    }
    .current-panel{color: $color-link-base-hover;}
    .select-lang{
        .el-tag{
            margin-left:15px;
            margin-top: 15px;
            cursor: pointer;
            &:hover{background:#dfefff;}
        }
    }
</style>
