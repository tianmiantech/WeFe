<template>
    <div
        v-loading="vData.loading"
        class="result"
    >
        <template v-if="vData.commonResultData.task">
            <el-collapse
                v-model="vData.activeName"
                @change="methods.collapseChanged"
            >
                <el-collapse-item title="基础信息" name="1">
                    <CommonResult
                        :result="vData.commonResultData"
                        :currentObj="currentObj"
                        :jobDetail="jobDetail"
                    />
                    <template v-if="vData.result">
                        <el-row>
                            <el-col :span="12">
                                迭代次数: {{ vData.loss.iters }}
                            </el-col>
                            <el-col
                                v-if="vData.loss.isConverged != null"
                                :span="12"
                            >
                                是否收敛: {{ vData.loss.isConverged ? '是' : '否' }}
                            </el-col>
                        </el-row>
                    </template>
                </el-collapse-item>

                <el-collapse-item
                    v-if="myRole === 'promoter'"
                    title="任务跟踪指标（LOSS）"
                    name="2"
                >
                    <LineChart
                        v-if="vData.loss.show"
                        :config="vData.loss"
                    />
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
        reactive,
    } from 'vue';
    import CommonResult from '../common/CommonResult';
    import resultMixin from '../result-mixin';

    const mixin = resultMixin();

    export default {
        name:       'HorzNN',
        components: {
            CommonResult,
        },
        props: {
            ...mixin.props,
        },
        setup(props, context) {
            let vData = reactive({
                activeName: '1',
                role:       'promoter',
                tableData:  [],
                train_loss: {
                    columns: ['x', 'loss'],
                    rows:    [],
                },
                resultTypes:         [],
                result:              null,
                pollingOnJobRunning: true,
                loss:                {
                    show:        false,
                    loading:     true,
                    xAxis:       [],
                    series:      [[]],
                    iters:       0,
                    isConverged: null,
                },
            });

            let methods = {
                // showResult(data) {
                //     if(data[0].result) {
                //         vData.result = true;
                //         const {
                //             model_param: {
                //                 intercept,
                //                 weight,
                //             },
                //             // train_loss,
                //         } = data[0].result;

                //         vData.tableData = [];
                //         for(const key in weight) {
                //             vData.tableData.push({
                //                 feature: key,
                //                 weight:  weight[key],
                //             });
                //         }
                //         vData.tableData.push({
                //             feature: 'b',
                //             weight:  intercept,
                //         });
                //     } else {
                //         vData.result = null;
                //     }
                // },
                showResult(data) {
                    if(data[0].result && data[0].result.model_param) {
                        vData.result = true;
                        const { historyLoss, isConverged } = data[0].result.model_param;

                        if(historyLoss && historyLoss.length) {
                            historyLoss.forEach((item, index) => {
                                vData.loss.xAxis.push(index);
                                vData.loss.series[0].push(item);
                            });
                            vData.loss.iters = historyLoss.length;
                        }
                        vData.loss.isConverged = isConverged;
                        vData.loss.loading = false;
                    } else {
                        vData.result = false;
                    }
                },
                collapseChanged(val) {
                    if(val.includes('2')){
                        vData.loss.show = true;
                        vData.loss.loading = false;
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
                methods,
            };
        },
    };
</script>
