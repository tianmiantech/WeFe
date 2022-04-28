<template>
    <div
        v-loading.fullscreen="loading"
        class="page"
    >
        <el-card
            name="项目简介"
            shadow="never"
            class="nav-title mb30"
            idx="-1"
        >
            <el-form @submit.prevent>
                <el-alert
                    v-if="project.closed"
                    :title="`该项目已由 ${ project.close_operator_nickname } (${ project.closed_by }) 于 ${ dateFormat(project.closed_time) } 关闭`"
                    :closable="false"
                    type="error"
                />
                <el-alert
                    v-if="form.is_exited"
                    :title="`${form.exit_operator_nickname} 已于 ${ dateFormat(form.exited_time) } 退出该项目`"
                    :closable="false"
                    type="error"
                />
                <el-alert
                    v-else-if="form.audit_status === 'disagree'"
                    :title="`已于 ${dateFormat(form.updated_time)} 拒绝加入该项目`"
                    :closable="false"
                    type="error"
                />
                <div class="project-title">
                    <h3 class="text-c">
                        {{ form.name }}
                        <br>
                        <span class="p-id">{{ form.project_id }}</span>
                    </h3>
                    <template v-if="!form.closed">
                        <PromoterProjectSetting
                            v-if="form.isCreator && !form.exited"
                            :form="form"
                        />
                        <ProviderProjectSetting
                            v-else
                            :project="project"
                        />
                    </template>
                </div>
                <el-row
                    class="pt10"
                    :gutter="20"
                >
                    <el-col
                        class="project-desc"
                        :span="form.audit_status === 'auditing' ? 12 : 24"
                    >
                        <p class="project-desc-value">
                            <span class="project-desc-key">项目简介：</span>
                            <span class="f14">{{ form.desc }}</span>
                        </p>
                        <p class="project-desc-value">
                            <span class="project-desc-key">项目类型：</span>
                            <span class="f14">{{ form.project_type === 'DeepLearning' ? '视觉处理' : form.project_type === 'MachineLearning' ? '机器学习' : '' }}</span>
                        </p>
                        <p class="project-desc-time f13 ml10">
                            <template v-if="form.isCreator">由 {{ project.creator_nickname }}</template> 创建于 {{ dateFormat(project.created_time) }}
                        </p>
                    </el-col>

                    <el-col
                        v-if="!form.closed && form.audit_status === 'auditing' && form.audit_status_from_myself === 'auditing' && !form.is_exited"
                        :span="10"
                    >
                        <!-- review self -->
                        <p
                            class="f14 mb10 color-danger"
                        >
                            参与合作审核意见:
                        </p>
                        <el-input
                            v-model="form.audit_comment"
                            type="textarea"
                            :rows="3"
                        />
                        <div class="mt20">
                            <el-button
                                type="primary"
                                @click="cooperAuth(true)"
                            >
                                同意
                            </el-button>
                            <el-button
                                type="danger"
                                @click="cooperAuth(false)"
                            >
                                拒绝
                            </el-button>
                        </div>
                    </el-col>
                </el-row>
            </el-form>
        </el-card>

        <template v-for="(item, index) in form.project_type === 'MachineLearning' ? moduleList : dModuleList" :key="item.name">
            <MembersList
                v-if="item.name === 'MembersList'"
                ref="membersListRef"
                :form="form"
                :promoter="promoter"
                :projectType="form.project_type"
                @deleteDataSetEmit="deleteDataSetEmit"
                :sort-index="index"
                :max-index="form.project_type === 'MachineLearning' ? moduleList.length-1 : dModuleList.length-1"
                @move-up="moveUp"
                @move-down="moveDown"
                @to-top="toTop"
                @to-bottom="toBottom"
            />

            <FusionList
                v-if="item.name === 'FusionList'"
                :form="form"
                :sort-index="index"
                :max-index="form.project_type === 'MachineLearning' ? moduleList.length-1 : dModuleList.length-1"
                @move-up="moveUp"
                @move-down="moveDown"
                @to-top="toTop"
                @to-bottom="toBottom"
            />

            <FlowList
                v-if="item.name === 'FlowList'"
                :form="form"
                :sort-index="index"
                :max-index="form.project_type === 'MachineLearning' ? moduleList.length-1 : dModuleList.length-1"
                @move-up="moveUp"
                @move-down="moveDown"
                @to-top="toTop"
                @to-bottom="toBottom"
            />

            <ModelingList
                v-if="item.name === 'ModelingList' && form.project_type === 'MachineLearning'"
                ref="ModelingList"
                :form="form"
                :sort-index="index"
                :max-index="form.project_type === 'MachineLearning' ? moduleList.length-1 : dModuleList.length-1"
                @move-up="moveUp"
                @move-down="moveDown"
                @to-top="toTop"
                @to-bottom="toBottom"
            />

            <DerivedList
                v-if="item.name === 'DerivedList' && form.project_type === 'MachineLearning'"
                :project-type="form.project_type"
                :sort-index="index"
                :max-index="form.project_type === 'MachineLearning' ? moduleList.length-1 : dModuleList.length-1"
                :form="form"
                @move-up="moveUp"
                @move-down="moveDown"
                @to-top="toTop"
                @to-bottom="toBottom"
            />
        </template>

        <el-dialog
            title="提示"
            width="400px"
            v-model="cooperAuthDialog.show"
            destroy-on-close
        >
            <div class="el-message-box__container">
                <el-icon class="el-message-box__status color-danger">
                    <elicon-warning-filled />
                </el-icon>
                <div class="el-message-box__message">{{ cooperAuthDialog.flag ? '同意加入合作' : '拒绝与发起方的此次项目合作' }}</div>
            </div>
            <div class="mt20 text-r">
                <el-button @click="cooperAuthDialog.show=false">
                    取消
                </el-button>
                <el-button
                    v-loading="locker"
                    type="primary"
                    @click="cooperAuthConfirm"
                >
                    确定
                </el-button>
            </div>
        </el-dialog>
    </div>
</template>

<script>
    import { mapGetters } from 'vuex';
    import FlowList from './components/flow-list';
    import DerivedList from './components/derived-list';
    import MembersList from './components/members-list';
    import FusionList from './components/fusion-job/fusion-list';
    import ModelingList from './components/modeling-list';
    import PromoterProjectSetting from './components/promoter-project-setting';
    import ProviderProjectSetting from './components/provider-project-setting';

    let timer = null;

    export default {
        components: {
            FlowList,
            DerivedList,
            MembersList,
            FusionList,
            ModelingList,
            PromoterProjectSetting,
            ProviderProjectSetting,
        },
        inject: ['refresh'],
        data() {
            return {
                locker:  false,
                loading: false,
                form:    {
                    project_id:               '',
                    closed:                   false,
                    exited:                   false,
                    isPromoter:               false,
                    isCreator:                false, // project creator
                    name:                     '',
                    desc:                     '',
                    memberList:               [],
                    promoterList:             [], // promoters
                    // project audit status
                    audit_status:             '',
                    // audit comment to other members
                    audit_comment:            '',
                    // audit comment to project
                    audit_status_from_myself: '',
                    // other member's audit comment
                    audit_status_from_others: '',
                    project_type:             'MachineLearning',
                    is_project_admin:         false,
                },
                cooperAuthDialog: {
                    show: false,
                    flag: false,
                },
                promoter: {
                    member_id:      '',
                    member_name:    '',
                    member_role:    '',
                    $data_set:      [],
                    $error:         '',
                    $serviceStatus: {
                        available:          null,
                        details:            null,
                        error_service_type: null,
                        message:            null,
                    },
                },
                promoterService: {},
                providerService: {},
                project:         {
                    closed:                  false,
                    close_operator_nickname: '',
                    closed_by:               '',
                    closed_time:             '',
                    project_id:              '',
                    name:                    '',
                    desc:                    '',
                    created_time:            null,
                    creator_nickname:        '',
                },
                getModelingList: false,
                moduleList:      [
                    {
                        name: 'MembersList',
                    },
                    {
                        name: 'FusionList',
                    },
                    {
                        name: 'FlowList',
                    },
                    {
                        name: 'ModelingList',
                    },
                    {
                        name: 'DerivedList',
                    },
                ],
                dModuleList: [
                    {
                        name: 'MembersList',
                    },
                    {
                        name: 'FlowList',
                    },
                ],
                isCustom: false, // 用户是否自定义当前项目的模块顺序
            };
        },
        computed: {
            ...mapGetters(['userInfo']),
            ...mapGetters(['uiConfig']),
            ...mapGetters(['adminUserList']),
        },
        watch: {
            moduleList: {
                handler() {
                    this.isCustom = true;
                },
                deep: true,
            },
            dModuleList: {
                handler() {
                    this.isCustom = true;
                },
                deep: true,
            },
        },
        async created() {
            this.loading = true;
            this.form.project_id = this.$route.query.project_id || '';
            this.getProjectInfo(() => {
                this.checkAllService();
            });

            this.$bus.$on('check-service-status', () => {
                this.checkAllService();
            });

            this.$bus.$on('update-member-audit-comment', ({ index, role, comment }) => {
                if (role === 'promoter') {
                    role = 'promoterList';
                }
                this.form[role][index].$audit_comment = comment;
            });
            this.$bus.$on('update-member-audit-status', ({ memberIndex, index, status, role }) => {
                if (role === 'promoter') {
                    role = 'promoterList';
                }
                this.form[role][memberIndex].$data_set[index].audit_status = status;
            });
        },
        mounted() {
            this.$bus.$on('delete-data-set-emit', (list, idx) => {
                list.splice(idx, 1);
            });
            this.$nextTick(_ => {
                this.$bus.$emit('update-title-navigator');
            });
        },
        beforeUnmount() {
            clearTimeout(timer);
            this.$bus.$off('check-service-status');
            this.$bus.$off('delete-data-set-emit');
            this.$bus.$off('update-title-navigator');
        },
        beforeUpdate() {
            this.updateProjectModuleList();
        },
        methods: {
            async updateProjectModuleList() {
                let list = {};

                if (this.uiConfig.project_module_sort) {
                    list = JSON.parse(JSON.stringify(this.uiConfig.project_module_sort));
                    if (this.isCustom) {
                        list[this.project.project_id] = this.form.project_type === 'MachineLearning' ? this.moduleList : this.dModuleList;
                    }
                }
                
                const { code } = await this.$http.post({
                    url:  '/account/update_ui_config',
                    data: {
                        uiConfig: { project_module_sort: list },
                    },
                });

                this.$nextTick(_ => {
                    if (code === 0) {
                        this.$store.commit('UI_CONFIG', { 'project_module_sort': list });
                    }
                });
            },
            async getProjectInfo(callback, opt = {
                requestFromRefresh: false,
            }) {
                // this.loading = true;
                const { code, data } = await this.$http.get({
                    url:    '/project/detail',
                    params: {
                        'request-from-refresh': opt.requestFromRefresh,
                        project_id:             this.form.project_id,
                    },
                });

                this.loading = false;
                if (code === 0) {
                    const {
                        created_time,
                        project_id,
                        promoter,
                        provider_list,
                        audit_status,
                        audit_status_from_myself,
                        audit_status_from_others,
                        creator_nickname,
                        project_desc,
                        is_creator,
                        my_role,
                        closed,
                        exit_operator_nickname,
                        close_operator_nickname,
                        closed_time,
                        closed_by,
                        name,
                        is_exited,
                        updated_time,
                        exited_time,
                        project_type,
                    } = data;
                    const promoter_list = data.promoter_list || [];

                    this.project.closed = closed;
                    this.project.closed_by = closed_by;
                    this.project.closed_time = closed_time;
                    this.project.close_operator_nickname = close_operator_nickname;
                    this.project.created_time = created_time;
                    this.project.creator_nickname = creator_nickname;
                    this.project.project_id = project_id;
                    this.project.audit_status = audit_status;
                    this.project.is_exited = is_exited;

                    this.form.name = name;
                    this.form.is_exited = is_exited;
                    this.form.exited_time = exited_time;
                    this.form.exit_nickname = exit_operator_nickname;
                    this.form.updated_time = updated_time;
                    this.form.project_type = project_type;
                    this.form.closed = closed;
                    this.form.desc = project_desc;
                    this.form.exited = promoter.exited;
                    this.form.exit_operator_nickname = exit_operator_nickname;
                    this.form.audit_status = audit_status;
                    this.form.isPromoter = my_role === 'promoter';
                    this.form.isCreator = is_creator || data.is_creator === undefined;
                    this.form.audit_status_from_myself = audit_status_from_myself || '';
                    this.form.audit_status_from_others = audit_status_from_others || '';
                    this.promoter.member_id = promoter.member_id;
                    this.promoter.member_role = promoter.member_role;
                    this.promoter.member_name = promoter.member_name;
                    this.promoter.$data_set = promoter.data_resource_list;

                    const admin_user = this.adminUserList.filter(item => item.id === this.userInfo.id) || [];

                    this.form.is_project_admin = admin_user.length > 0 ? true : false || this.userInfo.id === data.created_by;

                    const members = {};
                    const { providerService, promoterService } = this;

                    // normal promoter
                    this.form.promoterList = promoter_list.map(member => {
                        const key = member.member_id;

                        if(!members[key]) {
                            members[key] = [];
                        }
                        member.data_resource_list.forEach(dataSet => {
                            dataSet.$keywords = dataSet.data_set_keys;
                            members[key].push(dataSet);
                        });

                        return {
                            ...member,
                            $error:         '',
                            $serviceStatus: {
                                available: promoterService[member.member_id] ? promoterService[member.member_id].available : null,
                                details:   promoterService[member.member_id] ? promoterService[member.member_id].details : null,
                            }, // services state
                            $other_audit:   [], // audit comment from others
                            $audit_comment: '', // audit other members
                            $data_set:      members[key] || [],
                        };
                    });

                    // providers
                    this.form.memberList = provider_list.map((member, midx) => {
                        if(!members[midx]) {
                            members[midx] = [];
                        }
                        member.data_resource_list.forEach(dataSet => {
                            dataSet.$keywords = dataSet.data_set_keys;
                            members[midx].push(dataSet);
                        });

                        return {
                            ...member,
                            $error:         '',
                            $serviceStatus: {
                                available: providerService[member.member_id] ? providerService[member.member_id].available : null,
                                details:   providerService[member.member_id] ? providerService[member.member_id].details : null,
                            }, // service state
                            $other_audit:   [], // audit from other members
                            $audit_comment: '', // audit other members
                            $data_set:      members[midx] || [],
                        };
                    });
                    // audit from other members
                    this.otherAudit(opt);
                    callback && callback();
                    // get project/detail first
                    if(!this.getModelingList && this.form.project_type === 'MachineLearning') {
                        this.$refs['ModelingList'][0].getList();
                        this.getModelingList = true;
                    }

                    const { memberTabName } = this.$refs['membersListRef'];

                    if(!memberTabName) {
                        let role;

                        if(this.userInfo.member_id === promoter.member_id) {
                            role = 'promoter';
                        } else {
                            let i = 0;

                            promoter_list.forEach(member => {
                                if(member.member_id === this.userInfo.member_id) {
                                    i++;
                                }
                            });

                            if(i) {
                                role = 'promoter';
                            } else {
                                role = 'provider';
                            }
                        }
                        this.$refs['membersListRef'][0].memberTabName = `${this.userInfo.member_id}-${role}`;
                    }

                    // refresh audit state every 30s
                    clearTimeout(timer);
                    timer = setTimeout(() => {
                        this.getProjectInfo(null, { requestFromRefresh: true });
                    }, 30 * 10e2);
                    
                    // 自定义模块顺序
                    if (this.uiConfig.project_module_sort) {
                        const idx = Object.keys(this.uiConfig.project_module_sort).indexOf(this.form.project_id);

                        if (idx >= 0) {
                            if (this.form.project_type === 'MachineLearning') {
                                this.moduleList = Object.values(this.uiConfig.project_module_sort)[idx];
                            }
                            if (this.form.project_type === 'DeepLearning') {
                                this.dModuleList = Object.values(this.uiConfig.project_module_sort)[idx];
                            }
                        }
                    }
                }
            },

            deleteDataSetEmit(list, idx) {
                list.splice(idx, 1);
            },

            removeDataSet(row) {
                this.$confirm('删除后将不再使用当前数据样本', '警告', {
                    type: 'warning',
                })
                    .then(async action => {
                        if(action === 'confirm') {
                            const { code } = await this.$http.post({
                                url:  '/project/data_resource/remove',
                                data: {
                                    project_id:  this.form.project_id,
                                    data_set_id: row.data_set_id,
                                    member_role: row.member_role,
                                },
                            });

                            if(code === 0) {
                                this.refresh();
                                this.$message.success('操作成功!');
                            }
                        }
                    });
            },

            async otherAudit(opt = { requestFromRefresh: false }) {
                const { code, data } = await this.$http.get({
                    url:    '/project/member/add/audit/list',
                    params: {
                        'request-from-refresh': opt.requestFromRefresh,
                        project_id:             this.form.project_id,
                    },
                });

                if(code === 0) {
                    const auditMembers = {};

                    let audit_disagree_name = '';

                    data.list.forEach(member => {
                        if(!auditMembers[member.member_id]) {
                            auditMembers[member.member_id] = [];
                        }
                        auditMembers[member.member_id].push(member);
                        if (member.audit_result === 'disagree') {
                            audit_disagree_name = member.auditor_name;
                        }
                    });
                    this.form.memberList.forEach(member => {
                        member.$other_audit = auditMembers[member.member_id] || [];
                        member.audit_name = audit_disagree_name;
                    });
                    this.form.promoterList.forEach(member => {
                        member.$other_audit = auditMembers[member.member_id] || [];
                        member.audit_name = audit_disagree_name;
                    });
                }
            },

            cooperAuth(flag) {
                if(!flag && !this.form.audit_comment) {
                    return this.$alert('', {
                        title:   '警告',
                        type:    'warning',
                        message: '请填写审核意见!',
                    });
                }
                this.cooperAuthDialog.show = true;
                this.cooperAuthDialog.flag = flag;
            },

            async cooperAuthConfirm() {
                if(this.locker) return;
                this.locker = true;

                const { flag } = this.cooperAuthDialog;
                const params = {
                    project_id:    this.form.project_id,
                    audit_result:  flag ? 'agree' : 'disagree',
                    audit_comment: this.form.audit_comment,
                };
                const { code } = await this.$http.post({
                    url: '/project/add/audit',
                    params,
                });

                this.locker = false;
                if (code === 0) {
                    this.refresh();
                    this.$message.success('操作成功!');
                }
            },

            async checkAllService() {
                this.serviceStatusCheck(this.promoter, this.promoter.member_id);
                this.form.memberList.forEach(member => {
                    this.serviceStatusCheck(member, member.member_id);
                });
                this.form.promoterList.forEach(member => {
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
                    // cache service current state
                    if (role.member_role === 'promoter') {
                        this.promoterService[member_id] = data;
                    } else {
                        this.providerService[member_id] = data;
                    }
                } else {
                    role.$error = message;
                    this.providerService[member_id] = null;
                    this.promoterService[member_id] = null;
                }
            },

            // 自定义排序操作
            moveUp(idx) {
                const list = this.form.project_type === 'MachineLearning' ? this.moduleList : this.dModuleList;

                this.swapArray(list, idx, idx - 1);
            },
            moveDown(idx) {
                const list = this.form.project_type === 'MachineLearning' ? this.moduleList : this.dModuleList;

                this.swapArray(list, idx, idx + 1);
            },
            toTop(idx) {
                const list = this.form.project_type === 'MachineLearning' ? this.moduleList : this.dModuleList;

                const temp = list.splice(idx, 1)[0];

                list.unshift(temp);
            },
            toBottom(idx) {
                const list = this.form.project_type === 'MachineLearning' ? this.moduleList : this.dModuleList;

                const temp = list.splice(idx, 1)[0];

                list.push(temp);
            },
            swapArray(arr, idx1, idx2) {
                arr[idx1] = arr.splice(idx2, 1, arr[idx1])[0];
                return arr;
            },
        },
    };
</script>

<style lang="scss">
    .audit_dialog{width: 360px;}
    .el-table-maxwidth{max-width: 1000px;}
    .flex-row {
        display: flex;
        justify-content: space-between;
    }
    .right-sort-area {
        display: flex;
        justify-content: space-between;
        align-items: center;
        justify-content: space-between;
        color: #c0c0c0;
        .el-icon, span {
            cursor: pointer;
        }
    }
    .el-card__header {
        padding-bottom: unset;
    }
</style>

<style lang="scss" scoped>
    .page { padding-right: 100px; }
    .project-title{
        position: relative;
        h3{margin: 10px;}
    }
    .p-id {
        color: #999;
        font-weight: 100;
        font-size: 12px;
        line-height:18px;
        margin-top: -6px;
    }
    .project-desc{
        .project-desc-key{
            font-weight: bold;
        }
        .project-desc-value{
            line-height: 1.4;
            text-indent: 10px;
            padding: 8px 0;
        }
        .project-desc-time{padding: 8px 0;}
    }
    .el-form{
        :deep(.el-form-item__label){font-weight: bold;}
    }
    .form-item__wrap{
        display:inline-block;
        vertical-align: top;
        .el-input,
        .el-textarea{
            min-width: 300px;
        }
    }
    .cell{
        .icon{
            cursor: pointer;
            &:hover{color:#5088fc;}
        }
    }
</style>
