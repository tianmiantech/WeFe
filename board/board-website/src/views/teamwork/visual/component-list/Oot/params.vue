<template>
    <el-form
        v-loading="vData.loading"
        :disabled="disabled"
        inline
    >
        <span
            v-if="ootModelFlowNodeId"
            style="margin-top:-10px;"
            class="el-button el-button--text el-button--small mb10"
            @click="methods.checkJobDetail"
        >
            查看前置流程详情
            <el-icon>
                <elicon-top-right />
            </el-icon>
        </span>

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

        <h4>选择验证数据集:</h4>
        <p class="f12 mt5 mb15">tips: 数据集需包含原流程数据集中的所有列</p>
        <div
            v-for="(member, index) in vData.member_list"
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
                class="role-title pt20 pb5"
            >
                协作方:
            </h3>
            <p class="member-info">
                <span class="name f16">
                    <el-icon
                        v-if="member.audit_status !== 'agree'"
                        class="el-icon-warning-outline color-danger"
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
                    选择数据集
                </el-button>
                <a
                    class="el-link el-link--info f12"
                    @click="methods.ootFeaturePreview($event, member.member_id)"
                >预览原入模特征列</a>
            </p>

            <div
                v-if="member.audit_status === 'agree'"
                class="data-set f14"
            >
                <el-form
                    v-for="row in member.$data_set_list"
                    :key="row.id"
                    label-width="100px"
                >
                    <el-form-item label="数据集名称：">
                        {{ row.name }}
                        <el-tag
                            v-if="row.contains_y"
                            type="success"
                        >
                            y
                        </el-tag>
                        <el-icon
                            v-if="!disabled"
                            title="移除"
                            class="el-icon-circle-close f20 ml10"
                            @click="methods.removeDataSet(index)"
                        >
                            <elicon-circle-close />
                        </el-icon>
                    </el-form-item>
                    <el-form-item label="数据集id：">
                        {{ row.data_set_id }}
                    </el-form-item>
                    <el-form-item label="数据量/特征量：">
                        {{ row.row_count }} / {{ row.feature_count }}
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

        <!-- Select the dataset for the specified member -->
        <el-dialog
            title="选择数据集"
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
                    label="原始数据集"
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
                    label="衍生数据集"
                    name="derived"
                >
                    <el-alert
                        class="mb10"
                        effect="dark"
                        type="success"
                        :closable="false"
                        title="使用衍生数据集将 自动替换 关联成员已选的数据集"
                    />
                    <el-form inline>
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

    export default {
        name:       'Oot',
        components: {
            DataSetList,
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
                form: {
                    eval_type: 'binary',
                    pos_label: 1,
                },
                oot_job_id: '',
            });

            const methods = {
                async getNodeDetail(model) {
                    const { code, data } = await $http.get({
                        url:    '/project/flow/node/detail',
                        params: {
                            nodeId:  model.id,
                            flow_id: props.flowId,
                        },
                    });

                    if (code === 0 && data && data.params) {
                        const { dataset_list, eval_type, pos_label } = data.params;

                        for(const memberIndex in vData.member_list) {
                            const member = vData.member_list[memberIndex];
                            const datasetIndex = dataset_list.findIndex(item => member.member_id === item.member_id && member.member_role === item.member_role && !item.deleted);

                            if(~datasetIndex) {
                                const item = dataset_list[datasetIndex];
                                const column_name_list = item.source_type ? item.features : item.feature_name_list.split(',');

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
                    }
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

                        ref.getDataList({
                            url:             '/project/raw_data_set/list',
                            to:              false,
                            resetPagination: true,
                        });
                        ref.isFlow = true;
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
                    if(item.source_type) {
                        // derived data set
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
                                    source_type:       item.source_type,
                                    row_count:         item.row_count,
                                    name:              item.name,
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
                        $notify({ type: 'success', message: '已自动关联相关数据集', duration: 1000 });
                    } else {
                        const currentMember = vData.member_list[vData.memberIndex];
                        const dataset_list = currentMember.$data_set_list[0];
                        const features = item.feature_name_list.split(',');
                        const dataset = {
                            ...item,
                            column_name_list:  features,
                            $column_name_list: features,
                        };

                        if(dataset_list) {
                            // remove old selection
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

                        $alert('原入模数据集特征列:', {
                            title:   '原入模数据集特征列:',
                            message: `<div style="max-height: 80vh;overflow:auto;">
                            <p>数据集id: <span class="p-id">${list && list[0] ? list[0].data_set_id : ''}</span></p>
                            特征列: ${list && list[0] ? list[0].features.join(',') : ''}
                            </div>`,
                            dangerouslyUseHTMLString: true,
                        });
                    }
                },

                checkParams() {
                    const dataset_list = [];
                    const params = {
                        job_id:          props.ootJobId,
                        modelFlowNodeId: props.ootModelFlowNodeId,
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
                                source_type:       row[0].source_type,
                                row_count:         row[0].row_count,
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
    .el-icon-circle-close{
        cursor: pointer;
        color:$--color-danger;
    }
    .data-set{
        border-top: 1px solid $border-color-base;
        .el-form{
            border: 1px solid $border-color-base;
            border-top: 0;
            padding: 5px 10px;
        }
        :deep(.el-form-item){
            display:flex;
            margin-bottom: 0;
            flex-wrap: wrap;
            .el-form-item__label{
                font-size: 12px;
                text-align: left;
                margin-bottom: 0;
            }
            .el-form-item__content{word-break:break-all;}
        }
    }
    .check-features{
        padding:0 10px;
        min-height: 24px;
        margin-left: 5px;
    }
</style>
