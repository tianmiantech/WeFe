<template>
    <div
        ref="card-count"
        class="card-count el-card"
        @click="changeDrawer"
    >
        <el-icon>
            <elicon-files />
        </el-icon>
        <span v-if="count" class="num">{{ count }}</span>
    </div>

    <el-drawer
        v-model="drawer"
        modal-class="speed-card"
        direction="rtl"
        destroy-on-close
        size="100%"
    >
        <template #title>
            <h4>
                <strong>快捷创建项目</strong>
                <el-tooltip
                    class="item"
                    effect="light"
                    placement="bottom"
                >
                    <template #content>
                        <p>1, 添加数据集到此处可快速创建项目</p>
                        <p>2, 多个发起方请前往项目详情添加</p>
                    </template>

                    <el-icon class="el-icon-opportunity">
                        <elicon-opportunity />
                    </el-icon>
                </el-tooltip>
            </h4>
        </template>

        <el-form class="flex-form">
            <el-form-item label="项目名称:" label-width="74px">
                <el-input v-model.trim="vData.name" clearable />
            </el-form-item>
            <el-form-item label="项目简介:" label-width="74px">
                <el-input v-model.trim="vData.desc" clearable />
            </el-form-item>
        </el-form>

        <div class="member-wrapper">
            <div class="member-list f14">
                <div
                    v-if="promoterDataSetList.length"
                    class="member-info mb10"
                >
                    <p>发起方: {{ promoterDataSetList[0].member_name }}</p>
                    <p class="p-id f12">{{ promoterDataSetList[0].member_id }}</p>
                </div>
                <ul
                    v-if="promoterDataSetList.length"
                    class="data-sets mb20"
                >
                    <li
                        v-for="(dataset, index) in promoterDataSetList"
                        :key="dataset.id"
                    >
                        <el-form>
                            <el-form-item>
                                <router-link :to="{ name: 'data-view', query: { id: dataset.id }}" class="data-link">
                                    {{ dataset.name }}
                                </router-link>
                            </el-form-item>
                            <el-form-item>
                                <p class="p-id f12">{{ dataset.id }}</p>
                            </el-form-item>
                            <el-form-item>
                                <span class="f12 mr10">数据集类型:</span>
                                <span class="p-id f12 under-line">{{ dataset.data_resource_type }}</span>
                            </el-form-item>
                            <el-form-item>
                                <span class="f12 mr10">是否含 Y:</span>
                                <el-icon v-if="dataset.contains_y" class="el-icon-check">
                                    <elicon-check />
                                </el-icon>
                                <el-icon v-else class="el-icon-close">
                                    <elicon-close />
                                </el-icon>
                            </el-form-item>
                            <el-icon class="el-icon-circle-close" @click="removeDataSet(index, dataset)">
                                <elicon-circle-close />
                            </el-icon>
                        </el-form>
                    </li>
                </ul>

                <div
                    v-for="member in providerList"
                    :key="member.member_id"
                    class="member-info"
                >
                    <p>协作方: {{ member.member_name }}</p>
                    <p class="p-id f12">{{ member.member_id }}</p>

                    <ul class="data-sets mt20">
                        <li
                            v-for="(dataset, index) in providerListMap[member.member_id]"
                            :key="dataset.id"
                        >
                            <el-form>
                                <el-form-item>
                                    <router-link :to="{ name: 'union-data-view', query: { id: dataset.id }}" class="data-link">
                                        {{ dataset.name }}
                                    </router-link>
                                </el-form-item>
                                <el-form-item>
                                    <p class="p-id f12">{{ dataset.id }}</p>
                                </el-form-item>
                                <el-form-item>
                                    <span class="f12 mr10">数据集类型:</span>
                                    <span class="p-id f12">{{ dataset.data_resource_type }}</span>
                                </el-form-item>
                                <el-form-item>
                                    <span class="f12 mr10">是否含 Y:</span>
                                    <el-icon v-if="dataset.contains_y" class="el-icon-check">
                                        <elicon-check />
                                    </el-icon>
                                    <el-icon v-else class="el-icon-close">
                                        <elicon-close />
                                    </el-icon>
                                </el-form-item>
                                <el-icon class="el-icon-circle-close" @click="removeDataSet(index, dataset)">
                                    <elicon-circle-close />
                                </el-icon>
                            </el-form>
                        </li>
                    </ul>
                </div>
            </div>
            <el-button
                type="primary"
                v-loading="loading"
                :disabled="promoterDataSetList.length === 0 &&  providerList.length === 0"
                @click="create"
            >
                创建
                <el-icon>
                    <elicon-right />
                </el-icon>
            </el-button>
        </div>
    </el-drawer>
</template>

<script>
    import {
        ref,
        reactive,
        getCurrentInstance,
        computed,
        nextTick,
    } from 'vue';
    import { useRouter } from 'vue-router';
    import { useStore } from 'vuex';

    export default {
        props: {
            list: Array,
        },
        setup(props) {
            let loading = ref(false);
            const router = useRouter();
            const store = useStore();
            const { appContext } = getCurrentInstance();
            const { $http, $message } = appContext.config.globalProperties;
            const userInfo = computed(() => store.state.base.userInfo);
            const promoterDataSetList = ref([]);
            const providerList = ref([]);
            const providerListMap = ref({});
            const cartList = ref(props.list);
            const allDataSetList = ref([]);
            const projectType = ref();
            const drawer = ref(false);
            const count = ref(0);
            const methods = {
                dateFormat(split) {
                    const now = new Date();
                    const year = now.getFullYear();
                    const month = (now.getMonth()+1).toString().padStart(2,'0');
                    const day = now.getDate().toString().padStart(2,'0');
                    const hours = now.getHours().toString().padStart(2,'0');
                    const minutes = now.getMinutes().toString().padStart(2,'0');
                    const seconds = now.getSeconds().toString().padStart(2,'0');

                    if(split) {
                        return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`;
                    } else {
                        return `${year}${month}${day}${hours}${minutes}${seconds}`;
                    }
                },
            };
            const vData = reactive({
                name: '',
                desc: '',
            });
            const initDesc = () => {
                const timestamp = methods.dateFormat();
                const time = methods.dateFormat(true);

                vData.name = `快捷项目-${timestamp}`;
                vData.desc = `创建自快捷项目, 创建时间: ${time}`;
            };
            const changeDrawer = () => {
                drawer.value = !drawer.value;

                if(!vData.name) {
                    initDesc();
                }
            };

            const addDataSet = (item) => {
                const dataset = {
                    member_role: item.member_id === userInfo.value.member_id ? 'promoter' : 'provider',
                    data_set_id: item.id ? item.id : item.data_resource_id,
                    id:          item.id ? item.id : item.data_resource_id,
                    ...item,
                };

                if(item.member_id === userInfo.value.member_id) {
                    const index = promoterDataSetList.value.findIndex(x => x.id === item.id || x.id === item.data_resource_id);

                    if(index < 0) {
                        promoterDataSetList.value.push(dataset);
                        allDataSetList.value.push(dataset);
                        // update count
                        count.value++;
                    }
                } else {
                    if(!providerListMap.value[item.member_id]) {
                        providerListMap.value[item.member_id] = ref([]);
                    }
                    const index = providerListMap.value[item.member_id].findIndex(x => x.id === item.id);

                    if(index < 0) {
                        providerListMap.value[item.member_id].push(dataset);
                        allDataSetList.value.push(dataset);
                        // update count
                        count.value++;
                    }

                    const member = providerList.value.find(member => member.member_id === item.member_id);

                    if(!member) {
                        providerList.value.push(item);
                    }
                }
            };
            const removeDataSet = (index, item) => {
                if(item.member_id === userInfo.value.member_id) {
                    promoterDataSetList.value.splice(index, 1);
                } else {
                    if(providerListMap.value[item.member_id]) {
                        providerListMap.value[item.member_id].splice(index, 1);
                        if(providerListMap.value[item.member_id].length === 0) {
                            const idx = providerList.value.findIndex(member => member.member_id === item.member_id);

                            providerList.value.splice(idx, 1);
                        }
                    }
                }
                count.value--;
                allDataSetList.value.forEach((i, idx) => {
                    if (i.data_set_id === item.data_set_id) {
                        allDataSetList.value.splice(idx, 1);
                    }
                });
            };
            const create = async () => {
                if(loading.value) return;
                loading = true;

                // 判断是否为同一格式的数据集 ImageDataSet / TableDataSet
                if (!isAllEqual(allDataSetList.value)) {
                    $message.error('项目中的数据集必须为同一类型的数据');
                    return;
                } else {
                    projectType.value = allDataSetList.value[0].data_resource_type === 'ImageDataSet' ? 'DeepLearning' : allDataSetList.value[0].data_resource_type === 'TableDataSet' ? 'MachineLearning' : '';
                }

                const list = providerList.value.map(item => {
                    const provider = {
                        member_id:   item.member_id,
                        dataSetList: [],
                    };

                    providerListMap.value[item.member_id].forEach(dataset => {
                        provider.dataSetList.push(dataset);
                    });

                    return provider;
                });


                const timestamp = methods.dateFormat();
                const time = methods.dateFormat(true);
                const { code, data } = await $http.post({
                    url:  '/project/add',
                    data: {
                        name:                `快捷项目-${timestamp}`,
                        desc:                `创建自快捷项目, 创建时间: ${time}`,
                        projectType:         projectType.value,
                        promoterDataSetList: promoterDataSetList.value,
                        providerList:        list,
                    },
                });

                nextTick(_ => {
                    loading = false;
                    if(code === 0) {
                        router.push({
                            name:  'project-detail',
                            query: {
                                project_id: data.project_id,
                            },
                        });
                    }
                });
            };

            const isAllEqual = (arr) => {
                if (arr.length > 0) {
                    return !arr.some(function(value, index) {
                        return value.data_resource_type !== arr[0].data_resource_type;
                    });
                } else {
                    return true;
                }
            };

            return {
                vData,
                cartList,
                drawer,
                create,
                changeDrawer,
                promoterDataSetList,
                providerList,
                providerListMap,
                addDataSet,
                removeDataSet,
                loading,
                count,
                isAllEqual,
                projectType,
            };
        },
    };
</script>

<style lang="scss">
    .el-overlay.speed-card{
        left:auto;
        width: 331px;
        background: none;
        border-left: 1px solid #f0f0f0;
        .el-drawer__header{margin-bottom:0;}
        .el-drawer__body{height:calc(100% - 44px);}
    }
</style>

<style lang="scss" scoped>
    .card-count{
        position: fixed;
        right: 20px;
        bottom: 70px;
        text-align: center;
        border-radius: 50%;
        cursor: pointer;
        z-index: 10;
        width: 44px;
        height: 44px;
        padding-top: 10px;
        overflow: visible;
        &:hover{background: #f0f0f0;}
        .num{
            font-size: 12px;
            position: absolute;
            top: 3px;
            right: 4px;
            height: 14px;
            padding:0 3px;
            line-height: 14px;
            border-radius: 10px;
            background: $--color-danger;
            color:#fff;
        }
    }
    .el-icon-opportunity{color:$--color-warning;}
    .member-wrapper{
        height:calc(100vh - 160px);
        padding: 0 0 70px;
        position: relative;
        .el-button{
            position: absolute;
            right: 20px;
            bottom: 20px;
            width: 100px;
        }
    }
    .member-list{
        height: 100%;
        overflow-y: auto;
    }
    .member-info{
        margin-top: 20px;
        &:first-child{margin-top:0;}
    }
    .data-sets{
        color:#666;
        padding:10px;
        border-radius: 4px;
        background: $background-color-hover;
        li{position: relative;
            margin-top: 15px;
            padding-right: 30px;
            &:first-child{margin-top: 0;}
        }
        .el-form-item{margin:0;}
        :deep(.el-form-item__label){font-size: 12px;}
        :deep(.el-form-item__label),
        :deep(.el-form-item__content){line-height:16px;}
        .el-icon-circle-close{
            position: absolute;
            right: 10px;
            top: 50%;
            margin-top: -10px;
            font-size: 18px;
            color:$--color-danger;
            cursor: pointer;
        }
        .under-line {
            text-decoration: underline;
        }
    }
    .data-link{word-break: break-all;}
    .el-icon-check,
    .el-icon-close{vertical-align: middle;}
    .el-icon-check{color:$--color-success;}
    .el-icon-close{color:$--color-danger;}
</style>
