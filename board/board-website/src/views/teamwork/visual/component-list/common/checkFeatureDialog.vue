<template>
    <el-dialog
        width="70%"
        title="选择特征列"
        v-model="vData.showColumnList"
        :close-on-click-modal="false"
        custom-class="large-width"
        destroy-on-close
        append-to-body
    >
        <el-tabs
            v-model="vData.featureSelectTabIndex"
            type="card"
        >
            <el-tab-pane
                v-for="(item, index) in featureSelectTab"
                :key="`${item.member_id}-${item.member_role}`"
                :label="`${item.member_name} (${item.member_role === 'promoter' ? '发起方': '协作方'})`"
                :name="`${index}`"
            >
                <el-form
                    v-loading="vData.columnListLoading"
                    element-loading-text="当前特征列较多需要时间处理, 请耐心等待"
                    class="flex-form"
                    @submit.prevent
                >
                    <el-form-item label="快速选择：">
                        <el-input
                            v-model="item.$checkedColumns"
                            placeholder="输入特征名称, 多个特征名称用,分开"
                            clearable
                        >
                            <template #append>
                                <el-button @click="methods.autoCheck(item)">
                                    确定
                                </el-button>
                            </template>
                        </el-input>
                    </el-form-item>
                    <div class="mt20 mb10">
                        <el-checkbox
                            v-model="item.$checkedAll"
                            :indeterminate="item.$indeterminate"
                            @change="methods.checkAll(item)"
                        >
                            全选
                        </el-checkbox>
                        <el-button
                            type="primary"
                            size="small"
                            class="ml10 revert-check-btn"
                            @click="methods.revertCheck(item)"
                        >
                            反选
                        </el-button>
                        <span class="ml15">({{ item.$checkedColumnsArr.length }} / {{ item.$feature_list.length }})</span>
                    </div>
                    <BetterCheckbox
                        v-if="vData.showColumnList"
                        :list="item.$feature_list"
                    >
                        <template #checkbox="{ index, list }">
                            <template
                                v-for="i in 5"
                                :key="`${index * 5 + i - 1}`"
                            >
                                <label
                                    v-if="list[index * 5 + i - 1]"
                                    :for="`label-${index * 5 + i - 1}`"
                                    :class="['el-checkbox el-checkbox--small', { 'is-checked': item.$checkedColumnsArr.includes(list[index * 5 + i - 1].name) }]"
                                    @click.prevent.stop="methods.checkboxChange($event, item, list[index * 5 + i - 1].name, columnListType)"
                                >
                                    <span :class="['el-checkbox__input', { 'is-checked': item.$checkedColumnsArr.includes(list[index * 5 + i - 1].name), 'is-disabled': Boolean(!!list[index * 5 + i - 1].method && selectListId && list[index * 5 + i - 1].id && list[index * 5 + i - 1].id !== selectListId) }]">
                                        <span class="el-checkbox__inner"></span>
                                        <input
                                            :id="`label-${index * 5 + i - 1}`"
                                            :disabled="Boolean(!!list[index * 5 + i - 1].method && selectListId && list[index * 5 + i - 1].id && list[index * 5 + i - 1].id !== selectListId)"
                                            class="el-checkbox__original"
                                            type="checkbox"
                                        />
                                    </span>
                                    <span class="el-checkbox__label">{{ list[index * 5 + i - 1].name }}</span>
                                </label>
                            </template>
                        </template>
                    </BetterCheckbox>
                </el-form>
            </el-tab-pane>
        </el-tabs>
        <div class="text-r mt10">
            <el-button @click="methods.hide">
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
</template>

<script>
    import { reactive } from 'vue';
    import checkFeatureMixin from './checkFeature';

    export default {
        props: {
            featureSelectTab: Array,
            selectListId:     Number,
            columnListType:   String,
            revertCheckEmit:  String,
        },
        emits: [...checkFeatureMixin().emits, 'confirmCheck', 'getCheckedFeature'],
        setup(props, context) {
            let vData = reactive({
                featureSelectTabIndex: '0',
                showColumnList:        false,
                columnListLoading:     false,
            });

            let methods = {
                show() {
                    vData.featureSelectTabIndex = '0';
                    vData.showColumnList = true;
                    vData.columnListLoading = true;
                    setTimeout(() => {
                        vData.columnListLoading = false;
                    }, 300);
                },
                hide() {
                    vData.showColumnList = false;
                    methods.hideColumnList();
                },
                confirmCheck() {
                    vData.showColumnList = false;
                    context.emit('confirmCheck', props.featureSelectTab);
                    context.emit('getCheckedFeature', props.featureSelectTab);
                },
                checkboxChange($event, item, name, method) {
                    const { $checkedColumnsArr, $feature_list } = item;
                    const index = $checkedColumnsArr.findIndex(x => x === name);
                    const feature = $feature_list.find(x => x.name === name);

                    if(~index) {
                        $checkedColumnsArr.splice(index, 1);

                        if(feature) {
                            feature.method = '';
                        }
                    } else {
                        $checkedColumnsArr.push(name);

                        if(feature) {
                            feature.method = method;
                        }
                    }
                },
                loadData() {
                    const start = vData.list.length ? vData.list.length : 0;
                    const maxNumer = 200;

                    for(let i = start; i < props.list.length; i++) {
                        const item = props.list[i];

                        if(i <= start + maxNumer) {
                            vData.list.push(item);
                        } else {
                            break;
                        }
                    }
                },
            };

            const { $data, $methods } = checkFeatureMixin().mixin({
                props,
                context,
                vData,
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
    .revert-check-btn{
        position: relative;
        top: -7px;
    }
</style>
