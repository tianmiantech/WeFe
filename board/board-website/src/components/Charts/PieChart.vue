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
        name:  'PieChart',
        props: {
            config: Object,
        },
        setup (props) {
            let chart;
            const chartDom = ref();
            const vData = reactive({
                seriesLength: 0,
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
                title: {
                    text: '饼图',
                    left: 'center',
                },
                tooltip: {
                    trigger:   'item',
                    formatter: '{a} <br/>{b} : {c} ({d}%)',
                },
                legend: {
                    data:   props.config.legend || [],
                    left:   props.config.legendLeft || 'center',
                    orient: props.config.legendOrient ||  'horizontal',
                },
                series: {
                    name:     '',
                    type:     'pie',
                    radius:   '70%',
                    data:     props.config.series || [],
                    width:    '50%',
                    left:     '25%',
                    emphasis: {
                        itemStyle: {
                            shadowBlur:    10,
                            shadowOffsetX: 0,
                            shadowColor:   'rgba(0, 0, 0, 0.5)',
                        },
                    },
                    label: {
                        show:      props.config.labelShow || false,
                        position:  'outside',
                        formatter: '{a}\r\n{b} : {c}({d}%)',
                        align:     'center',
                    },
                    labelLine: {
                        show:    true,
                        length:  3,
                        length2: 6,
                    },
                    labelLayout: {
                        width: 200,
                    },
                },
            };

            // init chart
            const initChart = () => {
                if(chartDom.value.offsetWidth) {
                    chart = init(chartDom.value);
                }
            };

            // update chart data
            const changeData = () => {
                options.title.text = props.config.titleText;
                options.series.name = props.config.name || options.title.text;
                options.legend.left = props.config.legendLeft || 'center';
                options.legend.orient = props.config.legendOrient || 'horizontal';
                options.series.label.show = props.config.labelShow || false;
                if(props.config.series) {
                    vData.seriesLength = props.config.series.length;
                    options.legend.data = props.config.legend || [];
                    options.series.data = props.config.series || [];

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
    .chart {height: 400px;}
</style>
