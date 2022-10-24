<template>
    <el-dialog
        width="700px"
        v-model="data.showDialog"
        :close-on-click-modal="false"
        title="特征筛选 - CV/IV"
        destroy-on-close
        append-to-body
    >
        <el-form inline @submit.prevent>
            <el-form-item>
                <label class="el-form-item__label">
                    CV: (<i class="iconfont icon-more-than" />)
                </label>
                <el-input v-model="params.cv" style="width: 100px" />
            </el-form-item>
            <el-form-item>
                <label class="el-form-item__label">
                    IV: (<i class="iconfont icon-more-than" />)
                </label>
                <el-input v-model="params.iv" style="width: 100px" />
            </el-form-item>
            <el-button class="ml10 mr10" @click="methods.filter">
                筛选
            </el-button>
            筛选结果 ({{ data.list.length }}/{{ data.total || 0 }})
        </el-form>

        <el-table :data="data.list" max-height="600px" border stripe>
            <el-table-column prop="name" label="特征" width="100" />
            <el-table-column prop="member_name" label="所属成员" width="140" />
            <el-table-column prop="iv" label="IV" />
            <el-table-column prop="cv" label="CV" />
        </el-table>

        <div class="text-r mt20">
            <el-button
                type="primary"
                :disabled="data.list.length === 0"
                @click="methods.confirm"
            >
                确定
            </el-button>
        </div>
    </el-dialog>
</template>

<script setup>
import { ref, reactive } from 'vue';
import useFeatureSelection from './useFeatureSelection';

const { show, confirm, filter, data } = useFeatureSelection();
const props = defineProps({
    jobId: String,
    flowId: String,
    flowNodeId: String,
});
const params = reactive({
    select_type: 'cv_iv',
    cv: 0.1,
    iv: 0.02,
});
const emit = defineEmits(['confirmCheck']);
defineExpose({
    show,
});
</script>
<script>
import { reactive } from 'vue';
import featureSelection from './feature-selection-mixin';

export default {
    props: {
        ...featureSelection.props,
    },
    emits: [...featureSelection.emits],
    setup(props, context) {
        let data = reactive({
            params: {
                select_type: 'cv_iv',
                cv: 0.1,
                iv: 0.02,
            },
        });

        let methods = {};

        const { $data, $methods } = featureSelection.mixin({
            data,
            methods,
            props,
            context,
        });

        data = $data;
        methods = $methods;

        return {
            data,
            methods,
        };
    },
};
</script>
