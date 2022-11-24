<template>
    <el-form-item :label="label">
        <el-tag
            v-for="tag in dynamicTags"
            :key="tag"
            :closable="!disabled"
            :disable-transitions="false"
            @close="handleClose(tag)"
            :style="{ margin: '8px' }"
        >
            {{ tag }}
        </el-tag>
        <template v-if="inputVisible">
            <el-select v-if="items" @change="selectChangeHandle">
                <el-option
                    v-for="{ value, text } in items.filter(
                        (each) => !dynamicTags.includes(each.value)
                    )"
                    :key="value"
                    :label="text"
                    :value="value"
                />
            </el-select>
            <el-input
                v-else
                :style="{ width: '60px' }"
                ref="InputRef"
                v-model="inputValue"
                size="small"
                @keyup.enter="handleInputConfirm"
                @blur="handleInputConfirm"
            />
        </template>
        <el-button
            v-else
            class="button-new-tag ml-1"
            size="small"
            @click="showInput"
        >
            + 添加
        </el-button>
    </el-form-item>
</template>
<script setup>
    import { nextTick, ref, watch } from 'vue';
    import { ElInput, ElMessage } from 'element-plus';

    const props = defineProps({
        label:      String,
        modelValue: Array,
        disabled:   Boolean,
        items:      Array,
        rule:       {
            type:    Object,
            default: () => ({}),
        },
    });
    const emit = defineEmits(['update:modelValue']);

    const inputValue = ref('');
    const dynamicTags = ref(props.modelValue);
    const inputVisible = ref(false);
    const InputRef = ref();

    watch(dynamicTags, (value) => {
        emit('update:modelValue', value);
    });

    const handleClose = (tag) => {
        dynamicTags.value.splice(dynamicTags.value.indexOf(tag), 1);
    };

    const showInput = async () => {
        inputVisible.value = true;
        await nextTick();
        if (InputRef.value) InputRef.value.input.focus();
    };

    const handleInputConfirm = () => {
        const {
            rule: { message, checkFun },
        } = props;

        if (!inputValue.value) {
            inputVisible.value = false;
            return;
        }
        if (checkFun(inputValue.value)) {
            dynamicTags.value = [...dynamicTags.value, inputValue.value];
            inputVisible.value = false;
            inputValue.value = '';
        } else {
            ElMessage.error(message);
        }
    };

    const selectChangeHandle = (e) => {
        if (e) {
            dynamicTags.value = [...dynamicTags.value, e];
        }
        inputVisible.value = false;
    };

</script>
