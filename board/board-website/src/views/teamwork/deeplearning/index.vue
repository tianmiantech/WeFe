<template>
    <el-card class="page_layer">
        <div class="deep_flow">
            <div class="left_content">
                <h3 class="flow_title">
                    <span v-if="vData.active === 0"><i class="el-icon-edit-outline"/> 基本设置</span>
                    <span v-if="vData.active === 1"><i class="el-icon-download"/> 数据输入</span>
                    <span v-if="vData.active === 2"><i class="el-icon-s-operation"/> 调整流程参数</span>
                </h3>
                <div class="step_content">
                    <div v-show="vData.active === 0" class="item base_setting">
                        <el-form
                            @submit.prevent
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
                                    @blur="methods.saveFlowInfo($event)"
                                />
                            </el-form-item>
                        </el-form>
                    </div>
                    <div v-show="vData.active === 1" class="item enter_data">
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
                                        {{ row.data_set.sample_count }}
                                    </el-form-item>
                                </el-form>
                            </div>
                        </div>
                        <el-dialog
                            title="选择数据集"
                            v-model="vData.showSelectDataSet"
                            custom-class="dialog-min-width"
                            :close-on-click-modal="false"
                            destroy-on-close
                            append-to-body
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
                                project-type="DeepLearning"
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
                        <el-form
                            @submit.prevent
                        >
                            <el-form-item label="算法类型：" required>
                                <el-select v-model="vData.form.algorithm_config.program" placeholder="请选择算法类型">
                                    <el-option
                                        v-for="item in vData.classifyList"
                                        :key="item.value"
                                        :label="item.label"
                                        :value="item.value">
                                    </el-option>
                                </el-select>
                            </el-form-item>
                            <el-form-item label="模型名称：" required>
                                <el-select v-model="vData.form.algorithm_config.architecture" placeholder="请选择模型名称">
                                    <el-option
                                        v-for="item in vData.form.algorithm_config.program === 'paddle_detection' ? vData.targetAlgorithmList : vData.imageAlgorithmList"
                                        :key="item.value"
                                        :label="item.label"
                                        :value="item.value">
                                    </el-option>
                                </el-select>
                            </el-form-item>
                            <el-form-item label="迭代次数：" required>
                                <el-input
                                    v-model="vData.form.algorithm_config.max_iter"
                                    @blur="methods.saveFlowInfo($event)"
                                />
                            </el-form-item>
                            <el-form-item label="聚合步长：" required>
                                <el-input
                                    v-model="vData.form.algorithm_config.inner_step"
                                    @blur="methods.saveFlowInfo($event)"
                                />
                            </el-form-item>
                            <el-form-item label="类别数：" required>
                                <el-input
                                    v-model="vData.form.algorithm_config.num_classes"
                                    @blur="methods.saveFlowInfo($event)"
                                />
                            </el-form-item>
                            <el-form-item label="学习率：" required>
                                <el-input
                                    v-model="vData.form.algorithm_config.base_lr"
                                    @blur="methods.saveFlowInfo($event)"
                                />
                            </el-form-item>
                            <el-form-item label="数据集路径：" required>
                                <el-input
                                    v-model="vData.form.algorithm_config.dataset_dir"
                                    @blur="methods.saveFlowInfo($event)"
                                />
                            </el-form-item>
                            <el-form-item label="图像输入尺寸：" required>
                                <el-input
                                    v-model="vData.form.algorithm_config.image_shape"
                                    @blur="methods.saveFlowInfo($event)"
                                />
                            </el-form-item>
                            <el-form-item label="批量大小：" required>
                                <el-input
                                    v-model="vData.form.algorithm_config.batch_size"
                                    @blur="methods.saveFlowInfo($event)"
                                />
                            </el-form-item>
                        </el-form>
                    </div>
                </div>
                <div class="operation_btn">
                    <el-button @click="methods.prev">上一步</el-button>
                    <el-button type="primary" @click="methods.next">下一步</el-button>
                </div>
            </div>
            <div class="step_header">
                <el-steps direction="vertical" :active="vData.active" align-center>
                    <el-step title="基本设置" @click="methods.changeSteps(0)" />
                    <el-step title="数据输入" @click="methods.changeSteps(1)" />
                    <el-step title="调整流程参数" @click="methods.changeSteps(2)" />
                </el-steps>
            </div>
        </div>
    </el-card>
</template>

<script>
    import { reactive, getCurrentInstance, computed, ref } from 'vue';
    import { useRoute, useRouter } from 'vue-router';
    import { nextTick, onBeforeMount } from '@vue/runtime-core';
    import { useStore } from 'vuex';
    import DataSetList from '@comp/views/data-set-list';

    export default {
        components: {
            DataSetList,
        },
        setup(props, context) {
            const { appContext } = getCurrentInstance();
            const { $http, $notify, $message } = appContext.config.globalProperties;
            const store = useStore();
            const userInfo = computed(() => store.state.base.userInfo);
            const route = useRoute();
            const router = useRouter();
            const rawDataSetListRef = ref();
            const vData = reactive({
                active:  0,
                flow_id: route.query.flow_id,
                form:    {
                    flow_name:        '',
                    flow_desc:        '',
                    algorithm_config: {
                        program:      'paddle_detection',
                        max_iter:     10,
                        inner_step:   10,
                        architecture: 'YOLOv3',
                        num_classes:  3,
                        base_lr:      0.00001,
                        dataset_dir:  '',
                        image_shape:  '3, 608, 608',
                        batch_size:   128,
                    },
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
                classify:  '',
                algorithm: '',
            });
            const methods = {
                async getFlowInfo() {
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
                        }
                    });
                },
                prev() {
                    if (vData.active-- === 0) vData.active = 0;
                },
                next() {
                    if (vData.active++ > 2) vData.active = 0;
                },
                changeSteps(val) {
                    vData.active = val;
                    if (vData.active === 1 && !vData.member_list.length) methods.getNodeData();
                },
                async getNodeData() {
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
                                            // only mix has more than one promoter member
                                            if(props.learningType === 'mix') {
                                                vData.promoterList.push(row);
                                            } else if(row.member_id === userInfo.value.member_id) {
                                                vData.promoterList.push(row);
                                            }
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
                            vData.inited = true;
                        }
                    });
                },
                async checkDataSet(member, index) {
                    vData.currentItem = member;
                    vData.memberIndex = index;
                    vData.memberId = member.member_id;
                    vData.memberRole = member.member_role;
                    vData.showSelectDataSet = true;
                    vData.dataSetTabName = 'raw';

                    nextTick(_ => {
                        if (props.learningType === 'horizontal' || (vData.memberRole === 'promoter' && props.learningType === 'vertical')) {
                            vData.rawSearch.contains_y = true;
                        }
                        const ref = rawDataSetListRef.value;

                        ref.searchField.project_id = vData.flowInfo.project_id;
                        ref.searchField.member_id = vData.currentItem.member_id;
                        ref.searchField.member_role = vData.currentItem.member_role;
                        ref.searchField.contains_y = vData.rawSearch.contains_y;
                        ref.searchField.data_set_type = 'ImageDataSet';

                        ref.getDataList({
                            url:             '/project/raw_data_set/list',
                            to:              false,
                            resetPagination: true,
                        });
                        ref.isFlow = true;
                    });
                },
                selectDataSet(item) {
                    console.log(item);
                    vData.showSelectDataSet = false;

                    const currentMember = vData.member_list[vData.memberIndex];
                    const dataset_list = currentMember.$data_set_list[0];
                    // const features = item.feature_name_list.split(',');
                    const dataset = {
                        ...item,
                    };

                    if(dataset_list) {
                        // remove last selected
                        const { data_set_id } = dataset_list;

                        vData.member_list.forEach(item => {
                            if(item.$data_set_list[0] && item.$data_set_list[0].data_set_id === data_set_id) {
                                item.$data_set_list = [];
                            }
                        });
                        $notify({ type: 'success', message: '已自动关联相关数据集', duration: 1000 });
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
                async submitFormData($event, params) {
                    const btnState = {};

                    if($event !== 'node-update') {
                        btnState.target = $event;
                    }
                    const { projectId, flowId } = vData.flowInfo;
                    const { code } = await $http.post({
                        url:  '/project/flow/node/update',
                        data: {
                            nodeId:        '',
                            componentType: '',
                            flowId,
                            params,
                        },
                        btnState,
                    });

                    if(code === 0) {
                        if(!flowId) {
                            router.replace({
                                query: {
                                    project_id: projectId,
                                    flow_id:    flowId,
                                },
                            });
                        }
                        if($event !== 'node-update') {
                            $notify.success({
                                offset:   -10,
                                duration: 1000,
                                title:    '提示',
                                message:  '保存成功!',
                            });
                        }
                    }
                },
            };

            onBeforeMount(() => {
                methods.getFlowInfo();
            });

            return {
                vData,
                methods,
                rawDataSetListRef,
            };
        },
    };
</script>

<style lang="scss" scoped>
.deep_flow {
    width: 100%;
    display: flex;
    justify-content: space-between;
    .left_content {
        // flex: 1;
        width: 700px;
        .item {
            margin: 20px 0 30px;
            padding: 10px;
        }
        .enter_data {
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
        width: 120px;
        height: 320px;
        margin-left: 70px;
        :deep(.el-step__title) {
            font-size: 14px;
            font-weight: 600;
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
