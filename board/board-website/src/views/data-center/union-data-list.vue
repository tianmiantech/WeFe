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
                    v-model="vData.search.data_set_id"
                    clearable
                />
            </el-form-item>
            <el-form-item label="名称：">
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

            <el-button
                type="primary"
                native-type="submit"
                @click="searchList({ to: true, resetPagination: true })"
            >
                查询
            </el-button>
        </el-form>

        <el-tabs
            v-model="vData.activeTab"
            type="border-card"
            @tab-click="tabChange"
        >
            <template
                v-for="tab in vData.unionTabs"
                :key="tab.name"
            >
                <el-tab-pane
                    v-if="tab.name === 'imageUnions'"
                    :name="tab.name"
                    :label="tab.label"
                >
                    <template #label>
                        {{ tab.label }}
                    </template>
                    <UnionImagesList
                        ref="imageUnionsRef"
                        key="imageUnions"
                        :table-loading="vData.loading"
                        :search-field="vData.search"
                        @add-data-set="addDataSet"
                        @check-card="checkCard"
                    />
                </el-tab-pane>
                <el-tab-pane
                    v-else
                    :name="tab.name"
                    :label="tab.label"
                >
                    <template #label>
                        <el-badge v-if="tab.label">
                            {{ tab.label }}
                        </el-badge>
                    </template>
                    <el-table
                        v-loading="vData.loading"
                        :data="vData.list"
                        stripe
                        border
                    >
                        <el-table-column label="添加" width="60" v-slot="scope">
                            <i
                                title="快捷创建项目"
                                class="el-icon-folder-add"
                                @click="addDataSet($event, scope.row)"
                            ></i>
                        </el-table-column>
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
                                <router-link :to="{ name: 'union-data-view', query: { id: scope.row.id }}">
                                    {{ scope.row.name }}
                                </router-link>
                                <br>
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
                                <el-icon v-if="scope.row.contains_y" class="el-icon-check">
                                    <elicon-check />
                                </el-icon>
                                <el-icon v-else class="el-icon-close">
                                    <elicon-close />
                                </el-icon>
                            </template>
                        </el-table-column>
                        <el-table-column
                            label="上传时间"
                            min-width="120"
                        >
                            <template v-slot="scope">
                                {{ dateFormat(scope.row.created_time) }}
                            </template>
                        </el-table-column>
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
                </el-tab-pane>
            </template>
        </el-tabs>

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

        <template v-for="(ball, index) in vData.balls" :key="index">
            <transition
                @before-enter="ballBeforeEnter"
                @enter="ballEnter"
                @after-enter="ballAfterEnter"
            >
                <i
                    v-if="ball.show"
                    :id="`ball-${ball.id}`"
                    class="horiz-ball"
                    :style="{
                        top: `${ball.y}px`,
                        left: `${ball.x}px`,
                    }"
                >
                    <el-icon class="ball-icon">
                        <elicon-folder-add />
                    </el-icon>
                </i>
            </transition>
        </template>

        <speedCart
            ref="speedCart"
            :list="vData.dataSetList"
        />
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
    import { useRouter } from 'vue-router';
    import table from '@src/mixins/table.js';
    import speedCart from './components/speed-cart';
    import UnionImagesList from './components/union-images-list';

    export default {
        mixins:     [table],
        components: {
            speedCart,
            UnionImagesList,
        },
        setup() {
            const { ctx, appContext } = getCurrentInstance();
            const { $http } = appContext.config.globalProperties;
            const memberCard = ref();
            const speedCart = ref();
            const router = useRouter();
            const imageUnionsRef = ref();
            const vData = reactive({
                loading: true,
                search:  {
                    data_set_id: '',
                    name:        '',
                    member_id:   '',
                    tag:         '',
                },
                getListApi:     '/union/data_set/query',
                member_list:    [],
                tag_list:       [],
                viewDataDialog: {
                    visible: false,
                    list:    [],
                },
                dialogCard:  false,
                cardData:    {}, // Business card information
                dataSetList: [], // dataset form Quick create project
                balls:       [],
                activeTab:   'allUnions',
                unionTabs:   [
                    {
                        name:  'allUnions',
                        label: '结构化数据',
                        count: 0,
                    },
                    {
                        name:  'imageUnions',
                        label: '图像数据',
                        count: 0,
                    },
                ],
            });
            const methods = {
                async loadTags() {
                    const { code, data } = await $http.get('/union/data_set/tag/query');

                    if (code === 0) {
                        vData.tag_list = data.tag_list;
                    }
                },

                async loadMemberList(keyward) {
                    const { code, data } = await $http.post({
                        url:  '/union/member/query',
                        data: {
                            page_size:          100,
                            name:               keyward,
                            requestFromRefresh: true,
                        },
                    });

                    if (code === 0) {
                        vData.member_list = data.list;
                    }
                },
                async checkCard(member_id) {
                    const res = await $http.post({
                        url:  '/union/member/query',
                        data: {
                            id:                 member_id,
                            requestFromRefresh: true,
                        },
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
                // add dataset to cart
                addDataSet(ev, row) {
                    const id = row.id ? row.id : row.data_set_id;

                    vData.balls.push({
                        id,
                        show: false,
                        x:    ev.x,
                        y:    ev.y,
                        item: row,
                    });

                    nextTick(() => {
                        const ball = vData.balls.find(ball => ball.id === id);

                        if(ball) ball.show = true;
                    });
                },
            };
            const ballBeforeEnter = (el) => {
                el.style.webkitTransform = 'translate3d(0,0,0)';
                el.style.transform = 'translate3d(0,0,0)';

                const child = el.children[0];

                child.style.transform = 'translate3d(0, 0, 0)';
                child.style.webkitTransform = 'translate3d(0, 0, 0)';
            };
            const ballEnter = (el, done) => {
                const rect = speedCart.value.$refs['card-count'].getBoundingClientRect();
                const elRect = el.getBoundingClientRect();
                const x = rect.left - elRect.left;
                const y = rect.top - elRect.top;

                el.timer = setTimeout(() => {
                    el.style.webkitTransform = `translate3d(${x}px,0,0)`;
                    el.style.transform = `translate3d(${x}px,0,0)`;

                    const child = el.children[0];

                    child.style.transform = `translate3d(0, ${y}px, 0)`;
                    child.style.webkitTransform = `translate3d(0, ${y}px, 0)`;

                    el.addEventListener('transitionEnd', done);
                    el.addEventListener('webkitTransitionEnd', done);
                });
            };
            const ballAfterEnter = (el) => {
                const ballIndex = vData.balls.findIndex(ball => `ball-${ball.id}` === el.id);

                if(~ballIndex) {
                    clearTimeout(el.timer);

                    const item = vData.balls.splice(ballIndex, 1);

                    speedCart.value.addDataSet(item[0].item);
                }
            };
            const tabChange = (refInstance) => {
                vData.search.data_set_id = '';
                router.push({
                    query: {
                        ...vData.search,
                        page_index:  1,
                        source_type: refInstance.paneName,
                    },
                });
                if (refInstance.paneName === 'allUnions') {
                    vData.getListApi = '/union/data_set/query';
                    ctx.getList();
                } else {
                    imageUnionsRef.value.search = vData.search;
                    vData.getListApi = imageUnionsRef.value.vData.getListApi;
                    imageUnionsRef.value.methods.getDataList();
                }
            };
            const searchList = (opt = {}) => {
                if (vData.activeTab === 'imageUnions') {
                    imageUnionsRef.value.search = vData.search;
                    imageUnionsRef.value.methods.getDataList();
                } else {
                    ctx.getList();
                }
            };

            onMounted(async () => {
                await methods.loadTags();
                await methods.loadMemberList();
                ctx.getList();
            });

            return {
                vData,
                speedCart,
                memberCard,
                addDataSet: methods.addDataSet,
                checkCard:  methods.checkCard,
                ballBeforeEnter,
                ballEnter,
                ballAfterEnter,
                tabChange,
                imageUnionsRef,
                searchList,
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
    .ball-icon{transition: 0.7s all cubic-bezier(0.49, -0.29, 0.75, 0.41);}
</style>
