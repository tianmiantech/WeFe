<template>
    <el-dialog v-model="open" title="根据条件选择" width="460px">
        <el-alert title="如需使用iv条件筛选，请在前置流程中使用“WOE编码”组件。" type="warning" :closable="false" v-if="!frontStatus.has_i_v" />
        <el-alert title="如需使用cv及缺失率条件筛选，请在前置流程中使用“特征统计”组件。" type="warning" :closable="false"
            v-if="!frontStatus.has_c_v" />
        <el-form :rules="rules" ref="formRef" :model="form">
            <el-form-item label="特征" prop="feature">
                <el-select v-model="form.feature" class="m-2">
                    <el-option v-for="item in featureOptions" :key="item.value" :label="item.label" :value="item.value"
                        :disabled="
                            visibleFeatureOptions.every(
                                ({ value }) => item.value !== value
                            )
                        " />
                </el-select>
            </el-form-item>
            <el-form-item label="条件" prop="range">
                <el-select v-model="form.range" class="m-2">
                    <el-option v-for="item in rangeOptions" :key="item.value" :label="item.label" :value="item.value" />
                </el-select>
            </el-form-item>
            <el-form-item label="值" prop="value">
                <el-input-number v-model="form.value" controls-position="right" :controls="false">
                </el-input-number>
                <template v-if="needPercent">%</template>
            </el-form-item>
        </el-form>
        <template #footer>
            <el-button type="primary" @click="confirmHandle"> 确定 </el-button>
            <el-button @click="open = false"> 取消 </el-button>
        </template>
    </el-dialog>
</template>
<script setup>
import { ref, computed, reactive } from 'vue';
const props = defineProps({
    featureOptions: Array,
    visibleFeatureOptions: Array,
    rangeOptions: Array,
    frontStatus: Object,
});
const emits = defineEmits(['addFeature']);
const open = ref(false);
const formRef = ref();
const form = reactive({
    feature: '',
    range: '',
    value: '',
});
const rules = reactive({
    feature: [{ required: true, message: '请选择特征' }],
    range: [{ required: true, message: '请选择条件' }],
    value: [{ required: true, message: '请输入值' }],
});
const needPercent = computed(() => form.feature === 'missing_rate');

const confirmHandle = async () => {
    const formEl = formRef.value;
    if (!formEl) return;
    const valid = await formEl.validate();
    if (!valid) return;
    emits('addFeature', { ...form });
    open.value = false;
    formEl.resetFields();
};

defineExpose({
    open,
});
</script>
