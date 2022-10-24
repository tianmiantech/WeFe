<template>
    <el-drawer
        v-model="open"
        title="特征筛选操作面板"
        direction="rtl"
        size="75%"
    >
        <el-space style="width: 100%" :size="100" alignment="flex-start">
            <div>
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
                        <el-button @click="openDialogHandle"
                            >快捷选择</el-button
                        >
                    </el-form-item>
                </el-form>
                <el-table ref="tableRef" :data="gridData">
                    <el-table-column type="selection" width="55" />
                    <el-table-column property="name" label="特征" width="150" />
                    <el-table-column
                        property="miss_rate"
                        label="缺失率"
                        width="120"
                        sortable
                    />
                    <el-table-column property="cv" label="CV" sortable />
                    <el-table-column property="iv" label="IV" sortable />
                </el-table>
            </div>
            <div>
                <h4 :style="{ margin: '8px' }">已选项</h4>
                <el-table :data="selectedData">
                    <el-table-column property="name" label="特征" width="150" />
                    <el-table-column>
                        <template v-slot="scope">
                            <a @click="removeItem(scope.row)">移除</a>
                        </template>
                    </el-table-column>
                </el-table>
            </div>
        </el-space>
        <template #footer>
            <el-button type="primary" @click="() => {}"> 保存 </el-button>
            <el-button @click="() => {}"> 取消 </el-button>
        </template>
    </el-drawer>
    <el-dialog v-model="dialogOpen" title="根据条件选择">
        <el-space direction="vertical" alignment="flex-start">
            <template v-for="(formData, index) in features">
                <el-space>
                    <el-select
                        v-model="formData.feature"
                        class="m-2"
                        placeholder="特征类型"
                        size="small"
                    >
                        <el-option
                            v-for="item in featureOptions"
                            :key="item.value"
                            :label="item.label"
                            :value="item.value"
                        />
                    </el-select>
                    <el-select
                        v-model="formData.range"
                        class="m-2"
                        placeholder="条件"
                        size="small"
                    >
                        <el-option
                            v-for="item in rangeOptions"
                            :key="item.value"
                            :label="item.label"
                            :value="item.value"
                        />
                    </el-select>
                    <el-input
                        v-model="formData.value"
                        :style="{
                            width: (needPercents[index] ? 120 : 80) + 'px',
                        }"
                        size="small"
                    >
                        <template #append v-if="needPercents[index]"
                            >%</template
                        >
                    </el-input>
                    <el-icon
                        :style="{ cursor: 'pointer' }"
                        @click="appendHandle(index)"
                    >
                        <Plus v-if="index === features.length - 1" />
                        <Minus v-else />
                    </el-icon>
                </el-space>
            </template>
        </el-space>
        <template #footer>
            <el-button type="primary" @click="() => {}"> 确定 </el-button>
            <el-button @click="dialogOpen = false"> 取消 </el-button>
        </template>
    </el-dialog>
</template>
<script setup>
import { ref, reactive, computed } from 'vue';
import { Plus, Minus } from '@element-plus/icons-vue';
const open = ref(false);
const gridData = [
    {
        name: 'member1.x1',
        cv: 0.352,
        iv: 0.671,
        miss_rate: 0.926,
    },
    {
        name: 'member1.x2',
        cv: 0.552,
        iv: 0.671,
        miss_rate: 0.552,
    },
    {
        name: 'member2.x1',
        cv: 0.652,
        iv: 0.171,
        miss_rate: 0.237,
    },
];
const featureOptions = [
    {
        value: 1,
        label: '缺失率',
    },
    {
        value: 2,
        label: 'IV值',
    },
    {
        value: 3,
        label: 'CV值',
    },
];
const rangeOptions = [
    {
        value: 1,
        label: '大于等于',
    },
    {
        value: 2,
        label: '小于等于',
    },
];

const featureInput = ref();
const searchHandle = () => {
    console.log(featureInput.value);
};

const dialogOpen = ref(false);
const openDialogHandle = () => (dialogOpen.value = true);

const tableRef = ref();
const selectedData = computed(
    () => tableRef.value && tableRef.value.getSelectionRows()
);
const removeItem = (row) => tableRef.value.toggleRowSelection(row, false);

const features = reactive([
    {
        feature: '',
        range: '',
        value: '',
    },
]);
const appendHandle = (index) => {
    const appendStatus = index === features.length - 1;
    if (appendStatus) features.push({ feature: '', range: '', value: '' });
    else features.splice(index, 1);
};
const needPercents = computed(() => features.map((each) => each.feature === 1));

defineExpose({
    open,
});
</script>
