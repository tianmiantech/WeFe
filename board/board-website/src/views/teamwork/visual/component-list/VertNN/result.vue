<template>
    <div
        v-loading="vData.loading"
        class="result"
    >
        <template v-if="vData.commonResultData.task">
            <CommonResult
                :result="vData.commonResultData"
                :currentObj="currentObj"
                :jobDetail="jobDetail"
            />
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
    import { reactive } from 'vue';
    import CommonResult from '../common/CommonResult';
    import resultMixin from '../result-mixin';

    const mixin = resultMixin();

    export default {
        name:       'VertNN',
        components: {
            CommonResult,
        },
        props: {
            ...mixin.props,
        },
        setup(props, context) {
            let vData = reactive({
                result:     null,
                role:       'promoter',
                tableData:  [],
                train_loss: {
                    columns: ['x', 'loss'],
                    rows:    [],
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
                methods,
            };
        },
    };
</script>
