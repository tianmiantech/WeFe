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
                active-color="#13ce66"
            />
        </el-form-item>

        <el-form-item v-if="vData.form.prob_need_to_bin">
            <el-select
                v-model="vData.form.bin_method"
                placeholder="请选择"
                style="width: 84px;"
            >
                <el-option
                    v-for="item in vData.bin_method"
                    :key="item.value"
                    :label="item.text"
                    :value="item.value"
                />
            </el-select>
            <el-input-number
                v-model="vData.form.bin_num"
                type="number"
                controls-position="right"
            />箱
            <span style="color: #999;">（建议设置10-20箱）</span>
        </el-form-item>
        <el-form-item v-if="vData.exitVertComponent" label="是否启用PSI分箱（预测概览概率/评分）">
            <el-switch v-model="vData.form.need_psi" active-color="#13ce66"/>
        </el-form-item>
    </el-form>

    <psi-bin
        v-if="vData.form.need_psi"
        title=""
        v-model:binValue="vData.binValue"
        :disabled="disabled"
        :filterMethod="['quantile']"
    />
</template>

<script>
    import { reactive, getCurrentInstance } from 'vue';
    import dataStore from '../data-store-mixin';
    import psiBin from '../../components/psi/psi-bin';
    import { checkExitVertModelComponet } from '@src/service';
    import { psiCustomSplit, replace } from '../common/utils';

    export default {
        name:       'Evaluation',
        components: { psiBin },
        props:      {
            projectId:          String,
            flowId:             String,
            disabled:           Boolean,
            learningType:       String,
            currentObj:         Object,
            jobId:              String,
            class:              String,
            ootModelFlowNodeId: String,
            ootJobId:           String,
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
                    need_psi:         false,
                },
                originForm: {
                    eval_type:        'binary',
                    pos_label:        1,
                    prob_need_to_bin: false,
                    need_psi:         false,
                    bin_num:          10,
                    bin_method:       'bucket',
                },
                binValue: {
                    method:       'bucket',
                    binNumber:    6,
                    split_points: '',
                },
                exitVertComponent: false,
            });

            let methods = {
                checkParams() {
                const { form, binValue, exitVertComponent } = vData;
                const { method, binNumber, split_points } = binValue;
                const isCustom = method === 'custom';
                const array = replace(split_points)
                    .replace(/，/g, ',')
                    .replace(/,$/, '')
                    .split(',');

                if (isCustom && !psiCustomSplit(array)) {
                    return false;
                }
                const re = array.map(parseFloat);

                re.sort((a, b) => a - b);

                const { eval_type, pos_label, prob_need_to_bin, bin_num, bin_method, need_psi } = form;

                return {
                    params: {
                        eval_type,
                        pos_label,
                        score_param: {
                            prob_need_to_bin,
                            bin_num,
                            bin_method,
                        },
                        psi_param: {
                            need_psi,
                            bin_method: exitVertComponent ? method : undefined,
                            bin_num:
                                exitVertComponent && !isCustom
                                    ? binNumber
                                    : undefined,
                            split_points: exitVertComponent && isCustom
                                ? [...new Set([0, ...re, 1])]
                                : undefined,
                        },
                    },
                };
            },
                /**
                 * 判断是否展示psi组件
                 */
                checkExistVertModel(model){
                    const { ootModelFlowNodeId,flowId,ootJobId } = props;

                    checkExitVertModelComponet({
                        nodeId:      model.id,
                        modelNodeId: ootModelFlowNodeId,
                        jobId:       ootJobId,
                        flowId,
                    }).then((bool = false)=>{
                        vData.exitVertComponent = bool;
                    });
                },
                async getNodeDetail(model) {
                    methods.checkExistVertModel(model);
                    const { code, data } = await $http.get({
                        url:    '/project/flow/node/detail',
                        params: {
                            nodeId:  model.id,
                            flow_id: props.flowId,
                        },
                    });

                    if (code === 0 && data && data.params && Object.keys(data.params).length) {
                        vData.form = data.params;
                        const { eval_type, pos_label, psi_param, score_param } =
                        data.params;
                        Object.assign(vData.form, {
                            eval_type,
                            pos_label,
                        });
                        if(psi_param) {
                            const { need_psi, bin_method, bin_number, split_points } = psi_param;
                            vData.form.need_psi = need_psi;
                            if(need_psi) {
                                vData.binValue = {
                                    method:       bin_method,
                                    binNumber:    bin_number,
                                    split_points: split_points? split_points.join() : '',
                                };
                            }
                        }
                        if (score_param) {
                        const { prob_need_to_bin, bin_num, bin_method } =
                            score_param;
                        Object.assign(vData.form, {
                            prob_need_to_bin,
                            bin_num,
                            bin_method,
                        });
                    }
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
    }
</style>
