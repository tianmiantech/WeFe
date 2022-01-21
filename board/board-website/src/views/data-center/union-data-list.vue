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
            <el-form-item label="数据资源 ID：">
                <el-input
                    v-model="vData.search.data_resource_id"
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
            <el-form-item
                label="资源类型："
                label-width="100"
            >
                <el-select
                    v-model="vData.search.dataResourceType"
                    filterable
                    clearable
                    multiple
                    @change="resourceTypeChange"
                >
                    <el-option
                        v-for="item in vData.sourceTypeList"
                        :key="item.label"
                        :value="item.value"
                        :label="item.label"
                    />
                </el-select>
            </el-form-item>
            <el-form-item
                v-if="vData.search.dataResourceType.length === 1 && vData.search.dataResourceType[0] === 'TableDataSet'"
                label="是否包含Y值："
                label-width="100"
            >
                <el-select
                    v-model="vData.search.containsY"
                    filterable
                    clearable
                >
                    <el-option label="是" :value="true"></el-option>
                    <el-option label="否" :value="false"></el-option>
                </el-select>
            </el-form-item>
            <el-form-item
                v-if="vData.search.dataResourceType.length === 1 && vData.search.dataResourceType[0] === 'ImageDataSet'"
                label-width="100"
                label="任务类型："
            >
                <el-select
                    v-model="vData.search.forJobType"
                    filterable
                    clearable
                >
                    <el-option
                        v-for="item in vData.forJobTypeList"
                        :key="item.value"
                        :label="item.label"
                        :value="item.value"
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

        <UnionDataResourceList
            ref="UnionDataResourceListRef"
            key="UnionDataResourceListRef"
            :table-loading="vData.loading"
            :search-field="vData.search"
            @search-update="methods.searchUpdate"
            @add-data-set="addDataSet"
            @check-card="checkCard"
        />
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
    import speedCart from './components/speed-cart';
    import UnionDataResourceList from './components/union-data-resource-list.vue';

    export default {
        components: {
            speedCart,
            UnionDataResourceList,
        },
        setup() {
            const { appContext } = getCurrentInstance();
            const { $http } = appContext.config.globalProperties;
            const memberCard = ref();
            const speedCart = ref();
            const UnionDataResourceListRef = ref();
            const vData = reactive({
                loading: true,
                search:  {
                    data_resource_id: '',
                    name:             '',
                    member_id:        '',
                    tag:              '',
                    dataResourceType: '',
                    containsY:        '',
                    forJobType:       '',
                },
                getListApi:     '/union/data_resource/query',
                member_list:    [],
                tag_list:       [],
                viewDataDialog: {
                    visible: false,
                    list:    [],
                },
                dialogCard:     false,
                cardData:       {}, // Business card information
                dataSetList:    [], // dataset form Quick create project
                balls:          [],
                sourceTypeList: [
                    {
                        label: 'TableDataSet',
                        value: 'TableDataSet',
                    },
                    {
                        label: 'ImageDataSet',
                        value: 'ImageDataSet',
                    },
                    {
                        label: '布隆过滤器',
                        value: 'BloomFilter',
                    },
                ],
                forJobTypeList: [
                    {
                        label: '目标检测',
                        value: 'detection',
                    },
                    {
                        label: '图像分类',
                        value: 'classify',
                    },
                ],
            });
            const methods = {
                searchUpdate(search) {
                    Object.assign(vData.search, {
                        ...search,
                        dataResourceType: search.dataResourceType ? Array.isArray(search.dataResourceType) ? search.dataResourceType : [search.dataResourceType] : '',
                    });
                },
                async loadTags() {
                    const { code, data } = await $http.post({
                        url:  '/union/data_resource/tags/query',
                        data: {
                            dataResourceType: '',
                        },
                    });

                    nextTick(_=> {
                        if (code === 0) {
                            vData.tag_list = data;
                        }
                    });
                },

                async loadMemberList(keyward) {
                    const { code, data } = await $http.post({
                        url:  '/union/member/query',
                        data: {
                            page_size: 100,
                            name:      keyward,
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
                            id: member_id,
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
                    const id = row.data_resource_id ? row.data_resource_id : row.id;

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
            const searchList = (opt = {}) => {
                UnionDataResourceListRef.value.search = vData.search;
                UnionDataResourceListRef.value.getDataList({ to: true });
            };
            const resourceTypeChange = () => {
                vData.search.containsY = '';
                vData.search.forJobType = '';
            };

            onMounted(async () => {
                await methods.loadTags();
                await methods.loadMemberList();
                UnionDataResourceListRef.value.getDataList();
            });

            return {
                vData,
                methods,
                speedCart,
                memberCard,
                addDataSet: methods.addDataSet,
                checkCard:  methods.checkCard,
                ballBeforeEnter,
                ballEnter,
                ballAfterEnter,
                UnionDataResourceListRef,
                resourceTypeChange,
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
