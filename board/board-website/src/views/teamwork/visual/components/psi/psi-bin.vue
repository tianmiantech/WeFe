<!-- psi分箱配置组件 -->
<template>
    <el-form>
        <div class="psi-title" v-if="title">{{ title }}</div>
        <el-form-item label="分箱方式">
            <el-select
                :model-value="binValue.method"
                @change="selectChange"
                :disabled="disabled"
            >
                <el-option
                    v-for="item in options"
                    :key="item.value"
                    :label="item.label"
                    :value="item.value"
                />
            </el-select>
        </el-form-item>
        <el-form-item label="箱数" v-if="binValue.method !== 'custom'">
            <el-input-number
                :model-value="binValue.binNumber"
                :min="1"
                :disabled="disabled"
                @change="numberChange"
                controls-position="right"
            />
        </el-form-item>

        <!-- <div v-if="binValue.method === 'custom'" style="margin-top: 20px;"> -->
        <el-form-item label="分割点" v-if="binValue.method == 'custom'">
            <el-input
                :disabled="disabled"
                :modelValue="binValue.split_points"
                placeholder="请输入分割点"
                @input="splitChange"
            ></el-input>
            <p class="f12 pl10 pr10 color-danger">
                请填写分割点，用逗号隔开,如0-1之间等宽分五箱，请填入0,0.2,0.4,0.6,0.8,1
            </p></el-form-item
        >
        <!-- </div> -->
    </el-form>
</template>

<script setup>
import { ref } from 'vue';
const baseOptions = [
    { label: '等频', value: 'quantile' },
    { label: '等宽', value: 'bucket' },
    { label: '自定义', value: 'custom' },
];
const props = defineProps({
    disabled: Boolean,
    /** 值 */
    binValue: {
        type: Object,
        default: () => ({
            method: '',
            binNumber: 0,
            split_points: '',
        }),
    },
    /** 不允许选择的项 */
    filterMethod: {
        type: Array,
        default: () => [],
    },
    /** 标题 */
    title: {
        type: String,
        default: '分箱方式',
    },
});
const emit = defineEmits(['update:binValue']);
const options = ref(
    baseOptions.filter((item) => !props.filterMethod.includes(item.value))
);

const selectChange = (value) => {
    noticeUp({
        method: value,
    });
};

const numberChange = (value) => {
    noticeUp({
        binNumber: value,
    });
};

const splitChange = (value) => {
    noticeUp({
        split_points: value,
    });
};

const noticeUp = (params) => {
    emit('update:binValue', {
        ...props.binValue,
        ...params,
    });
};
</script>

<style lang="scss" scoped>
.psi-title {
    margin: 20px 0;
}
</style>
