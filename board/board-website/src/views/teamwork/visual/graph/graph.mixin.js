/* graph mixins */
import {
    Menu,
    Minimap,
    Graph,
    Grid,
    SnapLine,
    registerNode,
    registerEdge,
    registerBehavior,
} from '@antv/g6';
import g6Register from 'welabx-g6';
import nodeStateApply from './node-state-apply';
import edgeStateApply from './edge-state-apply';

export default {
    mixin({
        appContext,
        context,
        vData,
        graph,
        Canvas,
        GraphMinimap,
        ComponentsPanel,
        errorPanel,
        ToolbarRef,
        methods,
        $bus,
        $http,
        $message,
        nextTick,
        onBeforeMount,
        onBeforeUnmount,
    }) {
        const {
            $alert,
            $confirm,
            $notify,
        } = appContext.config.globalProperties;
        const $methods = {
            async createGraph(data = { nodes: [], edges: [], combos: [] }) {
                vData.waiting = true;
                vData.loadingText = '加载中...';

                if (Canvas.value) {
                    const menu = methods.createContextMenu();
                    const minimap = new Minimap({
                        container:         GraphMinimap.value,
                        viewportClassName: 'graph-minimap-dom',
                        // type:              'keyShape',
                        size:              [200, 100],
                    });
                    const snapLine = new SnapLine({
                        itemAlignType: true,
                    });
                    const grid = new Grid();
                    const plugins = [grid, minimap, menu, snapLine];

                    const config = g6Register(
                        {
                            registerNode,
                            registerEdge,
                            registerBehavior,
                        },
                        {
                            container:   Canvas.value,
                            width:       Canvas.value.offsetWidth,
                            height:      Canvas.value.offsetHeight,
                            defaultNode: {
                                type:         'flow-node',
                                anchorPoints: [
                                    [0.5, 0],
                                    [0.5, 1],
                                ],
                                style: {
                                    fill:  '#ecf3ff',
                                    width: 100,
                                },
                                labelCfg: {
                                    style: {
                                        fill: '#4483FF',
                                    },
                                },
                            },
                            nodeStateStyles: {
                                'nodeState:default': {
                                    lineWidth: 1,
                                    fill:      '#ecf3ff',
                                    stroke:    '#4483FF',
                                    labelCfg:  {
                                        style: {
                                            fill:       '#4483FF',
                                            fontWeight: 'normal',
                                        },
                                    },
                                },
                                'nodeState:selected': {
                                    lineWidth: 2,
                                    fill:      '#4483FF',
                                    labelCfg:  {
                                        style: {
                                            fill:       '#fff',
                                            fontWeight: 'bold',
                                        },
                                    },
                                },
                                highlight: {
                                    lineWidth: 1,
                                    fill:      '#f85564',
                                    stroke:    '#f85564',
                                    labelCfg:  {
                                        style: {
                                            fill:       '#fff',
                                            fontWeight: 'bold',
                                        },
                                    },
                                },
                            },
                            defaultEdge: {
                                type:  'flow-edge',
                                style: {
                                    stroke:          '#aab7c3',
                                    lineAppendWidth: 20,
                                    endArrow:        true,
                                },
                            },
                            layout: {
                                type: '',
                            },
                            modes: {
                                default: [
                                    // Drag and drop a single node to add a virtual node style
                                    {
                                        type: 'drag-shadow-node',
                                        shouldBegin(e) {
                                            const states = e.item.get('states');

                                            if (!states.includes('selected')) {
                                                return true;
                                            }
                                        },
                                    },
                                    // brush select node
                                    {
                                        type:       'brush-select',
                                        brushStyle: {
                                            lineWidth:   1,
                                            fillOpacity: 0.1,
                                            fill:        '#4088fc',
                                            stroke:      '#4088fc',
                                        },
                                    },
                                    {
                                        type: 'drag-node',
                                        shouldBegin(e) {
                                            const states = e.item.get('states');

                                            if (states.includes('selected')) {
                                                return true;
                                            }
                                        },
                                    },
                                    'drag-canvas',
                                    'scroll-canvas',
                                    'canvas-event',
                                    'delete-item',
                                    'select-node',
                                    'active-edge',
                                    {
                                        type: 'hover-node',
                                        shouldBegin(e) {
                                            const states = e.item.get('states');

                                            if (!states.includes('selected')) {
                                                return true;
                                            }
                                        },
                                    },
                                ],
                            },
                            plugins,
                        },
                    );

                    nodeStateApply({ registerNode });
                    edgeStateApply({ registerEdge });

                    /* rewrite graph object */
                    graph.instance = new Graph(config);
                    graph.instance.get('canvas').set('localRefresh', false);
                    if (data.nodes.length === 0) {
                        // Automatically add start node
                        data.nodes.push({
                            id:           'start',
                            label:        '开始',
                            x:            Canvas.value.offsetWidth / 2,
                            y:            150,
                            anchorPoints: [[0.5, 1]],
                            singleEdge:   true, // Dragging an anchor can only generate 1 edge
                            style:        {
                                fill:   '#f2f9ec',
                                stroke: '#8BC34A',
                            },
                            labelCfg: {
                                style: {
                                    fill:     '#8BC34A',
                                    fontSize: 14,
                                },
                            },
                            nodeStateStyles: {
                                'nodeState:default': {
                                    lineWidth: 1,
                                },
                                'nodeState:selected': {
                                    lineWidth: 2,
                                },
                            },
                            data: {
                                nodeType: 'system', // !System node, skipping component parsing in the server
                            },
                        });
                    }
                    nextTick(_ => {
                        graph.instance.read(data);
                        setTimeout(() => {
                            vData.waiting = false;
                        }, 300);
                    });
                    methods.bindEvents();
                }

                nextTick(_ => {
                    vData.loading = false;
                });
            },

            createContextMenu() {
                return new Menu({
                    offsetX: 10,
                    offsetY: -14,
                    shouldBegin(e) {
                        if (vData.my_role !== 'promoter') return false;

                        let shouldBegin = true;

                        if (e.item) {
                            const type = e.item.get('type');

                            if (type === 'edge') {
                                shouldBegin = true;
                            } else if (type === 'node') {
                                const {
                                    nodeType,
                                    task,
                                } = e.item.getModel().data;
                                const readonly = [];

                                // System nodes cannot be copied or deleted
                                if (nodeType === 'system' || readonly[task]) {
                                    shouldBegin = false;
                                } else {
                                    shouldBegin = true;
                                }
                            }
                            return shouldBegin;
                        }
                    },
                    getContent(e) {
                        if (e.name === 'edge-shape:contextmenu') {
                            // edges
                            return `
                            <p class="menu-item" command="deleteItem">
                                <i class="iconfont icon-warning-outline" command="deleteItem"></i>删除
                            </p>`;
                        }

                        // nodes
                        const commands = [
                            {
                                command: 'runToHere',
                                name:    '运行到此处',
                                icon:    'iconfont icon-to-this',
                            },
                            /* {
                                command: 'checkResult',
                                name:    '查看执行结果',
                            },  */ {
                                command: 'copyNode',
                                icon:    'iconfont icon-plus',
                                name:    '复制节点',
                            },
                            {
                                command: 'deleteItem',
                                icon:    'iconfont icon-warning-outline',
                                name:    '删除节点',
                            },
                            {
                                command: 'checkHelp',
                                icon:    'iconfont icon-help',
                                name:    '帮助文档',
                            },
                        ];

                        let menus = '';

                        commands.forEach(item => {
                            menus += `<p class="menu-item" command="${
                                item.command
                            }">${
                                item.icon
                                    ? `<i class="${item.icon}" command="${item.command}"></i>`
                                    : ''
                            }${item.name}</p>`;
                        });

                        return menus;
                    },
                    handleMenuClick(target, item) {
                        const command = target.getAttribute('command');

                        methods[command] && methods[command](item);
                    },
                });
            },

            // focusNode
            focusNode({ node_id }) {
                const item = graph.instance.findById(node_id);

                methods.resetGraphState();
                graph.instance.focusItem(node_id);
                graph.instance.setItemState(
                    node_id,
                    'nodeState:selected',
                    true,
                );
                methods.switchComponent(item.getModel());
            },

            // generate nodeID
            generateNodeId() {
                return `${+new Date() + (Math.random() * 10000).toFixed(0)}`;
            },

            bindEvents() {
                graph.instance.on('on-canvas-dragend', e => {
                    methods.updateErrorPanelPosition();
                });

                // Drag release add node
                graph.instance.on('drop', async ({ originalEvent, x, y }) => {
                    if (vData.my_role === 'promoter' && vData.is_creator) {
                        vData.dragover = false;

                        if (originalEvent.dataTransfer) {
                            const transferData = originalEvent.dataTransfer.getData(
                                'dragComponent',
                            );

                            if (transferData) {
                                const id = methods.generateNodeId();
                                const { label, data } = JSON.parse(
                                    transferData,
                                );
                                const model = {
                                    id,
                                    x,
                                    y,
                                    anchorPoints: data.anchorPoints || [
                                        [0.5, 0],
                                        [0.5, 1],
                                    ],
                                    singleEdge: true, // Dragging an anchor can only generate 1 edge
                                    label,
                                    data,
                                };

                                /* reset history */
                                methods.resetGraphState();

                                graph.instance.addItem('node', model);

                                // Drag detection
                                if (data.autoSave) {
                                    graph.instance.setItemState(
                                        model.id,
                                        'nodeState',
                                        'selected',
                                    );
                                }

                                // save graph first
                                await methods.save();

                                if (data.autoSave) {
                                    methods.switchComponent(model);
                                    // auto submit form
                                    nextTick(_ => {
                                        ComponentsPanel.value.saveComponentData('node-update');
                                    });
                                }
                            }
                        }
                    } else if (
                        vData.my_role === 'promoter' &&
                        !vData.is_creator
                    ) {
                        $message.error('只可以编辑自己创建的流程!');
                    } else {
                        $message.error('协作方不可以编辑流程!');
                    }
                });

                // node connection
                graph.instance.on(
                    'before-edge-add',
                    ({ source, target, sourceAnchor, targetAnchor }) => {
                        if (vData.my_role === 'promoter' && vData.is_creator) {
                            graph.instance.addItem('edge', {
                                id:     methods.generateNodeId(),
                                source: source.get('id'),
                                target: target.get('id'),
                                sourceAnchor,
                                targetAnchor,
                                label:  '',
                            });
                            nextTick(_ => {
                                methods.save();
                            });
                        } else if (
                            vData.my_role === 'promoter' &&
                            !vData.is_creator
                        ) {
                            $message.error('只可以编辑自己创建的流程!');
                        } else {
                            $message.error('协作方不可以编辑流程!');
                        }
                    },
                );

                graph.instance.on('click', e => {
                    if (!e.item) {
                        // Click the canvas
                        methods.resetGraphState();
                    }
                });

                graph.instance.on('canvas:dragend', e => {
                    methods.updateErrorPanelPosition();
                });

                graph.instance.on('after-node-selected', e => {
                    if (e && e.item) {
                        const model = e.item.getModel();

                        if (model.data.nodeType === 'system') {
                            vData.componentType = 'defaultPanel';
                            vData.currentObj.nodeId = '';
                            vData.currentObj.componentType = null;
                        }
                        // switch component
                        methods.switchComponent(model);
                        methods.updateErrorPanelPosition();
                    }
                });

                graph.instance.on('on-node-drag', e => {
                    // Drag node
                    if (errorPanel.value && errorPanel.value.vData.show) {
                        methods.updateErrorPanelPosition();
                    }
                });

                // delete nodes
                graph.instance.on('before-node-removed', ({ callback }) => {
                    methods.removeNode(null, callback);
                });

                // delete edges
                graph.instance.on('before-edge-removed', ({ callback }) => {
                    methods.removeEdge(callback);
                });

                // Mouse scroll event
                graph.instance.on('wheel', () => {
                    methods.updateErrorPanelPosition();
                });

                // listen minimap drag event
                document.addEventListener('dragend', e => {
                    if (
                        e.target.className &&
                        e.target.className.includes('graph-minimap-dom')
                    ) {
                        methods.updateErrorPanelPosition();
                    }
                });
            },

            // Automatically update error prompt panel location
            updateErrorPanelPosition() {
                if (
                    errorPanel.value &&
                    errorPanel.value.vData.message &&
                    errorPanel.value.vData.nodeId
                ) {
                    const item = graph.instance.findById(
                        errorPanel.value.vData.nodeId,
                    );

                    if (item) {
                        const group = item.getContainer();
                        const cacheCanvasBBox = group.get('cacheCanvasBBox');

                        if (cacheCanvasBBox) {
                            const { x, y } = cacheCanvasBBox;

                            errorPanel.value.vData.left = x + 375;
                            errorPanel.value.vData.top = y - 20;
                            errorPanel.value.vData.show = true;
                        } else {
                            errorPanel.value.vData.show = false;
                        }
                        graph.instance.setItemState(item, 'highlight', true);
                    }
                }
            },

            // delete nodes & edges
            deleteItem(item) {
                const isEdge = item.get('type') === 'edge';

                if (isEdge) {
                    graph.instance.removeItem(item);
                    methods.save();
                } else {
                    $alert('确定要删除该节点吗? 此操作不可撤销!', '警告', {
                        type: 'warning',
                    }).then(action => {
                        if (action === 'confirm') {
                            const model = item.getModel();

                            graph.instance.removeItem(item);

                            if (errorPanel.value.vData.nodeId === model.id) {
                                errorPanel.value.vData.show = false;
                            }
                            vData.currentObj.nodeId = '';
                            vData.currentObj.componentType = null;
                            vData.componentType = 'defaultPanel';
                            methods.removeEmptyParamsNode(model.id);
                            methods.save();
                        }
                    });
                }
            },

            /* remove node width keyboard shortcut key */
            removeNode(e, callback) {
                const { nodeId } = vData.currentObj;

                if (
                    ((e && (e.keyCode === 8 || e.keyCode === 46)) ||
                        callback) &&
                    nodeId
                ) {
                    if (vData.locker) return;
                    vData.locker = true;

                    $confirm('确定要删除该节点吗?', '警告', {
                        type: 'warning',
                        beforeClose(action, instance, done) {
                            if (action === 'confirm') {
                                if (
                                    errorPanel.value &&
                                    errorPanel.value.vData.nodeId === nodeId
                                ) {
                                    errorPanel.value.vData.show = false;
                                }

                                callback(true);
                                methods.removeEmptyParamsNode(nodeId);
                                methods.resetGraphState();
                                methods.save();
                            }
                            vData.locker = false;
                            done();
                        },
                    });
                }
            },

            removeEdge(callback) {
                $confirm('确定要删除这条边吗?', '警告', {
                    type: 'warning',
                    beforeClose(action, instance, done) {
                        if (action === 'confirm') {
                            callback(true);
                            methods.resetGraphState();
                            methods.save();
                        }
                        vData.locker = false;
                        done();
                    },
                });
            },

            excute(command) {
                methods[command]();
            },

            runToHere(item) {
                if (ToolbarRef) {
                    ToolbarRef.value.methods.shouldStart(false, item);
                }
            },

            copyNode(item) {
                const model = item.getModel();

                graph.instance.addItem('node', {
                    ...model,
                    id: methods.generateNodeId(),
                    x:  model.x + 20,
                    y:  model.y + 20,
                });
                nextTick(_ => {
                    methods.save();
                });
            },

            /* save the graph */
            async save() {
                if (vData.flow_name === '') {
                    vData.componentType = 'defaultPanel';
                    return $message.error('请填写流程名称!');
                }

                const { nodes, edges, combos } = graph.instance.save();

                /* set start node */
                nodes.forEach(node => {
                    // Do not commit extra status values
                    delete node.stateIcon;
                    delete node.style['nodeState:created'];
                    delete node.style['nodeState:success'];
                    delete node.style['nodeState:running'];
                    delete node.style['nodeState:error'];
                    delete node.style['nodeState:default'];
                    delete node.style['nodeState:selected'];
                    // commit default status values
                    if (node.id !== 'start') {
                        node.style.stroke = '#4483FF';
                        node.style.fill = '#ecf3ff';
                        node.style.lineWidth = 1;
                        if (!node.labelCfg.style) {
                            node.labelCfg.style = {};
                        }
                        node.labelCfg.style.fill = '#4483FF';
                    }
                    node.type = 'flow-node';
                });

                const params = {
                    flow_id: vData.flow_id,
                    graph:   {
                        nodes,
                        edges,
                        combos,
                    },
                };

                if (vData.flow_id) {
                    params.flow_id = vData.flow_id;
                }

                const { code } = await $http.post({
                    url:  '/project/flow/update/graph',
                    data: params,
                });

                if (code === 0) {
                    $notify.success({
                        offset:   5,
                        duration: 1000,
                        title:    '提示',
                        message:  '保存成功!',
                    });
                }
            },
        };

        // merge mixin
        Object.assign(methods, $methods);

        onBeforeMount(() => {
            // When an exception occurs to a node, the node is automatically positioned to the center of the canvas
            $bus.$on('node-error', ({ clear, node_id, message }) => {
                vData.jobFinishedMessage.show = false;
                if (graph.instance) {
                    if (clear) {
                        errorPanel.value.vData.message = '';
                        errorPanel.value.vData.show = false;
                    } else {
                        const item = graph.instance.findById(node_id);

                        if (item) {
                            graph.instance.focusItem(item);
                            graph.instance.setItemState(item, 'highlight', true);

                            setTimeout(() => {
                                errorPanel.value.vData.show = true;
                                errorPanel.value.vData.nodeId = node_id;
                                errorPanel.value.vData.message = message;
                                errorPanel.value.vData.left = graph.instance.getWidth() / 2 + 320;
                                errorPanel.value.vData.top = graph.instance.getHeight() / 2 - 40;
                            }, 400);
                        }
                    }
                }
            });

            $bus.$on('sideCollapsed', () => {
                if (graph.instance) {
                    graph.instance.changeSize(Canvas.value.offsetWidth, Canvas.value.offsetHeight);
                }
            });
        });

        onBeforeUnmount(() => {
            $bus.$off('node-error');
            $bus.$off('sideCollapsed');
        });

        return {
            methods,
        };
    },
};
