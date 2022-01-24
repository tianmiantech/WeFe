<template>
    <el-card
        class="page"
        shadow="never"
    >
        <el-alert
            v-if="vData.status === 'Await'"
            style="max-width: 400px;margin-bottom:10px;"
            type="error"
            effect="dark"
            title="待协作方审核"
            :closable="false"
        />
        <el-alert
            v-if="vData.status === 'Failure'"
            style="max-width: 400px;margin-bottom:10px;"
            type="error"
            effect="dark"
            title="任务已失败"
            :closable="false"
        />

        <h3 class="mb30">新建融合任务</h3>
        <el-form @submit.prevent style="max-width:700px;">
            <el-form-item label="任务名称:" required>
                <el-input
                    v-model="vData.name"
                    :disabled="vData.myRole !== 'promoter' || (vData.myRole === 'promoter' && vData.status === 'Await')"
                    show-word-limit
                    maxlength="40"
                    clearable
                />
            </el-form-item>
            <el-form-item label="任务描述:">
                <el-input
                    v-model="vData.desc"
                    :disabled="vData.myRole !== 'promoter' || (vData.myRole === 'promoter' && vData.status === 'Await')"
                    type="textarea"
                    rows="5"
                    clearable
                />
            </el-form-item>
            <el-form-item required>
                <template #label>
                    选择算法
                    <el-tooltip>
                        <template #content>
                            <p class='mb5'>算法选择: </p>
                            RSA-PSI
                        </template>
                        <i class="iconfont icon-why" />
                    </el-tooltip>
                </template>
                <el-select
                    v-model="vData.algorithm"
                    :disabled="vData.myRole !== 'promoter' || (vData.myRole === 'promoter' && vData.status === 'Await')"
                >
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
                <span
                    v-if="vData.algorithm === 'RSA_PSI'"
                    class="f12 color-danger"
                >
                    RSA-PSI 算法要求至少一方需要选择布隆过滤器资源, 另一方则必须为数据集资源
                </span>

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
                            <el-table-column label="资源名称:" min-width="210">
                                <template v-slot="scope">
                                    <i style="display:none;">{{ scope.row }}</i>
                                    {{ vData.promoter.name }}
                                    <el-tag v-if="vData.promoter.data_resource_type === 'BloomFilter'">
                                        bf
                                    </el-tag>
                                    <p class="p-id f12">{{ vData.promoter.data_set_id }}</p>
                                </template>
                            </el-table-column>
                            <el-table-column label="数据量:" min-width="100">
                                <template v-slot="scope">
                                    <i style="display:none;">{{ scope.row }}</i>
                                    {{ vData.promoter.data_resource_type === 'BloomFilter' ? '布隆过滤器' : '数据集' }}
                                    <p>{{ vData.promoter.total_data_count }}</p>
                                </template>
                            </el-table-column>
                            <el-table-column label="融合主键 (可选):" min-width="200">
                                <template v-slot="scope">
                                    <span style="display:none;">{{ scope.row }}</span>
                                    <el-button
                                        v-if="vData.myRole !== 'provider' && (vData.status === '' || vData.status === 'Refuse' || vData.status === 'Interrupt' || vData.status === 'Failure' || vData.status === 'Success') && vData.promoter.data_resource_type !== 'BloomFilter'"
                                        :disabled="vData.myRole !== 'promoter'"
                                        @click="methods.fusionKeyMapsDialog('promoter')"
                                    >
                                        设置
                                    </el-button>
                                    <p class="mt5">主键组合方式: {{ vData.promoter.hash_func || '无' }}</p>
                                </template>
                            </el-table-column>
                            <el-table-column
                                v-if="vData.myRole === 'promoter' && vData.status !== 'Await' && vData.status !== 'Refuse' && vData.status !== 'Running'"
                                fixed="right"
                                label="操作"
                            >
                                <el-button
                                    type="danger"
                                    @click="methods.removeDataSet('promoter')"
                                >
                                    移除
                                </el-button>
                            </el-table-column>
                        </el-table>
                    </template>

                    <!-- provider -->
                    <h4 class="mt10 f14">{{ vData.myRole === 'promoter' && vData.providerList.length > 1 ? '选择' : ''}}协作方:</h4>
                    <el-radio-group
                        v-if="vData.myRole === 'promoter' && vData.providerList.length > 1"
                        v-model="vData.provider.member_id"
                    >
                        <el-radio v-for="(item, index) in vData.providerList" :key="index" :label="item.inviter_name" />
                    </el-radio-group>
                    <p v-else>{{ vData.provider.member_name }} <span style="color:#999;">({{ vData.provider.member_id }})</span></p>

                    <div v-if="!vData.provider.data_set_id">
                        <el-button
                            v-if="vData.myRole === 'promoter'"
                            type="primary"
                            :disabled="vData.providerList.length > 1 && !vData.provider.member_id"
                            @click="methods.addDataResource('provider')"
                        >
                            添加数据资源
                        </el-button>
                    </div>

                    <el-table
                        v-else
                        :data="[{}]"
                        size="mini"
                        border
                    >
                        <el-table-column label="资源名称:" min-width="210">
                            <template v-slot="scope">
                                <i style="display:none;">{{ scope.row }}</i>
                                {{ vData.provider.name }}
                                <el-tag v-if="vData.provider.data_resource_type === 'BloomFilter'">
                                    bf
                                </el-tag>
                                <p class="p-id f12">{{ vData.provider.data_set_id }}</p>
                            </template>
                        </el-table-column>
                        <el-table-column label="数据量:" min-width="100">
                            <template v-slot="scope">
                                <i style="display:none;">{{ scope.row }}</i>
                                <p>{{ vData.provider.data_resource_type === 'BloomFilter' ? '过滤器' : '数据集' }}</p>
                                {{ vData.provider.total_data_count }}
                            </template>
                        </el-table-column>
                        <el-table-column label="融合主键 (可选):" min-width="200">
                            <template v-slot="scope">
                                <span style="display:none;">{{ scope.row }}</span>
                                <el-button
                                    v-if="vData.status === 'Pending' && vData.provider.data_resource_type !== 'BloomFilter' && vData.provider.member_id === userInfo.member_id"
                                    @click="methods.fusionKeyMapsDialog('provider')"
                                >
                                    设置
                                </el-button>
                                <p class="mt5">主键组合方式: {{ vData.provider.hash_func || '无' }}</p>
                            </template>
                        </el-table-column>
                        <el-table-column
                            v-if="vData.myRole === 'promoter' && vData.status !== 'Await' && vData.status !== 'Refuse' && vData.status !== 'Running'"
                            fixed="right"
                            label="操作"
                        >
                            <el-button
                                type="danger"
                                @click="methods.removeDataSet('provider')"
                            >
                                移除
                            </el-button>
                        </el-table-column>
                    </el-table>
                </el-form>
            </el-form-item>

            <el-form-item v-if="vData.status === 'Running' || vData.status === 'Success' || vData.status === 'Failure' || vData.status === 'Interrupt'">
                <el-table :data="[{}]">
                    <el-table-column label="进度">
                        <template v-slot="scope">
                            <i style="display:none;">{{ scope.row }}</i>
                            <el-progress
                                :text-inside="true"
                                :stroke-width="24"
                                :percentage="vData.task.progress || 0"
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
                            {{ vData.task.spend }}
                        </template>
                    </el-table-column>
                    <el-table-column label="操作" min-width="100">
                        <template v-slot="scope">
                            <i style="display:none;">{{ scope.row }}</i>
                            <el-button
                                v-if="!vData.export_status || vData.export_status !== 'exporting'"
                                type="primary"
                                :disabled="vData.status !== 'Success'"
                                @click="vData.exportDialog.visible = true"
                            >
                                导出融合结果
                            </el-button>
                            <el-button
                                v-else-if="vData.export_status === 'exporting'"
                                type="primary"
                                @click="vData.exportDialog.onProcess = true"
                            >
                                正在导出...
                            </el-button>
                        </template>
                    </el-table-column>
                    <el-table-column v-if="vData.export_status === 'success' || vData.export_status === 'failure'" label="最后导出时间" min-width="140">
                        <template v-slot="scope">
                            <i style="display:none;">{{ scope.row }}</i>
                            <div v-if="vData.export_status === 'success'" class="mt10">
                                <p>{{ dateFormat(vData.finish_time) }}</p>
                                已导出到表:
                                <p>{{ vData.table_name }}</p>
                            </div>
                            <p v-if="vData.export_status === 'failure'" class="color-danger">导出失败, 可重试</p>
                        </template>
                    </el-table-column>
                </el-table>
            </el-form-item>

            <el-form-item v-if="vData.resultPreview.list.length" label="融合结果预览">
                <el-table
                    :data="vData.resultPreview.list"
                    border
                    stripe
                >
                    <el-table-column type="index" />
                    <el-table-column
                        v-for="head in vData.resultPreview.header"
                        :key="head"
                        :label="head"
                        :prop="head"
                    >
                    </el-table-column>
                </el-table>
            </el-form-item>

            <p v-if="vData.status === 'Refuse' && vData.comment" class="color-danger mb10">协作方拒绝原因: {{ vData.comment }}</p>

            <el-form-item>
                <el-button
                    v-if="!vData.id"
                    type="primary"
                    :disabled="!vData.promoter.data_set_id && !vData.provider.data_set_id"
                    @click="methods.submit"
                >
                    发起融合
                </el-button>
                <!-- provider -->
                <template v-else-if="vData.status === 'Pending' && vData.myRole === 'provider'">
                    <el-button
                        type="primary"
                        @click="methods.audit($event, 'agree')"
                    >
                        审核通过并运行
                    </el-button>
                    <el-button
                        type="danger"
                        @click="methods.audit($event, 'disagree')"
                    >
                        拒绝
                    </el-button>
                </template>
                <el-button
                    v-if="vData.myRole === 'promoter' && (vData.status === 'Refuse' || vData.status === 'Interrupt' || vData.status === 'Failure' || vData.status === 'Success')"
                    type="primary"
                    @click="methods.submit"
                >
                    重新发起融合
                </el-button>
                <el-button
                    v-if="vData.myRole === 'promoter' && (vData.status === 'Interrupt' || vData.status === 'Failure')"
                    type="primary"
                    @click="methods.submit"
                >
                    重跑任务
                </el-button>
                <!-- <el-button
                    v-if="vData.id && vData.status !== 'running'"
                    type="danger"
                    @click="methods.deleteTask"
                >
                    删除任务
                </el-button> -->
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

        <el-dialog
            v-model="vData.exportDialog.visible"
            title="导出融合结果"
            width="450px"
        >
            <el-form class="flex-form" label-width="100px">
                <el-form-item label="数据源">
                    <el-select v-model="vData.exportDialog.databaseType">
                        <el-option
                            v-for="(item, index) in vData.exportDialog.databaseTypes"
                            :key="index"
                            :label="item"
                            :value="item"
                        ></el-option>
                    </el-select>
                </el-form-item>
                <el-form-item label="IP">
                    <el-input type="text" v-model="vData.exportDialog.host" />
                </el-form-item>
                <el-form-item label="端口">
                    <el-input type="text" v-model="vData.exportDialog.port" />
                </el-form-item>
                <el-form-item label="库名">
                    <el-input type="text" v-model="vData.exportDialog.databaseName" />
                </el-form-item>
                <el-form-item label="数据库用户名">
                    <el-input type="text" v-model="vData.exportDialog.userName" />
                </el-form-item>
                <el-form-item label="数据库密码">
                    <el-input
                        type="password"
                        v-model="vData.exportDialog.password"
                        clearable
                    />
                </el-form-item>
            </el-form>
            <div class="text-c mt20">
                <el-button @click="methods.urlTest">
                    连接测试
                </el-button>
                <el-button
                    type="primary"
                    @click="methods.exportResult"
                >
                    导出
                </el-button>
            </div>
        </el-dialog>

        <el-dialog
            v-model="vData.exportDialog.onProcess"
            :close-on-click-modal="false"
            title="正在导出..."
            width="450px"
        >
            <div class="text-c">
                <el-progress type="circle" :percentage="vData.exportDialog.progress || 0" />

                <p class="mt10 mb5">样本总量：<span>{{vData.exportDialog.total_data_count}}</span></p>
                <p class="mb5">已处理样本量：<span>{{vData.exportDialog.processed_count}}</span></p>
            </div>
        </el-dialog>
    </el-card>
</template>

<script>
    import {
        ref,
        inject,
        computed,
        nextTick,
        reactive,
        getCurrentInstance,
    } from 'vue';
    import { useStore } from 'vuex';
    import { useRoute, useRouter } from 'vue-router';
    import EncryptionDialog from './encryption-dialog';
    import FusionDataResources from './fusion-data-resources';

    export default {
        components: {
            FusionDataResources,
            EncryptionDialog,
        },
        setup() {
            const store = useStore();
            const route = useRoute();
            const router = useRouter();
            const { appContext } = getCurrentInstance();
            const refresh = inject('refresh');
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
                id,
                project_id,
            } = route.query;

            const vData = reactive({
                id,
                project_id,
                business_id:       '',
                comment:           '',
                finish_time:       '',
                table_name:        '',
                myRole:            'promoter',
                loading:           false,
                name:              '',
                desc:              '',
                algorithm:         '',
                trace_column:      '',
                is_trace:          false,
                created_time:      '',
                error:             '',
                status:            '',
                export_status:     '',
                fusion_count:      0,
                field_info_list:   [],
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
                promoterList:  [],
                providerList:  [],
                currentRole:   '',
                task:          {},
                resultPreview: {
                    header: [],
                    list:   [],
                },
                exportDialog: {
                    visible:          false,
                    onProcess:        false,
                    databaseTypes:    ['MySql', 'Hive', 'Impala'],
                    databaseType:     '',
                    host:             '',
                    port:             '',
                    databaseName:     '',
                    userName:         '',
                    password:         '',
                    progress:         0,
                    processed_count:  0,
                    total_data_count: 0,
                    colors:           [
                        { color: '#f56c6c', percentage: 20 },
                        { color: '#e6a23c', percentage: 40 },
                        { color: '#5cb87a', percentage: 60 },
                        { color: '#1989fa', percentage: 80 },
                        { color: '#6f7ad3', percentage: 100 },
                    ],
                },
            });
            const methods = {
                async getDetail() {
                    const { code, data } = await $http.get({
                        url:    '/fusion/task/detail',
                        params: {
                            id,
                        },
                    });

                    if(code === 0) {
                        vData.name = data.name;
                        vData.myRole = data.my_role;
                        vData.desc = data.description;
                        vData.comment = data.comment;
                        vData.algorithm = data.algorithm;
                        vData.business_id = data.business_id;
                        vData.export_status = data.export_status;
                        vData.bloom_filter_list = data.bloom_filter_list;
                        vData.created_time = data.created_time;
                        vData.trace_column = data.trace_column;
                        vData.is_trace = data.is_trace;
                        vData.status = data.status;
                        vData.error = data.error;
                        if(data.promoter.data_resource_type === 'TableDataSet') {
                            vData.field_info_list = data.promoter.field_info_list;
                        }
                        if(data.provider.data_resource_type === 'TableDataSet') {
                            vData.field_info_list = data.provider.field_info_list;
                        }
                        // promoter
                        vData.promoter.member_id = data.promoter.member_id;
                        vData.promoter.member_name = data.promoter.member_name;
                        vData.promoter.data_resource_type = data.promoter.data_resource_type;
                        vData.promoter.data_set_id = data.promoter.data_resource_id;
                        vData.promoter.total_data_count = data.promoter.row_count;
                        vData.promoter.hash_func = data.promoter.hash_function;
                        vData.promoter.name = data.promoter.data_resource_name;
                        vData.promoter.columns = data.promoter.column_name_list;
                        // provider
                        vData.provider.member_id = data.provider.member_id;
                        vData.provider.member_name = data.provider.member_name;
                        vData.provider.data_resource_type = data.provider.data_resource_type;
                        vData.provider.data_set_id = data.provider.data_resource_id;
                        vData.provider.total_data_count = data.provider.row_count;
                        vData.provider.hash_func = data.provider.hash_function;
                        vData.provider.name = data.provider.data_resource_name;
                        vData.provider.columns = data.provider.column_name_list;

                        if(data.status === 'Running' || data.status === 'Success' || data.status === 'Failure' || data.status === 'Interrupt') {
                            methods.taskInfo();
                        }

                        if(data.status === 'Success') {
                            methods.getExportProgress();
                        }
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
                async taskInfo(opt = {}) {
                    const { code, data } = await $http.get({
                        url:    '/fusion/task/info',
                        params: {
                            business_id: vData.business_id,
                        },
                    });

                    nextTick(_ => {
                        if(code === 0 && data) {
                            vData.status = data.status;
                            vData.task.progress = data.progress;
                            vData.fusion_count = data.fusion_count;
                            vData.task.spend = methods.timeSpend(data.spend);

                            if(data.status === 'Success') {
                                methods.getResultPreview();
                            }

                            setTimeout(() => {
                                if(data.status === 'Running' || opt.status !== data.status) {
                                    methods.taskInfo(data);
                                }
                            }, 5000);
                        }
                    });
                },
                timeSpend(milliseconds) {
                    let ss = ~~Math.ceil(milliseconds / 1000), hh = 0, mm = 0, result = '';

                    if(ss > 3599){
                        hh = Math.floor(ss/3600);
                        mm = Math.floor(ss%3600/60);
                        ss = ss % 60;
                        result = (hh > 9 ? hh :'0' + hh) + ':' +(mm > 9 ? mm :'0' + mm) + ':' + (ss > 9 ? ss : '0' + ss);
                    } else if (ss > 59){
                        mm = Math.floor(ss/60);
                        ss = ss % 60;
                        result = '00:'+(mm > 9 ? mm : '0' + mm)+':'+(ss>9?ss:'0'+ss);
                    } else {
                        result = '00:00:'+ (ss > 9 ? ss : '0' + ss);
                    }

                    return result;
                },
                async getResultPreview() {
                    const { code, data } = await $http.get({
                        url:    '/fusion/result/preview',
                        params: {
                            business_id: vData.business_id,
                        },
                    });

                    if(code === 0) {
                        nextTick(_ => {
                            vData.resultPreview.header = data.header;
                            vData.resultPreview.list = data.list;
                        });
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
                    vData[role].hash_func = item.data_set.hash_function;
                    vData[role].columns = item.data_set.feature_name_list || '';
                    vData[role].total_data_count = item.data_set.total_data_count;
                    vData[role].data_resource_type = item.data_resource_type;
                },
                fusionKeyMapsDialog(role) {
                    const $ref = encryptionDialogRef.value;
                    const data = vData[role];

                    $ref.methods.init(role, data, vData.field_info_list);
                },
                removeDataSet(role) {
                    const { data_resource_type } = vData[role];

                    $confirm('确定要删除该条资源吗?', '警告', {
                        type: 'warning',
                    }).then(async () => {
                        vData[role].data_set_id = '';
                        vData[role].name = '';
                        vData[role].columns = [];
                        vData[role].data_resource_type = '';
                        vData[role].total_data_count = 0;

                        if(data_resource_type === 'TableDataSet') {
                            vData.field_info_list = [];
                        }
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
                    vData.field_info_list = rest.encryptionList.map((x, i) => {
                        return {
                            columns: x.features.join(','),
                            options: x.encryption,
                        };
                    });
                    vData.trace_column = rest.trace_column;
                    vData.is_trace = rest.is_trace;
                },
                /* deleteTask() {
                    $confirm('警告', {
                        type:    'warning',
                        message: '你确定要删除改任务吗? 此操作无法撤销!',
                    }).then(async () => {
                        const { code } = await this.$http.post({
                            url:  '/fusion/task/delete',
                            data: {
                                id,
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
                }, */
                audit(event, status) {
                    const fields = vData.field_info_list;

                    if(status === 'agree' && vData.provider.data_resource_type === 'TableDataSet' && (Array.isArray(fields) && !fields.length || fields == null)) return $message.error('请先设置主键组合方式');

                    const actions = status === 'agree' ? $confirm('同意本次合作', '警告', {
                        type: 'warning',
                    }) : $prompt('请输入审核意见:', '拒绝本次合作', {
                        inputPattern:      !/^\s/,
                        inputErrorMessage: '请输入审核意见',
                    });

                    actions.then(async ({ value }) => {
                        const { code } = await $http.post({
                            url:  '/fusion/task/audit',
                            data: {
                                field_info_list: fields || [],
                                business_id:     vData.business_id,
                                row_count:       vData.promoter.total_data_count,
                                trace_column:    vData.trace_column,
                                is_trace:        vData.is_trace,
                                audit_comment:   value,
                                audit_status:    status,
                                id,
                            },
                        });

                        if(code === 0) {
                            $message.success('操作成功!');
                            refresh();
                        }
                    });
                },
                async submit(event) {
                    const fields = vData.field_info_list ? vData.field_info_list.map(x => {
                        return {
                            columns:  x.columns,
                            options:  x.options,
                            position: x.position,
                        };
                    }) : [];

                    const { code } = await $http.post({
                        url:  id ? '/fusion/task/restart' : '/fusion/task/add',
                        data: {
                            project_id,
                            description:                vData.desc,
                            algorithm:                  vData.algorithm,
                            name:                       id ? `${vData.name}-copy` : vData.name,
                            data_resource_type:         vData.promoter.data_resource_type,
                            data_resource_id:           vData.promoter.data_set_id,
                            dst_member_id:              vData.provider.member_id,
                            partner_data_resource_id:   vData.provider.data_set_id,
                            partner_data_resource_type: vData.provider.data_resource_type,
                            partner_row_count:          vData.provider.total_data_count,
                            row_count:                  vData.promoter.total_data_count,
                            trace_column:               vData.trace_column,
                            is_trace:                   vData.is_trace,
                            field_info_list:            fields,
                        },
                        btnState: {
                            target: event,
                        },
                    });

                    if(code === 0) {
                        $message.success('任务创建成功!');
                        router.replace({
                            name:  'project-detail',
                            query: {
                                project_id,
                            },
                        });
                    }
                },
                async urlTest(event) {
                    const { code } = await $http.post({
                        url:  '/fusion/test_db_connect',
                        data: {
                            business_id:  vData.business_id,
                            databaseType: vData.exportDialog.databaseType,
                            host:         vData.exportDialog.host,
                            port:         vData.exportDialog.port,
                            databaseName: vData.exportDialog.databaseName,
                            userName:     vData.exportDialog.userName,
                            password:     vData.exportDialog.password,
                        },
                        btnState: {
                            target: event,
                        },
                    });

                    if(code === 0) {
                        $message.success('链接可用!');
                    }
                },
                async exportResult(event) {
                    const { code } = await $http.post({
                        url:  '/fusion/result/export',
                        data: {
                            business_id:  vData.business_id,
                            databaseType: vData.exportDialog.databaseType,
                            host:         vData.exportDialog.host,
                            port:         vData.exportDialog.port,
                            databaseName: vData.exportDialog.databaseName,
                            userName:     vData.exportDialog.userName,
                            password:     vData.exportDialog.password,
                        },
                        btnState: {
                            target: event,
                        },
                    });

                    if(code === 0) {
                        nextTick(_ => {
                            vData.exportDialog.visible = false;
                            vData.exportDialog.onProcess = true;

                            $message.success('正在导出!');
                            methods.getExportProgress();
                        });
                    }
                },
                async getExportProgress(opt = { progress: -1 }) {
                    const { code, data } = await $http.get({
                        url:    '/fusion/result/export_progress',
                        params: {
                            business_id: vData.business_id,
                        },
                    });

                    if(code === 0) {
                        nextTick(_ => {
                            vData.table_name = data.table_name;
                            vData.finish_time = data.finish_time;
                            vData.exportDialog.progress = data.progress;
                            vData.exportDialog.processed_count = data.processed_count;
                            vData.exportDialog.total_data_count = data.total_data_count;
                            vData.export_status = data.status;

                            if(data.progress !== opt.progress) {
                                if(data.progress === 100) {
                                    vData.exportDialog.onProcess = false;
                                } else {
                                    setTimeout(() => {
                                        methods.getExportProgress({ progress: data.progress });
                                    }, 3000);
                                }
                            }
                        });
                    }
                },
            };

            if(id) {
                methods.getDetail();
            } else {
                vData.promoter.member_id = userInfo.value.member_id;
                vData.promoter.member_name = userInfo.value.member_name;
                methods.getProviders();
            }

            return {
                vData,
                methods,
                userInfo,
                encryptionDialogRef,
                fusionDataResourcesRef,
            };
        },
    };
</script>

<style lang="scss" scoped>
    .el-input,
    .el-textarea{max-width: 400px;}
    .member-list{max-width: 650px;
        .flex-form {
            .el-form-item{margin-bottom: 0;}
            :deep(.el-form-item__label){
                color:#999;
                font-size: 12px;
            }
        }
    }
</style>
