<template>
    <el-dialog
        title="请选择成员"
        width="750px"
        v-model="show"
        destroy-on-close
    >
        <div v-loading="loading" class="dialog-wrapper">
            <el-form
                inline
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
                    native-type="submit"
                    @click="loadDataList"
                >
                    查询
                </el-button>
            </el-form>
            <el-table
                :data="list"
                max-height="500"
                border
                stripe
            >
                <el-table-column
                    prop="id"
                    label="Id"
                    min-width="220"
                >
                    <template v-slot="scope">
                        {{ scope.row.name }}<br>
                        <span class="p-id">
                            {{ scope.row.id }}
                        </span>
                    </template>
                </el-table-column>
                <el-table-column
                    label="联系方式"
                    width="200"
                >
                    <template v-slot="scope">
                        {{ scope.row.email }}<br>
                        {{ scope.row.mobile }}
                    </template>
                </el-table-column>
                <el-table-column
                    label="最后活动时间"
                    width="160"
                >
                    <template v-slot="scope">
                        {{ dateFormat(scope.row.last_activity_time) }}
                    </template>
                </el-table-column>
                <el-table-column
                    label="是否对其可见"
                    align="center"
                    min-width="120"
                >
                    <template v-slot="scope">
                        <el-switch
                            v-model="scope.row.selected"
                            active-color="#13ce66"
                            inactive-color="#ff4949"
                            @change="selectMemberCheckbox(scope.row, scope.$index)"
                        />
                    </template>
                </el-table-column>
            </el-table>
            <div
                v-if="pagination.total"
                class="mt20 text-r comfirm-bar"
            >
                <el-pagination
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
        </div>
    </el-dialog>
</template>

<script>
    import { mapGetters } from 'vuex';

    export default {
        props: {
            publicMemberInfoList: Array,
            blockMyId:            Boolean,
        },
        emits: ['selectMember'],
        data() {
            return {
                loading:       false,
                show:          false,
                list:          [],
                selected_list: [],
                search:        {
                    id:   '',
                    name: '',
                },
                pagination: {
                    page_index: 1,
                    page_size:  30,
                    total:      0,
                },
                mutipleMembers: [],
                checkedList:    [], // cache checked member
            };
        },
        computed: {
            ...mapGetters(['userInfo']),
            checkedNumber() {
                let total = 0;

                this.list.forEach(item => {
                    if (item.selected) {
                        total++;
                    }
                });
                return total;
            },
        },
        methods: {
            async loadDataList() {
                this.loading = true;

                const { code, data } = await this.$http.post({
                    url:  'union/member/query',
                    data: {
                        ...this.search,
                        page_index: this.pagination.page_index - 1,
                        page_size:  this.pagination.page_size,
                    },
                });

                if (code === 0) {
                    const hasMyId = data.list.findIndex(x => x.id === this.userInfo.member_id);
                    const list = this.blockMyId ? data.list.filter(x => x.id !== this.userInfo.member_id) : data.list;

                    for(const i in list){
                        const item = list[i];

                        item.selected = !!this.publicMemberInfoList.find(x => x.id === item.id);
                    }
                    this.pagination.total = data.total - (this.blockMyId ? (hasMyId >= 0 ? 1 : 0) : 0);
                    this.list = list;
                    this.list.forEach(item => {
                        this.publicMemberInfoList.find(sitem => {
                            if (item.id === sitem.id && this.checkedList.length < this.publicMemberInfoList.length) {
                                this.checkedList.push(item);
                            }
                        });
                    });
                }

                this.loading = false;
            },

            selectMemberCheckbox(item, idx) {
                this.list[idx] = item;
                if (item.selected) {
                    this.checkedList.push(item);
                } else {
                    this.removeByValue(this.checkedList, 'id', item.id);
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
                this.mutipleMembers = [];
                for (const item of this.checkedList) {
                    this.mutipleMembers.push(item);
                }
                this.$emit('selectMember', this.mutipleMembers);
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
