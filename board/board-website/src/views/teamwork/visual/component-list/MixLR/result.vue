<template>
    <div
        v-loading="vData.loading"
        class="result"
    >
        <template v-if="vData.commonResultData.task">
            <el-collapse v-model="activeName">
                <el-collapse-item
                    title="基础信息"
                    name="1"
                >
                    <CommonResult
                        :result="vData.commonResultData"
                        :currentObj="currentObj"
                        :jobDetail="jobDetail"
                    />
                </el-collapse-item>
                <el-collapse-item
                    v-if="vData.result"
                    title="特征权重"
                    name="2"
                >
                    <el-table
                        :data="vData.tableData"
                        style="max-width:355px;"
                        max-height="600px"
                        class="mt10"
                        stripe
                        border
                    >
                        <el-table-column
                            type="index"
                            label="序号"
                            width="60"
                        />
                        <el-table-column
                            prop="feature"
                            label="特征"
                            width="80"
                        />
                        <el-table-column
                            prop="weight"
                            label="权重"
                        />
                    </el-table>
                </el-collapse-item>
            </el-collapse>
        </template>
        <div
            v-else
            class="data-empty"
        >
            查无结果!
        </div>
    </div>
</template>

<script>
    import {
        ref, reactive,
    } from 'vue';
    import CommonResult from '../common/CommonResult';
    import resultMixin from '../result-mixin';

    const mixin = resultMixin();

    export default {
        name:       'MixLR',
        components: {
            CommonResult,
        },
        props: {
            ...mixin.props,
        },
        emits: [...mixin.emits],
        setup(props, context) {
            const activeName = ref('1');

            let vData = reactive({
                result:     null,
                role:       'promoter',
                tableData:  [],
                train_loss: {
                    columns: ['x', 'loss'],
                    rows:    [],
                },
                resultTypes:         [],
                pollingOnJobRunning: true,
            });

            let methods = {
                showResult(data) {
                    if(data && data.result) {
                        vData.result = true;
                        const {
                            model_param: {
                                intercept,
                                weight,
                            },
                        } = data.result;

                        vData.tableData = [];
                        for(const key in weight) {
                            vData.tableData.push({
                                feature: key,
                                weight:  weight[key],
                            });
                        }
                        vData.tableData.push({
                            feature: 'b',
                            weight:  intercept,
                        });
                    } else {
                        vData.result = false;
                    }
                },
            };

            const { $data, $methods } = mixin.mixin({
                props,
                context,
                vData,
                methods,
            });

            vData = $data;
            methods = $methods;

            return {
                vData,
                activeName,
                methods,
            };
        },
    };
</script>
