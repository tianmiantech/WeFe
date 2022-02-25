<template>
    <div class="img_layer">
        <div v-for="(item, index) in sampleList" class="img_items" :key="item.id">
            <div class="img_item">
                <el-image :src="item.img_src" :id="item.id" fit="contain">
                    <template #reference>
                        <div class="image-slot">
                            <i class="el-icon-picture-outline"></i>
                        </div>
                    </template>
                </el-image>
                <div class="btns">
                    <el-popover
                        v-if="item.labeled"
                        placement="bottom-start"
                        trigger="hover"
                        :width="200"
                    >
                        <template #default>
                            <p class="color-danger"><el-tag v-for="item in item.$label_list" :key="item" style="margin-right: 4px;">{{item}}</el-tag></p>
                        </template>
                        <template #reference>
                            <div class="l_tips">{{item.$label_list.join(',')}}</div>
                        </template>
                    </el-popover>
                    <div v-else class="l_tips">未标注</div>
                    <div class="r_btn">
                        <!-- <el-icon class="el-icon-edit-outline" @click="methods.labelSingle(item.id, index)"><elicon-edit /></el-icon> -->
                        <el-icon class="el-icon-delete" @click="methods.deleteEvent(item.id, index)"><elicon-delete /></el-icon>
                    </div>
                </div>
            </div>
        </div>
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
                deleteEvent(id, idx) {
                    context.emit('delete-options', id, idx);
                },
                labelSingle(id, idx) {
                    context.emit('label-single-sample', id, idx);
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
        background: #f5f5f5;
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
            color: #999;
            .l_tips {
                font-size: 12px;
                cursor: pointer;
                width: 90px;
                white-space: nowrap;
                text-overflow: ellipsis;
                overflow: hidden;
                text-align: left;
            }
            .r_btn {
                font-size: 13px;
                cursor: pointer;
                .el-icon-edit-outline {
                    margin-right: 7px;
                }
            }
        }
    }
}
</style>
