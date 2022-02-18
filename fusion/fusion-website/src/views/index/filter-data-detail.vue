<template>
    <el-card>
        <div class="header">
            <h3>{{ dataInfo.name }}</h3>
            <p>{{ dataInfo.id }}</p>
        </div>
        <div class="top_side">
            <h4>数据集简介</h4>
            <p class="subtitle">上传于 <span>{{ dataInfo.created_time | dateFormat }}</span> ，参与了 <span>{{ dataInfo.used_count }}</span> 任务。</p>
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
            <el-tabs
                v-loading="loading"
                type="border-card"
                @tab-click="tabChange"
            >
                <el-tab-pane label="数据信息">
                    <el-table
                        :data="previewDataInfo"
                        :border="true"
                    >
                        <el-table-column
                            prop="name"
                            label="特征名称"
                        />
                        <el-table-column
                            label="数据类型"
                        >
                            <template slot-scope="scope">
                                {{ scope.row.data_type }}
                            </template>
                        </el-table-column>
                    </el-table>
                </el-tab-pane>
                <el-tab-pane
                    label="数据预览"
                    name="preview"
                >
                    <div style="min-height: 200px;">
                        <c-grid
                            v-if="!loading"
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
                                {{ item }}
                            </c-grid-column>
                        </c-grid>
                    </div>
                </el-tab-pane>
            </el-tabs>
        </div>
    </el-card>
</template>

<script>

export default {
    data() {
        return {
            id:              '',
            name:            '',
            loading:         false,
            dataInfo:        {},
            list:            [],
            previewDataInfo: [],
            table_data:      {
                header: [],
                rows:   [],
            },
            gridTheme: {
                color:       '#6C757D',
                borderColor: '#EBEEF5',
            },
            gridHeight: 0,
        };
    },
    created() {
        this.id = this.$route.query.id;
        this.getDataSetDetail();
        this.getDataSetPreview();
    },
    methods: {

        async getDataSetDetail() {
            this.loading = true;
            const { code, data } = await this.$http.post({
                url:  '/filter/detail',
                data: {
                    id: this.id,
                },
            });

            if (code === 0) {
                if (data) {
                    this.dataInfo = data;
                }
            }
            this.loading = false;
        },
        async getDataSetPreview() {
            const { code, data } = await this.$http.get({
                url:    '/filter/preview',
                params: { id: this.id },
            });

            if (code === 0) {
                if (data && data.header) {
                    this.previewDataInfo = data.metadata_list;

                    let { length } = data.raw_data_list;

                    const rows = data.raw_data_list;

                    if(length >= 15) length = 15;

                    this.resize(length);
                    this.table_data.rows = rows;
                    this.table_data.header = data.header;
                }
            }
        },
        tabChange() {
            this.loading = true;
            setTimeout(_ => {
                this.loading = false;
            }, 200);
        },
        resize(length) {
            this.gridHeight = 41 * (length + 1) + 1;
        },
    },
};
</script>

<style lang="scss" scoped>
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
    margin-top: 20px;
}
 .c-grid {
    border: 1px solid #EBEEF5;
    position: relative;
    z-index: 1;
}
</style>
