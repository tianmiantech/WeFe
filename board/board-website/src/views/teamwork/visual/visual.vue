<template>
    <div
        v-loading.fullscreen="vData.loading || vData.waiting"
        :element-loading-text="vData.loadingText"
        ref="PageRef"
        class="page"
    >
        <div
            v-show="!vData.jobGraphShow && !vData.componentsHide"
            class="el-card"
        >
            <div class="panel-title">组件库</div>
            <p :class="['panel-desc', { 'color-danger': vData.oot_job_id }]">{{ vData.oot_job_id ? 'OOT模式下禁止拖拽组件' : '拖动组件到右侧画布区' }}</p>
            <div :class="['components', { disabled: vData.oot_job_id }]">
                <ComponentTree
                    v-for="(item, $index) in vData.treeData"
                    :ref="el => { if (el) treeContainers[$index] = el }"
                    :key="$index"
                    :data="item"
                    @ready-to-drag="vData.dragover=true"
                    @drag-to-end="vData.dragover=false"
                />
            </div>
        </div>

        <div
            :class="['canvas-wrap', { draggable: vData.dragover, 'job-graph-show': vData.jobGraphShow }]"
            @click="methods.closeExplainPop"
        >
            <!-- Task reminder -->
            <transition name="dropIn">
                <div
                    v-if="vData.jobFinishedMessage.show"
                    :class="['job-alert', 'el-alert', 'is-dark', `el-alert--${vData.jobFinishedMessage.status}`]"
                >
                    <div class="el-alert__content">
                        <p
                            class="el-alert__title mr10"
                            v-html="`【${vData.jobFinishedMessage.status}】${vData.jobFinishedMessage.message}`"
                        />
                        <span
                            v-if="!vData.jobGraphShow"
                            class="check-job mt10 mb5"
                            @click="vData.jobFinishedMessage.show=false; vData.jobGraphShow=true"
                        >
                            点此查看任务详情
                        </span>
                        <el-icon
                            class="el-icon-close f16"
                            @click="vData.jobFinishedMessage.show=false"
                        >
                            <elicon-close />
                        </el-icon>
                    </div>
                </div>
            </transition>
            <ToolBar
                ref="ToolbarRef"
                reference="main"
                :minimap="vData.showMinimap"
                :is-creator="vData.is_creator"
                :project-id="vData.project_id"
                :oot-job-id="vData.oot_job_id"
                :flow-id="vData.flow_id"
                :my-role="vData.my_role"
                :graph="graph"
                @excute="methods.excute"
                @reset-graph-state="methods.resetGraphState"
                @switchJobGraphPanel="methods.switchJobGraphPanel"
                @job-running="methods.jobRunning"
            />
            <div
                id="canvas"
                ref="Canvas"
            />
            <div
                id="graph-minimap"
                ref="GraphMinimap"
                :class="['minimap', { inactive: !vData.showMinimap }]"
            />
            <div
                v-if="vData.paramsEmptynodes.length && vData.paramsEmptynodesPanel"
                id="graph-todos"
                class="f14 p10"
            >
                <el-icon
                    class="el-icon-close f18"
                    @click="vData.paramsEmptynodesPanel = false"
                >
                    <elicon-close />
                </el-icon>
                <p class="pb5"><strong class="f13">由于数据集发生变更<br>以下节点需要重新保存参数: </strong></p>
                <span
                    class="f12"
                    style="color: #c0c4cc;"
                >tips: 点击列表可快速定位节点 </span>😉
                <ul class="todo-list pt10 mt10">
                    <li
                        v-for="node in vData.paramsEmptynodes"
                        :key="node.id"
                        @click="methods.focusNode(node)"
                    >
                        <p>{{ node.component_name }}</p>
                        <span
                            class="f12"
                            style="color: #c0c4cc;"
                        >{{ node.node_id }}</span>
                    </li>
                </ul>
            </div>
            <!-- job view -->
            <JobPanel
                ref="JobPanelRef"
                :main-graph="graph"
                :flow-id="vData.flow_id"
                :my-role="vData.my_role"
                :parent-canvas="Canvas"
                :job-detail="vData.jobDetail"
                :is-creator="vData.is_creator"
                :project-id="vData.project_id"
                :oot-job-id="vData.oot_job_id"
                :current-obj="vData.currentObj"
                :component-type="vData.componentType"
                :job-graph-show="vData.jobGraphShow"
                @resetGraphState="methods.resetGraphState"
                @switchComponent="methods.switchComponent"
                @switchJobGraphPanel="methods.switchJobGraphPanel"
                @resizeCanvas="methods.resizeCanvas"
                @checkResult="methods.checkResult"
                @checkHelp="methods.checkHelp"
            />
            <!-- Error prompt box -->
            <ErrorPanel ref="errorPanel" />
        </div>
        <!-- Components Panel -->
        <ComponentsPanel
            ref="ComponentsPanel"
            :page-ref="PageRef"
            :oot-job-id="vData.oot_job_id"
            :oot-model-flow-node-id="vData.oot_model_flow_node_id"
            :component-type="vData.componentType"
            :job-graph-show="vData.jobGraphShow"
            :current-obj="vData.currentObj"
            :job-detail="vData.jobDetail"
            :is-creator="vData.is_creator"
            :my-role="vData.my_role"
            :flow-id="vData.flow_id"
            :job-id="vData.job_id"
            :project-id="vData.project_id"
            :old-learning-type="vData.learningType"
            @getComponents="methods.getComponents"
            @updateFlowInfo="methods.updateFlowInfo"
            @resetGraphState="methods.resetGraphState"
            @changeHeaderTitle="methods.changeHeaderTitle"
            @update-currentObj="methods.updateCurrentObj"
            @update-empty-params-node="methods.updateEmptyParamsNode"
            @component-panel-change-size="methods.componentPanelChangeSize"
            @remove-params-node="methods.removeParamsNode"
        />

        <el-dialog
            v-model="vData.failedDialog"
            destroy-on-close
            width="420px"
            title="警告"
            top="25vh"
        >
            <el-icon class="el-icon-warning f20">
                <elicon-warning-filled />
            </el-icon>
            流程初始化失败, 请重试!
            <div class="text-r">
                <el-button
                    type="primary"
                    @click="refresh"
                >
                    重试
                </el-button>
            </div>
        </el-dialog>
    </div>
</template>

<script>
    import {
        ref,
        reactive,
        nextTick,
        computed,
        onBeforeMount,
        onBeforeUnmount,
        getCurrentInstance,
    } from 'vue';
    import {
        useRoute,
        useRouter,
    } from 'vue-router';
    import { useStore } from 'vuex';
    import ToolBar from './components/toolbar';
    import ComponentTree from './component-tree';
    import JobPanel from './components/job-panel';
    import ErrorPanel from './components/error-panel';
    import ComponentsPanel from './components/components-panel';
    import toolbarMixin from './graph/toolbar.mixin';
    import graphMixin from './graph/graph.mixin';

    export default {
        components: {
            ToolBar,
            JobPanel,
            ErrorPanel,
            ComponentTree,
            ComponentsPanel,
        },
        inject: ['refresh'],
        setup() {
            const store = useStore();
            const userInfo = computed(() => store.state.base.userInfo);
            const { ctx, appContext } = getCurrentInstance();
            const {
                $bus,
                $http,
                $confirm,
                $message,
            } = appContext.config.globalProperties;
            const route = useRoute();
            const router = useRouter();
            const { flow_id } = route.query;
            const vData = reactive({
                loadingText:            '',
                locker:                 false,
                loading:                false,
                waiting:                false,
                readonly:               true,
                showMinimap:            true,
                project_id:             '',
                flow_id,
                job_id:                 '',
                oot_job_id:             '',
                oot_model_flow_node_id: '',
                flow_name:              '新流程',
                learningType:           'vertical',
                my_role:                'provider',
                is_creator:             false,
                project:                {
                    name: '',
                },
                /* component list, isFolder is parent */
                treeData: [
                    {
                        name:     '系统组件',
                        isFolder: true,
                        children: [],
                    },
                    /* {
                        name:     '我的组件',
                        isFolder: true,
                        children: [{
                            name: '1-1',
                            id:   '',
                        }],
                    }, */
                ],
                dragover:              false,
                failedDialog:          false,
                /* current component name */
                componentType:         'defaultPanel',
                jobGraphShow:          false,
                componentsHide:        false,
                paramsEmptynodesPanel: false,
                paramsEmptynodes:      [], // node params is null
                jobFinishedMessage:    {
                    show:    false,
                    status:  '',
                    message: '',
                },
                jobDetail:  {},
                /* cache current component */
                currentObj: {
                    nodeId:        '',
                    componentType: null,
                    /* dataset cache */
                    dataSource:    [],
                },
            });

            let resizeObserver,
                signDialog;
            const graph = {
                instance: null,
            };
            const Canvas = ref();
            const PageRef = ref();
            const ToolbarRef = ref();
            const errorPanel = ref();
            const JobPanelRef = ref();
            const GraphMinimap = ref();
            const ComponentsPanel = ref();
            const treeContainers = ref([]);
            const methods = {
                async beforeInitCheck() {
                    const params = { mobile: userInfo.value.phone_number };
                    const { code, data } = await $http.post({
                        url:  '/tianmiantech/call_api',
                        data: {
                            api: 'realNameState',
                            params,
                        },
                    });

                    if(code === 0) {
                        if(data.realNameState === 'unnamed') {
                            vData.loading = false;
                            const res = await $http.post({
                                url:  '/tianmiantech/page_url',
                                data: {
                                    page: 'testApply',
                                    params,
                                },
                            });

                            if(res.code === 0) {
                                if(res.data.url) {
                                    signDialog = $confirm('', {
                                        title:             '用户免费体验测试申请',
                                        message:           '您还没有签订测试申请协议，请填写《用户免费体验测试申请》后继续体验全部功能。',
                                        confirmButtonText: '立刻签署',
                                        cancelButtonText:  '返回项目详情',
                                        type:              'warning',
                                        closeOnClickModal: false,
                                        showClose:         false,
                                    })
                                        .then(() => {
                                            window.location.href = res.data.url;
                                        })
                                        .catch(() => {
                                            router.replace({
                                                name:  'project-detail',
                                                query: {
                                                    project_id: vData.project_id,
                                                },
                                            });
                                        });

                                    return false;
                                }
                            }
                        }
                    }
                    return true;
                },
                async init () {
                    vData.loading = true;

                    const { code, data } = await $http.get({
                        url:    '/project/flow/detail',
                        params: {
                            flow_id: vData.flow_id,
                        },
                    });

                    nextTick(async () => {
                        vData.loading = false;
                        if(code === 0 && data) {
                            const _data = data.graph || {};
                            const nodes = _data.nodes || [];
                            const edges = _data.edges || [];
                            const combos = _data.combos || [];

                            vData.my_role = data.my_role;
                            vData.flow_name = data.flow_name;
                            vData.project_id = data.project_id;
                            vData.is_creator = data.is_creator || data.is_creator === undefined;
                            vData.project.name = data.project.name;
                            vData.learningType = data.federated_learning_type;
                            vData.paramsEmptynodes = data.params_is_null_flow_nodes || [];
                            vData.oot_model_flow_node_id = data.oot_model_flow_node_id;
                            vData.oot_job_id = data.oot_job_id;

                            let continually = true;

                            if(window.location.hostname === 'wefe-demo.tianmiantech.com') {
                                continually = await methods.beforeInitCheck();
                            }

                            if(continually) {
                                if(vData.paramsEmptynodes.length) {
                                    vData.paramsEmptynodesPanel = true;
                                }

                                if(ComponentsPanel.value) {
                                    ComponentsPanel.value.flow_name = data.flow_name;
                                    ComponentsPanel.value.flow_desc = data.flow_desc;
                                    ComponentsPanel.value.learningType = vData.learningType;
                                }

                                methods.createGraph({ nodes, edges, combos });
                                methods.changeHeaderTitle();
                                methods.getComponents();

                                // get task details
                                ToolbarRef.value && ToolbarRef.value.methods.init();
                            }
                        } else if(code !== 10006) {
                            vData.failedDialog = true;
                        }

                        methods.resizeCanvas(graph.instance);
                    });
                },

                updateEmptyParamsNode(list) {
                    vData.paramsEmptynodes = list;
                    vData.paramsEmptynodesPanel = true;
                },

                removeEmptyParamsNode(id) {
                    const index = vData.paramsEmptynodes.findIndex(node => node.node_id === id);

                    if(~index) {
                        vData.paramsEmptynodes.splice(index, 1);
                    }
                },

                removeParamsNode(node_id) {
                    const index = vData.paramsEmptynodes.findIndex(node => node.node_id === node_id);

                    if(~index) {
                        vData.paramsEmptynodes.splice(index, 1);
                    }
                    if(vData.paramsEmptynodes.length === 0) {
                        vData.paramsEmptynodesPanel = false;
                    }
                },

                resizeCanvas(graph) {
                    let lastTime = 0, timer = null;

                    // observe canvas size
                    resizeObserver = new ResizeObserver(() => {
                        const timestamp = Date.now();

                        if(timestamp - lastTime >= 200) {
                            lastTime = timestamp;
                            clearTimeout(timer);
                            timer = setTimeout(() => {
                                if(graph) {
                                    graph.changeSize(Canvas.value.offsetWidth, Canvas.value.offsetHeight);
                                }
                            }, 200);
                        }
                    });

                    resizeObserver.observe(Canvas.value);
                },

                changeHeaderTitle() {
                    if(route.meta.titleParams) {
                        const htmlTitle = `<strong>${vData.project.name}</strong> - ${vData.flow_name} (${vData.learningType === 'vertical' ? '纵向' : vData.learningType === 'horizontal' ? '横向' : '混合' })`;

                        $bus.$emit('change-layout-header-title', { meta: htmlTitle });
                    }
                },

                updateFlowInfo(data) {
                    vData.flow_name = data.flow_name;
                    vData.learningType = data.learningType;
                },

                switchJobGraphPanel(flag) {
                    if(flag != null) {
                        vData.jobGraphShow = flag;
                        if(flag === false) {
                            if(ComponentsPanel.value.tabName === 'result') {
                                vData.componentType = 'defaultPanel';
                            }
                        }
                    } else {
                        vData.jobGraphShow = !vData.jobGraphShow;
                    }
                },

                jobRunning(data) {
                    nextTick(_ => {
                        const jobStopStatus = ['stop_on_running', 'error_on_running', 'reject_on_auditing', 'deleted', 'error', 'success', 'stop'];

                        JobPanelRef.value && JobPanelRef.value.methods.jobRunning(data);

                        if(data.job) {
                            vData.jobDetail = data.job;
                            vData.flow_name = vData.jobDetail.name;
                        }

                        // running job open job panel
                        if(data.status && !jobStopStatus.includes(data.job.status)) {
                            vData.jobGraphShow = true;
                        }
                    });
                },

                updateCurrentObj({ componentType, nodeId }) {
                    vData.componentType = componentType;
                    vData.currentObj.componentType = componentType;
                    vData.currentObj.nodeId = nodeId;
                },

                jobChange({ waiting, text }) {
                    if(waiting) {
                        vData.waiting = waiting;
                        vData.loadingText = text;
                    } else {
                        setTimeout(() => {
                            vData.waiting = waiting;
                            vData.loadingText = text;
                        }, 2000);
                    }
                },

                jobFinished({ status, message }) {
                    vData.jobFinishedMessage.show = true;
                    vData.jobFinishedMessage.status = status;
                    vData.jobFinishedMessage.message = message;
                },

                async getComponents() {
                    const { code, data } = await $http.get({
                        url:    '/component/list',
                        params: {
                            federatedLearningType: vData.learningType,
                        },
                    });

                    if(code === 0) {
                        nextTick(_ => {
                            vData.treeData[0] = {
                                name:     '系统组件',
                                isFolder: true,
                                children: data,
                            };
                            treeContainers.value[0] && treeContainers.value[0].methods.updateHeight();
                        });
                    }
                },

                checkResult(item) {
                    const model = item.getModel();

                    methods.resetGraphState();
                    graph.instance.setItemState(item, 'nodeState', 'selected');
                    ComponentsPanel.value.switchComponent(model, 'result');
                },

                checkHelp(item) {
                    const model = item.getModel();

                    methods.resetGraphState();
                    graph.instance.setItemState(item, 'nodeState', 'selected');
                    ComponentsPanel.value.switchComponent(model, 'help');
                },

                switchComponent(model, type) {
                    ComponentsPanel.value.switchComponent(model, type);
                },

                resetGraphState() {
                    vData.currentObj.nodeId = '';
                    vData.componentType = 'defaultPanel';
                    graph.instance.getNodes().forEach(node => {
                        graph.instance.setItemState(node, 'nodeState:default', true);
                    });
                    graph.instance.getEdges().forEach(edge => {
                        graph.instance.setItemState(edge, 'edgeState:default', true);
                    });
                },

                /* toggle minimap */
                switchMinimap() {
                    vData.showMinimap = !vData.showMinimap;
                },

                closeExplainPop() {
                    if(ComponentsPanel.value.isExplainShow) {
                        ComponentsPanel.value.closeExplainPop();
                    }
                },

                componentPanelChangeSize(maxSize) {
                    vData.componentsHide = maxSize;
                },
            };

            // mixin function
            graphMixin.mixin({
                appContext,
                ctx,
                vData,
                graph,
                Canvas,
                GraphMinimap,
                ComponentsPanel,
                errorPanel,
                ToolbarRef,
                $bus,
                $http,
                $message,
                nextTick,
                methods,
                onBeforeMount,
                onBeforeUnmount,
            });
            toolbarMixin.mixin({
                graph,
                methods,
            });

            onBeforeMount(_ => {

                if(flow_id) {
                    methods.init();
                } else {
                    $message.error('缺少流程 id, 请重新创建流程!');
                }

                $bus.$on('history-backward', () => {
                    router.push({
                        name:  'project-detail',
                        query: {
                            project_id: vData.project_id,
                        },
                    });
                });

                $bus.$on('job-change', data => {
                    methods.jobChange(data);
                });

                $bus.$on('job-finished', data => {
                    methods.jobFinished(data);
                });

                $bus.$on('update-job-id', id => {
                    vData.job_id = id;
                });
            });

            onBeforeUnmount(_ => {
                vData.currentObj.nodeId = '';
                vData.currentObj.componentType = null;
                vData.failedDialog && (vData.failedDialog = false);
                graph.instance && graph.instance.destroy();
                graph.instance = null;

                $bus.$off('history-backward');
                $bus.$off('job-change');
                $bus.$off('job-finished');
                $bus.$off('update-job-id');
                signDialog && signDialog.close();
                resizeObserver && resizeObserver.disconnect();
            });

            return {
                vData,
                graph,
                PageRef,
                treeContainers,
                Canvas,
                ToolbarRef,
                errorPanel,
                JobPanelRef,
                ComponentsPanel,
                GraphMinimap,
                methods,
            };
        },
    };
</script>

<style lang="scss">
    .g6-component-contextmenu{
        z-index: 2;
        line-height: 24px;
        min-width: 110px;
        .menu-item{
            height: 24px;
            padding: 0 5px;
            margin-top:3px;
            cursor: pointer;
            &:first-child{margin-top:0;}
            &:hover{
                background:#669ef8;
                color:#fff;
            }
            &.disabled{
                color: #999;
                cursor: not-allowed;
                background: #fff;
            }
        }
        .iconfont{
            margin-right: 5px;
            font-size: 12px;
        }
        .icon-warning-outline{color:#F85564;}
    }
</style>

<style lang="scss" scoped>
    @import './visual.scss';
</style>
