<template>
    <el-card class="page_layer" v-loading="vData.startLoading">
        <div class="deep_flow">
            <div class="left_content">
                <div class="step_content">
                    <el-collapse v-model="vData.activeNames">
                        <el-collapse-item title="基本设置" name="base">
                            <div class="item base_setting">
                                <el-form
                                    @submit.prevent
                                    :disabled="vData.flowInfo.my_role !=='promoter'"
                                >
                                    <el-form-item
                                        label="流程名称："
                                        class="is-required"
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
                                    <el-form-item label="训练类型：">
                                        <p>{{ vData.flowType === 'detection' ? '目标检测' : vData.flowType === 'classify' ? '图像分类' : '' }}</p>
                                    </el-form-item>
                                </el-form>
                            </div>
                        </el-collapse-item>
                        <el-collapse-item title="参数设置" name="params">
                            <div class="params_box">
                                <div class="item enter_data mr40">
                                    <div class="data_select">
                                        <h4 class="mb5">选择数据资源</h4>
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
                                                    选择数据资源
                                                </el-button>
                                            </p>

                                            <div
                                                v-if="member.audit_status === 'agree'"
                                                class="data-set f14"
                                            >
                                                <el-form
                                                    v-for="row in member.$data_set_list"
                                                    :key="row.id"
                                                    label-width="130px"
                                                >
                                                    <el-form-item label="数据资源名称：">
                                                        {{ row.data_resource.name }}
                                                        <el-icon
                                                            v-if="!vData.disabled"
                                                            class="el-icon-circle-close f16 ml10"
                                                            @click="methods.removeDataSet(index)"
                                                        >
                                                            <elicon-circle-close />
                                                        </el-icon>
                                                    </el-form-item>
                                                    <el-form-item label="数据资源id："> {{ row.data_set_id }} </el-form-item>
                                                    <el-form-item v-if="row.data_resource.description" label="数据资源简介：">
                                                        {{ row.data_resource.description }}
                                                    </el-form-item>
                                                    <el-form-item label="关键词：">
                                                        <template v-for="item in row.data_resource.tags.split(',')" :key="item">
                                                            <el-tag
                                                                v-show="item"
                                                                class="mr10"
                                                            >
                                                                {{ item }}
                                                            </el-tag>
                                                        </template>
                                                    </el-form-item>
                                                    <el-form-item label="数据总量/已标注：">
                                                        {{ row.data_resource.total_data_count }} / {{ row.data_resource.labeled_count }}
                                                    </el-form-item>
                                                    <el-form-item label="样本分类：">
                                                        {{row.data_resource.for_job_type === 'classify' ? '图像分类' : row.data_resource.for_job_type === 'detection' ? '目标检测' : '-'}}
                                                    </el-form-item>
                                                    <el-form-item v-if="row.data_resource.label_list" label="标注标签：">
                                                        <template v-if="!vData.isAllLabel && row.data_resource.label_list.split(',').length>9">
                                                            <template v-for="item in row.data_resource.label_list_part" :key="item">
                                                                <el-tag
                                                                    v-show="item"
                                                                    class="mr10"
                                                                >
                                                                    {{ item }}
                                                                </el-tag>
                                                            </template>
                                                        </template>
                                                        <template v-else>
                                                            <template v-for="item in row.data_resource.label_list.split(',')" :key="item">
                                                                <el-tag
                                                                    v-show="item"
                                                                    class="mr10"
                                                                >
                                                                    {{ item }}
                                                                </el-tag>
                                                            </template>
                                                        </template>
                                                        <span v-if="row.data_resource.label_list.split(',').length>9" @click="vData.isAllLabel = !vData.isAllLabel" class="check_tips">
                                                            {{ vData.isAllLabel ? '收起' : '查看全部' }}
                                                            <el-icon v-if="!vData.isAllLabel">
                                                                <elicon-arrow-down />
                                                            </el-icon>
                                                            <el-icon v-else>
                                                                <elicon-arrow-up />
                                                            </el-icon>
                                                        </span>
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
                                                            size="small"
                                                        />
                                                    </div>

                                                    <div class="float-right">
                                                        <p style="font-weight:bold;" class="text-r color-danger mb5">验证:</p>
                                                        <el-input-number
                                                            v-model="vData.dataCutForm.verification_ratio"
                                                            style="width:100px"
                                                            size="small"
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
                                </div>
                                <div class="item params_setting">
                                    <h4 class="mb5">设置模型参数</h4>
                                    <el-form
                                        @submit.prevent
                                        :disabled="vData.flowInfo.my_role !=='promoter'"
                                    >
                                        <el-form-item label="算法类型：" class="is-required">
                                            <el-select v-model="vData.deepLearnParams.program" disabled placeholder="请选择算法类型">
                                                <el-option
                                                    v-for="item in vData.classifyList"
                                                    :key="item.value"
                                                    :label="item.label"
                                                    :value="item.value">
                                                </el-option>
                                            </el-select>
                                        </el-form-item>
                                        <el-form-item label="模型名称：" class="is-required">
                                            <el-select v-model="vData.deepLearnParams.architecture" placeholder="请选择模型名称">
                                                <el-option
                                                    v-for="item in vData.deepLearnParams.program === 'paddle_detection' ? vData.targetAlgorithmList : vData.imageAlgorithmList"
                                                    :key="item.value"
                                                    :label="item.label"
                                                    :value="item.value">
                                                </el-option>
                                            </el-select>
                                        </el-form-item>
                                        <el-form-item label="迭代次数：" class="is-required">
                                            <el-input
                                                v-model="vData.deepLearnParams.max_iter"
                                            />
                                        </el-form-item>
                                        <el-form-item label="聚合步长：" class="is-required">
                                            <el-input
                                                v-model="vData.deepLearnParams.inner_step"
                                            />
                                        </el-form-item>
                                        <el-form-item label="学习率：" class="is-required">
                                            <el-input
                                                v-model="vData.deepLearnParams.base_lr"
                                            />
                                        </el-form-item>
                                        <el-form-item label="图片通道数：" class="is-required">
                                            <el-radio-group v-model="vData.image_shape.aisle">
                                                <el-radio :label="3">彩色（3）</el-radio>
                                                <el-radio :label="1">黑白（1）</el-radio>
                                            </el-radio-group>
                                        </el-form-item>
                                        <el-form-item label="图片宽度( px )：" class="is-required">
                                            <el-input
                                                type="number"
                                                v-model="vData.image_shape.width"
                                            />
                                        </el-form-item>
                                        <el-form-item label="图片高度( px )：" class="is-required">
                                            <el-input
                                                type="number"
                                                v-model="vData.image_shape.height"
                                            />
                                        </el-form-item>
                                        <el-form-item label="批量大小：" class="is-required">
                                            <el-input
                                                v-model="vData.deepLearnParams.batch_size"
                                            />
                                        </el-form-item>
                                    </el-form>
                                </div>
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
                                        :member-id="vData.memberId"
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
                        </el-collapse-item>
                        <el-collapse-item v-if="vData.jobInfo.status" title="模型结果" name="result">
                            <deeplearning-result
                                ref="deeplearningResultRef"
                                v-if="vData.showDLResult"
                                :job-id="vData.jobInfo.job_id"
                                :flow-node-id="vData.deepLearnNodeId"
                                :my-role="vData.flowInfo.project.my_role"
                                type="loss"
                                :autoReadResult="true"
                                :member-job-detail-list="vData.memberJobDetailList"
                                :flow-type="vData.flowType"
                            />
                        </el-collapse-item>
                    </el-collapse>
                </div>
                <div class="operations_btn mt20">
                    <span v-if="vData.stopNext" class="stop_next_tips">请确保成员数据资源标注标签统一！</span>
                    <div class="mt20">
                        <el-button
                            type="primary"
                            @click="methods.saveDeeplearningNode"
                            :disabled="vData.flowInfo.my_role !=='promoter'"
                        >
                            开始训练
                        </el-button>
                        <template v-if="vData.jobInfo.status === 'running'">
                            <el-button
                                type="warning"
                                @click="methods.pause"
                            >
                                暂停运行
                            </el-button>
                        </template>
                        <template v-if="vData.jobInfo.status === 'stop_on_running' || vData.jobInfo.status === 'error_on_running'">
                            <el-button
                                type="primary"
                                @click="methods.resume"
                            >
                                继续运行
                            </el-button>
                        </template>
                    </div>
                </div>
            </div>
        </div>
    </el-card>
</template>

<script>
    import {
        reactive,
        getCurrentInstance,
        nextTick,
        ref,
        watch,
        onBeforeMount,
        onBeforeUnmount,
    } from 'vue';
    import { useRoute, useRouter } from 'vue-router';
    import DataSetList from '@comp/views/data-set-list';
    import DeeplearningResult from './components/deeplearning-result.vue';

    const imageAlgorithmList = ['LeNet', 'AlexNet', 'VGG11', 'VGG13', 'VGG16', 'VGG19', 'ShuffleNetV2_x0_25', 'ShuffleNetV2_x0_33', 'ShuffleNetV2_x0_5', 'ShuffleNetV2_x1_0', 'ShuffleNetV2_x1_5', 'ShuffleNetV2_x2_0', 'SqueezeNet1_0', 'SqueezeNet1_1', 'InceptionV4', 'Xception41', 'Xception65', 'Xception71', 'ResNet18', 'ResNet34','ResNet50', 'ResNet101', 'ResNet152', 'ResNet50_vc', 'ResNet101_vc', 'ResNet152_vc', 'ResNet18_vd', 'ResNet34_vd', 'ResNet50_vd', 'ResNet101_vd', 'ResNet152_vd', 'ResNet200_vd', 'SE_ResNet18_vd', 'SE_ResNet34_vd', 'SE_ResNet50_vd', 'SE_ResNet101_vd', 'SE_ResNet152_vd', 'SE_ResNet200_vd', 'SE_ResNeXt50_32x4d', 'SE_ResNeXt101_32x4d', 'SE_ResNeXt152_32x4d', 'SE_ResNeXt50_vd_32x4d', 'SE_ResNeXt101_vd_32x4d', 'SENet154_vd', 'DenseNet121', 'DenseNet161', 'DenseNet169', 'DenseNet201', 'DenseNet264', 'DarkNet53', 'ResNeXt50_64x4d', 'ResNeXt101_64x4d', 'ResNeXt152_64x4d', 'ResNeXt50_32x4d', 'ResNeXt101_32x4d', 'ResNeXt152_32x4d', 'ResNeXt50_vd_64x4d', 'ResNeXt101_vd_64x4d', 'ResNeXt152_vd_64x4d', 'ResNeXt50_vd_32x4d', 'ResNeXt101_vd_32x4d', 'ResNeXt152_vd_32x4d', 'Res2Net50_48w_2s', 'Res2Net50_26w_4s', 'Res2Net50_14w_8s', 'Res2Net50_26w_6s', 'Res2Net50_26w_8s', 'Res2Net101_26w_4s', 'Res2Net152_26w_4s', 'Res2Net50_vd_48w_2s', 'Res2Net50_vd_26w_4s', 'Res2Net50_vd_14w_8s', 'Res2Net50_vd_26w_6s', 'Res2Net50_vd_26w_8s', 'Res2Net101_vd_26w_4s', 'Res2Net152_vd_26w_4s', 'Res2Net200_vd_26w_4s', 'DPN68','DPN92', 'DPN98','DPN107', 'DPN131', 'MobileNetV1_x0_25', 'MobileNetV1_x0_5', 'MobileNetV1_x1_0', 'MobileNetV1_x0_75','MobileNetV3_small_x0_25', 'MobileNetV3_small_x0_5', 'MobileNetV3_small_x0_75', 'MobileNetV3_small_x1_0','MobileNetV3_small_x1_25', 'HRNet_W18_C', 'HRNet_W32_C', 'HRNet_W48_C','HRNet_W64_C'];

    export default {
        components: {
            DataSetList,
            DeeplearningResult,
        },
        setup(props, context) {
            const { appContext } = getCurrentInstance();
            const { $http, $notify, $confirm, $message, $bus } = appContext.config.globalProperties;
            const route = useRoute();
            const router = useRouter();
            const rawDataSetListRef = ref();
            const deeplearningResultRef = ref();
            const { training_type } = route.query;
            const vData = reactive({
                loading:    false,
                active:     0,
                flow_id:    route.query.flow_id,
                project_id: route.query.project_id,
                form:       {
                    flow_name: '',
                    flow_desc: '',
                },
                deepLearnParams: {
                    program:      training_type === 'detection' ? 'paddle_detection' : training_type === 'classify' ? 'paddle_clas' : 'paddle_detection',
                    max_iter:     10,
                    inner_step:   10,
                    architecture: training_type === 'detection' ? 'yolov3_darknet_voc' : training_type === 'classify' ? 'LeNet' : 'ppyolo_r18vd',
                    // num_classes:  3,
                    base_lr:      0.00001,
                    image_shape:  [],
                    batch_size:   128,
                },
                image_shape: {
                    aisle:  3,
                    width:  224,
                    height: 224,
                },
                flowInfo: {
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
                        label: 'ppyolo_r18vd',
                        value: 'ppyolo_r18vd',
                    },
                    {
                        label: 'ssd_vgg16_300_voc',
                        value: 'ssd_vgg16_300_voc',
                    },
                    {
                        label: 'ssd_vgg16_512_voc',
                        value: 'ssd_vgg16_512_voc',
                    },
                    {
                        label: 'yolov3_darknet_voc',
                        value: 'yolov3_darknet_voc',
                    },
                    {
                        label: 'yolov3_mobilenet_v1_voc',
                        value: 'yolov3_mobilenet_v1_voc',
                    },
                    {
                        label: 'yolov3_r34_voc',
                        value: 'yolov3_r34_voc',
                    },
                    {
                        label: 'yolov4_cspdarknet_voc',
                        value: 'yolov4_cspdarknet_voc',
                    },
                ],
                imageAlgorithmList: [],
                formImageDataIO:    {
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
                prevActive:          0,
                graphNodes:          {},
                startLoading:        false,
                jobInfo:             {},
                taskInfo:            [],
                deepLearnNodeId:     '',
                imageDataIONodeId:   '',
                showDataIOResult:    false,
                showDLResult:        false,
                flowType:            training_type || 'detection',
                isAllLabel:          false,
                stopNext:            false,
                memberJobDetailList: [],
                jobDetailTimer:      null,
                jobProgressTimer:    null,
                pauseJobTimer:       null,
                resumeJobTimer:      null,
                activeNames:         ['base', 'params'],
                datacutTimer:        null,
            });

            if(vData.flowType === 'detection') {
                vData.image_shape.width = 608;
                vData.image_shape.height = 608;
            }

            vData.imageAlgorithmList = imageAlgorithmList.map(x => {
                return {
                    label: x,
                    value: x,
                };
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
                            vData.flowType = data.deep_learning_job_type;
                            methods.getJobDetail();
                            methods.changeHeaderTitle();
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
                            methods.getJobMemberDetail();
                            if(data.job.status === 'wait_run' || data.job.status === 'wait_stop') {
                                if (vData.jobDetailTimer) clearTimeout(vData.jobDetailTimer);
                                vData.jobDetailTimer = setTimeout(() => {
                                    methods.getJobDetail();
                                }, 3000);
                            } else {
                                vData.activeNames = ['result'];
                                vData.showDLResult = true;
                            }
                        }
                    });
                },
                async getJobMemberDetail() {
                    const { code, data } = await $http.get({
                        url:    '/flow/job/get_progress',
                        params: {
                            requestFromRefresh: true,
                            jobId:              vData.jobInfo.job_id,
                        },
                    });

                    nextTick(()=>{
                        if (code === 0 && data) {
                            vData.memberJobDetailList = data;

                            const isRunning = data.filter(x => x.job_status === 'running');

                            if(isRunning) {
                                if (vData.jobProgressTimer) clearTimeout(vData.jobProgressTimer);
                                vData.jobProgressTimer = setTimeout(() => {
                                    methods.getJobMemberDetail();
                                }, 3000);
                            }
                        }
                    });
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
                                        componentType: vData.flowType === 'detection' ? 'PaddleDetection' : 'PaddleClassify',
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
                    if(vData.active === 1) {
                        // 检查数据集是否被选
                    }
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
                    methods.submitFormData();
                },
                async submitFormData($event) {
                    const btnState = {};

                    if($event !== 'node-update') {
                        btnState.target = $event;
                    }
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
                    if (val.paneName === 1 || val.paneName === '1') {
                        vData.showDataIOResult = true;
                    } else {
                        vData.showDataIOResult = false;
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
                async getDeeplearningOResult() {
                    const params = {
                        jobId:      vData.jobInfo.job_id,
                        flowId:     vData.flow_id,
                        flowNodeId: vData.deepLearnNodeId,
                        type:       'loss',
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
                            projectId: vData.project_id,
                        },
                    });

                    nextTick(() => {
                        if(code === 0) {
                            if(data.list.length) {
                                data.list.forEach(row => {
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
                        ref.searchField.forJobType = vData.flowType || '';
                        ref.isFlow = true;

                        ref.getDataList({
                            url:             '/project/raw_data_set/list',
                            to:              false,
                            resetPagination: true,
                        });
                    });
                },
                selectDataSet(item) {
                    vData.stopNext = false;
                    vData.showSelectDataSet = false;
                    const currentMember = vData.member_list[vData.memberIndex];
                    const dataset_list = currentMember.$data_set_list[0];
                    const label_list = item.data_resource.label_list.split(',');
                    const label_list_part = [];

                    for (let i=0; i<9; i++) {
                        label_list_part.push(label_list[i]);
                    }
                    item.data_resource.label_list_part = label_list_part;
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
                    // if (vData.member_list[0].$data_set_list.length && vData.member_list[1].$data_set_list.length) {
                    //     const isEqual = vData.member_list[0].$data_set_list[0] && vData.member_list[1].$data_set_list[0] && methods.scalarArrayEquals(vData.member_list[0].$data_set_list[0].data_resource.label_list.split(','), vData.member_list[1].$data_set_list[0].data_resource.label_list.split(','));

                    //     if (isEqual) {
                    //         methods.saveImageDataIOInfo();
                    //         methods.submitFormData();
                    //     } else {
                    //         $message.error('请确保成员数据资源标注标签统一！');
                    //         vData.stopNext = true;
                    //     }
                    // }
                    methods.saveImageDataIOInfo();
                },
                scalarArrayEquals(array1,array2) {
                    return array1.length === array2.length && array1.every(function(v,i) { return v === array2[i];});
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
                    methods.saveImageDataIOInfo();
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
                    vData.imageDataIONodeId = nodeId;
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
                                    const label_list = item.data_resource.label_list.split(',');
                                    const label_list_part = [];

                                    for (let i=0; i<9; i++) {
                                        label_list_part.push(label_list[i]);
                                    }
                                    item.data_resource.label_list_part = label_list_part;
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
                        if (code === 0 && data && data.params && Object.keys(data.params).length) {
                            const { params } = data;

                            vData.deepLearnNodeId = data.node_id;
                            vData.deepLearnParams.program = data.component_type === 'detection' ? 'paddle_detection' : data.component_type === 'classify' ? 'paddle_clas' : 'paddle_detection';
                            if (params) {
                                if (params.image_shape.length) {
                                    vData.image_shape.aisle = params.image_shape[0] || 3;
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
                    methods.radioSet();
                },
                // Event: drag the training and validation data scale slider
                onDataSetTrainingVerificationRatioChange(newVaule) {
                    vData.dataCutForm.verification_ratio = 100 - newVaule;
                    methods.radioSet();
                },
                radioSet() {
                    vData.formImageDataIO.params.train_test_split_ratio = vData.dataCutForm.training_ratio;
                    if(vData.datacutTimer) clearTimeout(vData.datacutTimer);
                    vData.datacutTimer = setTimeout(() => {
                        methods.saveImageDataIOInfo();
                    }, 1000);
                },
                async saveDeeplearningNode($event) {
                    // 1. 保存deeplearning node 数据
                    // 2. 启动流程
                    const btnState = {};

                    if($event !== 'node-update') {
                        btnState.target = $event;
                    }
                    vData.formImageDataIO.flowId = vData.flow_id;
                    vData.formImageDataIO.nodeId = vData.flowInfo.graph ? vData.flowInfo.graph.nodes[1].id : vData.graphNodes.graph.nodes[1].id;
                    vData.deepLearnParams.image_shape[0] = Number(vData.image_shape.aisle) || 3;
                    vData.deepLearnParams.image_shape[1] = Number(vData.image_shape.width) || 0;
                    vData.deepLearnParams.image_shape[2] = Number(vData.image_shape.height) || 0;
                    const params = {
                        componentType: vData.flowInfo.graph ? vData.flowInfo.graph.nodes[2].data.componentType : vData.graphNodes.graph.nodes[2].data.componentType,
                        flowId:        vData.flow_id,
                        nodeId:        vData.flowInfo.graph ? vData.flowInfo.graph.nodes[2].id : vData.graphNodes.graph.nodes[2].id,
                        params:        vData.deepLearnParams,
                    };

                    // return;
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
                                methods.getJobDetail();
                            }
                        }
                        vData.startLoading = false;
                    });
                },
                pause() {
                    $confirm('确定要暂停任务吗', '警告', {
                        type: 'warning',
                    }).then(async action => {
                        if(action === 'confirm') {
                            vData.startLoading = true;
                            const { code, data } = await $http.post({
                                url:  '/flow/job/stop',
                                data: {
                                    jobId: vData.jobInfo.job_id,
                                },
                            });

                            nextTick(_ => {
                                vData.startLoading = false;
                                if(code === 0) {
                                    if (vData.pauseJobTimer) clearTimeout(vData.pauseJobTimer);
                                    vData.pauseJobTimer = setTimeout(() => {
                                        methods.getJobDetail();
                                        deeplearningResultRef.value.methods.tabChange();
                                    }, 500);
                                    $message.success('操作成功! 请稍后');
                                } else {
                                    $message.error(data.message || '未知错误');
                                }
                            });
                        }
                    });
                },
                resume() {
                    $confirm('确定要继续执行任务吗', '警告', {
                        type: 'warning',
                    }).then(async action => {
                        if(action === 'confirm') {
                            vData.startLoading = true;
                            const { code, data } = await $http.post({
                                url:  '/flow/job/resume',
                                data: {
                                    jobId: vData.jobInfo.job_id,
                                },
                            });

                            nextTick(_ => {
                                vData.startLoading = false;
                                if(code === 0) {
                                    if (vData.resumeJobTimer) clearTimeout(vData.resumeJobTimer);
                                    vData.resumeJobTimer = setTimeout(() => {
                                        methods.getJobDetail();
                                        deeplearningResultRef.value.methods.tabChange();
                                    }, 500);
                                    $message.success('操作成功! 请稍后');
                                } else {
                                    $message.error(data.message || '未知错误');
                                }
                            });
                        }
                    });
                },
                changeHeaderTitle() {
                    if(route.meta.titleParams) {
                        const htmlTitle = `<strong>${vData.flowInfo.project.name}</strong> - ${vData.flowInfo.flow_name} (${vData.flowType === 'detection' ? '目标检测' : vData.flowType === 'classify' ? '图像分类' : ''})`;

                        $bus.$emit('change-layout-header-title', { meta: htmlTitle });
                    }
                },
            };

            onBeforeMount(() => {
                methods.getFlowInfo();
                methods.getMemberList();
                $bus.$on('history-backward', () => {
                    router.push({
                        name:  'project-detail',
                        query: {
                            project_id: vData.flowInfo.project_id,
                        },
                    });
                });
            });

            onBeforeUnmount(_ => {
                $bus.$off('history-backward');
                clearTimeout(vData.jobDetailTimer);
                clearTimeout(vData.jobProgressTimer);
                clearTimeout(vData.pauseJobTimer);
                clearTimeout(vData.resumeJobTimer);
                clearTimeout(vData.datacutTimer);
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
                deeplearningResultRef,
            };
        },
    };
</script>

<style lang="scss" scoped>
.page_layer {
    overflow-y: auto;
    position: relative;
    .fixed_alert {
        position: fixed;
        left: 50%;
        z-index:20;
        width: 30%;
        transform: translateX(-50%);
        padding-left: 20px;
        cursor: pointer;
    }
}
.deep_flow {
    // width: 100%;
    min-height: calc(100vh - 180px);
    display: flex;
    justify-content: space-between;
    :deep(.el-collapse-item__header) {
        font-size: 16px;
    }
    .left_content {
        // flex: 1;
        // width: 790px;
        width: 100%;
        .params_box {
            width: 100%;
            display: flex;
            // .item {
            //     flex: 1;
            // }
        }
        .enter_data {
            width: 400px;
            margin-right: 100px;
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
                    top: 2px;
                }
            }
        }
        .params_setting {
            width: 400px;
        }
    }
    .step_content {
        .base_setting {
            width: 900px;
        }
        .check_tips {
            font-size: 12px;
            color: #999;
            cursor: pointer;
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
    .operations_btn {
        .stop_next_tips {
            font-size: 12px;
            color: $--color-danger;
            padding-left: 6px;
        }
    }
}
</style>
