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
            <div
                v-if="vData.resultConfig"
                class="mt20"
            >
                <PieChart
                    ref="piechartRef"
                    :config="vData.resultConfig"
                />
            </div>
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
        name:       'Intersection',
        components: {
            CommonResult,
        },
        props: {
            ...mixin.props,
        },
        emits: [...mixin.emits],
        setup(props, context) {
            const { appContext } = getCurrentInstance();
            const { $bus } = appContext.config.globalProperties;
            const piechartRef = ref();

            let vData = reactive({
                resultTypes:  ['metric'],
                resultConfig: {},
            });

            let methods = {
                showResult(data) {
                    vData.resultConfig = {
                        titleText:    '',
                        name:         `数据总量：${data.result.count}`,
                        legend:       ['对齐数据量','未对齐数据量'],
                        legendLeft:   'left',
                        legendOrient: 'vertical',
                        labelShow:    true,
                        series:       [{
                            name:  '对齐数据量',
                            value: data.result.intersect_count,
                        }, {
                            name:  '未对齐数据量',
                            value: data.result.count - data.result.intersect_count,
                        }],
                    };
                },
            };

            onBeforeMount(() => {
                $bus.$on('drag-end', _ => {
                    if (piechartRef.value) {
                        nextTick(_=> {
                            piechartRef.value.chartResize();
                        });
                    }
                });
            });

            const { $data, $methods } = mixin.mixin({
                props,
                context,
                vData,
                methods,
                piechartRef,
            });

            vData = $data;
            methods = $methods;

            return {
                vData,
                methods,
                piechartRef,
            };
        },
    };
</script>
