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
                    <CommonResult
                        :result="vData.commonResultData"
                        :currentObj="currentObj"
                        :jobDetail="jobDetail"
                    />
                </el-collapse-item>
                <template v-if="vData.result">
                    <el-collapse-item
                        title="特征权重"
                        name="2"
                    >
                        <el-table
                            :data="vData.tableData"
                            style="max-width:355px;"
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
                            <el-table-column
                                prop="weight"
                                label="权重"
                            />
                        </el-table>
                    </el-collapse-item>
                    <el-collapse-item
                        title="任务跟踪指标（LOSS）"
                        name="3"
                    >
                        <LineChart
                            v-if="vData.train_loss.show"
                            :config="vData.train_loss"
                        />
                    </el-collapse-item>
                </template>
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
    import { ref, reactive } from 'vue';
    import CommonResult from '../common/CommonResult';
    import resultMixin from '../result-mixin';

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
                    if(data[0].result) {
                        vData.result = true;
                        const {
                            model_param: {
                                intercept,
                                weight,
                            },
                            train_loss,
                        } = data[0].result;

                        vData.tableData = [];
                        for(const key in weight) {
                            vData.tableData.push({
                                feature: key,
                                weight:  weight[key],
                            });
                        }
                        vData.tableData.push({
                            feature: 'b',
                            weight:  intercept,
                        });

                        train_loss.data.forEach((item, index) => {
                            vData.train_loss.xAxis.push(index);
                            vData.train_loss.series[0].push(item);
                        });
                    } else {
                        vData.result = false;
                    }
                },
                collapseChanged(val) {
                    if(val.includes('3')){
                        vData.train_loss.show = true;
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
            };
        },
    };
</script>
