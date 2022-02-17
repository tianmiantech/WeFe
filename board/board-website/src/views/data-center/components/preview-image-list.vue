<template>
    <div class="img_list" v-loading="vData.imgLoading">
        <div v-for="item in vData.sampleList" class="img_items" :key="item.id">
            <el-image :src="item.img_src" :id="item.id" fit="contain" style="max-height: 114px;">
                <template #reference>
                    <div class="image-slot">
                        <i class="el-icon-picture-outline"></i>
                    </div>
                </template>
            </el-image>
            <div class="btns">{{item.labeled ? item.label_list.split(',')[0] : '未标注'}}</div>
        </div>
    </div>
</template>

<script>
    import { getCurrentInstance, nextTick, reactive } from 'vue';
    export default {
        setup(props, context) {
            const { appContext } = getCurrentInstance();
            const { $http } = appContext.config.globalProperties;
            const vData = reactive({
                imgLoading: false,
                sampleList: [],
                search:     {
                    page_index: 1,
                    page_size:  20,
                    label:      '',
                    labeled:    '',
                    total:      1,
                },
            });
            const methods = {
                async getSampleList(id) {
                    console.log(vData.sampleList);
                    vData.imgLoading = true;
                    const params = {
                        page_index:  vData.search.page_index - 1,
                        page_size:   vData.search.page_size,
                        label:       vData.search.label,
                        data_set_id: id,
                        labeled:     vData.search.labeled,
                    };
                    const { code, data } = await $http.post({
                        url:  '/image_data_set_sample/query',
                        data: params,
                    });
                    
                    nextTick(_=> {
                        if(code === 0) {
                            if (data && data.list.length > 0) {
                                vData.search.total = data.total;
                                data.list.forEach((item, idx) => {
                                    methods.downloadImage(item.id, idx, item);
                                });
                            } else {
                                vData.search.total = data.total;
                                vData.sampleList = data.list;
                                vData.imgLoading = false;
                            }
                        }
                    });
                },
                async downloadImage(id, idx, item) {
                    vData.sampleList = [];
                    const { code, data } = await $http.get({
                        url:          '/image_data_set_sample/download',
                        params:       { id },
                        responseType: 'blob',
                    });

                    nextTick(_=>{
                        if(code === 0) {
                            const url = window.URL.createObjectURL(data);

                            if (id === item.id) {
                                item.img_src = url;
                            }
                            vData.sampleList.push(item);
                            setTimeout(_=> {
                                vData.imgLoading = false;
                            }, 200);
                        }
                    });
                },
            };

            return {
                vData,
                methods,
            };
        },
    };
</script>

<style lang="scss" scoped>
@mixin flex_box {
    display: flex;
}
.img_list {
    @include flex_box;
    flex-wrap: wrap;
    align-content: baseline;
    min-height: 200px;
    .img_items {
        width: 124px;
        height: 144px;
        border: 1px solid #eee;
        margin-right: 10px;
        margin-bottom: 10px;
        background: #f5f5f5;
        position: relative;
        .img_item {
            width: 120px;
            max-height: 110px;
            text-align: center;
        }
    }
    .btns {
        @include flex_box;
        width: 100%;
        justify-content: center;
        align-items: center;
        padding: 0 4px;
        font-size: 12px;
        color: #666;
        position: absolute;
        bottom: 4px;
    }
}
</style>
