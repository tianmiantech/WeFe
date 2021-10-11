<template>
    <el-table
        v-loading="loading"
        :data="list"
        border
    >
        <el-table-column
            label="数据集"
            min-width="150px"
        >
            <template slot-scope="scope">
                <p class="id">{{ scope.row.id }}</p>
                {{ scope.row.name }}
            </template>
        </el-table-column>
        <el-table-column
            label="描述"
            prop="description"
            width="360px"
        />
        <el-table-column
            label="数据量"
            prop="row_count"
            width="100px"
        />
        <el-table-column
            label="字段信息"
            prop="rows"
        >
            <template slot-scope="scope">
                <el-tag
                    v-for="item in scope.row.rows.split(',')"
                    :key="item"
                    :type="item"
                    effect="plain"
                    style="margin-left : 5px"
                >
                    {{ item }}
                </el-tag>
            </template>
        </el-table-column>
        <el-table-column
            label="数据来源"
            prop="data_resource_source"
            width="100px"
        />
        <el-table-column
            label="使用次数"
            prop="used_count"
            width="80px"
        />
        <el-table-column
            label="创建时间"
            min-width="50px"
        >
            <template slot-scope="scope">
                {{ scope.row.created_time | dateFormat }}
            </template>
        </el-table-column>

        <el-table-column
            label="更新时间"
            min-width="50px"
        >
            <template slot-scope="scope">
                {{ scope.row.updated_time | dateFormat }}
            </template>
        </el-table-column>
    </el-table>
</template>

<script>
export default {
    data() {
        return {
            currentItem: {},
            loading:     false,
            list:        [],
        };
    },
    created() {
        this.currentItem.id = this.$route.query.id;
        this.currentItem.name = this.$route.query.name;
        this.getDataSetDetail();
    },
    methods: {

        async getDataSetDetail() {
            this.loading = true;
            const { code, data } = await this.$http.post({
                url:  '/data_set/query',
                data: {
                    id:   this.currentItem.id,
                    name: this.currentItem.name,
                },
            });

            if (code === 0) {
                if (data) {
                    this.list = data.list;
                }
            }
            this.loading = false;
        },
    },
};
</script>

<style>

</style>
