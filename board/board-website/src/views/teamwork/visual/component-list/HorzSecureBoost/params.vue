<template>
    <div class="board-form readonly-form">
        <h4 class="mb10">HorzSecureBoost参数设置</h4>
        <el-form
            ref="form"
            class="flex-form"
            :model="vData.form"
            :disabled="disabled"
            @submit.prevent
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
                    <el-form-item label="学习率：">
                        <el-input
                            v-model="vData.form.other_param.learning_rate"
                            placeholder="learning_rate"
                        />
                    </el-form-item>
                    <el-form-item label="最大树数量：">
                        <el-input
                            v-model="vData.form.other_param.num_trees"
                            placeholder="num_trees"
                        />
                    </el-form-item>
                    <el-form-item label="树的最大深度：">
                        <el-input
                            v-model="vData.form.tree_param.max_depth"
                            placeholder="max_depth"
                        />
                    </el-form-item>
                    <el-form-item label="特征随机采样比率：">
                        <el-input
                            v-model="
                                vData.form.other_param.subsample_feature_rate
                            "
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
                        label="收敛阈值："
                    >
                        <el-input
                            v-model="vData.form.other_param.tol"
                            placeholder="tol"
                        />
                    </el-form-item>
                    <el-form-item
                        prop="bin_num"
                        label="最大桶数量："
                    >
                        <el-input
                            v-model="vData.form.other_param.bin_num"
                            placeholder="bin_num"
                        />
                    </el-form-item>
                </el-collapse-item>
                <el-collapse-item title="tree param" name="2">
                    <el-form-item label="L2 正则项系数：">
                        <el-input
                            v-model="vData.form.tree_param.criterion_method"
                            placeholder="如 xgboost"
                        />
                    </el-form-item>
                    <el-form-item label="标准参数：">
                        <el-input
                            v-model="vData.form.tree_param.criterion_params"
                            placeholder="criterion_params"
                            @input="methods.replaceComma"
                        />
                    </el-form-item>
                    <el-form-item label="分裂一个内部节点(非叶子节点)需要的最小样本：">
                        <el-input
                            v-model="vData.form.tree_param.min_sample_split"
                            placeholder="min_sample_split"
                        />
                    </el-form-item>
                    <el-form-item label="每个叶子节点包含的最小样本数：">
                        <el-input
                            v-model="vData.form.tree_param.min_leaf_node"
                            placeholder="min_leaf_node"
                        />
                    </el-form-item>
                    <el-form-item label="单个拆分要达到的最小增益：">
                        <el-input
                            v-model="vData.form.tree_param.min_impurity_split"
                            placeholder="min_impurity_split"
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
                                v-for="(model, index) in objectiveList"
                                :key="index"
                                :label="model.text"
                                :value="model.value"
                            />
                        </el-select>
                    </el-form-item>
                    <el-form-item label="学习目标参数：">
                        <el-input
                            v-model="vData.form.objective_param.params"
                            placeholder="1.5"
                        />
                    </el-form-item>
                </el-collapse-item>
                <el-collapse-item title="cv param" name="4">
                    <el-form-item label="是否执行cv：">
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
                    <el-form-item label="KFold分割次数：">
                        <el-input
                            v-model.number="vData.form.cv_param.n_splits"
                            placeholder="n_splits"
                            @change="methods.watchNum($event, false)"
                        />
                    </el-form-item>
                    <el-form-item label="KFold之前洗牌：">
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
                </el-collapse-item>
                <el-collapse-item title="grid search param" name="5">
                    <el-form-item label="是否开启网格搜索">
                        <el-radio-group
                            v-model="
                                vData.form.grid_search_param.need_grid_search
                            "
                        >
                            <el-radio :label="true">是</el-radio>
                            <el-radio :label="false">否</el-radio>
                        </el-radio-group>
                    </el-form-item>
                    <template
                        v-if="vData.form.grid_search_param.need_grid_search"
                    >
                        <MultiGridSearchTag
                            v-for="{ key, label, rule } in xgboostGrid"
                            :key="key"
                            v-model="vData.form.grid_search_param[key]"
                            :disabled="disabled"
                            :label="label"
                            :rule="rule"
                        />
                        <p :style="{ textAlign: 'center' }">
                            当前设置的超参会执行
                            {{ runTime }} 次模型训练，任务耗时会延长。
                        </p>
                        <p :style="{ textAlign: 'center' }">
                            任务执行完毕后会自动将最优参数回写到当前节点的参数中。
                        </p>
                    </template>
                </el-collapse-item>
            </el-collapse>
        </el-form>
    </div>
</template>

<script>
    import { reactive, getCurrentInstance, computed, watch } from 'vue';
    import MultiGridSearchTag from '../../../../../components/Common/MultiGridSearchTag.vue';
    import gridSearchParams from '../../../../../assets/js/const/gridSearchParams';
    import dataStore from '../data-store-mixin';

    const grid_search_param = {
        need_grid_search: false,
    };
    const xgboostGrid = gridSearchParams.xgboost;

    xgboostGrid.forEach(({ key }) => (grid_search_param[key] = []));
    const XGBoost = {
        grid_search_param,
        tree_param: {
            criterion_params:   0.1,
            max_depth:          3,
            min_sample_split:   2,
            min_leaf_node:      1,
            min_impurity_split: 0.001,
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
            subsample_feature_rate: 1.0,
            early_stopping_rounds:  5,
            num_trees:              10,
            bin_num:                50,
            tol:                    0.0001,
        },
    };
    const targetFuns = [
        { value: 'cross_entropy',text: 'cross_entropy' },
        { value: 'lse',text: 'lse' },
        { value: 'lae',text: 'lae' },
        { value: 'log_cosh',text: 'log_cosh' },
        { value: 'tweedie',text: 'tweedie' },
        { value: 'fair',text: 'fair' },
        { value: 'huber',text: 'huber' },
    ];

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
        components: { MultiGridSearchTag },
        setup(props) {
            const { appContext } = getCurrentInstance();
            const { $http } = appContext.config.globalProperties;

            let vData = reactive({
                penaltyList: [
                    { value: 'L1', text: 'L1' },
                    { value: 'L2', text: 'L2' },
                ],
                initMethodList: [
                    { value: 'random_uniform', text: 'random_uniform' },
                    { value: 'random_normal', text: 'random_normal' },
                    { value: 'ones', text: 'ones' },
                    { value: 'zeros', text: 'zeros' },
                    { value: 'const', text: 'const' },
                ],
                optimizerList: [
                    { value: 'sgd', text: 'sgd' },
                    { value: 'rmsprop', text: 'rmsprop' },
                    { value: 'adam', text: 'adam' },
                    {
                        value: 'nesterov_momentum_sgd',
                        text:  'nesterov_momentum_sgd',
                    },
                    { value: 'sqn', text: 'sqn' },
                    { value: 'adagrad', text: 'adagrad' },
                ],
                earlyStopList: [
                    { value: 'diff', text: 'diff' },
                    { value: 'weight_diff', text: 'weight_diff' },
                    { value: 'abs', text: 'abs' },
                ],
                multiClassList: [
                    { value: 'ovr', text: 'ovr' },
                    { value: 'ovo', text: 'ovo' },
                ],
                taskTypeList: [
                    { value: 'classification', text: 'classification' },
                    { value: 'regression', text: 'regression' },
                ],
                encryptionTypeList: [
                    { value: '', text: '------' },
                    { value: 'Paillier', text: 'Paillier' },
                ],
                originForm:  { ...XGBoost },
                form:        { ...XGBoost },
                activeNames: ['1'],
            });

            let methods = {
                replaceComma(val) {
                    if (val.indexOf('，') !== -1) {
                        val = val.replace(/，/gi, ',');
                    }
                    vData.form.tree_param.criterion_params = val;
                },
                formatter(params) {
                    vData.form = {
                        ...params,
                    };
                    if (Array.isArray(params.tree_param.criterion_params)) {
                        vData.form.tree_param.criterion_params =
                            params.tree_param.criterion_params.join('');
                    }
                    if (Array.isArray(params.objective_param.params)) {
                        vData.form.objective_param.params =
                            params.objective_param.params.join('');
                    }
                },
                async getNodeDetail(model) {
                    const { code, data } = await $http.get({
                        url:    '/project/flow/node/detail',
                        params: {
                            nodeId:  model.id,
                            flow_id: props.flowId,
                        },
                    });

                    if (
                        code === 0 &&
                        data &&
                        data.params &&
                        Object.keys(data.params).length
                    ) {
                        vData.form.tree_param.criterion_params = data.params
                            .tree_param.criterion_params
                            ? data.params.tree_param.criterion_params.join(',')
                            : '';
                        // vData.form = data.params;
                        vData.form = Object.assign(vData.form, data.params);
                    }
                },
                checkParams() {
                    const $params = {
                        ...JSON.parse(JSON.stringify(vData.form)),
                    };
                    const {
                        tree_param: { criterion_params },
                        objective_param: { params },
                        grid_search_param,
                    } = vData.form;
                    const { need_grid_search } = grid_search_param;

                    if (need_grid_search) {
                    // 网格搜索校验
                    }
                    if (String(criterion_params).includes(',')) {
                        $params.tree_param.criterion_params = String(
                            criterion_params,
                        )
                            .split(',')
                            .map((str) => +str);
                    } else {
                        $params.tree_param.criterion_params = [+criterion_params];
                    }
                    if (params.includes(',')) {
                        $params.objective_param.params = params
                            .split(',')
                            .map((str) => +str);
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

            const objectiveList = computed(() => 
                targetFuns.slice(...vData.form.other_param.task_type === 'classification' ?  [0, 1]: [1]),
            );

            watch(objectiveList, (p) => {
                vData.form.objective_param.objective = p[0].value;
            });
            const runTime = computed(() =>
                Object.values(vData.form.grid_search_param).reduce(
                    (acc, cur) => acc * (cur.length || 1),
                    1,
                ),
            );

            return {
                vData,
                methods,
                objectiveList,
                xgboostGrid,
                runTime,
            };
        },
    };
</script>

<style lang="scss" scoped>
.board-form-item {
    margin-bottom: 10px;
    :deep(.board-form-item__label) {
        flex: 1;
    }
}
.board-collapse-item {
    :deep(.board-collapse-item__header) {
        color: #438bff;
        font-size: 16px;
        padding-left: 5px;
        .board-collapse-item__arrow {
            color: #999;
        }
    }
    :deep(.board-collapse-item__wrap) {
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
