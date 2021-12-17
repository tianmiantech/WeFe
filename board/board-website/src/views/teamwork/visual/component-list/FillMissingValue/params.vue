<template>
    <el-form
        ref="form"
        v-loading="vData.loading"
        :disabled="disabled"
    >
        <el-form-item
            v-for="(item, index) in vData.selectList"
            :key="item.id"
        >
            <el-select
                v-model="item.method"
                style="width:100px;"
                placeholder="请选择填充方式"
                @change="methods.changeMethod(item)"
            >
                <el-option
                    v-for="option in vData.methodList"
                    :key="option.value"
                    :label="option.label"
                    :value="option.value"
                />
            </el-select>
            <el-input
                v-if="item.method === 'const'"
                v-model="item.count"
                type="number"
                style="width:150px;"
                @blur="methods.changeMethodCount(item, index)"
                @change="methods.changeMethodCount(item, index)"
            />
            <el-button
                size="mini"
                class="ml10"
                style="margin-top:2px;"
                :disabled="vData.total_column_count === 0"
                @click="methods.showColumnListDialog(item, index)"
            >
                选择特征（{{ item.feature_column_count }}/{{ vData.total_column_count }}）
            </el-button>
            <el-button
                type="text"
                class="elicon-delete"
                style="color:#F85564;font-size: 14px;"
                @click="methods.removeRow(item, index)"
            />
        </el-form-item>

        <el-button @click="methods.addPolicy">
            添加填充策略
        </el-button>

        <el-tabs
            type="card"
            class="mt20"
        >
            <el-tab-pane
                v-for="(item, index) in vData.featureSelectTab"
                :key="`${item.member_id}-${item.member_role}`"
                :label="`${item.member_name} (${item.member_role === 'promoter' ? '发起方': '协作方'})`"
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
                    <el-table-column label="策略">
                        <template v-slot="scope">
                            {{ scope.row.method ? `${vData.methodObj[scope.row.method]} ${scope.row.method === 'const' ? `${scope.row.count}` : ''}` : '' }}
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
            @columnCheckChange="methods.columnCheckChange"
            @hideColumnList="methods.hideColumnList"
            @confirmCheck="methods.confirmCheck"
        />
    </el-form>
</template>

<script>
    import {
        ref,
        reactive,
    } from 'vue';
    import checkFeatureMixin from '../common/checkFeature';
    import CheckFeatureDialog from '../common/checkFeatureDialog';

    export default {
        name:       'FillMissingValue',
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

            let vData = reactive({
                feature_column_count: 0,
                total_column_count:   0,
                selectList:           [{
                    id:                   Math.round(Math.random() * 10e12),
                    method:               'max',
                    count:                1,
                    feature_column_count: 0,
                }],
                methodList: [
                    { value: 'max', label: '最大值' },
                    { value: 'min', label: '最小值' },
                    { value: 'const', label: '常量' },
                    { value: 'mean', label: '平均值' },
                    { value: 'median', label: '中位数' },
                    // { value: 'mode', label: '众数' },
                ],
                methodObj: {
                    'max':    '最大值',
                    'min':    '最小值',
                    'const':  '常量',
                    'mean':   '平均值',
                    'median': '中位数',
                    // 'mode':   '众数',
                },
                columnListType:   'max',
                selectListIndex:  0,
                featureSelectTab: [],
            });

            let methods = {
                addPolicy () {
                    vData.selectList.push({
                        id:                   Math.round(Math.random() * 10e12),
                        method:               'max',
                        feature_column_count: 0,
                        count:                1,
                    });
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
