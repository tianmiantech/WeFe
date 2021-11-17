<template>
    <el-card class="page_layer">
        <div class="check_label">
            <div class="tabs_nav_btns">
                <router-link :to="{ name: 'data-label', query: { id: vData.sampleId }}">
                    <el-button type="primary">标注图片</el-button>
                </router-link>
            </div>
            <el-tabs v-model="vData.activeName" @tab-click="methods.tabChange">
                <div class="label_list_box">
                    <div class="label_bar">
                        <p>标签栏</p>
                    </div>
                    <div class="label_search">
                        <el-input type="text" placeholder="请输入标签名称" prefix-icon="el-icon-search"></el-input>
                    </div>
                </div>
                <el-tab-pane v-for="item in vData.tabsList" :key="item.label" :label="item.label + ' (' + item.count + ')'" :name="item.name">
                    <div class="loading_layer" :style="{display: vData.imgLoading ? 'block' : 'none'}"><i class="el-icon-loading"></i></div>
                    <check-image-list ref="imgListRef" v-if="vData.sampleList.length" :sampleList="vData.sampleList" />
                    <template v-else>
                        <EmptyData />
                    </template>
                </el-tab-pane>
            </el-tabs>
            <div
                v-if="vData.search.total"
                class="mt20 text-r"
            >
                <el-pagination
                    :total="vData.search.total"
                    :page-sizes="[10, 20, 30, 40, 50]"
                    :page-size="vData.search.page_size"
                    :current-page="vData.search.page_index"
                    layout="total, sizes, prev, pager, next, jumper"
                    @current-change="methods.currentPageChange"
                    @size-change="methods.pageSizeChange"
                />
            </div>
        </div>
    </el-card>
</template>

<script>
    import { ref, reactive, onBeforeMount, getCurrentInstance, nextTick } from 'vue';
    import { useRoute } from 'vue-router';
    import CheckImageList from './components/check-image-list.vue';
    export default {
        components: {
            CheckImageList,
        },
        setup() {
            const route = useRoute();
            // const router = useRouter();
            const { appContext } = getCurrentInstance();
            const { $http } = appContext.config.globalProperties;
            const imgListRef = ref();
            const vData = reactive({
                activeName: '',
                sampleId:   route.query.id,
                search:     {
                    page_index: 1,
                    page_size:  20,
                    label:      '',
                    labeled:    '',
                    total:      1,
                },
                tabsList: [
                    {
                        label: '全部',
                        name:  '',
                        count: '',
                    },
                    {
                        label: '有标注信息',
                        name:  'labeled',
                        count: '',
                    },
                    {
                        label: '无标注信息',
                        name:  'unlabeled',
                        count: '',
                    },
                ],
                sampleList: [],
                imgLoading: false,
            });

            const methods = {
                async getSampleInfo() {
                    const { code, data } = await $http.get({
                        url:    '/image_data_set/detail',
                        params: { id: vData.sampleId },
                    });

                    nextTick(_ => {
                        if(code === 0) {
                            vData.tabsList[0].count = data.sample_count;
                            vData.tabsList[1].count = data.labeled_count;
                            vData.tabsList[2].count = data.sample_count - data.labeled_count;
                        }
                    });
                },
                async getSampleList() {
                    vData.imgLoading = true;
                    const { code, data } = await $http.post({
                        url:    '/image_data_set_sample/query',
                        params: Object.assign(vData.search, { data_set_id: vData.sampleId }),
                    });
                    
                    nextTick(_ => {
                        if(code === 0) {
                            if (data && data.list.length>0) {
                                vData.search.total = data.total;
                                data.list.forEach((item, idx) => {
                                    methods.downloadImage(item.id, idx, data.list);
                                });
                            } else {
                                vData.search.total = data.total;
                                vData.sampleList = data.list;
                                vData.imgLoading = false;
                            }
                        }
                    });
                },
                async downloadImage(id, idx, list) {
                    const { code, data } = await $http.get({
                        url:          '/image_data_set_sample/download',
                        params:       { id },
                        responseType: 'blob',
                    });

                    nextTick(_ => {
                        if(code === 0) {
                            const url = window.URL.createObjectURL(data);

                            if (id === list[idx].id) {
                                list[idx].img_src = url;
                            }
                            setTimeout(_=> {
                                vData.sampleList = list;
                                vData.imgLoading = false;
                            }, 500);
                            
                        }
                    });
                },
                currentPageChange (val) {
                    vData.search.page_index = val;
                    methods.getSampleList();
                },

                pageSizeChange (val) {
                    vData.search.page_size = val;
                    methods.getSampleList();
                },
                tabChange(val) {
                    const label_type = val.props.name === 'labeled' ? true : val.props.name === 'unlabeled' ? false : '';

                    vData.search.labeled = label_type;
                    methods.getSampleList();
                },
            };

            onBeforeMount(() => {
                methods.getSampleInfo();
                setTimeout(_=> {
                    methods.getSampleList();
                }, 200);
            });

            return {
                vData,
                methods,
                imgListRef,
            };
        },
    };
</script>

<style lang="scss">
@mixin flex_box {
    display: flex;
    align-items: center;
    justify-content: space-between;
}
.page_layer {
    height: calc(100vh - 120px);
    .check_label {
        position: relative;
        .tabs_nav_btns {
            position: absolute;
            right: 20px;
            z-index: 2;
        }
        .el-tab-pane {
            position: relative;
        }
        .loading_layer {
            width: 100%;
            height: 100%;
            background: rgba(255, 255, 255, .85);
            position: absolute;
            z-index: 3;
            i {
                display: block;
                font-size: 28px;
                color: #438bff;
                position: absolute;
                left: 50%;
                top: 50%;
                transform: translate(-50%);
                z-index: 5;
            }
        }
        .el-tabs__nav {
            .el-tabs__item {
                font-size: 16px;
            }
        }
        .el-tabs__content {
            display: flex;
            border: 1px solid #eee;
            height: calc(100vh - 270px);
            .label_list_box {
                width: 320px;
                border-right: 1px solid #eee;
                .label_bar {
                    height: 60px;
                    @include flex_box;
                    padding: 0 20px;
                    border-bottom: 1px solid #eee;
                }
                .label_search {
                    height: 80px;
                    @include flex_box;
                    justify-content: center;
                    border-bottom: 1px solid #eee;
                }
            }
            .el-tab-pane {
                flex: 1;
            }
        }
    }
}

</style>
<style lang="scss" scoped>
.page_layer {
    .label_search {
        .el-input {
            width: 90%;
            height: 40px;
            :deep(input.el-input__inner) {
                height: 40px;
            }
            :deep(.el-input__prefix) {
                top: 5px;
            }
        }
    }
}
</style>
