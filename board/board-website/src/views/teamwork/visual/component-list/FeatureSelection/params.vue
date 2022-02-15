<template>
    <div
        v-loading="vData.loading"
        class="form"
    >
        <el-form
            :disabled="disabled"
            @submit.prevent
        >
            <el-alert
                v-if="vData.hasError"
                :title="vData.hasError"
                type="error"
                show-icon
            />
            <el-form-item
                v-for="(item, index) in vData.selectList"
                :key="item.id"
                :label="`第${index + 1}次筛选:`"
            >
                <el-select
                    v-model="item.select_type"
                    placeholder="请选择筛选方式"
                    style="width: 120px;"
                    :disabled="vData.selectList.length > index + 1"
                    @change="methods.changeMethod(item)"
                >
                    <el-option
                        v-for="option in vData.methodList"
                        :key="option.value"
                        :label="option.label"
                        :value="option.value"
                        :disabled="vData.methodObj[option.value].disabled"
                    />
                </el-select>
                <el-button
                    size="small"
                    class="ml10"
                    style="margin-top:2px;"
                    :disabled="item.feature_count === 0 || vData.selectList.length > index + 1"
                    @click="methods.showDialog(item, index)"
                >
                    选择特征（{{ item.select_count }}/{{ item.feature_count }}）
                </el-button>
                <el-button
                    v-if="vData.selectList.length > 1"
                    type="text"
                    icon="elicon-delete"
                    class="color-danger"
                    @click="methods.removeRow(item, index)"
                />
                <div class="mt10">
                    <el-button
                        v-if="item.select_count > 0 && item.feature_count > 1 && vData.selectList.length <= index + 1"
                        size="small"
                        @click="methods.addPolicy"
                    >
                        + 继续筛选
                    </el-button>
                </div>
            </el-form-item>
        </el-form>

        <el-tabs
            v-if="vData.lastList.length"
            type="card"
            class="mt20"
        >
            <el-tab-pane
                v-for="(item, index) in vData.lastList"
                :key="`${item.member_id}-${item.member_role}`"
                :label="`${item.member_name} (${item.member_role === 'provider' ? '协作方' : '发起方'}) /${item.features.length}`"
                :name="`${index}`"
            >
                <el-table
                    v-if="index === 0"
                    :data="item.features"
                    style="max-width:600px;"
                    stripe
                >
                    <el-table-column prop="name" label="特征名称"></el-table-column>
                    <el-table-column label="数据类型">
                        <template v-slot="scope">
                            {{ vData.dataSetObj[scope.row.name] ? vData.dataSetObj[scope.row.name].data_type : '' }}
                        </template>
                    </el-table-column>
                    <el-table-column label="注释">
                        <template v-slot="scope">
                            {{ vData.dataSetObj[scope.row.name] ? vData.dataSetObj[scope.row.name].comment : '' }}
                        </template>
                    </el-table-column>
                </el-table>
                <template v-else>
                    {{ item.features.map(item => item.name).join(', ') }}
                </template>
            </el-tab-pane>
        </el-tabs>

        <MissingRateDialog
            ref="MissingRateDialogRef"
            :job-id="jobId"
            :flow-id="flowId"
            :flow-node-id="currentObj.nodeId"
            @confirmCheck="methods.confirmCheck"
        />
        <CVIVDialog
            ref="CVIVDialogRef"
            :job-id="jobId"
            :flow-id="flowId"
            :flow-node-id="currentObj.nodeId"
            @confirmCheck="methods.confirmCheck"
        />

        <CheckFeatureDialog
            ref="CheckFeatureDialogRef"
            :feature-select-tab="vData.manualLastList"
            :select-list-id="vData.selectList[vData.selectListIndex] ? vData.selectList[vData.selectListIndex].id : 0"
            :column-list-type="vData.columnListType"
            @confirmCheck="methods.manualConfirmCheck"
        />
    </div>
</template>

<script>
    import {
        ref,
        reactive,
        getCurrentInstance,
        nextTick,
    } from 'vue';
    import CheckFeatureDialog from '../common/checkFeatureDialog';
    import MissingRateDialog from './miss-rate';
    import CVIVDialog from './cv-iv';

    export default {
        name:       'FeatureSelection',
        components: {
            CheckFeatureDialog,
            MissingRateDialog,
            CVIVDialog,
        },
        props: {
            projectId:    String,
            flowId:       String,
            disabled:     Boolean,
            learningType: String,
            currentObj:   Object,
            jobId:        String,
            class:        String,
        },
        setup(props, context) {
            const CVIVDialogRef = ref();
            const CheckFeatureDialogRef = ref();
            const MissingRateDialogRef = ref();
            const { appContext } = getCurrentInstance();
            const {
                $http,
                $message,
            } = appContext.config.globalProperties;
            const vData = reactive({
                hasError:                '',
                select_all_count:        0, // selected number
                total_column_count:      0, // all features count
                inited:                  false,
                loading:                 false,
                has_feature_statistic:   false,
                has_feature_calculation: false,
                selectList:              [{
                    id:            Math.round(Math.random()*10e12),
                    select_type:   'miss_rate',
                    feature_count: 0,
                    select_count:  0,
                    list:          [],
                }],
                methodList: [
                    { value: 'miss_rate', label: '缺失率' },
                    { value: 'cv_iv', label: 'CV/IV 过滤' },
                    { value: 'manual', label: '手动勾选' },
                ],
                methodObj: {
                    miss_rate: {
                        name:     '缺失率',
                        disabled: false,
                        count:    0,
                    },
                    cv_iv: {
                        name:     'CV/IV 过滤',
                        disabled: false,
                        count:    0,
                    },
                    manual: {
                        name:     '手动勾选',
                        disabled: false,
                        count:    0,
                    },
                },
                columnListType:   'miss_rate',
                selectListIndex:  0,
                featureSelectTab: [],
                lastList:         [],
                list:             [],
                manualLastList:   [],
                dataSetObj:       {},
            });
            const methods = {
                async readData (model) {
                    if(vData.loading) return;
                    vData.loading = true;

                    const { code, data }  = await $http.post({
                        url:  '/flow/job/task/feature',
                        data: {
                            job_id:       props.jobId,
                            flow_id:      props.flowId,
                            flow_node_id: model.id,
                        },
                    });

                    nextTick(async _ => {
                        vData.loading = false;
                        if(code === 0) {
                            vData.select_all_count = 0;
                            vData.total_column_count = 0;
                            // flatten a 2d array
                            vData.featureSelectTab = [];

                            const { data_set_id } = data.members[0];
                            const response = await $http.get({
                                url:    '/table_data_set/column/list',
                                params: {
                                    data_set_id,
                                },
                            });

                            if(response.code === 0) {
                                const { list } = response.data;

                                if(list.length) {
                                    list.forEach(item => {
                                        vData.dataSetObj[item.name] = item;
                                    });
                                }
                            }

                            data.members.forEach(member => {
                                member.features.forEach(feature => {
                                    vData.select_all_count += 1;
                                    vData.total_column_count += 1;
                                    vData.featureSelectTab.push({
                                        member_id:   member.member_id,
                                        member_name: member.member_name,
                                        member_role: member.member_role,
                                        ...feature,
                                    });
                                });
                            });

                            const {
                                has_feature_calculation,
                                has_feature_statistic,
                            } = data;

                            vData.methodObj['cv_iv'].disabled = !has_feature_calculation;
                            vData.methodObj['miss_rate'].disabled = !has_feature_statistic;

                            for (const key in vData.methodObj) {
                                const item = vData.methodObj[key];

                                if(!item.disabled) {
                                    vData.selectList[0].select_type = key;
                                    break;
                                }
                            }
                            vData.selectList[0].feature_count = vData.total_column_count;
                            methods.getNodeDetail(model, data);
                        }
                    });
                },

                async getNodeDetail (model, {
                    has_feature_calculation,
                    has_feature_statistic,
                }) {
                    if (vData.loading) return;
                    vData.loading = true;

                    const { code, data } = await $http.get({
                        url:    '/project/flow/node/detail',
                        params: {
                            nodeId:  model.id,
                            flow_id: props.flowId,
                        },
                    });

                    nextTick(_ => {
                        vData.loading = false;
                        if (code === 0) {
                            const { params } = data || {};

                            vData.hasError = '';
                            if (params.members) {
                                const {
                                    strategies,
                                    members,
                                } = params;

                                vData.select_all_count = 0;
                                if(strategies && strategies.length) {
                                    const types = [];

                                    vData.selectList = strategies;
                                    strategies.forEach((row, index) => {
                                        if(!has_feature_calculation && row.select_type === 'cv_iv') {
                                            types.push('[CV/IV]');
                                        }
                                        if(!has_feature_statistic && row.select_type === 'miss_rate') {
                                            types.push('[缺失率]');
                                        }
                                        vData.select_all_count += row.select_count;
                                    });
                                    if(types.length) {
                                        vData.hasError = `发生错误! 筛选策略中无法处理 ${types.join(' ')}, 请重新调整策略!`;
                                    }
                                }
                                vData.lastList = members || [];
                            } else {
                                vData.selectList[0].list = vData.featureSelectTab;
                            }
                            vData.inited = true;
                        }
                    });
                },

                addPolicy () {
                    const last = vData.selectList[vData.selectList.length - 1];

                    if(last.select_count === 0) {
                        return $message.error('请先选择特征 再添加新的策略');
                    }

                    let count = 0; // selected number

                    vData.selectList.forEach(item => {
                        count = item.select_count;
                    });

                    for(const key in vData.methodObj) {
                        const value = vData.methodObj[key];

                        if(!value.disabled) {
                            const list = [];

                            vData.lastList.forEach(member => {
                                member.features.forEach(feature => {
                                    list.push({
                                        member_id:   member.member_id,
                                        member_name: member.member_name,
                                        member_role: member.member_role,
                                        ...feature,
                                        id:          '',
                                    });
                                });
                            });

                            vData.selectList.push({
                                id:            Math.round(Math.random() * 10e12),
                                feature_count: count || 0,
                                select_type:   key,
                                select_count:  0,
                                count:         1,
                                list,
                            });
                            value.count++;
                            break;
                        }
                    }
                },

                removeRow ({ select_type }, index) {
                    vData.lastList = [];
                    if(vData.selectList.length === 1) {
                        vData.selectList[0].list.forEach(row => {
                            row.id = '';
                        });
                    } else {
                        vData.selectList.splice(index, 1);
                    }
                    // recount last selected
                    vData.select_all_count = 0;
                    vData.selectList.forEach(row => {
                        vData.select_all_count += row.select_count;
                    });

                    /* restore last selected */
                    const members = {};

                    vData.selectList[vData.selectList.length - 1].list.forEach(row => {
                        const { member_id, member_name, member_role } = row;

                        if(!members[member_id]) {
                            members[member_id] = {
                                member_id,
                                member_name,
                                member_role,
                                features: [],
                            };
                        }
                        if(row.id) {
                            members[member_id].features.push(row);
                        }
                    });

                    for(const key in members) {
                        if(members[key].features.length) {
                            vData.lastList.push(members[key]);
                        }
                    }
                },

                transformToMembers(array) {
                    const list = [];
                    const members = {};

                    array.forEach(feature => {
                        if(!members[feature.member_id]) {
                            members[feature.member_id] = {
                                member_name: feature.member_name,
                                member_role: feature.member_role,
                                member_id:   feature.member_id,
                                features:    [], // all features
                            };
                        }
                        members[feature.member_id].features.push({
                            miss_rate: feature.miss_rate,
                            name:      feature.name,
                            iv:        feature.iv,
                            cv:        feature.cv,
                            id:        feature.id,
                        });
                    });

                    for (const key in members) {
                        const val = members[key];

                        list.push(val);
                    }
                    return list;
                },

                changeMethod(selected) {
                    // clear last selected result
                    for(let j = 0, { length } = vData.lastList; j < length; j++) {
                        const member = vData.lastList[j];

                        if(member) {
                            const { features } = member;

                            for(let k = 0, len = features.length; k < len; k++) {
                                const value = features[k];

                                if(value && value.id === selected.id) {
                                    features.splice(k, 1);
                                    k--;
                                }
                            }

                            if(features && features.length === 0) {
                                vData.lastList.splice(j, 1);
                                j--;
                            }
                        }
                    }
                    // clear the current selection
                    selected.select_count = 0;
                    selected.list.forEach(row => row.id = '');
                },

                showDialog({ select_type, list }, index) {
                    const params = {
                        features: list,
                    };

                    vData.selectListIndex = index;
                    switch(select_type) {
                    case 'miss_rate':
                        MissingRateDialogRef.value.methods.show(params);
                        break;
                    case 'cv_iv':
                        CVIVDialogRef.value.methods.show(params);
                        break;
                    case 'manual':
                        {
                            vData.manualLastList = [];
                            methods.transformToMembers(list).forEach((member, index) => {
                                const features = [];

                                list.forEach(item => {
                                    if(item.member_id === member.member_id && item.id) {
                                        features.push(item.name);
                                    }
                                });

                                vData.manualLastList[index] = {
                                    ...member,
                                    $checkedAll:        features.length === member.features.length,
                                    $feature_list:      member.features,
                                    $checkedColumnsArr: features,
                                    $checkedColumns:    '',
                                };
                            });
                            console.log(vData.manualLastList);
                            nextTick(_ => {
                                CheckFeatureDialogRef.value.methods.show();
                            });
                        }
                        break;
                    }
                },

                confirmCheck({ list }) {
                    let count = 0;
                    const selected = vData.selectList[vData.selectListIndex];
                    const _list = methods.transformToMembers(list);

                    // reset id
                    selected.list.forEach(row => {
                        const item = list.find(feature => {
                            if(row.member_id === feature.member_id && feature.name === row.name) {
                                return feature;
                            }
                        });

                        row.id = '';

                        if(item) {
                            row.id = item.id;
                        }
                    });

                    _list.forEach(row => {
                        count += row.features.length;
                    });

                    selected.select_count = count;
                    vData.selectList.forEach(row => {
                        vData.select_all_count += row.select_count;
                    });
                    vData.lastList = _list;
                },

                manualConfirmCheck(list) {
                    if(list.length) {
                        // selected
                        let count = 0;
                        const lastList = [];
                        const selected = vData.selectList[vData.selectListIndex];

                        selected.list.forEach(feature => {
                            const { $checkedColumnsArr } = list.find(row => row.member_id === feature.member_id);

                            // reset id
                            feature.id = '';
                            $checkedColumnsArr.forEach(item => {
                                if(item === feature.name) {
                                    feature.id = selected.id;
                                }
                            });
                        });

                        list.forEach(member => {
                            const {
                                $checkedColumnsArr,
                                $feature_list,
                            } = member;
                            const features = [];

                            $checkedColumnsArr.forEach(feature => {
                                const item = $feature_list.find(item => item.name === feature);

                                if(item) {
                                    features.push(item);
                                }
                            });

                            if(features.length) {
                                count += features.length;
                                lastList.push({
                                    member_id:   member.member_id,
                                    member_name: member.member_name,
                                    member_role: member.member_role,
                                    features,
                                });
                            }
                        });

                        vData.lastList = lastList;
                        vData.select_all_count = 0;
                        selected.select_count = count;
                        vData.selectList.forEach(row => {
                            vData.select_all_count += row.select_count;
                        });
                    } else {
                        // not selected
                        vData.lastList = [];
                    }
                },

                checkParams () {
                    const strategies = [];

                    vData.selectList.forEach(row => {
                        if (row.feature_count) {
                            strategies.push(JSON.parse(JSON.stringify(row)));
                        }
                    });

                    return {
                        params: {
                            strategies,
                            members: vData.lastList,
                        },
                    };
                },
            };

            return {
                vData,
                methods,
                MissingRateDialogRef,
                CheckFeatureDialogRef,
                CVIVDialogRef,
            };
        },
    };
</script>
