<template>
    <div
        v-loading="vData.loading"
        class="result"
    >
        <template v-if="vData.commonResultData.task">
            <el-collapse v-model="activeName" @change="methods.collapseChanged">
                <el-collapse-item title="基础信息" name="1">
                    <CommonResult
                        :result="vData.commonResultData"
                        :currentObj="currentObj"
                        :jobDetail="jobDetail"
                        :showHistory="false"
                    />
                    <p>job_id: {{jobId}}</p>
                    <p>任务详细信息：</p>
                    <ul>
                        <li v-for="item in memberJobDetailList" :key="item.member_id">
                            {{item.member_name}}:
                            <div style="margin-left: 20px;">
                                <p :style="{'color': item.job_status === 'success' ? 'green' : '#f85564'}">job: {{item.job_status}}</p>
                                <p :style="{'color': item.task_status === 'success' ? 'green' : '#f85564'}">task: {{item.task_status}}</p>
                                <p>message:  {{item.message}}</p>
                            </div>
                        </li>
                    </ul>
                </el-collapse-item>
                <el-collapse-item title="任务跟踪指标" name="2">
                    <el-tabs v-model="vData.expandparams.type" @tab-click="methods.tabChange">
                        <el-tab-pane label="Loss" name="loss"></el-tab-pane>
                        <!-- <el-tab-pane label="Accuracy" name="accuracy"></el-tab-pane> -->
                    </el-tabs>
                    <LineChart
                        v-if="vData.isshow"
                        ref="LineChart"
                        :config="vData.train_loss"
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
    import { ref, reactive, onMounted } from 'vue';
    import CommonResult from '../../visual/component-list/common/CommonResult.vue';
    import resultMixin from '../../visual/component-list/result-mixin';

    const mixin = resultMixin();

    export default {
        components: {
            CommonResult,
        },
        props: {
            ...mixin.props,
            memberJobDetailList: Array,
        },
        setup(props, context) {
            const activeName = ref('1');
            const LineChart = ref();

            let vData = reactive({
                header:       [],
                datasetList:  [],
                expandparams: {
                    type: 'loss',
                },
                train_loss: {
                    xAxis:  [],
                    series: [[]],
                },
                isshow: false,
            });

            let methods = {
                tabChange() {
                    methods.readData();
                },
                collapseChanged(val) {
                    if(val.includes('2')){
                        vData.isshow = true;
                    }
                },
                showResult(data) {
                    if(data[0].result) {
                        vData.result = true;
                        const train_loss = data[0].result.data;

                        for (const key in train_loss) {
                            vData.train_loss.xAxis.push(key);
                            vData.train_loss.series[0].push(train_loss[key].value);
                        }
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

            onMounted(_=> {
                window.onresize = () => {
                    LineChart.value && LineChart.value.chartResize();
                };
            });

            return {
                vData,
                activeName,
                methods,
                LineChart,
            };
        },
    };
</script>
