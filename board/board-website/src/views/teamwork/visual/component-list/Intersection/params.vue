<template>
    <el-form
        ref="form"
        v-loading="vData.loading"
        :disabled="disabled"
        @submit.prevent
        inline
    >
        <el-form-item label="对齐算法">
            <el-select
                v-model="vData.form.intersect_method"
                placeholder="请选择类型"
                style="width:150px;"
            >
                <el-option
                    label="dh"
                    value="dh"
                />
            </el-select>
        </el-form-item>

        <el-form-item label="是否保存对齐后数据">
            <el-switch
                v-model="vData.form.save_dataset"
                inactive-color="#ff4949"
                active-color="#13ce66"
            />
        </el-form-item>
    </el-form>
</template>

<script>
    import { reactive } from 'vue';
    import dataStore from '../data-store-mixin';

    export default {
        name:  'Intersection',
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
        setup(props, context) {
            let vData = reactive({
                originForm: {
                    intersect_method: 'dh',
                    save_dataset:     true,
                },
                form: {
                    intersect_method: 'dh',
                    save_dataset:     true,
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
