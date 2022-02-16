<template>
    <div
        v-loading="loading"
        class="page"
    >
        <el-card
            v-if="no_job"
            shadow="never"
            style="height: 500px;"
        >
            暂无任务
        </el-card>

        <template v-else>
            <el-card
                name="任务概览"
                shadow="never"
                class="nav-title mb20"
            >
                <h2 class="title">
                    <router-link
                        class="f14 mr10"
                        :to="{name: 'project-job-history', query: { flow_id: flowId, project_id }}"
                    >
                        <el-icon>
                            <elicon-arrow-left />
                        </el-icon>
                        返回执行记录页
                    </router-link>
                    <br>
                </h2>
                <div class="el-row">
                    <div class="el-col">
                        <el-form class="flex-form">
                            <el-form-item label="任务状态">
                                <el-tag
                                    class="el-tag--dark"
                                    :type="form.tagStatus"
                                >
                                    {{ jobStatus[form.status] }}
                                </el-tag>
                            </el-form-item>
                            <el-form-item label="我方身份">
                                <el-tag
                                    class="el-tag--dark"
                                    :type="form.my_role === 'promoter' ? 'success' : 'dark'"
                                >
                                    <i :class="[form.my_role === 'promoter' ? 'el-icon-star-off' : 'el-icon-medal']" />
                                    {{ form.my_role }}
                                </el-tag>
                            </el-form-item>
                            <el-form-item label="开始时间">
                                {{ dateFormat(form.start_time) }}
                            </el-form-item>
                            <el-form-item label="结束时间">
                                {{ dateFormat(form.finish_time) }}
                            </el-form-item>
                            <el-form-item label="总耗时">
                                {{ timeFormat(form.spend_time) }}
                            </el-form-item>
                            <el-form-item label="任务类型">
                                {{ learningType(form.federated_learning_type) }}
                            </el-form-item>
                            <DownloadJobLog :job-id="jobId" />
                        </el-form>
                    </div>
                    <div class="el-col graph">
                        <div
                            ref="canvas"
                            style="height:100%;"
                        >
                            <em class="f12 ml10">区域内滚动鼠标滚轮缩放画布</em>
                        </div>
                        <div
                            ref="graph-minimap"
                            class="minimap"
                        />
                    </div>
                </div>
            </el-card>

            <el-card shadow="never">
                <h2 class="title">组件详情</h2>
                <h4 class="mb10">节点概览:</h4>
                <el-table
                    :data="task_views"
                    class="mb20"
                    stripe
                >
                    <el-table-column type="index" />
                    <el-table-column label="组件名称">
                        <template v-slot="scope">
                            <a
                                :target="`#${scope.row.task_id}`"
                                @click="transitionToTask(scope.row.task_id)"
                            >{{ scope.row.task.component_name }}</a>
                        </template>
                    </el-table-column>
                    <el-table-column label="运行状态">
                        <template v-slot="scope">
                            {{ jobStatus[scope.row.task.status] }}
                            <p
                                v-if="scope.row.task.message"
                                :class="{'color-danger': scope.row.task.status !== 'success'}"
                                style="line-height: 16px;"
                            >
                                <template v-if="scope.row.task.message.length > 300">
                                    任务信息: {{ scope.row.task.message.substr(0, 300) }}...
                                    <el-button
                                        type="primary"
                                        size="small"
                                        @click="checkErrorDetail(scope.row.task.message)"
                                    >查看更多</el-button>
                                </template>
                                <template v-else>
                                    任务信息: {{ scope.row.task.message }}
                                </template>
                            </p>
                        </template>
                    </el-table-column>
                    <el-table-column
                        label="运行时长"
                        width="140"
                    >
                        <template v-slot="scope">
                            <template v-if="scope.row.task.finish_time > scope.row.task.start_time">
                                <strong>{{ timeFormat((scope.row.task.finish_time - scope.row.task.start_time) / 1000) }}</strong>
                                <p>{{ dateFormat(scope.row.task.start_time, 'yyyy-MM-dd hh:mm:ss') }}</p>
                                <p>{{ dateFormat(scope.row.task.finish_time, 'yyyy-MM-dd hh:mm:ss') }}</p>
                            </template>
                            <template v-else>--</template>
                        </template>
                    </el-table-column>
                    <el-table-column
                        label="操作"
                        min-width="120"
                    >
                        <template v-slot="scope">
                            <el-button
                                v-if="scope.row.serving_model"
                                @click="syncModel($event, scope.row.task)"
                            >
                                同步模型到 serving
                            </el-button>
                            <template v-else>--</template>
                        </template>
                    </el-table-column>
                </el-table>
                <el-divider />
                <h4 class="nav-title mb20" name="节点执行结果">节点执行结果:</h4>
                <ul class="mt10">
                    <li
                        v-for="item in task_views"
                        :key="item.task.id"
                        class="task-nodes mb10 p20"
                    >
                        <h4
                            :id="item.task_id"
                            :name="item.task.component_name"
                            class="nav-title f16 mb10"
                        >
                            <strong>{{ item.task.component_name }}:</strong>
                        </h4>
                        <template v-if="componentsList[item.task.task_type]">
                            <component
                                :autoReadResult="true"
                                :is="`${item.task.task_type}-result`"
                                :flow-node-id="item.task.flow_node_id"
                                :is-creator="form.isCreator"
                                :job-detail="jobDetail"
                                :my-role="form.my_role"
                                :flow-id="item.task.flow_id"
                                :job-id="item.task.job_id"
                            />
                        </template>
                    </li>
                </ul>
            </el-card>
        </template>
    </div>
</template>

<script>
    import {
        Minimap,
        Grid,
        Graph,
    } from '@antv/g6';
    import {
        componentsList,
        resultComponents,
    } from '../visual/component-list/component-map';
    import DownloadJobLog from '../components/download-job-log';

    export default {
        components: {
            ...componentsList,
            ...resultComponents,
            DownloadJobLog,
        },
        data() {
            return {
                componentsList,
                jobId:       '',
                flowId:      '',
                project_id:  '',
                member_role: '',
                loading:     false,
                no_job:      true,
                jobDetail:   {},
                jobStatus:   {
                    created:          '已创建',
                    wait_run:         '等待运行',
                    running:          '运行中',
                    stop:             '人为结束',
                    wait_stop:        '等待结束',
                    stop_on_running:  '人为关闭',
                    error_on_running: '程序异常关闭',
                    error:            '执行失败',
                    success:          '成功(正常结束)',
                },
                form: {
                    name:                    '',
                    start_time:              '',
                    finish_time:             '',
                    spend_time:              '',
                    my_role:                 '',
                    progress:                '',
                    status:                  '',
                    tagStatus:               '',
                    federated_learning_type: '',
                },
                task_views: [],
            };
        },
        computed: {
            learningType() {
                return function (val) {
                    switch(val) {
                    case 'vertical':
                        return '纵向';
                    case 'horizontal':
                        return '横向';
                    case 'mix':
                        return '混合';
                    }
                    return '-';
                };
            },
        },
        created() {
            const { job_id, member_role } = this.$route.query;

            this.jobId = job_id;
            this.member_role = member_role;
            this.getJobDetails();

            this.$bus.$on('history-backward', meta => {
                this.$router.push({
                    name:  meta.name,
                    query: {
                        flow_id: this.flowId,
                    },
                });
            });
        },
        beforeUnmount() {
            // this.$bus.$off('history-backward');
        },
        methods: {
            async getJobDetails() {
                if(this.loading) return;
                this.loading = true;

                const { code, data } = await this.$http.get({
                    url:    '/flow/job/detail',
                    params: {
                        jobId:       this.jobId,
                        member_role: this.member_role,
                        needResult:  true,
                    },
                });

                this.loading = false;
                if(code === 0) {
                    if(data) {
                        if(data.job) {
                            this.jobDetail = data.job;
                            this.flowId = data.job.flow_id;
                            this.project_id = data.job.project_id;
                            this.form.name = data.job.name;
                            this.form.start_time = data.job.start_time;
                            this.form.finish_time = data.job.finish_time;
                            if(data.job.finish_time && this.form.start_time) {
                                this.form.spend_time = (data.job.finish_time - data.job.start_time) / 1000;
                            }
                            switch(data.job.status) {
                            case 'created':
                            case 'wait_run':
                                this.form.tagStatus = 'warning';
                                break;
                            case 'running':
                            case 'wait_stop':
                                this.form.tagStatus = '';
                                break;
                            case 'stop_on_running':
                            case 'stop':
                            case 'error_on_running':
                                this.form.tagStatus = 'danger';
                                break;
                            case 'success':
                                this.form.tagStatus = 'success';
                                break;
                            default:
                                this.form.tagStatus = '';
                                break;
                            }
                            this.form.federated_learning_type = data.job.federated_learning_type;
                            this.form.progress = data.job.progress;
                            this.form.my_role = data.job.my_role;
                            this.form.status = data.job.status;
                            this.no_job = false;

                            this.$nextTick(_ => {
                                /* 实例化 g6 */
                                const canvas = this.$refs['canvas'];
                                const minimap = new Minimap({
                                    container: this.$refs['graph-minimap'],
                                    size:      [200, 100],
                                });
                                const grid = new Grid();
                                const plugins = [grid, minimap];

                                const graph = new Graph({
                                    container:   canvas,
                                    width:       canvas.offsetWidth,
                                    height:      canvas.offsetHeight,
                                    defaultNode: {
                                        style: {
                                            radius: 4,
                                            fill:   '#ecf3ff',
                                        },
                                    },
                                    modes: {
                                        default: ['drag-canvas', 'zoom-canvas'],
                                    },
                                    layout: {
                                        type: '',
                                    },
                                    fitCenter: true,
                                    maxZoom:   5,
                                    plugins,
                                });

                                this.$nextTick(_ => {
                                    data.job.graph.nodes.forEach(node => {
                                        node.type = 'rect';
                                        if(!node.labelCfg.style) {
                                            node.labelCfg.style = {};
                                        }
                                        node.labelCfg.style.fill = node.id === 'start' ? '#8BC34A' : '#4483FF';
                                    });
                                    graph.get('canvas').set('localRefresh', false);
                                    graph.read(data.job.graph);
                                    graph.fitCenter();
                                    graph.fitView();
                                });
                            });
                        }

                        if(data.task_views) {
                            this.task_views = data.task_views.map(row => {
                                return {
                                    results: row.results || [],
                                    task:    row.task || {
                                        status: 'wait_run',
                                    },
                                    task_id:       row.task ? row.task.task_id : '',
                                    serving_model: false,
                                };
                            });
                        }

                        this.$nextTick(_ => {
                            this.$bus.$emit('update-title-navigator');
                        });
                    }
                }
            },

            updateTask({ task_id, serving_model }) {
                if(serving_model) {
                    this.task_views.find(row => {
                        if(row.task_id === task_id) {
                            row.serving_model = true;
                        }
                    });
                }
            },

            async syncModel($event, task) {
                const { code } = await this.$http.post({
                    url:  '/data_output_info/sync_model_to_serving',
                    data: {
                        task_id: task.task_id,
                        role:    this.form.my_role,
                    },
                    btnState: {
                        target: $event,
                    },
                });

                if(code === 0) {
                    this.$message.success('同步成功!');
                }
            },

            transitionToTask(id) {
                const el = document.getElementById('layout-main');
                const top = document.getElementById(id).offsetTop;

                el.scrollTo(0, top - 30);
            },

            checkErrorDetail(message) {
                this.$message.error({
                    message,
                    showClose: true,
                });
            },
        },
    };
</script>

<style lang="scss" scoped>
    .page{margin-right: 100px;}
    .title{
        margin:0 0 20px;
        font-size: 20px;
    }
    .el-form-item{margin-bottom: 10px;}
    .el-row{display: flex;}
    .el-col{width: 40%;}
    .graph{
        flex: 1;
        position: relative;
        user-select: none;
        overflow: hidden;
        border: 1px solid $border-color-base;
    }
    .minimap{
        position: absolute;
        right:-1px;
        bottom:-1px;
        background: #fff;
        border: 1px solid #ccc;
        z-index: 10;
    }
    .task-nodes{
        border: 1px solid #EBEEF5;
        max-height: 1000px;
        overflow: auto;
        :deep(.history-btn){display:none;}
    }
    .task-status{
        font-size: 12px;
        font-weight: normal;
        color: #999;
    }
</style>
