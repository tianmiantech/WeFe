<template>
    <el-form
        v-loading="vData.loading"
        :disabled="disabled"
        label-position="top"
    >
        <template
            v-for="(member, $index) in vData.data_set_list"
            :key="`${member.member_id}-${member.member_role}`"
        >
            <h4 class="f14 mb5">{{member.member_role === 'promoter' ? '发起方' : '协作方'}}:</h4>
            <div class="el-form-item">
                <div class="el-form-item__label">
                    <span class="mr10">{{ member.member_name }}</span>
                    <el-button
                        size="small"
                        @click="methods.checkColumns(member, $index)"
                    >
                        选择特征（{{ member.features.length }}/{{ member.columns }}）
                    </el-button>
                </div>
                <div
                    v-if="member.features.length"
                    class="el-tag-list mb10"
                >
                    <template
                        v-for="(item, index) in member.features"
                        :key="index"
                    >
                        <el-tag
                            v-if="index < 20"
                            :label="item"
                            :value="item"
                        >
                            {{ item }}
                        </el-tag>
                    </template>
                    <el-button
                        v-if="member.features.length > 20"
                        size="small"
                        type="primary"
                        class="check-features"
                        @click="methods.checkFeatures(member.features)"
                    >
                        查看更多
                    </el-button>
                </div>
            </div>
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
                        size="small"
                        class="ml10"
                        style="margin-top: -7px;"
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
                                class="el-checkbox"
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
        nextTick,
        reactive,
        getCurrentInstance,
    } from 'vue';

    export default {
        name:  'HorzOneHot',
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
            const { $alert, $http } = appContext.config.globalProperties;

            const vData = reactive({
                inited:            false,
                loading:           false,
                row_index:         0,
                data_set_list:     [],
                column_list:       [],
                checkedColumns:    '',
                checkedColumnsArr: [],
                showColumnList:    false,
                columnListLoading: false,
                indeterminate:     false,
                checkedAll:        false,
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

                    vData.loading = false;
                    if (code === 0 && data && data.params && Object.keys(data.params).length) {
                        const { members } = data.params;

                        members.forEach(member => {
                            const item = vData.data_set_list.find(row => row.member_id === member.member_id && row.member_role === member.member_role);

                            if(item) {
                                item.features.push(...member.features);
                            }
                        });
                        vData.inited = true;
                    }
                },

                checkColumns(row, index) {
                    vData.row_index = index;
                    vData.checkedAll = false;
                    vData.indeterminate = false;
                    vData.showColumnList = true;
                    vData.column_list = row.$features;
                    vData.checkedColumnsArr = [];
                    vData.checkedColumns = '';
                    if(row.$features.length) {
                        vData.checkedColumns = row.features.join(',');
                        methods.autoCheck();
                    }
                },

                checkFeatures(arr) {
                    $alert('已选特征:', {
                        title:                    '已选特征:',
                        message:                  `<div style="max-height: 80vh;overflow:auto;">${arr.join(',')}</div>`,
                        dangerouslyUseHTMLString: true,
                    });
                },

                autoCheck() {
                    vData.columnListLoading = true;

                    setTimeout(() => {
                        if(vData.checkedColumns.trim().length) {
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

                checkParams() {
                    const members = vData.data_set_list.map(row => {
                        return {
                            member_id:   row.member_id,
                            member_role: row.member_role,
                            member_name: row.member_name,
                            features:    row.features,
                        };
                    });

                    return {
                        params: {
                            members,
                        },
                    };
                },
            };

            return {
                vData,
                methods,
            };
        },
    };
</script>

<style lang="scss" scoped>
    .el-form-item{
        .el-form-item__label{
            line-height: 28px !important;
        }
    }
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
    .check-features{
        padding:0 10px;
        min-height: 24px;
        margin-left: 5px;
    }
</style>
