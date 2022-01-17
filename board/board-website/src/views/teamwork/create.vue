<template>
    <el-card
        v-loading.fullscreen="loading"
        shadow="never"
        class="page"
    >
        <div class="step-wrap pb30">
            <span class="step">1</span>
            <h3 class="mb20">发起项目</h3>
            <el-form>
                <el-form-item
                    label="项目名称"
                    label-width="80px"
                    required
                >
                    <el-input
                        v-model="form.name"
                        maxlength="40"
                        show-word-limit
                        style="max-width:400px;"
                    />
                </el-form-item>
                <el-form-item
                    label="项目描述"
                    label-width="80px"
                    required
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
                    class="ml20"
                    size="mini"
                    @click="showSelectMemberDialog('promoter')"
                >
                    + 添加更多发起方
                </el-button>
            </h3>
            <ul class="members mb30">
                <li class="mt20">
                    <h4 class="member-name mb10">
                        {{ promoter.member_name }}
                        <MemberServiceStatus :status="promoter.$serviceStatus" />
                    </h4>
                    <p
                        v-if="promoter.$error"
                        class="service-offline f12 pt5 mb10"
                    >
                        {{ promoter.$error }}
                    </p>
                    <el-button @click="addDataSet('promoter_creator', userInfo.member_id, 0, promoter.$data_set)">+ 添加数据集到此项目</el-button>
                    <el-table
                        v-if="promoter.$data_set.length"
                        :data="promoter.$data_set"
                        max-height="520px"
                        class="mt20"
                        border
                        stripe
                    >
                        <el-table-column type="index" />
                        <el-table-column
                            label="数据集id"
                            prop="id"
                        />
                        <el-table-column label="数据集名称">
                            <template v-slot="scope">
                                <router-link :to="{ name: 'data-view', query: { id: scope.row.id } }">
                                    {{ scope.row.name }}
                                </router-link>
                            </template>
                        </el-table-column>
                        <el-table-column label="特征量/数据量">
                            <template v-slot="scope">
                                {{ scope.row.feature_count }} / {{ scope.row.row_count }}
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
                                    @click="removeDataSet({ role: 'promoter_creator', memberIndex: 0, $index: scope.$index })"
                                />
                            </template>
                        </el-table-column>
                    </el-table>
                </li>
                <li
                    v-for="(member, memberIndex) in form.promoterList"
                    :key="`${member.member_id}-${member.member_role}`"
                    class="mt20"
                >
                    <h4 class="member-name mb10">
                        {{ member.member_name }}
                        <MemberServiceStatus :status="member.$serviceStatus" />
                        <el-icon
                            class="el-icon-remove-outline"
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
                    <el-button @click="addDataSet('promoter', member.member_id, memberIndex, member.$data_set)">+ 添加数据集到此项目</el-button>
                    <el-table
                        v-if="member.$data_set.length"
                        :data="member.$data_set"
                        max-height="520px"
                        class="mt20"
                        border
                        stripe
                    >
                        <el-table-column type="index" />
                        <el-table-column
                            label="数据集id"
                            prop="id"
                        />
                        <el-table-column label="数据集名称">
                            <template v-slot="scope">
                                <router-link :to="{ name: scope.row.member_id === userInfo.member_id ? 'data-view' : 'union-data-view', query: { id: scope.row.id } }">
                                    {{ scope.row.name }}
                                </router-link>
                            </template>
                        </el-table-column>
                        <el-table-column label="特征量/数据量">
                            <template v-slot="scope">
                                {{ scope.row.feature_count }} / {{ scope.row.row_count }}
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
                </li>
            </ul>
        </div>

        <div class="step-wrap last">
            <h3 class="mb20">
                <span class="step">3</span>
                协作方
                <el-button
                    class="ml20"
                    size="mini"
                    @click="showSelectMemberDialog('provider')"
                >
                    + 添加更多协作方
                </el-button>
            </h3>
            <ul
                v-if="form.memberList.length"
                class="members mb30"
            >
                <li
                    v-for="(member, memberIndex) in form.memberList"
                    :key="`${member.member_id}-${member.member_role}`"
                    class="mt20"
                >
                    <h4 class="member-name mb10">
                        {{ member.member_name }}
                        <MemberServiceStatus :status="member.$serviceStatus" />
                        <el-icon
                            class="el-icon-remove-outline"
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
                    <el-button @click="addDataSet('provider', member.member_id, memberIndex, member.$data_set)">+ 添加数据集到此项目</el-button>
                    <el-table
                        v-if="member.$data_set.length"
                        :data="member.$data_set"
                        max-height="520px"
                        class="mt20"
                        border
                        stripe
                    >
                        <el-table-column type="index" />
                        <el-table-column
                            label="数据集id"
                            prop="id"
                        />
                        <el-table-column label="数据集名称">
                            <template v-slot="scope">
                                <router-link :to="{ name: scope.row.member_id === userInfo.member_id ? 'data-view' : 'union-data-view', query: { id: scope.row.id } }">
                                    {{ scope.row.name }}
                                </router-link>
                            </template>
                        </el-table-column>
                        <el-table-column label="特征量/数据量">
                            <template v-slot="scope">
                                {{ scope.row.feature_count }} / {{ scope.row.row_count }}
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
                                    @click="removeDataSet({ role: 'provider', memberIndex, $index: scope.$index })"
                                />
                            </template>
                        </el-table-column>
                    </el-table>
                </li>
            </ul>

            <el-button
                type="primary"
                :disabled="form.memberList.length === 0"
                @click="submit"
            >
                保存项目
            </el-button>
            <p
                v-if="form.promoterList.length === 0 && userInfo.member_id === promoter.member_id"
                style="color:#6C757D;"
                class="f12 mt10"
            >
                <el-icon>
                    <elicon-info-filled />
                </el-icon>
                只有己方成员时可进行本地建模
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
                    desc:         '',
                    memberList:   [],
                    promoterList: [],
                },
                promoter: {
                    member_id:      '',
                    member_name:    '',
                    $data_set:      [],
                    $online:        'loading',
                    $error:         '',
                    $serviceStatus: {
                        all_status_is_success: null,
                        status:                null,
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
            };
        },
        computed: {
            ...mapGetters(['userInfo']),
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
            }

            this.checkAllService();
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
                const currentMembersList = this.memberType === 'promoter' ? this.form.promoterList : this.memberType === 'provider' ? this.form.memberList : [];

                const has = currentMembersList.find(row => row.member_id === item.id);

                if(!has) {
                    /* add dataset */
                    if(!item.$data_set) {
                        item.$data_set = [];
                    }
                    if(!item.$keywords) {
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
                            all_status_is_success: null,
                            status:                null,
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
                role.$serviceStatus.all_status_is_success = null;
                const { code, data, message } = await this.$http.post({
                    url:  '/member/service_status_check',
                    data: {
                        member_id,
                    },
                });

                if(code === 0) {
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
                        data_set_id: row.id,
                    };
                });
                ref.show = true;
                this.$nextTick(async _ => {
                    ref.loadDataList({ memberId, jobRole: role, $data_set: this.dataSets.list });
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
                const has = list.find(row => row.id === item.id);

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
                            member_role: 'promoter',
                            member_id:   this.userInfo.member_id,
                            data_set_id: data.id,
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
                            member_role: 'promoter',
                            member_id:   item.member_id,    // promoter Id
                            data_set_id: data.id,
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
                            member_role: 'provider',
                            member_id:   item.member_id,    // provider Id
                            data_set_id: data.id,
                        });
                    });
                    providerList.push(provider);
                });

                const { code, data } = await this.$http.post({
                    url:  '/project/add',
                    data: {
                        name: this.form.name,
                        desc: this.form.desc,
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
    .el-form{
        :deep(.el-form-item__label){font-weight: bold;}
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
    .el-icon-remove-outline{
        color: $--color-danger;
        margin-left: 10px;
        font-size:14px;
        cursor: pointer;
    }
</style>
