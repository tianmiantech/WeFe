<template>
    <el-dialog
        v-model="show"
        width="750px"
        title="请选择成员"
        destroy-on-close
        :close-on-click-modal="false"
    >
        <el-form
            inline
            size="small"
            @submit.prevent
        >
            <el-form-item
                label="Id："
                label-width="40px"
            >
                <el-input
                    v-model="search.id"
                    clearable
                />
            </el-form-item>
            <el-form-item
                label="名称："
                label-width="60px"
            >
                <el-input
                    v-model="search.name"
                    clearable
                />
            </el-form-item>
            <el-button
                type="primary"
                @click="loadDataList({ resetPagination: true })"
            >
                查询
            </el-button>
        </el-form>
        <el-table
            v-loading="loading"
            max-height="500"
            :data="list"
            border
            stripe
        >
            <el-table-column label="Id" width="160">
                <template v-slot="scope">
                    {{ scope.row.name }}<br>
                    <span class="p-id">
                        {{ scope.row.id }}
                    </span>
                </template>
            </el-table-column>
            <el-table-column
                label="E-Mail"
                prop="email"
                width="180"
            />
            <el-table-column
                label="电话"
                prop="mobile"
                width="160"
            />
            <el-table-column
                label="最后活动时间"
                width="160"
            >
                <template v-slot="scope">
                    {{ dateFormat(scope.row.last_activity_time) }}
                </template>
            </el-table-column>
            <el-table-column
                label="操作"
                width="100"
                fixed="right"
                align="center"
            >
                <template v-slot="scope">
                    <el-switch
                        v-model="scope.row.$checked"
                        :disabled="scope.row.$unchanged"
                        active-color="#35c895"
                        @change="selectMemberCheckbox(scope.row, scope.$index)"
                    />
                </template>
            </el-table-column>
        </el-table>
        <div
            v-if="pagination.total"
            :class="['pagination', {'text-r': !multiple}, 'comfirm-bar']"
        >
            <el-pagination
                :pager-count="5"
                :total="pagination.total"
                :page-sizes="[10, 20, 30, 40, 50]"
                :page-size="pagination.page_size"
                :current-page="pagination.page_index"
                layout="total, sizes, prev, pager, next, jumper"
                @current-change="currentPageChange"
                @size-change="pageSizeChange"
            />
            <div class="comfirm-bar">
                <p>已选择 <span>{{ checkedNumber }}</span> 项</p>
                <el-button
                    type="primary"
                    :disabled="!checkedNumber"
                    @click="addConfirm"
                >
                    确定添加
                </el-button>
            </div>
        </div>
    </el-dialog>
</template>

<script>
    import table from '@src/mixins/table';

    export default {
        mixins: [table],
        props:  {
            multiple: {
                type:    Boolean,
                default: false,
            },
            members:             Array,
            currentDeleteMember: Object,
        },
        emits: ['select-member'],
        data() {
            return {
                loading:    false,
                show:       false,
                getListApi: '/union/member/query',
                search:     {
                    id:   '',
                    name: '',
                },
                mutipleMembers:  [],
                watchRoute:      false,
                turnPageRoute:   false,
                isIndeterminate: false,
                checkAll:        false,
            };
        },
        computed: {
            checkedNumber() {
                let total = 0;

                this.list.forEach(item => {
                    // exclude normal promoter
                    if (item.$checked && !item.$ispromoterself) {
                        total++;
                    }
                });
                return total;
            },
        },
        watch: {
            show: {
                handler(val) {
                    if(val) {
                        this.search.id = '';
                        this.search.name = '';
                        this.pagination = {
                            page_index: 1,
                            page_size:  20,
                            total:      0,
                        };
                        this.list = [];
                        this.checkAll = false;
                        this.isIndeterminate = false;
                        this.mutipleMembers = [...this.members];
                        this.loadDataList({ resetPagination: true });
                    }
                },
            },
        },
        methods: {
            async loadDataList(opt = {}) {
                this.loading = true;
                await this.getList(opt);

                this.list.forEach((item, index) => {
                    item.$checked = false;
                    item.$unchanged = false;
                    item.$ispromoterself = false;
                    this.list[index] = item;
                    for(let i = 0; i < this.mutipleMembers.length; i++) {
                        if (!this.mutipleMembers[i].exited && (this.mutipleMembers[i].audit_status !== 'disagree' || this.mutipleMembers[i].audit_status_from_myself !== 'disagree')) {
                            if(item.id === this.mutipleMembers[i].id || item.id === this.mutipleMembers[i].member_id) {
                                item.$checked = true;
                                item.$ispromoterself = true; // exclude normal promoter
                                break;
                            }
                        }
                    }

                    // members checked before cannot be deleted
                    for(let i = 0; i < this.mutipleMembers.length; i++) {
                        if (!this.mutipleMembers[i].exited && (this.mutipleMembers[i].audit_status !== 'disagree' || this.mutipleMembers[i].audit_status_from_myself !== 'disagree')) {
                            if(item.id === this.mutipleMembers[i].id || item.id === this.mutipleMembers[i].member_id) {
                                // set to disabled
                                item.$unchanged = true;
                                break;
                            }
                        }
                    }
                    // update checked item before parent deleted provider
                    if(item.id === this.currentDeleteMember.id || item.id === this.currentDeleteMember.member_id) {
                        item.$unchanged = false;
                        item.$checked = false;
                    }
                });
                this.loading = false;
            },
            selectMember(item){
                this.$emit('select-member', item);
                this.show = false;
            },
            currentPageChange(val) {
                this.pagination.page_index = val;
                this.loadDataList();
            },
            pageSizeChange(val) {
                this.pagination.page_size = val;
                this.loadDataList();
            },
            selectMemberCheckbox(item, idx) {
                this.list[idx] = item;
                if (item.$checked) {
                    this.mutipleMembers.push(item);
                } else {
                    this.removeByValue(this.mutipleMembers, 'id', item.id);
                }
            },
            removeByValue(arr, attr, value) {
                let index = 0;

                for(const i in arr){
                    if(arr[i][attr] === value){
                        index = i;
                        break;
                    }
                }
                arr.splice(index, 1);
            },
            addConfirm() {
                for (const item of this.mutipleMembers) {
                    // exclude normal promoter
                    if (!item.$ispromoterself) {
                        this.selectMember(item);
                    }
                }
            },
        },
    };
</script>

<style lang="scss" scoped>
    .pagination{
        display: flex;
        margin-top: 20px;
    }
    .comfirm-bar {
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding: 2px 5px;
        p {
            margin-right: 10px;
            span {
                color: #4D84F7;
            }
        }
    }
</style>
