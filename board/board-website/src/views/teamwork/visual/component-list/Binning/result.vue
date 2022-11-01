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
                    />
                </el-collapse-item>

                    <el-tabs
                        v-if="vData.list.length"
                        v-model="vData.tabName"
                    >
                        <el-tab-pane
                            v-for="(row, index) in vData.list"
                            :key="`${row.member_id}-${index}`"
                            :name="`${row.member_id}-${index}`"
                            :label="`${row.member_name} (${row.member_role === 'provider' ? '协作方' : '发起方'})`"
                        >
                        <el-table
                                :data="row.dataList"
                                :stripe="true"
                                :border="false"
                                style="width :100%;"
                                row-style="background: whitesmoke; "
                                class="fold-table"
                                :row-class-name="methods.tableRowClassName"
                                row-key="Index"
                                :expand-row-keys="vData.expandRowKeys"
                                @expand-change="methods.expandChange"
                                :fit="true"
                                :cell-style="{borderColor: 'white'}"

                            >
                                <el-table-column type="expand" >

                                    <template #default="props">

                                        <el-table :data="row.dataList[props.$index].inline_table" :span-method="methods.arraySpanMethod" style="width: 100%; border: 1px solid lightgray; border-bottom: none;" :cell-style="{borderColor: 'white'}"  :header-cell-style="{borderColor: 'white'}">

                                            <el-table-column v-if="row.dataList[props.$index].woeArray.length" label="WOE变化图" prop="weight" min-width="380%" align="center">
                                                <template v-slot="scope">
                                                    <LineChart ref="LineChart" :config="scope.row.woeLineConfig" />
                                                </template>
                                            </el-table-column>

                                            <el-table-column v-if="row.member_role === 'promoter'" label="分布" min-width="550%" align="center" fixed="right">
                                                <template v-slot="scope">
                                                    <BarChartNew ref="BarChart" :config="scope.row.mapdata" />
                                                </template>
                                            </el-table-column>
                                        </el-table>

                                        <div>

                                                    <el-table :data="row.dataList[props.$index].inline_table" style="width: 100%; border: 1px solid lightgray; border-top: none; border-bottom: 1px solid lightgray;" :cell-style="{borderColor: 'white'}" :header-cell-style="{borderColor: 'white'}">
                                                        <el-table-column label="箱号" width="55" type="index" align="center" />
                                                        <el-table-column label="划分区间" prop="binning" align="center" width="110" />
                                                        <el-table-column label="正样本数" prop="eventCountArray" align="center" />
                                                        <el-table-column label="负样本数" prop="nonEventCountArray" align="center" />
                                                        <el-table-column label="总样本数" prop="countArray" align="center" />
                                                        <el-table-column label="正样本占总样本比例" prop="eventRateArray" align="center" />
                                                        <el-table-column label="负样本占总样本比例" prop="nonEventRateArray" align="center" />
                                                        <el-table-column label="总占比" prop="countRateArray" align="center" />
                                                        <el-table-column label="WOE" prop="woeArray" align="center" />
                                                        <el-table-column label="IV" prop="ivArray" align="center" />

                                                    </el-table>

                                        </div><br>

                                    </template>

                                </el-table-column>
                                <el-table-column label="特征名称" prop="column"></el-table-column>
                                <el-table-column label="分箱方法" prop="paramsMethod"></el-table-column>
                                <el-table-column label="分箱数量" prop="binNums"></el-table-column>
                                <el-table-column label="总IV" prop="iv"></el-table-column>
                            </el-table>
                        </el-tab-pane>
                    </el-tabs>

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
    import {
        ref, reactive,
    } from 'vue';
    import CommonResult from '../common/CommonResult';
    import resultMixin from '../result-mixin';

    const mixin = resultMixin();

    export default {
        name:       'Binning',
        components: {
            CommonResult,
        },
        props: {
            ...mixin.props,
        },
        setup(props, context) {
            const activeName = ref('1');
            const activeDetails = ref('[]');
            const LineChart = ref();

            let vData = reactive({
                tabName:     '',
                list:        [],
                resultTypes: ['model_binning_model'],
                expandRowKeys:[0],
            });

            let methods = {
                expandChange(row) {
                    const tabIdx = vData.tabName.split('-')[1];

                    vData.list[tabIdx].dataList[row.Index].isShowWOE = true;
                },
                tableRowClassName({ row, rowIndex }) {
                    row.Index = rowIndex;
                    methods.expandChange(row);
                },
                arraySpanMethod({ row, column, rowIndex, columnIndex }) {
                    const tabIdx = vData.tabName.split('-')[1];

                    if (columnIndex === 0 || columnIndex === 1) {
                        return {
                            rowspan: vData.list[tabIdx].dataList.length || 0,
                            colspan: 1,
                        };
                    }
                },
                showResult(data) {
                    if(data[0].result) {
                        const list = [];

                        data[0].result.result.forEach(member => {
                            const { binningResult, member_role } = member;
                            const dataList = [];

                            for(const column in binningResult) {
                                const val = binningResult[column];
                                const series = [], xAxis = [], goodData = [], badData = [], badLineData = [];

                                // 概率
                                // for (let i=0; i<val.countArray.length; i++) {
                                //     goodData.push((val.eventCountArray[i] / val.countArray[i])*val.countRateArray[i]);
                                //     badData.push((val.nonEventCountArray[i] / val.countArray[i])*val.countRateArray[i]);
                                //     badLineData.push(val.nonEventCountArray[i] / val.countArray[i]);
                                // }

                                // 样本量
                                for (let i=0; i<val.countArray.length; i++) {
                                    goodData.push(val.eventCountArray[i]);
                                    badData.push(val.nonEventCountArray[i]);
                                    badLineData.push(val.nonEventCountArray[i] / val.countArray[i]);
                                }

                                series.push(
                                    {
                                        name:      'good',
                                        stack:     'Ad',
                                        type:      'bar',
                                        data:      goodData,
                                        itemStyle: {
                                            color: 'rgba(5, 115, 107, .7)',
                                        },
                                        // tooltip: {
                                        //     valueFormatter: (value) => value.toFixed(3),
                                        // },
                                    },
                                    {
                                        name:      'bad',
                                        stack:     'Ad',
                                        type:      'bar',
                                        data:      badData,
                                        itemStyle: {
                                            color: 'rgba(174, 6, 23, .7)',
                                        },
                                        // tooltip: {
                                        //     valueFormatter: (value) => value.toFixed(3),
                                        // },
                                    },
                                    {
                                        type:       'line',
                                        yAxisIndex: 1,
                                        itemStyle:  {
                                            color: 'rgba(11, 89, 153, 1)',
                                        },
                                        data:  badLineData,
                                        label: {
                                            show:     true,
                                            position: 'top',
                                            formatter (value) {
                                                return Number(value.data).toFixed(2);
                                            },
                                        },
                                        tooltip: {
                                            valueFormatter: (value) => value.toFixed(3),
                                        },
                                    },
                                );
                                if (val.splitPoints.length) {
                                    for(let i=0; i<val.splitPoints.length; i++) {
                                        xAxis.push(val.splitPoints[i].toFixed(3));
                                    }
                                } else {
                                    for (let j=0; j<Number(val.binNums); j++) {
                                        xAxis.push(j+1);
                                    }
                                }
                                const mapdata = {
                                    xAxis: {
                                        type: 'category',
                                        data: xAxis || [],
                                    },
                                    yAxis: [
                                        {
                                            type:         'value',
                                            name:         'Bin count distribution',
                                            nameLocation: 'middle',
                                            nameGap:      30,
                                            nameRotate:   90,
                                        },
                                        {
                                            type:         'value',
                                            name:         'Bad probability',
                                            nameLocation: 'middle',
                                            nameGap:      30,
                                            nameRotate:   90,
                                        },
                                    ],
                                    series,
                                    legend: {},
                                };

                                const inline_table = [], woeData = { xAxis: [], series: [[]] };

                                for (let j=0; j<Number(val.binNums); j++) {
                                    woeData.xAxis.push(j+1);
                                    woeData.series[0].push(val.woeArray[j]);
                                    let binningData = null;

                                    if (val.splitPoints.length) {
                                        binningData = j === 0 ? `(${Number.NEGATIVE_INFINITY}, ${Number(val.splitPoints[j]).toFixed(2)}]` : j === Number(val.binNums)-1 ? `(${Number(val.splitPoints[j]).toFixed(2)}, ${Number.POSITIVE_INFINITY})` : `(${Number(val.splitPoints[j-1]).toFixed(2)}, ${Number(val.splitPoints[j]).toFixed(2)}]`;
                                    } else {
                                        binningData = '-';
                                    }
                                    inline_table.push({
                                        column,
                                        countArray:         val.countArray[j],
                                        countRateArray:     Number(val.countRateArray[j]).toFixed(2),
                                        eventCountArray:    member_role === 'promoter' && props.myRole === 'promoter' ? Number(val.eventCountArray[j]).toFixed(2) : '-',
                                        eventRateArray:     member_role === 'promoter' && props.myRole === 'promoter' ? Number(val.eventRateArray[j]).toFixed(2) : '-',
                                        nonEventCountArray: member_role === 'promoter' && props.myRole === 'promoter' ? val.nonEventCountArray[j] : '-',
                                        nonEventRateArray:  member_role === 'promoter' && props.myRole === 'promoter' ? Number(val.nonEventRateArray[j]).toFixed(2): '-',
                                        // eventCountArray:    Number(val.eventCountArray[j]).toFixed(2),
                                        // eventRateArray:     Number(val.eventRateArray[j]).toFixed(2),
                                        // nonEventCountArray: val.nonEventCountArray[j],
                                        // nonEventRateArray:  Number(val.nonEventRateArray[j]).toFixed(2),
                                        ivArray:            Number(val.ivArray[j]).toFixed(2),
                                        splitPoints:        val.splitPoints.length ? Number(val.splitPoints[j]).toFixed(2) : [],
                                        woeArray:           Number(val.woeArray[j]).toFixed(2),
                                        binning:            binningData,
                                        woeLineConfig:      woeData,
                                        mapdata,
                                    });
                                }

                                dataList.push({
                                    column,
                                    ...val,
                                    eventCountArray:    val.eventCountArray.length ? val.eventCountArray.join(','): '',
                                    woeArray:           val.woeArray.length ? val.woeArray.join(','): '',
                                    eventRateArray:     val.eventRateArray.length ? val.eventRateArray.join(','): '',
                                    nonEventCountArray: val.nonEventCountArray.length ? val.nonEventCountArray.join(','): '',
                                    nonEventRateArray:  val.nonEventRateArray.length ? val.nonEventRateArray.join(','): '',
                                    isCheckBarChart:    false,
                                    inline_table,
                                    isShowWOE:          false,
                                });
                            }

                            list.push({
                                member_id:   member.member_id,
                                member_name: member.member_name,
                                member_role: member.member_role,
                                dataList,
                            });
                        });

                        vData.list = list;
                        vData.tabName = `${list[0].member_id}-0`;
                        
                        if (list[0].member_id === list[1].member_id){
                            vData.expandRowKeys = [];
                        }
                        
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
                activeDetails,
                methods,
                LineChart,
            };
        },
    };
</script>

<style lang="scss">
    .fold-table tbody tr:hover>td{
            background: white !important
        }
</style>
