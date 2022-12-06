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
                        <h4>训练结果:</h4>
                        <el-row class="mb10">
                            <el-col :span="8">
                                auc：{{ vData.train.auc }}
                            </el-col>
                            <el-col :span="12">
                                ks：{{ vData.train.ks }}
                            </el-col>
                        </el-row>
                        <h4>验证结果:</h4>
                        <el-row>
                            <el-col :span="8">
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
                            component-type="Evaluation"
                            :job-id="jobId"
                            :flow-id="flowId"
                            :flow-node-id="flowNodeId"
                        />
                    </el-collapse-item>
                    <el-collapse-item :title="`预测概率/评分 PSI:${vData.featurePsi}`" name="4">
                        <psi-table :tableData="vData.tableData" type="evalution" />
                    </el-collapse-item>
                </template>
            </el-collapse>
        </template>
        <div
            v-else
            class="data-empty"
        >
            <p v-if="myRole === 'promoter'">查无结果!</p>
            <el-alert
                v-else
                title="!!! 协作方无法查看结果"
                style="width:250px;"
                :closable="false"
                type="warning"
                effect="dark"
                class="mb10"
                show-icon
            />
        </div>
    </div>
</template>

<script>
    import {
        ref,
        reactive,
        getCurrentInstance,
    } from 'vue';
    import CommonResult from '../common/CommonResult';
    import resultMixin from '../result-mixin';
    import TopN from './TopN.vue';
    import psiTable from '../../components/psi/psi-table.vue';
    import { getDataResult } from '@src/service';
    import { turnDemical } from '@src/utils/utils';


    const mixin = resultMixin();

    export default {
        name:       'Evaluation',
        components: {
            CommonResult,
            TopN,
            psiTable,
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
                hasResult:   false,
                resultTypes: ['ks'],
                train:       {
                    auc: 0,
                    ks:  0,
                },
                validate: {
                    auc: 0,
                    ks:  0,
                },
                showCharts:          false,
                pollingOnJobRunning: true,
                tableData:           [],
                featurePsi:          '',
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

                        if(data[0].result && data[0].result.train) {
                            const { train, validate } = data[0].result;

                            vData.train = {
                                auc: train.data.auc.value,
                                ks:  train.data.ks.value,
                            };
                            vData.validate = {
                                auc: validate.data.auc.value,
                                ks:  validate.data.ks.value,
                            };
                            vData.hasResult= true;
                            methods.getTopNData(data[0]);
                            methods.getPSIResult(data[0]);
                        } else {
                            vData.hasResult = false;
                        }
                    }
                },
                getPSIResult(res){
                    const { flow_id, flow_node_id, job_id } = res || {};

                    getDataResult({
                        flowId: flow_id, flowNodeId: flow_node_id, jobId: job_id, type: 'psi',
                    }).then((data) => {
                        const { psi= {} } = data;
                        const { 
                            pred_label_psi = '',
                            train_pred_label_static,
                            test_pred_label_static ,
                            bin_cal_results = {},
                            split_point = [] } = psi; 

                        vData.featurePsi = turnDemical(pred_label_psi, 4);
                        vData.tableData = {
                            '预测概率/评分': {
                                train_feature_static: train_pred_label_static || {},
                                test_feature_static:  test_pred_label_static || {},
                                feature_psi:          pred_label_psi,
                                bin_cal_results,
                                split_point:          split_point.slice(1),
                            },
                        };
                    });
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
