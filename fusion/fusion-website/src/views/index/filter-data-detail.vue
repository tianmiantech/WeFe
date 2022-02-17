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
    <!-- <el-table
        v-loading="loading"
        :data="list"
        border
    >
        <el-table-column
            label="数据集"
        >
            <template slot-scope="scope">
                <p class="id">{{ scope.row.id }}</p>
                {{ scope.row.name }}
            </template>
        </el-table-column>
        <el-table-column
            label="描述"
            prop="description"
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
    </el-table> -->
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
                url:  '/filter/query',
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
