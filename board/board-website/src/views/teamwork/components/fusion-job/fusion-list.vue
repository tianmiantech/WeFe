<template>
    <el-card
        v-if="form.project_type !== 'DeepLearning'"
        name="数据融合"
        class="nav-title mb30"
        shadow="never"
        :show="project_type !== 'DeepLearning'"
        :idx="sortIndex"
        style="background: white"
    >
        <template #header>
            <div class="mb10 flex-row">
                <div style="display: flex; align-items: center;">
                    <h3 class="card-title f19" style="position: relative; right: 10px; top: -10px;">
                        <el-icon :class="['el-icon-connection', 'mr10', 'ml10']" style="font-size: xx-large; top:8px; right: -3px; color: dodgerblue"><elicon-connection />
                            </el-icon>
                                数据融合
                    </h3>
                    <template v-if="form.isPromoter">
                        <router-link v-if="form.is_project_admin" class="board-link" :to="{ name: 'fusion-edit', query: { project_id: form.project_id, is_project_admin: form.is_project_admin } }">
                            <el-button
                                
                                v-if="!form.closed && !form.is_exited"
                                type="primary"
                                class="ml10"
                                size="small"
                                style="position: relative; top:-4px; left: -15px; background: #007ad1; border: none;"
                            >
                            发起数据融合
                            </el-button>
                        </router-link>
                    </template>
                    <span v-else class="ml10 f12">(协作方无法创建任务)</span>
                </div>
                <div v-if="form.is_project_admin" class="right-sort-area">
                    <el-icon v-if="sortIndex !== 0" :sidx="sortIndex" :midx="maxIndex" :class="['board-icon-top f14', {'mr10': maxIndex === sortIndex}]" @click="moveUp"><elicon-top /></el-icon>
                    <el-icon v-if="maxIndex !== sortIndex" :class="['board-icon-bottom f14', 'ml10', 'mr10']" @click="moveDown"><elicon-bottom /></el-icon>
                    <span v-if="sortIndex !== 0 && sortIndex !== 1" :class="['f12', {'mr10': sortIndex === 2}]" @click="toTop">置顶</span>
                    <span v-if="sortIndex !== maxIndex && sortIndex !== maxIndex -1" class="f12" @click="toBottom">置底</span>
                </div>
            </div>
        </template>

        <el-table
            max-height="500px"
            :data="list"
            stripe
        >
            <el-table-column
                label="训练"
                min-width="220px"
            >
                <template v-slot="scope">
                    <router-link :to="{ name: 'fusion-detail', query: { id: scope.row.id, project_id, is_project_admin: form.is_project_admin } }">
                        {{ scope.row.name }}
                    </router-link>
                </template>
            </el-table-column>
            <el-table-column
                label="算法"
                prop="algorithm"
            />
            <el-table-column
                label="融合量"
                prop="fusion_count"
            />
            <el-table-column
                label="任务状态"
                min-width="160"
            >
                <template v-slot="scope">
                    <span :class="{
                        'color-danger': scope.row.status === 'Await' || scope.row.status === 'Pending' || scope.row.status === 'Failure' || scope.row.status === 'Interrupt' || scope.row.status === 'Refuse'
                    }">{{ statusMap[scope.row.status] }}</span>
                    <p>耗时: {{ timeSpend(scope.row.spend || 0) }}</p>
                </template>
            </el-table-column>
            <el-table-column
                label="创建者"
                prop="creator_nickname"
            />
            <el-table-column
                label="创建时间"
                min-width="160"
            >
                <template v-slot="scope">
                    {{ dateFormat(scope.row.created_time) }}
                </template>
            </el-table-column>
            <el-table-column
                v-if="form.audit_status !== 'disagree'"
                min-width="200"
                fixed="right"
                label="操作"
            >
                <template v-slot="scope">
                    <el-button
                        class="mr5"
                        type="text"
                        @click="checkDetail(scope.row.id)">
                        查看
                    </el-button>
                </template>
            </el-table-column>
        </el-table>
        <div
            v-if="pagination.total"
            class="mt20 text-r"
        >
            <el-pagination
                :total="pagination.total"
                :page-size="pagination.page_size"
                :page-sizes="[10, 20, 30, 40, 50]"
                :current-page="pagination.page_index"
                layout="total, sizes, prev, pager, next, jumper"
                @current-change="currentPageChange"
                @size-change="pageSizeChange"
            />
        </div>
    </el-card>
</template>

<script>
    import { mapGetters } from 'vuex';
    import table from '@src/mixins/table';

    export default {
        mixins: [table],
        inject: ['refresh'],
        props:  {
            form:      Object,
            sortIndex: Number,
            maxIndex:  Number,
        },
        emits: ['move-up', 'move-down', 'to-top', 'to-bottom'],
        data() {
            return {
                timer:      null,
                locker:     false,
                loading:    false,
                project_id: '',
                getListApi: '/fusion/task/paging',
                pagination: {
                    page_index: 1,
                    page_size:  10,
                    total:      0,
                },
                statusMap: {
                    Await:     '待审核',
                    Pending:   '待审核',
                    Running:   '运行中',
                    Success:   '成功',
                    Failure:   '失败',
                    Interrupt: '中断',
                    Refuse:    '拒绝',
                },
                flowTimer: null,
            };
        },
        computed: {
            ...mapGetters(['userInfo']),
        },
        created() {
            this.project_id = this.$route.query.project_id;
            this.project_type = this.$route.query.project_type;
            this.getTaskList();
        },
        beforeUnmount() {
            clearTimeout(this.timer);
            clearTimeout(this.flowTimer);
        },
        methods: {
            afterTableRender() {
                clearTimeout(this.timer);

                if(this.userInfo && this.userInfo.id) {
                    this.timer = setTimeout(() => {
                        this.getTaskList({
                            requestFromRefresh: true,
                        });
                    }, 5000);
                }
            },

            async getTaskList(opt = { resetPagination: false, requestFromRefresh: false }) {
                if(opt.resetPagination) {
                    this.pagination.page_index = 1;
                }

                const { code, data } = await this.$http.get({
                    url:    this.getListApi,
                    params: {
                        'request-from-refresh': opt.requestFromRefresh,
                        project_id:             this.project_id,
                        page_index:             this.pagination.page_index - 1,
                        page_size:              this.pagination.page_size,
                    },
                });

                if(code === 0) {
                    this.pagination.total = data.total || 0;
                    if(data.list.length) {
                        data.list.forEach(item => {
                            item.creator_nickname = item.creator_nickname || item.creator_member_name;
                        });
                        this.list = data.list;
                        this.afterTableRender();
                    }
                }
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

            currentPageChange (val) {
                this.pagination.page_index = val;
                this.getTaskList();
            },

            pageSizeChange (val) {
                this.pagination.page_size = val;
                this.getTaskList();
            },

            customColorMethod(percentage) {
                if (percentage < 30) {
                    return '#909399';
                } else if (percentage < 90) {
                    return '#e6a23c';
                } else {
                    return '#67c23a';
                }
            },

            deleteFlow(row, idx) {
                this.$confirm('确定要删除该流程吗? 此操作不可撤销!', '警告', {
                    type: 'warning',
                })
                    .then(async action => {
                        if(action === 'confirm') {
                            const { code } = await this.$http.post({
                                url:  '/project/flow/delete',
                                data: {
                                    flow_id: row.flow_id,
                                },
                            });

                            if(code === 0) {
                                this.list.splice(idx, 1);
                                this.getTaskList({ resetPagination: true });
                            }
                        }
                    });
            },

            checkDetail(id) {
                this.$router.push({
                    name:  'fusion-detail',
                    query: {
                        id,
                        project_id:       this.project_id,
                        is_project_admin: this.form.is_project_admin || 'true',
                    },
                });
            },
            moveUp() {
                this.$emit('move-up', this.sortIndex);
            },
            moveDown() {
                this.$emit('move-down', this.sortIndex);
            },
            toTop() {
                this.$emit('to-top', this.sortIndex);
            },
            toBottom() {
                this.$emit('to-bottom', this.sortIndex);
            },
        },
    };
</script>

<style lang="scss">

</style>

<style lang="scss" scoped>
    .board-alert__description{
        color: $--color-danger;
    }
    h3{margin: 10px;}
    .board-link{text-decoration: none;}
    .model-list{
        display: flex;
        justify-content: center;
        flex-wrap: wrap;
    }
    .li{
        margin: 0 20px 10px;
        text-align: center;
        &:hover{
            .model-img{
                transform: scale(1.02);
            }
        }
    }
    .empty-flow{
        .model-img{background: #F5F7FA;}
        .board-icon-plus{
            font-size: 50px;
            color:#DCDFE6;
        }
    }
    .model-img{
        display: block;
        width: 120px;
        height: 120px;
        line-height: 120px;
        margin-bottom: 10px;
        border:1px solid #ebebeb;
    }
    .link{text-decoration: none;}
    .btn-danger{color: #F85564;}
    .board-switch{
        :deep(.board-switch__label){
            color: #999;
            &.is-active{color: $--color-primary;}
        }
    }
</style>
