<template>
    <div
        v-loading="vData.loading"
        class="result"
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
                <template v-if="vData.result">
                    <el-collapse-item
                        title="特征权重"
                        name="2"
                    >
                        <el-table
                            :data="vData.loss.weight"
                            style="max-width:355px;"
                            max-height="600px"
                            stripe
                            border
                        >
                            <el-table-column
                                prop="number"
                                label="序号"
                                width="60"
                            />
                            <el-table-column
                                prop="name"
                                label="列名"
                                width="140"
                            />
                            <el-table-column
                                prop="height"
                                label="权重"
                            />
                        </el-table>
                    </el-collapse-item>
                    <el-collapse-item
                        title="任务跟踪指标（LOSS）"
                        name="3"
                    >
                        <LineChart
                            v-if="vData.loss.show"
                            :config="vData.loss"
                        />
                    </el-collapse-item>
                </template>
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
        name:       'HorzSecureBoost',
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
                result:      null,
                resultTypes: [],
                loss:        {
                    show:        false,
                    loading:     true,
                    xAxis:       [],
                    series:      [[]],
                    iters:       0,
                    isConverged: null,
                    weight:      [],
                },
                pollingOnJobRunning: true,
            });

            let methods = {
                showResult(data) {
                    if(data.result && data.result.model_param) {
                        vData.result = true;
                        const { losses, isConverged, lossHistory, weight } = data.result.model_param;

                        losses.forEach((item, index) => {
                            vData.loss.xAxis.push(index);
                            vData.loss.series[0].push(item);
                        });
                        vData.loss.isConverged = isConverged;
                        vData.loss.lossHistory = lossHistory;
                        vData.loss.iters = losses.length;
                        vData.loss.weight = weight || [];
                        vData.loss.loading = false;
                    } else {
                        vData.result = false;
                    }
                },
                collapseChanged(val) {
                    if(val.includes('3')){
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
