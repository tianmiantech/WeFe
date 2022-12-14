<template>
    <el-form
        v-loading="vData.loading"
        :disabled="disabled"
        label-position="top"
        @submit.prevent
    >
        <div>
            <el-button
                style="margin-top:2px;"
                :disabled="vData.total_column_count === 0"
                @click="methods.showColumnListDialog"
            >
                选择特征（{{ vData.feature_column_count }}/{{ vData.total_column_count }}）
            </el-button>
            <el-tabs
                type="card"
                class="mt20"
                size="small"
            >
                <el-tab-pane
                    v-for="(item, index) in vData.featureSelectTab"
                    :key="`${item.member_id}-${item.member_role}`"
                    :label="`${item.member_name} (${item.member_role === 'provider' ? '协作方' : '发起方'})`"
                    :name="`${index}`"
                >
                    <template v-if="item.$checkedColumnsArr.length">
                        <el-space wrap>
                            <template
                                v-for="(tag, index) in item.$checkedColumnsArr"
                                :key="index"
                            >
                                <el-tag
                                    v-if="index < 20"
                                    :label="tag"
                                    :value="tag"
                                >
                                    {{ tag }}
                                </el-tag>
                            </template>
                            <el-button
                                v-if="item.$checkedColumnsArr.length > 20"
                                size="small"
                                type="primary"
                                class="check-features"
                                @click="methods.checkFeatures(item.$checkedColumnsArr)"
                            >
                                查看更多
                            </el-button>
                        </el-space>
                    </template>
                </el-tab-pane>
            </el-tabs>
        </div>

        <div class="mt20">
            <p class="board-form-item mt20">
                运行模式:
                <el-radio-group
                    v-model="vData.workMode"
                    @change="methods.workModeChange"
                >
                    <el-radio label="local">本地</el-radio>
                    <el-radio label="federation">多方</el-radio>
                </el-radio-group>
            </p>
        </div>

        <CheckFeatureDialog
            ref="CheckFeatureDialogRef"
            revertCheckEmit="true"
            :feature-select-tab="vData.featureSelectTab"
            :column-list-type="vData.columnListType"
            @revertCheck="methods.revertCheck"
            @confirmCheck="methods.confirmCheck"
        />
    </el-form>
</template>

<script>
    import { reactive, getCurrentInstance, ref } from 'vue';
    import checkFeatureMixin from '../common/checkFeature';
    import CheckFeatureDialog from '../common/checkFeatureDialog';

    export default {
        name:       'FeatureStatistic',
        components: {
            CheckFeatureDialog,
        },
        emits: [...checkFeatureMixin().emits],
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
            const { appContext } = getCurrentInstance();
            const { $http, $alert } = appContext.config.globalProperties;
            const CheckFeatureDialogRef = ref();

            let vData = reactive({
                inited:               false,
                loading:              false,
                row_index:            0,
                data_set_list:        [],
                column_list:          [],
                showColumnList:       false,
                feature_column_count: 0,
                total_column_count:   0,
                columnListType:       'quantile',
                selectList:           [{
                    id:                   Math.round(Math.random()*10e12),
                    method:               'quantile',
                    count:                1,
                    feature_column_count: 0,
                }],
                form: {
                    isIndeterminate: false,
                    checkAll:        false,
                },
                featureSelectTab: [],
                workMode:         'federation',
                fixedOptions:     [],
            });

            let methods = {
                async getNodeDetail(model) {
                    vData.loading = true;
                    const { code, data } = await $http.get({
                        url:    '/project/flow/node/detail',
                        params: {
                            nodeId:  model.id,
                            flow_id: props.flowId,
                        },
                    });

                    vData.loading = false;
                    if (code === 0 && data && data.params) {
                        const { params } = data;

                        if(params.members) {
                            const { members, workMode, form } = params;

                            vData.feature_column_count = 0;
                            vData.form = form;
                            members.forEach(member => {
                                const item = vData.featureSelectTab.find(row => row.member_id === member.member_id && row.member_role === member.member_role);
                                const chooseItem = [];
                                if(item) {
                                    item.$feature_list.forEach(feature => {
                                        if (member.features.includes(feature.name)) {
                                            chooseItem.push(feature.name);
                                        }
                                    });
                                    item.$checkedColumnsArr = [...chooseItem];
                                    vData.feature_column_count += chooseItem.length;
                                }
                            });
                            vData.workMode = workMode;
                            methods.workModeChange(workMode);
                        }
                        vData.featureSelectTab.forEach(tab => {
                            vData.data_set_list.push({
                                member_id:   tab.member_id,
                                member_role: tab.member_role,
                                member_name: tab.member_name,
                                features:    tab.$checkedColumnsArr,
                            });
                        });
                        vData.inited = true;
                    }
                },

                workModeChange(val) {
                    vData.data_set_list.forEach(member => {
                        if(val === 'local') {
                            member.show = false;
                            if(member.member_role === 'promoter') {
                                member.show = true;
                            }
                        } else {
                            member.show = true;
                        }
                    });
                },

                checkFeatures(arr) {
                    $alert('已选特征:', {
                        title:                    '已选特征:',
                        message:                  `<div style="max-height: 80vh;overflow:auto;">${arr.join(',')}</div>`,
                        dangerouslyUseHTMLString: true,
                    });
                },

                confirmCheck(list) {
                    vData.feature_column_count = 0;
                    vData.data_set_list = [];
                    list.forEach(item => {
                        vData.feature_column_count += item.$checkedColumnsArr.length;
                        vData.data_set_list.push({
                            member_id:   item.member_id,
                            member_role: item.member_role,
                            member_name: item.member_name,
                            features:    item.$checkedColumnsArr,
                        });
                    });
                },

                checkParams() {
                    const members = [];

                    vData.data_set_list.forEach(row => {
                        members.push({
                            member_id:   row.member_id,
                            member_role: row.member_role,
                            member_name: row.member_name,
                            features:    row.features,
                        });
                    });

                    return {
                        params: {
                            form:     vData.form,
                            workMode: vData.workMode,
                            members,
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
    .board-form-item{
        .board-form-item__label{
            line-height: 28px !important;
        }
    }
    .board-checkbox-group{
        max-height: 500px;
        overflow: auto;
        font-size: 14px;
    }
    .board-checkbox{user-select:auto;}
    .board-tag-list{
        max-height: 140px;
        overflow: auto;
    }
    .check-features{
        padding:0 10px;
        min-height: 24px;
        margin-left: 5px;
    }
</style>
