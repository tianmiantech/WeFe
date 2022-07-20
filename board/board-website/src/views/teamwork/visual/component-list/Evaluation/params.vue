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


        <el-form-item label="是否计算分布：">
            <el-switch
                v-model="vData.form.prob_need_to_bin"
                active-color="#13ce66">
            </el-switch>
        </el-form-item>

        <el-form-item v-if="vData.form.prob_need_to_bin">
            <el-select 
                v-model="vData.form.bin_method"
                placeholder="请选择"
                disabled=true
                style="width:86px;">
                <el-option
                    v-for="item in vData.bin_method"
                    :key="item.value"
                    :label="item.text"
                    :value="item.value">
                </el-option>
            </el-select>
            <el-input-number
                v-model="vData.form.bin_num"
                type="number"
                :min="10"
                :max="20"
                controls-position="right"
            />箱
        </el-form-item>
    </el-form>
</template>

<script>
    import { reactive, getCurrentInstance } from 'vue';
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
            const { appContext } = getCurrentInstance();
            const { $http } = appContext.config.globalProperties;

            let vData = reactive({
                bin_method: [
                    { value: 'bucket',text: '等宽' },
                ],
                evalTypes: [
                    { value: 'binary',text: 'binary' },
                    { value: 'regression',text: 'regression' },
                    { value: 'multi',text: 'multi' },
                ],
                form: {
                    eval_type:        'binary',
                    pos_label:        1,
                    prob_need_to_bin: false,
                    bin_num:          10,
                    bin_method:       'bucket',
                },
                originForm: {
                    eval_type:        'binary',
                    pos_label:        1,
                    prob_need_to_bin: false,
                    bin_num:          10,
                    bin_method:       'bucket',
                },
            });

            let methods = {
                checkParams() {
                    return {
                        params: vData.form,
                    };
                },
                async getNodeDetail(model) {
                    const { code, data } = await $http.get({
                        url:    '/project/flow/node/detail',
                        params: {
                            nodeId:  model.id,
                            flow_id: props.flowId,
                        },
                    });

                    if (code === 0 && data && data.params && Object.keys(data.params).length) {
                        vData.form = data.params;
                    }
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

<style lang="scss" scoped>
    .el-input-number{
        width: 104px;
        margin:0 10px;
        :deep(.el-input__inner){
            padding-left:5px;
            padding-right: 40px;
        }
    }
</style>
