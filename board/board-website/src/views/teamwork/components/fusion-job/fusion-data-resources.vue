<template>
    <el-dialog
        title="选择数据资源"
        v-model="vData.show"
        custom-class="dialog-min-width"
        :close-on-click-modal="false"
        destroy-on-close
        append-to-body
        width="70%"
    >
        <div v-loading="vData.pending">
            <el-form
                inline
                @submit.prevent
            >
                <el-form-item label="名称">
                    <el-input
                        v-model="vData.search.name"
                        clearable
                    />
                </el-form-item>
                <el-form-item>
                    <el-button
                        type="primary"
                        native-type="submit"
                        @click="methods.getList"
                    >
                        搜索
                    </el-button>
                </el-form-item>
            </el-form>

            <el-table
                :data="vData.list"
                border
                stripe
            >
                <template #empty>
                    <EmptyData />
                </template>
                <el-table-column label="序号" type="index" />
                <el-table-column
                    label="名称 / Id"
                    min-width="220"
                >
                    <template v-slot="scope">
                        {{ scope.row.data_resource.name }}
                        <p class="p-id">{{ scope.row.data_resource.data_resource_id }}</p>
                    </template>
                </el-table-column>
                <el-table-column
                    label="授权情况"
                    min-width="100"
                >
                    <template v-slot="scope">
                        <el-tag v-if="scope.row.audit_status === 'agree'">已授权</el-tag>
                        <el-tag
                            v-else
                            type="danger"
                        >
                            {{ scope.row.audit_status === 'disagree' ? '已拒绝' : '等待授权' }}
                        </el-tag>
                    </template>
                </el-table-column>
                <el-table-column
                    label="资源类型"
                    prop="data_resource_type"
                    align="center"
                    width="130"
                >
                    <template v-slot="scope">
                        {{ vData.sourceTypeMap[scope.row.data_resource_type] }}
                    </template>
                </el-table-column>
                <el-table-column
                    label="包含Y"
                    width="100"
                    align="center"
                >
                    <template v-slot="scope">
                        <p v-if="scope.row.data_resource_type === 'TableDataSet'">
                            <el-icon v-if="scope.row.data_resource.contains_y" style="color: #67C23A">
                                <elicon-check />
                            </el-icon>
                            <el-icon v-else>
                                <elicon-close />
                            </el-icon>
                        </p>
                        <p v-else>-</p>
                    </template>
                </el-table-column>
                <el-table-column
                    label="数据信息"
                    prop="row_count"
                    min-width="160"
                >
                    <template v-slot="scope">
                        <p v-if="scope.row.data_resource_type === 'BloomFilter'">
                            样本量：{{ scope.row.data_resource.total_data_count }}
                            <br>
                            主键组合方式: {{ scope.row.data_resource.hash_function || '无' }}
                        </p>
                        <template v-else>
                            特征量：{{ scope.row.data_resource.feature_count }}
                            <br>
                            样本量：{{ scope.row.data_resource.total_data_count }}
                            <template v-if="scope.row.data_resource.contains_y && scope.row.data_resource.y_positive_sample_count">
                                <br>
                                正例样本数量：{{ scope.row.data_resource.y_positive_sample_count }}
                                <br>
                                正例样本比例：{{(scope.row.data_resource.y_positive_sample_ratio * 100).toFixed(1)}}%
                            </template>
                        </template>
                    </template>
                </el-table-column>
                <el-table-column
                    label="上传时间"
                    min-width="140"
                >
                    <template v-slot="scope">
                        {{ scope.row.creator_nickname }}<br>
                        {{ dateFormat(scope.row.created_time) }}
                    </template>
                </el-table-column>
                <el-table-column
                    fixed="right"
                    label="操作"
                    width="100px"
                >
                    <template v-slot="scope">
                        <el-button
                            type="primary"
                            :disabled="methods.btnState(scope.row)"
                            @click="methods.selectDataSet(scope.row)"
                        >
                            确定
                        </el-button>
                    </template>
                </el-table-column>
            </el-table>
        </div>
    </el-dialog>
</template>

<script>
    import { nextTick, reactive, getCurrentInstance } from 'vue';

    export default {
        props: {
            project_id: String,
        },
        emits: ['selectDataSet'],
        setup(props, context) {
            const { appContext } = getCurrentInstance();
            const { $http } = appContext.config.globalProperties;

            const vData = reactive({
                loading:       false,
                show:          false,
                sourceTypeMap: {
                    BloomFilter:  '布隆过滤器',
                    ImageDataSet: '图像数据集',
                    TableDataSet: '结构化数据集',
                },
                search: {
                    name:       '',
                    role:       '',
                    member_id:  '',
                    project_id: props.project_id,
                },
            });
            const methods = {
                async getList() {
                    vData.pending = true;

                    const { code, data } = await $http.get({
                        url:    '/data_resource/member/query',
                        params: vData.search,
                    });

                    nextTick(_ => {
                        vData.pending = false;
                        if(code === 0 && data.data_set_list) {
                            vData.list = data.data_set_list;
                        }
                    });
                },
                btnState(row) {
                    return row.deleted || row.audit_status === 'disagree' || row.audit_status === 'auditing' || (props.role === 'promoter' && vData.promoter.data_set_id === row.data_set_id) || (props.role === 'provider' && vData.provider.data_set_id === row.data_set_id);
                },
                selectDataSet(item) {
                    context.emit('selectDataSet', item);
                },
            };

            return { vData, methods };
        },
    };
</script>
