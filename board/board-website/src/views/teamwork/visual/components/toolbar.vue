<template>
    <div class="top-bar">
        <div class="toolbar">
            <!-- <el-tooltip
                :open-delay="600"
                content="回退操作"
                placement="top"
            >
                <i
                    :class="['iconfont', 'icon-backward', { disabled: stack.length === 0 }]"
                    @click="excute('backward')"
                />
            </el-tooltip>
            <el-tooltip
                :open-delay="600"
                placement="top"
                content="重做"
            >
                <i
                    :class="['iconfont', 'icon-forward', { disabled: stack.length === 0 }]"
                    @click="excute('forward')"
                />
            </el-tooltip> -->
            <template v-if="myRole === 'promoter' && isCreator">
                <el-tooltip
                    v-if="vData.jobStatus === '' || jobStopStatus.includes(vData.jobStatus)"
                    :open-delay="600"
                    placement="top"
                    content="启动"
                >
                    <i
                        v-loading="vData.loading"
                        class="iconfont icon-play"
                        @click="methods.shouldStart($event)"
                    />
                </el-tooltip>
                <el-tooltip
                    v-if="!(vData.jobStatus === '' || jobStopStatus.includes(vData.jobStatus))"
                    :open-delay="600"
                    placement="top"
                    content="暂停"
                >
                    <i
                        class="iconfont icon-pause"
                        @click="methods.pause"
                    />
                </el-tooltip>
                |
            </template>

            <template v-if="reference === 'main'">
                <el-tooltip
                    :open-delay="600"
                    placement="top"
                    content="任务详情面板"
                >
                    <i
                        :class="['iconfont', 'icon-job', { disabled: !vData.jobStatus }]"
                        @click="methods.excute('switchJobGraphPanel', !vData.jobStatus ? 'disabled' : '')"
                    />
                </el-tooltip>
                <el-tooltip
                    :open-delay="600"
                    placement="top"
                    content="任务历史"
                >
                    <i
                        class="iconfont icon-history"
                        @click="methods.openWindow('project-job-history')"
                    />
                </el-tooltip>
                |
            </template>
            <el-tooltip
                :open-delay="600"
                placement="top"
                content="重定位"
            >
                <i
                    class="iconfont icon-reset"
                    @click="methods.excute('relocation')"
                />
            </el-tooltip>
            <el-tooltip
                :open-delay="600"
                placement="top"
                content="1倍"
            >
                <i
                    class="iconfont icon-1x"
                    @click="methods.excute('resize')"
                />
            </el-tooltip>
            <el-tooltip
                :open-delay="600"
                placement="top"
                content="缩小"
            >
                <i
                    class="iconfont icon-zoom-out"
                    @click="methods.excute('zoomIn')"
                />
            </el-tooltip>
            <el-tooltip
                :open-delay="600"
                placement="top"
                content="放大"
            >
                <i
                    class="iconfont icon-zoom-in"
                    @click="methods.excute('zoomOut')"
                />
            </el-tooltip>
            <el-tooltip
                :open-delay="600"
                placement="top"
                content="缩略图"
            >
                <i
                    :class="['iconfont', 'icon-minimap', { inactive: !minimap }]"
                    @click="methods.excute('switchMinimap')"
                />
            </el-tooltip>

            <template v-if="reference === 'main' && myRole === 'promoter' && isCreator">
                <el-tooltip
                    :open-delay="600"
                    placement="top"
                    content="帮助"
                >
                    <i
                        class="iconfont icon-why"
                        @click="vData.checkHelpBook = true"
                    />
                </el-tooltip>
                |
                <el-tooltip
                    :open-delay="600"
                    placement="top"
                    content="保存"
                >
                    <i
                        class="iconfont icon-save"
                        @click="methods.excute('save')"
                    />
                </el-tooltip>
            </template>
        </div>
    </div>

    <!-- preview view -->
    <div
        id="job-preview"
        :class="{ active: vData.jobShouldStart }"
    >
        <span style="position: absolute; top: 15px; left: 15px; z-index:2;">任务预览</span>
        <div
            ref="jobPreview"
            class="graph-preview"
        />
        <div class="preview-options">
            使用任务缓存启动
            <el-switch
                v-model="vData.jobUseCache"
                @change="methods.jobUseCacheChange"
            />
            <div class="mt15">
                <el-button @click="vData.jobShouldStart=false">取消</el-button>
                <el-button
                    type="primary"
                    @click="methods.play($event, { useCache: vData.jobUseCache })"
                >
                    启动
                </el-button>
            </div>
        </div>
    </div>

    <el-dialog
        width="500px"
        append-to-body
        destroy-on-close
        title="支持的操作"
        custom-class="toolbar-help"
        v-model="vData.checkHelpBook"
    >
        <h3 class="mb10">工具栏:</h3>
        <p>通过点击工具栏按钮可完成对画布和流程的编辑, 如缩放, 移动等</p>

        <h3 class="mb10 mt15">手势:</h3>
        <p>1. 支持触摸板的设备可以通过手势完成画布操作</p>
        <p>2. <i class="iconfont icon-fingers-zoom"></i> = 缩放画布</p>
        <p>3. <i class="iconfont icon-fingers-move"></i> = 画布万向滚动</p>

        <h3 class="mb10 mt15">鼠标:</h3>
        <p>1. 支持鼠标操作画布</p>
        <p>2. 按住 <i class="iconfont icon-left-btn"></i> = 拖动画布或节点</p>

        <h3 class="mb10 mt15">键盘快捷键:</h3>
        <p><i class="iconfont icon-shift"></i> + <i class="iconfont icon-left-click"></i> = 框选节点</p>
    </el-dialog>
</template>

<script>
    import {
        ref,
        reactive,
        onBeforeMount,
        onBeforeUnmount,
        getCurrentInstance,
        nextTick,
    } from 'vue';
    import { useRouter } from 'vue-router';
    import {
        Graph,
        registerNode,
        registerEdge,
        registerBehavior,
    } from '@antv/g6';
    import g6Register from 'welabx-g6';

    export default {
        props: {
            reference: String, // Parent reference
            projectId: String,
            flowId:    String,
            myRole:    String,
            isCreator: Boolean,
            ootJobId:  String,
            graph:     Object,
            minimap:   {
                type:    Boolean,
                default: true,
            },
        },
        emits: ['excute', 'switchJobGraphPanel', 'reset-graph-state', 'job-change', 'reset-graph-state', 'job-running'],
        setup(props, context) {
            const { appContext } = getCurrentInstance();
            const {
                $bus,
                $http,
                $message,
            } = appContext.config.globalProperties;
            const router = useRouter();
            const jobPreview = ref();
            const vData = reactive({
                job_id:         '',
                stack:          [],
                checkHelpBook:  false,
                loading:        false,
                locker:         false,
                isPaused:       false,
                jobStatus:      '',
                jobShouldStart: false,
                jobUseCache:    false,
            });
            const jobStopStatus = ['stop_on_running', 'error_on_running', 'reject_on_auditing', 'deleted', 'error', 'success', 'stop'];

            let heatbeatTimer = null,
                endNodeItem,
                endNodeId,
                previewGraph;

            const methods = {
                init() {
                    methods.getJobStatus(data => {
                        if(data && data.job) {
                            vData.job_id = data.job.job_id;
                            vData.jobStatus = data.job.status;
                            $bus.$emit('update-job-id', vData.job_id);
                        }
                    });
                },

                excute(command, shouldExcute) {
                    shouldExcute !== 'disabled' && context.emit('excute', command);
                },

                openWindow(name) {
                    const { href } = router.resolve({
                        name,
                        query: {
                            project_id: props.projectId,
                            flow_id:    props.flowId,
                        },
                    });

                    window.open(href, '_blank');
                },

                async shouldStart($event, node) {
                    if(vData.jobStatus === 'wait_stop') return;
                    if(node) {
                        endNodeItem = node;
                        endNodeId = node.getModel().id;
                    } else {
                        endNodeItem = null;
                        endNodeId = null;
                    }

                    if(vData.job_id) {
                        if(vData.locker) return;
                        vData.loading = true;
                        vData.locker = true;

                        const params = {
                            flow_id:   props.flowId,
                            use_cache: true,
                        };

                        if(endNodeId) {
                            // Run here
                            params.endNodeId = endNodeId;
                        }

                        const { code, data } = await $http.get({
                            url: '/project/flow/job/preview',
                            params,
                        });

                        nextTick(_ => {
                            vData.loading = false;
                            vData.locker = false;
                            if(code === 0) {
                                // If you click the toolbar start button
                                if($event) {
                                    vData.jobUseCache = data.has_cache_result_count > 0;
                                }

                                vData.jobShouldStart = true;

                                const nodeIds = {};

                                data.list.filter(item => {
                                    nodeIds[item.node_id] = item;
                                });

                                // Initialize Preview
                                methods.initPreviewGraph(nodeIds);
                            } else {
                                methods.play(null, { useCache: false });
                            }
                        });
                    } else {
                        methods.play(null, { useCache: false });
                    }
                },

                async play($event, opt = { useCache: false }) {
                    if(vData.locker) return;
                    vData.locker = true;
                    vData.jobShouldStart = false;
                    $bus.$emit('node-error', { clear: true });
                    // start job
                    $bus.$emit('job-change', { waiting: true, text: '任务启动中, 请稍候...' });

                    if(props.reference === 'main') {
                        // After the main panel is started, propagate events to let the methods in the task panel execute
                        $bus.$emit('job-play', { ...opt, endNodeId });

                        setTimeout(() => {
                            vData.locker = false;
                        }, 300);
                    } else {
                        const params = {
                            flowId:   props.flowId,
                            useCache: opt.useCache,
                        };

                        if(props.ootJobId) {
                            params.ootJobId = props.ootJobId;
                        }
                        // run here
                        if(endNodeId) {
                            params.endNodeId = endNodeId;
                        }

                        const { code, data } = await $http.post({
                            url:     '/flow/start',
                            timeout: 1000 * 30,
                            data:    params,
                        });

                        // job started
                        $bus.$emit('job-change', { waiting: false, text: '' });
                        nextTick(_ => {
                            vData.locker = false;
                            if(code === 0) {
                                if(data.job_id) {
                                    vData.isPaused = true;
                                    vData.job_id = data.job_id;
                                    $message.success('启动成功! ');
                                    $bus.$emit('update-job-id', data.job_id);
                                    context.emit('switchJobGraphPanel', true);
                                    context.emit('reset-graph-state');
                                    methods.getJobStatus();
                                }
                            }
                        });
                    }
                },

                async pause() {
                    if(vData.jobStatus === 'wait_stop') return;
                    if(vData.locker) return;
                    vData.locker = true;
                    // pause job
                    context.emit('job-change', { waiting: true, text: '任务暂停中, 请稍后...' });

                    if(props.reference === 'main') {
                        // After the main panel is started, propagate events to let the methods in the task panel execute
                        $bus.$emit('job-pause');

                        setTimeout(() => {
                            vData.locker = false;
                        }, 300);
                    } else {
                        const { code } = await $http.post({
                            url:  '/flow/job/stop',
                            data: {
                                jobId: vData.job_id,
                            },
                        });

                        // job paused
                        context.emit('job-change', { waiting: false, text: '' });

                        nextTick(_ => {
                            vData.locker = false;
                            if(code === 0) {
                                clearTimeout(heatbeatTimer);
                                $message.success('已暂停执行');
                                methods.getJobStatus();
                            }
                        });

                        return code;
                    }
                },

                async restart() {
                    if(!vData.jobStatus) return;
                    if(!jobStopStatus.includes(vData.jobStatus)) {
                        // Abnormal state
                        await methods.pause();
                    }
                    methods.play(null, { useCache: false });
                },

                jobUseCacheChange() {
                    methods.shouldStart(false, endNodeItem);
                },

                // Check task status heartbeat
                heartbeat() {
                    clearTimeout(heatbeatTimer);

                    heatbeatTimer = setTimeout(() => {
                        methods.getJobStatus();
                    }, 2000);
                },

                async getJobStatus(callback) {
                    const { code, data } = await $http.get({
                        url:    '/flow/job/detail',
                        params: {
                            requestFromRefresh: true,
                            flowId:             props.flowId,
                            memberRole:         props.myRole,
                            needResult:         true,
                        },
                    });

                    if(code === 0) {
                        callback && callback(data);
                        if(data) {
                            const { job } = data;

                            if(job) {
                                // Display error prompt panel
                                const { status, message } = job;

                                vData.jobStatus = status;
                                if(jobStopStatus.includes(status)) {
                                    let msg = '';

                                    if(message) {
                                        msg = message;
                                    } else if(vData.jobStatus === 'success'){
                                        msg = '运行成功! ';
                                    }
                                    $bus.$emit('job-finished', {
                                        status:  vData.jobStatus === 'success' ? 'success' : 'error',
                                        message: msg,
                                    });
                                }

                                const res = await $http.get({
                                    url:    '/flow/job/get_progress',
                                    params: {
                                        requestFromRefresh: true,
                                        jobId:              job.job_id,
                                    },
                                });

                                nextTick(_ => {
                                    if(res.code === 0) {
                                        vData.jobStatus = job.status;
                                        if(!jobStopStatus.includes(job.status)) {
                                            methods.heartbeat();
                                        }
                                        /* merge the job status and member process */
                                        if(data.task_views && res.data) {
                                            methods.setJobStatus(data, res.data);
                                        }
                                    }
                                });
                            }
                        } else {
                            context.emit('job-running', { status: false });
                        }
                    }
                },

                setJobStatus({ job, task_views }, members = []) {
                    const nodes = {};

                    task_views.map(item => {
                        const task = item.task || {};
                        const node = job.graph.nodes.find(node => node.id === item.task.flow_node_id);

                        switch(task.status) {
                        case 'created':
                        case 'auditing':
                        case 'audited':
                        case 'building_task':
                        case 'wait_run':
                            task.icon = 'waiting';
                            task.run_status = 'wait';
                            task.statusZh = '等待运行';
                            break;
                        case 'running':
                        case 'wait_stop':
                            task.icon = 'running';
                            task.run_status = 'running';
                            task.statusZh = '正在运行';
                            methods.taskProgress(node, task);
                            break;
                        case 'stop':
                            task.icon = 'stop';
                            task.run_status = 'stop';
                            task.statusZh = '已结束运行';
                            break;
                        case 'timeout':
                        case 'stop_on_running':
                        case 'error_on_running':
                        case 'reject_on_auditing':
                        case 'error':
                        case 'deleted':
                            task.icon = 'failed';
                            task.run_status = 'failed';
                            task.statusZh = '运行失败';
                            break;
                        case 'success':
                            task.icon = 'ok';
                            task.run_status = 'success';
                            task.statusZh = '运行成功';
                            break;
                        /* default:
                        task.icon = 'running';
                        task.run_status = 'running';
                        task.statusZh = '正在运行';
                        break; */
                        }

                        nodes[item.task.flow_node_id] = {
                            node: JSON.parse(JSON.stringify(node)),
                            task,
                        };

                        return {
                            node: item.node,
                            task,
                        };
                    });
                    context.emit('job-running', { status: true, job, nodes, members });
                },

                async taskProgress({ id }, { task_id }) {
                    const { code, data } = await $http.get({
                        url:    '/task/progress/detail',
                        params: {
                            task_id,
                            memberRole:         props.myRole,
                            requestFromRefresh: true,
                        },
                    });

                    if(code === 0) {
                        $bus.$emit('jobProgressUpdate', { node_id: id, data });
                    }
                },

                initPreviewGraph(ids) {
                    if(previewGraph) {
                        previewGraph.destroy();
                    }

                    if(props.graph.instance) {
                        nextTick(async _ => {
                            const { nodes, edges } = JSON.parse(JSON.stringify(props.graph.instance.save()));

                            if(jobPreview.value) {
                                const config = g6Register({
                                    registerNode,
                                    registerEdge,
                                    registerBehavior,
                                }, {
                                    container:   jobPreview.value,
                                    width:       jobPreview.value.offsetWidth,
                                    height:      jobPreview.value.offsetHeight,
                                    defaultNode: {
                                        type:         'flow-node',
                                        anchorPoints: [
                                            [0.5, 0],
                                            [0.5, 1],
                                        ],
                                        style: {
                                            cursor: 'pointer',
                                            fill:   '#ecf3ff',
                                            width:  100,
                                        },
                                        labelCfg: {
                                            style: {
                                                fill:   '#4483FF',
                                                cursor: 'pointer',
                                            },
                                        },
                                    },
                                    nodeStateStyles: {
                                        active: {
                                            fill: '#ecf3ff',
                                        },
                                    },
                                    defaultEdge: {
                                        type:  'cubic-edge',
                                        style: {
                                            endArrow: true,
                                        },
                                    },
                                    modes: {
                                        default: [
                                            'scroll-canvas',
                                            'drag-node',
                                            'drag-canvas',
                                            {
                                                type: 'tooltip',
                                                formatText({ preview }) {
                                                    const html = preview ? `
                                                    <div class="preview-context text-l">
                                                    <p>组件名称: ${preview.component_name}</p>
                                                    <p>是否已缓存: ${preview.has_cache_result ? '是' : '否'}</p>
                                                    <p>执行深度: ${preview.deep}</p>
                                                    <p>执行顺序: ${preview.position}</p>
                                                    <p>input: ${JSON.stringify(preview.input)}</p>
                                                    </div>` : '';

                                                    return html;
                                                },
                                                offset: 20,
                                            },
                                        ],
                                    },
                                    layout: {
                                        type: '',
                                    },
                                    fitCenter: true,
                                });

                                previewGraph = new Graph(config);

                                nodes.forEach(node => {
                                    node.preview = ids[node.id];
                                });

                                previewGraph.read({
                                    nodes,
                                    edges,
                                });

                                /* bind events */
                                previewGraph.on('afterrender', e => {
                                    previewGraph.getNodes().forEach(node => {
                                        const model = node.getModel();

                                        // nocache
                                        if(model.id !== 'start' && ids[model.id] && !ids[model.id].has_cache_result) {
                                            // Open Preview
                                            if(vData.jobUseCache) {
                                                model.style.fill = '#ccc';
                                                model.style.stroke = '#ccc';
                                                model.labelCfg.style.fill = '#fff';
                                            } else {
                                                model.style.fill = '#ecf3ff';
                                                model.style.stroke = '#4483FF';
                                                model.labelCfg.style.fill = '#4483FF';
                                            }

                                            node.update(model);
                                        }
                                    });
                                });

                            }
                        });
                    }
                },

                updateJobDetail(job) {
                    vData.job_id = job.job_id;
                    vData.jobStatus = job.status;
                },
            };

            onBeforeMount(() => {
                if(props.reference === 'main') {
                    $bus.$on('update-job-detail', data => {
                        methods.updateJobDetail(data);
                    });
                }

                // Cross component communication
                if(props.reference === 'job-panel') {
                    $bus.$on('job-play', opt => {
                        endNodeId = opt.endNodeId;
                        methods.play(null, opt);
                    });

                    $bus.$on('job-pause', () => {
                        methods.pause();
                    });
                }
            });

            onBeforeUnmount(() => {
                clearTimeout(heatbeatTimer);
                $bus.$off('update-job-detail');
                $bus.$off('job-play');
                $bus.$off('job-pause');
            });

            return {
                jobPreview,
                jobStopStatus,
                vData,
                methods,
            };
        },
    };
</script>

<style lang="scss" scoped>
    .top-bar{
        text-align: center;
        position: absolute;
        top: 0;
        left: 30px;
        right: 30px;
        z-index: 15;
        user-select: none;
        min-width:46px;
    }
    .toolbar{
        padding: 0 8px;
        border-radius: 4px;
        border: 1px solid #f0f0f0;
        display: inline-block;
        margin: 10px auto 0;
        background: #fff;
        position: relative;
        color:$color-light;
        z-index: 2;
        .iconfont{
            width: 30px;
            height: 30px;
            font-size: 20px;
            line-height: 30px;
            border-radius:4px;
            color: #4088fc;
            cursor: pointer;
            text-align: center;
            display: inline-block;
            &:hover{background: #f0f0f0;}
            &.disabled {
                color:#999;
                opacity: 0.5;
                cursor: not-allowed;
            }
            &.inactive {color:#999;}
        }
        :deep(.el-loading-spinner){margin-top:-12px;}
        :deep(.circular){
            width: 20px;
            height: 20px;
        }
    }
    #job-preview{
        position: absolute;
        top: 0;
        left: 0;
        width: 100%;
        height: 100%;
        background: #F5F7FA;
        transition-duration: 0.2s;
        transform: translateY(-100%);
        border:1px solid #ccc;
        text-align: right;
        font-size: 14px;
        padding:15px;
        z-index: 20;
        &.active{transform: translateY(0);}
    }
    .graph-preview{
        position: absolute;
        top:0;
        left:0;
        width:100%;
        bottom: 100px;
        overflow: hidden;
        background: #fff;
        :deep(.g6-tooltip){
            background: #fff;
            border-radius: 4px;
            border: 1px solid $border-color-base;
            max-width: 300px;
        }
        :deep(.preview-context){
            font-size: 12px;
            line-height: 1.4;
            padding: 10px;
        }
    }
    .preview-options{
        position: absolute;
        width: 100%;
        left: 0;
        bottom: 0;
        padding: 15px;
    }
    .toolbar-help{
        .iconfont{
            font-size:26px;
            vertical-align: middle;
        }
        p{margin-bottom: 5px;}
    }
</style>
