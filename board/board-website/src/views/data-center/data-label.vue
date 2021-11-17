<template>
    <el-card class="page_layer_label">
        <div class="check_label">
            <el-tabs v-model="vData.activeName" @tab-click="methods.tabChange">
                <div class="label_content">
                    <label-system ref="labelSystemRef" :currentImage="vData.currentImage" @save-label="methods.saveCurrentLabel" />
                    <image-thumbnail-list ref="imgThumbnailListRef" :sampleList="vData.sampleList" @select-image="methods.selectImage" />
                </div>
                <el-tab-pane v-for="item in vData.tabsList" :key="item.label" :label="item.label + ' (' + item.count + ')'" :name="item.name"></el-tab-pane>
                <div class="label_list_box">
                    <div class="label_bar">
                        <p>标签栏</p>
                        <el-button plain type="primary">添加标签</el-button>
                    </div>
                    <div class="label_search">
                        <el-input type="text" placeholder="请输入标签名称" prefix-icon="el-icon-search"></el-input>
                    </div>
                </div>
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
    import LabelSystem from './components/label-system.vue';
    import ImageThumbnailList from './components/image-thumbnail-list.vue';
    export default {
        components: {
            LabelSystem,
            ImageThumbnailList,
        },
        setup() {
            const route = useRoute();
            const { appContext } = getCurrentInstance();
            const { $http } = appContext.config.globalProperties;
            const imgListRef = ref();
            const labelSystemRef = ref();
            const imgThumbnailListRef = ref();
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
                sampleList:   [],
                imgLoading:   false,
                currentImage: '',
                timer:        null,
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
                                vData.currentImage = list[0].img_src;
                                nextTick(_=> {
                                    labelSystemRef.value.methods.createStage();
                                });
                                vData.sampleList = list;
                                vData.imgLoading = false;
                            }, 500);
                            
                        }
                    });
                },
                selectImage(item) {
                    vData.currentImage = item.img_src;
                    nextTick(_=> {
                        labelSystemRef.value.methods.createStage();
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
                debounce(){
                    if(vData.timer) clearTimeout(vData.timer);
                    vData.timer = setTimeout(() => {
                        methods.resetWidth();
                    }, 300);
                },
                resetWidth() {
                    labelSystemRef.value.vData.width = document.getElementsByClassName('label_content')[0].offsetWidth - 280;
                    imgThumbnailListRef.value.vData.width = document.getElementsByClassName('label_content')[0].offsetWidth - 280;
                    labelSystemRef.value.methods.createStage();
                },
                // 保存当前标注
                saveCurrentLabel(data) {
                    console.log(data);
                },
            };

            onBeforeMount(() => {
                methods.getSampleInfo();
                setTimeout(_=> {
                    methods.getSampleList();
                    methods.resetWidth();
                }, 200);
                window.onresize = () => {
                    methods.debounce();
                };
            });

            return {
                vData,
                methods,
                imgListRef,
                labelSystemRef,
                imgThumbnailListRef,
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
.page_layer_label {
    height: calc(100vh - 120px);
    .check_label {
        position: relative;
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
                width: 280px;
                border-left: 1px solid #eee;
                position: absolute;
                right: 0;
                top: 0;
                bottom: 0;
                background: #fff;
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
            .label_content {
                flex: 1;
                overflow-y: auto;
            }
        }
        
    }
}
</style>
<style lang="scss" scoped>
.page_layer_label {
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
