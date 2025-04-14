<template>
    <div v-loading="vData.loading" class="result">
        <template v-if="vData.commonResultData.task">
            <el-collapse v-model="activeName" @change="methods.collapseChanged">
                <el-collapse-item title="基础信息" name="1">
                    <CommonResult
                        :result="vData.commonResultData"
                        :currentObj="currentObj"
                        :jobDetail="jobDetail"
                    />
                </el-collapse-item>
                <template v-if="vData.result">
                    <el-collapse-item
                        v-if="vData.label_species_count<=2"
                        title="特征权重"
                        name="2"
                    >
                        <el-table
                            :data="vData.tableData"
                            style="max-width: 355px"
                            max-height="600px"
                            class="mt10"
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
                            <el-table-column prop="weight" label="权重">
                                <template v-slot="scope">
                                    {{ dealNumPrecision(scope.row.weight) }}
                                </template>
                            </el-table-column>
                        </el-table>
                    </el-collapse-item>
                    <el-collapse-item
                        v-if="vData.label_species_count<=2"
                        title="任务跟踪指标（LOSS）"
                        name="3"
                    >
                        <LineChart
                            v-if="vData.train_loss.show"
                            :config="vData.train_loss"
                        />
                    </el-collapse-item>
                    <el-collapse-item
                        v-if="vData.gridParams"
                        title="网格搜索结果"
                        name="4"
                    >
                        <el-descriptions>
                            <el-descriptions-item
                                v-for="(values, key) in vData.gridParams"
                                :key="key"
                                :label="mapGridName(key)"
                            >
                                <el-tag
                                    v-for="item in values"
                                    :key="item"
                                    :style="{ margin: '8px' }"
                                >{{ item.value }}
                                    <el-tooltip v-if="item.best">
                                        <template #content> 最优参数 </template>
                                        <el-icon color="gold"
                                        ><elicon-trophy
                                        /></el-icon>
                                    </el-tooltip>
                                </el-tag>
                            </el-descriptions-item>
                        </el-descriptions>
                    </el-collapse-item>
                </template>
            </el-collapse>
        </template>
        <div v-else class="data-empty">查无结果!</div>
    </div>
</template>

<script>
    import { ref, reactive } from 'vue';
    import CommonResult from '../common/CommonResult';
    import resultMixin from '../result-mixin';
    import gridSearchParams from '../../../../../assets/js/const/gridSearchParams';
    import { dealNumPrecision } from '@src/utils/utils';

    const mixin = resultMixin();

    export default {
        name:       'VertLR',
        components: {
            CommonResult,
        },
        props: {
            ...mixin.props,
        },
        setup(props, context) {
            const activeName = ref('1');

            let vData = reactive({
                result:     null,
                role:       'promoter',
                tableData:  [],
                train_loss: {
                    show:   false,
                    xAxis:  [],
                    series: [[]],
                },
                resultTypes:         [],
                pollingOnJobRunning: true,
            });

            let methods = {
                showResult(data) {
                    if (data[0].result) {
                        vData.result = true;
                        const {
                            model_param: { intercept, weight },
                            train_best_parameters,
                            train_params_list,
                            train_loss,
                        } = data[0].result;

                        vData.tableData = [];
                        for (const key in weight) {
                            vData.tableData.push({
                                feature: key,
                                weight:  weight[key],
                            });
                        }
                        vData.tableData.push({
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
                    } else {
                        vData.result = false;
                    }
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
