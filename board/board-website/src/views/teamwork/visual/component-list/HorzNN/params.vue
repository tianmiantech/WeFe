<template>
    <el-form
        ref="form"
        :model="vData.form"
        :disabled="vData.disabled"
    >
        <el-collapse v-model="vData.activeNames">
            <el-collapse-item title="HorzNN参数设置" name="1">
                <el-form-item
                    prop="max_iter"
                    label="最大迭代次数："
                >
                    <el-input
                        v-model="vData.form.max_iter"
                        placeholder="max_iter"
                    />
                </el-form-item>
                <el-form-item
                    prop="batch_size"
                    label="批量大小："
                >
                    <el-input
                        v-model="vData.form.batch_size"
                        placeholder="batch_size"
                    />
                </el-form-item>
                <el-form-item
                    prop="learning_rate"
                    label="学习率："
                >
                    <el-input
                        v-model="vData.form.learning_rate"
                        placeholder="learning_rate"
                    />
                </el-form-item>
                <el-form-item prop="optimizer" label="优化算法：">
                    <el-select
                        v-model="vData.form.optimizer"
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
                <el-form-item prop="loss" label="损失函数：">
                    <el-select
                        v-model="vData.form.loss"
                        clearable
                    >
                        <el-option
                            v-for="(model, index) in vData.lossList"
                            :key="index"
                            :label="model.text"
                            :value="model.value"
                        />
                    </el-select>
                </el-form-item>
                <el-form-item label="每层参数：" :index="idx">
                    <p class="add-one-group">
                        <el-icon class="el-icon-plus" @click="methods.addOneGroup">
                            <elicon-plus />
                        </el-icon>
                    </p>
                    <template v-for="(item, idx) in vData.form.nn_define.layers" :key="item">
                        <div class="single-box" :index="idx">
                            <div class="single-left">
                                <div>
                                    <label for="">类型：</label>
                                    <el-select
                                        v-model="item.class_name"
                                        clearable
                                    >
                                        <el-option
                                            v-for="(model, index) in vData.classNameList"
                                            :key="index"
                                            :label="model.text"
                                            :value="model.value"
                                        />
                                    </el-select>
                                </div>
                                <div v-if="idx === 0">
                                    <label for="">输入维度：</label>
                                    <el-input
                                        v-model="item.config.input_shape[0]"
                                        placeholder="input_shape"
                                        disabled
                                    />
                                </div>
                                <div>
                                    <label for="">输出维度：</label>
                                    <el-input
                                        v-model="item.config.units"
                                        placeholder="units"
                                        type="number"
                                    />
                                </div>
                                <div>
                                    <label for="">激活函数：</label>
                                    <el-select
                                        v-model="item.config.activation"
                                        clearable
                                    >
                                        <el-option
                                            v-for="(model, index) in vData.activationList"
                                            :key="index"
                                            :label="model.text"
                                            :value="model.value"
                                        />
                                    </el-select>
                                </div>
                            </div>
                            <el-icon
                                v-if="idx !== 0"
                                class="el-icon-delete"
                                @click="methods.deleteOneGroup(idx)"
                            >
                                <elicon-delete />
                            </el-icon>
                        </div>
                    </template>
                </el-form-item>
            </el-collapse-item>
        </el-collapse>
    </el-form>
</template>

<script>
    import { reactive, getCurrentInstance, onBeforeMount } from 'vue';
    import { useRoute } from 'vue-router';
    import dataStore from '../data-store-mixin';

    const LogisticRegression = {
        max_iter:      10,
        batch_size:    320,
        learning_rate: 0.1,
        optimizer:     'Adam',
        loss:          'binary_crossentropy',
        nn_define:     {
            layers: [
                {
                    class_name: 'Dense',
                    config:     {
                        'units':       10,
                        'input_shape': [],
                        'activation':  'relu',
                    },
                },
                {
                    class_name: 'Dense',
                    config:     {
                        'units':      1,
                        'activation': 'sigmoid',
                    },
                },
            ],
        },
    };

    export default {
        name:  'HorzNN',
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
            const route = useRoute();
            const { flow_id } = route.query;

            let vData = reactive({
                flow_id,
                optimizerList: [
                    { value: 'Adadelta',text: 'Adadelta' },
                    { value: 'Adagrad',text: 'Adagrad' },
                    { value: 'Adam',text: 'Adam' },
                    { value: 'Adamax',text: 'Adamax' },
                    { value: 'Nadam',text: 'Nadam' },
                    { value: 'RMSprop',text: 'RMSprop' },
                    { value: 'SGD',text: 'SGD' },
                ],
                lossList: [
                    { value: 'binary_crossentropy',text: 'binary_crossentropy' },
                    { value: 'categorical_crossentropy',text: 'categorical_crossentropy' },
                ],
                classNameList: [
                    { value: 'Dense',text: 'Dense' },
                ],
                activationList: [
                    { value: 'relu',text: 'relu' },
                    { value: 'sigmoid',text: 'sigmoid' },
                    { value: 'tanh',text: 'tanh' },
                    { value: 'softmax',text: 'softmax' },
                    { value: 'selu',text: 'selu' },
                ],
                // config
                originForm:  { ... LogisticRegression },
                form:        { ... LogisticRegression },
                activeNames: ['1'],
            });

            let methods = {
                // get dataset information, feature number
                async getDataFeatures() {
                    const { code, data } = await $http.get({
                        url:    '/flow/dataset/info',
                        params: {
                            flow_id: vData.flow_id,
                        },
                    });

                    if (code === 0) {
                        if (data.flow_data_set_features.length) {
                            const members = data.flow_data_set_features[0].members || [];

                            let totalCount = 0;

                            members.forEach(member => {
                                if (member.member_role === 'promoter') {
                                    totalCount = member.data_set_features;
                                }
                            });
                            methods.changeInputShape(totalCount);
                        }
                    }
                },
                changeInputShape(data) {
                    vData.form.nn_define.layers[0].config.input_shape[0] = data;
                },
                checkParams() {
                    vData.inited = false;
                    return {
                        params: vData.form,
                    };
                },
                addOneGroup() {
                    vData.form.nn_define.layers.push({
                        class_name: 'Dense',
                        config:     {
                            'units':      1,
                            'activation': 'sigmoid',
                        },
                    });
                },
                deleteOneGroup(idx) {
                    vData.form.nn_define.layers.splice(idx, 1);
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

                    if (code === 0 && data && data.params) {
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

            onBeforeMount(_ => {
                methods.getDataFeatures();
            });

            return {
                vData,
                methods,
            };
        },
    };
</script>

<style lang="scss" scoped>
.el-form {
    margin-top: 25px;
}
.el-form-item{
    margin-bottom: 10px;
    :deep(.el-form-item__label){
        text-align: left;
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
    :deep(.el-collapse-item__content) {
        padding-bottom: 0;
    }
}
.is-active {
    border-right: 1px solid #f1f1f1;
    border-left: 1px solid #f1f1f1;
}
.readonly-form:before {
    position: unset !important;
}
.add-one-group {
    text-align: right;
    margin-top: -28px;
    margin-bottom: 10px;
    .el-icon-plus {
        font-size: 16px;
        color: #438bff;
        cursor: pointer;
    }
}
.single-box {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 10px;
    border: 1px solid #f1f1f1;
    padding: 0 5px;
    .single-left {
        margin-top: 10px;
        >div {
            display: flex;
            margin-bottom: 10px;
        }
        label {
            width: 100px;
        }
    }
    .el-icon-delete {
        color: #ff4949;
        cursor: pointer;
        padding-left: 5px;
    }
}
</style>
