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
                    <CommonResult :result="vData.commonResultData" />
                </el-collapse-item>

                <el-collapse-item
                    v-if="vData.results.length"
                    title="成员信息"
                    name="2"
                >
                    <template
                        v-for="(result, index) in vData.results"
                        :key="index"
                    >
                        <strong class="mb10">{{ result.title }} :</strong>
                        <el-tabs v-if="vData.showCharts" v-model="result.tabName">
                            <el-tab-pane
                                v-for="member in result.members"
                                :key="`${member.member_id}-${member.member_role}`"
                                :name="`${member.member_id}-${member.member_role}`"
                                :label="`${member.member_name} (${member.member_role === 'provider' ? '协作方' : '发起方'})`"
                            >
                                <p class="mb10 pb5">分布:</p>
                                <el-table
                                    :data="member.table"
                                    stripe
                                    border
                                >
                                    <el-table-column
                                        label="列名"
                                        prop="feature"
                                    />
                                    <el-table-column
                                        label="最小值"
                                        prop="min"
                                    >
                                        <template v-slot="scope">
                                            {{ dealNumPrecision(scope.row.min) }}
                                        </template>
                                    </el-table-column>
                                    <el-table-column
                                        label="最大值"
                                        prop="max"
                                    >
                                        <template v-slot="scope">
                                            {{ dealNumPrecision(scope.row.max) }}
                                        </template>
                                    </el-table-column>
                                    <el-table-column
                                        label="平均值"
                                        prop="mean"
                                    >
                                        <template v-slot="scope">
                                            {{ dealNumPrecision(scope.row.mean) }}
                                        </template>
                                    </el-table-column>
                                    <!-- <el-table-column
                                    label="众数"
                                    prop="mode"
                                >
                                    <template v-slot="scope">
                                        <p v-if="scope.row.mode.length">
                                            {{ scope.row.mode[0][0] }}({{ scope.row.mode[0][1] }}次)
                                        </p>
                                    </template>
                                </el-table-column> -->
                                    <el-table-column
                                        label="实际数量"
                                        prop="not_null_count"
                                    />
                                    <el-table-column
                                        label="缺失数量"
                                        prop="missing_count"
                                    />
                                    <el-table-column
                                        label="峰度"
                                        prop="kurtosis"
                                    >
                                        <template v-slot:header>
                                            <el-popover
                                                trigger="hover"
                                                placement="top-start"
                                                content="表征概率密度分布曲线在平均值处峰值高低的特征数"
                                            >
                                                <template #reference>峰度<el-icon>
                                                    <elicon-warning-filled />
                                                </el-icon>
                                                </template>
                                            </el-popover>
                                        </template>
                                        <template v-slot="scope">
                                            {{ dealNumPrecision(scope.row.kurtosis) }}
                                        </template>
                                    </el-table-column>
                                    <el-table-column
                                        label="偏态"
                                        prop="skewness"
                                    >
                                        <template v-slot:header>
                                            <el-popover
                                                trigger="hover"
                                                placement="top-start"
                                                content="统计数据分布偏斜方向和程度的度量，是统计数据分布非对称程度的数字特征"
                                            >
                                                <template #reference>偏态<el-icon>
                                                    <elicon-warning-filled />
                                                </el-icon>
                                                </template>
                                            </el-popover>
                                        </template>
                                        <template v-slot="scope">
                                            {{ dealNumPrecision(scope.row.skewness) }}
                                        </template>
                                    </el-table-column>
                                    <el-table-column
                                        label="标准差"
                                        prop="std_variance"
                                    >
                                        <template v-slot="scope">
                                            {{ dealNumPrecision(scope.row.std_variance) }}
                                        </template>
                                    </el-table-column>
                                    <el-table-column
                                        label="方差"
                                        prop="variance"
                                    >
                                        <template v-slot="scope">
                                            {{ dealNumPrecision(scope.row.variance) }}
                                        </template>
                                    </el-table-column>
                                    <el-table-column
                                        label="唯一值"
                                        min-width="180"
                                    >
                                        <template v-slot="scope">
                                            <div v-if="Object.keys(scope.row.unique_count).length" style="max-height:400px;overflow: auto;">
                                                <p
                                                    v-for="(value, key) in scope.row.unique_count"
                                                    :key="key"
                                                >
                                                    {{ key }} : {{ value }}
                                                </p>
                                            </div>
                                            <template v-else>-</template>
                                        </template>
                                    </el-table-column>
                                    <el-table-column
                                        label="分布图"
                                        min-width="330"
                                    >
                                        <template v-slot="scope">
                                            <PieChart
                                                v-if="`${member.member_id}-${member.member_role}` === result.tabName"
                                                :config="scope.row.distributionChart"
                                            />
                                        </template>
                                    </el-table-column>
                                    <el-table-column
                                        label="百分位图"
                                        min-width="330"
                                    >
                                        <template v-slot="scope">
                                            <LineChart
                                                v-if="`${member.member_id}-${member.member_role}` === result.tabName"
                                                :config="scope.row.percentileChart"
                                            />
                                        </template>
                                    </el-table-column>
                                </el-table>
                            </el-tab-pane>
                        </el-tabs>
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
    import { dealNumPrecision } from '@src/utils/utils';

    const mixin = resultMixin();

    export default {
        name:       'MixStatistic',
        components: {
            CommonResult,
        },
        props: {
            ...mixin.props,
        },
        setup(props, context) {
            const activeName = ref('1');

            let vData = reactive({
                showCharts:  false,
                results:     [],
                resultTypes: [],
            });

            let methods = {
                showResult(list) {
                    vData.results = list.map(data => {
                        const { members } = data.result;
                        const result = {
                            title:   data.members.map(m => `${m.member_name} (${m.member_role})`).join(' & '),
                            tabName: '',
                            members: [],
                        };

                        if (data.result && data.result.members) {
                            members.forEach((member, index) => {
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
                                    } = val;

                                    for(let i = 0; i < 10; i++) {
                                        distributionChartxAxis.push(i);
                                        distributionChartSeries.push({
                                            name:  i,
                                            value: distribution[i] || 0,
                                        });
                                    }

                                    // percentileChart
                                    const percents = Object.keys(percentile);

                                    if(!percents.includes(0)) {
                                        percents.unshift(0);
                                    }
                                    if(!percents.includes(100)) {
                                        percents.push(100);
                                    }

                                    const percentilexAxis = [], percentileSeries = [];

                                    percents.forEach(percent => {
                                        const val = percentile[percent];

                                        percentilexAxis.push(percent);
                                        percentileSeries.push(
                                            dealNumPrecision(percent === 0 ? min : (percent === 100 ? max : val)),
                                        );
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
                                    });
                                }

                                if(index === 0) {
                                    result.tabName = `${member.member_id}-${member.role}`;
                                }

                                result.members.push({
                                    member_id:   member.member_id,
                                    member_name: member.member_name,
                                    member_role: member.role,
                                    table,
                                });
                            });
                        }

                        return result;
                    });
                },
                collapseChanged(val) {
                    if(val.includes('2')){
                        vData.showCharts = true;
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
                dealNumPrecision,
            };
        },
    };
</script>
