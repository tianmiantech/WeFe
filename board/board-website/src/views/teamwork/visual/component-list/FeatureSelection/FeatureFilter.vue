<template>
    <el-drawer
        v-model="open"
        title="特征筛选操作面板"
        direction="rtl"
        size="1000px"
    >
        <div class="container">
            <div class="table">
                <el-form inline>
                    <el-form-item label="特征">
                        <el-input
                            v-model="featureInput"
                            placeholder="请输入特征名"
                        />
                    </el-form-item>
                    <el-form-item>
                        <el-button type="primary" @click="searchHandle"
                            >查询</el-button
                        >
                    </el-form-item>
                </el-form>
                <el-table :data="visibleFeatures" :style="{ height: '600px' }">
                    <el-table-column property="name" label="特征" width="150">
                        <template v-slot="scope">
                            <el-tag
                                :style="{ height: '22px', color: 'white' }"
                                :color="calcColor(scope.row)"
                            >
                                <span>{{ scope.row.name }}</span>
                                ({{ scope.row.member_name }})
                            </el-tag>
                        </template>
                    </el-table-column>
                    <el-table-column
                        property="missing_rate"
                        label="缺失率"
                        width="120"
                        sortable
                        v-if="frontStatus.has_c_v"
                    />
                    <el-table-column
                        property="iv"
                        label="IV"
                        sortable
                        v-if="frontStatus.has_i_v"
                    />
                    <el-table-column
                        property="cv"
                        label="CV"
                        sortable
                        v-if="frontStatus.has_c_v"
                    />
                    <el-table-column label="操作">
                        <template v-slot="scope">
                            <el-link
                                type="text"
                                disabled
                                v-if="
                                    campareItem(computedSelectData, scope.row)
                                "
                            >
                                已选择
                            </el-link>
                            <el-link
                                v-else
                                @click="selectItemHandle(scope.row)"
                                :type="
                                    campareItem(manulSelectData, scope.row)
                                        ? 'danger'
                                        : 'primary'
                                "
                            >
                                {{
                                    campareItem(manulSelectData, scope.row)
                                        ? '移除'
                                        : '选择'
                                }}
                            </el-link>
                        </template>
                    </el-table-column>
                </el-table>
            </div>
            <div class="right">
                <el-alert
                    title="如需使用iv条件筛选，请在前置流程中使用“WOE编码”组件。"
                    type="warning"
                    :closable="false"
                    v-if="!frontStatus.has_i_v"
                />
                <el-alert
                    title="如需使用cv及缺失率条件筛选，请在前置流程中使用“特征统计”组件。"
                    type="warning"
                    :closable="false"
                    v-if="!frontStatus.has_c_v"
                />
                <h4 class="title">条件</h4>
                <el-space wrap>
                    <template
                        v-for="(
                            { feature, range, value }, index
                        ) in conditionList"
                        :key="tag"
                    >
                        <el-tag
                            class="mx-1"
                            closable
                            :disable-transitions="false"
                            @close="removeFeature({ feature })"
                        >
                            {{ findLabelByValue(feature, featureOptions)
                            }}{{ findLabelByValue(range, rangeOptions) }}
                            {{ value }}
                            <template v-if="feature === 'missing_rate'">
                                %
                            </template>
                        </el-tag>
                        <el-tag
                            round
                            type="success"
                            v-if="index !== conditionList.length - 1"
                        >
                            and
                        </el-tag>
                    </template>
                    <el-button
                        :icon="Plus"
                        :disabled="!visibleFeatureOptions.length"
                        circle
                        @click="openDialogHandle"
                    />
                </el-space>
                <h4 class="title">手动选择</h4>
                <el-space wrap v-if="manulSelectData.length">
                    <el-tag
                        style="color: white"
                        type="info"
                        effect="plain "
                        :color="calcColor(item)"
                        @close="selectItemHandle(item)"
                        closable
                        v-for="item in manulSelectData"
                    >
                        {{ item.name }}({{ item.member_name }})
                    </el-tag>
                </el-space>
                <div class="empty" v-else>暂无数据</div>
                <h4 class="title">已选项</h4>
                <el-space wrap v-if="allSelectData.length">
                    <el-tag
                        style="color: white"
                        v-for="item in allSelectData"
                        type="info"
                        :color="calcColor(item)"
                    >
                        {{ item.name }}({{ item.member_name }})
                    </el-tag>
                </el-space>
                <div class="empty" v-else>暂无数据</div>
            </div>
        </div>
        <template #footer>
            <el-space>
                <el-tag
                    v-for="({ total, selected }, memeberName) in membersInfo"
                    :type="selected ? 'success' : 'danger'"
                    round
                >
                    {{ memeberName }}：{{ selected }}/{{ total }}
                </el-tag>
            </el-space>
            <el-button type="primary" @click="saveResult"> 保存 </el-button>
            <el-button @click="open = false"> 取消 </el-button>
        </template>
    </el-drawer>
    <el-dialog v-model="dialogOpen" title="根据条件选择" width="460px">
        <el-form :rules="dialogRules" ref="dialogRef" :model="featureForm">
            <el-form-item label="特征" prop="feature">
                <el-select v-model="featureForm.feature" class="m-2">
                    <el-option
                        v-for="item in featureOptions"
                        :key="item.value"
                        :label="item.label"
                        :value="item.value"
                        :disabled="
                            visibleFeatureOptions.every(
                                ({ value }) => item.value !== value
                            )
                        "
                    />
                </el-select>
            </el-form-item>
            <el-form-item label="条件" prop="range">
                <el-select v-model="featureForm.range" class="m-2">
                    <el-option
                        v-for="item in rangeOptions"
                        :key="item.value"
                        :label="item.label"
                        :value="item.value"
                    />
                </el-select>
            </el-form-item>
            <el-form-item label="值" prop="value">
                <el-input v-model="featureForm.value">
                    <template #append v-if="needPercent">%</template>
                </el-input>
            </el-form-item>
        </el-form>
        <template #footer>
            <el-button type="primary" @click="DialogConfirmHandle">
                确定
            </el-button>
            <el-button @click="dialogOpen = false"> 取消 </el-button>
        </template>
    </el-dialog>
</template>
<script setup>
import { ref, reactive, computed, watch, nextTick } from 'vue';
import { ElMessage } from 'element-plus';
import { Plus } from '@element-plus/icons-vue';
const open = ref(false);
const props = defineProps({
    allFeatures: Array,
    selectedFeature: Array,
    frontStatus: Object,
    selectedConditions: Array,
});
const emits = defineEmits(['submit']);
const colors = ['#108ee9', '#87d068', '#2db7f5', '#f50'];
const colorSet = computed(() => {
    const temp = props.allFeatures.reduce(
        (acc, { member_name }) =>
            acc.includes(member_name) ? acc : [...acc, member_name],
        []
    );
    return temp.map((each, index) => ({ member: each, color: colors[index] }));
});
const campareItem = (list, target) =>
    list.find(
        ({ member_name, name }) =>
            member_name === target.member_name && name === target.name
    );
const calcColor = (item) =>
    colorSet.value.find((each) => each.member === item.member_name).color;
const featureOptions = [
    {
        value: 'iv',
        label: 'IV值',
    },
    {
        value: 'missing_rate',
        label: '缺失率',
    },
    {
        value: 'cv',
        label: 'CV值',
    },
];
const {
    frontStatus: { has_c_v, has_i_v },
} = props;
const visibleFeatureOptions = computed(() => {
    const temp = [];
    const [ivItem, ...cvItem] = featureOptions;
    const has_c_v = props.frontStatus?.has_c_v;
    const has_i_v = props.frontStatus?.has_i_v;
    if (has_i_v) {
        temp.push(ivItem.value);
    }
    if (has_c_v) {
        temp.push(...cvItem);
    }
    return temp.filter(
        ({ value }) =>
            !conditionList.value.find((each) => each.feature === value)
    );
});
const rangeOptions = [
    {
        value: true,
        label: '大于等于',
    },
    {
        value: false,
        label: '小于等于',
    },
];
const findLabelByValue = (value, options) =>
    options.find((each) => each.value === value).label;

const featureInput = ref();
const visibleFeatures = ref();
watch(
    () => props.allFeatures,
    (value) => {
        visibleFeatures.value = value;
    },
    {
        immediate: true,
    }
);
watch(
    () => open.value,
    (value) => {
        if (value) {
            const { selectedFeature, selectedConditions } = props;

            console.log(selectedConditions);
            conditionList.value = selectedConditions;
            manulSelectData.value = selectedFeature.filter(
                ({ type }) => type === 'manul'
            );
            computedSelectData.value = selectedFeature.filter(
                ({ type }) => type === 'computed'
            );
        } else {
            allSelectData.value = [];
            computedSelectData.value = [];
            manulSelectData.value = [];
        }
    }
);
const searchHandle = () => {
    visibleFeatures.value = props.allFeatures.filter((each) =>
        each.name.includes(featureInput.value)
    );
};

const manulSelectData = ref([]);
const computedSelectData = ref([]);
const allSelectData = computed(() => [
    ...manulSelectData.value,
    ...computedSelectData.value,
]);
const membersInfo = computed(() => {
    const temp = props.allFeatures.reduce(
        (acc, cur) => ({
            ...acc,
            [cur.member_name]:
                1 +
                (Reflect.has(acc, cur.member_name) ? acc[cur.member_name] : 0),
        }),
        {}
    );
    for (const key in temp)
        temp[key] = {
            total: temp[key],
            selected: allSelectData.value.filter(
                (each) => each.member_name === key
            ).length,
        };
    return temp;
});
const selectItemHandle = (row) => {
    if (campareItem(allSelectData.value, row)) {
        manulSelectData.value = manulSelectData.value.filter(
            (each) => !campareItem([each], row)
        );
    } else manulSelectData.value = [...manulSelectData.value, row];
};

const dialogOpen = ref(false);
const dialogRef = ref();
const openDialogHandle = () => (dialogOpen.value = true);
const dialogRules = reactive({
    feature: [{ required: true, message: '请选择特征' }],
    range: [{ required: true, message: '请选择条件' }],
    value: [{ required: true, message: '请输入值' }],
});
const featureForm = reactive({
    feature: '',
    range: '',
    value: '',
});
const conditionList = ref([]);
const needPercent = computed(() => featureForm.feature === 'missing_rate');
const removeFeature = ({ feature }) => {
    conditionList.value = conditionList.value.filter(
        (each) => each.feature !== feature
    );
};
const DialogConfirmHandle = async () => {
    const formEl = dialogRef.value;
    if (!formEl) return;
    const valid = await formEl.validate();
    if (!valid) return;
    conditionList.value = [
        ...conditionList.value,
        JSON.parse(JSON.stringify(featureForm)),
    ];
    await nextTick();
    formEl.resetFields();
    dialogOpen.value = false;
    dialogOpen.value = false;
};
watch(
    () => conditionList.value,
    (newValue) => {
        const result = newValue.length
            ? newValue.reduce((total, cur) => {
                  if (!total.length) return total;
                  const { feature, range, value } = cur;
                  return total.reduce(
                      (acc, row) =>
                          range ===
                              (feature === 'missing_rate' ? 100 : 1) *
                                  row[feature] -
                                  value >=
                                  0 && !campareItem(manulSelectData.value, row)
                              ? [...acc, row]
                              : acc,
                      []
                  );
              }, props.allFeatures)
            : [];
        computedSelectData.value = result;
    }
);
const saveResult = () => {
    let flag = true;
    for (const key in membersInfo.value) {
        const value = membersInfo.value[key];
        if (!value.selected) {
            ElMessage({
                message: `成员${key}未选择特征，无法保存`,
                type: 'warning',
            });
            flag = false;
        }
    }
    if (flag)
        emits(
            'submit',
            [
                ...manulSelectData.value.map((each) => ({
                    ...each,
                    type: 'manul',
                })),
                ...computedSelectData.value.map((each) => ({
                    ...each,
                    type: 'computed',
                })),
            ],
            conditionList.value
        );
};

defineExpose({
    open,
});
</script>
<style lang="scss" scoped>
.container {
    display: flex;
    flex-direction: row;
    column-gap: 40px;
    .table {
        height: 600px;
        width: 700px;
    }
    .right {
        margin-top: 70px;
        h4.title {
            margin: 8px 0;
        }
        div.empty {
            color: #909399;
            font-size: 14px;
        }
    }
}
</style>
