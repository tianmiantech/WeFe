<script setup>
    import { ElMessage, ElMessageBox } from 'element-plus';
    import { Loading, InfoFilled } from '@element-plus/icons-vue';
    import { ref, onMounted, reactive, toRaw, computed, watch } from 'vue';
    import { useStore } from 'vuex';
    import http from '../../http/http';

    const tableData = reactive({ data: [], loading: false });
    const rules = {
        member_id:       [{ required: true, message: '请选择成员' }],
        gateway_address: [{ required: true, message: '请输入gatewayaddress' }],
    };
    const dialogVisible = ref(false);
    const members = ref([]);
    const ruleFormRef = ref();
    const testLoading = ref(false);
    const search = reactive({
        member_id:       '',
        gateway_address: '',
    });

    const store = useStore();

    const userInfo = computed(() => store.state.base.userInfo);

    onMounted(() => {
        http.get('/union/member/map').then(({ code, data }) => {
            if (code === 0)
                members.value = Object.entries(data)
                    .filter(([, { freezed }]) => !freezed)
                    .map(([id, values]) => ({
                        id,
                        ...values,
                    }));
        });
        refreshTable();
    });
    watch(dialogVisible, (newV) => {
        if (!newV) {
            search.member_id = '';
            search.gateway_address = '';
        }
    });

    const refreshTable = async () => {
        try {
            tableData.loading = true;
            const { code, data } = await http.post({
                url: '/partner_config/query',
            });

            if (code === 0) {
                tableData.data = data.list.map((each) => ({
                    ...each,
                    status:   null,
                    errorMsg: '',
                }));
                tableData.data.forEach(checkConnect);
            }
        } finally {
            tableData.loading = false;
        }
    };

    const okHandle = async () => {
        const valid = await ruleFormRef.value.validate();

        if (valid) {
            const { code } = await http.post({
                url:  '/partner_config/add',
                data: toRaw(search),
            });

            if (code === 0) {
                refreshTable();
                ElMessage.success('添加成功');
                dialogVisible.value = false;
            }
        }
    };

    const testHandle = async () => {
        const valid = await ruleFormRef.value.validate();

        if (valid) {
            try {
                const { member_id, gateway_address } = toRaw(search);

                testLoading.value = true;
                const { code } = await http.post({
                    url:  '/member/check_route_connect',
                    data: { member_id, member_gateway_uri: gateway_address },
                });

                if (code === 0) {
                    ElMessage.success('连接成功');
                }
            } finally {
                testLoading.value = false;
            }
        }
    };

    const checkConnect = async (row) => {
        const { member_id, gateway_address, id } = row;
        const target = tableData.data.find((each) => each.id === id);

        target.status = null;
        const { code, message } = await http.post({
            url:         '/member/check_route_connect',
            data:        { member_id, member_gateway_uri: gateway_address },
            systemError: false,
        });

        if (code === 0) {
            target.status = true;
        } else {
            target.status = false;
            target.errorMsg = message;
        }
    };
    const deleteItem = (id) => {
        ElMessageBox.confirm('确认删除此项吗？', 'Warning', {
            confirmButtonText: '确认',
            cancelButtonText:  '取消',
            type:              'warning',
        }).then(async () => {
            const { code } = await http.post({
                url:  '/partner_config/delete',
                data: { id },
            });

            if (code === 0) {
                refreshTable();
                ElMessage.success('删除成功');
            }
        });
    };

</script>
<template>
    <el-card>
        <h4>
            访问其他成员时，如果需要使用对方指定的专用gateway地址，请在此添加记录。
        </h4>
        <el-button
            :style="{ margin: '20px 0' }"
            @click="dialogVisible = true"
            :disabled="!userInfo.admin_role"
            type="primary"
        >
            添加配置
        </el-button>
        <el-table
            :data="tableData.data"
            v-loading="tableData.loading"
            stripe
            border
        >
            <el-table-column prop="member_name" label="成员" />
            <el-table-column prop="gateway_address" label="专用gateway地址" />
            <el-table-column label="状态">
                <template v-slot="scope">
                    <template v-if="scope.row.status !== null">
                        <el-tag
                            v-if="scope.row.status"
                            class="ml-2"
                            type="success"
                        >
                            正常
                        </el-tag>
                        <el-space v-else class="ml-2">
                            <el-tag type="danger">
                                异常
                            </el-tag>
                            <el-tooltip
                                effect="dark"
                                :content="scope.row.errorMsg"
                                placement="top"
                            >
                                <el-icon><InfoFilled /></el-icon>
                            </el-tooltip>
                        </el-space>
                    </template>
                    <el-icon class="is-loading" v-else>
                        <Loading />
                    </el-icon>
                </template>
            </el-table-column>
            <el-table-column label="操作">
                <template v-slot="scope">
                    <el-button
                        type="text"
                        :disabled="!userInfo.admin_role"
                        @click="deleteItem(scope.row.id)"
                    >
                        删除
                    </el-button>
                    <el-button type="text" @click="checkConnect(scope.row)">
                        测试
                    </el-button>
                </template>
            </el-table-column>
        </el-table>
        <el-dialog v-model="dialogVisible" title="添加配置" width="500px">
            <el-form
                label-position="right"
                label-width="120"
                ref="ruleFormRef"
                :model="search"
                :rules="rules"
            >
                <el-form-item
                    label="选择成员"
                    label-width="120"
                    prop="member_id"
                >
                    <el-select v-model="search.member_id" filterable clearable>
                        <el-option
                            v-for="item in members"
                            :key="item.id"
                            :label="item.name"
                            :value="item.id"
                        />
                    </el-select>
                </el-form-item>
                <el-form-item
                    label="gateway address"
                    label-width="120"
                    prop="gateway_address"
                >
                    <el-input
                        v-model="search.gateway_address"
                        placeholder="格式为host:port"
                    />
                </el-form-item>
            </el-form>
            <template #footer>
                <span>
                    <el-button @click="dialogVisible = false">取消</el-button>
                    <el-button
                        @click="testHandle"
                        type="warning"
                        :loading="testLoading"
                    >测试</el-button
                    >
                    <el-button type="primary" @click="okHandle">添加</el-button>
                </span>
            </template>
        </el-dialog>
    </el-card>
</template>
