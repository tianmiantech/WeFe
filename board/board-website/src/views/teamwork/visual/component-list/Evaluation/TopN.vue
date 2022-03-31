<template>
    <div class="topn mb20">
        <el-table
            :data="vData.tableData"
            max-height="416"
        >
            <el-table-column
                prop="name"
                label="TopN"
                width="150">
            </el-table-column>
            <el-table-column v-if="vData.train_topn.length" label="训练集" align="center">
                <el-table-column
                    label="cutoff 区间"
                    width="120">
                    <template v-slot="scope">
                        [{{scope.row.cut_off}}, 1]
                    </template>
                </el-table-column>
                <el-table-column
                    prop="total"
                    label="样本数"
                    width="120">
                </el-table-column>
                <el-table-column
                    prop="TP"
                    label="正例数">
                </el-table-column>
                <el-table-column
                    label="正例比例"
                    width="120">
                    <template v-slot="scope">
                        {{scope.row.TP / scope.row.total}}
                    </template>
                </el-table-column>
            </el-table-column>
            <el-table-column label="测试集" align="center">
                <el-table-column
                    label="cutoff 区间"
                    width="120">
                    <template v-slot="scope">
                        [{{scope.row.v_cut_off}}, 1]
                    </template>
                </el-table-column>
                <el-table-column
                    prop="v_total"
                    label="样本数"
                    width="120">
                </el-table-column>
                <el-table-column
                    prop="v_TP"
                    label="正例数">
                </el-table-column>
                <el-table-column
                    label="正例比例"
                    width="120">
                    <template v-slot="scope">
                        {{scope.row.v_TP / scope.row.v_total}}
                    </template>
                </el-table-column>
            </el-table-column>
        </el-table>
    </div>
</template>

<script>
    import { reactive } from 'vue';

    export default {
        name: 'TopN',
        setup(props) {
            const vData = reactive({
                loading:       false,
                tableData:     [],
                tableDataList: [],
                train_topn:    [],
                validate_topn: [],
            });

            const methods = {};

            const renderTopnTable = (result) => {
                const topnList = result.train_topn || result.validate_topn;

                vData.validate_topn = result.validate_topn;
                if(topnList && topnList.length) {
                    vData.tableData = topnList.map((item, i) => {
                        return {
                            ...item,
                            v_TP:      vData.validate_topn[i].TP,
                            v_cut_off: vData.validate_topn[i].cut_off,
                            v_name:    vData.validate_topn[i].name,
                            v_recall:  vData.validate_topn[i].recall,
                            v_total:   vData.validate_topn[i].total,
                        };
                    });
                }
            };

            return {
                vData,
                methods,
                renderTopnTable,
            };
        },
    };
</script>

<style lang="scss" scoped>
</style>
