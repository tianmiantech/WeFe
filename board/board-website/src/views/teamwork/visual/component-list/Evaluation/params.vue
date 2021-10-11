<template>
    <el-form
        ref="form"
        :model="vData.form"
        :disabled="disabled"
        v-loading="vData.loading"
        inline
    >
        <el-form-item label="评估类别：">
            <el-select v-model="vData.form.eval_type">
                <el-option
                    v-for="(model, index) in vData.evalTypes"
                    :key="index"
                    :label="model.text"
                    :value="model.value"
                />
            </el-select>
        </el-form-item>

        <el-form-item label="正标签类型：">
            <el-input v-model="vData.form.pos_label" />
        </el-form-item>
    </el-form>
</template>

<script>
    import { reactive } from 'vue';
    import dataStore from '../data-store-mixin';

    export default {
        name:  'Evaluation',
        props: {
            projectId:    String,
            flowId:       String,
            disabled:     Boolean,
            learningType: String,
            currentObj:   Object,
            jobId:        String,
            class:        String,
        },
        setup(props) {
            let vData = reactive({
                evalTypes: [
                    { value: 'binary',text: 'binary' },
                    { value: 'regression',text: 'regression' },
                    { value: 'multi',text: 'multi' },
                ],
                form: {
                    eval_type: 'binary',
                    pos_label: 1,
                },
                originForm: {
                    eval_type: 'binary',
                    pos_label: 1,
                },
            });

            let methods = {
                checkParams() {
                    return {
                        params: vData.form,
                    };
                },
            };

            const { $data, $methods } = dataStore.mixin({
                props,
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
