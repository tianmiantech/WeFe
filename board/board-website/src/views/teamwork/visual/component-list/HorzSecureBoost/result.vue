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
                    <template v-if="vData.result">
                        <el-row>
                            <el-col :span="12">
                                迭代次数: {{ vData.loss.iters }}
                            </el-col>
                            <el-col
                                v-if="vData.loss.isConverged != null"
                                :span="12"
                            >
                                是否收敛:
                                {{ vData.loss.isConverged ? '是' : '否' }}
                            </el-col>
                        </el-row>
                    </template>
                </el-collapse-item>
                <el-collapse-item
                    title="特征重要性"
                    name="2"
                    v-if="vData.featureImportances"
                >
                    <el-tabs v-model="tabPostion" @tab-change="TabChangeHandle">
                        <el-tab-pane
                            v-for="(
                                value, key, index
                            ) in vData.featureImportances"
                            :key="key"
                            :name="index"
                            :label="key.split(':')[0]"
                        >
                            <el-table
                                :data="value"
                                :default-sort="{
                                    prop: 'importanceShow',
                                    order: 'descending',
                                }"
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
                                <el-table-column prop="fullname" label="特征" />
                                <el-table-column prop="importanceShow" label="重要性" sortable />
                            </el-table>
                        </el-tab-pane>
                    </el-tabs>
                </el-collapse-item>
                <el-collapse-item
                    title="任务跟踪指标（LOSS）"
                    name="3"
                    v-if="
                        vData.result &&
                            vData.loss.series.reduce(
                                (acc, cur) => acc + cur.length,
                                0
                            )
                    "
                >
                    <LineChart v-if="vData.loss.show" :config="vData.loss" />
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

    const mixin = resultMixin();

    export default {
        name:       'HorzSecureBoost',
        components: {
            CommonResult,
        },
        props: {
            ...mixin.props,
        },
        setup(props, context) {
            const activeName = ref('1');
            const tabPostion = ref(0);

            let vData = reactive({
                result:             null,
                resultTypes:        [],
                featureImportances: false,
                loss:               {
                    show:        false,
                    loading:     true,
                    xAxis:       [],
                    series:      [[]],
                    iters:       0,
                    isConverged: null,
                },
                pollingOnJobRunning: true,
            });

            let methods = {
                showResult(data) {
                    if (data[0].result && data[0].result.model_param) {
                        vData.result = true;
                        const {
                            losses,
                            isConverged,
                            lossHistory,
                            featureImportances,
                        } = data[0].result.model_param;
                        const { train_best_parameters, train_params_list } =
                            data[0].result;

                        losses.forEach((item, index) => {
                            vData.loss.xAxis.push(index);
                            vData.loss.series[0].push(item);
                        });
                        vData.loss.iters = losses.length;
                        vData.loss.isConverged = isConverged;
                        vData.loss.lossHistory = lossHistory;
                        vData.featureImportances = featureImportances
                            .map((each) => ({
                                ...each,
                                importanceShow:
                                    Math.round(
                                        each[
                                            each.main === 'split'
                                                ? 'importance2'
                                                : 'importance'
                                        ] * 1e2,
                                    ) / 1e2,
                            }))
                            .reduce(
                                (acc, cur) => ({
                                    ...acc,
                                    [cur.sitename]: Reflect.has(acc, cur.sitename)
                                        ? [...acc[cur.sitename], cur]
                                        : [cur],
                                }),
                                {},
                            );
                        vData.loss.loading = false;
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
                        }
                    } else {
                        vData.result = false;
                    }
                },
                collapseChanged(val) {
                    if (val.includes('3')) {
                        vData.loss.show = true;
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
            const TabChangeHandle = (e) => (tabPostion.value = e);
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
                tabPostion,
                TabChangeHandle,
            };
        },
    };
</script>
