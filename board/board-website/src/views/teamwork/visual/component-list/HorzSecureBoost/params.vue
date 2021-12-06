<template>
    <div class="el-form readonly-form">
        <h4 class="mb10">HorzSecureBoost参数设置</h4>
        <el-form
            ref="form"
            :model="vData.form"
            :disabled="disabled"
            label-width="130px"
            @submit.prevent
            inline
        >
            <el-collapse v-model="vData.activeNames">
                <el-collapse-item title="模型参数" name="1">
                    <el-form-item label="任务类型：">
                        <el-select
                            v-model="vData.form.other_param.task_type"
                            clearable
                        >
                            <el-option
                                v-for="(model, index) in vData.taskTypeList"
                                :key="index"
                                :label="model.text"
                                :value="model.value"
                            />
                        </el-select>
                    </el-form-item>
                    <el-form-item label="学习率">
                        <el-input
                            v-model="vData.form.other_param.learning_rate"
                            placeholder="learning_rate"
                        />
                    </el-form-item>
                    <el-form-item label="最大树数量">
                        <el-input
                            v-model="vData.form.other_param.num_trees"
                            placeholder="num_trees"
                        />
                    </el-form-item>
                    <el-form-item label="树的最大深度">
                        <el-input
                            v-model="vData.form.tree_param.max_depth"
                            placeholder="max_depth"
                        />
                    </el-form-item>
                    <el-form-item label="特征随机采样比率">
                        <el-input
                            v-model="vData.form.other_param.subsample_feature_rate"
                            placeholder="subsample_feature_rate"
                        />
                    </el-form-item>

                    <el-form-item label="n次迭代没变化是否停止：">
                        <el-radio
                            v-model="vData.form.other_param.n_iter_no_change"
                            :label="true"
                        >
                            是
                        </el-radio>
                        <el-radio
                            v-model="vData.form.other_param.n_iter_no_change"
                            :label="false"
                        >
                            否
                        </el-radio>
                    </el-form-item>

                    <el-form-item
                        prop="tol"
                        label="收敛阀值"
                    >
                        <el-input
                            v-model="vData.form.other_param.tol"
                            placeholder="tol"
                        />
                    </el-form-item>
                    <el-form-item
                        prop="bin_num"
                        label="最大桶数量"
                    >
                        <el-input
                            v-model="vData.form.other_param.bin_num"
                            placeholder="bin_num"
                        />
                    </el-form-item>
                </el-collapse-item>
                <el-collapse-item title="tree param" name="2">
                    <el-form-item label="标准函数">
                        <el-input
                            v-model="vData.form.tree_param.criterion_method"
                            placeholder="criterion_method"
                        />
                    </el-form-item>
                    <el-form-item label="标准参数">
                        <el-input
                            v-model="vData.form.tree_param.criterion_params"
                            placeholder="criterion_params"
                        />
                    </el-form-item>
                    <el-form-item label="分裂一个内部节点(非叶子节点)需要的最小样本">
                        <el-input
                            v-model="vData.form.tree_param.min_sample_split"
                            placeholder="min_sample_split"
                        />
                    </el-form-item>
                    <el-form-item label="每个叶子节点包含的最小样本数">
                        <el-input
                            v-model="vData.form.tree_param.min_leaf_node"
                            placeholder="min_leaf_node"
                        />
                    </el-form-item>
                    <el-form-item label="单个拆分的要达到的最小增益">
                        <el-input
                            v-model="vData.form.tree_param.min_impurity_split"
                            placeholder="min_impurity_split"
                        />
                    </el-form-item>
                    <el-form-item label="可拆分的最大并样本量">
                        <el-input
                            v-model="vData.form.tree_param.max_split_nodes"
                            placeholder="max_split_nodes"
                        />
                    </el-form-item>
                </el-collapse-item>
                <el-collapse-item title="objective param" name="3">
                    <el-form-item label="目标函数：">
                        <el-select
                            v-model="vData.form.objective_param.objective"
                            placeholder="cross_entropy"
                            clearable
                        >
                            <el-option
                                v-for="(model, index) in vData.objectiveList"
                                :key="index"
                                :label="model.text"
                                :value="model.value"
                            />
                        </el-select>
                    </el-form-item>
                    <el-form-item label="学习目标参数">
                        <el-input
                            v-model="vData.form.objective_param.params"
                            placeholder="1.5"
                        />
                    </el-form-item>
                </el-collapse-item>
                <el-collapse-item title="cv param" name="4">
                    <el-form-item label="在KFold中使分割符次数：">
                        <el-input
                            v-model.number="vData.form.cv_param.n_splits"
                            placeholder="n_splits"
                            @change="methods.watchNum($event, false)"
                        />
                    </el-form-item>
                    <el-form-item label="在KFold之前是否进行洗牌：">
                        <el-radio
                            v-model="vData.form.cv_param.shuffle"
                            :label="true"
                        >
                            是
                        </el-radio>
                        <el-radio
                            v-model="vData.form.cv_param.shuffle"
                            :label="false"
                        >
                            否
                        </el-radio>
                    </el-form-item>

                    <el-form-item label="是否需要进行此模块：">
                        <el-radio
                            v-model="vData.form.cv_param.need_cv"
                            :label="true"
                        >
                            是
                        </el-radio>
                        <el-radio
                            v-model="vData.form.cv_param.need_cv"
                            :label="false"
                        >
                            否
                        </el-radio>
                    </el-form-item>
                </el-collapse-item>
            </el-collapse>
        </el-form>
    </div>
</template>

<script>
    import { reactive } from 'vue';
    import dataStore from '../data-store-mixin';

    const XGBoost = {
        tree_param: {
            criterion_method:   'xgboost',
            criterion_params:   0.1,
            max_depth:          5,
            min_sample_split:   2,
            min_leaf_node:      1,
            min_impurity_split: 0.001,
            max_split_nodes:    65536,
        },
        encrypt_param: {
            method: 'Paillier',
        },
        cv_param: {
            n_splits: 5,
            shuffle:  true,
            need_cv:  false,
        },
        objective_param: {
            objective: 'cross_entropy',
            params:    '1.5',
        },
        other_param: {
            task_type:              'classification',
            learning_rate:          0.1,
            n_iter_no_change:       true,
            validation_freqs:       10,
            subsample_feature_rate: 0.8,
            early_stopping_rounds:  5,
            num_trees:              100,
            bin_num:                50,
            tol:                    0.0001,
        },
    };

    export default {
        name:  'HorzSecureBoost',
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
                penaltyList: [
                    { value: 'L1',text: 'L1' },
                    { value: 'L2',text: 'L2' },
                ],
                initMethodList: [
                    { value: 'random_uniform',text: 'random_uniform' },
                    { value: 'random_normal',text: 'random_normal' },
                    { value: 'ones',text: 'ones' },
                    { value: 'zeros',text: 'zeros' },
                    { value: 'const',text: 'const' },
                ],
                optimizerList: [
                    { value: 'sgd',text: 'sgd' },
                    { value: 'rmsprop',text: 'rmsprop' },
                    { value: 'adam',text: 'adam' },
                    { value: 'nesterov_momentum_sgd',text: 'nesterov_momentum_sgd' },
                    { value: 'sqn',text: 'sqn' },
                    { value: 'adagrad',text: 'adagrad' },
                ],
                earlyStopList: [
                    { value: 'diff',text: 'diff' },
                    { value: 'weight_diff',text: 'weight_diff' },
                    { value: 'abs',text: 'abs' },
                ],
                multiClassList: [
                    { value: 'ovr',text: 'ovr' },
                    { value: 'ovo',text: 'ovo' },
                ],
                taskTypeList: [
                    { value: 'classification',text: 'classification' },
                    { value: 'regression',text: 'regression' },
                ],
                objectiveList: [
                    { value: 'cross_entropy',text: 'cross_entropy' },
                    { value: 'lse',text: 'lse' },
                    { value: 'lae',text: 'lae' },
                    { value: 'log_cosh',text: 'log_cosh' },
                    { value: 'tweedie',text: 'tweedie' },
                    { value: 'fair',text: 'fair' },
                    { value: 'huber',text: 'huber' },
                ],
                encryptionTypeList: [
                    { value: '', text: '------' },
                    { value: 'Paillier', text: 'Paillier' },
                    { value: 'IterativeAffine', text: 'IterativeAffine' },
                ],

                originForm:  { ...XGBoost },
                form:        { ...XGBoost },
                activeNames: ['1'],
            });

            let methods = {
                formatter(params) {
                    vData.form = {
                        ...params,
                    };
                    if(Array.isArray(params.tree_param.criterion_params)) {
                        vData.form.tree_param.criterion_params = params.tree_param.criterion_params.join('');
                    }
                    if(Array.isArray(params.objective_param.params)) {
                        vData.form.objective_param.params = params.objective_param.params.join('');
                    }
                },
                checkParams() {
                    const $params = {
                        ...JSON.parse(JSON.stringify(vData.form)),
                    };
                    const {
                        tree_param: {
                            criterion_params,
                        },
                        objective_param: {
                            params,
                        },
                    } = vData.form;

                    if(String(criterion_params).includes(',')) {
                        $params.tree_param.criterion_params = criterion_params.split(',').map(str => +str);
                    } else {
                        $params.tree_param.criterion_params = [+criterion_params];
                    }
                    if(params.includes(',')) {
                        $params.objective_param.params = params.split(',').map(str => +str);
                    } else {
                        $params.objective_param.params = [+params];
                    }

                    return {
                        params: $params,
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

<style lang="scss" scoped>
    .el-form-item{
        margin-bottom: 10px;
        :deep(.el-form-item__label){
            text-align: left;
            line-height: 16px;
            padding-bottom:6px;
            font-size: 12px;
            display: block;
        }
    }
    .el-collapse-item {
        :deep(.el-collapse-item__header) {
            color: #438bff;
            font-size: 16px;
            padding-left: 5px;
            .el-collapse-item__arrow {
                color: #999;
            }
        }
        :deep(.el-collapse-item__wrap) {
            padding: 0 10px;
        }
    }
    .is-active {
        border-right: 1px solid #f1f1f1;
        border-left: 1px solid #f1f1f1;
    }
    .readonly-form:before {
        position: unset !important;
    }
</style>
