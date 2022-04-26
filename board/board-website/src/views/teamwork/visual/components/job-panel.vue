<template>
    <div
        id="job-graph-wrap"
        :class="{show: jobGraphShow}"
    >
        <div class="toolbar-ref">
            <ToolBar
                ref="ToolbarRef"
                reference="job-panel"
                :minimap="vData.showMinimap"
                :project-id="projectId"
                :oot-job-id="ootJobId"
                :flow-id="flowId"
                :my-role="myRole"
                :graph="mainGraph"
                :is-creator="isCreator"
                @excute="methods.excute"
                @job-running="methods.jobRunning"
                @reset-graph-state="methods.resetGraphState"
                @switchJobGraphPanel="methods.switchJobGraphPanel"
            />
        </div>
        <div v-if="vData.jobHistoryLoading" class="job-info">
            <el-scrollbar>
                <div class="p20">
                    当前任务ID
                    <p class="f12 mb10">{{ vData.jobId }}</p>
                    <DownloadJobLog
                        :job-id="vData.jobId"
                        size="small"
                    />
                </div>
                <template v-if="vData.jobHistoryList.length">
                    <span class="ml20">任务历史 <span class="f12">(共{{ vData.jobHistory.total }}条)</span></span>
                    <el-collapse
                        class="job-history"
                        accordion
                        v-model="vData.currentJob"
                    >
                        <el-collapse-item
                            v-for="item in vData.jobHistoryList"
                            :key="item.job_id"
                            :name="item.job_id"
                            :title="`${item.my_role} - ${item.job_id}`"
                            @click="methods.jobHistoryDetail(item)"
                        >
                            <el-form
                                class="job-history-info"
                                label-width="70px"
                            >
                                <el-form-item label="创建者：">
                                    {{ item.creator_nickname }}
                                </el-form-item>
                                <el-form-item label="开始时间：">
                                    {{ dateFormat(item.start_time) }}
                                </el-form-item>
                                <el-form-item label="任务进度：">
                                    {{ item.progress }}
                                </el-form-item>
                                <el-form-item label="任务状态：">
                                    {{ item.status }}
                                </el-form-item>
                                <el-form-item
                                    v-if="item.message"
                                    label="任务消息："
                                >
                                    {{ item.message.length > 100 ? `${item.message.substring(0, 100)}...` : item.message }}
                                </el-form-item>
                            </el-form>
                            <el-button
                                size="small"
                                class="mt10"
                                @click="methods.toJobDetails(item)"
                            >
                                查看更多
                                <el-icon>
                                    <elicon-top-right />
                                </el-icon>
                            </el-button>
                            <!-- TODO:
                                <span>
                                <el-button
                                    type="primary"
                                    class="mt10"
                                    size="small"
                                    @click="methods.reEdit(item)"
                                >
                                    编辑此任务
                                </el-button>
                            </span> -->
                        </el-collapse-item>

                        <el-pagination
                            v-if="vData.jobHistory.total"
                            class="history-pagination"
                            layout="prev, pager, next"
                            :total="vData.jobHistory.total"
                            :pager-count="2"
                            :page-sizes="[10, 20, 30, 40, 50]"
                            :page-size="vData.jobHistory.page_size"
                            :current-page="vData.jobHistory.page_index"
                            @current-change="methods.currentPageChange"
                        />
                    </el-collapse>
                </template>
            </el-scrollbar>
        </div>
        <div
            id="job-graph"
            ref="jobGraphRef"
        >
            <div class="job-legend-name-list">
                <p
                    v-for="(member, index) in vData.members"
                    :key="`${member.member_id}-${member.job_role}`"
                    :class="['job-legend-name', { 'promoter': member.job_role === 'promoter', 'disabled': !member.$visible }]"
                    @click="methods.changeMemberVisible(member)"
                >
                    <i :class="['iconfont', `icon-user-${index + 1}`]" />
                    <span class="name">{{ member.member_name }} ({{ member.job_role }})</span>
                    <el-tooltip v-if="member.message && !member.message.includes('success')" effect="light">
                        <template #content>
                            <p>{{member.message}}</p>
                        </template>
                        <span style="color: #222;">
                            <el-icon class="color-danger" style="padding: 0 2px;">
                                <elicon-warning />
                            </el-icon>
                            {{member.message}}
                        </span>
                    </el-tooltip>
                </p>
            </div>
        </div>
        <div
            id="job-minimap"
            ref="jobMinimap"
            :class="['minimap', { inactive: !vData.showMinimap }]"
        />
        <el-button
            class="graph-switch"
            :disabled="vData.jobInfo.status === 'running'"
            @click="methods.switchJobGraphPanel(false)"
        >
            返回编辑状态
        </el-button>

        <NodeResultHistory
            ref="NodeResultHistoryRef"
            :componentType="componentType"
            :currentObj="currentObj"
            :flowId="flowId"
            :myRole="myRole"
        />
    </div>
</template>

<script>
    import {
        ref,
        reactive,
        nextTick,
        onMounted,
        onBeforeUnmount,
        getCurrentInstance,
    } from 'vue';
    import { useRoute, useRouter } from 'vue-router';
    import {
        Menu,
        Minimap,
        Graph,
        Util,
        Tooltip,
        registerNode,
        registerEdge,
        registerBehavior,
    } from '@antv/g6';
    import g6Register from 'welabx-g6';
    import ToolBar from './toolbar';
    import toolbarMixin from '../graph/toolbar.mixin';
    import nodeStateApply from '../graph/node-state-apply';
    import DownloadJobLog from '../../components/download-job-log';
    import NodeResultHistory from '../components/node-result-history';

    export default {
        components: {
            ToolBar,
            DownloadJobLog,
            NodeResultHistory,
        },
        props: {
            currentObj:    Object,
            jobDetail:     Object,
            componentType: String,
            projectId:     String,
            flowId:        String,
            myRole:        String,
            isCreator:     Boolean,
            jobGraphShow:  Boolean,
            ootJobId:      String,
            mainGraph:     Object,
            parentCanvas:  Object,
        },
        emits: ['resetGraphState', 'switchJobGraphPanel', 'checkResult', 'checkHelp', 'switchComponent', 'graphInit'],
        setup (props, context) {
            const { appContext } = getCurrentInstance();
            const {
                $bus,
                $http,
                $confirm,
                $message,
                dateFormat,
            } = appContext.config.globalProperties;
            const router = useRouter();
            const route = useRoute();
            const vData = reactive({
                jobId:          '',
                showName:       true,
                legendIsShow:   true,
                showMinimap:    true,
                members:        [],
                progressDetail: {
                    visible: false,
                },
                jobInfo:           {},
                jobHistoryList:    [],
                jobHistoryLoading: false,
                jobHistory:        {
                    total:      0,
                    page_size:  10,
                    page_index: 1,
                },
                currentJob: route.query.job_id || '',
            });
            const jobMinimap = ref();
            const jobGraphRef = ref();
            const ToolbarRef = ref();
            const NodeResultHistoryRef = ref();
            const graph = {
                instance: null, // !Keep object references
            };

            let jobNodes = {},
                jobGraph;

            const methods = {
                excute (command) {
                    methods[command]();
                },

                changeMemberVisible(member) {
                    member.$visible = !member.$visible;
                },

                resetGraphState(data) {
                    vData.members.forEach(member => member.message = '');
                    context.emit('resetGraphState', data);
                },

                switchJobGraphPanel(flag) {
                    context.emit('switchJobGraphPanel', flag);
                },

                async initJobGraph (data = { nodes: [], edges: [], combos: [] }) {
                    if (jobGraphRef.value) {
                        const minimap = new Minimap({
                            container: jobMinimap.value,
                            size:      [200, 100],
                        });
                        const menu = methods.createContextMenu();
                        const tooltip = methods.createTooltip();
                        const config = g6Register({
                            registerNode,
                            registerEdge,
                            registerBehavior,
                        }, {
                            container:   jobGraphRef.value,
                            width:       props.parentCanvas.offsetWidth,
                            height:      props.parentCanvas.offsetHeight,
                            defaultNode: {
                                type:         'flow-node',// 'rect-node',
                                anchorPoints: [
                                    [0.5, 0],
                                    [0.5, 1],
                                ],
                                style: {
                                    fill:  '#ecf3ff',
                                    width: 100,
                                },
                                labelCfg: {
                                    fill: '#4483FF',
                                },
                                // fitCenter: true,
                            },
                            nodeStateStyles: {
                                'nodeState:default': {
                                    lineWidth: 1,
                                    labelCfg:  {
                                        style: {
                                            fontWeight: 'normal',
                                        },
                                    },
                                },
                                'nodeState:selected': {
                                    lineWidth: 2,
                                    labelCfg:  {
                                        style: {
                                            fontWeight: 'bold',
                                        },
                                    },
                                },
                                'nodeState:active': {
                                    lineWidth: 2,
                                    labelCfg:  {
                                        style: {
                                            fontWeight: 'bold',
                                        },
                                    },
                                },
                            },
                            defaultEdge: {
                                type:  'cubic-edge',
                                style: {
                                    stroke:   '#b7c0ca',
                                    endArrow: true,
                                },
                            },
                            modes: {
                                default: ['brush-select', 'scroll-canvas', 'drag-canvas', 'drag-shadow-node', 'canvas-event', 'delete-item', 'select-node', 'hover-node', 'active-edge'],
                            },
                            layout: {
                                type: '',
                            },
                            plugins:   [menu, minimap, tooltip],
                            fitCenter: true,
                        });

                        // Inherit built-in nodes, add node status
                        nodeStateApply({ registerNode });

                        graph.instance = jobGraph = new Graph(config); // let toolbarmixin get currect graph
                        jobGraph.removeBehaviors(['drag-node', 'hover-node'], 'default');
                        jobGraph.get('canvas').set('localRefresh', false); // close local refresh
                        methods.addEvents();
                        nextTick(_ => {
                            data.nodes.forEach(node => {
                                node.type = 'flow-node';
                            });
                            jobGraph.read(data);
                        });
                    }
                },

                createContextMenu () {
                    return new Menu({
                        offsetX: -185,
                        offsetY: -64,
                        shouldBegin (e) {
                            let shouldBegin = true;

                            if (e.item) {
                                const type = e.item.get('type');

                                if (type === 'node') {
                                    const { nodeType, task } = e.item.getModel().data;
                                    const readonly = [];

                                    // cannot copy|delete system node
                                    if (nodeType === 'system' || readonly[task]) {
                                        shouldBegin = false;
                                    } else {
                                        shouldBegin = true;
                                    }
                                }
                                return shouldBegin;
                            }
                        },
                        getContent (e) {
                            const commands = [{
                                command: 'checkResult',
                                name:    '查看执行结果',
                            }, {
                                command: 'checkResultHistory',
                                name:    '查看执行历史',
                            }, {
                                command: 'checkHelp',
                                icon:    'icon el-icon-help',
                                name:    '帮助文档',
                            }];

                            let menus = '';

                            commands.forEach(item => {
                                menus += `<p class="menu-item" command="${item.command}">${item.icon ? `<i class="${item.icon}" command="${item.command}"></i>` : ''}${item.name}</p>`;
                            });

                            return menus;
                        },
                        handleMenuClick (target, item) {
                            const command = target.getAttribute('command');

                            methods[command] && methods[command](item);
                        },
                    });
                },

                createTooltip() {
                    return new Tooltip({
                        offsetX:   -140,
                        offsetY:   -25,
                        itemTypes: ['node'],
                        shouldBegin({ item }) {
                            const group = item.getContainer();
                            const children = group.get('children');
                            const index = children.findIndex(shape => {
                                return shape.cfg.className === 'progress';
                            });

                            return index >= 0;
                        },
                        getContent({ item }) {
                            const group = item.getContainer();
                            const children = group.get('children');
                            const shape = children.find(shape => {
                                return shape.cfg.className === 'progress';
                            });

                            if(shape) {
                                return `<li>开始时间: ${ shape.cfg.$toolTipData.startTime }</li>
                                <li>预计结束时间: ${ shape.cfg.$toolTipData.endTime }</li>`;
                            } else {
                                return '';
                            }
                        },
                    });
                },

                checkResultHistory(item) {
                    const model = item.getModel();

                    NodeResultHistoryRef.value.getResultHistory(model.id);
                },

                // check node job results
                checkResult(item) {
                    context.emit('checkResult', item);
                },

                // Help documentation
                checkHelp(item) {
                    context.emit('checkHelp', item);
                },

                addEvents () {
                    jobGraph.on('afterrender', e => {
                        setTimeout(() => {
                            // merge job state
                            if (Object.keys(jobNodes).length) {
                                methods.mergeJobStatus();
                            }
                        });
                    });

                    jobGraph.on('after-node-selected', e => {
                        if (e && e.item) {
                            const model = e.item.getModel();

                            if (model.data.nodeType === 'system') {
                                context.emit('resetGraphState');
                            } else {
                                // switch components
                                context.emit('checkResult', e.item);
                            }
                        }
                    });

                    jobGraph.on('node:mouseenter', e => {
                        const group = e.item.getContainer();
                        const children = group.get('children');
                        const progressBar = children.find(child => child.cfg.className === 'progress');

                        // If it is a node with a progress bar, the floating window information is displayed
                        vData.progressDetail.visible = !!progressBar;
                        if(progressBar) {
                            vData.progressDetail.x = e.x;
                            vData.progressDetail.y = e.y;
                        }
                    });

                    jobGraph.on('click', e => {
                        if(!e.item) {
                            context.emit('resetGraphState');
                            graph.instance.getNodes().forEach(node => {
                                graph.instance.clearItemStates(node);
                            });
                            graph.instance.getEdges().forEach(edge => {
                                graph.instance.clearItemStates(edge);
                            });
                        }
                    });
                },

                /* toggle minimap */
                switchMinimap() {
                    vData.showMinimap = !vData.showMinimap;
                },

                jobRunning ({ status, job, nodes, members }) {
                    if(job) {
                        ToolbarRef.value.methods.updateJobDetail(job);
                        $bus.$emit('update-job-detail', job);
                    }
                    if (status) {
                        // job has running status
                        vData.jobInfo = job;
                        vData.jobId = job.job_id;
                        jobNodes = nodes;
                        if(vData.members.length === 0) {
                            // direct coverage
                            vData.members = members.map(member => {
                                return {
                                    ...member,
                                    $visible: vData.members.length === 0,
                                };
                            });
                        } else {
                            // remerge
                            // remove non-existent members
                            for(let i = 0, { length } = vData.members; i < length; i++) {
                                const row = vData.members[i];

                                if(row) {
                                    const exist = members.findIndex(member => member.member_id === row.member_id);

                                    if(!~exist) {
                                        vData.members.splice(i, 1);
                                        i--;
                                    }
                                }
                            }
                            // add again
                            members.forEach(member => {
                                const sameIndex = vData.members.findIndex(row => row.member_id === member.member_id);

                                if(sameIndex >= 0) {
                                    // If there are the same members
                                    const sameMember = vData.members[sameIndex];

                                    sameMember.node_id = member.node_id;
                                    vData.members[sameIndex] = {
                                        ...sameMember,
                                    };
                                } else {
                                    // If it is a new member
                                    vData.members.push({
                                        ...member,
                                        $visible: true,
                                    });
                                }
                            });
                        }

                        const { graph: { nodes: _nodes, edges, combos } } = job;

                        if (jobGraph) {
                            if (jobGraph.save().nodes.length) {
                                // merge task status
                                methods.mergeJobStatus();
                            } else {
                                // Reread data
                                _nodes.forEach(node => {
                                    // Set to new node type
                                    node.type = 'flow-node';
                                });
                                jobGraph.read({ nodes: _nodes, edges, combos });
                            }
                        } else {
                            methods.initJobGraph({ nodes: _nodes, edges, combos });
                        }
                        // Run again after solving the task error report, and the member error report status is not updated
                        if (job.status !== 'success') {
                            vData.members = members.map(member => {
                                return {
                                    ...member,
                                    $visible: vData.members.length,
                                };
                            });
                        }
                    } else {
                        vData.members.forEach(member => member.message = '');
                        if(jobGraph) {
                            jobGraph.clear();
                        } else {
                            methods.initJobGraph({ nodes: [] });
                        }
                    }
                },

                mergeJobStatus () {
                    jobGraph.getNodes().forEach((item, index) => {
                        const model = item.getModel();
                        const { id } = model;

                        if (index === 0) {
                            jobGraph.setItemState(item, 'nodeStatus', 'success');
                        } else if (jobNodes[id]) {
                            const { task: { run_status } } = jobNodes[id];

                            if (run_status) {
                                jobGraph.setItemState(item, 'nodeStatus', run_status);

                                // Remove progress bar after success
                                if(run_status === 'success') {
                                    const group = item.getContainer();
                                    const children = group.get('children');

                                    children.forEach(shape => {
                                        if(shape.cfg.className === 'progress') {
                                            shape.remove();
                                        }
                                    });
                                    children.forEach(shape => {
                                        if(shape.cfg.className === 'progress-text') {
                                            shape.remove();
                                        }
                                    });
                                }
                            } else {
                                jobGraph.setItemState(item, 'nodeStatus', 'default');
                            }
                            methods.addMemberInfo();
                        }
                    });
                },

                addMemberInfo() {
                    // Character set corresponding to user Avatar
                    const userCharcode = {
                        1:  '\ue602',
                        2:  '\ue606',
                        3:  '\ue607',
                        4:  '\ue603',
                        5:  '\ue605',
                        6:  '\ue609',
                        7:  '\ue60b',
                        8:  '\ue60a',
                        9:  '\ue60c',
                        10: '\ue60d',
                    };

                    jobGraph.getNodes().forEach(item => {
                        const group = item.getContainer();
                        const children = group.get('children');

                        // Remove member avatars from all nodes
                        children.forEach(shape => {
                            if(shape.cfg.className === 'member') {
                                shape.remove();
                            }
                        });
                    });

                    // add member avatars
                    vData.members.forEach((member, index) => {
                        if(member.$visible) {
                            const item = jobGraph.findById(member.node_id || 'start');

                            if(item) {
                                const model = item.getModel();
                                const group = item.getContainer();

                                group.addShape('text', {
                                    attrs: {
                                        fontFamily: 'iconfont',
                                        text:       userCharcode[index + 1],
                                        fill:       member.job_role === 'promoter' ? '#4c84ff' : '#909399',
                                        x:          model.style.width / 2 + 20 * (index + 1),
                                        y:          7,
                                    },
                                    className: 'member',
                                });
                            }
                        }
                    });
                },

                updateRunningProgress(data) {
                    // Remove progress bars for all nodes
                    jobGraph.getNodes().forEach(item => {
                        const group = item.getContainer();
                        const children = group.get('children');

                        children.forEach(shape => {
                            if(shape.cfg.className === 'progress') {
                                shape.remove();
                            }
                        });
                        children.forEach(shape => {
                            if(shape.cfg.className === 'progress-text') {
                                shape.remove();
                            }
                        });
                    });

                    if(!data.data) return;
                    // Add a progress bar to a running node
                    const { created_time, expect_end_time } = data.data;
                    const item = jobGraph.findById(data.node_id);
                    const { style: { width } } = item.getModel();
                    const group = item.getContainer();

                    const percentage = data.data.progress_rate;
                    const percentageSize = Util.getTextSize(`${percentage}%`, 14);

                    // Add a progress bar
                    group.addShape('rect', {
                        attrs: {
                            x:      -width / 2,
                            y:      17,
                            fill:   '#f39c12',
                            width:  percentage * width / 100,
                            height: 3,
                        },
                        className:    'progress',
                        $toolTipData: {
                            startTime: created_time ? dateFormat(created_time) : '',
                            endTime:   expect_end_time ? dateFormat(expect_end_time) : '正在评估...',
                        },
                    });

                    group.addShape('text', {
                        attrs: {
                            x:           -percentageSize[0] / 2,
                            y:           25,
                            fontSize:    14,
                            fontWeight:  'bold',
                            text:        `${percentage}%`,
                            fill:        '#e67e22',
                            stroke:      '#fff',
                            strokeWidth: 2,
                        },
                        className: 'progress-text',
                    });
                },

                async getJobHistory() {
                    vData.jobHistoryLoading = false;
                    const { code, data } = await $http.get({
                        url:    '/flow/job/query',
                        params: {
                            project_id: props.projectId,
                            flow_id:    props.flowId,
                            page_index: vData.jobHistory.page_index - 1,
                            page_size:  vData.jobHistory.page_size,
                        },
                    });

                    nextTick(_ => {
                        vData.jobHistoryLoading = true;
                        if(code === 0 && data.total) {
                            vData.jobHistoryList = data.list;
                            vData.jobHistory.total = data.total;
                        }
                    });
                },

                toJobDetails(item) {
                    const { href } = router.resolve({
                        name:  'project-job-detail',
                        query: {
                            member_role: props.myRole,
                            project_id:  props.projectId,
                            flow_id:     props.flowId,
                            job_id:      item.job_id,
                        },
                    });

                    window.open(href, '_blank');
                },

                reEdit(item) {
                    $confirm('重新编辑该任务将会导致当前画布内容全部丢失, 此操作不可恢复, 请谨慎操作!', '警告', {
                        type: 'warning',
                    }).then(() => {
                        $message.success('操作成功! 画布已恢复到历史状态');
                    });
                },

                currentPageChange(val) {
                    vData.jobHistory.page_index = val;
                    methods.getJobHistory();
                },

                async jobHistoryDetail(item) {
                    // loading
                    const { code, data } = await $http.get({
                        url:    '/flow/job/detail',
                        params: {
                            requestFromRefresh: true,
                            jobId:              item.job_id,
                            flowId:             item.flow_id,
                            member_role:        item.my_role,
                            needResult:         true,
                        },
                    });

                    nextTick(()=>{
                        if (code === 0 && data) {
                            // 未更新视图，后续有时间再整
                            // const _data = data.job.graph || {};
                            // const nodes = _data.nodes || [];
                            // const edges = _data.edges || [];
                            // const combos = _data.combos || [];

                            // methods.initJobGraph({ nodes, edges, combos });
                            // methods.createGraph({ nodes, edges, combos });
                            // context.emit('graphInit', { nodes, edges, combos }); // visual.vue中的job-panel组件上面添加了该方法
                        }
                    });
                    // context.emit('graphInit', { requestFromRefresh: false, job_id: item.job_id });
                },
            };

            toolbarMixin.mixin({
                graph,
                methods,
            });

            onMounted(_ => {
                methods.getJobHistory();

                if (vData.currentJob) {
                    setTimeout(()=> {
                        methods.switchJobGraphPanel(true);
                    }, 500);
                }

                $bus.$on('sideCollapsed', () => {
                    if (graph.instance) {
                        setTimeout(() => {
                            graph.instance.changeSize(jobGraphRef.value.offsetWidth, jobGraphRef.value.offsetHeight);
                        }, 300);
                    }
                });

                $bus.$on('jobProgressUpdate', data => {
                    methods.updateRunningProgress(data);
                });

                $bus.$on('update-job-id', id => {
                    jobGraph && jobGraph.clear();
                    vData.jobId = id;
                });

                $bus.$on('job-finished', data => {
                    methods.getJobHistory();
                });
            });

            onBeforeUnmount(_ => {
                $bus.$off('sideCollapsed');
                $bus.$off('jobProgressUpdate');
                $bus.$off('update-job-id');
            });

            return {
                vData,
                NodeResultHistoryRef,
                jobGraphRef,
                jobMinimap,
                ToolbarRef,
                methods,
            };
        },
    };
</script>

<style lang="scss" scoped>
.toolbar-ref{
    position: absolute;
    left: 200px;
    top:0;
    right:0;
    bottom:0;
}
.graph-switch{
    position: absolute;
    top: 10px;
    right:60px;
    z-index:15;
}
.minimap{
    position: absolute;
    z-index:12;
    right:0;
    bottom:-1px;
    background: #fff;
    border: 1px solid #ccc;
    &.inactive {display:none;}
    :deep(.g6-minimap){overflow: visible !important;}
}
#job-graph-wrap{
    height: 100%;
    width: 100%;
    position: absolute;
    top: 0;
    left:0;
    z-index: 15;
    transition-duration: 0.3s;
    transform: translateX(-100%);
    background: #f2f6fc;
    overflow: hidden;
    &.show{transform: translateX(0);}
}
#job-graph{
    overflow: auto;
    position: absolute;
    top: 45px;
    left:200px;
    right: 0;
    bottom: 0;
    z-index: 11;
    :deep(.g6-component-tooltip){
        margin-left: 10px;
    }
    :deep(li){
        list-style: none;
        line-height: 16px;
    }
}
.job-info{
    width: 200px;
    padding:10px 0;
    height:100%;
    background: #fff;
}
.job-history{
    padding:0 10px;
    margin-top: 10px;
    border-bottom: 0;
    :deep(.el-collapse-item__header){
        white-space: normal;
        word-break: break-all;
        line-height: 18px;
        &.is-active{
            color: $--color-primary;
        }
    }
}
.job-history-info{
    .el-form-item{
        display: flex;
        margin-bottom: 0;
    }
    :deep(.el-form-item__label),
    :deep(.el-form-item__content){
        font-size: 12px;
        line-height: 18px;
    }
}
.history-pagination{
    margin-top:10;
    text-align: right;
    :deep(.el-pagination__total){display:block;}
}
.job-legend-name-list{
    overflow: auto;
    max-height: 200px;
    position: absolute;
    top:0;
    left:20px;
}
.job-legend-name{
    font-size: 12px;
    max-width: 500px;
    height: 16px;
    line-height: 16px;
    margin-bottom:5px;
    overflow: hidden;
    white-space: nowrap;
    text-overflow: ellipsis;
    cursor: pointer;
    &.promoter{color: $color-link-base;}
    // &.disabled{color:$color-text-disabled;}
    .iconfont{
        font-size: 12px;
        margin-right:5px;
    }
}
</style>
