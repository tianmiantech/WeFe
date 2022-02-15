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
                <el-collapse-item
                    title="基础信息"
                    name="1"
                >
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
                    v-if="vData.results.length"
                    title="任务跟踪指标（LOSS）"
                    name="2"
                >
                    <template
                        v-for="(result, index) in vData.results"
                        :key="index"
                    >
                        <p class="mb10"><strong>{{ result.title }} :</strong></p>
                        <LineChart
                            v-if="result.loss.show"
                            :config="result.loss"
                        />
                        <el-divider v-if="index === 0"></el-divider>
                    </template>
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
        name:       'MixSecureBoost',
        components: {
            CommonResult,
        },
        props: {
            ...mixin.props,
        },
        setup(props, context) {
            const activeName = ref('1');

            let vData = reactive({
                results:             [],
                pollingOnJobRunning: true,
            });

            let methods = {
                showResult(list) {
                    vData.results = list.map(data => {
                        const result = {
                            title: data.members.map(m => `${m.member_name} (${m.member_role})`).join(' & '),
                            loss:  {
                                show:        false,
                                loading:     true,
                                xAxis:       [],
                                series:      [[]],
                                iters:       0,
                                isConverged: null,
                            },
                        };

                        if(data.result && data.result.model_param) {
                            const { losses, isConverged, lossHistory } = data.result.model_param;

                            losses.forEach((item, index) => {
                                result.loss.xAxis.push(index);
                                result.loss.series[0].push(item);
                            });
                            result.loss.isConverged = isConverged;
                            result.loss.lossHistory = lossHistory;
                            result.loss.iters = losses.length;
                            result.loss.loading = false;
                        }

                        return result;
                    });
                },
                collapseChanged(val) {
                    if(val.includes('2')){
                        vData.results.forEach(result => {
                            result.loss.show = true;
                        });
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
