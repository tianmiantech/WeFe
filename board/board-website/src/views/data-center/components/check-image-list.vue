<template>
    <div class="img_layer">
        <div v-for="item in sampleList" class="img_items" :key="item.id">
            <!-- <img :src="methods.downloadImage(item.id)"> -->
            <div class="img_item">
                <el-image :src="`${vData.baseUrl}/image_data_set_sample/download?id=${item.id}`" fit="contain">
                    <template #reference>
                        <div class="image-slot">
                            <i class="el-icon-picture-outline"></i>
                        </div>
                    </template>
                </el-image>
                <div class="btns">
                    <div class="l_tips">{{item.labeled ? '已标注' : '未标注'}}</div>
                    <div class="r_btn">
                        <i class="el-icon-edit-outline" />
                        <i class="el-icon-delete" />
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script>
    import { reactive, getCurrentInstance } from 'vue';
    export default {
        props: {
            sampleList: Array,
        },
        setup() {
            const { appContext } = getCurrentInstance();
            const { $http } = appContext.config.globalProperties;
            const vData = reactive({
                baseUrl: window.api.baseUrl,
            });

            const methods = {
                async downloadImage(id) {
                    const { code } = await $http.get({
                        url:    '/image_data_set_sample/download',
                        params: { id },
                    });

                    console.log(code);
                    // nextTick(_ => {
                    //     if(code === 0) {
                            
                    //     }
                    // });
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
.img_layer {
    height: calc(100vh - 280px);
    overflow-y: auto;
    @include flex_box;
    flex-wrap: wrap;
    align-content: baseline;
    padding: 10px 0 0 10px;
    .img_items {
        width: 124px;
        height: 144px;
        border: 1px solid #eee;
        margin-right: 10px;
        margin-bottom: 10px;
        background: #eee;
        .img_item {
            width: 120px;
            height: 110px;
            text-align: center;
            .el-image {
                height: 100%;
            }
        }
        .btns {
            @include flex_box;
            justify-content: space-between;
            align-items: center;
            height: 30px;
            padding: 0 4px;
            color: #777;
            .l_tips {
                font-size: 12px;
            }
            .r_btn {
                font-size: 13px;
                cursor: pointer;
                i:first-child {
                    padding-right: 7px;
                }
            }
        }
    }
}
</style>
