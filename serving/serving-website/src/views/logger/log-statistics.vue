<template>
    <el-card
        class="page"
        shadow="never"
    >
        <el-form
            class="mb20"
            inline
        >
            <el-form-item
                label="模型ID："
                label-width="100px"
            >
                <el-input
                    v-model="search.model_id"
                    clearable
                />
            </el-form-item>
            <el-form-item
                label="成员ID："
                label-width="100px"
            >
                <el-input
                    v-model="search.member_id"
                    clearable
                />
            </el-form-item>
            <el-form-item
                label="区间："
                label-width="100px"
            >
                <el-input
                    v-model="search.interval"
                    style="width:70px"
                    clearable
                />
                <el-select
                    v-model="search.date_type"
                    style="width:100px"
                    clearable
                >
                    <el-option
                        v-for="item in dataTypeList"
                        :key="item.value"
                        :value="item.value"
                        :label="item.label"
                    />
                </el-select>
            </el-form-item>

            <el-button
                type="primary"
                @click="getChartList()"
            >
                查询
            </el-button>
        </el-form>

        <LineChart
            ref="lineChart"
            v-loading="loading"
            :chart-data="chartData"
        />
    </el-card>
</template>

<script>
    import LineChart from '../../components/Common/LineChart.vue';

    export default {
        components: {
            LineChart,
        },
        data() {
            return {
                loading: false,
                search:  {
                    member_id: '',
                    model_id:  '',
                    date_type: 'month',
                    interval:  3,
                },
                getListApi:   '/log/statistics',
                dataTypeList: [
                    {
                        label: '个月',
                        value: 'month',
                    },
                    {
                        label: '天',
                        value: 'day',
                    },
                    {
                        label: '分钟',
                        value: 'minute',
                    },
                ],
                chartData: {
                    title:  '调用统计',
                    xAxis:  [],
                    series: [],
                },
            };
        },
        async created() {
            await this.getDataTypeList();
            this.getChartList();
        },
        methods: {
            // 获取日期类型
            async getDataTypeList() {
                const { code, data } = await this.$http.get({
                    url: '/log/date_type',
                });

                if (code === 0) {
                    console.log(data);
                }
            },

            // 获取图表数据
            async getChartList() {
                this.loading = true;
                const { code, data } = await this.$http.get({
                    url:    this.getListApi,
                    params: this.search,
                });


                this.chartData.legend = ['成功数', '失败数'];
                const xAxis = [], series = [
                    {
                        name: '成功数',
                        type: 'line',
                        data: [],
                    },
                    {
                        name: '失败数',
                        type: 'line',
                        data: [],
                    },
                ];

                this.loading = false;
                if (code === 0) {
                    if (data && data.length) {
                        data.forEach(item => {
                            switch(this.search.date_type) {
                                case 'month':
                                    xAxis.push(item.month);
                                    break;
                                case 'minute':
                                    xAxis.push(item.minute);
                                    break;
                                case 'day':
                                    xAxis.push(item.day);
                                    break;
                            }
                            series[0].data.push(item.success);
                            series[1].data.push(item.fail);
                        });
                        this.chartData.xAxis = xAxis;
                        this.chartData.series = series;
                        this.$refs.lineChart.initChart();
                    }
                }
            },
        },
    };
</script>

<style lang="scss">
    .structure-table{
        .ant-table-title{
            font-weight: bold;
            text-align: center;
            padding: 10px;
            font-size:16px;
        }
    }
    .radio-group{
        margin-top: 10px;
        .el-radio{
            width: 90px;
            margin-bottom: 10px;
        }
        .el-radio__label{padding-left: 10px;}
    }
</style>
