<template>
    <div class="board-form readonly-form">
        <h4 class="mb10">VertSecureBoost参数设置</h4>
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
                            placeholder="classification"
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
                            placeholder="0.1"
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
                            v-model.trim="vData.form.tree_param.max_depth"
                            placeholder="5"
                            :disabled="vData.form.other_param.work_mode === 'layered'"
                        />
                    </el-form-item>
                    <el-form-item label="特征随机采样比率：">
                        <el-input
                            v-model="vData.form.other_param.subsample_feature_rate"
                            placeholder="0.8"
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
                            placeholder="0.0001"
                        />
                    </el-form-item>
                    <el-form-item
                        prop="bin_num"
                        label="最大桶数量："
                    >
                        <el-input
                            v-model="vData.form.other_param.bin_num"
                            placeholder="50"
                        />
                    </el-form-item>
                    <el-form-item
                        prop="validation_freqs"
                        label="验证频次："
                    >
                        <el-input
                            v-model="vData.form.other_param.validation_freqs"
                            placeholder="10"
                        />
                    </el-form-item>
                    <el-form-item
                        prop="early_stopping_rounds"
                        label="提前结束的迭代次数："
                    >
                        <el-input
                            v-model="vData.form.other_param.early_stopping_rounds"
                            placeholder="5"
                        />
                    </el-form-item>

                    <el-form-item
                        prop="work_mode"
                        label="工作模式："
                    >
                        <el-select
                            v-model="vData.form.other_param.work_mode"
                            clearable
                        >
                            <el-option
                                label="普通模式"
                                value="normal"
                            />
                            <template v-if="vData.member_list.length === 2">
                                <el-option
                                    label="layered 模式"
                                    value="layered"
                                />
                            </template>
                            <el-option
                                label="skip 模式"
                                value="skip"
                            />
                            <el-option
                                label="dp 模式"
                                value="dp"
                            />
                        </el-select>
                    </el-form-item>
                    <template v-if="vData.form.other_param.work_mode === 'dp'">
                        <el-form-item label="隐私预算：">
                            <el-input v-model="vData.form.other_param.epsilon" />
                        </el-form-item>
                        <p
                            v-if="vData.form.other_param.epsilon && vData.form.other_param.bin_num"
                            style="padding-left:90px;"
                            class="f12"
                        >
                            有 {{ ((vData.form.other_param.bin_num - 1) / (Math.E ** vData.form.other_param.epsilon + vData.form.other_param.bin_num - 1) * 100).toFixed(2) }}% 的概率移动到其他箱中
                        </p>
                    </template>
                    <el-form-item
                        v-if="vData.form.other_param.work_mode === 'skip'"
                        label="单方每次构建树的数量："
                    >
                        <el-input v-model="vData.tree_num_per_member" />
                    </el-form-item>
                    <template v-if="vData.form.other_param.work_mode === 'layered'">
                        <el-form-item label="promoter深度：">
                            <el-input v-model="vData.promoter_depth" @change="methods.depthChange" />
                        </el-form-item>
                        <el-form-item label="provider深度：">
                            <el-input v-model="vData.provider_depth" @change="methods.depthChange" />
                        </el-form-item>
                    </template>
                </el-collapse-item>
                <el-collapse-item title="tree param" name="2">
                    <el-form-item label="正则项系数">
                        <el-input
                            v-model="vData.form.tree_param.criterion_params"
                            placeholder="支持 0.1,0.2 区间范围"
                            @input="methods.replaceComma"
                        />
                    </el-form-item>
                    <el-form-item label="分裂一个内部节点(非叶子节点)需要的最小样本：">
                        <el-input
                            v-model="vData.form.tree_param.min_sample_split"
                            placeholder="2"
                        />
                    </el-form-item>
                    <el-form-item label="每个叶子节点包含的最小样本数：">
                        <el-input
                            v-model="vData.form.tree_param.min_leaf_node"
                            placeholder="1"
                        />
                    </el-form-item>
                    <el-form-item label="单个拆分要达到的最小增益：">
                        <el-input
                            v-model="vData.form.tree_param.min_impurity_split"
                            placeholder="0.001"
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
                <el-collapse-item
                    v-if="vData.form.other_param.work_mode !== 'dp'"
                    title="encrypt param"
                    name="4"
                >
                    <el-form-item
                        prop="encrypt_param__method"
                        label="同态加密方法："
                    >
                        <el-select
                            v-model="vData.form.encrypt_param.method"
                            placeholder="Paillier"
                            clearable
                        >
                            <el-option
                                v-for="(model, index) in vData.encryptionTypeList"
                                :key="index"
                                :label="model.text"
                                :value="model.value"
                            />
                        </el-select>
                    </el-form-item>
                </el-collapse-item>
                <el-collapse-item title="cv param" name="5">
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
                <el-collapse-item title="grid search param" name="6">
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
                            :label="label"
                            :disabled="disabled"
                            :rule="rule"
                        />
                        <p :style="{ textAlign: 'center' }">
                            当前设置的超参会执行
                            {{ runTime }} 次模型训练，任务耗时会延长。
                        </p>
                        <p :style="{ textAlign: 'center' }">
                            任务执行完毕后会自动将最优参数回写到当前节点的参数中。
                        </p></template>
                </el-collapse-item>
            </el-collapse>
        </el-form>
    </div>
</template>

<script>
    import { getCurrentInstance, reactive, computed, watch } from 'vue';
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
        objective_param: {
            objective: 'cross_entropy',
            params:    '1.5',
        },
        encrypt_param: {
            method: 'Paillier',
        },
        cv_param: {
            n_splits: 5,
            shuffle:  true,
            need_cv:  false,
        },
        other_param: {
            task_type:              'classification',
            learning_rate:          0.1,
            subsample_feature_rate: 1.0,
            n_iter_no_change:       true,
            tol:                    0.0001,
            num_trees:              10,
            bin_num:                50,
            validation_freqs:       10,
            early_stopping_rounds:  5,
            work_mode:              'dp',
            epsilon:                3,
        },
    };
    const targetFuns =                 [
        { value: 'cross_entropy',text: 'cross_entropy' },
        { value: 'lse',text: 'lse' },
        { value: 'lae',text: 'lae' },
        { value: 'log_cosh',text: 'log_cosh' },
        { value: 'tweedie',text: 'tweedie' },
        { value: 'fair',text: 'fair' },
        { value: 'huber',text: 'huber' },
    ];


    export default {
        name:  'VertSecureBoost',
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
                member_list: [],
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
                encryptionTypeList: [
                    { value: '', text: '------' },
                    { value: 'Paillier', text: 'Paillier' },
                ],

                originForm:          { ...XGBoost },
                form:                { ...XGBoost },
                activeNames:         ['1'],
                tree_num_per_member: 1,
                promoter_depth:      1,
                provider_depth:      2,
            });

            let methods = {
                replaceComma(val) {
                    if (val.indexOf('，') !== -1) {
                        val = val.replace(/，/ig, ',');
                    }
                    vData.form.tree_param.criterion_params = val;
                },
                formatter(params) {
                    Object.assign(vData.form, params);
                    vData.tree_num_per_member = params.other_param.tree_num_per_member || 1;
                    vData.promoter_depth = params.other_param.promoter_depth || 1;
                    vData.provider_depth = params.other_param.provider_depth || 2;

                    if(Array.isArray(params.tree_param.criterion_params)) {
                        vData.form.tree_param.criterion_params = params.tree_param.criterion_params.join('');
                    }
                    if(Array.isArray(params.objective_param.params)) {
                        vData.form.objective_param.params = params.objective_param.params.join('');
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

                    if (code === 0 && data && data.params && data.params.tree_param.criterion_params) {
                        vData.promoter_depth = data.params.other_param.promoter_depth || 1;
                        vData.provider_depth = data.params.other_param.provider_depth || 2;
                        vData.form.cv_param = data.params.cv_param;
                        vData.form.encrypt_param = data.params.encrypt_param;
                        vData.form.objective_param = data.params.objective_param;
                        vData.form.other_param = data.params.other_param;
                        vData.form.tree_param = data.params.tree_param;
                        vData.form.tree_param.criterion_params = data.params.tree_param.criterion_params.join(',');
                        if(data.params.grid_search_param)
                            vData.form.grid_search_param = data.params.grid_search_param;
                    }
                },
                async getNodeData() {
                    const { code, data } = await $http.get({
                        url:    '/flow/dataset/info',
                        params: {
                            flow_id: props.flowId,
                        },
                    });

                    if (code === 0) {
                        if (data.flow_data_set_features.length) {
                            const members = data.flow_data_set_features[0].members || [];

                            // eslint-disable-next-line require-atomic-updates
                            vData.member_list = members;
                        }
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

                    if($params.other_param.work_mode === 'skip') {
                        $params.other_param.tree_num_per_member = vData.tree_num_per_member;
                    }

                    if($params.other_param.work_mode === 'layered') {
                        $params.other_param.promoter_depth = +vData.promoter_depth;
                        $params.other_param.provider_depth = +vData.provider_depth;
                        $params.tree_param.max_depth = +vData.promoter_depth + (+vData.provider_depth);
                        vData.form.tree_param.max_depth = +vData.promoter_depth + (+vData.provider_depth);
                    }

                    return {
                        params: $params,
                    };
                },
            };

            methods.getNodeData();

            const objectiveList = computed(() =>
                targetFuns.slice(...vData.form.other_param.task_type === 'classification' ?  [0, 1]: [1]),
            );

            watch(objectiveList, (p) => {
                vData.form.objective_param.objective = p[0].value;
            });
            
            const { $data, $methods } = dataStore.mixin({
                props,
                vData,
                methods,
            });

            vData = $data;
            methods = $methods;

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
    .board-form-item{
        margin-bottom: 10px;
        :deep(.board-form-item__label){
            max-width:200px;
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
