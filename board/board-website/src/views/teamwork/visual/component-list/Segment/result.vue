<template>
    <div
        v-loading="vData.loading"
        class="result"
    >
        <el-collapse
            v-if="vData.commonResultData.task"
            v-model="activeName"
            @change="methods.collapseChanged"
        >
            <el-collapse-item title="基础信息" name="1">
                <CommonResult
                    v-if="vData.commonResultData.task"
                    :result="vData.commonResultData"
                    :currentObj="currentObj"
                    :jobDetail="jobDetail"
                />
            </el-collapse-item>
            <el-collapse-item
                title="模型结果"
                name="2"
            >
                <div
                    v-if="vData.showCharts"
                    class="pie-wrapper"
                >
                    <div class="pie">
                        <div class="table mb10">
                            <el-table
                                :data="vData.resultTableData"
                                border
                                striped
                            >
                                <el-table-column
                                    label="样本占比"
                                    align="center"
                                    prop="example_ratio"
                                    width="120">
                                    <template v-slot="scope">
                                        {{scope.$index ? '测试集' : '训练集'}} ({{ scope.row.example_ratio }}%)
                                    </template>
                                </el-table-column>
                                <el-table-column
                                    prop="example_total"
                                    align="center"
                                    label="样本数量"
                                    width="120">
                                </el-table-column>
                                <el-table-column
                                    prop="example_good"
                                    align="center"
                                    label="正样本">
                                    <template v-slot="scope">
                                        {{scope.row.example_good}}({{scope.row.example_ratio_good}}%)
                                    </template>
                                </el-table-column>
                                <el-table-column
                                    prop="example_bad"
                                    align="center"
                                    label="负样本">
                                    <template v-slot="scope">
                                        {{scope.row.example_bad}}({{scope.row.example_ratio_bad}}%)
                                    </template>
                                </el-table-column>
                                <el-table-column
                                    align="center"
                                    label="正 : 负"
                                    width="150">
                                    <template v-slot="scope">
                                        {{scope.row.example_good / scope.row.example_good}} : {{(scope.row.example_bad / scope.row.example_good).toFixed(2)}}
                                    </template>
                                </el-table-column>
                            </el-table>
                        </div>
                        <!-- <div class="flex count"> -->
                        <PieChart
                            ref="trainPiechartRef"
                            :config="vData.trainCountConfig"
                        />
                        <PieChart
                            ref="validatePieChartRef"
                            :config="vData.verifyCountConfig"
                        />
                        <!-- </div> -->
                    </div>
                </div>
            </el-collapse-item>
        </el-collapse>
        <div
            v-else
            class="data-empty"
        >
            查无结果!
        </div>
    </div>
</template>

<script>
    import { ref, reactive, onBeforeMount, getCurrentInstance, nextTick } from 'vue';
    import CommonResult from '../common/CommonResult';
    import resultMixin from '../result-mixin';

    const mixin = resultMixin();

    export default {
        name:       'Segment',
        components: {
            CommonResult,
        },
        props: {
            ...mixin.props,
        },
        emits: [...mixin.emits],
        setup(props, context) {
            const trainPiechartRef = ref();
            const validatePieChartRef = ref();
            const { appContext } = getCurrentInstance();
            const { $bus } = appContext.config.globalProperties;

            let vData = reactive({
                showCharts:        false,
                trainCountConfig:  {},
                train_count:       0,
                eval_count:        0,
                verifyCountConfig: {},
                resultTableData:   [],
            });

            let methods = {
                showResult(data) {
                    if (data.result) {
                        vData.train_count = data.result.train_count;
                        vData.eval_count = data.result.eval_count;

                        vData.resultTableData.push(
                            // train
                            {
                                example_ratio:       ((vData.train_count / (vData.train_count + vData.eval_count)) * 100).toFixed(2),
                                example_total:       vData.train_count,
                                example_good:        data.result.train_count - data.result.train_y_positive_example_count,
                                example_bad:         data.result.train_y_positive_example_count,
                                example_ratio_good:  ((1 - data.result.train_y_positive_example_ratio) * 100).toFixed(2),
                                example_ratio_bad:   (data.result.train_y_positive_example_ratio * 100).toFixed(2),
                                example_count_ratio: 0,
                            },
                            // test
                            {
                                example_ratio:       ((vData.eval_count / (vData.train_count + vData.eval_count)) * 100).toFixed(2),
                                example_total:       vData.eval_count,
                                example_good:        data.result.eval_count - data.result.eval_y_positive_example_count,
                                example_bad:         data.result.eval_y_positive_example_count,
                                example_ratio_good:  ((1 - data.result.eval_y_positive_example_ratio) * 100).toFixed(2),
                                example_ratio_bad:   data.result.eval_y_positive_example_ratio * 100,
                                example_count_ratio: 0,
                            },
                        );

                        // train
                        vData.trainCountConfig = {
                            name:         `样本总数：${data.result.train_count}`,
                            titleText:    '训练集',
                            legend:       ['负样本数量','正样本数量'],
                            legendLeft:   'left',
                            legendOrient: 'vertical',
                            labelShow:    true,
                            series:       [{
                                name:  '负样本数量',
                                value: data.result.train_y_positive_example_count,
                            },{
                                name:  '正样本数量',
                                value: data.result.train_count - data.result.train_y_positive_example_count,
                            }],
                        };

                        // test
                        vData.verifyCountConfig = {
                            name:         `样本总数：${data.result.eval_count}`,
                            titleText:    '测试集',
                            legend:       ['负样本数量','正样本数量'],
                            legendLeft:   'left',
                            legendOrient: 'vertical',
                            labelShow:    true,
                            series:       [{
                                name:  '负样本数量',
                                value: data.result.eval_y_positive_example_count,
                            },{
                                name:  '正样本数量',
                                value: data.result.eval_count - data.result.eval_y_positive_example_count,
                            }],
                        };
                    }
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
                trainPiechartRef,
                validatePieChartRef,
            });

            onBeforeMount(() => {
                $bus.$on('drag-end', _ => {
                    if (trainPiechartRef.value && validatePieChartRef.value) {
                        nextTick(_=> {
                            trainPiechartRef.value.chartResize();
                            validatePieChartRef.value.chartResize();
                        });
                    }
                });
            });

            vData = $data;
            methods = $methods;

            return {
                vData,
                methods,
                trainPiechartRef,
                validatePieChartRef,
            };
        },
    };
</script>

<style lang="scss" scoped>
.pie-wrapper {
    .flex {
        display: flex;
        justify-content: space-between;
        align-items: center;
        flex-wrap: wrap;
        >div {
            flex: 1;
        }
    }
    .pie {
        p {
            font-size: 13px;
            color: #999;
        }
    }
}
</style>
