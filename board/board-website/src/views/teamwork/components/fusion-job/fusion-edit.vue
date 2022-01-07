<template>
    <el-card
        class="page"
        shadow="never"
    >
        <h3 class="mb30">新建融合任务</h3>
        <el-form @submit.prevent>
            <el-form-item label="任务名称:" required>
                <el-input
                    v-model="vData.name"
                    show-word-limit
                    maxlength="40"
                    clearable
                />
            </el-form-item>
            <el-form-item label="任务描述:">
                <el-input
                    v-model="vData.desc"
                    type="textarea"
                    rows="5"
                    clearable
                />
            </el-form-item>
            <el-form-item label="选择算法:" required>
                <el-select v-model="vData.algorithm">
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
                <span v-if="vData.algorithm === 'RSA_PSI'" class="f12 color-danger">当前已选RSA-PSI算法，发起方或协作方至少一方需要选择布隆过滤器资源</span>

                <el-form class="el-card p20 flex-form">
                    <!-- promoter -->
                    <h4 class="f14">发起方:</h4>
                    <p>{{ vData.promoter.member_name }} <span style="color:#999;">({{ vData.promoter.member_id }})</span></p>
                    <el-button
                        v-if="!vData.promoter.data_set_id"
                        type="primary"
                        @click="methods.addDataResource('promoter')"
                    >
                        添加数据资源
                    </el-button>
                    <template v-else>
                        <el-table :data="[{}]" size="mini" border>
                            <el-table-column label="资源名称:" min-width="200">
                                <template v-slot="scope">
                                    <span style="display:none;">{{ scope.row }}</span>
                                    {{ vData.promoter.name }}
                                    <el-tag v-if="vData.provider.data_resource_type === 'BloomFilter'" type="primary">
                                        bf
                                    </el-tag>
                                    <p class="p-id f12">{{ vData.promoter.data_set_id }}</p>
                                </template>
                            </el-table-column>
                            <el-table-column label="数据量:" width="70">
                                <template v-slot="scope">
                                    <span style="display:none;">{{ scope.row }}</span>
                                    {{ vData.promoter.total_data_count }}
                                </template>
                            </el-table-column>
                            <el-table-column label="融合主键 (可选):" min-width="200">
                                <template v-slot="scope">
                                    <span style="display:none;">{{ scope.row }}</span>
                                    <el-button
                                        v-if="vData.promoter.data_resource_type !== 'BloomFilter'"
                                        @click="methods.fusionKeyMapsDialog('promoter')"
                                    >
                                        设置
                                    </el-button>
                                    <p class="mt5">融合公式: {{ vData.promoter.hash_func || '无' }}</p>
                                </template>
                            </el-table-column>
                            <el-table-column label="操作" fixed="right">
                                <el-button type="danger" @click="methods.removeDataSet('promoter')">
                                    移除
                                </el-button>
                            </el-table-column>
                        </el-table>
                    </template>

                    <!-- provider -->
                    <h4 class="mt10 f14">{{ vData.providerList.length > 1 ? '选择' : ''}}协作方:</h4>
                    <el-radio-group
                        v-if="vData.providerList.length > 1"
                        v-model="vData.provider.member_id"
                    >
                        <el-radio v-for="(item, index) in vData.providerList" :key="index" :label="item.inviter_name" />
                    </el-radio-group>
                    <p v-else>{{ vData.provider.member_name }} <span style="color:#999;">({{ vData.provider.member_id }})</span></p>
                    <p v-if="!vData.provider.data_set_id">
                        <el-button
                            type="primary"
                            :disabled="vData.providerList.length > 1 && !vData.provider.member_id"
                            @click="methods.addDataResource('provider')"
                        >
                            添加数据资源
                        </el-button>
                    </p>
                    <template v-else>
                        <el-table :data="[{}]" size="mini" border>
                            <el-table-column label="资源名称:" min-width="200">
                                <template v-slot="scope">
                                    <span style="display:none;">{{ scope.row }}</span>
                                    {{ vData.provider.name }}
                                    <el-tag v-if="vData.provider.data_resource_type === 'BloomFilter'" type="primary">
                                        bf
                                    </el-tag>
                                    <p class="p-id f12">{{ vData.provider.data_set_id }}</p>
                                </template>
                            </el-table-column>
                            <el-table-column label="数据量:" width="70">
                                <template v-slot="scope">
                                    <span style="display:none;">{{ scope.row }}</span>
                                    {{ vData.provider.total_data_count }}
                                </template>
                            </el-table-column>
                            <el-table-column label="融合主键 (可选):" min-width="200">
                                <template v-slot="scope">
                                    <span style="display:none;">{{ scope.row }}</span>
                                    <el-button
                                        v-if="vData.provider.data_resource_type !== 'BloomFilter'"
                                        @click="methods.fusionKeyMapsDialog('provider')"
                                    >
                                        设置
                                    </el-button>
                                    <p class="mt5">融合公式: {{ vData.provider.hash_func || '无' }}</p>
                                </template>
                            </el-table-column>
                            <el-table-column label="操作" fixed="right">
                                <el-button type="danger" @click="methods.removeDataSet('provider')">
                                    移除
                                </el-button>
                            </el-table-column>
                        </el-table>
                    </template>
                </el-form>
            </el-form-item>
            <el-form-item v-if="vData.status !== 'finished'">
                <el-button
                    v-if="!vData.business_id"
                    type="primary"
                    :disabled="!vData.promoter.data_set_id && !vData.provider.data_set_id"
                    @click="methods.submit"
                >
                    发起融合
                </el-button>
                <template v-else-if="vData.status === 'auditing'">
                    <el-button
                        type="primary"
                        @click="methods.audit($event, true)"
                    >
                        审核通过并运行
                    </el-button>
                    <el-button
                        type="danger"
                        @click="methods.audit($event, false)"
                    >
                        拒绝
                    </el-button>
                </template>
                <el-button
                    v-if="vData.status === 'reject'"
                    type="primary"
                    @click="methods.submit"
                >
                    重新发起融合
                </el-button>
                <el-button
                    v-if="vData.business_id && vData.status !== 'running'"
                    type="danger"
                    @click="methods.deleteTask"
                >
                    删除任务
                </el-button>
            </el-form-item>
            <el-form-item v-else>
                <el-table :data="[{}]">
                    <el-table-column label="进度">
                        <template v-slot="scope">
                            <i style="display:none;">{{ scope.row }}</i>
                            <el-progress
                                :text-inside="true"
                                :stroke-width="24"
                                :percentage="100"
                                status="success"
                            />
                        </template>
                    </el-table-column>
                    <el-table-column label="融合量">
                        <template v-slot="scope">
                            <i style="display:none;">{{ scope.row }}</i>
                            {{ vData.fusion_count }}
                        </template>
                    </el-table-column>
                    <el-table-column label="耗时">
                        <template v-slot="scope">
                            <i style="display:none;">{{ scope.row }}</i>
                            {{ dateLast(spend) }}
                        </template>
                    </el-table-column>
                </el-table>
            </el-form-item>
        </el-form>

        <!-- Select the dataset for the specified member -->
        <FusionDataResources
            ref="fusionDataResourcesRef"
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
    import EncryptionDialog from './encryption-dialog';
    import FusionDataResources from './fusion-data-resources';

    export default {
        inject:     ['refresh'],
        components: {
            FusionDataResources,
            EncryptionDialog,
        },
        setup() {
            const store = useStore();
            const route = useRoute();
            const router = useRouter();
            const { appContext } = getCurrentInstance();
            const {
                $http,
                $confirm,
                $message,
                $prompt,
            } = appContext.config.globalProperties;
            const userInfo = computed(() => store.state.base.userInfo);
            const fusionDataResourcesRef = ref(null);
            const encryptionDialogRef = ref(null);
            const {
                project_id,
                business_id,
            } = route.query;

            const vData = reactive({
                loading:           false,
                name:              '',
                desc:              '',
                algorithm:         '',
                trace_column:      '',
                is_trace:          false,
                created_time:      '',
                error:             '',
                status:            '',
                spend:             '',
                fusion_count:      0,
                field_info_list:   [],
                business_id,
                project_id,
                bloom_filter_list: [],
                algorithms:        [{
                    label: 'RSA-PSI',
                    value: 'RSA_PSI',
                }],
                promoter: {
                    member_id:          '',
                    member_name:        '',
                    data_set_id:        '',
                    name:               '',
                    columns:            [],
                    data_resource_type: '',
                    total_data_count:   0,
                    hash_func:          '',
                },
                provider: {
                    member_id:          '',
                    member_name:        '',
                    data_set_id:        '',
                    name:               '',
                    columns:            [],
                    data_resource_type: '',
                    total_data_count:   0,
                    hash_func:          '',
                },
                promoterList: [],
                providerList: [],
                currentRole:  '',
            });
            const methods = {
                async getDetail() {
                    const { code, data } = await $http.get({
                        url:    '/fusion/task/detail',
                        params: {
                            id: business_id,
                        },
                    });

                    if(code === 0) {
                        vData.name = data.name;
                        vData.desc = data.description;
                        vData.algorithm = data.algorithm;
                        vData.promoter.partner_id = data.partner_id;
                        vData.promoter.data_resource_type = data.data_resource_type;
                        vData.promoter.data_resource_id = data.data_resource_id;
                        vData.bloom_filter_list = data.bloom_filter_list;
                        vData.fusion_count = data.fusion_count;
                        vData.created_time = data.created_time;
                        vData.trace_column = data.trace_column;
                        vData.is_trace = data.is_trace;
                        vData.status = data.status;
                        vData.error = data.error;
                        vData.spend = data.spend;
                    }
                },
                async getProviders() {
                    vData.loading = true;
                    const { code, data } = await $http.get({
                        url:    '/fusion/query/providers',
                        params: {
                            project_id,
                        },
                    });

                    vData.loading = false;
                    if(code === 0 && data) {
                        if(data.length > 1) {
                            vData.providerList = data;
                        } else {
                            vData.provider = data[0];
                        }
                    }
                },
                addDataResource(role) {
                    const $ref = fusionDataResourcesRef.value;

                    vData.currentRole = role;
                    $ref.vData.show = true;
                    $ref.vData.search.role = role;
                    $ref.vData.search.member_id = vData[role].member_id;
                    $ref.methods.getList();
                },
                selectDataSet(item) {
                    const role = vData.currentRole;

                    fusionDataResourcesRef.value.vData.show = false;
                    vData[role].data_set_id = item.data_set_id;
                    vData[role].name = item.data_set.name;
                    if(item.data_set.feature_name_list) {
                        vData[role].columns = item.data_set.feature_name_list.split(',').map(x=> {
                            return {
                                label: x,
                                value: x,
                            };
                        });
                    }
                    vData[role].hash_func = item.data_set.hash_function;
                    vData[role].total_data_count = item.data_set.total_data_count;
                    vData[role].data_resource_type = item.data_resource_type;
                },
                fusionKeyMapsDialog(role) {
                    const $ref = encryptionDialogRef.value;
                    const data = vData[role];

                    $ref.methods.init(role, data);
                },
                removeDataSet(role) {
                    $confirm('确定要删除该条资源吗?', '警告', {
                        type: 'warning',
                    }).then(async () => {
                        vData[role].data_set_id = '';
                        vData[role].name = '';
                        vData[role].columns = [];
                        vData[role].data_resource_type = '';
                        vData[role].total_data_count = 0;
                    });
                },
                confirmCheck({ role, ...rest }) {
                    if(rest.encryptionList[0].encryption) {
                        vData[role].encryptionList = rest.encryptionList.map((x, i) => {
                            return {
                                columns:  x.features,
                                options:  x.encryptions,
                                position: i,
                            };
                        });
                    } else {
                        vData[role].encryptionList = [];
                    }
                    vData[role].hash_func = rest.hash_func;
                    vData.trace_column = rest.trace_column;
                    vData.is_trace = rest.is_trace;
                    console.log(rest);
                },
                deleteTask() {
                    $confirm('警告', {
                        type:    'warning',
                        message: '你确定要删除改任务吗? 此操作无法撤销!',
                    }).then(async () => {
                        const { code } = await this.$http.post({
                            url:  '/fusion/task/delete',
                            data: {
                                id: business_id,
                            },
                        });

                        if (code === 0) {
                            $message.success('删除成功!');
                            router.replace({
                                name:  'project-detail',
                                query: {
                                    project_id,
                                },
                            });
                        }
                    });
                },
                audit(event, status) {
                    $prompt('请输入审核意见', status ? '警告' : '拒绝本次合作', {
                        inputPattern:      !/^\s/,
                        inputErrorMessage: '请输入审核意见',
                    }).then(async ({ value }) => {
                        const { code } = await $http.post({
                            url:  '/fusion/task/audit',
                            data: {
                                field_info_list: vData.field_info_list,
                                row_count:       vData.promoter.total_data_count,
                                trace_column:    vData.trace_column,
                                is_trace:        vData.is_trace,
                                audit_comment:   value,
                                audit_status:    status,
                                business_id,
                            },
                        });

                        if(code === 0) {
                            $message.success('操作成功!');
                            router.replace({
                                name:  'project-detail',
                                query: {
                                    project_id,
                                },
                            });
                        }
                    });
                },
                async submit(event) {
                    const { code, data } = await $http.post({
                        url:  '/fusion/task/add',
                        data: {
                            project_id,
                            algorithm:                  vData.algorithm,
                            name:                       vData.name,
                            description:                vData.desc,
                            data_resource_type:         vData.promoter.data_resource_type,
                            data_resource_id:           vData.promoter.data_set_id,
                            dst_member_id:              vData.promoter.member_id,
                            trace_column:               vData.trace_column,
                            field_info_list:            vData.field_info_list,
                            row_count:                  vData.promoter.total_data_count,
                            is_trace:                   vData.is_trace,
                            partner_data_resource_id:   vData.provider.data_set_id,
                            partner_data_resource_type: vData.provider.data_resource_type,
                        },
                        btnState: {
                            target: event,
                        },
                    });

                    if(code === 0) {
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

            vData.promoter.member_name = userInfo.value.member_name;
            vData.promoter.member_id = userInfo.value.member_id;
            methods.getProviders();

            if(business_id) {
                methods.getDetail();
            }

            return {
                vData,
                methods,
                encryptionDialogRef,
                fusionDataResourcesRef,
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
