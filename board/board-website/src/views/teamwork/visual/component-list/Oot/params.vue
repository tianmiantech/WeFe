<template>
    <el-form
        v-loading="vData.loading"
        :disabled="disabled"
        @submit.prevent
        inline
    >
        <span
            v-if="ootModelFlowNodeId"
            class="board-button board-button--text board-button--small mb10"
            @click="methods.checkJobDetail"
        >
            查看前置流程详情
            <el-icon>
                <elicon-top-right />
            </el-icon>
        </span>
        <h4>选择验证数据资源:</h4>
        <p class="f12 mt5 mb15">tips: 数据资源需包含原流程数据资源中的所有列</p>
        <div
            v-for="(member, index) in vData.member_list"
            v-show="disabled ? member.$data_set_list.length : true"
            :key="member.id"
        >
            <h3
                v-if="index === 0"
                class="role-title pb5"
            >
                发起方:
            </h3>
            <h3
                v-if="index === vData.promoterList.length"
                class="role-title pt20 pb5"
            >
                协作方:
            </h3>
            <p class="member-info">
                <span class="name f14">
                    <el-icon
                        v-if="member.audit_status !== 'agree'"
                        class="board-icon-warning-outline color-danger"
                    >
                        <elicon-warning />
                    </el-icon>
                    {{ member.member_name }}
                </span>
                <span
                    v-if="member.audit_status !== 'agree'"
                    class="f12"
                >({{ member.audit_comment || '审核通过的成员才能参与流程' }})</span>
                <el-button
                    v-if="member.audit_status === 'agree' && !disabled"
                    type="text"
                    class="ml10 mr30"
                    @click="methods.checkDataSet(member, index)"
                >
                    选择数据资源
                </el-button>
                <span
                    class="board-link board-link--info f12"
                    @click="methods.ootFeaturePreview($event, member.member_id)"
                >预览原入模特征列</span>
            </p>

            <div
                v-if="member.audit_status === 'agree'"
                class="data-set"
            >
                <el-form
                    v-for="row in member.$data_set_list"
                    :key="row.id"
                    label-width="110px"
                >
                    <el-form-item label="数据资源名称：">
                        {{ row.name }}
                        <el-tag
                            v-if="row.contains_y"
                            type="success"
                            size="small"
                            class="ml5"
                        >
                            y
                        </el-tag>
                        <el-icon
                            v-if="!disabled"
                            title="移除"
                            class="board-icon-circle-close f20"
                            @click="methods.removeDataSet(index)"
                        >
                            <elicon-circle-close />
                        </el-icon>
                    </el-form-item>
                    <el-form-item label="数据资源id：">
                        {{ row.data_set_id }}
                    </el-form-item>
                    <el-form-item label="数据量/特征量：">
                        {{ row.total_data_count ? row.total_data_count : row.row_count }} / {{ row.feature_count }}
                    </el-form-item>
                    <div class="features mt5 mb10">
                        <template v-for="(item, $index) in row.$column_name_list" :key="$index">
                            <el-tag
                                v-if="$index < 20"
                                :label="item"
                                :value="item"
                            >
                                {{ item }}
                            </el-tag>
                        </template>
                        <el-button
                            v-if="row.$column_name_list.length > 20"
                            size="small"
                            type="primary"
                            class="check-features"
                            @click="methods.checkFeatures(row)"
                        >
                            查看更多
                        </el-button>
                    </div>
                </el-form>
            </div>
        </div>
        <div v-if="!vData.check_result" class="flex-form">
            <h4 class="f14 mb10 mr30 pr20">前置流程缺少评估模型, 请选择评估模型入参:</h4>
            <el-form-item label="评估类别：">
                <el-select v-model="vData.form.eval_type">
                    <el-option
                        v-for="(model, index) in vData.evalTypes"
                        :key="index"
                        :label="model.text"
                        :value="model.value"
                    />
                </el-select>
            </el-form-item>

            <el-form-item label="正标签类型：">
                <el-input v-model="vData.form.pos_label" />
            </el-form-item>
        </div>
        <el-form>
            <el-form-item v-if="vData.exitVertComponent" label="是否启用PSI分箱（预测概览概率/评分）">
                <el-switch v-model="vData.need_psi" active-color="#13ce66"/>
            </el-form-item>
        </el-form>
        <psi-bin 
            v-if="vData.exitVertComponent && vData.need_psi"
            v-model:binValue="vData.binValue" 
            title="PSI分箱方式（预测概率/评分）"
            :disabled="disabled"
            :filterMethod="['quantile']"
        />
        <!-- Select the dataset for the specified member -->
        <el-dialog
            title="选择数据资源"
            v-model="vData.showSelectDataSet"
            custom-class="dialog-min-width"
            :close-on-click-modal="false"
            destroy-on-close
            append-to-body
            width="70%"
        >
            <el-tabs
                v-model="vData.dataSetTabName"
                @tab-click="methods.dataSetTabChange"
            >
                <el-tab-pane
                    label="原始数据资源"
                    name="raw"
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
                        <el-form-item label="是否包含 Y">
                            <el-select
                                v-model="vData.rawSearch.contains_y"
                                style="width: 100px"
                                :disabled="learningType === 'horizontal' || (vData.memberRole === 'promoter' && learningType === 'vertical')"
                            >
                                <el-option
                                    label="全部"
                                    value=""
                                />
                                <el-option
                                    label="是"
                                    :value="true"
                                />
                                <el-option
                                    label="否"
                                    :value="false"
                                />
                            </el-select>
                        </el-form-item>
                        <el-form-item>
                            <el-button
                                type="primary"
                                native-type="submit"
                                @click="methods.dataSetSearch"
                            >
                                搜索
                            </el-button>
                        </el-form-item>
                    </el-form>
                    <DataSetList
                        ref="rawDataSetListRef"
                        :audit-status="true"
                        :search-field="vData.rawSearch"
                        :paramsExclude="['allList', 'list']"
                        @list-loaded="methods.listLoaded"
                        @selectDataSet="methods.selectDataSet"
                        @close-dialog="vData.showSelectDataSet=false;"
                    >
                        <template #data-add>
                            <i />
                        </template>
                    </DataSetList>
                </el-tab-pane>
                <el-tab-pane
                    ref="derivedRef"
                    label="衍生数据资源"
                    name="derived"
                >
                    <el-alert
                        effect="dark"
                        type="success"
                        :closable="false"
                        title="使用衍生数据资源将 自动替换 关联成员已选的数据资源"
                    />
                    <el-form class="mt10" inline>
                        <el-form-item label="名称">
                            <el-input
                                v-model="vData.derivedSearch.name"
                                clearable
                            />
                        </el-form-item>
                        <el-form-item label="id">
                            <el-input
                                v-model="vData.derivedSearch.data_set_id"
                                clearable
                            />
                        </el-form-item>
                        <el-button
                            class="mb20"
                            type="primary"
                            @click="dataSetTabChange('derived')"
                        >
                            搜索
                        </el-button>
                    </el-form>
                    <DataSetList
                        ref="derivedDataSetListRef"
                        :paramsExclude="['allList', 'list']"
                        :search-field="vData.derivedSearch"
                        @selectDataSet="methods.selectDataSet"
                        @close-dialog="vData.showSelectDataSet=false;"
                    >
                        <template #data-add>
                            <i />
                        </template>
                    </DataSetList>
                </el-tab-pane>
            </el-tabs>
        </el-dialog>
    </el-form>
</template>

<script>
    import {
        ref,
        reactive,
        computed,
        nextTick,
        getCurrentInstance,
        watch,
    } from 'vue';
    import { useStore } from 'vuex';
    import { useRoute, useRouter } from 'vue-router';
    import DataSetList from '@comp/views/data-set-list';
    import psiBin from '../../components/psi/psi-bin';
    import { checkExitVertModelComponet } from '@src/service';
    import { psiCustomSplit,replace } from '../common/utils';

    export default {
        name:       'Oot',
        components: {
            DataSetList,
            psiBin,
        },
        props: {
            projectId:          String,
            flowId:             String,
            disabled:           Boolean,
            isCreator:          Boolean,
            learningType:       String,
            currentObj:         Object,
            jobId:              String,
            class:              String,
            ootModelFlowNodeId: String,
            ootJobId:           String,
        },
        setup(props, context) {
            const store = useStore();
            const route = useRoute();
            const router = useRouter();
            const userInfo = computed(() => store.state.base.userInfo);
            const { appContext } = getCurrentInstance();
            const {
                $http,
                $alert,
                $notify,
            } = appContext.config.globalProperties;
            const derivedRef = ref();
            const derivedDataSetListRef = ref();
            const rawDataSetListRef = ref();
            const vData = reactive({
                inited:            false,
                locker:            false,
                loading:           false,
                memberId:          '',
                memberRole:        '',
                memberIndex:       0,
                member_list:       [],
                history_list:      [],
                data_set_id:       [],
                data_set_list:     [],
                column_list:       [],
                checkedColumns:    '',
                checkedColumnsArr: [],
                showColumnList:    false,
                indeterminate:     false,
                checkedAll:        false,
                showSelectDataSet: false,
                dataSetTabName:    'raw',
                derivedSearch:     {
                    name:        '',
                    data_set_id: '',
                },
                rawSearch: {
                    allList:     [],
                    list:        [],
                    name:        '',
                    contains_y:  '',
                    data_set_id: '',
                },
                currentItem:  {}, // current member
                providerList: [],
                promoterList: [],
                check_result: true, // Is there an evaluation model
                evalTypes:    [
                    { value: 'binary',text: 'binary' },
                    { value: 'regression',text: 'regression' },
                    { value: 'multi',text: 'multi' },
                ],
                bin_method: [
                    { value: 'bucket',text: '等宽' },
                ],
                form: {
                    eval_type: 'binary',
                    pos_label: 1,
                },
                oot_job_id:       '',
                need_psi:         false,
                prob_need_to_bin: false,
                binValue:         {
                    method:       'bucket',
                    binNumber:    6,
                    split_points: '',
                },
                exitVertComponent: false,
            });

            const methods = {
                async getNodeDetail(model) {
                    methods.checkExistVertModel(model);
                    const { code, data } = await $http.get({
                        url:    '/project/flow/node/detail',
                        params: {
                            nodeId:  model.id,
                            flow_id: props.flowId,
                        },
                    });

                    if (code === 0 && data && data.params && data.params.dataset_list) {
                        const { dataset_list, eval_type, pos_label, psi_param } = data.params;

                        for(const memberIndex in vData.member_list) {
                            const member = vData.member_list[memberIndex];
                            const datasetIndex = dataset_list.findIndex(item => member.member_id === item.member_id && member.member_role === item.member_role && !item.deleted);

                            if(~datasetIndex) {
                                const item = dataset_list[datasetIndex];
                                const column_name_list = item.derived_from ? item.features : item.feature_name_list.split(',');

                                member.$data_set_list.push({
                                    ...item,
                                    // all features
                                    column_name_list,
                                    // checked features
                                    $column_name_list: item.features,
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
                            vData.member_list = [
                                ...vData.promoterList,
                                ...vData.providerList,
                            ];
                        }

                        vData.form.eval_type = eval_type || 'binary';
                        vData.form.pos_label = pos_label || 1;
                        if(psi_param) {
                            const { bin_method, bin_num, need_psi, split_points } = psi_param;

                            vData.need_psi = need_psi;
                            vData.binValue = {
                                method:       bin_method,
                                binNumber:    bin_num ,
                                split_points: split_points ? split_points.join() : '',
                            };
                        }
                    }
                },

                /**
                 * 判断是否展示psi组件
                 */
                checkExistVertModel(model){
                    const { ootModelFlowNodeId,flowId,ootJobId } = props;

                    checkExitVertModelComponet({
                        nodeId:      model.id,
                        modelNodeId: ootModelFlowNodeId,
                        flowId,jobId:       ootJobId,
                    }).then((bool = false)=>{
                        vData.exitVertComponent = bool;
                    });
                },

                async getNodeData() {
                    const { code, data } = await $http.get({
                        url:    '/project/member/list',
                        params: {
                            projectId: props.projectId,
                            ootJobId:  props.ootJobId || '',
                        },
                    });

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
                },

                async readData(model) {
                    vData.loading = true;
                    methods.checkDetail(model);
                    await methods.getNodeData();
                    await methods.getNodeDetail(model);
                    vData.loading = false;
                },

                async checkDetail(model) {
                    const params = {
                        jobId:       props.ootJobId,
                        modelNodeId: props.ootModelFlowNodeId,
                    };

                    if(!params.modelNodeId) {
                        params.nodeId = model.id;
                        params.flowId = route.query.flow_id;
                    }

                    const { code, data } = await $http.get({
                        url: '/project/flow/node/check_exist_evaluation_component',
                        params,
                    });

                    if(code === 0) {
                        vData.check_result = data.check_result;
                    }
                },

                listLoaded(list) {
                    vData.rawSearch.allList = list;
                },

                checkJobDetail() {
                    const { href } = router.resolve({
                        name:  'project-job-detail',
                        query: {
                            member_role: 'promoter',
                            project_id:  props.projectId,
                            flow_id:     props.flowId,
                            job_id:      props.ootJobId,
                        },
                    });

                    window.open(href, '_blank');
                },
                // search raw dataset
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

                        ref.searchField.project_id = props.projectId;
                        ref.searchField.member_id = vData.memberId;
                        ref.searchField.member_role = vData.memberRole;
                        ref.searchField.contains_y = vData.rawSearch.contains_y;
                        ref.searchField.data_resource_type = 'TableDataSet';
                        ref.isFlow = true;

                        ref.getDataList({
                            $data_set:       member.$data_set_list,
                            url:             '/project/raw_data_set/list',
                            to:              false,
                            resetPagination: true,
                        });
                    });
                },

                dataSetTabChange(ref) {
                    const params = {
                        to:              false,
                        resetPagination: true,
                    };
                    const refInstances = {
                        raw:     rawDataSetListRef,
                        derived: derivedDataSetListRef,
                    };
                    const refInstance = refInstances[ref.paneName].value;

                    refInstance.searchField.project_id = props.projectId;
                    refInstance.searchField.member_id = vData.memberId;
                    refInstance.searchField.member_role = vData.memberRole;
                    refInstance.searchField.contains_y = vData.rawSearch.contains_y;
                    refInstance.searchField.data_resource_type = 'TableDataSet';
                    refInstance.isFlow = true;

                    switch(ref.paneName) {
                    case 'raw':
                        params.url = '/project/raw_data_set/list';
                        break;
                    case 'derived':
                        params.url = '/project/derived_data_set/query';
                        refInstance.searchField.contains_y = null;
                        break;
                    }

                    nextTick(_ => {
                        refInstance.getDataList(params);
                    });
                },

                /* add to the list*/
                selectDataSet(item) {
                    vData.showSelectDataSet = false;
                    if(item.data_resource.derived_from) {
                        // derived dataset
                        const memberIds = {}; // cache member_id

                        item.members.forEach(member => {
                            if(member.job_role === 'promoter' || member.job_role === 'provider') {
                                const features = member.feature_name_list.split(',');

                                memberIds[member.member_id] = {
                                    member_role:       member.job_role,
                                    member_id:         member.member_id,
                                    member_name:       member.member_name,
                                    feature_count:     member.feature_count,
                                    data_set_id:       item.data_set_id,
                                    derived_from:      item.data_resource.derived_from,
                                    row_count:         item.row_count ? item.row_count : item.data_resource.total_data_count,
                                    name:              item.data_resource.name,
                                    column_name_list:  features,
                                    $column_name_list: features,
                                };
                            }
                        });

                        vData.member_list.forEach(member => {
                            // Remove the selected derived dataset of other members first
                            const dataset_list = member.$data_set_list[0];

                            if(dataset_list) {
                                const { data_set_id } = dataset_list;

                                vData.member_list.forEach(item => {
                                    if(item.$data_set_list[0] && item.$data_set_list[0].data_set_id === data_set_id) {
                                        item.$data_set_list = [];
                                    }
                                });
                            }

                            // Add derived dataset again
                            const data_set = memberIds[member.member_id];

                            if(data_set) {
                                member.$data_set_list = [];
                                member.$data_set_list.push(data_set);
                            }
                        });
                        $notify({ type: 'success', message: '已自动关联相关数据资源', duration: 1000 });
                    } else {
                        const currentMember = vData.member_list[vData.memberIndex];
                        const dataset_list = currentMember.$data_set_list[0];
                        const features = item.data_resource.feature_name_list && item.data_resource.feature_name_list.split(',') ? item.data_resource.feature_name_list.split(',') : [];

                        const dataset = {
                            ...item.data_resource,
                            data_set_id:       item.data_resource.data_resource_id,
                            column_name_list:  features,
                            $column_name_list: features,
                        };

                        if(dataset_list) {
                            // remove last selected
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
                    }
                },

                checkFeatures({ $column_name_list }) {
                    $alert('已选特征:', {
                        title:                    '已选特征:',
                        message:                  `<div style="max-height: 80vh;overflow:auto;">${$column_name_list.join(',')}</div>`,
                        dangerouslyUseHTMLString: true,
                    });
                },

                async ootFeaturePreview($event, memberId) {
                    const { code, data } = await $http.get({
                        url:    '/project/flow/query/data_io_task_features',
                        params: {
                            jobId:  props.ootJobId,
                            flowId: props.flowId,
                            memberId,
                        },
                    });

                    if(code === 0) {
                        const list = data.data_io_task_feature_info_list;

                        $alert('原入模数据资源特征列:', {
                            title:   '原入模数据资源特征列:',
                            message: list && list.length ? `<div style="max-height: 80vh;overflow:auto;">
                            <p>数据资源id: <span class="p-id">${list && list[0] ? list[0].data_set_id : ''}</span></p>
                            特征列: ${list && list[0] ? list[0].features.join(',') : ''}
                            </div>`: '无',
                            dangerouslyUseHTMLString: true,
                        });
                    }
                },

                checkParams() {
                    const { binValue, exitVertComponent, need_psi } = vData;
                    const { method, binNumber, split_points } = binValue;
                    const isCustom = method === 'custom';
                    const array = replace(split_points).replace(/，/g,',').replace(/,$/, '').split(',');

                    if(isCustom && !psiCustomSplit(array)){
                        return false;
                    }
                    const dataset_list = [];
                    const re = array.map(parseFloat);

                    re.sort(((a,b) => a-b));
                    const params = {
                        job_id:          props.ootJobId,
                        modelFlowNodeId: props.ootModelFlowNodeId,
                        psi_param:       {
                            need_psi,
                            bin_method:   exitVertComponent ? method : undefined,
                            bin_num:      exitVertComponent && !isCustom ? binNumber : undefined,
                            split_points: exitVertComponent && isCustom ?  [...new Set([0, ...re ,1])] : undefined,
                        },
                    };

                    vData.member_list.forEach((member, index) => {
                        const row = member.$data_set_list;

                        if(row.length) {
                            dataset_list.push({
                                member_id:         member.member_id,
                                member_role:       member.member_role,
                                data_set_id:       row[0].data_set_id,
                                features:          row[0].$column_name_list,
                                feature_name_list: row[0].feature_name_list,
                                feature_count:     row[0].feature_count,
                                contains_y:        row[0].contains_y,
                                derived_from:      row[0].derived_from,
                                row_count:         row[0].row_count || row[0].total_data_count,
                                name:              row[0].name,
                            });
                        }
                    });

                    params.dataset_list = dataset_list;

                    if(!vData.check_result) {
                        params.eval_type = vData.form.eval_type;
                        params.pos_label = vData.form.pos_label;
                    }

                    return {
                        params,
                    };
                },
            };

            watch(
                () => vData.showSelectDataSet,
                (newVal) => {
                    if (!newVal) {
                        vData.rawSearch.contains_y = '';
                    }
                },
            );

            return {
                vData,
                userInfo,
                methods,
                derivedRef,
                derivedDataSetListRef,
                rawDataSetListRef,
            };
        },
    };
</script>

<style lang="scss" scoped>
    .line-through{text-decoration: line-through;}
    .member-info{line-height:30px;}
    .role-title{
        font-weight: bold;
        font-size: 16px;
    }
    .dialog-min-width{min-width: 800px;}
    .board-icon-circle-close{
        cursor: pointer;
        color:$--color-danger;
        position: absolute;
        right:-30px;
        top: 2px;
    }
    .data-set{
        border-top: 1px solid $border-color-base;
        .board-form{
            padding: 5px 10px;
            border: 1px solid $border-color-base;
            border-top: 0;
        }
        :deep(.board-form-item){
            display:flex;
            margin-bottom: 0;
            flex-wrap: wrap;
            .board-form-item__label{
                font-size: 12px;
                text-align: left;
                margin-bottom: 0;
                line-height: 24px;
            }
            .board-form-item__content{
                font-size: 12px;
                line-height: 22px;
                word-break:break-all;
            }
        }
    }
    .check-features{
        padding:0 10px;
        min-height: 24px;
        margin-left: 5px;
    }
</style>
