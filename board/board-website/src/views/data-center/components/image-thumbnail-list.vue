<template>
    <div class="img_layer">
        <div v-for="item in sampleList" class="img_items" :key="item.id">
            <div class="img_item" @click="methods.selectImage(item)">
                <el-image :src="item.img_src" :id="item.id" fit="contain">
                    <template #reference>
                        <div class="image-slot">
                            <i class="el-icon-picture-outline"></i>
                        </div>
                    </template>
                </el-image>
            </div>
        </div>
        <!-- <div class="btns">
            <p><i class="el-icon-d-arrow-left"/></p>
            <p><i class="el-icon-d-arrow-right"/></p>
        </div> -->
    </div>
</template>

<script>
    import { reactive, onBeforeMount } from 'vue';
    export default {
        props: {
            sampleList: Array,
        },
        setup(props, context) {
            const vData = reactive({
                baseUrl: window.api.baseUrl,
            });

            const methods = {
                selectImage(item) {
                    context.emit('select-image', item);
                },
            };

            onBeforeMount(_=> {
                
            });
            
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
    position: relative;
    width: 100%;
    height: 80px;
    @include flex_box;
    overflow-x: auto;
    overflow-y: hidden;
    align-content: baseline;
    padding: 10px 0 0 10px;
    .img_items {
        width: 50px;
        height: 80px;
        border: 1px solid #eee;
        margin-right: 10px;
        margin-bottom: 10px;
        background: #f5f5f5;
        cursor: pointer;
        .img_item {
            width: 50px;
            height: 80px;
            text-align: center;
            .el-image {
                height: 100%;
            }
        }
    }
    .btns {
        position: fixed;
        width: 100%;
        height: 30px;
        top: 50%;
        @include flex_box;
        justify-content: space-between;
        z-index: 2;
        p {
            i {
                font-size: 30px;
            }
        }
    }
}
</style>
