<template>
    <div class="chart-wrap">
        <div
            v-if="!vData.loading && vData.yAxisListLength === 0"
            class="chart-empty"
        >暂无数据</div>
        <slot :config="config"></slot>
        <div
            v-loading="vData.loading"
            ref="chartDom"
            class="chart"
        />
    </div>
</template>

<script>
    import { init, use } from 'echarts/core';
    import { LineChart } from 'echarts/charts';
    import { CanvasRenderer } from 'echarts/renderers';
    import {
        GridComponent,
        MarkLineComponent,
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
        LineChart,
        CanvasRenderer,
        MarkLineComponent,
        TooltipComponent,
        LegendComponent,
        GridComponent,
    ]);

    export default {
        name:  'LineChart',
        props: {
            config: {
                type:    Object,
                default: () => {},
            },
        },
        setup (props) {
            let chart;
            const chartDom = ref();
            const vData = reactive({
                yAxisListLength: 0,
                loading:         false,
                show:            true,
            });
            const options = {
                color: [
                    '#19d4ae',
                    '#5ab1ef',
                    '#fa6e86',
                    '#ffb980',
                    '#0067a6',
                    '#c4b4e4',
                    '#d87a80',
                    '#9cbbff',
                    '#d9d0c7',
                    '#87a997',
                    '#d49ea2',
                    '#5b4947',
                    '#7ba3a8',
                ],
                /* title: {
                    text: '堆叠区域图',
                }, */
                tooltip: {
                    trigger:     'axis',
                    confine:     true,
                    axisPointer: {
                        type:  'cross',
                        label: {
                            backgroundColor: '#6a7985',
                        },
                    },
                },
                legend: {
                    data: props.config.legend || [],
                },
                grid: {
                    left:         '3%',
                    right:        '4%',
                    bottom:       '3%',
                    containLabel: true,
                },
                xAxis: {
                    boundaryGap: false,
                    type:        props.config.xAxisType || 'category',
                    data:        props.config.xAxis || [],
                    splitLine:   {
                        show:      true,
                        lineStyle: {
                            color: ['#ddd'],
                            type:  'solid',
                            width: 1,
                        },
                    },
                    axisLine: {
                        lineStyle: {
                            color: '#666',
                        },
                    },
                },
                yAxis: {
                    type:      'value',
                    splitLine: {
                        show:      true,
                        lineStyle: {
                            color: ['#ddd'],
                            type:  'solid',
                            width: 1,
                        },
                    },
                    axisLine: {
                        lineStyle: {
                            color: '#666',
                        },
                    },
                },
                series: [],
            };

            // init chart
            const initChart = () => {
                if(chartDom.value.offsetWidth) {
                    chart = init(chartDom.value);
                }
            };

            // update chart data
            const changeData = () => {
                options.legend.data = props.config.legend;
                options.xAxis.type = props.config.xAxisType;
                options.xAxis.data = props.config.xAxis || [];

                if (props.config.series) {
                    options.series = [];
                    vData.yAxisListLength = 0;

                    props.config.series.forEach((series, index) => {
                        vData.yAxisListLength += series.length;

                        if(series.length) {
                            const config = {
                                name:     props.config.legend ? props.config.legend[index] : '',
                                type:     'line',
                                emphasis: {
                                    focus: 'series',
                                },
                                showSymbol: false,
                                lineStyle:  {
                                    width: 1,
                                },
                                data:   series,
                                smooth: true,
                            };

                            if (props.config.settings && props.config.settings.area) {
                                config.areaStyle = {};
                            }

                            // markLine
                            if(index === 0 && props.config.markLine) {
                                config.markLine = props.config.markLine;
                            }

                            options.series.push(config);
                        }
                    });
                }

                if(chart) {
                    chart.clear();
                } else {
                    initChart();
                }
                chart && chart.setOption(options);
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
                vData.loading = props.config.loading || false;

                setTimeout(() => {
                    initChart();
                    changeData();
                }, 100);
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
    .chart {min-height: 400px;}
</style>
