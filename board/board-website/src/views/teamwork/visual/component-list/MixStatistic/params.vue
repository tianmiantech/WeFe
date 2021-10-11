<template>
    <el-form
        v-loading="vData.loading"
        :disabled="disabled"
        @submit.prevent
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
                        style="margin-left: 4px;"
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
                <el-form-item label="快速选择">
                    <el-input
                        v-model="vData.checkedColumns"
                        placeholder="输入特征名称, 多个特征名称用,分开"
                    >
                        <template #append>
                            <el-button @click="methods.autoCheck">
                                确定
                            </el-button>
                        </template>
                    </el-input>
                </el-form-item>
                <div class="mt20 mb10">
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
                        class="ml10"
                        @click="methods.revertCheck"
                    >
                        反选
                    </el-button>
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
    </el-form>
</template>

<script>
    import {
        reactive,
        nextTick,
        getCurrentInstance,
    } from 'vue';
    import checkFeatureMixin from '../common/checkFeature';

    export default {
        name:  'MixStatistic',
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
            const { appContext } = getCurrentInstance();
            const { $http } = appContext.config.globalProperties;

            let vData = reactive({
                inited:               false,
                loading:              false,
                data_set_list:        [],
                feature_column_count: 0,
                total_column_count:   0,
                checkedColumnsArr:    [],
                showColumnList:       false,
                columnListLoading:    false,
                selectList:           [{
                    id:                   Math.round(Math.random()*10e12),
                    method:               'quantile',
                    count:                1,
                    feature_column_count: 0,
                }],
                columnListType:   'quantile',
                selectListIndex:  0,
                featureSelectTab: [],
                col_names:        [],
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

                            if (params) {
                                const { col_names } = params;

                                if (col_names && col_names.length) {
                                    vData.selectList = col_names;
                                }

                                if (vData.data_set_list.length) {
                                    vData.data_set_list.forEach(member => {
                                        const item = vData.data_set_list.find(row => row.member_id === member.member_id);

                                        if(item) {
                                            // item.features.push(...member.features);
                                            item.features.push(...col_names);
                                        }
                                    });
                                }
                            }
                            vData.inited = true;
                        }
                    });
                },

                checkColumns(row, index) {
                    vData.row_index = index;
                    vData.checkedAll = false;
                    vData.indeterminate = false;
                    vData.showColumnList = true;
                    vData.column_list = row.$features;
                    if(row.$features.length) {
                        vData.checkedColumns = row.features.join(',');
                        methods.autoCheck();
                    }
                },

                autoCheck() {
                    vData.columnListLoading = true;

                    setTimeout(() => {
                        const checkedColumnsArr = [...vData.checkedColumnsArr];
                        const column_list = [...vData.column_list];

                        vData.checkedColumns.split(/,|，/).forEach(name => {
                            const $index = column_list.findIndex(column => column === name.trim());

                            // check name is exist
                            if(~$index) {
                                const index = checkedColumnsArr.findIndex(column => column === name.trim());

                                if(index < 0) {
                                    vData.checkedColumnsArr.push(name.trim());
                                }
                                checkedColumnsArr.splice(index, 1);
                                column_list.splice($index, 1);
                            }
                        });

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

                checkAll() {
                    vData.columnListLoading = true;

                    setTimeout(() => {
                        vData.indeterminate = false;
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

                checkboxChange($event, item) {
                    if(props.disabled) return;
                    item.checked = !item.checked;

                    const index = vData.checkedColumnsArr.findIndex(x => x === item);

                    if(~index) {
                        vData.checkedColumnsArr.splice(index, 1);
                    } else {
                        vData.checkedColumnsArr.push(item);
                    }
                    if(vData.checkedColumnsArr.length === vData.column_list.length) {
                        vData.indeterminate = false;
                        vData.checkedAll = true;
                    } else if(vData.checkedAll) {
                        vData.indeterminate = true;
                    }
                },

                confirmCheck() {
                    const row = vData.data_set_list[vData.row_index];

                    vData.data_set_list[vData.row_index] = {
                        ...row,
                        features: vData.checkedColumnsArr,
                    };
                    vData.showColumnList = false;
                },

                getCheckedFeature(list) {
                    const feature = [];

                    list.forEach(item => {
                        feature.push(item.features);
                    });
                    vData.col_names = methods.getTheSame(feature);
                },

                getTheSame(arr) {
                    return arr.reduce(function(a, b) {
                        return a.filter(function(item) {
                            return b.includes(item);
                        });
                    });
                },

                checkParams() {
                    methods.getCheckedFeature(vData.data_set_list);
                    return {
                        params: {
                            col_names: vData.col_names,
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
            });

            vData = $data;
            methods = $methods;

            return {
                vData,
                methods,
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
