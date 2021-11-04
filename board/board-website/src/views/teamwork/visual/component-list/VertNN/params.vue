<template>
    <div
        class="form"
    >
        <el-form
            ref="form"
            :model="vData.form"
            :disabled="vData.disabled"
        >
            <el-collapse v-model="vData.activeNames">
                <el-collapse-item title="VertNN参数设置" name="1">
                    <el-form-item
                        prop="epochs"
                        label="最大迭代次数："
                    >
                        <el-input
                            v-model="vData.form.epochs"
                            placeholder="epochs"
                            type="number"
                        />
                    </el-form-item>
                    <el-form-item
                        prop="interactive_layer_lr"
                        label="交互层学习率："
                    >
                        <el-input
                            v-model="vData.form.interactive_layer_lr"
                            placeholder="interactive_layer_lr"
                        />
                    </el-form-item>
                    <el-form-item
                        prop="batch_size"
                        label="批量大小："
                        type="number"
                    >
                        <el-input
                            v-model="vData.form.batch_size"
                            placeholder="batch_size"
                        />
                    </el-form-item>
                    <el-form-item
                        prop="learning_rate"
                        label="学习率"
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
                    <el-form-item label="底层参数：">
                        <p class="add-one-group"><i class="el-icon-plus" @click="methods.addOneGroup('bottom_nn_define')"></i></p>
                        <template v-for="(item, idx) in vData.form.bottom_nn_define.layers" :key="item">
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
                                            @change="methods.unitsChangeEvent"
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
                                <i v-if="idx !== 0" class="el-icon-delete" @click="methods.deleteOneGroup('bottom_nn_define', idx)"></i>
                            </div>
                        </template>
                    </el-form-item>
                    <el-form-item label="中层参数：">
                        <p hidden class="add-one-group"><i class="el-icon-plus" @click="methods.addOneGroup('interactive_layer_define')"></i></p>
                        <template v-for="(item, idx) in vData.form.interactive_layer_define.layers" :key="item">
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
                                            @change="methods.unitsChangeEvent"
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
                                <i v-if="idx !== 0" class="el-icon-delete" @click="methods.deleteOneGroup('interactive_layer_define', idx)"></i>
                            </div>
                        </template>
                    </el-form-item>
                    <el-form-item label="顶层参数：">
                        <p class="add-one-group"><i class="el-icon-plus" @click="methods.addOneGroup('top_nn_define')"></i></p>
                        <template v-for="(item, idx) in vData.form.top_nn_define.layers" :key="item">
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
                                            @change="methods.unitsChangeEvent"
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
                                <i v-if="idx !== 0" class="el-icon-delete" @click="methods.deleteOneGroup('top_nn_define', idx)"></i>
                            </div>
                        </template>
                    </el-form-item>
                </el-collapse-item>
            </el-collapse>
        </el-form>
    </div>
</template>

<script>
    import dataStore from '../data-store-mixin';
    import { reactive, getCurrentInstance, onBeforeMount } from 'vue';
    import { useRoute } from 'vue-router';

    const LogisticRegression = {
        epochs:               100,
        interactive_layer_lr: 0.15,
        batch_size:           320,
        learning_rate:        0.15,
        optimizer:            'SGD',
        loss:                 'binary_crossentropy',
        bottom_nn_define:     {
            layers: [
                {
                    class_name: 'Dense',
                    config:     {
                        units:       5,
                        input_shape: [],
                        activation:  'relu',
                    },
                },
            ],
        },
        interactive_layer_define: {
            layers: [
                {
                    class_name: 'Dense',
                    config:     {
                        units:       3,
                        input_shape: [],
                        activation:  'relu',
                    },
                },
            ],
        },
        top_nn_define: {
            layers: [
                {
                    class_name: 'Dense',
                    config:     {
                        units:       1,
                        input_shape: [],
                        activation:  'relu',
                    },
                },
            ],
        },
    };

    export default {
        name:  'VertNN',
        props: {
            projectId:    String,
            flowId:       String,
            disabled:     Boolean,
            learningType: String,
            currentObj:   Object,
            jobId:        String,
            class:        String,
        },
        setup(props, context) {
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
                                totalCount += member.data_set_features;
                            });
                            methods.changeInputShape(totalCount);
                        }
                    }
                },
                changeInputShape(data) {
                    vData.form.bottom_nn_define.layers[0].config.input_shape[0] = data;
                },

                reactiveInputShape() {
                    // top_nn_define = promoter + provider
                    // interactive_layer_define = bottom_nn_define
                    vData.form.interactive_layer_define.layers[0].config.input_shape[0] = vData.form.bottom_nn_define.layers[vData.form.bottom_nn_define.layers.length-1].config.units;
                    // top_nn_define = interactive_layer_define
                    vData.form.top_nn_define.layers[0].config.input_shape[0] = vData.form.interactive_layer_define.layers[vData.form.interactive_layer_define.layers.length-1].config.units;
                },
                checkParams() {
                    vData.inited = false;
                    if (!Array.isArray(vData.form.bottom_nn_define.layers[0].config.input_shape)) {
                        vData.form.bottom_nn_define.layers[0].config.input_shape = vData.form.bottom_nn_define.layers[0].config.input_shape.split(',');
                    }

                    if (!Array.isArray(vData.form.interactive_layer_define.layers[0].config.input_shape)) {
                        vData.form.interactive_layer_define.layers[0].config.input_shape = vData.form.interactive_layer_define.layers[0].config.input_shape.split(',');
                    }

                    if (!Array.isArray(vData.form.top_nn_define.layers[0].config.input_shape)) {
                        vData.form.top_nn_define.layers[0].config.input_shape = vData.form.top_nn_define.layers[0].config.input_shape.split(',');
                    }
                    return {
                        params: vData.form,
                    };
                },

                unitsChangeEvent() {
                    methods.reactiveInputShape();
                },

                addOneGroup(type) {
                    const json = {
                        class_name: 'Dense',
                        config:     {
                            'units':      1,
                            'activation': 'relu',
                        },
                    };

                    switch(type) {
                    case 'bottom_nn_define':
                        vData.form.bottom_nn_define.layers.push(json);
                        break;
                    case 'interactive_layer_define':
                        vData.form.interactive_layer_define.layers.push(json);
                        break;
                    case 'top_nn_define':
                        vData.form.top_nn_define.layers.push(json);
                        break;
                    }
                    methods.reactiveInputShape();
                },

                deleteOneGroup(type, idx) {
                    switch(type) {
                    case 'bottom_nn_define':
                        vData.form.bottom_nn_define.layers.splice(idx, 1);
                        break;
                    case 'interactive_layer_define':
                        vData.form.interactive_layer_define.layers.splice(idx, 1);
                        break;
                    case 'top_nn_define':
                        vData.form.top_nn_define.layers.splice(idx, 1);
                        break;
                    }
                    methods.reactiveInputShape();
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
                methods.reactiveInputShape();
            });

            return {
                vData,
                methods,
            };
        },
    };
</script>

<style lang="scss" scoped>
.el-form {margin-top: 25px;}
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
            width: 102px;
        }
    }
    .el-icon-delete {
        color: #ff4949;
        cursor: pointer;
        padding-left: 5px;
    }
}
</style>
