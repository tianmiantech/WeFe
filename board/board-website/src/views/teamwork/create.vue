<template>
    <el-card
        v-loading.fullscreen="loading"
        shadow="never"
        class="page"
    >
        <div class="step-wrap pb30">
            <span class="step">1</span>
            <h3 class="mb20">发起项目</h3>
            <el-form @submit.prevent>
                <el-form-item
                    label="项目名称"
                    label-width="80px"
                    class="is-required"
                >
                    <el-input
                        v-model="form.name"
                        maxlength="40"
                        show-word-limit
                        style="max-width:400px;"
                    />
                </el-form-item>
                <el-form-item
                    label="项目类型"
                    label-width="80px"
                    class="is-required"
                >
                    <el-select v-model="form.projectType" placeholder="请选择项目类型" style="width:400px;" @change="changeProjectType">
                        <el-option
                            v-for="item in typeList"
                            :key="item.value"
                            :label="item.label"
                            :value="item.value">
                        </el-option>
                    </el-select>
                </el-form-item>
                <el-form-item
                    label="项目描述"
                    label-width="80px"
                    class="is-required"
                >
                    <el-input
                        v-model="form.desc"
                        type="textarea"
                        :rows="4"
                        style="max-width:400px;"
                    />
                </el-form-item>
            </el-form>
        </div>

        <div class="step-wrap pb10">
            <h3 class="mb20">
                <span class="step">2</span>
                <el-tooltip>
                    <template #content>
                        <p>1、发起人可移除与新增成员；</p>
                        <p>2、成员之间只能查看，不能新增成员与相互移除。</p>
                        <p>3、新增成员后，对应成员即可在主页看到参与的项目。</p>
                        <p>4、成员可选择同意参与或主动退出项目。</p>
                        <p>5、退出参与的成员将只有看到之前参与的流程。</p>
                        6、退出参与的成员可在主页删除项目。
                    </template>
                    <span>发起方
                        <el-icon class="color-danger">
                            <elicon-warning />
                        </el-icon>
                    </span>
                </el-tooltip>
                <el-button
                    v-if="form.projectType !== 'DeepLearning'"
                    class="ml20"
                    size="small"
                    @click="showSelectMemberDialog('promoter')"
                >
                    + 添加更多发起方
                </el-button>
            </h3>
            <ul class="members mb30">
                <div class="container">
                    <div class="block">
                        <h4 class="member-name mb10">
                            {{ promoter.member_name }}
                            <MemberServiceStatus :status="promoter.$serviceStatus" />
                            <div>{{promoter.member_id}}</div>
                        </h4>
                        <p v-if="promoter.member_mobile">联系电话：{{promoter.member_mobile}}</p>
                        <p v-if="promoter.member_email">邮箱：{{promoter.member_email}}</p>
                        <p v-if="promoter.member_gateway_uri">gateway：{{promoter.member_gateway_uri}}</p>
                        <p
                            v-if="promoter.$error"
                            class="service-offline f12 pt5 mb10"
                        >
                            {{ promoter.$error }}
                        </p>
                        <div
                            class="privatenetwork"
                            v-if="selectedPrivateMembers.length"
                        >
                            <h4 class="title">专用网络</h4>
                            <template
                                v-for="{
                                    member_name,
                                    gateway_address,
                                    id
                                } in selectedPrivateMembers"
                                :key="id">
                                <p class="ip">
                                    {{ member_name }}: {{ gateway_address }}
                                </p>
                                <p class="desc">
                                    [全局设置][专用网络设置]中对"{{
                                        member_name
                                    }}"指定了专用gateway地址，我方将访问对方的该地址与之通信。
                                </p>
                            </template>
                            <el-button type="text" @click="$router.push({ name: 'network-set' });"
                            >前往修改</el-button
                            >
                        </div>
                    </div>
                    <div style="flex: 1">
                        <el-table
                            :data="promoter.$data_set"
                            :max-height="350"
                            class="mt20"
                            border
                            stripe
                        >
                            <el-table-column type="index" />
                            <el-table-column
                                label="数据资源id"
                                prop="data_resource_id"
                            />
                            <el-table-column label="数据资源名称">
                                <template v-slot="scope">
                                    <router-link :to="{ name: 'data-view', query: { id: scope.row.data_resource_id } }">
                                        {{ scope.row.name }}
                                    </router-link>
                                </template>
                            </el-table-column>
                            <el-table-column
                                v-if="form.projectType === 'MachineLearning'"
                                label="特征量/数据量"
                            >
                                <template v-slot="scope">
                                    {{ scope.row.feature_count }} / {{ scope.row.total_data_count }}
                                </template>
                            </el-table-column>
                            <el-table-column
                                v-if="form.projectType === 'MachineLearning'"
                                label="是否有 Y"
                            >
                                <template v-slot="scope">
                                    {{ scope.row.contains_y ? '是' : '否' }}
                                </template>
                            </el-table-column>
                            <el-table-column
                                v-if="form.projectType === 'DeepLearning'"
                                label="样本分类"
                                prop="for_job_type"
                            >
                                <template v-slot="scope">
                                    {{scope.row.for_job_type === 'classify' ? '图像分类' : scope.row.for_job_type === 'detection' ? '目标检测' : '-'}}
                                </template>
                            </el-table-column>
                            <el-table-column v-if="form.projectType === 'DeepLearning'" label="数据总量/已标注">
                                <template v-slot="scope">
                                    {{ scope.row.total_data_count }} / {{ scope.row.labeled_count }}
                                </template>
                            </el-table-column>
                            <el-table-column
                                v-if="form.projectType === 'DeepLearning'"
                                label="标注状态"
                                prop="label_completed"
                            >
                                <template v-slot="scope">
                                    {{scope.row.label_completed ? '已完成' : '标注中'}}
                                </template>
                            </el-table-column>
                            <el-table-column label="操作">
                                <template v-slot="scope">
                                    <el-button
                                        type="danger"
                                        icon="elicon-delete"
                                        @click="removeDataSet({ role: 'promoter_creator', memberIndex: 0, $index: scope.$index })"
                                    />
                                </template>
                            </el-table-column>
                        </el-table>
                        <el-button
                            class="mt-4"
                            style="width: 100%" @click="addDataSet('promoter_creator', userInfo.member_id, 0, promoter.$data_set)">
                            + 添加资源到此项目
                        </el-button>
                    </div>
                </div>
                <div
                    v-for="(member, memberIndex) in form.promoterList"
                    :key="`${member.member_id}-${member.member_role}`"
                    class="container"
                >
                    <div class="block">
                        <h4 class="member-name mb10">
                            {{ member.member_name }}
                            <div>{{member.member_id}}</div>
                            <MemberServiceStatus :status="member.$serviceStatus" />
                            <el-icon
                                class="board-icon-remove-outline"
                                @click="removeMember(memberIndex, 'promoter')"
                            >
                                <elicon-remove />
                            </el-icon>
                        </h4>
                        <p
                            v-if="member.$error"
                            class="service-offline f12 pt5 mb10"
                        >
                            {{ member.$error }}
                        </p>
                        <p v-if="member.mobile">联系电话：{{member.mobile}}</p>
                        <p v-if="member.email">邮箱：{{member.email}}</p>
                        <p v-if="member.gateway_uri">gateway：{{member.gateway_uri}}</p>
                    </div>
                    <div style="flex: 1">
                        <el-table
                            :data="member.$data_set"
                            :max-height="350"
                            class="mt20"
                            border
                            stripe
                        >
                            <el-table-column type="index" />
                            <el-table-column
                                label="数据资源id"
                                prop="data_resource_id"
                            />
                            <el-table-column label="数据资源名称" width="120">
                                <template v-slot="scope">
                                    <router-link :to="{ name: scope.row.member_id === userInfo.member_id ? 'data-view' : 'union-data-view', query: { id: scope.row.data_resource_id } }">
                                        {{ scope.row.name }}
                                    </router-link>
                                </template>
                            </el-table-column>
                            <el-table-column label="特征量/数据量">
                                <template v-slot="scope">
                                    {{ scope.row.feature_count }} / {{ scope.row.total_data_count }}
                                </template>
                            </el-table-column>
                            <el-table-column label="是否有 Y">
                                <template v-slot="scope">
                                    {{ scope.row.contains_y ? '是' : '否' }}
                                </template>
                            </el-table-column>
                            <el-table-column label="操作">
                                <template v-slot="scope">
                                    <el-button
                                        type="danger"
                                        icon="elicon-delete"
                                        @click="removeDataSet({ role: 'promoter', memberIndex, $index: scope.$index })"
                                    />
                                </template>
                            </el-table-column>
                        </el-table>
                        <el-button
                            class="mt-4"
                            style="width: 100%" @click="addDataSet('promoter', member.member_id, memberIndex, member.$data_set)">
                            + 添加资源到此项目
                        </el-button>
                    </div>
                </div>
            </ul>
        </div>

        <div class="step-wrap last">
            <h3 class="mb20">
                <span class="step">3</span>
                协作方
                <el-button
                    class="ml20"
                    size="small"
                    @click="showSelectMemberDialog('provider')"
                >
                    + 添加更多协作方
                </el-button>
            </h3>
            <div v-if="form.memberList.length">
                <div
                    v-for="(member, memberIndex) in form.memberList"
                    :key="`${member.member_id}-${member.member_role}`"
                    class="container"
                >
                    <div class="block">
                        <h4>
                            {{ member.member_name }}
                            <div>{{member.member_id}}</div>
                            <MemberServiceStatus :status="member.$serviceStatus" />
                            <el-icon
                                class="board-icon-remove-outline"
                                @click="removeMember(memberIndex, 'provider')"
                            >
                                <elicon-remove />
                            </el-icon>
                        </h4>
                        <p
                            v-if="member.$error"
                            class="service-offline f12 pt5 mb10"
                        >
                            {{ member.$error }}
                        </p>
                        <p v-if="member.mobile">联系电话：{{member.mobile}}</p>
                        <p v-if="member.email">邮箱：{{member.email}}</p>
                        <p v-if="member.gateway_uri">gateway：{{member.gateway_uri}}</p>
                    </div>
                    <div style="flex: 1">
                        <el-table
                            :data="member.$data_set"
                            :max-height="350"
                            class="mt20"
                            border
                            stripe
                        >
                            <el-table-column type="index" />
                            <el-table-column
                                label="数据资源id"
                                prop="data_resource_id"
                            />
                            <el-table-column label="数据资源名称" width="120">
                                <template v-slot="scope">
                                    <router-link :to="{ name: scope.row.member_id === userInfo.member_id ? 'data-view' : 'union-data-view', query: { id: scope.row.data_resource_id } }">
                                        {{ scope.row.name }}
                                    </router-link>
                                </template>
                            </el-table-column>
                            <el-table-column v-if="form.projectType === 'MachineLearning'" label="特征量/数据量">
                                <template v-slot="scope">
                                    {{ scope.row.feature_count }} / {{ scope.row.total_data_count || scope.row.row_count}}
                                </template>
                            </el-table-column>
                            <el-table-column v-if="form.projectType === 'MachineLearning'" label="是否有 Y">
                                <template v-slot="scope">
                                    {{ scope.row.contains_y ? '是' : '否' }}
                                </template>
                            </el-table-column>
                            <el-table-column
                                v-if="form.projectType === 'DeepLearning'"
                                label="样本分类"
                                prop="for_job_type"
                                width="100"
                            >
                                <template v-slot="scope">
                                    {{scope.row.for_job_type === 'classify' ? '图像分类' : scope.row.for_job_type === 'detection' ? '目标检测' : '-'}}
                                </template>
                            </el-table-column>
                            <el-table-column v-if="form.projectType === 'DeepLearning'" label="数据总量/已标注">
                                <template v-slot="scope">
                                    {{ scope.row.total_data_count }} / {{ scope.row.labeled_count }}
                                </template>
                            </el-table-column>
                            <el-table-column
                                v-if="form.projectType === 'DeepLearning'"
                                label="标注状态"
                                prop="label_completed"
                                width="100"
                            >
                                <template v-slot="scope">
                                    {{scope.row.label_completed ? '已完成' : '标注中'}}
                                </template>
                            </el-table-column>
                            <el-table-column label="操作">
                                <template v-slot="scope">
                                    <el-button
                                        type="danger"
                                        icon="elicon-delete"
                                        @click="removeDataSet({ role: 'provider', memberIndex, $index: scope.$index })"
                                    />
                                </template>
                            </el-table-column>
                        </el-table>
                        <el-button
                            class="mt-4"
                            style="width: 100%" @click="addDataSet('provider', member.member_id, memberIndex, member.$data_set)">
                            + 添加资源到此项目
                        </el-button>
                    </div>
                </div>
            </div>

            <div :style="{ marginTop: '40px' }">
                <el-button
                    type="primary"
                    :disabled="form.memberList.length === 0"
                    @click="submit"
                >
                    创建项目
                </el-button>
            </div>
            <p
                v-if="form.promoterList.length === 0 && userInfo.member_id === promoter.member_id"
                style="color:#6C757D;"
                class="f12 mt10"
            >
                <el-icon>
                    <elicon-info-filled />
                </el-icon>
                本地建模：“只支持自方成员间进行本地建模”
            </p>
        </div>

        <SelectMemberDialog
            ref="SelectMemberDialog"
            :members="checkedMembersList"
            :current-delete-member="currentDeleteMember"
            @select-member="selectMember"
        />
        <SelectDatasetDialog
            ref="SelectDatasetDialog"
            :data-sets="dataSets.list"
            :member-role="dataSets.role"
            :contains-y="`${dataSets.role !== 'provider' ? true : ''}`"
            @selectDataSet="selectDataSet"
            @batchDataSet="batchDataSet"
        />
    </el-card>
</template>

<script>
    import { mapGetters } from 'vuex';
    import { updateMemberInfo } from '@src/router/auth';
    import SelectMemberDialog from '@comp/views/select-member-dialog';
    import SelectDatasetDialog from '@comp/views/select-data-set-dialog';
    import MemberServiceStatus from './components/member-service-status';

    let canLeave = false; // a flag before leave this page

    export default {
        components: {
            MemberServiceStatus,
            SelectMemberDialog,
            SelectDatasetDialog,
        },
        data() {
            return {
                loading: false,
                form:    {
                    name:         '',
                    projectType:  'MachineLearning',
                    desc:         '',
                    memberList:   [],
                    promoterList: [],
                },
                typeList: [
                    {
                        label: '机器学习',
                        value: 'MachineLearning',
                    },
                    {
                        label: '视觉处理',
                        value: 'DeepLearning',
                    },
                ],
                promoter: {
                    member_id:      '',
                    member_name:    '',
                    $data_set:      [],
                    $online:        'loading',
                    $error:         '',
                    $serviceStatus: {
                        available:          null,
                        details:            null,
                        error_service_type: null,
                        message:            null,
                    },
                },
                dataSets: {
                    role:  '',
                    id:    '',
                    index: 0,
                    list:  [],
                },
                currentDeleteMember: {},
                memberType:          '', // promoter | provider
                checkedMembersList:  [],
                privateMembers:      [],
            };
        },
        computed: {
            ...mapGetters(['userInfo']),
            selectedPrivateMembers: ({ form, privateMembers }) => privateMembers.filter(
                ({ member_id }) => form.memberList.find((each) => each.member_id === member_id),
            ),
        },
        async created() {
            this.loading = true;
            const { code, data } = await this.$http.get({
                url: '/member/detail',
            });

            this.loading = false;
            if(code === 0) {
                this.promoter.member_id = data.member_id;
                this.promoter.member_name = data.member_name;
                this.promoter.member_email = data.member_email;
                this.promoter.member_gateway_uri = data.member_gateway_uri;
                this.promoter.member_mobile = data.member_mobile;

                updateMemberInfo(data);
            }

            this.checkAllService();
            this.$http.post('/partner_config/query').then(({ code,data }) => {
                if(code === 0)
                    this.privateMembers = data.list;
            });
        },
        beforeRouteLeave(to, from, next) {
            if(canLeave) {
                canLeave = false;
                next();
            } else {
                this.$confirm('未保存的数据将会丢失! 确定要离开当前页面吗?', '警告', {
                    type: 'warning',
                }).then(async () => {
                    canLeave = false;
                    next();
                });
            }
        },
        methods: {
            showSelectMemberDialog(type) {
                this.memberType = type;
                const memberlist = [];

                this.userInfo.$ispromoterself = true;
                memberlist.push(this.userInfo);
                this.checkedMembersList = type === 'promoter' ? this.form.promoterList.concat(memberlist) : type === 'provider' ? this.form.memberList : [];
                const ref = this.$refs['SelectMemberDialog'];

                setTimeout(() => {
                    ref.show = true;
                });
            },

            selectMember(item) {
                this.currentDeleteMember = {};
                const currentMembersList =
                    this.memberType === 'promoter'
                        ? this.form.promoterList
                        : this.memberType === 'provider'
                            ? this.form.memberList
                            : [];

                const has = currentMembersList.find(
                    (row) => row.member_id === item.id,
                );

                if (!has) {
                    /* add dataset */
                    if (!item.$data_set) {
                        item.$data_set = [];
                    }
                    if (!item.$keywords) {
                        item.$keywords = [];
                    }
                    const { length } = currentMembersList;

                    currentMembersList[length] = {
                        ...item,
                        member_name:    item.name,
                        member_id:      item.id,
                        $online:        'loading',
                        $error:         '',
                        $serviceStatus: {
                            available: null,
                            details:   null,
                        },
                    };
                    this.checkAllService(currentMembersList);
                }
            },

            async checkAllService(list = {}) {
                this.serviceStatusCheck(this.promoter, this.promoter.member_id);
                if (!list.length) return;
                list.forEach(member => {
                    this.serviceStatusCheck(member, member.member_id);
                });
            },

            async serviceStatusCheck(role, member_id) {
                role.$serviceStatus.available = null;
                const { code, data, message } = await this.$http.post({
                    url:  '/member/available',
                    data: {
                        member_id,
                    },
                });

                if(code === 0) {
                    const keys = Object.keys(data.details);

                    Object.values(data.details).forEach((key, idx) => {
                        key.service = keys[idx];
                    });
                    role.$error = '';
                    role.$serviceStatus = data;
                } else {
                    role.$error = message;
                }
            },

            removeMember(index, type) {
                let role = '', list = [];

                if (type === 'promoter') {
                    role = '发起方';
                    list = this.form.promoterList;
                } else {
                    role = '协作方';
                    list = this.form.memberList;
                }
                this.$confirm(`确定要删除该${role}吗?`, '警告', {
                    type: 'warning',
                })
                    .then(action => {
                        if(action === 'confirm') {
                            this.currentDeleteMember = list[index];
                            setTimeout(_=> {
                                list.splice(index, 1);
                            },200);

                        }
                    });
            },

            addDataSet(role, memberId, memberIndex, $data_set) {
                const ref = this.$refs['SelectDatasetDialog'];

                this.dataSets.role = role;
                this.dataSets.index = memberIndex;
                this.dataSets.list = $data_set.map(row => {
                    return {
                        ...row,
                        data_set_id: row.data_resource_id,
                    };
                });
                ref.show = true;
                this.$nextTick(async _ => {
                    ref.loadDataList({ memberId, jobRole: role, $data_set: this.dataSets.list, projectType: this.form.projectType });
                });
            },

            async batchDataSet(batchlist) {
                const { role, index } = this.dataSets;
                const row = role === 'promoter_creator' ? this.promoter : role === 'promoter' ? this.form.promoterList[index] : this.form.memberList[index];
                const list = row.$data_set;

                if (batchlist.length) {
                    batchlist.forEach(item => {
                        list.push(item);
                    });
                }
            },

            selectDataSet(item) {
                const { role, index } = this.dataSets;

                const row = role === 'promoter_creator' ? this.promoter : role === 'promoter' ? this.form.promoterList[index] : this.form.memberList[index];
                const list = row.$data_set;
                const has = list.find(row => row.data_resource_id === item.data_resource_id);

                if(!has) {
                    list.push(item);
                    if(role === 'provider') {
                        this.form.memberList[index] = { ...row };
                    }
                }
            },

            removeDataSet({ role, memberIndex, $index }) {
                this.$confirm('删除后将不再使用当前数据样本', '警告', {
                    type: 'warning',
                })
                    .then(action => {
                        if(action === 'confirm') {
                            if(role === 'promoter_creator') {
                                this.promoter.$data_set.splice($index, 1);
                            } else if (role === 'promoter') {
                                this.form.promoterList[memberIndex].$data_set.splice($index, 1);
                            } else {
                                this.form.memberList[memberIndex].$data_set.splice($index, 1);
                            }
                        }
                    });
            },

            async submit() {
                if(!this.form.name) {
                    return this.$message.error('项目名称不能为空!');
                } else if(!this.form.desc) {
                    return this.$message.error('项目描述不能为空!');
                }
                if(this.loading) return;
                this.loading = true;

                const promoterDataSetList = [];
                const providerList = [], promoterList = [];

                // promoter creator
                if(this.promoter.$data_set.length) {
                    this.promoter.$data_set.forEach(data => {
                        promoterDataSetList.push({
                            member_role:        'promoter',
                            member_id:          this.userInfo.member_id,
                            data_set_id:        data.data_resource_id,
                            data_resource_type: this.form.projectType === 'DeepLearning' ? 'ImageDataSet' : this.form.projectType === 'MachineLearning' ? 'TableDataSet' : '',
                        });
                    });
                }

                // normal promoter
                this.form.promoterList.forEach(item => {
                    const promoter = {
                        member_id:   item.member_id,
                        dataSetList: [],
                    };

                    item.$data_set.forEach(data => {
                        promoter.dataSetList.push({
                            member_role:        'promoter',
                            member_id:          item.member_id,    // promoter Id
                            data_set_id:        data.data_resource_id,
                            data_resource_type: this.form.projectType === 'DeepLearning' ? 'ImageDataSet' : this.form.projectType === 'MachineLearning' ? 'TableDataSet' : '',
                        });
                    });
                    promoterList.push(promoter);
                });

                // provider
                this.form.memberList.forEach(item => {
                    const provider = {
                        member_id:   item.member_id,
                        dataSetList: [],
                    };

                    item.$data_set.forEach(data => {
                        provider.dataSetList.push({
                            member_role:        'provider',
                            member_id:          item.member_id,    // provider Id
                            data_set_id:        data.data_resource_id,
                            data_resource_type: this.form.projectType === 'DeepLearning' ? 'ImageDataSet' : this.form.projectType === 'MachineLearning' ? 'TableDataSet' : '',
                        });
                    });
                    providerList.push(provider);
                });

                const { code, data } = await this.$http.post({
                    url:  '/project/add',
                    data: {
                        name:        this.form.name,
                        desc:        this.form.desc,
                        projectType: this.form.projectType,
                        promoterDataSetList,
                        providerList,
                        promoterList,
                    },
                });

                this.loading = false;
                if (code === 0) {
                    canLeave = true;

                    this.$router.replace({
                        name:  'project-detail',
                        query: {
                            project_id: data.project_id,
                        },
                    });
                }
            },

            changeProjectType(val) {
                this.form.memberList = [];
                this.promoter.$data_set = [];
                if (val === 'DeepLearning') {
                    this.form.promoterList = [];
                }
            },
        },
    };
</script>

<style lang="scss" scoped>
    .page{padding-left: 60px;}
    .step-wrap{
        position: relative;
        margin-top: 20px;
        .step,
        &:before{
            content: '';
            position: absolute;
        }
        .step{
            top: -3px;
            left:-52px;
            width: 24px;
            height:24px;
            font-size: 14px;
            line-height:24px;
            text-align: center;
            background:#438BFF;
            border-radius: 50%;
            color:#fff;
        }
        &:before{
            top:0;
            left:-40px;
            width: 1px;
            height:100%;
            border-left: 1px dashed #ccc;
        }
        &.last{
            padding-bottom: 20px;
            &:before{display:none;}
        }
    }
    .board-form{
        :deep(.board-form-item__label){font-weight: bold;}
    }
    .member-name{
        :deep(.iconfont),
        :deep(.status_waiting) {vertical-align: initial;}
    }
    .service-offline{
        font-size: 14px;
        color: $--color-danger;
        cursor: pointer;
    }
    .board-icon-remove-outline{
        color: $--color-danger;
        margin-left: 10px;
        font-size:14px;
        cursor: pointer;
    }
.container {
    display: flex;
    .container-table {
        max-height: 350px;
        overflow: auto;
    }
    > .block {
        width: 320px;
        margin-top: 20px;
        margin-right: 20px;
        > h4 {
            font-size: 18px;
            color: #438bff;
            > div {
                font-size: 12px;
                color: gray;
            }
        }
        > p {
            font-size: 14px;
        }
        .privatenetwork{
            margin-top: 30px;
            font-size: 14px;
            .title{
                font-weight: 600;
            }
            .desc{
                font-size: 12px;
                color:#808080;
            }
        }
    }
}
</style>
