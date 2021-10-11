<template>
    <el-form
        v-loading="vData.loading"
        :disabled="disabled"
        label-position="top"
    >
        <template v-for="(member, $index) in vData.data_set_list">
            <el-form-item
                v-if="member.show"
                :key="`${member.member_id}-${member.member_role}`"
                :label="`${member.member_name} (${member.member_role === 'promoter' ? '发起方' : '协作方'}):`"
            >
                <div
                    v-if="member.features.length"
                    class="el-tag-list mb10"
                >
                    <el-tag
                        v-for="(item, index) in member.features"
                        :key="index"
                        :label="item"
                        :value="item"
                    >
                        {{ item }}
                    </el-tag>
                </div>
                <p>
                    <el-button
                        size="mini"
                        @click="methods.checkColumns(member, $index)"
                    >
                        选择特征（{{ member.features.length }}/{{ member.columns }}）
                    </el-button>
                </p>
            </el-form-item>
        </template>
    </el-form>

    <CheckFeatureDialog
        ref="CheckFeatureDialogRef"
        :feature-select-tab="vData.featureSelectTab"
        revert-check-emit="revertCheck"
        @confirmCheck="methods.confirmCheck"
        @revertCheck="methods.revertCheck"
    />
</template>

<script>
    import {
        ref,
        reactive,
        nextTick,
        getCurrentInstance,
    } from 'vue';
    import checkFeatureMixin from '../common/checkFeature';
    import CheckFeatureDialog from '../common/checkFeatureDialog';

    export default {
        name:       'HorzOneHot',
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
                cross_parties:    true,
                featureSelectTab: [],
            });

            let methods = {
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

                    nextTick(_ => {
                        vData.loading = false;
                        if(code === 0) {
                            vData.data_set_list = [];
                            data.members.forEach(member => {
                                const $features = member.features.map(feature => feature.name);

                                vData.data_set_list.push({
                                    member_id:   member.member_id,
                                    member_role: member.member_role,
                                    member_name: member.member_name,
                                    columns:     member.features.length,
                                    show:        true,
                                    features:    [],
                                    $features,
                                });
                            });
                            methods.getNodeDetail(model);
                        }
                    });
                },

                async getNodeDetail(model) {
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
                            const { params } = data;

                            if(params) {
                                const { featureMethods, members, workMode, form } = params;

                                vData.form = form;
                                vData.percentages = [];
                                vData.typeChecked = [];

                                featureMethods.forEach(row => {
                                    if(row.name !== 'percentile') {
                                        vData.typeChecked.push(row.name);
                                    } else {
                                        vData.percentages.push({
                                            label:   'percentile',
                                            number:  row.value || 50,
                                            checked: true,
                                        });
                                    }
                                });
                                if(vData.percentages.length === 0) {
                                    vData.percentages = [{
                                        label:   'percentile',
                                        checked: false,
                                        number:  50,
                                    }];
                                }

                                members.forEach(member => {
                                    const item = vData.data_set_list.find(row => row.member_id === member.member_id);

                                    if(item) {
                                        item.features.push(...member.features);
                                    }
                                });
                                vData.workMode = workMode;
                                methods.workModeChange(workMode);
                            }
                            vData.inited = true;
                        }
                    });
                },

                showColumnListDialog() {
                    vData.featureSelectTab.forEach(row => {
                        row.$checkedColumnsArr = [];
                        row.$feature_list.forEach(x => {
                            if (x.method) {
                                row.$checkedColumnsArr.push(x.name);
                            }
                        });
                    });
                    CheckFeatureDialogRef.value.show();
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
                            members,
                            cross_parties: vData.cross_parties,
                        },
                    };
                },
            };

            // merge mixin
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

<style lang="scss" scoped>
    .el-checkbox-group{
        max-height: 500px;
        overflow: auto;
        font-size: 14px;
    }
    .el-checkbox{user-select:auto;}
    .el-tag-list{
        max-height: 140px;
        overflow: auto;
    }
</style>
