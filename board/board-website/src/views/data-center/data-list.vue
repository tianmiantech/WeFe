<template>
    <el-card
        class="page"
        shadow="never"
    >
        <el-form
            inline
            class="clearfix"
            @submit.prevent
        >
            <el-form-item
                label="ID："
                label-width="80"
            >
                <el-input
                    v-model="vData.search.id"
                    clearable
                />
            </el-form-item>
            <el-form-item
                label="名称："
                label-width="80"
            >
                <el-input
                    v-model="vData.search.name"
                    clearable
                />
            </el-form-item>
            <el-form-item
                label="上传者："
                label-width="120"
            >
                <el-select
                    v-model="vData.search.creator"
                    filterable
                    clearable
                >
                    <el-option
                        v-for="(user, index) in vData.userList"
                        :key="index"
                        :label="user.nickname"
                        :value="user.id"
                    />
                </el-select>
            </el-form-item>
            <el-form-item
                label="关键词："
                label-width="100"
            >
                <el-select
                    v-model="vData.search.tag"
                    filterable
                    clearable
                >
                    <el-option
                        v-for="(tag, index) in vData.tagList"
                        :key="index"
                        :value="tag.tag_name"
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
                v-if="vData.search.dataResourceType === 'TableDataSet'"
                label="是否包含Y值："
                label-width="100"
            >
                <el-select
                    v-model="vData.search.containsY"
                    style="width:90px;"
                    filterable
                    clearable
                >
                    <el-option label="是" :value="true"></el-option>
                    <el-option label="否" :value="false"></el-option>
                </el-select>
            </el-form-item>
            <el-form-item
                v-if="vData.search.dataResourceType === 'ImageDataSet'"
                label="样本分类："
                label-width="100"
            >
                <el-select
                    v-model="vData.search.forJobType"
                    style="width:120px;"
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
            <el-button native-type="submit" class="mb20 fr" @click="checkUploadingData">
                上传中的数据资源 <i class="el-icon-right"></i>
            </el-button>
        </el-form>

        <DataResourceList
            ref="DataResourceListRef"
            key="DataResourceListRef"
            :table-loading="vData.loading"
            :search-field="vData.search"
        />

        <el-dialog
            title="上传中的数据资源"
            v-model="vData.showUploadingDialog"
            custom-class="dialog-min-width"
            :close-on-click-modal="false"
            destroy-on-close
            width="70%"
        >
            <UploadingList
                ref="uploadingRef"
                key="uploadingRef"
                :table-loading="vData.loading"
            />
        </el-dialog>
    </el-card>
</template>

<script>
    import {
        ref,
        watch,
        reactive,
        onMounted,
        onBeforeUnmount,
        getCurrentInstance,
        nextTick,
    } from 'vue';
    import { useRoute } from 'vue-router';
    import UploadingList from './components/uploading-list';
    import DataResourceList from './components/data-resource-list.vue';

    export default {
        components: {
            UploadingList,
            DataResourceList,
        },
        setup() {
            const timer = null;
            const route = useRoute();
            const { appContext } = getCurrentInstance();
            const { $http } = appContext.config.globalProperties;
            const uploadingRef = ref();
            const DataResourceListRef = ref();
            const vData = reactive({
                loading: false,
                search:  {
                    id:               '',
                    name:             '',
                    creator:          '',
                    tag:              '',
                    dataResourceType: '',
                    containsY:        '',
                    forJobType:       '',
                },
                userList:       [],
                tagList:        [],
                viewDataDialog: {
                    visible: false,
                    list:    [],
                },
                unionTabs: [
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
                showUploadingDialog: false,
                sourceTypeList:      [
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
                async getTags() {
                    const { code, data } = await $http.post({
                        url:  '/union/data_resource/tags/query',
                        data: {
                            dataResourceType: vData.search.dataResourceType,
                        },
                    });

                    nextTick(_=> {
                        if (code === 0) {
                            vData.tagList = data;
                        }
                    });
                },

                // uploader account list
                async getUploaders() {
                    const { code, data } = await $http.get('/account/query');

                    if (code === 0) {
                        vData.userList = data.list;
                    }
                },
            };
            const syncUrlParams = () => {
                vData.search = {
                    id:               '',
                    name:             '',
                    creator:          '',
                    tag:              '',
                    dataResourceType: '',
                    ...route.query,
                };
            };
            const searchList = (opt = {}) => {
                DataResourceListRef.value.getDataList(opt);
            };
            const checkUploadingData = () => {
                vData.showUploadingDialog = true;
                nextTick(_=>{
                    uploadingRef.value.getDataList();
                });
            };
            const resourceTypeChange = () => {
                vData.search.containsY = '';
                vData.search.forJobType = '';
            };

            onMounted(async () => {
                syncUrlParams();
                await methods.getTags();
                await methods.getUploaders();
                searchList();
            });

            onBeforeUnmount(() => {
                clearTimeout(timer);
            });

            watch(
                () => route.query,
                (newVal) => {
                    syncUrlParams();
                    searchList();
                },
                { deep: true },
            );

            return {
                vData,
                searchList,
                syncUrlParams,
                checkUploadingData,
                uploadingRef,
                DataResourceListRef,
                resourceTypeChange,
            };
        },
    };
</script>

<style lang="scss" scoped>
    // 清除浮动
    .clearfix:after, .clearfix:before {
        content: '';
        display: table;
    }
    .clearfix:after {
        clear: both;
    }
    .clearfix {
        *zoom: 1;
        .fr {
            float: right;
        }
    }
    .el-tabs{
        :deep(.el-tabs__header) {height: 40px;}
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
    }
</style>
