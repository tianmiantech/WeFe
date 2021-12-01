<template>
    <div
        v-loading="vData.loading"
        class="result"
    >
        <template v-if="vData.commonResultData.task">
            <el-collapse v-model="activeName">
                <el-collapse-item
                    title="基础信息"
                    name="1"
                >
                    <CommonResult
                        :result="vData.commonResultData"
                        :currentObj="currentObj"
                        :jobDetail="jobDetail"
                    />
                </el-collapse-item>
                <el-collapse-item
                    v-if="vData.results.length"
                    title="成员信息"
                    name="2"
                >
                    <template
                        v-for="(result, index) in vData.results"
                        :key="index"
                    >
                        <strong class="mb10">{{ result.title }} :</strong>
                        <el-tabs v-model="result.tabName">
                            <el-tab-pane
                                v-for="(row, index) in result.list"
                                :key="`${row.member_id}-${index}`"
                                :name="`${row.member_id}-${index}`"
                                :label="`${row.member_name} (${row.member_role === 'provider' ? '协作方' : '发起方'})`"
                            >
                                <el-table
                                    :data="row.dataList"
                                    class="mt10"
                                    stripe
                                    border
                                >
                                    <el-table-column
                                        label="序号"
                                        type="index"
                                    />
                                    <el-table-column
                                        label="列名"
                                        prop="column"
                                    />
                                    <el-table-column
                                        label="分箱数量"
                                        prop="binNums"
                                    />
                                    <el-table-column
                                        label="分箱方式"
                                        prop="paramsMethod"
                                    />
                                    <el-table-column
                                        label="iv"
                                        prop="iv"
                                    />
                                    <el-table-column
                                        label="woe"
                                        prop="woeArray"
                                    />
                                    <el-table-column
                                        label="count"
                                        prop="countArray"
                                    />
                                    <el-table-column
                                        label="count_rate"
                                        prop="countRateArray"
                                    />
                                    <el-table-column
                                        label="bad_rate"
                                        prop="nonEventRateArray"
                                    />
                                    <el-table-column
                                        label="event_count"
                                        prop="eventCountArray"
                                    />
                                    <el-table-column
                                        label="event_rate"
                                        prop="eventRateArray"
                                    />
                                    <el-table-column
                                        label="non_event_count"
                                        prop="nonEventCountArray"
                                    />
                                    <!-- <el-table-column
                                        label="non_event_rate"
                                        prop="nonEventRateArray"
                                    /> -->
                                </el-table>
                            </el-tab-pane>
                        </el-tabs>
                        <el-divider v-if="index === 0"></el-divider>
                    </template>
                </el-collapse-item>
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
        name:       'MixBinning',
        components: {
            CommonResult,
        },
        props: {
            ...mixin.props,
        },
        setup(props, context) {
            const activeName = ref('1');

            let vData = reactive({
                results:     [],
                resultTypes: ['model_binning_model'],
            });

            let methods = {
                showResult(list) {
                    vData.results = list.map(data => {
                        const result = {
                            list:    [],
                            title:   data.members.map(m => `${m.member_name} (${m.member_role})`).join(' & '),
                            tabName: '',
                        };

                        if(data.result) {
                            const arr = [];

                            data.result.result.forEach(member => {
                                const { binningResult } = member;
                                const dataList = [];

                                for(const column in binningResult) {
                                    const val = binningResult[column];

                                    dataList.push({
                                        column,
                                        ...val,
                                        eventCountArray:    val.eventCountArray.length ? val.eventCountArray.join(','): '',
                                        woeArray:           val.woeArray.length ? val.woeArray.join(','): '',
                                        eventRateArray:     val.eventRateArray.length ? val.eventRateArray.join(','): '',
                                        nonEventCountArray: val.nonEventCountArray.length ? val.nonEventCountArray.join(','): '',
                                        nonEventRateArray:  val.nonEventRateArray.length ? val.nonEventRateArray.join(','): '',
                                    });
                                }

                                arr.push({
                                    member_id:   member.member_id,
                                    member_name: member.member_name,
                                    member_role: member.member_role,
                                    dataList,
                                });
                            });

                            result.list = arr;
                            result.tabName = `${arr[0].member_id}-0`;
                        }

                        return result;
                    });
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
                methods,
            };
        },
    };
</script>
