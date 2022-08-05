<template>
    <el-card
        v-loading="loading"
        class="page"
        shadow="never"
    >
        <el-form inline>
            <el-form-item label="数据源Id:">
                <el-input
                    v-model="search.service_id"
                    clearable
                />
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

        <div class="radio_filter mb10">
            <el-radio-group
                v-model="currentRadio"
                size="mini"
                @change="statisticsRadioChange"
            >
                <el-radio-button label="PSI" />
                <el-radio-button label="Count" />
                <el-radio-button label="Percent" />
            </el-radio-group>
        </div>

        <!-- 展开方式 -->
        <!-- <el-table
            v-loading="loading"
            :data="list"
            stripe
            border
        >
            <el-table-column type="expand">
                <template #default="props">
                    <el-table
                        :data="list[props.$index].slist"
                        stripe
                        border
                    >
                        <el-table-column
                            label="count"
                            prop="count"
                        />
                        <el-table-column
                            label="percent"
                            prop="percent"
                        />
                        <el-table-column
                            label="psi"
                            prop="psi"
                        />
                    </el-table>
                </template>
            </el-table-column>
            <el-table-column
                label="日期"
                min-width="120px"
                prop="date"
            />
        </el-table> -->

        <div class="table_box">
            <c-grid
                v-if="table_data.rows.length"
                :theme="gridTheme"
                :data="table_data.rows"
                :frozen-col-count="1"
                font="12px sans-serif"
                :style="{height:`${gridHeight}px`}"
            >
                <c-grid-column
                    v-for="(item, index) in table_data.header"
                    :key="index"
                    :field="item"
                    min-width="100"
                    :width="item === table_data.header[0] ? 120 : 'auto'"
                    :column-style="{textOverflow: 'ellipsis'}"
                >
                    {{ item.replace(/'/g, '').replace(/\[|]/g, '') }}
                </c-grid-column>
            </c-grid>
            <el-table
                v-else
                stripe
                border
            >
                <div slot="empty">
                    <TableEmptyData />
                </div>
            </el-table>
        </div>
    

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
                time:       '',
                table_data: {
                    header: [],
                    rows:   [],
                },
                gridTheme: {
                    color:       '#6C757D',
                    borderColor: '#EBEEF5',
                },
                gridHeight:   0,
                currentRadio: 'PSI',
            };
        },
        created() {
            // this.search.service_id = this.$$route.query.service_id || '';
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
                this.table_data.header = [];
                this.table_data.rows = [];
                // this.currentRadio = 'PSI';
                this.loading = true;
                const { code, data } = await this.$http.post({
                    url:  '/model/psi',
                    data: this.search,
                });

                if (code === 0 && data) {
                    const { data_grid } = data;

                    this.data_grid = data_grid;
                    this.dataRender();
                }
                this.loading = false;
            },
            statisticsRadioChange() {
                this.table_data.rows = [];
                this.dataRender();
            },
            dataRender() {
                let count_header = [], unEmptyCount = 0, new_rows = null;
                const count_rows = [], obj = {};

                this.data_grid.forEach(item => {
                    if (Object.values(item)[0].length) {
                        count_header = [];
                        count_rows[0] = { ['日期']: Object.keys(item)[0] };
                        count_header[0] = '日期';
                        let count_count = 0;

                        for (const key in item) {
                            item[key].forEach(sitem => {
                                count_header.push(`['${sitem[0]}']`);
                                let val = this.currentRadio === 'PSI' ? sitem[3] : this.currentRadio === 'Count' ? sitem[1] : sitem[2];
                                const label = `['${[sitem[0]]}']`;

                                val = Number(val.toFixed(5));
                                obj[label] = val;
                                count_rows.push(obj);
                                new_rows = Object.assign(
                                    { ...obj }, 
                                    count_rows[0],
                                );
                                count_count +=val;
                            });
                        }
                        
                        count_header[Object.values(item)[0].length] = 'Sum';
                        new_rows.Sum = this.currentRadio === 'PSI' ? count_count.toFixed(5) : count_count;
                        this.table_data.rows.push(new_rows);
                        unEmptyCount++;
                    }
                });

                this.table_data.header = count_header;

                // the height of grid.
                if(unEmptyCount >= 15) unEmptyCount = 15;
                this.resize(unEmptyCount);
            },
            resize(length) {
                this.gridHeight = 41 * (length + 1) + 1;
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

<style lang="scss" scoped>
.radio_filter {
    text-align: right;
}
</style>
<style lang="scss">
    .structure-table{
        .ant-table-title{
            font-weight: bold;
            text-align: center;
            padding: 10px;
            font-size:16px;
        }
    }
    .c-grid {
        border: 1px solid #EBEEF5;
        position: relative;
        z-index: 1;
    }
</style>
