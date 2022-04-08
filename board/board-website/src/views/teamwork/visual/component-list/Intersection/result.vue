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
            <div v-if="vData.resultConfigs.length">
                <el-divider></el-divider>
                <template
                    v-for="(result, $index) in vData.resultConfigs"
                    :key="$index"
                >
                    <strong class="mb10">{{ result.titleText }}</strong>
                    <!-- <PieChart
                        :ref="piechartRefs[$index]"
                        :config="result.chart"
                    /> -->
                    <div v-for="item in vData.textList" :key="item" class="mt10">
                        <p class="mb5"><el-tag type="success" size="small">已对齐</el-tag><span class="f14">：{{item.intersect_count}}</span></p>
                        <p><el-tag type="warning" size="small">未对齐</el-tag><span class="f14">：{{item.count - item.intersect_count}}</span></p>
                    </div>

                </template>
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
        setup(props, context) {
            const { appContext } = getCurrentInstance();
            const { $bus } = appContext.config.globalProperties;

            let piechartRefs = [];

            let vData = reactive({
                resultTypes:   ['metric'],
                resultConfigs: [],
                textList:      [],
            });

            let methods = {
                showResult(list) {
                    console.log(list);
                    piechartRefs = [];
                    vData.resultConfigs = [];
                    vData.textList = [];
                    list.forEach((data, index) => {
                        const titleText = data.members.map(m => `${m.member_name} (${m.member_role})`).join(' & ');
                        const result = {
                            titleText,
                            chart: {
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
                            },
                        };

                        vData.textList.push(data.result);
                        vData.resultConfigs.push(result);
                        piechartRefs.push(ref(index));
                    });
                },
            };

            onBeforeMount(() => {
                $bus.$on('drag-end', _ => {
                    console.log(0);
                    piechartRefs.forEach($ref => {
                        if ($ref.value) {
                            nextTick(_=> {
                                $ref.value.chartResize();
                            });
                        }
                    });
                });
            });

            const { $data, $methods } = mixin.mixin({
                props,
                context,
                vData,
                methods,
                piechartRefs,
            });

            vData = $data;
            methods = $methods;

            return {
                vData,
                methods,
                piechartRefs,
            };
        },
    };
</script>
