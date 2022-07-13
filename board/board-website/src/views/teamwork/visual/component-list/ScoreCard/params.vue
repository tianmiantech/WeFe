<template>
    <el-form
        ref="form"
        v-loading="vData.loading"
        :disabled="disabled"
        @submit.prevent
        inline
        label-width="64px"
        :model="vData.form"
    >
        <el-form-item label="基准分：" prop="pd0">
            <el-input
                v-model="vData.form.pd0"
                type="number"
                placeholder="10"
            />
        </el-form-item>

        <el-form-item label="pdo：" prop="pdo">
            <el-input
                v-model="vData.form.pdo"
                type="number"
                placeholder="50"
            />
        </el-form-item>
    </el-form>
</template>

<script>
    import { reactive } from 'vue';
    import dataStore from '../data-store-mixin';

    export default {
        name:  'ScoreCard',
        props: {
            projectId:    String,
            flowId:       String,
            disabled:     Boolean,
            learningType: String,
            currentObj:   Object,
            jobId:        String,
            class:        String,
        },
        mixins: [dataStore],
        setup(props) {
            const formData = {
                pd0: 10,
                pdo: 50,
            };

            let vData = reactive({
                originForm: { ...formData },
                form:       { ...formData },
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
