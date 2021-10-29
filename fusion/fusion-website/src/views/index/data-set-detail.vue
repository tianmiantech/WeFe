<template>
    <el-card>
        <div class="header">
            <h3>{{ dataInfo.name }}</h3>
            <p>{{ dataInfo.id }}</p>
        </div>
        <div class="top_side">
            <h4>数据集简介</h4>
            <p class="subtitle">上传于 <span>{{ dataInfo.created_time }}</span> ，参与了 <span>{{ dataInfo.used_count }}</span> 任务。</p>
            <el-row :gutter="20">
                <el-col :span="6">描述: {{ dataInfo.description }}</el-col>
                <el-col :span="6">数据量: {{ dataInfo.row_count }}</el-col>
                <el-col
                    v-if="dataInfo.rows"
                    :span="6"
                >
                    字段: <el-tag
                        v-for="item in dataInfo.rows.split(',')"
                        :key="item"
                        :type="item"
                        effect="plain"
                        style="margin-left : 5px"
                    >
                        {{ item }}
                    </el-tag>
                </el-col>
            </el-row>
        </div>
        <div class="bottom_side">
            <el-divider />
            <h4>数据预览</h4>
            <el-table
                :data="previewData"
                border="1"
            >
                <el-table-column
                    prop="name"
                    label="名称"
                />
                <el-table-column
                    label="数据类型"
                >
                    <template slot-scope="scope">
                        {{ scope.row.data_type }}
                    </template>
                </el-table-column>
            </el-table>
        </div>
    </el-card>
</template>

<script>
export default {
    data() {
        return {
            currentItem: {},
            loading:     false,
            dataInfo:    {},
            previewData: [],
        };
    },
    created() {
        this.currentItem.id = this.$route.query.id;
        this.currentItem.name = this.$route.query.name;
        this.getDataSetDetail();
        this.getDataSetPreview();
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
                if (data && data.list) {
                    this.dataInfo = data.list[0];
                }
            }
            this.loading = false;
        },
        async getDataSetPreview() {
            console.log(this.currentItem);
            const { code, data } = await this.$http.get({
                url:    '/data_set/preview',
                params: { id: this.currentItem.id },
            });

            if (code === 0) {
                console.log(data);
            }
        },
    },
};
</script>

<style lang="scss">
.header {
    text-align: center;
    h3 {
        font-size: 18px;
        margin: 10px 0;
    }
    p {
        font-size: 14px;
        color: #777;
    }
}
.top_side {
    .subtitle {
        font-size: 14px;
        font-weight: bold;
        margin: 10px 0;
    }
    .el-row {
        font-size: 14px;
        color: #606266;
        margin-bottom: 20px;
        &:last-child {
            margin-bottom: 0;
        }
    }
}
.bottom_side {
    .el-table {
        margin-top: 10px;
    }
}
</style>
