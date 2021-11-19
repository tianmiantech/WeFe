<template>
    <div class="img_layer" :style="{width: vData.width+'px'}">
        <!-- <div><i class="el-icon-d-arrow-left"/></div> -->
        <div v-for="(item, index) in sampleList" class="img_items" :key="item.id">
            <div class="img_item" @click="methods.selectImage(item, index)" :style="{border: item.$isselected ? '1px solid #438bff' : ''}">
                <el-image :src="item.img_src" :id="item.id" fit="contain">
                    <template #reference>
                        <div class="image-slot">
                            <i class="el-icon-picture-outline"></i>
                        </div>
                    </template>
                </el-image>
            </div>
            <p class="label_tips" v-if="!item.labeled">未标注</p>
        </div>
        <!-- <div><i class="el-icon-d-arrow-right"/></div> -->
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
                width:   0,
            });

            const methods = {
                selectImage(item, idx) {
                    context.emit('select-image', item, idx);
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
    height: 85px;
    @include flex_box;
    overflow-x: auto;
    overflow-y: hidden;
    align-content: baseline;
    padding: 5px 10px;
    .img_items {
        width: 50px;
        height: 80px;
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
        .label_tips {
            width: 50px;
            position: absolute;
            bottom: 0;
            font-size: 12px;
            color: #999;
            text-align: center;
        }
    }
}
</style>
