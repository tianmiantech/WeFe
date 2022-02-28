<template>
    <el-form
        ref="form"
        v-loading="vData.loading"
        :disabled="disabled"
        class="flex-form"
    >
        <el-form-item
            label="是否联合计算相关性系数："
            label-width="130px"
        >
            <el-radio-group v-model="vData.cross_parties">
                <el-radio :label="true">
                    是
                </el-radio>
                <el-radio :label="false">
                    否
                </el-radio>
            </el-radio-group>
        </el-form-item>

        <el-form-item>
            <el-button
                size="small"
                :disabled="vData.total_column_count === 0"
                @click="methods.showColumnListDialog"
            >
                选择特征（{{ vData.feature_column_count }}/{{ vData.total_column_count }}）
            </el-button>
        </el-form-item>

        <el-tabs
            v-if="vData.lastSelection.length"
            type="card"
        >
            <el-tab-pane
                v-for="(item, index) in vData.lastSelection"
                :key="`${item.member_id}-${item.member_role}`"
                :label="`${item.member_name} (${item.member_role === 'promoter' ? '发起方': '协作方'})`"
                :name="`${index}`"
                style="max-height:500px;overflow:auto;"
            >
                {{ item.$feature_list.map(item => item.name).join(', ') }}
            </el-tab-pane>
        </el-tabs>

        <CheckFeatureDialog
            ref="CheckFeatureDialogRef"
            :feature-select-tab="vData.featureSelectTab"
            :column-list-type="'cross_parties'"
            revert-check-emit="revertCheck"
            @confirmCheck="methods.confirmCheck"
            @revertCheck="methods.revertCheck"
        />
    </el-form>
</template>

<script>
    import {
        ref,
        reactive,
        getCurrentInstance,
        nextTick,
    } from 'vue';
    import checkFeatureMixin from '../common/checkFeature';
    import CheckFeatureDialog from '../common/checkFeatureDialog';

    export default {
        name:       'VertPearson',
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
        emits: [...checkFeatureMixin().emits],
        setup(props, context) {
            const CheckFeatureDialogRef = ref();
            const { appContext } = getCurrentInstance();
            const {
                $http,
                $message,
            } = appContext.config.globalProperties;

            let vData = reactive({
                feature_column_count: 0,
                total_column_count:   0,
                cross_parties:        true,
                featureSelectTab:     [],
                lastSelection:        [],
            });

            let methods = {
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
                        if (code === 0 && data && data.params && Object.keys(data.params).length) {
                            const {
                                members,
                                cross_parties,
                            } = data.params;

                            vData.cross_parties = cross_parties;
                            vData.feature_column_count = 0;
                            vData.total_column_count = 0;

                            vData.lastSelection = [];
                            vData.featureSelectTab = members.map(member => {
                                vData.feature_column_count += member.$checkedColumnsArr.length;
                                vData.total_column_count += member.features.length;

                                const $feature_list = member.$checkedColumnsArr.map(item => {
                                    return {
                                        name: item,
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
                    });
                },

                revertCheck(item) {
                    const lastIds = [...item.$checkedColumnsArr];

                    // Remove last selected result
                    for (let i = 0; i < lastIds.length; i++) {
                        const name = lastIds[i];
                        const column = item.$feature_list.find(x => name === x.name);
                        const index = item.$checkedColumnsArr.findIndex(x => x === column.name);

                        item.$checkedColumnsArr.splice(index, 1);
                    }

                    // Add selected results
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
                                item.method = 'cross_parties';
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
                        $message.error('请先选择特征!');
                        return false;
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
                            members,
                            cross_parties: vData.cross_parties,
                        },
                    };
                },
            };

            const { $data, $methods } = checkFeatureMixin().mixin({
                vData,
                props,
                context,
                methods,
                CheckFeatureDialogRef,
            });

            vData = $data;
            methods = $methods;

            return {
                vData,
                methods,
                CheckFeatureDialogRef,
            };
        },
    };
</script>
