<template>
    <el-card
        class="page"
        shadow="never"
    >
        <el-form
            inline
            class="mb20"
            @submit.prevent
        >
            <el-form-item label="数据集 ID：">
                <el-input
                    v-model="vData.search.id"
                    clearable
                />
            </el-form-item>
            <el-form-item label="数据集名称：">
                <el-input
                    v-model="vData.search.name"
                    clearable
                />
            </el-form-item>
            <el-form-item label="成员：">
                <el-select
                    v-model="vData.search.member_id"
                    filterable
                    clearable
                >
                    <el-option
                        v-for="(item, index) in vData.member_list"
                        :key="index"
                        :label="item.name"
                        :value="item.id"
                    />
                </el-select>
            </el-form-item>
            <el-form-item label="关键词：">
                <el-select
                    v-model="vData.search.tag"
                    filterable
                    clearable
                >
                    <el-option
                        v-for="(item) in vData.tag_list"
                        :key="item.tag_name"
                        :value="item.tag_name"
                    />
                </el-select>
            </el-form-item>
            <el-form-item label="包含 Y：">
                <el-select
                    v-model="vData.search.containsY"
                    style="width:100px;"
                    filterable
                    clearable
                >
                    <el-option :key="0" value="true" label="是"></el-option>
                    <el-option :key="1" value="false" label="否"></el-option>
                </el-select>
            </el-form-item>
            <el-form-item label="已启用：">
                <el-select
                    v-model="vData.search.enable"
                    style="width:100px;"
                    filterable
                    clearable
                >
                    <el-option :key="0" value="true" label="是"></el-option>
                    <el-option :key="1" value="false" label="否"></el-option>
                </el-select>
            </el-form-item>
            <el-form-item label="已删除：">
                <el-select
                    v-model="vData.search.status"
                    style="width:100px;"
                    filterable
                    clearable
                >
                    <el-option :key="0" value="true" label="是"></el-option>
                    <el-option :key="1" value="false" label="否"></el-option>
                </el-select>
            </el-form-item>

            <el-button
                type="primary"
                native-type="submit"
                :disabled="vData.loading"
                @click="getList({ to: true, resetPagination: true })"
            >
                查询
            </el-button>
        </el-form>

        <el-table
            v-loading="vData.loading"
            :data="vData.list"
            style="min-height:500px"
            stripe
            border
        >
            <template #empty>
                <EmptyData />
            </template>
            <el-table-column label="序号" type="index" />
            <el-table-column
                label="成员"
                min-width="100"
            >
                <template v-slot="scope">
                    <span
                        class="p-name"
                        @click="checkCard(scope.row.member_id)"
                    >
                        <i class="iconfont icon-visiting-card" />
                        {{ scope.row.member_name }}
                    </span>
                    <span class="p-id">{{ scope.row.member_id }}</span>
                </template>
            </el-table-column>
            <el-table-column
                label="数据集"
                min-width="100"
            >
                <template v-slot="scope">
                    <router-link v-if="!scope.row.status" class="mb10" :to="{ name: 'data-view', query: { id: scope.row.id }}">
                        {{ scope.row.name }}
                    </router-link>
                    <p v-else>{{ scope.row.name }}</p>
                    <span class="p-id">{{ scope.row.id }}</span>
                </template>
            </el-table-column>
            <el-table-column label="关键词">
                <template v-slot="scope">
                    <template
                        v-for="(item, index) in scope.row.tags.split(',')"
                        :key="index"
                    >
                        <el-tag
                            v-show="item"
                            class="mr10"
                        >
                            {{ item }}
                        </el-tag>
                    </template>
                </template>
            </el-table-column>
            <el-table-column
                label="数据量"
                prop="row_count"
                width="140"
            >
                <template v-slot="scope">
                    特征量：{{ scope.row.feature_count }}
                    <br>
                    样本量：{{ scope.row.row_count }}
                </template>
            </el-table-column>
            <el-table-column
                label="参与项目数"
                prop="usage_count_in_project"
                width="100"
            />
            <el-table-column
                label="包含Y"
                width="100"
            >
                <template v-slot="scope">
                    <i
                        v-if="scope.row.contains_y "
                        class="el-icon-check"
                    />
                    <i
                        v-else
                        class="el-icon-close"
                    />
                </template>
            </el-table-column>
            <el-table-column
                label="上传时间"
                min-width="140"
            >
                <template v-slot="scope">
                    {{ dateFormat(scope.row.created_time) }}
                </template>
            </el-table-column>
            <el-table-column
                label="状态"
                min-width="120"
            >
                <template v-slot="scope">
                    <p v-if="scope.row.status">已删除</p>
                    <template v-else>
                        <el-button
                            v-if="scope.row.ext_json.enable"
                            type="danger"
                            @click="methods.changeStatus($event, scope.row)"
                        >禁用</el-button>
                        <el-button
                            v-else
                            @click="methods.changeStatus($event, scope.row)"
                        >启用</el-button>
                    </template>
                </template>
            </el-table-column>
            <!-- <el-table-column
                label="操作"
                min-width="120"
            >
                <template v-slot="scope">
                    <el-button
                        type="danger"
                        @click="deleteDataSet($event, scope.row)"
                    >
                        移除
                    </el-button>
                </template>
            </el-table-column> -->
        </el-table>
        <div
            v-if="pagination.total"
            class="mt20 text-r"
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
        </div>

        <el-dialog
            title="名片预览"
            v-model="vData.dialogCard"
            custom-class="card-dialog"
            destroy-on-close
            width="500px"
            top="30vh"
        >
            <MemberCard
                ref="memberCard"
                :form="vData.cardData"
            />
        </el-dialog>
    </el-card>
</template>

<script>
    import {
        ref,
        reactive,
        onMounted,
        getCurrentInstance,
        nextTick,
    } from 'vue';
    import table from '@src/mixins/table.js';

    export default {
        inject: ['refresh'],
        mixins: [table],
        setup() {
            const { ctx, appContext } = getCurrentInstance();
            const { $http, $confirm } = appContext.config.globalProperties;
            const memberCard = ref();
            const vData = reactive({
                loading: true,
                search:  {
                    id:        '',
                    name:      '',
                    member_id: '',
                    tag:       '',
                    containsY: '',
                    enable:    '',
                    status:    '',
                },
                getListApi:     '/data_set/query',
                member_list:    [],
                tag_list:       [],
                viewDataDialog: {
                    visible: false,
                    list:    [],
                },
                dialogCard:  false,
                cardData:    {}, // Business card information
                dataSetList: [], // dataset form Quick create project
            });

            const methods = {
                async loadTags() {
                    const { code, data } = await $http.get('/data_set/tags/query');

                    if (code === 0) {
                        vData.tag_list = data.tag_list;
                    }
                },

                async loadMemberList() {
                    const { code, data } = await $http.post({
                        url:  '/member/query',
                        data: {
                            page_index:  0,
                            page_size:   100,
                            id:          '',
                            name:        '',
                            freezed:     false,
                            hidden:      false,
                            lostContact: false,
                            status:      false,
                        },
                    });

                    if (code === 0) {
                        vData.member_list = data.list;
                    }
                },

                async checkCard(member_id) {
                    const res = await $http.post({
                        url:  '/member/query',
                        data: { id: member_id },
                    });

                    if(res.code === 0){
                        const resData = res.data.list[0];

                        vData.cardData.name = resData.name;
                        vData.cardData.logo = resData.logo;
                        vData.cardData.email = resData.email;
                        vData.cardData.mobile = resData.mobile;
                        vData.dialogCard = true;
                        nextTick(_ => {
                            memberCard.value.init();
                        });
                    }
                },

                changeStatus($event, row) {
                    $confirm(`你确定要${ row.ext_json.enable ? '禁用' : '启用' }该数据集吗?`, '警告', {
                        type:              'warning',
                        cancelButtonText:  '取消',
                        confirmButtonText: '确定',
                    }).then(async _ => {
                        await $http.post({
                            url:  '/data_set/update_ext_json',
                            data: {
                                id:      row.id,
                                extJson: {
                                    enable: !row.ext_json.enable,
                                },
                            },
                        });

                        ctx.refresh();
                    });
                },
            };

            onMounted(() => {
                methods.loadTags();
                methods.loadMemberList();
                ctx.getList();
            });

            return {
                vData,
                methods,
                memberCard,
                checkCard: methods.checkCard,
            };
        },
    };
</script>

<style lang="scss" scoped>
    .page{
        :deep(.card-dialog) {
            .el-dialog__body {
                overflow: hidden;
                display: flex;
                justify-content: center;
                padding: 20px 20px 40px;
            }
        }
    }
    .el-icon-folder-add{
        cursor: pointer;
        font-size: 16px;
        color: $color-link-base;
    }
    .p-name {
        color: $color-link-base;
        cursor: pointer;
        display: flex;
        align-items: center;
        i {padding-right: 5px;}
    }
    .horiz-ball{
        position: fixed;
        z-index: 11;
        transition: 0.7s all linear;
    }
    .ball-icon{
        transition: 0.7s all cubic-bezier(0.49, -0.29, 0.75, 0.41);
    }
</style>
