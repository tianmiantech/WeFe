<template>
    <div
        v-loading="vData.loading"
        class="result"
    >
        <template v-if="vData.commonResultData.task">
            <el-collapse v-model="activeName">
                <el-collapse-item title="基础信息" name="1">
                    <CommonResult
                        :result="vData.commonResultData"
                        :currentObj="currentObj"
                        :jobDetail="jobDetail"
                    />
                </el-collapse-item>
                <el-collapse-item
                    v-if="vData.members.length"
                    title="成员信息"
                    name="2"
                >
                    <el-tabs v-model="vData.tabName">
                        <el-tab-pane
                            v-for="(member, mIdx) in vData.members"
                            :key="`${member.member_id}-${member.member_role}`"
                            :name="`${member.member_id}-${member.member_role}`"
                            :label="`${member.member_name} (${member.member_role === 'provider' ? '协作方' : '发起方'})`"
                        >
                            <p class="mb10 pb5">分布:</p>
                            <el-table
                                :data="member.table"
                                stripe
                                :border="true"
                                style="width: 100%"
                                class="fold-table">
                                <el-table-column type="expand">
                                    <template #default="props">
                                        <el-tabs type="border-card" v-model="vData.activeTab" @tab-click="methods.tabChangeEvent(props.$index, mIdx, $event)">
                                            <el-tab-pane label="Overview" name="overview">
                                                <el-table
                                                    :data="member.table[props.$index].overviewtable"
                                                    stripe
                                                    border
                                                >
                                                    <el-table-column
                                                        label="最小值"
                                                        prop="min"
                                                    />
                                                    <el-table-column
                                                        label="最大值"
                                                        prop="max"
                                                    />
                                                    <el-table-column
                                                        label="平均值"
                                                        prop="mean"
                                                    />
                                                    <el-table-column
                                                        label="众数"
                                                        prop="mode"
                                                    />
                                                    <el-table-column
                                                        label="非空值数量"
                                                        prop="count"
                                                        width="95px"
                                                    />
                                                    <el-table-column
                                                        label="缺失数量"
                                                        prop="missing_count"
                                                        width="80px"
                                                    />
                                                    <el-table-column
                                                        label="峰度"
                                                        prop="kurtosis"
                                                    >
                                                        <template v-slot:header>
                                                            <el-tooltip placement="top" effect="light">
                                                                <template #content>表征概率密度分布曲线在平均值处峰值高低的特征数</template>
                                                                <p>峰度<i class="el-icon-warning" /></p>
                                                            </el-tooltip>
                                                        </template>
                                                    </el-table-column>
                                                    <el-table-column
                                                        label="偏态"
                                                        prop="skewness"
                                                    >
                                                        <template v-slot:header>
                                                            <el-tooltip placement="top" effect="light">
                                                                <template #content>统计数据分布偏斜方向和程度的度量，是统计数据分布非对称程度的数字特征</template>
                                                                <p>偏态<i class="el-icon-warning" /></p>
                                                            </el-tooltip>
                                                        </template>
                                                    </el-table-column>
                                                    <el-table-column
                                                        label="标准差"
                                                        prop="std_variance"
                                                    />
                                                    <el-table-column
                                                        label="方差"
                                                        prop="variance"
                                                    />
                                                </el-table>
                                            </el-tab-pane>
                                            <!-- Continuous type -->
                                            <el-tab-pane v-if="JSON.stringify(props.row.unique_count) === '{}'" label="Statistics" name="statistics">
                                                <div style="display:flex;">
                                                    <el-descriptions class="margin-top" title="Quantile statistics" :column="1" size="medium" border style="flex: 1">
                                                        <el-descriptions-item label="Minimum">{{props.row.min}}</el-descriptions-item>
                                                        <el-descriptions-item label="5-th percentile"></el-descriptions-item>
                                                        <el-descriptions-item label="Q1">{{props.row.q1}}</el-descriptions-item>
                                                        <el-descriptions-item label="median">{{props.row.median}}</el-descriptions-item>
                                                        <el-descriptions-item label="95-th percentile">{{props.row.q95}}</el-descriptions-item>
                                                        <el-descriptions-item label="Maximum">{{props.row.max}}</el-descriptions-item>
                                                        <el-descriptions-item label="Variance">{{props.row.variance}}</el-descriptions-item>
                                                    </el-descriptions>
                                                    <el-descriptions class="margin-top" title="Descriptive statistics" :column="1" size="medium" border style="flex: 1">
                                                        <el-descriptions-item label="Standard deviation">{{props.row.std_variance}}</el-descriptions-item>
                                                        <el-descriptions-item label="Coefficient of variation（CV）">{{props.row.cv}}</el-descriptions-item>
                                                        <el-descriptions-item label="kurtosis">{{props.row.kurtosis}}</el-descriptions-item>
                                                        <el-descriptions-item label="Mean">{{props.row.mean}}</el-descriptions-item>
                                                        <el-descriptions-item label="Skewness">{{props.row.skewness}}</el-descriptions-item>
                                                        <el-descriptions-item label="Sum">{{}}</el-descriptions-item>
                                                    </el-descriptions>
                                                </div>
                                            </el-tab-pane>
                                            <el-tab-pane v-if="JSON.stringify(props.row.unique_count) === '{}'" label="Histogram" name="histogram">
                                                <BarChart
                                                    ref="BarChart"
                                                    v-if="`${member.member_id}-${member.member_role}` === vData.tabName"
                                                    :config="props.row.distributionChart"
                                                />
                                            </el-tab-pane>
                                            <!-- Discrete type -->
                                            <el-tab-pane v-else label="Categories" name="categories">
                                                <div style="display: flex;">
                                                    <el-table
                                                        :data="member.table[props.$index].categoryTable"
                                                        border
                                                        style="flex: 1; max-height: 400px; overflow-y: auto;">
                                                        <el-table-column
                                                            prop="category"
                                                            label="category"
                                                            width="180">
                                                        </el-table-column>
                                                        <el-table-column
                                                            prop="count"
                                                            label="count"
                                                            width="180">
                                                        </el-table-column>
                                                        <el-table-column
                                                            prop="frequency"
                                                            label="frequency">
                                                        </el-table-column>
                                                    </el-table>
                                                    <PieChart
                                                        ref="PieChart"
                                                        style="flex: 1;"
                                                        v-if="`${member.member_id}-${member.member_role}` === vData.tabName"
                                                        :config="props.row.pieChartData"
                                                    />
                                                </div>
                                            </el-tab-pane>
                                        </el-tabs>
                                    </template>
                                </el-table-column>
                                <el-table-column
                                    label="特征名称"
                                    prop="feature">
                                </el-table-column>
                                <el-table-column
                                    label="特征类型"
                                    prop="unique_count">
                                    <template v-slot="scope">
                                        <p>{{JSON.stringify(scope.row.unique_count) === '{}' ? '连续型' : '离散型'}}</p>
                                    </template>
                                </el-table-column>
                                <el-table-column label="非空值数量 / 缺失数量">
                                    <template v-slot="scope">
                                        <p>{{scope.row.not_null_count}} / {{scope.row.missing_count}}</p>
                                    </template>
                                </el-table-column>
                            </el-table>
                        </el-tab-pane>
                    </el-tabs>
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
    import { reactive, ref, onBeforeMount, getCurrentInstance, nextTick } from 'vue';
    import CommonResult from '../common/CommonResult';
    import resultMixin from '../result-mixin';

    const mixin = resultMixin();

    export default {
        name:       'FeatureStatistic',
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
                tabName:     '',
                members:     [],
                resultTypes: [],
                activeTab:   'overview',
            });
            const PieChart = ref(), BarChart = ref();
            const { appContext } = getCurrentInstance();
            const { $bus } = appContext.config.globalProperties;

            let methods = {
                numberSum(arr) {
                    let total = 0;

                    for (let i=0; i<arr.length; i++) {
                        total += arr[i];
                    }
                    return total;
                },
                showResult(data) {
                    vData.members = [];
                    if (data.result && data.result.members) {
                        const { members } = data.result;

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

                                for (let i=0; i<Object.values(unique_count).length; i++) {
                                    pieSeries.push({
                                        name:  Object.keys(unique_count)[i],
                                        value: Object.values(unique_count)[i],
                                    });
                                    list.push({
                                        category:  Object.keys(unique_count)[i],
                                        count:     Object.values(unique_count)[i],
                                        frequency: Object.values(unique_count)[i] / methods.numberSum(Object.values(unique_count)),
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
                                    pieChartData:  {
                                        titleText: key,
                                        series:    pieSeries,
                                    },
                                    categoryTable: list,
                                    median,
                                    q1,
                                    q95,
                                });
                                table[table.length-1].overviewtable.push(val);
                            }

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
                    switch (vData.activeTab) {
                    case 'categories':
                        PieChart.value.chartResize();
                        break;
                    case 'histogram':
                        BarChart.value.chartResize();
                        break;
                    }
                },
            };

            const { $data, $methods } = mixin.mixin({
                props,
                context,
                vData,
                methods,
                PieChart,
                BarChart,
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
                PieChart,
                BarChart,
            };
        },
    };
</script>
<style lang="scss" scoped>
.fold-table {
    :deep(.el-table__expanded-cell) {
        padding: 20px;
    }
    :deep(.el-descriptions__header) {
        padding: 10px;
        border: 1px solid #ebeef5;
        margin-bottom: 0;
        text-align: center;
    }
}
</style>
