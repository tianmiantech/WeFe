<template>
    <div v-loading="vData.loading" class="result">
        <template v-if="vData.commonResultData.task">
            <el-collapse v-model="activeName">
                <el-collapse-item title="基础信息" name="1">
                    <CommonResult :result="vData.commonResultData"  :currentObj="currentObj" :jobDetail="jobDetail"/>
                </el-collapse-item>
                <el-collapse-item v-if="vData.members.length" title="成员信息" name="2">
                    <el-tabs v-model="vData.tabName">
                        <el-tab-pane v-for="(member, mIdx) in vData.members" :key="`${member.member_id}-${member.member_role}`" :name="`${member.member_id}-${member.member_role}`" :label="`${member.member_name} (${member.member_role === 'provider' ? '协作方' : '发起方' })`">
                            <p class="mb10 pb5">分布:</p>
                            <el-table :data="member.table" stripe :border="true" style="width :100%" class="fold-table">
                                <el-table-column type="expand">
                                    <template #default="props">
                                        <el-tabs
                                            type="border-card"
                                            @tab-click="methods.tabChangeEvent(props.$index, mIdx, $event)"
                                        >
                                            <el-tab-pane label="Overview">
                                                <el-table :data="member.table[props.$index].overviewtable" stripe border>
                                                    <el-table-column label="最小值" prop="min">
                                                        <template v-slot="scope">
                                                            {{ dealNumPrecision(scope.row.min) }}
                                                        </template>
                                                    </el-table-column>
                                                    <el-table-column label="最大值" prop="max">
                                                        <template v-slot="scope">
                                                            {{ dealNumPrecision(scope.row.max) }}
                                                        </template>
                                                    </el-table-column>
                                                    <el-table-column label="平均值" prop="mean">
                                                        <template v-slot="scope">
                                                            {{ dealNumPrecision(scope.row.mean) }}
                                                        </template>
                                                    </el-table-column>
                                                    <el-table-column v-if="reference !== 'HorzStatistic'" label="众数" prop="mode">
                                                        <template v-slot="scope">
                                                            {{ dealNumPrecision(scope.row.mode) }}
                                                        </template>
                                                    </el-table-column>
                                                    <el-table-column label="非空值数量" prop="count" width="95px"/>
                                                    <el-table-column label="缺失数量" prop="missing_count" width="80px"/>
                                                    <el-table-column label="峰度" prop="kurtosis">
                                                        <template v-slot:header>
                                                            <el-tooltip placement="top" effect="light">
                                                                <template #content>表征概率密度分布曲线在平均值处峰值高低的特征数</template>
                                                                <p>峰度
                                                                    <el-icon>
                                                                        <elicon-warning-filled />
                                                                    </el-icon></p>
                                                            </el-tooltip>
                                                        </template>
                                                        <template v-slot="scope">
                                                            {{ dealNumPrecision(scope.row.kurtosis) }}
                                                        </template>
                                                    </el-table-column>
                                                    <el-table-column label="偏态" prop="skewness">
                                                        <template v-slot:header>
                                                            <el-tooltip placement="top" effect="light">
                                                                <template #content>统计数据分布偏斜方向和程度的度量，是统计数据分布非对称程度的数字特征</template>
                                                                <p>偏态<el-icon>
                                                                    <elicon-warning-filled />
                                                                </el-icon></p>
                                                            </el-tooltip>
                                                        </template>
                                                        <template v-slot="scope">
                                                            {{ dealNumPrecision(scope.row.skewness) }}
                                                        </template>
                                                    </el-table-column>
                                                    <el-table-column label="标准差" prop="std_variance">
                                                        <template v-slot="scope">
                                                            {{ dealNumPrecision(scope.row.std_variance) }}
                                                        </template>
                                                    </el-table-column>
                                                    <el-table-column label="方差" prop="variance">
                                                        <template v-slot="scope">
                                                            {{ dealNumPrecision(scope.row.variance) }}
                                                        </template>
                                                    </el-table-column>
                                                </el-table>
                                            </el-tab-pane>
                                            <!--Continuous type-->
                                            <el-tab-pane v-if="JSON.stringify(props.row.unique_count) === '{}'" label="Statistics" name="statistics">
                                                <div class="flexbox">
                                                    <el-descriptions class="margin-top" title="Quantile statistics" :column="1" size="small" border style="flex: 1">
                                                        <el-descriptions-item label="Minimum">{{dealNumPrecision(props.row.min)}}</el-descriptions-item>
                                                        <el-descriptions-item v-if="reference !== 'HorzStatistic'" label="5-th percentile">{{dealNumPrecision(props.row.percentile[5])}}</el-descriptions-item>
                                                        <el-descriptions-item v-if="reference !== 'HorzStatistic'" label="Q1">{{dealNumPrecision(props.row.percentile[25])}}</el-descriptions-item>
                                                        <el-descriptions-item v-if="reference !== 'HorzStatistic'" label="median">{{dealNumPrecision(props.row.percentile[50])}}</el-descriptions-item>
                                                        <el-descriptions-item v-if="reference !== 'HorzStatistic'" label="Q3">{{dealNumPrecision(props.row.percentile[75])}}</el-descriptions-item>
                                                        <el-descriptions-item v-if="reference !== 'HorzStatistic'" label="95-th percentile">{{dealNumPrecision(props.row.percentile[95])}}</el-descriptions-item>
                                                        <el-descriptions-item label="Maximum">{{dealNumPrecision(props.row.max)}}</el-descriptions-item>
                                                        <el-descriptions-item label="Variance">{{dealNumPrecision(props.row.variance)}}</el-descriptions-item>
                                                    </el-descriptions>
                                                    <el-descriptions class="margin-top" title="Descriptive statistics" :column="1" size="small" border style="flex: 1">
                                                        <el-descriptions-item label="Standard deviation">{{dealNumPrecision(props.row.std_variance)}}</el-descriptions-item>
                                                        <el-descriptions-item label="Coefficient of variation（CV）">{{dealNumPrecision(props.row.cv)}}</el-descriptions-item>
                                                        <el-descriptions-item label="kurtosis">{{dealNumPrecision(props.row.kurtosis)}}</el-descriptions-item>
                                                        <el-descriptions-item label="Mean">{{dealNumPrecision(props.row.mean)}}</el-descriptions-item>
                                                        <el-descriptions-item label="Skewness">{{dealNumPrecision(props.row.skewness)}}</el-descriptions-item>
                                                    </el-descriptions>
                                                </div>
                                            </el-tab-pane>
                                            <template v-if="reference !== 'HorzStatistic'">
                                                <el-tab-pane
                                                    v-if="JSON.stringify(props.row.unique_count) === '{}'"
                                                    label="Histogram"
                                                    name="histogram"
                                                >
                                                    <BarChart
                                                        v-if="member.table[props.$index].isBarChart"
                                                        :ref="BarChartHandle"
                                                        :config="props.row.distributionChart"
                                                    />
                                                </el-tab-pane>
                                                <!--Discrete type-->
                                                <el-tab-pane v-else label="Categories" name="categories">
                                                    <div style="display : flex">
                                                        <el-table :data="member.table[props.$index].categoryTable" border style="flex: 1;max-height: 400px;overflow-y: auto;">
                                                            <el-table-column prop="category" label="category" width="180"></el-table-column>
                                                            <el-table-column prop="count" label="count" width="180"></el-table-column>
                                                            <el-table-column prop="frequency" label="frequency">
                                                                <template v-slot="scope">
                                                                    {{ dealNumPrecision(scope.row.frequency) }}
                                                                </template>
                                                            </el-table-column>
                                                        </el-table>
                                                        <PieChart
                                                            v-if="member.table[props.$index].isPieChart"
                                                            :ref="PieChartHandle"
                                                            style="flex: 1"
                                                            :config="props.row.pieChartData"
                                                        />
                                                    </div>
                                                </el-tab-pane>
                                            </template>
                                        </el-tabs>
                                    </template>
                                </el-table-column>
                                <el-table-column label="特征名称" prop="feature" sortable sort-by="feature" ></el-table-column>
                                <el-table-column label="特征类型" prop="unique_count">
                                    <template v-slot="scope">
                                        <p>{{JSON.stringify(scope.row.unique_count)==='{}'?'连续型' :'离散型'}}</p>
                                    </template>
                                </el-table-column>
                                <el-table-column label="类别数目" prop="unique_count_sum" sortable sort-by="unique_count_sum">
                                    <template v-slot="scope">
                                        <p>{{scope.row.unique_count_sum || '--'}}</p>
                                    </template>
                                </el-table-column>
                                <el-table-column label="非空值数量 / 缺失数量" sortable :sort-method="methods.sortMethod"	>
                                    <template v-slot="scope">
                                        <p>{{scope.row.not_null_count}}/{{scope.row.missing_count}}</p>
                                    </template>
                                </el-table-column>
                            </el-table>
                        </el-tab-pane>
                    </el-tabs>
                </el-collapse-item>
            </el-collapse>
        </template>
        <div v-else class="data-empty">查无结果!</div>
    </div>
</template>

<script>
    import { reactive, ref, onBeforeMount, getCurrentInstance, nextTick } from 'vue';
    import CommonResult from '../common/CommonResult';
    import resultMixin from '../result-mixin';
    import { dealNumPrecision } from '@src/utils/utils.js';

    const mixin = resultMixin();

    export default {
        name:       'FeatureStatistic',
        components: {
            CommonResult,
        },
        props: {
            ...mixin.props,
            reference: String,
        },
        setup(props, context) {
            const activeName = ref('1');

            let vData = reactive({
                tabName:     '',
                members:     [],
                resultTypes: [],
            });
            const BarChart = ref();
            const PieChart = ref();
            const { appContext, ctx } = getCurrentInstance();
            const { $bus } = appContext.config.globalProperties;

            let methods = {
                sortMethod({not_null_count: a, missing_count: b}, {not_null_count: c, missing_count: d}){
                    /**
                     * 用作非空值数量/缺失数量排序
                     */
                    return (a>c || (a==c && b>d)) ? -1 : 0; 

                },
                numberSum(arr) {
                    let total = 0;

                    for (let i=0; i<arr.length; i++) {
                        total += arr[i];
                    }
                    return total;
                },
                showResult(data) {
                    vData.members = [];
                    if (data[0].result && data[0].result.members) {
                        const { members } = data[0].result;

                        vData.tabName = `${members[0].member_id}-${members[0].role}`;

                        members.forEach(member => {
                            const { feature_statistic } = member;
                            const table = [];

                            for(const key in feature_statistic) {
                                const val = feature_statistic[key];
                                // distributionChart
                                const distributionChartxAxis = [], distributionChartSeries = [];
                                const {
                                    distribution,
                                    percentile,
                                    min,
                                    max,
                                    unique_count,
                                } = val;
                                const pieSeries = [], list = [];

                                const unique_count_values = Object.values(unique_count);
                                const unique_count_keys = Object.keys(unique_count);
                                for (let i=0; i<unique_count_values.length; i++) {
                                    pieSeries.push({
                                        name:  unique_count_keys[i],
                                        value: unique_count_values[i],
                                    });
                                    list.push({
                                        category:  unique_count_keys[i],
                                        count:     unique_count_values[i],
                                        frequency: unique_count_values[i] / methods.numberSum(unique_count_values),
                                    });
                                }

                                for(let i = 0; i < 10; i++) {
                                    distributionChartxAxis.push(i);
                                    distributionChartSeries.push({
                                        name:  i,
                                        value: distribution[i] || 0,
                                    });
                                }

                                // percentileChart
                                const percents = Object.keys(percentile), percentvalues = Object.values(percentile);

                                if(!percents.includes(0)) {
                                    percents.unshift(0);
                                }
                                if(!percents.includes(100)) {
                                    percents.push(100);
                                    percentvalues.push([]);
                                }

                                const percentilexAxis = [], percentileSeries = [];

                                percents.forEach(percent => {
                                    const val = percentile[percent];

                                    percentilexAxis.push(percent);
                                    percentileSeries.push(
                                        percent === 0 ? min : (percent === 100 ? max : val),
                                    );
                                });
                                let median = null, q1 = null, q95 = null;

                                percents.forEach((percent, idx) => {
                                    if (percents[idx] === 5 || percents[idx] === '5') {
                                        q1 = percentvalues[idx][0];
                                    }
                                    if (percents[idx] === 50 || percents[idx] === '50') {
                                        median = percentvalues[idx][0];
                                    }
                                    if (percents[idx] === 95 || percents[idx] === '95') {
                                        q95 = percentvalues[idx][0];
                                    }
                                });

                                table.push({
                                    feature:           key,
                                    ...val,
                                    distributionChart: {
                                        titleText: key,
                                        xAxis:     distributionChartxAxis,
                                        series:    distributionChartSeries,
                                    },
                                    percentileChart: {
                                        xAxis:  percentilexAxis,
                                        series: [percentileSeries],
                                    },
                                    overviewtable: [],
                                    activeTab:     'overview',
                                    isBarChart:    true,
                                    isPieChart:    true,
                                    pieChartData:  {
                                        titleText: key,
                                        series:    pieSeries,
                                    },
                                    categoryTable: list,
                                    median,
                                    q1,
                                    q95,
                                    unique_count_sum: unique_count_values.length,
                                });
                                table[table.length-1].overviewtable.push(val);
                            }

                            /** 
                             * 后台返回的表格顺序是乱的，故在此稍微排序
                             * 可能preview看到的是正的，但是response里面是乱的。
                             *  */
                            table.sort((a,b) => a.feature> b.feature ? 1 : -1)

                            vData.members.push({
                                member_id:   member.member_id,
                                member_name: member.member_name,
                                member_role: member.role,
                                table,
                            });
                        });
                    }
                },
                tabChangeEvent(idx, mIdx, $event){
                    nextTick(_ => {
                        switch ($event.props.name) {
                        case 'categories':
                            setTimeout(()=>{
                                if (PieChart.value) PieChart.value.chartResize();
                            }, 200);
                            break;
                        case 'histogram':
                            setTimeout(()=>{
                                if (BarChart.value) BarChart.value.chartResize();
                            }, 200);
                            break;
                        }
                    });
                },
            };

            const { $data, $methods } = mixin.mixin({
                props,
                context,
                vData,
                methods,
            });

            onBeforeMount(() => {
                $bus.$on('drag-end', _ => {
                    if (PieChart.value) {
                        nextTick(_=> {
                            PieChart.value.chartResize();
                        });
                    }
                });
            });

            vData = $data;
            methods = $methods;

            return {
                vData,
                activeName,
                methods,
                PieChartHandle: el => PieChart.value = el,
                BarChartHandle: el => BarChart.value = el,
                dealNumPrecision,
            };
        },
    };
</script>

<style lang="scss" scoped>
    .fold-table {
        :deep(.board-table__expanded-cell) {
            padding: 20px;
        }
        :deep(.board-descriptions__header) {
            padding: 10px;
            border: 1px solid #ebeef5;
            margin-bottom: 0;
            text-align: center;
        }
    }
</style>
