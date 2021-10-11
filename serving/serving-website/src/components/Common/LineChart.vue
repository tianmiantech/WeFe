<template>
    <div class="chart-wrap">
        <div
            id="myChart"
            :style="{width: '100%', height: '710px'}"
        />
    </div>
</template>

<script>
import echarts from 'echarts';

export default {
    name:  'LineChart',
    props: {
        chartData: Object,
    },
    data() {
        return {
            yAxisListLength: 0,
            loading:         false,
            show:            true,
        };
    },
    methods: {
        initChart() {
            const getchart = echarts.init(document.getElementById('myChart'));
            const  option = {
                title: {
                    text: this.chartData.title,
                },
                color:   ['#c23531', '#61a0a8'],
                tooltip: {
                    trigger: 'axis',
                },
                legend: {
                    data: this.chartData.legend,
                },
                grid: {
                    left:         '3%',
                    right:        '4%',
                    bottom:       '3%',
                    containLabel: true,
                },
                toolbox: {
                    feature: {
                        saveAsImage: {},
                    },
                },
                xAxis: {
                    type:        'category',
                    boundaryGap: false,
                    data:        this.chartData.xAxis,
                },
                yAxis: {
                    type: 'value',
                },
                series: this.chartData.series,
            };

            getchart.setOption(option);
            //随着屏幕大小调节图表
            window.addEventListener('resize', () => {
                getchart.resize();
            });
        },

    },
};
</script>

<style>

</style>
