<template>
    <div
        v-loading="vData.loading"
        class="result"
    >
        <template v-if="vData.commonResultData.task">
            <el-collapse v-model="activeName" @change="methods.collapseChanged">
                <el-collapse-item
                    title="基础信息"
                    name="1"
                >
                    <CommonResult
                        :result="vData.commonResultData"
                        :currentObj="currentObj"
                        :jobDetail="jobDetail"
                    />
                </el-collapse-item>
                <el-collapse-item
                    v-if="vData.results.length"
                    title="特征权重"
                    name="2"
                >
                    <template
                        v-for="(result, index) in vData.results"
                        :key="index"
                    >
                        <p class="mb10"><strong>{{ result.title }} :</strong></p>
                        <el-table
                            :data="result.tableData"
                            style="max-width:355px;"
                            max-height="600px"
                            class="mb20"
                            stripe
                            border
                        >
                            <el-table-column
                                type="index"
                                label="序号"
                                width="60"
                            />
                            <el-table-column
                                prop="feature"
                                label="特征"
                                width="80"
                            />
                            <el-table-column
                                prop="weight"
                                label="权重"
                            >
                                <template v-slot="scope">
                                    {{ dealNumPrecision(scope.row.weight) }}
                                </template>
                            </el-table-column>
                        </el-table>
                        <el-divider v-if="index === 0"></el-divider>
                    </template>
                </el-collapse-item>
                <el-collapse-item
                    v-if="vData.results.length"
                    title="任务跟踪指标（LOSS）"
                    name="3"
                >
                    <LineChart
                        v-if="vData.train_loss.show"
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
    import {
        ref, reactive,
    } from 'vue';
    import CommonResult from '../common/CommonResult';
    import resultMixin from '../result-mixin';
    import gridSearchParams from '../../../../../assets/js/const/gridSearchParams';
    import { dealNumPrecision } from '@src/utils/utils';

    const mixin = resultMixin();

    export default {
        name:       'MixLR',
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
                train_loss:          {
                    show:   false,
                    xAxis:  [],
                    series: [[]],
                },
            });

            let methods = {
                showResult(list) {
                    vData.results = list.map(data => {
                        const result = {
                            title:     data.members.map(m => `${m.member_name} (${m.member_role})`).join(' & '),
                            tableData: [],
                        };
                        const {
                            model_param: {
                                intercept,
                                weight,
                            },
                            train_best_parameters,
                            train_params_list,
                            train_loss,
                        } = data.result;

                        for(const key in weight) {
                            result.tableData.push({
                                feature: key,
                                weight:  weight[key],
                            });
                        }
                        result.tableData.push({
                            feature: 'b',
                            weight:  intercept,
                        });

                        if (train_params_list) {
                            const gridParams =
                                train_params_list.data.params_list.value;

                            for (const key in gridParams) {
                                const bestParam =
                                    train_best_parameters.data.best_parameters
                                        .value[key];

                                gridParams[key] = gridParams[key].map((value) => ({
                                    value,
                                    best: value === bestParam,
                                }));
                            }
                            vData.gridParams = gridParams;
                            const bestLossItem =
                                data[0].result[
                                    `train_loss.${train_best_parameters.data.best_iter.value}`
                                ];

                            if (bestLossItem) {
                                Object.entries(bestLossItem.data).forEach(
                                    ([index, item]) => {
                                        vData.train_loss.xAxis.push(index);
                                        vData.train_loss.series[0].push(dealNumPrecision(item.value));
                                    },
                                );
                            }
                        } else {
                            if (train_loss) {
                                train_loss.data.forEach((item, index) => {
                                    vData.train_loss.xAxis.push(index);
                                    vData.train_loss.series[0].push(dealNumPrecision(item));
                                });
                            }
                        }

                        return result;
                    });
                },
                collapseChanged(val) {
                    if (val.includes('3')) {
                        vData.train_loss.show = true;
                    }
                },
            };

            const toCamelCase = (str) =>
                str
                    .split('_')
                    .reduce((acc, cur, index) =>
                        index === 0
                            ? cur
                            : acc +
                                String.prototype.toUpperCase.call(cur[0]) +
                                cur.slice(1),
                    );
            const mapGridName = (key) =>
                gridSearchParams.xgboost
                    .concat(gridSearchParams.lr)
                    .find(
                        (each) => each.key === key || each.key === toCamelCase(key),
                    ).label;

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
                mapGridName,
                dealNumPrecision,
            };
        },
    };
</script>
