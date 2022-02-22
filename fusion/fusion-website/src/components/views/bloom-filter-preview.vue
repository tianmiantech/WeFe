<template>
    <div
        v-loading="loading"
        style="min-height: 200px;"
    >
        <el-table
            v-loading="tableLoading"
            max-height="500"
            :data="table_data.rows"
            stripe
            border
        >
            <div slot="empty">
                <TableEmptyData />
            </div>
            <el-table-column
                v-for="index in table_data.header"
                :key="index"
                :label="index"
                :prop="index"
                min-width="100"
            />
        </el-table>
    </div>
</template>

<script>
export default {
    data() {
        return {
            loading:    false,
            table_data: {
                header: [],
                rows:   [],
            },
            gridTheme: {
                color:       '#6C757D',
                borderColor: '#EBEEF5',
            },
        };
    },
    methods: {
        async loadData(id) {
            this.loading = true;

            const { code, data } = await this.$http.get({
                url: '/data_set/detail_and_preview?id=' + id,
            });

            if (code === 0) {
                if(data.list){
                    const rows = data.list;

                    this.table_data.rows = rows;

                    this.table_data.header = data.header;

                }
            }

            this.loading = false;
        },
    },
};
</script>

<style lang="scss" scoped>
    .c-grid{
        border: 1px solid #EBEEF5;
        position: relative;
        z-index: 1;
    }
</style>
