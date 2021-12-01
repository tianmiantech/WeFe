<template>
    <el-form
        v-loading="vData.loading"
        :disabled="disabled"
        @submit.prevent
    >
        <p class="color-danger mb10 f12">tips: 所有发起方需选择共有的特征!</p>
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
            <span class="mr10">
                <el-input-number
                    v-model="item.count"
                    type="number"
                    :min="1"
                    controls-position="right"
                    @blur="methods.changeMethodCount(item, index)"
                    @change="methods.changeMethodCount(item, index)"
                />箱
            </span>
            <el-button
                size="mini"
                style="margin-top:2px;"
                :disabled="vData.total_column_count === 0"
                @click="methods.showColumnListDialog(item, index)"
            >
                选择特征（{{ item.feature_column_count }}/{{ vData.total_column_count }}）
            </el-button>
            <el-button
                v-if="vData.selectList.length > 1"
                type="text"
                class="color-danger"
                icon="elicon-delete"
                @click="methods.removeRow(item, index)"
            />
        </el-form-item>

        <el-button
            :disabled="vData.total_column_count === 0 || vData.total_column_count === vData.feature_column_count"
            @click="methods.addPolicy"
        >
            添加分箱策略
        </el-button>

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
                            {{ scope.row.method ? `${vData.methodObj[scope.row.method]} ${scope.row.count}箱` : '' }}
                        </template>
                    </el-table-column>
                </el-table>
            </el-tab-pane>
        </el-tabs>

        <CheckFeatureDialog
            ref="CheckFeatureDialogRef"
            revertCheckEmit="true"
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
        name:       'MixBinning',
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
                columnListType:   'quantile',
                selectListIndex:  0,
                featureSelectTab: [],
            });

            let methods = {
                addPolicy () {
                    vData.selectList.push({
                        id:                   Math.round(Math.random() * 10e12),
                        method:               'quantile',
                        feature_column_count: 0,
                        count:                1,
                    });
                },

                hideColumnList() {
                    vData.featureSelectTab.forEach(row => {
                        // restore last results
                        row.$checkedColumnsArr = [];
                        row.$feature_list.forEach(column => {
                            if(column.id) {
                                column.method = vData.selectList[vData.selectListIndex].method;
                                row.$checkedColumnsArr.push(column.name);
                            } else {
                                column.method = '';
                            }
                        });
                    });
                },

                columnCheckChange(item) {
                    const exists = item.$feature_list.filter(column => !item.$checkedColumnsArr.includes(column.name));

                    exists.forEach(column => column.method = '');
                },
                /*
                    1. only one policy
                */
                revertCheck(item) {
                    if (item.$checkedColumnsArr.length === item.$feature_list.length) {
                        item.$indeterminate = false;
                        item.$checkedAll = false;
                    }

                    const lastIds = [...item.$checkedColumnsArr];

                    // remove last selected result
                    for (let i = 0; i < lastIds.length; i++) {
                        const name = lastIds[i];
                        const column = item.$feature_list.find(x => name === x.name);

                        if (!column.id || column.id === vData.selectList[vData.selectListIndex].id) {
                            const index = item.$checkedColumnsArr.findIndex(x => x === column.name);

                            column.method = '';
                            item.$checkedColumnsArr.splice(index, 1);
                        }
                    }

                    // Add selected results
                    item.$feature_list.forEach(column => {
                        const name = lastIds.find(x => x === column.name);

                        if (!name && (!column.method || column.method === vData.columnListType)) {
                            column.method = vData.columnListType;
                            item.$checkedColumnsArr.push(column.name);
                        }
                    });

                    if (item.$checkedColumnsArr.length === item.$feature_list.length) {
                        item.$indeterminate = false;
                        item.$checkedAll = true;
                    }
                },

                paramsCheck() {
                    let promoters = 0;
                    const checked = true;
                    const featureMaps = {};

                    vData.featureSelectTab.forEach(member => {
                        if(member.member_role === 'promoter') {
                            promoters++;
                        }
                    });

                    vData.featureSelectTab.forEach((member, index) => {
                        if(member.member_role === 'promoter') {
                            member.$feature_list.forEach(row => {
                                if(row.method) {
                                    if(!featureMaps[row.name]) {
                                        featureMaps[row.name] = 1;
                                    } else {
                                        featureMaps[row.name]++;
                                    }
                                }
                            });
                        }
                    });

                    for(const key in featureMaps) {
                        const val = featureMaps[key];

                        if(val !== promoters) {
                            $alert(`所有 [发起方] 需选择共有的特征! <p class="color-danger">特征 ${key} 未被所有发起方选择, 请检查</p>`, '警告', {
                                type:                     'warning',
                                dangerouslyUseHTMLString: true,
                            });
                            return false;
                        }
                    }

                    return checked;
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
