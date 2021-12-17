<template>
    <div
        v-loading="vData.loading"
        class="el-form"
    >
        <template v-if="vData.commonResultData.task">
            <el-collapse
                v-model="activeName"
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
    import { ref, reactive } from 'vue';
    import CommonResult from '../common/CommonResult';
    import resultMixin from '../result-mixin';

    const mixin = resultMixin();

    export default {
        name:       'VertSecureBoost',
        components: {
            CommonResult,
        },
        props: {
            ...mixin.props,
        },
        setup(props, context) {
            const activeName = ref('1');

            let vData = reactive({
                result:      null,
                resultTypes: ['vert_secureboost'],
                loss:        {
                    show:        false,
                    loading:     true,
                    xAxis:       [],
                    series:      [[]],
                    iters:       0,
                    isConverged: null,
                },
                pollingOnJobRunning: true,
            });

            let methods = {
                showResult(data) {
                    if(data[0].result && data[0].result.model_param) {
                        vData.result = true;
                        const { losses, isConverged, lossHistory } = data[0].result.model_param;

                        losses.forEach((item, index) => {
                            vData.loss.xAxis.push(index);
                            vData.loss.series[0].push(item);
                        });
                        vData.loss.isConverged = isConverged;
                        vData.loss.lossHistory = lossHistory;
                        vData.loss.iters = losses.length;
                        vData.loss.loading = false;
                    } else {
                        vData.result = false;
                    }
                },
                collapseChanged(val) {
                    if(val.includes('2')){
                        vData.loss.show = true;
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
