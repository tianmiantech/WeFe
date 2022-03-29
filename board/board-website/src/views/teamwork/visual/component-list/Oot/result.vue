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

                    <el-form v-if="vData.hasResult">
                        <h4 class="mb10 pb5">验证结果:</h4>
                        <el-row class="mb20">
                            <el-col :span="12">
                                auc：{{ vData.validate.auc }}
                            </el-col>
                            <el-col :span="12">
                                ks：{{ vData.validate.ks }}
                            </el-col>
                        </el-row>
                    </el-form>
                </el-collapse-item>
                <template v-if="vData.hasResult">
                    <el-collapse-item title="模型准确率表现" name="2">
                        <TopN ref="topnRef" />
                    </el-collapse-item>
                    <el-collapse-item title="模型评估" name="3">
                        <ChartsWithTabs
                            v-if="vData.showCharts"
                            component-type="Oot"
                            :job-id="jobId"
                            :flow-id="flowId"
                            :flow-node-id="flowNodeId"
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
    import { ref, reactive, getCurrentInstance } from 'vue';
    import CommonResult from '../common/CommonResult';
    import resultMixin from '../result-mixin';
    import TopN from '../Evaluation/TopN';

    const mixin = resultMixin();

    export default {
        name:       'Oot',
        components: {
            CommonResult,
            TopN,
        },
        props: {
            ...mixin.props,
        },
        setup(props, context) {
            const activeName = ref('1');
            const { appContext } = getCurrentInstance();
            const { $http } = appContext.config.globalProperties;
            const topnRef = ref();

            let vData = reactive({
                showCharts:  false,
                hasResult:   false,
                resultTypes: ['ks'],
                validate:    {
                    auc: 0,
                    ks:  0,
                },
                pollingOnJobRunning: true,
            });

            let methods = {
                initParams() {
                    return {
                        type: 'ks',
                    };
                },
                showResult(data) {
                    if (data[0].status) {
                        vData.commonResultData = {
                            task: data[0],
                        };

                        if(data[0].result) {
                            const { validate } = data[0].result;

                            vData.validate = {
                                auc: validate.data.auc.value,
                                ks:  validate.data.ks.value,
                            };
                            vData.hasResult = true;
                            methods.getTopNData(data[0]);
                        } else {
                            vData.hasResult = false;
                        }
                    }
                },
                async getTopNData(res) {
                    const { code, data } = await $http.get({
                        url:    '/flow/job/task/result',
                        params: {
                            flowId:     res.flow_id,
                            flowNodeId: res.flow_node_id,
                            jobId:      res.job_id,
                            type:       'topn',
                        },
                    });

                    if (code === 0) {
                        const { result } = Array.isArray(data) ? data[0]: data;

                        if (result) {
                            topnRef.value.renderTopnTable(result);
                        }
                    }
                },
                collapseChanged(val) {
                    if(val.includes('3')){
                        vData.showCharts = true;
                    }
                },
            };

            const { $data, $methods } = mixin.mixin({
                props,
                context,
                vData,
                methods,
                topnRef,
            });

            vData = $data;
            methods = $methods;

            return {
                vData,
                activeName,
                methods,
                topnRef,
            };
        },
    };
</script>
