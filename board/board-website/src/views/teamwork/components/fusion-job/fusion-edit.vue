<template>
    <el-card
        class="page"
        shadow="never"
    >
        <h3 class="mb30">新建融合任务</h3>
        <el-form @submit.prevent>
            <el-form-item label="任务名称:" required>
                <el-input
                    v-model="vData.task.name"
                    show-word-limit
                    maxlength="40"
                    clearable
                />
            </el-form-item>
            <el-form-item label="任务描述:">
                <el-input
                    v-model="vData.task.desc"
                    type="textarea"
                    rows="5"
                    clearable
                />
            </el-form-item>
            <el-form-item label="选择算法:" required>
                <el-select v-model="vData.task.algorithm">
                    <el-option
                        v-for="alg in vData.algorithms"
                        :key="alg.value"
                        :label="alg.label"
                        :value="alg.value"
                    />
                </el-select>
            </el-form-item>

            <el-form-item v-loading="vData.loading" class="member-list">
                融合样本:
                <span v-if="vData.task.algorithm === 'RSA-PSI'" class="f12 color-danger">当前已选RSA-PSI算法，发起方或协作方至少一方需要选择布隆过滤器资源</span>

                <el-form class="el-card p20 flex-form">
                    <!-- promoter -->
                    <h4 class="f14">发起方:</h4>
                    <p>{{ vData.promoter.member_name }} <span style="color:#999;">({{ vData.promoter.member_id }})</span></p>
                    <el-button
                        v-if="!vData.promoter.data_set_id"
                        size="mini"
                        type="primary"
                        @click="methods.addDataResource('promoter')"
                    >
                        添加数据资源
                    </el-button>
                    <template v-else>
                        <el-table :data="[{}]" size="mini" border>
                            <el-table-column label="资源名称:" width="100">
                                <template v-slot="scope">
                                    <span style="display:none;">{{ scope.row }}</span>
                                    {{ vData.promoter.name }}
                                </template>
                            </el-table-column>
                            <el-table-column label="数据量:" width="70">
                                <template v-slot="scope">
                                    <span style="display:none;">{{ scope.row }}</span>
                                    {{ vData.promoter.total_data_count }}
                                </template>
                            </el-table-column>
                            <el-table-column label="融合主键:" min-width="200">
                                <template v-slot="scope">
                                    <span style="display:none;">{{ scope.row }}</span>
                                    <el-button
                                        v-if="vData.promoter.data_resource_type !== 'BloomFilter'"
                                        @click="methods.fusionKeyMapsDialog('promoter')"
                                    >
                                        设置
                                    </el-button>
                                    {{ vData.promoter.keys }}
                                </template>
                            </el-table-column>
                        </el-table>
                    </template>

                    <!-- provider -->
                    <h4 class="mt10 f14">{{ vData.providerList.length > 1 ? '选择' : ''}}协作方:</h4>
                    <el-radio-group
                        v-if="vData.providerList.length > 1"
                        v-model="vData.provider.member_id"
                    >
                        <el-radio v-for="(item, index) in vData.providerList" :key="index" :label="item.label">
                            {{ item.label }}
                        </el-radio>
                    </el-radio-group>
                    <p v-else>{{ vData.provider.member_name }} <span style="color:#999;">({{ vData.provider.member_id }})</span></p>
                    <p v-if="!vData.provider.data_set_id">
                        <el-button
                            type="primary"
                            :disabled="vData.providerList.length > 1 && !vData.provider.member_id"
                            @click="addDataResource('provider')"
                        >
                            添加数据资源
                        </el-button>
                    </p>
                    <template v-else>
                        <el-table :data="[{}]" size="mini" border>
                            <el-table-column label="资源名称:" width="100">
                                <template v-slot="scope">
                                    <span style="display:none;">{{ scope.row }}</span>
                                    {{ vData.provider.name }}
                                </template>
                            </el-table-column>
                            <el-table-column label="数据量:" width="70">
                                <template v-slot="scope">
                                    <span style="display:none;">{{ scope.row }}</span>
                                    {{ vData.provider.total_data_count }}
                                </template>
                            </el-table-column>
                            <el-table-column label="融合主键:" min-width="200">
                                <template v-slot="scope">
                                    <span style="display:none;">{{ scope.row }}</span>
                                    <el-button
                                        v-if="vData.provider.data_resource_type !== 'BloomFilter'"
                                        @click="fusionKeyMapsDialog('provider')"
                                    >
                                        设置
                                    </el-button>
                                    {{ vData.provider.keys }}
                                </template>
                            </el-table-column>
                        </el-table>
                    </template>
                </el-form>
            </el-form-item>
            <el-form-item>
                <el-button
                    type="primary"
                    @click="methods.submit"
                >
                    发起融合/审核通过并运行
                </el-button>
                <el-button
                    type="primary"
                    @click="methods.submit"
                >
                    重新发起融合
                </el-button>
                <el-button
                    type="primary"
                    @click="methods.submit"
                >
                    拒绝+理由
                </el-button>
            </el-form-item>
        </el-form>

        <!-- Select the dataset for the specified member -->
        <FusionDataList
            ref="fusionDataListRef"
            :project_id="vData.project_id"
            @selectDataSet="methods.selectDataSet"
        />

        <EncryptionDialog
            ref="encryptionDialogRef"
            @confirmCheck="methods.confirmCheck"
        />
    </el-card>
</template>

<script>
    import {
        ref,
        computed,
        reactive,
        getCurrentInstance,
    } from 'vue';
    import { useStore } from 'vuex';
    import { useRoute, useRouter } from 'vue-router';
    import FusionDataList from './fusion-data-list';
    import EncryptionDialog from './encryption-dialog';

    export default {
        inject:     ['refresh'],
        components: {
            FusionDataList,
            EncryptionDialog,
        },
        setup() {
            const store = useStore();
            const route = useRoute();
            const router = useRouter();
            const { appContext } = getCurrentInstance();
            const { $http, $message } = appContext.config.globalProperties;
            const userInfo = computed(() => store.state.base.userInfo);
            const encryptionDialogRef = ref(null);
            const fusionDataListRef = ref(null);
            const { project_id } = route.query;

            const vData = reactive({
                loading: false,
                task:    {
                    name:      '',
                    desc:      '',
                    algorithm: '',
                },
                algorithms: [{
                    label: 'RSA-PSI',
                    value: 'RSA-PSI',
                }],
                promoter: {
                    member_id:        '',
                    member_name:      '',
                    data_set_id:      '',
                    name:             '',
                    columns:          [],
                    encryptionList:   [],
                    total_data_count: 0,
                    key2str:          '',
                },
                provider: {
                    member_id:        '',
                    member_name:      '',
                    data_set_id:      '',
                    name:             '',
                    columns:          [],
                    encryptionList:   [],
                    total_data_count: 0,
                    key2str:          '',
                },
                promoterList:  [],
                providerList:  [],
                currentRole:   '',
                fusionKeyMaps: {
                    encryptionList: [],
                    columns:        [],
                    is_trace:       true,
                    trace_column:   [],
                    key2str:        '',
                },
                encryptions: [],
                project_id,
            });
            const methods = {
                async getDetail() {
                    const { code, data } = await $http.get({
                        url:    '/',
                        params: {
                            project_id,
                        },
                    });

                    if(code === 0) {
                        vData.fusionKeyMaps.encryptionList = data.encryptionList;
                        vData.fusionKeyMaps.columns = data.columns;
                        vData.fusionKeyMaps.is_trace = data.is_trace;
                        vData.fusionKeyMaps.trace_column = data.trace_column;
                        vData.fusionKeyMaps.key2str = data.key2str;
                    }
                },
                async getProviders() {
                    vData.loading = true;
                    const { code, data } = await $http.get({
                        url:    '/',
                        params: {
                            project_id,
                        },
                    });

                    vData.loading = false;
                    if(code === 0) {
                        vData.providerList = data.list;
                    }
                },
                addDataResource(role) {
                    const $ref = fusionDataListRef.value;

                    vData.currentRole = role;
                    $ref.vData.show = true;
                    $ref.vData.search.role = role;
                    $ref.vData.search.member_id = vData[role].member_id;
                    $ref.methods.getList();
                },
                selectDataSet(item) {
                    const role = vData.currentRole;

                    fusionDataListRef.value.vData.show = false;
                    vData[role].data_set_id = item.data_set_id;
                    vData[role].name = item.data_set.name;
                    vData[role].columns = item.data_set.feature_name_list.split(',');
                    vData[role].total_data_count = item.data_set.total_data_count;
                    vData[role].data_resource_type = item.data_resource_type;
                },
                fusionKeyMapsDialog(role) {
                    const $ref = encryptionDialogRef.value;
                    const data = vData[role];

                    $ref.methods.init(role, data);
                },
                confirmCheck(data) {
                    vData[data.role].encryptionList = data.encryptionList;
                    vData[data.role].key2str = data.key2str;
                    console.log(data);
                },
                async submit(event) {
                    const { code, data } = await $http.post({
                        url:  '/fusion/task/add',
                        data: {
                            project_id,
                            algorithm:                vData.task.algorithm,
                            name:                     vData.task.name,
                            description:              vData.task.desc,
                            dst_member_id:            vData.promoter.member_id,
                            partner_data_resource_id: vData.provider.member_id,
                        },
                        btnState: {
                            target: event,
                        },
                    });

                    if(code === 0) {
                        console.log(data);
                        $message.success('任务创建成功!');
                        router.replace({
                            name:  'fusion-detail',
                            query: {
                                project_id,
                                business_id: data.business_id,
                            },
                        });
                    }
                },
            };

            methods.getProviders();

            vData.promoter.member_name = userInfo.value.member_name;
            vData.promoter.member_id = userInfo.value.member_id;
            vData.provider.member_name = '03';
            vData.provider.member_id = '03-id';

            return {
                vData,
                methods,
                encryptionDialogRef,
                fusionDataListRef,
            };
        },
    };
</script>

<style lang="scss" scoped>
    .el-form-item{max-width: 400px;}
    .member-list{max-width: 540px;}
    .flex-form {
        .el-form-item{margin-bottom: 0;}
        :deep(.el-form-item__label){
            color:#999;
            font-size: 12px;
        }
    }
</style>
