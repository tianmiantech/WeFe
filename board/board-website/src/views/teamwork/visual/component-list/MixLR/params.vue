<template>
    <div class="board-form">
        <h4 class="mb10">MixLR参数设置</h4>
        <el-form
            ref="form"
            class="flex-form"
            :model="vData.form"
            :disabled="disabled"
            @submit.prevent
        >
            <el-collapse v-model="vData.activeNames">
                <el-collapse-item title="模型参数" name="1">
                    <el-form-item label="惩罚方式：">
                        <el-select
                            v-model="vData.form.other_param.penalty"
                            clearable
                        >
                            <el-option
                                v-for="(model, index) in vData.penaltyList"
                                :key="index"
                                :label="model.text"
                                :value="model.value"
                            />
                        </el-select>
                    </el-form-item>
                    <el-form-item
                        prop="tol"
                        label="收敛容忍度："
                    >
                        <el-input
                            v-model="vData.form.other_param.tol"
                            placeholder="tol"
                        />
                    </el-form-item>
                    <el-form-item
                        prop="alpha"
                        label="惩罚项系数："
                    >
                        <el-input
                            v-model="vData.form.other_param.alpha"
                            placeholder="alpha"
                        />
                    </el-form-item>
                    <el-form-item label="优化算法：">
                        <el-select
                            v-model="vData.form.other_param.optimizer"
                            clearable
                        >
                            <el-option
                                v-for="(model, index) in vData.optimizerList"
                                :key="index"
                                :label="model.text"
                                :value="model.value"
                            />
                        </el-select>
                    </el-form-item>
                    <el-form-item
                        prop="batch_size"
                        label="批量大小："
                    >
                        <el-input
                            v-model="vData.form.other_param.batch_size"
                            placeholder="batch_size"
                        />
                    </el-form-item>
                    <el-form-item
                        prop="learning_rate"
                        label="学习率："
                    >
                        <el-input
                            v-model="vData.form.other_param.learning_rate"
                            placeholder="learning_rate"
                        />
                    </el-form-item>
                    <el-form-item
                        prop="max_iter"
                        label="最大迭代次数："
                    >
                        <el-input
                            v-model="vData.form.other_param.max_iter"
                            placeholder="max_iter"
                        />
                    </el-form-item>

                    <el-form-item label="判断收敛性与否的方法：">
                        <el-select
                            v-model="vData.form.other_param.early_stop"
                            clearable
                        >
                            <el-option
                                v-for="(model, index) in vData.earlyStopList"
                                :key="index"
                                :label="model.text"
                                :value="model.value"
                            />
                        </el-select>
                    </el-form-item>
                    <el-form-item
                        prop="decay"
                        label="学习速率的衰减率："
                    >
                        <el-input
                            v-model="vData.form.other_param.decay"
                            placeholder="decay"
                        />
                    </el-form-item>
                    <el-form-item label="衰减率是否开平方：">
                        <el-radio
                            v-model="vData.form.other_param.decay_sqrt"
                            :label="true"
                        >
                            是
                        </el-radio>
                        <el-radio
                            v-model="vData.form.other_param.decay_sqrt"
                            :label="false"
                        >
                            否
                        </el-radio>
                    </el-form-item>
                    <el-form-item label="多分类策略：">
                        <el-select
                            v-model="vData.form.other_param.multi_class"
                            clearable
                        >
                            <el-option
                                v-for="(model, index) in vData.multiClassList"
                                :key="index"
                                :label="model.text"
                                :value="model.value"
                            />
                        </el-select>
                    </el-form-item>
                    <el-form-item
                        prop="validation_freqs"
                        label="验证频次："
                    >
                        <el-input
                            v-model="vData.form.other_param.validation_freqs"
                            placeholder="validation_freqs"
                        />
                    </el-form-item>
                    <el-form-item
                        prop="early_stopping_rounds"
                        label="提前结束的迭代次数："
                    >
                        <el-input
                            v-model="vData.form.other_param.early_stopping_rounds"
                            placeholder="early_stopping_rounds"
                        />
                    </el-form-item>
                </el-collapse-item>
                <el-collapse-item title="init param" name="2">
                    <el-form-item label="模型初始化方式：">
                        <el-select
                            v-model="vData.form.init_param.init_method"
                            clearable
                        >
                            <el-option
                                v-for="(model, index) in vData.initMethodList"
                                :key="index"
                                :label="model.text"
                                :value="model.value"
                            />
                        </el-select>
                    </el-form-item>
                    <el-form-item label="是否需要偏置系数：">
                        <el-radio
                            v-model="vData.form.init_param.fit_intercept"
                            :label="true"
                        >
                            是
                        </el-radio>
                        <el-radio
                            v-model="vData.form.init_param.fit_intercept"
                            :label="false"
                        >
                            否
                        </el-radio>
                    </el-form-item>
                </el-collapse-item>
                <el-collapse-item title="encrypt param" name="3">
                    <el-form-item label="同态加密方法：">
                        <el-select
                            v-model="vData.form.encrypt_param.method"
                            :disabled="disabled || vData.form.fl_type === 'horizontal'"
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
                            @change="methods.watchNum($event, true)"
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
                            v-for="{ key, label, items, rule } in lrGrid"
                            :key="key"
                            :label="label"
                            :disabled="disabled"
                            :items="items"
                            :rule="rule"
                            v-model="vData.form.grid_search_param[key]"
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
    import { reactive, getCurrentInstance, computed } from 'vue';
    import MultiGridSearchTag from '../../../../../components/Common/MultiGridSearchTag.vue';
    import gridSearchParams from '../../../../../assets/js/const/gridSearchParams';
    import dataStore from '../data-store-mixin';

    const lrGrid = gridSearchParams.lr;
    const grid_search_param = { need_grid_search: false };

    lrGrid.forEach(({ key }) => (grid_search_param[key] = []));

    const LogisticRegression = {
        init_param: {
            init_method:   'random_uniform',
            fit_intercept: true,
        },
        encrypt_param: { method: 'Paillier' },
        cv_param:      {
            n_splits: 5,
            shuffle:  true,
            need_cv:  false,
        },
        other_param: {
            penalty:               'L2',
            tol:                   0.0001,
            alpha:                 1,
            optimizer:             'adam',
            batch_size:            3000,
            learning_rate:         0.1,
            early_stop:            'diff',
            max_iter:              10,
            decay:                 1,
            decay_sqrt:            true,
            multi_class:           'ovr',
            validation_freqs:      10,
            early_stopping_rounds: 5,
        },
        grid_search_param,
    };

    export default {
        name:  'MixLR',
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
                    { value: 'L1',text: 'L1' },
                    { value: 'L2',text: 'L2' },
                ],
                initMethodList: [
                    { value: 'random_uniform',text: 'random_uniform' },
                    { value: 'random_normal',text: 'random_normal' },
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
                ],

                // config
                originForm:  { ...LogisticRegression },
                form:        { ...LogisticRegression },
                activeNames: ['1'],
            });

            let methods = {
                checkParams() {
                    return {
                        params: vData.form,
                    };
                },
                async readData(model) {
                    await methods.getNodeDetail(model);
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
                        Object.assign(vData.form, data.params);
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

            const runTime = computed(() =>
                Object.values(vData.form.grid_search_param).reduce(
                    (acc, cur) => acc * (cur.length || 1),
                    1,
                ),
            );

            return {
                vData,
                methods,
                lrGrid,
                runTime,
            };
        },
    };
</script>

<style lang="scss" scoped>
.board-form-item{
    margin-bottom: 10px;
    :deep(.board-form-item__label){
        flex:1;
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
