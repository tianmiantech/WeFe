<template>
    <div
        v-loading="vData.loading"
        class="result"
    >
        <template v-if="vData.commonResultData.task">
            <el-collapse v-model="activeName">
                <el-collapse-item title="基础信息" name="1">
                    <CommonResult :result="vData.commonResultData"  :currentObj="currentObj" :jobDetail="jobDetail"/>
                </el-collapse-item>
                <el-collapse-item title="特征评分表" name="2">
                    <el-table :data="vData.resultTableData" stripe :border="true" style="width :100%" class="fold-table">
                        <el-table-column type="expand">
                            <template #default="props">
                                <el-table :data="vData.resultTableData[props.$index].inlineTable" stripe border>
                                    <el-table-column label="分箱" prop="binning" />
                                    <el-table-column label="评分" prop="score">
                                        <template v-slot="scope">
                                            {{ dealNumPrecision(scope.row.score) }}
                                        </template>
                                    </el-table-column>
                                    <el-table-column label="woe" prop="woe">
                                        <template v-slot="scope">
                                            {{ dealNumPrecision(scope.row.woe) }}
                                        </template>
                                    </el-table-column>
                                    <el-table-column label="特征权重" prop="weight">
                                        <template v-slot="scope">
                                            {{ dealNumPrecision(scope.row.weight) }}
                                        </template>
                                    </el-table-column>
                                </el-table>
                            </template>
                        </el-table-column>
                        <el-table-column label="特征名称" prop="feature"></el-table-column>
                    </el-table>
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
    import { reactive, ref, onBeforeMount } from 'vue';
    import CommonResult from '../common/CommonResult';
    import resultMixin from '../result-mixin';
    import { dealNumPrecision } from '@src/utils/utils';

    const mixin = resultMixin();

    export default {
        name:       'ScoreCard',
        components: {
            CommonResult,
        },
        props: {
            ...mixin.props,
        },
        setup(props, context) {
            const activeName = ref('1');

            let vData = reactive({
                resultTypes:     ['metric'],
                resultConfigs:   [],
                resultTableData: [],
            });

            let methods = {
                showResult(data) {
                    if (data && data[0] && data[0].result) {
                        const { result } = data[0], outerTable = [];

                        for (const key in result) {
                            outerTable.push({
                                feature:     key,
                                inlineTable: [...result[key]],
                            });
                        }
                        vData.resultTableData = outerTable;
                    }
                },
            };

            onBeforeMount(() => {

            });

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
                methods,
                activeName,
                dealNumPrecision,
            };
        },
    };
</script>
