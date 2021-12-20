<template>
    <div
        v-loading.fullscreen="loading"
        class="page"
    >
        <el-card
            name="项目简介"
            shadow="never"
            class="nav-title mb30"
        >
            <el-form>
                <el-alert
                    v-if="project.closed"
                    :title="`该项目已由 ${ project.close_operator_nickname } (${ project.closed_by }) 于 ${ dateFormat(project.closed_time) } 关闭`"
                    :closable="false"
                    type="error"
                />
                <el-alert
                    v-if="form.is_exited"
                    :title="`已于 ${ dateFormat(form.exited_time) } 退出该项目`"
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
                            <span class="f14">{{ form.project_type }}</span>
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

        <MembersList
            ref="membersListRef"
            :promoter="promoter"
            :projectType="form.project_type"
            :form="form"
            @deleteDataSetEmit="deleteDataSetEmit"
        />

        <FlowList :form="form" />

        <el-card
            name="TopN 展示"
            shadow="never"
            style="display:none;"
        >
            <h3 class="mb10">TopN 展示</h3>
            <TopN ref="topnRef"></TopN>
        </el-card>
        
        <ModelingList
            v-if="form.project_type === 'MachineLearning'"
            ref="ModelingList"
            :form="form"
        />

        <DerivedList v-if="form.project_type === 'MachineLearning'" :project-type="form.project_type" />

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
    import ModelingList from './components/modeling-list';
    import PromoterProjectSetting from './components/promoter-project-setting';
    import ProviderProjectSetting from './components/provider-project-setting';
    import TopN from '@views/teamwork/visual/component-list/Evaluation/TopN.vue';

    let timer = null;

    export default {
        components: {
            FlowList,
            DerivedList,
            MembersList,
            ModelingList,
            PromoterProjectSetting,
            ProviderProjectSetting,
            TopN,
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
                        all_status_is_success: null,
                        status:                null,
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
            };
        },
        computed: {
            ...mapGetters(['userInfo']),
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
        methods: {
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
                    this.form.project_type = project_type;
                    this.form.is_exited = is_exited;
                    this.form.exited_time = exited_time;
                    this.form.updated_time = updated_time;
                    this.form.closed = closed;
                    this.form.desc = project_desc;
                    this.form.exited = promoter.exited;
                    this.form.audit_status = audit_status;
                    this.form.isPromoter = my_role === 'promoter';
                    this.form.isCreator = is_creator || data.is_creator === undefined;
                    this.form.audit_status_from_myself = audit_status_from_myself || '';
                    this.form.audit_status_from_others = audit_status_from_others || '';
                    this.promoter.member_id = promoter.member_id;
                    this.promoter.member_role = promoter.member_role;
                    this.promoter.member_name = promoter.member_name;
                    this.promoter.$data_set = promoter.data_set_list;

                    const members = {};
                    const { providerService, promoterService } = this;

                    // normal promoter
                    this.form.promoterList = promoter_list.map(member => {
                        const key = member.member_id;

                        if(!members[key]) {
                            members[key] = [];
                        }
                        member.data_set_list.forEach(dataSet => {
                            dataSet.$keywords = dataSet.data_set_keys;
                            members[key].push(dataSet);
                        });

                        return {
                            ...member,
                            $error:         '',
                            $serviceStatus: {
                                all_status_is_success: promoterService[member.member_id] ? promoterService[member.member_id].all_status_is_success : null,
                                status:                promoterService[member.member_id] ? promoterService[member.member_id].status : null,
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
                        member.data_set_list.forEach(dataSet => {
                            dataSet.$keywords = dataSet.data_set_keys;
                            members[midx].push(dataSet);
                        });

                        return {
                            ...member,
                            $error:         '',
                            $serviceStatus: {
                                all_status_is_success: providerService[member.member_id] ? providerService[member.member_id].all_status_is_success : null,
                                status:                providerService[member.member_id] ? providerService[member.member_id].status : null,
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
                        this.$refs['ModelingList'].getList();
                        this.getModelingList = true;
                    }

                    const { memberTabName } = this.$refs['membersListRef'];

                    if(!memberTabName) {
                        this.$refs['membersListRef'].memberTabName = `${this.promoter.member_id}-${this.promoter.member_role}`;
                    }

                    // refresh audit state every 30s
                    clearTimeout(timer);
                    timer = setTimeout(() => {
                        this.getProjectInfo(null, { requestFromRefresh: true });
                    }, 30 * 10e2);
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
        },
    };
</script>

<style lang="css">
    .audit_dialog{width: 360px;}
    .el-table-maxwidth{max-width: 1000px;}
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
