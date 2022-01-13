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
                    <div
                        v-for="(result, index) in vData.results"
                        :key="index"
                        class="pie"
                    >
                        <strong>{{ result.title }} :</strong>
                        <div class="table mb10 mt10">
                            <el-table
                                :data="result.resultTableData"
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
                            :ref="result.pieCharts[0]"
                            :config="result.trainCountConfig"
                        />
                        <PieChart
                            :ref="result.pieCharts[1]"
                            :config="result.verifyCountConfig"
                        />
                        <!-- </div> -->
                        <el-divider v-if="index === 0"></el-divider>
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
        setup(props, context) {
            const activeName = ref('1');
            const { appContext } = getCurrentInstance();
            const { $bus } = appContext.config.globalProperties;

            let vData = reactive({
                showCharts: false,
                results:    [],
            });

            let methods = {
                showResult(list) {
                    vData.results = list.map((data, index) => {
                        const result = {
                            title:            data.members.map(m => `${m.member_name} (${m.member_role})`).join(' & '),
                            train_count:      '',
                            eval_count:       '',
                            resultTableData:  [],
                            trainCountConfig: {},
                            pieCharts:        [ref(0), ref(1)],
                        };

                        if (data.result) {
                            result.train_count = data.result.train_count;
                            result.eval_count = data.result.eval_count;

                            result.resultTableData.push(
                                // train
                                {
                                    example_ratio:       ((result.train_count / (result.train_count + result.eval_count)) * 100).toFixed(2),
                                    example_total:       result.train_count,
                                    example_good:        data.result.train_count - data.result.train_y_positive_example_count,
                                    example_bad:         data.result.train_y_positive_example_count,
                                    example_ratio_good:  ((1 - data.result.train_y_positive_example_ratio) * 100).toFixed(2),
                                    example_ratio_bad:   (data.result.train_y_positive_example_ratio * 100).toFixed(2),
                                    example_count_ratio: 0,
                                },
                                // test
                                {
                                    example_ratio:       ((result.eval_count / (result.train_count + result.eval_count)) * 100).toFixed(2),
                                    example_total:       result.eval_count,
                                    example_good:        data.result.eval_count - data.result.eval_y_positive_example_count,
                                    example_bad:         data.result.eval_y_positive_example_count,
                                    example_ratio_good:  ((1 - data.result.eval_y_positive_example_ratio) * 100).toFixed(2),
                                    example_ratio_bad:   data.result.eval_y_positive_example_ratio * 100,
                                    example_count_ratio: 0,
                                },
                            );

                            // train
                            result.trainCountConfig = {
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
                            result.verifyCountConfig = {
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

            onBeforeMount(() => {
                $bus.$on('drag-end', _ => {
                    vData.results.forEach(result => {
                        result.pieCharts.forEach(ref => {
                            if (ref.value) {
                                nextTick(_=> {
                                    ref.value.chartResize();
                                });
                            }
                        });
                    });
                });
            });

            vData = $data;
            methods = $methods;

            return {
                vData,
                methods,
                activeName,
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
