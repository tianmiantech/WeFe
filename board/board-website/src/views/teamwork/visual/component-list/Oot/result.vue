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
                        <h4 class="mb10 pb5">测试结果:</h4>
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
                    <el-collapse-item :title="`预测概率/评分 PSI:${vData.featurePsi}`" name="4">
                        <psi-table :tableData="vData.tableData" type="Oot"/>
                    </el-collapse-item>
                </template>
            </el-collapse>
            <div v-if="vData.isSqlShow && vData.hasResult" class="sql-box">
                <p class="sql-title">请到board所在的mysql库执行以下sql语句查询批量跑分结果，或者点击<a @click="methods.export_predict_data">导出数据</a></p>
                <h4 class="mb10 pb5" >{{vData.tips}}</h4>
                <div class="sql" >
                    <p class="copy-btn" @click="methods.copyCode" :data-clipboard-text="vData.sqlStatement">copy</p>
                    <div>
                        <p><code>SELECT</code> <code>DISTINCT</code>(t2.raw_key) <code>AS</code> raw_key, t1.predict_percentage <code>FROM</code> model_predict_result t1 </p>
                        <p><code>LEFT JOIN</code> key_hash_mapper t2 <code>ON</code> t1.intersect_pre_example_id = t2.hashed_key <code>WHERE</code> t1.job_id <span class="equal">=</span> '<span class="id">{{jobId}}</span>' <code>AND</code> t1.oot_node_id <span class="equal">=</span> '<span class="id">{{flowNodeId}}</span>';</p>
                    </div>
                </div>
                <div class="code-tips mt10">
                    <div>sql语句输出字段说明：raw_key（样本ID）、predict_percentage（预测概率）</div>
                </div>
            </div>

            <div v-if="vData.isSqlShow && vData.hasResult" style="margin-top: 20px;">
            </div>
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
    import psiTable from '../../components/psi/psi-table.vue';
    import Clipboard from 'clipboard';
    import { setStorage } from '@src/router/auth';
    import { mapGetters } from 'vuex';
    import { getDataResult } from '@src/service';
    import { turnDemical } from '@src/utils/utils';

    const mixin = resultMixin();

    export default {
        name:       'Oot',
        components: {
            CommonResult,
            TopN,
            psiTable,
        },
        props: {
            ...mixin.props,
            learningType: String,
        },
        computed: {
            ...mapGetters(['userInfo']),
        },
        setup(props, context) {
            const activeName = ref('1');
            const { appContext } = getCurrentInstance();
            const { $http, $message } = appContext.config.globalProperties;
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
                sqlStatement:        '',
                isSqlShow:           true,
                tips:                '',
                tableData:           {},
                featurePsi:          '',
            });

            vData.sqlStatement = `
            SELECT DISTINCT(t2.raw_key) AS raw_key, t1.predict_percentage
            FROM model_predict_result t1
            LEFT JOIN key_hash_mapper t2 ON t1.intersect_pre_example_id = t2.hashed_key
            WHERE t1.job_id = '${props.jobId}'
            AND t1.oot_node_id = '${props.flowNodeId}';`;

            if (props.learningType === 'vertical' && props.myRole === 'provider') vData.isSqlShow = false;

            let methods = {
                initParams() {
                    return {
                        type: 'ks',
                    };
                },
                //延迟的方法
                sleep1(numberMillis){
                    let now = new Date();

                    const exitTime = now.getTime() + numberMillis;

                    while (true) {
                        now = new Date();
                        if (now.getTime() > exitTime) return;
                    }
                },
                async export_predict_data(){
                    vData.tips = '正在查询，请不要离开.....';

                    const { baseUrl } = window.api;

                    let userInfo = setStorage().getItem(`${baseUrl}_userInfo`);

                    userInfo = JSON.parse(userInfo);
                    let count = 0;

                    while(count < 360){
                        const { code, data } = await $http.get({
                            url:    '/flow/job/task/get_oot_predict_progress',
                            params: {
                                oot_node_id: props.flowNodeId,
                                job_id:      props.jobId,
                            },
                        });

                        if(data.finished){
                            const api = `${window.api.baseUrl}${data.get_file_url}&token=${userInfo.token}&time=${new Date().getTime()}`;
                            const link = document.createElement('a');

                            link.href = api;
                            link.target = '_blank';
                            link.style.display = 'none';
                            document.body.appendChild(link);
                            link.click();
                            vData.tips = '';
                            break;
                        }
                        else{
                            this.sleep1(5000);// 5秒
                            count++;
                        }
                    }
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
                                feature_psi:          pred_label_psi || '',
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
                copyCode() {
                    const clipboard = new Clipboard('.copy-btn');

                    clipboard.on('success', e => {
                        $message.success('复制成功！');
                        clipboard.destroy();
                    });
                    clipboard.on('error', e => {
                        $message.warning('该浏览器不支持自动复制！');
                        clipboard.destroy();
                    });
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

<style lang="scss" scoped>
.sql-box {
    .sql-title {
        font-size: 13px;
        color: #303133;
        font-weight: 500;
        margin: 20px 0 4px 0;
    }
    .sql {
        background: #f5f5f5;
        padding: 10px;
        position: relative;
        .copy-btn {
            position: absolute;
            right: 5px;
            font-size: 13px;
            color: #999;
            cursor: pointer;
            z-index: 2;
            &:hover {
                color: #89BDD4;
            }
        }
        code {
            color: #358DB7;
        }
        .equal {
            color: #B79875;
        }
        .id {
            color: #8CB140;
        }
    }
    .code-tips {
        font-size: 12px;
    }
}
</style>
