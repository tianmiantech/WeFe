<template>
    <div class="chart-wrap">
        <div
            v-if="vData.seriesLength === 0"
            class="chart-empty"
        >暂无数据</div>

        <div
            ref="chartDom"
            class="chart"
        />
    </div>
</template>

<script>
    import { init, use } from 'echarts/core';
    import { PieChart } from 'echarts/charts';
    import { CanvasRenderer } from 'echarts/renderers';
    import {
        TooltipComponent,
        LegendComponent,
    } from 'echarts/components';
    import {
        ref,
        reactive,
        onMounted,
        onBeforeUnmount,
    } from 'vue';

    use([
        PieChart,
        CanvasRenderer,
        TooltipComponent,
        LegendComponent,
    ]);

    export default {
        name:  'BarChartNew',
        props: {
            config: Object,
        },
        setup (props) {
            let chart;
            const chartDom = ref();
            const vData = reactive({
                seriesLength: 0,
            });

            console.log(props.config);
            const options = {
                tooltip: {
                    confine:     true,
                    trigger:     'axis',
                    axisPointer: {
                        type: 'shadow',
                    },
                    transitionDuration: 0,
                },
                grid:   props.config.grid || {},
                xAxis:  props.config.xAxis,
                yAxis:  props.config.yAxis,
                legend: {},
                series: props.config.series,
            };

            // init chart
            const initChart = () => {
                if(chartDom.value.offsetWidth) {
                    chart = init(chartDom.value);
                }
            };

            // update chart data
            const changeData = () => {
                if(props.config.series) {
                    vData.seriesLength = props.config.series.length;
                    options.legend = props.config.legend || [];

                    if(chart) {
                        chart.clear();
                    } else {
                        initChart();
                    }

                    chart && chart.setOption(options);
                }
            };

            // resize chart
            const chartResize = () => {
                setTimeout(() => {
                    changeData();
                    chart && chart.resize({
                        animation: {
                            duration: 300,
                            easing:   'linear',
                        },
                    });
                });
            };

            onMounted(() => {
                if(props.config) {
                    setTimeout(() => {
                        initChart();
                        changeData();
                    }, 300);
                }
            });

            onBeforeUnmount(() => {
                chart && chart.clear();
            });

            return {
                vData,
                chartDom,
                chartResize,
            };
        },
    };
</script>

<style lang="scss" scoped>
    .chart-wrap{
        position: relative;
        overflow:auto;
    }
    .chart-empty{
        position: absolute;
        top:0;
        left:0;
        width:100%;
        height:100%;
        background:#fff;
        text-align: center;
        padding-top: 170px;
        z-index: 20;
    }
    .chart {
        width: 300px;
        height: 400px;
        margin:0 auto;
    }
</style>
