<template>
    <el-form
        ref="form"
        v-loading="vData.loading"
        label-position="top"
        :disabled="disabled"
    >
        <el-form-item label="标准化方法：">
            <el-select
                v-model="vData.columnListType"
                placeholder="请选择标准化方法"
                style="width:110px;"
            >
                <el-option
                    v-for="option in vData.methodList"
                    :key="option.value"
                    :label="option.label"
                    :value="option.value"
                />
            </el-select>
            <el-button
                size="mini"
                class="ml10"
                style="margin-top:2px;"
                :disabled="vData.total_column_count === 0"
                @click="methods.showColumnListDialog"
            >
                选择特征（{{ vData.feature_column_count }}/{{ vData.total_column_count }}）
            </el-button>
        </el-form-item>

        <el-tabs
            v-if="vData.lastSelection.length"
            type="card"
            class="mt20"
        >
            <el-tab-pane
                v-for="(item, index) in vData.lastSelection"
                :key="`${item.member_id}-${item.member_role}`"
                :label="`${item.member_name} (${item.member_role === 'promoter' ? '发起方': '协作方'})`"
                :name="`${index}`"
                style="max-height:500px;overflow:auto;"
            >
                {{ item.$feature_list.length ? item.$feature_list.map(item => item.name).join(', ') : '尚未选择特征' }}
            </el-tab-pane>
        </el-tabs>

        <CheckFeatureDialog
            ref="CheckFeatureDialogRef"
            :feature-select-tab="vData.featureSelectTab"
            :column-list-type="vData.columnListType"
            revert-check-emit="revertCheck"
            @confirmCheck="methods.confirmCheck"
            @revertCheck="methods.revertCheck"
        />
    </el-form>
</template>

<script>
    import { ref, reactive, getCurrentInstance, nextTick } from 'vue';
    import CheckFeatureDialog from '../common/checkFeatureDialog';

    export default {
        name:       'FeatureStandardized',
        components: {
            CheckFeatureDialog,
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
        setup (props, context) {
            const { appContext } = getCurrentInstance();
            const { $http, $message } = appContext.config.globalProperties;
            const CheckFeatureDialogRef= ref();
            const vData = reactive({
                loading:              false,
                feature_column_count: 0,
                total_column_count:   0,
                methodList:           [
                    { value: 'z-core', label: 'z-core' },
                    { value: 'min-max', label: 'min-max' },
                ],
                columnListType:   'z-core',
                featureSelectTab: [],
                lastSelection:    [],
            });
            const methods = {
                async readData (model) {
                    if (vData.loading) return;
                    vData.loading = true;

                    const { code, data } = await $http.post({
                        url:  '/flow/job/task/feature',
                        data: {
                            job_id:       props.jobId,
                            flow_id:      props.flowId,
                            flow_node_id: model.id,
                        },
                    });

                    nextTick(_ => {
                        vData.loading = false;
                        if (code === 0) {
                            vData.total_column_count = 0;
                            if (data.members && data.members.length) {
                                data.members.forEach(member => {
                                    const $feature_list = [];

                                    member.features.forEach(feature => {
                                        $feature_list.push({
                                            name:   feature.name,
                                            method: '',
                                        });
                                        vData.total_column_count++;
                                    });
                                    vData.featureSelectTab.push({
                                        member_id:          member.member_id,
                                        member_name:        member.member_name,
                                        member_role:        member.member_role,
                                        $checkedAll:        false,
                                        $indeterminate:     false,
                                        $checkedColumnsArr: [],
                                        $checkedColumns:    '',
                                        $feature_list,
                                    });
                                });
                                methods.getNodeDetail(model);
                            }
                        }
                    });
                },
                async getNodeDetail (model) {
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
                            if(data.params && data.params.members) {
                                const { method, members } = data.params;

                                vData.columnListType = method;
                                vData.featureSelectTab = members;
                                vData.feature_column_count = 0;
                                vData.total_column_count = 0;

                                vData.featureSelectTab = members.map(member => {
                                    vData.feature_column_count += member.$checkedColumnsArr.length;
                                    vData.total_column_count += member.features.length;

                                    const $feature_list = member.$checkedColumnsArr.map(item => {
                                        return {
                                            name: item,
                                            method,
                                        };
                                    });

                                    vData.lastSelection.push({
                                        member_id:   member.member_id,
                                        member_name: member.member_name,
                                        member_role: member.member_role,
                                        $feature_list,
                                    });

                                    return {
                                        ...member,
                                        $checkedColumns: '',
                                        $feature_list:   member.features,
                                    };
                                });
                            }
                        }
                    });
                },
                showColumnListDialog() {
                    vData.featureSelectTab.forEach(row => {
                        const member = vData.lastSelection.find(item => item.member_id === row.member_id && item.member_role === row.member_role);

                        if(member) {
                            row.$checkedColumnsArr = member.$feature_list.map(x => x.name);
                        }
                    });
                    CheckFeatureDialogRef.value.methods.show();
                },
                revertCheck(item) {
                    const lastIds = [...item.$checkedColumnsArr];

                    // remove last selected
                    for (let i = 0; i < lastIds.length; i++) {
                        const name = lastIds[i];
                        const column = item.$feature_list.find(x => name === x.name);
                        const index = item.$checkedColumnsArr.findIndex(x => x === column.name);

                        item.$checkedColumnsArr.splice(index, 1);
                    }

                    // add selected result
                    item.$feature_list.forEach(column => {
                        const name = lastIds.find(x => x === column.name);

                        if (!name) {
                            item.$checkedColumnsArr.push(column.name);
                        } else {
                            column.method = '';
                        }
                    });
                },
                confirmCheck(list) {
                    let count = 0;
                    const lastList = [];

                    list.forEach(member => {
                        const {
                            $checkedColumnsArr,
                            $feature_list,
                        } = member;
                        const features = [];

                        $checkedColumnsArr.forEach(feature => {
                            const item = $feature_list.find(item => item.name === feature);

                            if(item) {
                                // auto selected
                                item.method = vData.columnListType;
                                features.push(item);
                            }
                        });

                        if(features.length) {
                            count += features.length;
                            lastList.push({
                                member_id:     member.member_id,
                                member_name:   member.member_name,
                                member_role:   member.member_role,
                                $feature_list: features,
                            });
                        }
                    });

                    vData.lastSelection = lastList;
                    vData.feature_column_count = count;
                },
                checkParams() {
                    if(vData.lastSelection.length === 0) {
                        return $message.error('请先选择特征!');
                    }

                    const members = vData.featureSelectTab.map(member => {
                        return {
                            $checkedColumnsArr: member.$checkedColumnsArr,
                            features:           member.$feature_list,
                            $checkedAll:        member.$checkedAll,
                            $indeterminate:     member.$indeterminate,
                            member_id:          member.member_id,
                            member_name:        member.member_name,
                            member_role:        member.member_role,
                        };
                    });

                    return {
                        params: {
                            method: vData.columnListType,
                            members,
                        },
                    };
                },
            };

            return {
                vData,
                methods,
                CheckFeatureDialogRef,
            };
        },
    };
</script>
