<template>
    <div
        v-if="componentType"
        v-loading="loading"
        class="charts-group"
    >
        <el-tabs
            v-if="ChartsMap[componentType].tabs && ChartsMap[componentType].tabs.length"
            v-model="tabName"
            @tab-click="tabChange"
        >
            <el-tab-pane
                v-for="(tab, index) in ChartsMap[componentType].tabs"
                :key="`${tab.name}-${index}`"
                :name="tab.name"
                :label="tab.label"
            >
                <TopN ref="topn" v-if="tab.name === 'topn'"></TopN>
                <component
                    :ref="tab.name"
                    :is="`${tab.chart.type.substring(0,1).toUpperCase()}${tab.chart.type.substring(1)}Chart`"
                    v-if="charts[tab.name] && tab.name !== 'topn'"
                    :config="charts[tab.name].config"
                >
                    <template #default="{ config }">
                        <div class="f13">
                            <template v-if="config.train.auc">
                                训练结果: auc: {{ config.train.auc }} <span class="ml10">ks: {{ config.train.ks }}</span>
                                <br />
                            </template>
                            验证结果: auc: {{ config.validate.auc }} <span class="ml10">ks: {{ config.validate.ks }}</span>
                        </div>
                    </template>
                </component>
            </el-tab-pane>
        </el-tabs>
        <!-- single chart -->
        <component
            :ref="tab.name"
            :is="`${ChartsMap[componentType].chart.type.substring(0, 1).UpperCase()}${ChartsMap[componentType].chart.type.substring(1)}Chart`"
            v-else-if="charts[tab.name]"
            :config="charts[tab.name].config"
        />
    </div>
</template>

<script>
    // import { getCurrentInstance } from 'vue';
    import ChartsMap from './ChartTypesMap';
    import TopN from '@views/teamwork/visual/component-list/Evaluation/TopN.vue';

    export default {
        name:  'ChartsWithTabs',
        props: {
            resultApi:  String,
            jobId:      String,
            flowId:     String,
            flowNodeId: String,
            showTopn:   Boolean,
        },
        components: { TopN },
        data() {
            return {
                ChartsMap,
                componentType: '',
                tabName:       '',
                loading:       false,
                charts:        {},
            };
        },
        created() {
            this.readResult();
            // show topn or not for modeling-list
            if (!this.showTopn && this.ChartsMap.Evaluation.tabs && this.ChartsMap.Evaluation.tabs.length) {
                this.ChartsMap.Evaluation.tabs.forEach((item, idx) => {
                    if (item.name === 'topn') {
                        this.ChartsMap.Evaluation.tabs.splice(idx, 1);
                    }
                });
            } else {
                let isHaveTopn = false;

                this.ChartsMap.Evaluation.tabs.forEach((item, idx) => {
                    if (item.name === 'topn') {
                        isHaveTopn = true;
                    }
                });
                if (!isHaveTopn) {
                    this.ChartsMap.Evaluation.tabs.push({
                        label: 'TOPN',
                        name:  'topn',
                        chart: {
                            type:   'table',
                            config: {
                                type: 'topn',
                            },
                        },
                    });
                }
            }
        },
        methods: {
            async readResult() {
                if (this.loading) return;
                this.loading = true;

                const params = {
                    jobId:      this.jobId,
                    flowId:     this.flowId,
                    flowNodeId: this.flowNodeId,
                    type:       'ks',
                };

                const { code, data } = await this.$http.post({
                    url:  this.resultApi || '/flow/job/task/result',
                    data: params,
                });

                setTimeout(_ => {
                    this.loading = false;
                }, 200);

                if (code === 0 && data) {
                    const $data = Array.isArray(data) ? data[0] : data;
                    const { result, component_type } = $data;

                    this.componentType = component_type;

                    if (result) {
                        const currentChart = this.ChartsMap[this.componentType];

                        // cache charts config
                        if(currentChart.tabs && currentChart.tabs.length) {
                            currentChart.tabs.forEach((tab, index) => {
                                if(index === 0) {
                                    this.tabName = tab.name;
                                    params.type = tab.chart.config.type;
                                }
                                this.charts[tab.name] = tab.chart;
                            });
                        }

                        if(component_type === 'Evaluation' || component_type === 'Oot') {
                            this.renderRoc($data);
                        } else {
                            const { train_loss } = result;

                            this.charts[this.tabName].config.legend = ['loss'];
                            this.charts[this.tabName].config.series = [train_loss.data.map((value, index) => {
                                return {
                                    x:    index,
                                    loss: value,
                                };
                            })];
                        }
                        this.charts[this.tabName].config.loading = false;
                    }
                }
            },

            tabChange() {
                this.$nextTick(() => {
                    this.getChartsData();
                });
            },

            async getChartsData() {
                const { tabName } = this;
                const { type } = this.charts[tabName].config;
                const $ref = this.$refs[this.tabName];
                const ref = Array.isArray($ref) ? $ref[0] : $ref;
                const Queue = [];

                ref.loading = true;

                Queue.push(
                    await this.$http.get({
                        url:    this.resultApi || '/flow/job/task/result',
                        params: {
                            flowId:     this.flowId,
                            flowNodeId: this.flowNodeId,
                            jobId:      this.jobId,
                            type,
                        },
                    }),
                );

                const res = await Promise.all(Queue);

                if (res.length === 1) {
                    // for one chart
                    if (res[0].code === 0 && res[0].data) {
                        const { result } = Array.isArray(res[0].data) ? res[0].data[0] : res[0].data;

                        if(tabName === 'roc') {
                            // render roc with ks
                            this.renderRoc({ result });
                        } else if(tabName === 'ks') {
                            const {
                                train_ks_fpr,
                                train_ks_tpr,
                                validate_ks_fpr,
                                validate_ks_tpr,
                            } = result;
                            const train_ks_fpr_data = [],
                                  train_ks_tpr_data = [],
                                  validate_ks_fpr_data = [],
                                  validate_ks_tpr_data = [],
                                  xAxis = [];
                            const diffArray = {
                                0: [],
                                1: [],
                            };
                            const diffValues = {
                                0: [],
                                1: [],
                            };

                            if(train_ks_fpr) {
                                this.charts[tabName].config.legend = ['train_ks_fpr', 'train_ks_tpr'];
                                train_ks_fpr.data.forEach((data, index) => {
                                    xAxis.push(data[0]);
                                    train_ks_fpr_data.push(train_ks_fpr.data[index][1]);
                                    train_ks_tpr_data.push(train_ks_tpr.data[index][1]);
                                });

                                train_ks_fpr.data.forEach((row, index) => {
                                    diffValues[0][index] = +Math.abs(train_ks_fpr.data[index][1] - train_ks_tpr.data[index][1]).toFixed(6);
                                    diffArray[0][index] = {
                                        x1: train_ks_fpr.data[index][0],
                                        y1: train_ks_fpr.data[index][1],
                                        x2: train_ks_tpr.data[index][0],
                                        y2: train_ks_tpr.data[index][1],
                                    };
                                });

                                const maxDiff = {
                                    0: Math.max(...diffValues[0]),
                                };
                                const maxIndex = {
                                    0: diffValues[0].findIndex(item => item === maxDiff[0]),
                                };

                                // Difference line
                                const maxValue = {
                                    0: diffArray[0][maxIndex[0]],
                                };

                                this.charts[tabName].config.markLine.data[0][0].coord = [String(maxValue[0].x1), String(maxValue[0].y1)];
                                this.charts[tabName].config.markLine.data[0][1].coord = [String(maxValue[0].x2), String(maxValue[0].y2)];
                                this.charts[tabName].config.series = [train_ks_fpr_data, train_ks_tpr_data];
                            }

                            if(validate_ks_fpr) {
                                this.charts[tabName].config.legend.push('validate_ks_fpr', 'validate_ks_tpr');
                                validate_ks_fpr.data.forEach((data, index) => {
                                    if(xAxis.length === 0) xAxis.push(data[0]);
                                    validate_ks_fpr_data.push(validate_ks_fpr.data[index][1]);
                                    validate_ks_tpr_data.push(validate_ks_tpr.data[index][1]);
                                });

                                validate_ks_tpr.data.forEach((row, index) => {
                                    diffValues[1][index] = +(validate_ks_tpr.data[index][1] - validate_ks_fpr.data[index][1]).toFixed(6);
                                    diffArray[1][index] = {
                                        x1: validate_ks_tpr.data[index][0],
                                        y1: validate_ks_tpr.data[index][1],
                                        x2: validate_ks_fpr.data[index][0],
                                        y2: validate_ks_fpr.data[index][1],
                                    };
                                });

                                const maxDiff = {
                                    1: Math.max(...diffValues[1]),
                                };
                                const maxIndex = {
                                    1: diffValues[1].findIndex(item => item === maxDiff[1]),
                                };

                                // Difference line
                                const maxValue = {
                                    1: diffArray[1][maxIndex[1]],
                                };

                                this.charts[tabName].config.markLine.data[1][0].coord = [String(maxValue[1].x1), String(maxValue[1].y1)];
                                this.charts[tabName].config.markLine.data[1][1].coord = [String(maxValue[1].x2), String(maxValue[1].y2)];
                                this.charts[tabName].config.series.push(validate_ks_fpr_data, validate_ks_tpr_data);
                            }
                            this.charts[tabName].config.xAxis = xAxis;

                        } else if (tabName === 'precisionRecall') {
                            const {
                                train_precision,
                                train_recall,
                                validate_precision,
                                validate_recall,
                            } = result;
                            const train_precision_data = [],
                                  train_recall_data = [],
                                  validate_precision_data = [],
                                  validate_recall_data = [],
                                  xAxis = [];

                            this.charts[tabName].config.legend = ['validate_precision', 'validate_recall'];
                            if(train_precision) {
                                const ArrayLength = Math.min(train_precision.data.length, train_recall.data.length, validate_precision.data.length, validate_recall.data.length);

                                for (let i = 0; i < ArrayLength; i++) {
                                    const data = train_precision.data[i];

                                    xAxis.push(data[0]);
                                    train_precision_data.push(data[1]);
                                    train_recall_data.push(train_recall.data[i][1]);
                                    validate_precision_data.push(validate_precision.data[i][1]);
                                    validate_recall_data.push(validate_recall.data[i][1]);
                                }

                                this.charts[tabName].config.legend.unshift('train_precision', 'train_recall');
                                this.charts[tabName].config.series = [train_precision_data, train_recall_data, validate_precision_data, validate_recall_data];
                            } else {
                                const ArrayLength = Math.min(validate_precision.data.length, validate_recall.data.length);

                                for (let i = 0; i < ArrayLength; i++) {
                                    const data = validate_precision.data[i];

                                    xAxis.push(data[0]);
                                    validate_precision_data.push(validate_precision.data[i][1]);
                                    validate_recall_data.push(validate_recall.data[i][1]);
                                }
                                this.charts[tabName].config.series = [validate_precision_data, validate_recall_data];
                            }

                            this.charts[tabName].config.xAxis = xAxis;

                        } else if (tabName === 'topn') {
                            ref.renderTopnTable(result);
                        } else {
                            const lineNames = [`train_${tabName}`, `validate_${tabName}`];

                            this.renderChart(tabName, lineNames, { result });
                        }
                    }
                }

                ref.chartResize();

                setTimeout(_ => {
                    ref.loading = false;
                }, 200);
            },

            renderRoc({ result }) {
                const { tabName } = this;
                const {
                    train,
                    validate,
                    train_ks_fpr,
                    train_ks_tpr,
                    validate_ks_tpr,
                } = result;
                const legend = ['validate_roc'],
                      train_roc = [],
                      validate_roc = [],
                      xAxis = [];

                this.charts[tabName].config.validate.auc = validate.data.auc.value;
                this.charts[tabName].config.validate.ks = validate.data.ks.value;
                this.charts[tabName].config.legend = legend;
                this.charts[tabName].config.xAxis = xAxis;
                this.charts[tabName].config.series = [validate_roc];

                if(train_ks_fpr) {
                    legend.unshift('train_roc');
                    train_ks_fpr.data.forEach((row, index) => {
                        xAxis.push(row[0]);
                        train_roc.push(train_ks_tpr.data[index][1]);
                    });

                    this.charts[tabName].config.train.auc = train.data.auc.value;
                    this.charts[tabName].config.train.ks = train.data.ks.value;
                    this.charts[tabName].config.series.unshift(train_roc);
                }

                validate_ks_tpr.data.forEach((row, index) => {
                    if(xAxis.length === 0) xAxis.push(row[0]);
                    validate_roc.push(validate_ks_tpr.data[index][1]);
                });

                const ref = this.$refs[this.tabName];

                ref && ref.chartResize();
            },

            renderChart(tabName, lineNames, { result }) {
                const train = result[lineNames[0]];
                const validate = result[lineNames[1]];

                if(train) {
                    const lines = [train, validate];
                    const xAxis = [];
                    const train_data = [],
                          validate_data = [];

                    for (let i = 0; i < lines.length; i++) {
                        const { data } = lines[i]; // data[0] fpr x, data[1] tpr y

                        for (let j = 0; j < data.length; j++) {
                            if(i === 0) {
                                xAxis.push(data[j][0]);
                                train_data.push(data[j][1]);
                            } else {
                                validate_data.push(data[j][1]);
                            }
                        }
                    }
                    this.charts[tabName].config.xAxis = xAxis;
                    this.charts[tabName].config.series = [train_data, validate_data];
                }
                this.charts[tabName].config.legend = [...lineNames];
            },
        },
    };
</script>
