<template>
    <el-card
        class="page"
        shadow="never"
    >
        <el-form
            class="mb20"
            inline
        >
            <el-form-item label="数据源Id:">
                <el-input v-model="search.service_id" />
            </el-form-item>
            <el-form-item>
                <el-date-picker
                    v-model="time"
                    type="daterange"
                    range-separator="至"
                    start-placeholder="开始日期"
                    end-placeholder="结束日期"
                    value-format="yyyy-MM-dd"
                    @change="dataChange"
                />
            </el-form-item>

            <el-button
                type="primary"
                @click="getPsiList()"
            >
                查询
            </el-button>
        </el-form>

        <el-table
            v-loading="loading"
            :data="list"
            stripe
            border
        >
            <el-table-column
                label="日期"
                min-width="120px"
            >
                <template slot-scope="scope">
                    {{ scope.row.name }}
                    <p class="id">{{ scope.row.id }}</p>
                </template>
            </el-table-column>
        </el-table>

        <div
            v-if="pagination.total"
            class="mt20 text-r"
        >
            <el-pagination
                :total="pagination.total"
                :page-sizes="[10, 20, 30, 40, 50]"
                :page-size="pagination.page_size"
                :current-page="pagination.page_index"
                layout="total, sizes, prev, pager, next, jumper"
                @current-change="currentPageChange"
                @size-change="pageSizeChange"
            />
        </div>
    </el-card>
</template>

<script>
    import table from '@src/mixins/table.js';

    export default {
        mixins: [table],
        inject: ['refresh'],
        data() {
            return {
                loading:      false,
                fillUrlQuery: false,
                search:       {
                    service_id: 'a336b0e826214ad786e5844553ffdadd_VertLR_16196034768611638',
                    start_time: '',
                    end_time:   '',
                    page_index: 0,
                    page_size:  20,
                },
                // getListApi: '/model/psi',
                time:      '',
                tableData: [],
            };
        },
        created() {
            this.syncUrlParams();
            this.getPsiList();
        },
        methods: {
            syncUrlParams() {
                this.search = {
                    service_id:             'a336b0e826214ad786e5844553ffdadd_VertLR_16196034768611638',
                    start_time:             '',
                    end_time:               '',
                    'request-from-refresh': false,
                    ...this.$route.query,
                };
                const date = new Date();
                const year = date.getFullYear();
                const mon = date.getMonth()+1;
                const day = date.getDate();
                const str = `${year}-${mon}-${day}`;

                this.search.start_time = str;
                this.search.end_time = str;
                if(this.search.start_time && this.search.end_time) {
                    this.time = [this.search.start_time, this.search.end_time];
                }
            },
            async getPsiList() {
                this.tableData = [];
                const { code, data } = await this.$http.post({
                    url:  '/model/psi',
                    data: this.search,
                });

                if (code === 0 && data) {
                    console.log(data);
                    const { data_grid } = data;
                    const list = [];

                    data_grid.forEach(item => {
                        console.log(Object.keys(item));
                    });

                    // list.push({
                    //     date: 
                    // });
                }
                await this.getList();
            },
            dataChange(val) {
                if (val) {
                    this.search.start_time = val[0];
                    this.search.end_time = val[1];
                } else {
                    this.search.start_time = '';
                    this.search.end_time = '';
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
</style>
