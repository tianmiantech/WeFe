<template>
    <el-drawer
        v-model="open"
        title="特征筛选操作面板"
        direction="rtl"
        size="1080px"
    >
        <div class="container">
            <div class="table">
                <el-form inline>
                    <el-form-item label="特征">
                        <el-input
                            v-model="featureInput"
                            placeholder="请输入特征名"
                            clearable
                        />
                    </el-form-item>
                    <el-form-item>
                        <el-button type="primary" @click="searchHandle"
                            >查询</el-button
                        >
                    </el-form-item>
                </el-form>
                <el-space>
                    <template v-for="({color, member}) in colorSet">
                        <div :style="{ width: '22px', height: '16px', backgroundColor: color }" />{{ member }}
                    </template>
                </el-space>
                <el-table :data="visibleFeatures" :style="{ height: '600px' }">
                    <el-table-column property="name" label="特征" width="150">
                        <template v-slot="scope">
                            <el-tag
                                :style="{ height: '22px', color: 'white' }"
                                :color="calcColor(scope.row)"
                            >
                                <span>{{ scope.row.name }}</span>
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
                        <template #default="{ row }">
                            <el-link
                                v-if="campareItem(computedSelectData, row)"
                                type="text"
                                disabled
                            >
                                已选择
                            </el-link>
                            <el-link
                                v-else
                                @click="selectItemHandle(row)"
                                :type="
                                    campareItem(manulSelectData, row)
                                        ? 'danger'
                                        : 'primary'
                                "
                            >
                                {{
                                    campareItem(manulSelectData, row)
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
                <el-space>
                    <el-tag v-for="({ total, selected }, memeberName) in membersInfo"
                        :type="selected ? 'success' : 'danger'" round>
                        {{ memeberName }}：{{ selected }}/{{ total }}
                    </el-tag>
                </el-space>
                <h4 class="title">条件筛选</h4>
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
                <h4 class="title">手动筛选</h4>
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
                        {{ item.name }}
                    </el-tag>
                </el-space>
                <div class="empty" v-else>在左侧列表中选择需要的特征</div>
                <h4 class="title">筛选结果</h4>
                <el-space wrap v-if="allSelectData.length">
                    <el-tag
                        style="color: white"
                        v-for="item in allSelectData"
                        type="info"
                        :color="calcColor(item)"
                    >
                        {{ item.name }}
                    </el-tag>
                </el-space>
                <div class="empty" v-else>未选择特征</div>
            </div>
        </div>
        <template #footer>
            <el-button type="primary" @click="saveResult"> 保存 </el-button>
            <el-button @click="open = false"> 取消 </el-button>
        </template>
    </el-drawer>
    <AddFeatureDialogVue
        ref="AddFeatureDialogRef"
        :featureOptions="featureOptions"
        :visibleFeatureOptions="visibleFeatureOptions"
        :rangeOptions="rangeOptions"
        :frontStatus="frontStatus"
        @addFeature="
            (value) => (conditionList = [...conditionList, value])
        "
    />
</template>
<script setup>
import { ref, computed, watch } from 'vue';
import { ElMessage } from 'element-plus';
import { Plus } from '@element-plus/icons-vue';
import AddFeatureDialogVue from './AddFeatureDialog.vue';
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
const visibleFeatureOptions = computed(() => {
    const temp = [];
    const [ivItem, ...cvItem] = featureOptions;
    const has_c_v = props.frontStatus?.has_c_v;
    const has_i_v = props.frontStatus?.has_i_v;
    if (has_i_v) {
        temp.push(ivItem);
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

            conditionList.value = selectedConditions;
            computedSelectData.value = selectedFeature.filter(
                ({ type }) => type === 'computed'
            );
            manulSelectData.value = selectedFeature.filter(
                ({ type }) => type !== 'computed'
            );
        } else {
            allSelectData.value = [];
            computedSelectData.value = [];
            manulSelectData.value = [];
        }
    }
);
const featureInput = ref();
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

const AddFeatureDialogRef = ref();
const openDialogHandle = () => (AddFeatureDialogRef.value.open = true);
const conditionList = ref([]);
const removeFeature = ({ feature }) => {
    conditionList.value = conditionList.value.filter(
        (each) => each.feature !== feature
    );
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
        width: 500px;
    }
    .right {
        margin-top: 70px;
        flex: 1;
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
