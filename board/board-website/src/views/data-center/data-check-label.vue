<template>
    <el-card class="page_layer">
        <div class="check_label">
            <el-tabs v-model="vData.activeName" @tab-click="methods.tabChange">
                <div class="label_list_box">
                    <div class="label_bar">
                        <p>标签栏</p>
                        <el-button plain type="primary">添加标签</el-button>
                    </div>
                    <div class="label_search">
                        <el-input type="text" placeholder="请输入标签名称" prefix-icon="el-icon-search"></el-input>
                    </div>
                </div>
                <el-tab-pane v-for="item in vData.tabsList" :key="item.label" :label="item.label + ' ( ' + item.count + ' )'" :value="item.value">
                    <check-image-list v-if="vData.sampleList" :sampleList="vData.sampleList" />
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
    import { reactive, onBeforeMount, getCurrentInstance, nextTick } from 'vue';
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
                        value: '',
                        label: '全部',
                        count: '',
                    },
                    {
                        value: true,
                        label: '有标注信息',
                        count: '',
                    },
                    {
                        value: false,
                        label: '无标注信息',
                        count: '',
                    },
                ],
                sampleList: [],
            });

            console.log(vData.sampleId);

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
                    const { code, data } = await $http.post({
                        url:    '/image_data_set_sample/query',
                        params: Object.assign(vData.search, { data_set_id: vData.sampleId }),
                    });
                    
                    nextTick(_ => {
                        if(code === 0) {
                            if (data && data.list) {
                                vData.search.total = data.total;
                                vData.sampleList = data.list;
                            }
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
                    vData.search.labeled = val.props.name;
                    methods.getSampleList();
                },
            };

            onBeforeMount(() => {
                methods.getSampleInfo();
                methods.getSampleList();
            });

            return {
                vData,
                methods,
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
}
.check_label {
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
</style>
<style lang="scss" scoped>
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
</style>
