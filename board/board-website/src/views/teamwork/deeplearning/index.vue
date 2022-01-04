<template>
    <el-card class="page_layer" v-loading="vData.startLoading">
        <div class="deep_flow">
            <div class="left_content">
                <h3 class="flow_title">
                    <span v-show="vData.active === 0"><i class="el-icon-edit-outline"/> 基本设置</span>
                    <span v-show="vData.active === 1"><i class="el-icon-download"/> 数据输入与处理</span>
                    <span v-show="vData.active === 2"><i class="el-icon-s-operation"/> 调整流程参数</span>
                </h3>
                <div class="step_content">
                    <div v-show="vData.active === 0" class="item base_setting">
                        <el-form
                            @submit.prevent
                            :disabled="vData.flowInfo.my_role !=='promoter'"
                        >
                            <el-form-item
                                label="流程名称："
                                required
                            >
                                <el-input
                                    v-model="vData.form.flow_name"
                                    @blur="methods.saveFlowInfo($event)"
                                />
                            </el-form-item>
                            <el-form-item label="流程描述：">
                                <el-input
                                    v-model="vData.form.flow_desc"
                                    type="textarea"
                                    rows="6"
                                    placeholder="请输入流程描述"
                                    @blur="methods.saveFlowInfo($event)"
                                />
                            </el-form-item>
                        </el-form>
                    </div>
                    <div v-show="vData.active === 1" class="item enter_data">
                        <el-tabs type="border-card" @tab-click="methods.dataIOTabchange">
                            <el-tab-pane label="参数">
                                <div class="data_select">
                                    <h4>选择数据集</h4>
                                    <div
                                        v-for="(member, index) in vData.member_list"
                                        v-show="vData.disabled ? member.$data_set_list.length : true"
                                        :key="member.id"
                                        class="li"
                                    >
                                        <h3
                                            v-if="index === 0"
                                            class="role-title pb5"
                                        >
                                            发起方:
                                        </h3>
                                        <h3
                                            v-if="index === vData.promoterList.length"
                                            class="role-title pb5"
                                        >
                                            协作方:
                                        </h3>
                                        <p class="member-info">
                                            <span class="name f16">
                                                <i
                                                    v-if="member.audit_status !== 'agree'"
                                                    class="el-icon-warning-outline color-danger"
                                                />
                                                {{ member.member_name }}
                                            </span>
                                            <span
                                                v-if="member.audit_status !== 'agree'"
                                                class="f12"
                                            >({{ member.audit_comment || '审核通过的成员才能参与流程' }})</span>
                                            <el-button
                                                v-if="member.audit_status === 'agree' && !vData.disabled"
                                                type="text"
                                                class="ml10"
                                                @click="methods.checkDataSet(member, index)"
                                                :disabled="vData.flowInfo.my_role !=='promoter'"
                                            >
                                                选择数据集
                                            </el-button>
                                        </p>

                                        <div
                                            v-if="member.audit_status === 'agree'"
                                            class="data-set f14"
                                        >
                                            <el-form
                                                v-for="row in member.$data_set_list"
                                                :key="row.id"
                                                label-width="96px"
                                            >
                                                <el-form-item label="数据集名称：">
                                                    {{ row.data_set.name }}
                                                    <i
                                                        v-if="!vData.disabled"
                                                        title="移除"
                                                        class="el-icon-circle-close f20 ml10"
                                                        @click="methods.removeDataSet(index)"
                                                    />
                                                </el-form-item>
                                                <el-form-item label="数据集id："> {{ row.data_set_id }} </el-form-item>
                                                <el-form-item label="数据总量：">
                                                    {{ row.data_set.total_data_count }}
                                                </el-form-item>
                                                <el-form-item label="样本分类：">
                                                    {{row.data_set.for_job_type === 'classify' ? '图像分类' : row.data_set.for_job_type === 'detection' ? '目标检测' : '-'}}
                                                </el-form-item>
                                            </el-form>
                                        </div>
                                    </div>
                                </div>
                                <div class="data_cut">
                                    <h4>数据切割</h4>
                                    <el-form
                                        ref="form"
                                        :model="vData.dataCutForm"
                                        @submit.prevent
                                        :disabled="vData.flowInfo.my_role !=='promoter'"
                                    >
                                        <el-form-item label="训练与验证数据比例（%）：" style="width: 300px">
                                            <div style="height: 50px;">
                                                <div class="float-left">
                                                    <p style="font-weight:bold;color:#4D84F7;" class="mb5">训练:</p>
                                                    <el-input-number
                                                        v-model="vData.dataCutForm.training_ratio"
                                                        style="width:100px"
                                                        size="mini"
                                                    />
                                                </div>

                                                <div class="float-right">
                                                    <p style="font-weight:bold;" class="text-r color-danger mb5">验证:</p>
                                                    <el-input-number
                                                        v-model="vData.dataCutForm.verification_ratio"
                                                        style="width:100px"
                                                        size="mini"
                                                        @change="methods.onDataSetVerificationRatioChange"
                                                    />
                                                </div>
                                            </div>
                                            <el-slider
                                                v-model="vData.dataCutForm.training_ratio"
                                                :show-tooltip="false"
                                                @input="methods.onDataSetTrainingVerificationRatioChange"
                                            />
                                        </el-form-item>
                                    </el-form>
                                </div>
                            </el-tab-pane>
                            <el-tab-pane v-if="vData.jobInfo.status" label="执行结果">
                                执行结果展示......
                            </el-tab-pane>
                        </el-tabs>
                        <el-dialog
                            title="选择数据资源"
                            v-model="vData.showSelectDataSet"
                            custom-class="dialog-min-width"
                            :close-on-click-modal="false"
                            destroy-on-close
                            width="70%"
                        >
                            <el-form
                                inline
                                @submit.prevent
                            >
                                <el-form-item label="名称">
                                    <el-input
                                        v-model="vData.rawSearch.name"
                                        clearable
                                    />
                                </el-form-item>
                                <el-form-item label="id">
                                    <el-input
                                        v-model="vData.rawSearch.data_set_id"
                                        clearable
                                    />
                                </el-form-item>
                                <el-button
                                    type="primary"
                                    native-type="submit"
                                    @click="methods.dataSetSearch"
                                >
                                    搜索
                                </el-button>
                            </el-form>
                            <DataSetList
                                ref="rawDataSetListRef"
                                :audit-status="true"
                                :search-field="vData.rawSearch"
                                :paramsExclude="['allList', 'list']"
                                :project-type="vData.flowInfo.project.project_type"
                                @list-loaded="methods.listLoaded"
                                @selectDataSet="methods.selectDataSet"
                                @close-dialog="vData.showSelectDataSet=false;"
                            >
                                <template #data-add>
                                    <i />
                                </template>
                            </DataSetList>
                        </el-dialog>
                    </div>
                    <div v-show="vData.active === 2" class="item params_setting">
                        <el-tabs type="border-card" @tab-click="methods.deeplearningOTabchange">
                            <el-tab-pane label="参数">
                                <el-form
                                    @submit.prevent
                                    :disabled="vData.flowInfo.my_role !=='promoter'"
                                >
                                    <el-form-item label="算法类型：" required>
                                        <el-select v-model="vData.deepLearnParams.program" placeholder="请选择算法类型">
                                            <el-option
                                                v-for="item in vData.classifyList"
                                                :key="item.value"
                                                :label="item.label"
                                                :value="item.value">
                                            </el-option>
                                        </el-select>
                                    </el-form-item>
                                    <el-form-item label="模型名称：" required>
                                        <el-select v-model="vData.deepLearnParams.architecture" placeholder="请选择模型名称">
                                            <el-option
                                                v-for="item in vData.deepLearnParams.program === 'paddle_detection' ? vData.targetAlgorithmList : vData.imageAlgorithmList"
                                                :key="item.value"
                                                :label="item.label"
                                                :value="item.value">
                                            </el-option>
                                        </el-select>
                                    </el-form-item>
                                    <el-form-item label="迭代次数：" required>
                                        <el-input
                                            v-model="vData.deepLearnParams.max_iter"
                                            @blur="methods.saveFlowInfo($event)"
                                        />
                                    </el-form-item>
                                    <el-form-item label="聚合步长：" required>
                                        <el-input
                                            v-model="vData.deepLearnParams.inner_step"
                                            @blur="methods.saveFlowInfo($event)"
                                        />
                                    </el-form-item>
                                    <!-- <el-form-item label="类别数：" required>
                                <el-input
                                    v-model="vData.deepLearnParams.num_classes"
                                    @blur="methods.saveFlowInfo($event)"
                                />
                            </el-form-item> -->
                                    <el-form-item label="学习率：" required>
                                        <el-input
                                            v-model="vData.deepLearnParams.base_lr"
                                            @blur="methods.saveFlowInfo($event)"
                                        />
                                    </el-form-item>
                                    <el-form-item label="图片通道数：" required>
                                        <el-input
                                            type="number"
                                            v-model="vData.image_shape.aisle"
                                        />
                                    </el-form-item>
                                    <el-form-item label="图片宽度：" required>
                                        <el-input
                                            type="number"
                                            v-model="vData.image_shape.width"
                                        />
                                    </el-form-item>
                                    <el-form-item label="图片高度" required>
                                        <el-input
                                            type="number"
                                            v-model="vData.image_shape.height"
                                        />
                                    </el-form-item>
                                    <el-form-item label="批量大小：" required>
                                        <el-input
                                            v-model="vData.deepLearnParams.batch_size"
                                            @blur="methods.saveFlowInfo($event)"
                                        />
                                    </el-form-item>
                                </el-form>
                            </el-tab-pane>
                            <el-tab-pane label="执行结果">
                                执行结果展示中......
                            </el-tab-pane>
                        </el-tabs>
                        
                    </div>
                </div>
                <div class="operation_btn">
                    <el-button v-show="vData.active !== 0" @click="methods.prev">上一步</el-button>
                    <el-button v-show="vData.active !== 2" type="primary" @click="methods.next">下一步</el-button>
                    <el-button v-show="vData.active === 2" type="primary" @click="methods.saveDeeplearningNode" :disabled="vData.flowInfo.my_role !=='promoter'">开始训练</el-button>
                </div>
            </div>
            <div class="step_header">
                <el-steps direction="vertical" :active="vData.active" align-center>
                    <el-step title="基本设置" />
                    <el-step title="数据输入与处理" />
                    <el-step title="调整流程参数" />
                </el-steps>
            </div>
        </div>
    </el-card>
</template>

<script>
    import { reactive, getCurrentInstance, ref } from 'vue';
    import { useRoute, useRouter } from 'vue-router';
    import { nextTick, onBeforeMount, watch } from '@vue/runtime-core';
    import DataSetList from '@comp/views/data-set-list';

    export default {
        components: {
            DataSetList,
        },
        setup(props, context) {
            const { appContext } = getCurrentInstance();
            const { $http, $notify, $message } = appContext.config.globalProperties;
            const route = useRoute();
            const router = useRouter();
            const rawDataSetListRef = ref();
            const vData = reactive({
                loading: false,
                active:  0,
                flow_id: route.query.flow_id,
                form:    {
                    flow_name: '',
                    flow_desc: '',
                },
                deepLearnParams: {
                    program:      'paddle_detection',
                    max_iter:     10,
                    inner_step:   10,
                    architecture: 'YOLOv3',
                    // num_classes:  3,
                    base_lr:      0.00001,
                    image_shape:  [],
                    batch_size:   128,
                },
                image_shape: {},
                flowInfo:    {
                    project_id: '',
                },
                promoterList:      [],
                providerList:      [],
                member_list:       [],
                disabled:          false,
                showSelectDataSet: false,
                rawSearch:         {
                    allList:     [],
                    list:        [],
                    name:        '',
                    contains_y:  '',
                    data_set_id: '',
                },
                currentItem:  {},
                classifyList: [
                    {
                        value: 'paddle_clas',
                        label: 'paddle_clas',
                    },
                    {
                        value: 'paddle_detection',
                        label: 'paddle_detection',
                    },
                ],
                targetAlgorithmList: [
                    {
                        label: 'Faster R-CNN',
                        value: 'Faster R-CNN',
                    },
                    {
                        label: 'Mask R-CNN',
                        value: 'Mask R-CNN',
                    },
                    {
                        label: 'Cascade R-CNN',
                        value: 'Cascade R-CNN',
                    },
                    {
                        label: 'YOLOv3',
                        value: 'YOLOv3',
                    },
                    {
                        label: 'YOLOv4',
                        value: 'YOLOv4',
                    },
                    {
                        label: 'PP-YOLO',
                        value: 'PP-YOLO',
                    },
                    {
                        label: 'SSD',
                        value: 'SSD',
                    },
                ],
                imageAlgorithmList: [
                    {
                        label: 'LeNet',
                        value: 'LeNet',
                    },
                    {
                        label: 'AlexNet',
                        value: 'AlexNet',
                    },
                    {
                        label: 'GoogleNet',
                        value: 'GoogleNet',
                    },
                    {
                        label: 'VGGNet',
                        value: 'VGGNet',
                    },
                    {
                        label: 'ResNet',
                        value: 'ResNet',
                    },
                    {
                        label: 'DenseNet',
                        value: 'DenseNet',
                    },
                ],
                formImageDataIO: {
                    componentType: 'ImageDataIO',
                    flowId:        '',
                    nodeId:        '',
                    params:        {
                        data_set_list: [
                            {
                                member_id:   '',
                                member_role: '',
                                data_set_id: '',
                            },
                        ],
                        train_test_split_ratio: 70,
                    },
                },
                dataCutForm: {
                    training_ratio:     70,
                    verification_ratio: 30,
                },
                prevActive:      0,
                graphNodes:      {},
                startLoading:    false,
                jobInfo:         {},
                taskInfo:        [],
                deepLearnNodeId: '',
            });
            const methods = {
                async getFlowInfo() {
                    vData.startLoading = true;
                    const { code, data } = await $http.get({
                        url:    '/project/flow/detail',
                        params: {
                            flow_id: vData.flow_id,
                        },
                    });

                    nextTick(()=>{
                        if (code === 0) {
                            vData.flowInfo = data;
                            vData.form.flow_name = data.flow_name;
                            vData.form.flow_desc = data.flow_desc;
                            methods.getMemberList();
                            methods.getJobDetail();
                            if(!data.graph) {
                                methods.createNode();
                            } else {
                                // 查看选择数据资源节点信息
                                methods.getDataIONodeDetail(data.graph.nodes[1].id);
                                methods.getDeeplearningNodeDetail(data.graph.nodes[2].id);
                            }
                        }
                    });
                },
                async getJobDetail() {
                    const { code, data } = await $http.get({
                        url:    '/flow/job/detail',
                        params: {
                            requestFromRefresh: true,
                            jobId:              '',
                            flowId:             vData.flow_id,
                            member_role:        vData.flowInfo.my_role,
                            needResult:         true,
                        },
                    });

                    nextTick(()=>{
                        if (code === 0 && data) {
                            vData.jobInfo = data.job;
                            vData.taskInfo = data.task_views;
                        }});
                },
                async createNode() {
                    vData.graphNodes = {
                        flow_id: vData.flow_id,
                        graph:   {
                            combos: [],
                            edges:  [
                                {
                                    source: 'start',
                                    target: '',
                                },
                                {
                                    source: '',
                                    target: '',
                                },
                            ],
                            nodes: [
                                {
                                    id:    'start',
                                    label: '开始',
                                    type:  'flow-node',
                                    data:  {
                                        nodeType: 'system',
                                    },
                                },
                                {
                                    id:    methods.generateNodeId(),
                                    label: '选择数据资源',
                                    type:  'flow-node',
                                    data:  {
                                        componentType: 'ImageDataIO',
                                    },
                                },
                                {
                                    id:    methods.generateNodeId(),
                                    label: '训练',
                                    type:  'flow-node',
                                    data:  {
                                        componentType: 'DeepLearning',
                                    },
                                },
                            ],
                        },
                    };
                    vData.graphNodes.graph.edges[0].target = vData.graphNodes.graph.nodes[1].id;
                    vData.graphNodes.graph.edges[1].source = vData.graphNodes.graph.nodes[1].id;
                    vData.graphNodes.graph.edges[1].target = vData.graphNodes.graph.nodes[2].id;
                    const { code } = await $http.post({
                        url:  '/project/flow/update/graph',
                        data: vData.graphNodes,
                    });

                    nextTick(_=> {
                        if (code === 0) {
                            $notify.success({
                                offset:   -10,
                                duration: 1000,
                                title:    '提示',
                                message:  '保存成功!',
                            });
                            // methods.saveImageDataIOInfo();
                            // methods.saveDeeplearningNode();
                            methods.getDataIONodeDetail(vData.graphNodes.graph.nodes[1].id);
                            methods.getDeeplearningNodeDetail(vData.graphNodes.graph.nodes[2].id);
                        }
                        vData.startLoading = false;
                    });
                },
                prev() {
                    if (vData.active-- === 0) vData.active = 0;
                    if (vData.active === 0) {
                        // 保存数据资源信息
                        methods.saveImageDataIOInfo();
                    }
                },
                next() {
                    vData.prevActive = vData.active;
                    if (vData.active++ > 2) vData.active = 0;
                    if (vData.prevActive === 1 && vData.active === 2) {
                        // 保存数据资源信息
                        methods.saveImageDataIOInfo();
                    }
                },
                saveImageDataIOInfo() {
                    vData.formImageDataIO.flowId = vData.flow_id;
                    vData.formImageDataIO.nodeId = vData.flowInfo.graph ? vData.flowInfo.graph.nodes[1].id : vData.graphNodes.graph.nodes[1].id;
                    const $dataset_list = [];

                    vData.member_list.forEach(item => {
                        if (item.$data_set_list.length) {
                            $dataset_list.push({
                                member_id:   item.member_id,
                                member_role: item.member_role,
                                data_set_id: item.$data_set_list[0].data_set_id,
                            });
                        }
                    });
                    vData.formImageDataIO.params.data_set_list = $dataset_list;
                    console.log(vData.formImageDataIO);
                    methods.submitFormData();
                },
                async submitFormData($event) {
                    const btnState = {};

                    if($event !== 'node-update') {
                        btnState.target = $event;
                    }
                    console.log(vData.formImageDataIO);
                    const { code } = await $http.post({
                        url:  '/project/flow/node/update',
                        data: vData.formImageDataIO,
                        btnState,
                    });

                    if(code === 0) {
                        nextTick(_=> {
                            if($event !== 'node-update') {
                                $notify.success({
                                    offset:   -10,
                                    duration: 1000,
                                    title:    '提示',
                                    message:  '保存成功!',
                                });
                            }
                        });
                    }
                },
                dataIOTabchange(val) {
                    console.log(val.paneName);
                    if (val.paneName === 1 || val.paneName === '1') {
                        methods.getImageDataIOResult();
                    }
                },
                async getImageDataIOResult() {
                    const params = {
                        jobId:      vData.jobInfo.job_id,
                        flowId:     vData.flow_id,
                        flowNodeId: vData.formImageDataIO.nodeId,
                        type:       '',
                    };

                    const { code, data } = await $http.post({
                        url:  '/flow/job/task/result',
                        data: params,
                    });

                    nextTick(_=> {
                        if (code === 0) {
                            console.log(data);
                        }
                    });
                },
                deeplearningOTabchange(val) {
                    if (val.paneName === 1 || val.paneName === '1') {
                        methods.getDeeplearningOResult();
                    }
                },
                async getDeeplearningOResult() {
                    const params = {
                        jobId:      vData.jobInfo.job_id,
                        flowId:     vData.flow_id,
                        flowNodeId: vData.deepLearnNodeId,
                        type:       '',
                    };

                    const { code, data } = await $http.post({
                        url:  '/flow/job/task/result',
                        data: params,
                    });

                    nextTick(_=> {
                        if (code === 0) {
                            console.log(data);
                        }
                    });
                },
                changeSteps(val) {
                    vData.active = val;
                },
                async getMemberList() {
                    const { code, data } = await $http.get({
                        url:    '/project/member/list',
                        params: {
                            projectId: vData.flowInfo.project_id,
                        },
                    });

                    nextTick(() => {
                        if(code === 0) {
                            if(data.list.length) {
                                vData.member_list = data.list.forEach(row => {
                                    row.$data_set_list = [];
                                    if(!row.exited) {
                                        if (row.member_role === 'promoter') {
                                            vData.promoterList.push(row);
                                        } else {
                                            vData.providerList.push(row);
                                        }
                                    }
                                });
                                vData.member_list = [
                                    ...vData.promoterList,
                                    ...vData.providerList,
                                ];
                            }
                        }
                    });
                },
                async getNodeData() {
                },
                async checkDataSet(member, index) {
                    vData.currentItem = member;
                    vData.memberIndex = index;
                    vData.memberId = member.member_id;
                    vData.memberRole = member.member_role;
                    vData.showSelectDataSet = true;

                    nextTick(_ => {
                        const ref = rawDataSetListRef.value;

                        ref.searchField.project_id = vData.flowInfo.project_id;
                        ref.searchField.member_id = vData.currentItem.member_id;
                        ref.searchField.member_role = vData.currentItem.member_role;
                        ref.searchField.contains_y = vData.rawSearch.contains_y;
                        ref.searchField.data_resource_type = 'ImageDataSet';

                        ref.getDataList({
                            url:             '/project/raw_data_set/list',
                            to:              false,
                            resetPagination: true,
                        });
                        ref.isFlow = true;
                    });
                },
                selectDataSet(item) {
                    vData.showSelectDataSet = false;
                    const currentMember = vData.member_list[vData.memberIndex];
                    const dataset_list = currentMember.$data_set_list[0];
                    const dataset = {
                        ...item,
                    };

                    if(dataset_list) {
                        const { data_set_id } = dataset_list;

                        vData.member_list.forEach(item => {
                            if(item.$data_set_list[0] && item.$data_set_list[0].data_set_id === data_set_id) {
                                item.$data_set_list = [];
                            }
                        });
                        $notify({ type: 'success', message: '已自动关联相关数据资源', duration: 1000 });
                    }
                    currentMember.$data_set_list = [];
                    currentMember.$data_set_list.push(dataset);
                },
                dataSetSearch() {
                    const { allList, name, contains_y, data_set_id } = vData.rawSearch;
                    const list = [];

                    allList.forEach(row => {
                        if(row.name.includes(name) && row.data_set_id.includes(data_set_id)) {
                            if(contains_y === '' || row.contains_y === contains_y) {
                                list.push(row);
                            }
                        }
                    });
                    rawDataSetListRef.value.list = list;
                    methods.checkDataSet(vData.currentItem, vData.memberIndex);
                },
                removeDataSet(index) {
                    vData.member_list[index].$data_set_list.pop();
                },
                async saveFlowInfo($event, callback) {
                    if(vData.form.flow_name.trim() === '') {
                        vData.form.flow_name = '新流程';
                        return $message.error('流程名称不能为空!');
                    }
                    const { project_id, flow_id } = vData.flowInfo;
                    const params = {
                        name:                  vData.form.flow_name,
                        desc:                  vData.form.flow_desc,
                        FederatedLearningType: 'horizontal',
                        projectId:             project_id,
                        flowId:                flow_id,
                    };

                    const { code } = await $http.post({
                        url:  '/project/flow/update/base_info',
                        data: params,
                    });

                    if(code === 0) {
                        $notify.success({
                            offset:   -10,
                            duration: 1500,
                            title:    '提示',
                            message:  '保存成功!',
                        });

                        callback && callback();

                        if(!flow_id) {
                            router.replace({
                                query: {
                                    project_id,
                                    flow_id,
                                },
                            });
                        }
                    }
                },
                async getDataIONodeDetail (nodeId) {
                    const { code, data } = await $http.get({
                        url:    '/project/flow/node/detail',
                        params: {
                            nodeId,
                            flow_id: vData.flow_id,
                        },
                    });

                    nextTick(() => {
                        if (code === 0 && data && data.params) {
                            const { data_set_list, train_test_split_ratio } = data.params;

                            vData.formImageDataIO.params.train_test_split_ratio  = train_test_split_ratio;
                            vData.formImageDataIO.nodeId  = data.node_id;
                            vData.dataCutForm.training_ratio  = train_test_split_ratio;
                            vData.dataCutForm.verification_ratio  = 100 - train_test_split_ratio;
                            for(const memberIndex in vData.member_list) {
                                const member = vData.member_list[memberIndex];
                                const datasetIndex = data_set_list.findIndex(item => member.member_id === item.member_id && member.member_role === item.member_role && !item.deleted);

                                if(~datasetIndex) {
                                    const item = data_set_list[datasetIndex];

                                    member.$data_set_list.push({
                                        ...item,
                                    });
                                } else if(!props.isCreator) {
                                    // only creator can see all members
                                    const { member_role } = member;
                                    const list = member_role === 'promoter' ? vData.promoterList : vData.providerList;

                                    const index = list.findIndex(item => member.member_id === item.member_id);

                                    if(~index) {
                                        list.splice(index, 1);
                                    }
                                }
                            }
                        }
                        vData.startLoading = false;
                    });
                },
                async getDeeplearningNodeDetail (nodeId) {
                    const { code, data } = await $http.get({
                        url:    '/project/flow/node/detail',
                        params: {
                            nodeId,
                            flow_id: vData.flow_id,
                        },
                    });

                    nextTick(() => {
                        if (code === 0) {
                            const { params } = data || {};

                            vData.deepLearnNodeId = data.node_id;
                            if (params) {
                                if (params.image_shape.length) {
                                    vData.image_shape.aisle = params.image_shape[0] || 0;
                                    vData.image_shape.width = params.image_shape[1] || 0;
                                    vData.image_shape.height = params.image_shape[2] || 0;
                                }
                                vData.deepLearnParams = params;
                            }
                        }
                        vData.startLoading = false;
                    });
                },
                formatter(params) {
                    vData.form = {
                        ...params,
                    };
                    if(Array.isArray(params.tree_param.criterion_params)) {
                        vData.form.tree_param.criterion_params = params.tree_param.criterion_params.join('');
                    }
                    if(Array.isArray(params.objective_param.params)) {
                        vData.form.objective_param.params = params.objective_param.params.join('');
                    }
                },
                generateNodeId() {
                    return `${+new Date() + (Math.random() * 10000).toFixed(0)}`;
                },
                // Event: modify validation data scale
                onDataSetVerificationRatioChange(newVaule) {
                    vData.dataCutForm.training_ratio = 100 - newVaule;
                    vData.formImageDataIO.params.train_test_split_ratio = vData.dataCutForm.training_ratio;
                },
                // Event: drag the training and validation data scale slider
                onDataSetTrainingVerificationRatioChange(newVaule) {
                    vData.dataCutForm.verification_ratio = 100 - newVaule;
                    vData.formImageDataIO.params.train_test_split_ratio = vData.dataCutForm.training_ratio;
                },
                async saveDeeplearningNode($event) {
                    console.log($event);
                    // 1. 保存deeplearning node 数据
                    // 2. 启动流程
                    const btnState = {};

                    if($event !== 'node-update') {
                        btnState.target = $event;
                    }
                    vData.formImageDataIO.flowId = vData.flow_id;
                    vData.formImageDataIO.nodeId = vData.flowInfo.graph ? vData.flowInfo.graph.nodes[1].id : vData.graphNodes.graph.nodes[1].id;
                    vData.deepLearnParams.image_shape[0] = Number(vData.image_shape.aisle) || 0;
                    vData.deepLearnParams.image_shape[1] = Number(vData.image_shape.width) || 0;
                    vData.deepLearnParams.image_shape[2] = Number(vData.image_shape.height) || 0;
                    const params = {
                        componentType: vData.flowInfo.graph ? vData.flowInfo.graph.nodes[2].data.componentType : vData.graphNodes.graph.nodes[2].data.componentType,
                        flowId:        vData.flow_id,
                        nodeId:        vData.flowInfo.graph ? vData.flowInfo.graph.nodes[2].id : vData.graphNodes.graph.nodes[2].id,
                        params:        vData.deepLearnParams,
                    };

                    vData.startLoading = true;
                    const { code } = await $http.post({
                        url:  '/project/flow/node/update',
                        data: params,
                        btnState,
                    });

                    if(code === 0) {
                        nextTick(_ => {
                            if($event !== 'node-update') {
                                $notify.success({
                                    offset:   -10,
                                    duration: 1000,
                                    title:    '提示',
                                    message:  '保存成功!',
                                });
                            }
                            // 点击开始训练时生效
                            if ($event) methods.startFlow();
                        });
                    }
                },
                async startFlow() {
                    vData.startLoading = true;
                    const { code, data } = await $http.post({
                        url:     '/flow/start',
                        timeout: 1000 * 30,
                        data:    { flowId: vData.flow_id },
                    });

                    nextTick(_ => {
                        if(code === 0) {
                            if(data.job_id) {
                                $message.success('启动成功! ');
                                console.log(vData.flowInfo);
                                console.log(vData.flowInfo.project_id);
                                router.replace({
                                    name:  'project-detail',
                                    query: { project_id: vData.flowInfo.project_id },
                                });
                            }
                        }
                        vData.startLoading = false;
                    });
                },
            };

            onBeforeMount(() => {
                methods.getFlowInfo();
            });

            watch(
                () => vData.deepLearnParams.program,
                (newVal, oldVal) => {
                    if (newVal !== oldVal) {
                        if (newVal === 'paddle_clas') {
                            vData.deepLearnParams.architecture = vData.imageAlgorithmList[0].value;
                        } else if (newVal === 'paddle_detection') {
                            vData.deepLearnParams.architecture = vData.targetAlgorithmList[0].value;
                        }
                    }
                },
            );
            return {
                vData,
                methods,
                rawDataSetListRef,
            };
        },
    };
</script>

<style lang="scss" scoped>
.page_layer {
    overflow-y: auto;
}
.deep_flow {
    width: 100%;
    min-height: calc(100vh - 180px);
    display: flex;
    min-height: calc(100vh - 180px);
    justify-content: space-between;
    .left_content {
        // flex: 1;
        width: 790px;
        .item {
            margin: 20px 0 30px;
            // padding: 10px;
        }
        .enter_data {
            h4 {
                color: #222;
                padding-bottom: 10px;
            }
            .data_cut {
                border-top: 1px solid #eee;
                h4 {
                    margin-top: 10px;
                }
                .el-slider {
                    margin-top: 15px;
                }
            }
            .li {
                margin-bottom: 14px;
                .member-info, .data-set {
                    margin-left: 10px;
                }
                .data-set {
                    .el-form-item {
                        display: flex;
                        margin-bottom: 0;
                    }
                }
                .el-icon-circle-close {
                    cursor: pointer;
                    color: $--color-danger;
                    position: relative;
                    top: 4px;
                }
            }
        }
    }
    .step_header {
        width: 135px;
        height: 320px;
        margin-left: 70px;
        :deep(.el-step__title) {
            font-size: 14px;
            font-weight: 600;
            // cursor: pointer;
        }
        :deep(.is-finish) {
            color: #5088fc;
        }
        :deep(.el-step__icon) {
            width: 20px;
            height: 20px;
        }
        :deep(.el-step.is-vertical .el-step__line) {
            top: 2px;
            left: 9px;
        }
        :deep(.el-step__head.is-finish) {
            border-color: #5088fc;
        }
    }
}
</style>
