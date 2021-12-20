<template>
    <el-form
        v-loading="vData.loading"
        :disabled="disabled"
        @submit.prevent
    >
        <p class="f12 color-danger mb10">tips: 所有成员需选择共有的特征!</p>
        <el-form-item
            v-for="(item, index) in vData.selectList"
            :key="item.id"
        >
            <el-select
                v-model="item.method"
                placeholder="请选择类型"
                style="width:86px;"
                @change="methods.changeMethod(item)"
            >
                <el-option
                    v-for="option in vData.methodList"
                    :key="option.value"
                    :label="option.label"
                    :value="option.value"
                />
            </el-select>
            <el-input-number
                v-model="item.count"
                type="number"
                :min="1"
                controls-position="right"
                @blur="methods.changeMethodCount(item, index)"
                @change="methods.changeMethodCount(item, index)"
            />箱
            <el-button
                size="mini"
                class="mt5"
                :disabled="vData.total_column_count === 0"
                @click="methods.showColumnListDialog(item, index)"
            >
                选择特征（{{ item.feature_column_count }}/{{ vData.total_column_count }}）
            </el-button>
        </el-form-item>

        <el-tabs
            type="card"
            class="mt20"
        >
            <el-tab-pane
                v-for="(item, index) in vData.featureSelectTab"
                :key="`${item.member_id}-${item.member_role}`"
                :label="`${item.member_name} (${item.member_role === 'provider' ? '协作方' : '发起方'})`"
                :name="`${index}`"
            >
                <el-table
                    :data="item.$feature_list"
                    style="width:402px"
                    max-height="500px"
                    stripe
                    border
                >
                    <el-table-column
                        prop="name"
                        label="特征"
                        width="150"
                    />
                    <el-table-column label="分箱策略">
                        <template v-slot="scope">
                            <template v-if="scope.row.method === 'custom'">
                                自定义分箱:
                                <el-input
                                    v-model.trim="scope.row.points"
                                    style="width:160px;"
                                    clearable
                                />
                            </template>
                            <template v-else-if="scope.row.method">
                                {{ `${vData.methodObj[scope.row.method]} ${scope.row.count}箱` }}
                            </template>
                        </template>
                    </el-table-column>
                </el-table>
            </el-tab-pane>
        </el-tabs>

        <CheckFeatureDialog
            ref="CheckFeatureDialogRef"
            :select-list-id="vData.selectList[vData.selectListIndex] ? vData.selectList[vData.selectListIndex].id : 0"
            :feature-select-tab="vData.featureSelectTab"
            :column-list-type="vData.columnListType"
            @autoCheck="methods.autoCheck"
            @checkAll="methods.checkAll"
            @revertCheck="methods.revertCheck"
            @confirmCheck="methods.confirmCheck"
            @columnCheckChange="methods.columnCheckChange"
            @hideColumnList="methods.hideColumnList"
        />
    </el-form>
</template>

<script>
    import {
        ref,
        reactive,
        getCurrentInstance,
    } from 'vue';
    import checkFeatureMixin from '../common/checkFeature';
    import CheckFeatureDialog from '../common/checkFeatureDialog';

    export default {
        name:       'HorzFeatureBinning',
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
            const { $alert } = appContext.config.globalProperties;

            let vData = reactive({
                inited:               false,
                loading:              false,
                feature_column_count: 0,
                total_column_count:   0,
                selectList:           [{
                    id:                   Math.round(Math.random()*10e12),
                    method:               'quantile',
                    count:                1,
                    feature_column_count: 0,
                }],
                methodList: [
                    { value: 'quantile', label: '等频' },
                ],
                methodObj: {
                    'quantile': '等频',
                },
                columnListType:  'quantile',
                selectListIndex: 0,
            });

            let methods = {
                paramsCheck() {
                    let featureName = '';
                    const members = vData.featureSelectTab.length;
                    const featureMaps = {};

                    for(const index in vData.featureSelectTab) {
                        const member = vData.featureSelectTab[index];

                        for(const i in member.$feature_list) {
                            const row = member.$feature_list[i];

                            if(row.method) {
                                if(featureName === '' && row.method === 'custom' && !row.points) {
                                    featureName = row.name;
                                    $alert(`<p class="color-danger">${member.member_name} 特征 ${featureName} 未填写, 请检查</p>`, '警告', {
                                        type:                     'warning',
                                        dangerouslyUseHTMLString: true,
                                    });
                                    return false;
                                }
                                if(!featureMaps[row.name]) {
                                    featureMaps[row.name] = 1;
                                } else {
                                    featureMaps[row.name]++;
                                }
                            }
                        }
                    }

                    for(const key in featureMaps) {
                        const val = featureMaps[key];

                        if(val !== members) {
                            $alert(`所有成员需选择共有的特征! <p class="color-danger">特征 ${key} 未被所有发起方选择, 请检查</p>`, '警告', {
                                type:                     'warning',
                                dangerouslyUseHTMLString: true,
                            });
                            return false;
                        }
                    }

                    return true;
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
    .el-input-number{
        width: 104px;
        margin:0 10px;
        :deep(.el-input__inner){
            padding-left:5px;
            padding-right: 40px;
        }
    }
</style>
