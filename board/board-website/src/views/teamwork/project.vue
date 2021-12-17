<template>
    <div class="page">
        <el-card
            class="mb20"
            shadow="never"
        >
            <el-form
                inline
                @submit.prevent
            >
                <el-form-item label="项目名称">
                    <el-input v-model="search.name" clearable />
                </el-form-item>
                <el-form-item label="参与方">
                    <el-autocomplete
                        v-model="search.member_name"
                        :fetch-suggestions="searchMember"
                        placeholder="请选择参与方"
                        :clearable="true"
                        @clear="search.member_id = ''"
                        @select="checkMember"
                    />
                </el-form-item>
                <el-form-item label="参与方角色">
                    <el-select
                        v-model="search.member_role"
                        style="width: 176px;"
                        clearable
                    >
                        <el-option
                            label="发起方"
                            value="promoter"
                        />
                        <el-option
                            label="协作方"
                            value="provider"
                        />
                    </el-select>
                </el-form-item>
                <el-form-item label="项目状态">
                    <el-select
                        v-model="search.audit_status"
                        style="width: 176px;"
                        class="mr10"
                        clearable
                    >
                        <el-option
                            v-for="item in projectStatus"
                            :key="item.value"
                            :value="item.value"
                            :label="item.label"
                        />
                    </el-select>
                </el-form-item>
                <el-form-item>
                    <el-checkbox
                        v-model="search.closed"
                        style="vertical-align:top;"
                        true-label="true"
                        false-label="false"
                    >项目已关闭</el-checkbox>
                </el-form-item>
                <el-form-item label="创建日期">
                    <DateTimePicker @change="timeChange" />
                </el-form-item>
                <el-form-item>
                    <el-button
                        type="primary"
                        native-type="submit"
                        @click="searchList"
                    >
                        搜索
                    </el-button>
                </el-form-item>
            </el-form>
        </el-card>

        <div class="lead-button text-r">
            <router-link :to="{ name: 'project-create' }">
                <el-button type="primary">
                    创建项目
                </el-button>
            </router-link>
        </div>
        <el-tabs
            v-model="activeTab"
            type="border-card"
            @tab-click="searchList"
        >
            <template
                v-for="tab in projectTabs"
                :key="tab.name"
            >
                <el-tab-pane
                    :name="tab.name"
                    :label="tab.label"
                >
                    <template #label>
                        <el-badge
                            :max="99"
                            :value="tab.count"
                            :hidden="tab.count < 1"
                            type="danger"
                        >
                            {{ tab.label }}
                        </el-badge>
                    </template>
                    <ListPart
                        :ref="tab.name"
                        :key="tab.name"
                        :tab-name="tab.name"
                        :search-key="searchRequest"
                        :my-role="tab.myRole"
                    />
                </el-tab-pane>
            </template>
        </el-tabs>
    </div>
</template>

<script>
    import { throttle } from '@src/utils/tools';
    import ListPart from './project-list';

    export default {
        components: {
            ListPart,
        },
        data() {
            return {
                timer:      null,
                isPromoter: false,
                activeTab:  'allProjects',
                member_id:  '',
                members:    [],
                search:     {
                    closed:            false,
                    name:              '',
                    member_id:         '',
                    member_name:       '',
                    member_role:       '',
                    audit_status:      '',
                    start_create_time: '',
                    end_create_time:   '',
                    my_role:           '',
                },
                searchRequest: {},
                projectStatus: [{
                    label: '已创建',
                    value: 'created',
                }, {
                    label: '等待审核',
                    value: 'auditing',
                }],
                projectTabs: [{
                    name:   'allProjects',
                    label:  '所有项目',
                    myRole: '',
                    count:  0,
                }, {
                    name:   'myProjects',
                    label:  '我发起的',
                    myRole: 'promoter',
                    count:  0,
                }, {
                    name:   'partnerProjects',
                    label:  '我协作的',
                    myRole: 'provider',
                    count:  0,
                }],
                filter: {
                    allProjects:     '',
                    myProjects:      'promoter',
                    partnerProjects: 'provider',
                },
                watchRoute: true,
            };
        },
        watch: {
            '$route.query': {
                handler (val) {
                    this.activeTab = val.activeTab || 'allProjects';
                    for (const key in this.search) {
                        this.search[key] = '';
                    }
                    for (const key in val) {
                        this.search[key] = val[key] || '';
                        this.searchRequest[key] = val[key] || '';
                    }
                    this.getProjectList();
                },
                deep: true,
            },
        },
        mounted() {
            const { query } = this.$route;

            this.activeTab = query.activeTab || 'allProjects';
            for (const key in query) {
                this.search[key] = query[key] || '';
                this.searchRequest[key] = query[key] || '';
            }
            this.$nextTick(() => {
                this.$router.replace({
                    query: {
                        ...this.search,
                        activeTab: this.activeTab,
                    },
                });
                this.getProjectList();
            });
        },
        methods: {
            async getProjectStatistic() {
                this.search.my_role = '';
                const { code, data } = await this.$http.get({
                    url:    '/project/count_statistics',
                    params: this.search,
                });

                if(code === 0) {
                    const { by_role: { promoter, provider }, total } = data;

                    this.projectTabs[0].count = total;
                    this.projectTabs[1].count = promoter;
                    this.projectTabs[2].count = provider;
                }
            },
            async searchMember(name, cb) {
                throttle.call(this, async () => {
                    const { code, data } = await this.$http.get({
                        url:    '/project/member/all',
                        params: {
                            name,
                        },
                    });

                    if(code === 0) {
                        const list = data.list.map(item => {
                            return {
                                value: item.member_name || item.member_id,
                                id:    item.member_id,
                            };
                        });

                        cb(list);
                    }
                })();
            },
            checkMember(item) {
                this.search.member_id = item.id;
            },
            searchList() {
                this.$router.push({
                    query: {
                        ...this.search,
                        activeTab: this.activeTab,
                    },
                });
                this.getProjectList();
            },
            getProjectList() {
                this.getProjectStatistic();
                this.search.my_role = this.filter[this.activeTab];
                this.searchRequest.member_name = '';
                this.$refs[this.activeTab].searchList({ to: false, resetPagination: false });
            },
            timeChange(value) {
                if(value) {
                    this.search.start_create_time = value[0];
                    this.search.end_create_time = value[1];
                } else {
                    this.search.start_create_time = '';
                    this.search.end_create_time = '';
                }
            },
        },
    };
</script>

<style lang="scss" scoped>
    .text-r{overflow: auto;}
    .lead-button{
        margin:0 10px -36px 0;
        position: relative;
        z-index: 2;
    }
    .el-tabs{
        :deep(.el-tabs__header){height: 40px;}
        :deep(.el-tabs__nav-wrap){
            overflow: visible;
            margin-bottom:0;
            .el-badge{vertical-align: top;}
        }
        :deep(.el-tabs__nav-scroll){overflow: visible;}
        :deep(.el-tabs__item){
            height: 40px;
            margin-top: 0;
        }
        :deep(.el-badge__content){
            right: -20px;
            transform:translateY(-50%) translateX(0);
        }
    }
</style>
