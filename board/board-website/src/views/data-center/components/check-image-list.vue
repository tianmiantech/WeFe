<template>
    <div class="img_layer">
        <div v-for="item in sampleList" class="img_items" :key="item.id">
            <div class="img_item">
                <el-image :src="`image_data_set_sample/download?id=${item.id}`">
                    <template #reference>
                        <div class="image-slot">
                            <i class="el-icon-picture-outline"></i>
                        </div>
                    </template>
                </el-image>
                <img src="http://172.29.20.91:8081/api/image_data_set_sample/download?id=78766cdb9fdb4ca485120cbff59ff05b" />
                <div class="btns">
                    操作栏
                </div>
            </div>
        </div>
    </div>
</template>

<script>
    import { reactive } from 'vue';
    export default {
        props: {
            sampleList: Array,
        },
        setup() {
            const vData = reactive({
                
            });
            const methods = {

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
    align-items: center;
    // justify-content: space-between;
}
.img_layer {
    height: 690px;
    overflow-y: auto;
    @include flex_box;
    flex-wrap: wrap;
    .img_items {
        width: 320px;
        height: 180px;
        border: 1px solid #eee;
        margin-right: 20px;
    }
}
</style>
