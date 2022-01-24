<template>
    <el-card v-loading="vData.pageLoading" class="page_layer_label">
        <div class="check_label">
            <el-tabs v-model="vData.activeName" @tab-click="methods.tabChange">
                <div class="label_content">
                    <label-system v-show="vData.sampleList.length" ref="labelSystemRef" :currentImage="vData.currentImage" :labelList="vData.count_by_sample" :for-job-type="vData.forJobType" @save-label="methods.saveCurrentLabel" />
                    <div v-if="vData.sampleList.length === 0" class="empty_box">
                        <EmptyData />
                    </div>
                    <image-thumbnail-list ref="imgThumbnailListRef" :sampleList="vData.sampleList" @select-image="methods.selectImage" />
                </div>
                <el-tab-pane v-for="item in vData.tabsList" :key="item.label" :label="item.label + ' (' + item.count + ')'" :name="item.name"></el-tab-pane>
                <div class="label_list_box">
                    <div class="label_bar">
                        <p>标签栏</p>
                    </div>
                    <div class="label_search">
                        <el-input type="text" placeholder="请输入标签名称" v-model="vData.labelName" @input="methods.labelSearch">
                            <template #suffix>
                                <el-icon class="el-input__icon"><elicon-search /></el-icon>
                            </template>
                        </el-input>
                    </div>
                    <div class="label_info">
                        <template v-if="vData.count_by_sample_list.length>0">
                            <div class="label_info_list">
                                <div v-for="(item, index) in vData.count_by_sample_list" :key="item.label" class="label_item">
                                    <p class="span_label" @click="vData.forJobType === 'classify' ? methods.labelSampleEvent(item.label) : ''">{{item.label}}</p>
                                    <span v-if="item.keycode !== '' && index<10" class="span_tips">快捷键<span class="span_count">{{item.keycode}}</span></span>
                                    <el-icon v-if="item.iscustomized" class="el-icon-close label_close" @click="methods.deleteLabel(index)"><elicon-circle-close-filled /></el-icon>
                                </div>
                            </div>
                        </template>
                        <template v-else>
                            <EmptyData />
                        </template>
                        <!-- 自定义的可支持 编辑 删除 当标签列表高度超过可视区时，添加标签的输入框固定 -->
                        <div class="fixed_box">
                            <el-input type="text" placeholder="按回车或Tab添加标签" v-model="vData.newLabel" show-word-limit :maxlength="10" @keyup.enter="methods.addLabel" @keydown.tab="methods.addLabel" />
                        </div>
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
    import { ref, reactive, onBeforeMount, getCurrentInstance, nextTick, onUnmounted } from 'vue';
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
            const { $http, $message } = appContext.config.globalProperties;
            const imgListRef = ref();
            const labelSystemRef = ref();
            const imgThumbnailListRef = ref();
            const vData = reactive({
                activeName: route.query.unlabeled_count !== '0' ? 'unlabeled' : '',
                sampleId:   route.query.id,
                forJobType: route.query.for_job_type,
                search:     {
                    page_index: route.query.page_index || 1,
                    page_size:  route.query.page_size || 20,
                    label:      '',
                    labeled:    route.query.unlabeled_count !== '0' ? false : '',
                    total:      1,
                },
                tabsList: [
                    {
                        label: '全部',
                        name:  '',
                        count: '',
                    },
                    {
                        label: '已标注',
                        name:  'labeled',
                        count: '',
                    },
                    {
                        label: '未标注',
                        name:  'unlabeled',
                        count: '',
                    },
                ],
                sampleList:           [],
                currentImage:         {},
                timer:                null,
                timer2:               null,
                count_by_label:       [],
                count_by_sample:      [],
                count_by_sample_list: [],
                labelName:            '',
                newLabel:             '',
                pageLoading:          false,
                pageSamplelength:     0, // 当前页图片数量
            });

            const methods = {
                async getSampleInfo() {
                    const { code, data } = await $http.post({
                        url:    '/image_data_set/detail',
                        params: { id: vData.sampleId },
                    });

                    nextTick(_ => {
                        if(code === 0) {
                            vData.tabsList[0].count = data.total_data_count;
                            vData.tabsList[1].count = data.labeled_count;
                            vData.tabsList[2].count = data.total_data_count - data.labeled_count;
                        }
                    });
                },
                async getSampleList() {
                    vData.pageLoading = true;
                    vData.sampleList = [];
                    const params = {
                        page_index:  vData.search.page_index - 1,
                        page_size:   vData.search.page_size,
                        label:       vData.search.label,
                        data_set_id: vData.sampleId,
                        labeled:     vData.search.labeled,
                    };
                    const { code, data } = await $http.post({
                        url:  '/image_data_set_sample/query',
                        data: params,
                    });
                    
                    nextTick(_ => {
                        if(code === 0) {
                            if (data && data.list.length>0) {
                                vData.search.total = data.total;
                                vData.pageSamplelength = data.list.length;
                                data.list.forEach((item, idx) => {
                                    methods.downloadImage(item.id, idx, item);
                                });
                                // window.addEventListener('keydown', function(e) {
                                //     labelSystemRef.value.methods.handleEvent(e);
                                // });
                            } else {
                                vData.search.total = data.total;
                                vData.sampleList = data.list;
                                vData.pageLoading = false;
                            }
                        }
                    });
                },
                async downloadImage(id, idx, item) {
                    const { code, data } = await $http.get({
                        url:          '/image_data_set_sample/download',
                        params:       { id },
                        responseType: 'blob',
                    });

                    nextTick(_ => {
                        if(code === 0) {
                            const url = window.URL.createObjectURL(data);

                            if (id === item.id) {
                                item.img_src = url;
                                item.$isselected = false;
                            }
                            vData.sampleList.push(item);
                            vData.sampleList[0].$isselected = true;
                            vData.currentImage = { item: vData.sampleList[0], idx: 0 };
                            nextTick(_=> {
                                // When the last picture is obtained, call the interface to update the current label information
                                if (idx === vData.search.page_size - 1 && vData.pageSamplelength === vData.search.page_size) {
                                    labelSystemRef.value.methods.createStage();
                                }
                                // When the number of data items on this page is less than the current page number, store the total number of samples on the current page for comparison
                                else if (vData.pageSamplelength === idx + 1 && vData.pageSamplelength < vData.search.page_size) {
                                    labelSystemRef.value.methods.createStage();
                                }
                            });
                            vData.pageLoading = false;
                            
                        }
                    });
                },
                async getLabelInfo() {
                    const { code, data } = await $http.get({
                        url:    '/image_data_set_sample/statistics',
                        params: { data_set_id: vData.sampleId },
                    });

                    nextTick(_=> {
                        if (code === 0) {
                            if (data) {
                                const { count_by_label, count_by_sample } = data;

                                vData.count_by_label = count_by_label;
                                count_by_sample.forEach((item, i) => {
                                    if (i < 10 ) item.keycode = i;
                                });
                                vData.count_by_sample = count_by_sample;
                                vData.count_by_sample_list = count_by_sample;
                            }
                        }
                    });
                },
                selectImage(item, idx) {
                    vData.currentImage = { item, idx };
                    nextTick(_=> {
                        labelSystemRef.value.methods.createStage();
                        vData.sampleList.forEach(i => {
                            i.$isselected = false;
                        });
                        vData.sampleList[idx].$isselected = true;
                    });
                },
                currentPageChange (val) {
                    vData.search.page_index = val;
                    vData.sampleList = [];
                    methods.getSampleList();
                },
                pageSizeChange (val) {
                    vData.search.page_size = val;
                    vData.search.page_index = 1;
                    vData.sampleList = [];
                    methods.getSampleList();
                },
                tabChange(val) {
                    const label_type = val.props.name === 'labeled' ? true : val.props.name === 'unlabeled' ? false : '';

                    if (label_type === vData.search.labeled) return;
                    vData.search.labeled = label_type;
                    vData.search.page_index = 1;
                    vData.sampleList = [];
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
                addLabel() {
                    if (vData.newLabel) {
                        vData.count_by_sample.push({
                            label:        vData.newLabel,
                            count:        0,
                            keycode:      vData.count_by_sample.length > 9 ? '' : vData.count_by_sample.length,
                            iscustomized: true,
                        });
                        vData.newLabel = '';
                        vData.count_by_sample_list = vData.count_by_sample;
                    }
                },
                deleteLabel(idx) {
                    vData.count_by_sample_list.splice(idx, 1);
                    vData.count_by_sample = vData.count_by_sample_list;
                },
                // 保存当前标注
                async saveCurrentLabel(res, id) {
                    vData.pageLoading = true;
                    const params = {
                        id,
                        label_info: {
                            objects: res, 
                        },
                    };

                    console.log(params);
                    const { code } = await $http.post({
                        url:  '/image_data_set_sample/update',
                        data: params,
                    });

                    nextTick(_ => {
                        if(code === 0) {
                            $message.success('保存成功');
                            // 标注下一张
                            // 判断是否为最后一张
                            if (vData.sampleList.length - 1 !== vData.currentImage.idx) {
                                // vData.sampleList[vData.currentImage.idx].label_info = params.label_info;
                                vData.sampleList[vData.currentImage.idx].$isselected = false;
                                vData.sampleList[vData.currentImage.idx+1].$isselected = true;
                                vData.currentImage = { item: vData.sampleList[vData.currentImage.idx+1], idx: vData.currentImage.idx+1 };
                                nextTick(_=> {
                                    labelSystemRef.value.methods.createStage();
                                    methods.getSampleInfo();
                                });
                            } else {
                                // 本页最后一张，获取第二页数据
                                if (vData.search.page_index !== vData.search.total / vData.search.page_size) {
                                    vData.search.page_index = vData.search.page_index + 1;
                                    methods.getSampleList();
                                }
                            }
                        }
                        vData.pageLoading = false;
                    });
                },
                labelSearch(val) {
                    vData.count_by_sample_list = vData.count_by_sample.filter(function(item) {
                        return Object.keys(item).some(function(key) {
                            return (
                                String(item[key]).toLowerCase().indexOf(val) > -1
                            );
                        });
                    });
                },
                // label classify sample
                labelSampleEvent(text) {
                    const res = [];

                    res.push({
                        label:  text,
                        points: [],
                    });
                    methods.saveCurrentLabel(res, vData.currentImage.item.id);
                },
            };

            onBeforeMount(() => {
                methods.getSampleInfo();
                methods.getLabelInfo();
                // 注意清除定时器
                if (vData.timer2) clearTimeout(vData.timer2);
                vData.timer2 = setTimeout(_=> {
                    methods.getSampleList();
                    methods.resetWidth();
                }, 200);
                window.onresize = () => {
                    methods.debounce();
                };
            });

            onUnmounted(()=>{
                clearTimeout(vData.timer);
                clearTimeout(vData.timer2);
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
                    .el-input__suffix {
                        position: absolute;
                        top: 5px;
                    }
                }
                .label_info {
                    padding: 0 10px;
                    .label_info_list {
                        height: calc(100vh - 490px);
                        overflow-y: auto;
                    }
                    .label_item {
                        height: 40px;
                        @include flex_box;
                        border: 1px solid #eee;
                        margin: 10px 0;
                        padding: 0 10px;
                        font-size: 14px;
                        position: relative;
                        .span_label {
                            flex: 1;
                            height: 100%;
                            cursor: pointer;
                            line-height: 40px;
                        }
                        .span_tips {
                            font-size: 12px;
                            color: #999;
                        }
                        .span_count {
                            flex-shrink: 0;
                            display: inline-block;
                            width: 18px;
                            height: 18px;
                            font-size: 12px;
                            color: #999;
                            letter-spacing: 0;
                            text-align: center;
                            line-height: 16px;
                            font-weight: 500;
                            border: 1px solid #ddd;
                            align-self: center;
                            border-radius: 2px;
                            margin-left: 4px;
                        }
                        .label_close {
                            font-size: 15px;
                            color: #ccc;
                            position: absolute;
                            right: -2px;
                            top: -7px;
                            cursor: pointer;
                            z-index: 3;
                        }
                        &:hover {
                            border: 1px solid #438bff;
                        }
                    }
                    .fixed_box {
                        position: absolute;
                        bottom: 5px;
                        width: 93%;
                    }
                }
            }
            .label_content {
                flex: 1;
                overflow-y: auto;
                .empty_box {
                    width: calc(100% - 280px);
                }
            }
        }
        
    }
}
</style>
<style lang="scss" scoped>
@mixin inputStyle {
    height: 40px;
    :deep(input.el-input__inner) {
        height: 40px;
    }
    :deep(.el-input__prefix) {
        top: 5px;
    }
}
.page_layer_label {
    .label_search {
        .el-input {
            width: 90%;
            @include inputStyle;
        }
    }
    .label_info {
        .el-input {
            @include inputStyle;
        }
    }
}
</style>
