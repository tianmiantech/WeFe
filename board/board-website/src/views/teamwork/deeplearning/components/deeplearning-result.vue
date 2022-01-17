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
                        :showHistory="false"
                    />
                </el-collapse-item>
                <el-collapse-item title="任务跟踪指标" name="2">
                    <el-tabs v-model="vData.expandparams.type" @tab-click="methods.tabChange">
                        <el-tab-pane label="Loss" name="loss"></el-tab-pane>
                        <!-- <el-tab-pane label="Accuracy" name="accuracy"></el-tab-pane> -->
                    </el-tabs>
                    <LineChart
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
    import { ref, reactive } from 'vue';
    import CommonResult from '../../visual/component-list/common/CommonResult.vue';
    import resultMixin from '../../visual/component-list/result-mixin';

    const mixin = resultMixin();

    export default {
        components: {
            CommonResult,
        },
        props: {
            ...mixin.props,
        },
        setup(props, context) {
            const activeName = ref('1');

            let vData = reactive({
                header:       [],
                datasetList:  [],
                expandparams: {
                    type: 'loss',
                },
                train_loss: {},
            });

            let methods = {
                tabChange() {
                    methods.readData();
                },
                showResult(data) {
                    if(data.result) {
                        vData.result = true;
                        const { train_loss } = data.result;

                        train_loss.data.forEach((item, index) => {
                            vData.train_loss.xAxis.push(index);
                            vData.train_loss.series[0].push(item);
                        });
                    } else {
                        vData.result = false;
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
