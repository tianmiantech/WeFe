<template>
    <div class="chart-wrap">
        <div
            v-if="!vData.loading && vData.yAxisListLength === 0"
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
    import { HeatmapChart } from 'echarts/charts';
    import { CanvasRenderer } from 'echarts/renderers';
    import {
        TitleComponent,
        TooltipComponent,
        LegendComponent,
        VisualMapComponent,
    } from 'echarts/components';
    import {
        ref,
        reactive,
        onMounted,
        onBeforeUnmount,
    } from 'vue';

    use([
        CanvasRenderer,
        HeatmapChart,
        TitleComponent,
        TooltipComponent,
        LegendComponent,
        VisualMapComponent,
    ]);

    export default {
        name:  'HeatmapChart',
        props: {
            config: Object,
        },
        setup(props) {
            let chart;
            const chartDom = ref();
            const vData = reactive({
                yAxisListLength: props.config.yAxis.length || 0,
            });
            const options = {
                tooltip: {
                    position: 'top',
                },
                grid: {
                    height: '80%',
                    top:    15,
                    left:   80,
                },
                xAxis: {
                    data:      props.config.xAxis || [],
                    splitArea: {
                        show: true,
                    },
                    axisLabel: { interval: 0, rotate: 30 },
                },
                yAxis: {
                    data:      props.config.yAxis || [],
                    splitArea: {
                        show: true,
                    },
                    axisLabel: { interval: 0, rotate: 30 },
                },
                visualMap: {
                    min:        -1,
                    max:        1,
                    calculable: true,
                    orient:     'horizontal',
                    left:       'center',
                    bottom:     15,
                    color:      ['#2c6838', '#49a155', '#86cb67', '#cdea83', '#f8d380', '#ef8d52', '#ed402f', '#ca2328'],
                },
                series: {
                    // name:  '',
                    type:  'heatmap',
                    data:  props.config.series || [],
                    label: {
                        show: true,
                    },
                    emphasis: {
                        itemStyle: {
                            shadowBlur:  10,
                            shadowColor: 'rgba(0, 0, 0, 0.5)',
                        },
                    },
                },
            };

            // init chart
            const initChart = () => {
                if(chartDom.value.offsetWidth) {
                    chart = init(chartDom.value, {
                        width:  props.config.width,
                        height: props.config.height,
                    });
                }
            };

            // update chart data
            const changeData = () => {
                options.xAxis.data = props.config.xAxis || [];
                options.yAxis.data = props.config.yAxis || [];
                options.grid.width = props.config.width - 100;
                options.grid.height = props.config.height - 140;

                if (props.config.series) {
                    vData.yAxisListLength = props.config.yAxis.length;
                    options.series.data = props.config.series;
                }

                if(chart) {
                    chart.clear();
                } else {
                    initChart();
                }

                if(chart) {
                    chart.setOption(options);

                    setTimeout(() => {
                        chart.resize({
                            width:     props.config.width,
                            height:    props.config.height,
                            animation: {
                                duration: 300,
                                easing:   'linear',
                            },
                        });
                    });
                }
            };

            onMounted(() => {
                if(props.config) {
                    setTimeout(() => {
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
                initChart,
                changeData,
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
    .chart {min-height: 400px;}
</style>
