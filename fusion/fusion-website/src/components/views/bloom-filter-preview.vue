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
                url: '/filter/detail_and_preview?id=' + id,
            });

            if (code === 0) {
                if (data.preview_data.raw_data_list) {
                    const rows = data.preview_data.raw_data_list;

                    // 默认显示前15条记录
                    if (rows.length >= 15) {
                        this.table_data.rows = rows.slice(0, 15)
                    } else {
                        this.table_data.rows = rows;
                    }

                    this.table_data.header = data.preview_data.header;

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
