<template>
    <el-form
        v-loading="vData.loading"
        :disabled="disabled"
        @submit.prevent
    >
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
                    class="ml10"
                    @click="methods.checkDataSet(member, index)"
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
                    label-width="110px"
                >
                    <el-form-item label="数据资源名称：">
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
                    <el-form-item label="数据资源id：">
                        {{ row.data_resource_id }}
                    </el-form-item>
                    <el-form-item label="数据量/特征量：">
                        {{ row.total_data_count }} / {{ row.feature_count }}
                    </el-form-item>
                    <template v-if="row.contains_y">
                        <el-form-item v-if="row.y_positive_sample_count" label="正例样本数量：">
                            {{ row.y_positive_sample_count }}
                        </el-form-item>
                        <el-form-item v-if="row.y_positive_sample_ratio" label="正例样本比例：">
                            {{ (row.y_positive_sample_ratio * 100).toFixed(1) }}%
                        </el-form-item>
                    </template>
                    <el-form-item label="选择特征：">
                        <el-button
                            size="mini"
                            @click="methods.checkColumns(row, index)"
                        >
                            {{ row.$column_name_list.length }} / {{ row.feature_count }}
                        </el-button>
                    </el-form-item>
                    <div class="mt5 mb10">
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

        <el-dialog
            width="70%"
            title="选择特征列"
            v-model="vData.showColumnList"
            :close-on-click-modal="false"
            custom-class="large-width"
            destroy-on-close
            append-to-body
        >
            <el-form
                v-loading="vData.columnListLoading"
                element-loading-text="当前特征列较多需要时间处理, 请耐心等待"
                class="flex-form"
                @submit.prevent
            >
                <el-form-item label="快速选择：">
                    <el-input
                        v-model="vData.checkedColumns"
                        placeholder="输入特征名称, 多个特征名称用,分开"
                        clearable
                    >
                        <template #append>
                            <el-button @click="methods.autoCheck">
                                确定
                            </el-button>
                        </template>
                    </el-input>
                </el-form-item>
                <div class="mb10">
                    <el-checkbox
                        v-model="vData.checkedAll"
                        :indeterminate="vData.indeterminate"
                        @change="methods.checkAll"
                    >
                        全选
                    </el-checkbox>
                    <el-button
                        type="primary"
                        size="mini"
                        class="ml10 revert-check-btn"
                        @click="methods.revertCheck"
                    >
                        反选
                    </el-button>
                    <span class="ml15">({{ vData.checkedColumnsArr.length }} / {{ vData.column_list.length }})</span>
                </div>

                <BetterCheckbox
                    v-if="vData.showColumnList"
                    :list="vData.column_list"
                >
                    <template #checkbox="{ index, list }">
                        <template
                            v-for="i in 5"
                            :key="`${index * 5 + i - 1}`"
                        >
                            <label
                                v-if="list[index * 5 + i - 1]"
                                :for="`label-${index * 5 + i - 1}`"
                                class="el-checkbox el-checkbox--small"
                                @click.prevent.stop="methods.checkboxChange($event, list[index * 5 + i - 1])"
                            >
                                <span :class="['el-checkbox__input', { 'is-checked': vData.checkedColumnsArr.includes(list[index * 5 + i - 1]) }]">
                                    <span class="el-checkbox__inner"></span>
                                    <input :id="`label-${index * 5 + i - 1}`" class="el-checkbox__original" type="checkbox" />
                                </span>
                                <span class="el-checkbox__label">{{ list[index * 5 + i - 1] }}</span>
                            </label>
                        </template>
                    </template>
                </BetterCheckbox>
            </el-form>
            <div class="text-r mt10">
                <el-button @click="vData.showColumnList=false">
                    取消
                </el-button>
                <el-button
                    type="primary"
                    @click="methods.confirmCheck"
                >
                    确定
                </el-button>
            </div>
        </el-dialog>

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
                        :project-type="projectType"
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
                        class="mb10"
                        effect="dark"
                        type="success"
                        :closable="false"
                        title="使用衍生数据资源将 自动替换 关联成员已选的数据资源"
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
                        :project-type="projectType"
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
    import DataSetList from '@comp/views/data-set-list';

    export default {
        name:       'DataIO',
        components: {
            DataSetList,
        },
        props: {
            projectId:    String,
            flowId:       String,
            disabled:     Boolean,
            isCreator:    Boolean,
            learningType: String,
            currentObj:   Object,
            jobId:        String,
            class:        String,
            projectType:  String,
        },
        setup(props, context) {
            const store = useStore();
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
                columnListLoading: false,
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
                        const { dataset_list } = data.params;

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
                        }
                        vData.member_list = [
                            ...vData.promoterList,
                            ...vData.providerList,
                        ];
                    }
                },

                async getNodeData() {
                    const { code, data } = await $http.get({
                        url:    '/project/member/list',
                        params: {
                            projectId: props.projectId,
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
                    await methods.getNodeData();
                    await methods.getNodeDetail(model);
                    vData.loading = false;
                },

                listLoaded(list) {
                    vData.rawSearch.allList = list;
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
                    refInstance.isFlow = true;
                },

                /* add dataset to list */
                selectDataSet(item) {
                    vData.showSelectDataSet = false;
                    if(item.source_type) {
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
                                    source_type:       item.source_type,
                                    row_count:         item.row_count,
                                    name:              item.name,
                                    column_name_list:  features,
                                    $column_name_list: features,
                                };
                            }
                        });

                        vData.member_list.forEach(member => {
                            // remove the selected derived dataset of other members first
                            const dataset_list = member.$data_set_list[0];

                            if(dataset_list) {
                                const { data_set_id } = dataset_list;

                                vData.member_list.forEach(item => {
                                    if(item.$data_set_list[0] && item.$data_set_list[0].data_set_id === data_set_id) {
                                        item.$data_set_list = [];
                                    }
                                });
                            }

                            // add derived dataset again
                            const data_set = memberIds[member.member_id];

                            if(data_set) {
                                member.$data_set_list = [];
                                member.$data_set_list.push(data_set);
                            }
                        });
                        $notify({ type: 'success', message: '已自动关联相关数据资源', duration: 2000 });
                    } else {
                        const currentMember = vData.member_list[vData.memberIndex];
                        const dataset_list = currentMember.$data_set_list[0];
                        const features = item.data_set.feature_name_list.split(',');
                        const dataset = {
                            ...item.data_set,
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

                checkColumns(row, index) {
                    vData.checkedColumns = '';
                    vData.memberIndex = index;
                    vData.checkedAll = false;
                    vData.indeterminate = false;
                    vData.showColumnList = true;
                    vData.checkedColumnsArr = [];
                    vData.column_list = row.column_name_list;
                    if(row.$column_name_list.length) {
                        vData.checkedColumns = row.$column_name_list.join(',');
                        methods.autoCheck();
                    }
                },

                checkFeatures({ $column_name_list }) {
                    $alert('已选特征:', {
                        title:                    '已选特征:',
                        message:                  `<div style="max-height: 80vh;overflow:auto;">${$column_name_list.join(',')}</div>`,
                        dangerouslyUseHTMLString: true,
                    });
                },

                autoCheck() {
                    vData.columnListLoading = true;

                    setTimeout(() => {
                        vData.checkedColumnsArr = [];
                        if(vData.checkedColumns.trim().length) {
                            const checkedColumnsArr = [...vData.checkedColumnsArr];
                            const column_list = [...vData.column_list];

                            vData.checkedColumns.split(/,|，/).forEach(name => {
                                const $index = column_list.findIndex(column => column === name.trim());

                                // check name is exist
                                if(~$index) {
                                    const index = checkedColumnsArr.findIndex(column => column === name.trim());

                                    // check name is not selected
                                    if(index < 0) {
                                        vData.checkedColumnsArr.push(name.trim());
                                    }
                                    checkedColumnsArr.splice(index, 1);
                                    column_list.splice($index, 1);
                                }
                            });
                        }

                        if(vData.checkedColumnsArr.length === vData.column_list.length) {
                            vData.indeterminate = false;
                            vData.checkedAll = true;
                        } else if(vData.checkedAll) {
                            vData.indeterminate = true;
                            vData.checkedAll = false;
                        }
                        setTimeout(() => {
                            vData.columnListLoading = false;
                        });
                    });
                },

                checkboxChange($event, name) {
                    const index = vData.checkedColumnsArr.findIndex(x => x === name);

                    if(~index) {
                        vData.checkedColumnsArr.splice(index, 1);
                    } else {
                        vData.checkedColumnsArr.push(name);
                    }
                },

                checkAll() {
                    vData.indeterminate = false;
                    vData.columnListLoading = true;

                    setTimeout(() => {
                        vData.checkedColumnsArr = [];
                        if(vData.checkedAll) {
                            vData.column_list.forEach(column => {
                                vData.checkedColumnsArr.push(column);
                            });
                        }
                        setTimeout(() => {
                            vData.columnListLoading = false;
                        });
                    }, 300);
                },

                revertCheck() {
                    vData.columnListLoading = true;

                    setTimeout(() => {
                        if(vData.checkedColumnsArr.length === vData.column_list.length) {
                            vData.indeterminate = false;
                            vData.checkedAll = false;
                        }

                        const lastIds = [...vData.checkedColumnsArr];

                        vData.checkedColumnsArr = [];
                        vData.column_list.forEach(column => {
                            if(!lastIds.find(id => column === id)) {
                                vData.checkedColumnsArr.push(column);
                            }
                        });

                        if(vData.checkedColumnsArr.length === vData.column_list.length) {
                            vData.indeterminate = false;
                            vData.checkedAll = true;
                        }
                        setTimeout(() => {
                            vData.columnListLoading = false;
                        });
                    }, 300);
                },

                confirmCheck() {
                    vData.member_list[vData.memberIndex].$data_set_list[0].$column_name_list = [...vData.checkedColumnsArr];
                    vData.checkedColumnsArr = [];
                    vData.showColumnList = false;
                },

                checkParams() {
                    const dataset_list = [];

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

                    return {
                        params: {
                            dataset_list,
                        },
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
    .revert-check-btn{
        vertical-align: middle;
        position: relative;
        top: -3px;
    }
    .dialog-min-width{min-width: 800px;}
    .el-icon-circle-close{
        cursor: pointer;
        color:$--color-danger;
    }
    .data-set{
        border-top: 1px solid $border-color-base;
        .el-form{
            padding: 5px 10px;
            border: 1px solid $border-color-base;
            border-top: 0;
        }
        :deep(.el-form-item){
            display:flex;
            margin-bottom:5px;
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
