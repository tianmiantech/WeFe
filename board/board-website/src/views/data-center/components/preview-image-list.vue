<template>
    <div class="img_list">
        <div v-for="item in sampleList" class="img_items" :key="item.id">
            <el-image :src="item.img_src" :id="item.id" fit="contain" style="max-height: 114px;">
                <template #reference>
                    <div class="image-slot">
                        <i class="el-icon-picture-outline"></i>
                    </div>
                </template>
            </el-image>
            <div class="btns">
                <div class="l_tips">{{item.labeled ? item.label_list.split(',')[0] : '未标注'}}</div>
            </div>
        </div>
    </div>
</template>

<script>
    export default {
        props: {
            sampleList: Array,
        },
        setup(props, context) {
            
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
    .img_items {
        width: 124px;
        max-height: 144px;
        border: 1px solid #eee;
        margin-right: 10px;
        margin-bottom: 10px;
        background: #f5f5f5;
        .img_item {
            width: 120px;
            max-height: 110px;
            text-align: center;
        }
    }
    .btns {
        @include flex_box;
        justify-content: space-between;
        align-items: center;
        padding: 0 4px;
        color: #999;
        .l_tips {
            font-size: 12px;
        }
    }
}
</style>
