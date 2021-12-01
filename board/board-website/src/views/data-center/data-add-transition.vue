<template>
    <el-card class=" card_layer" shadow="never">
        <div class="card_content">
            <el-card class="card_box" @click="methods.jumpToDataAdd('csv')">
                <el-image :src="vData.baseData" class="img_box"></el-image>
                <h3>结构化数据</h3>
                <p class="description">用于机器学习解决二分类，多分类，回归等问题，挖掘数据中隐藏的模式，适用于信用评分、风控预测、广告推销等场景。</p>
            </el-card>
            <el-card class="card_box" @click="methods.jumpToDataAdd('img')">
                <el-image :src="vData.imgData" class="img_box"></el-image>
                <h3>图像数据</h3>
                <p class="description">定制图像分类，物体检测的AI模型，适用于图像内容检索定位、安防监控、工业质检等场景。</p>
            </el-card>
        </div>
    </el-card>
</template>

<script>
    import { reactive } from 'vue';
    import { useRouter } from 'vue-router';
    export default {
        setup() {
            const router = useRouter();
            const vData = reactive({
                baseData: require('@assets/images/data-analysis.png'),
                imgData:  require('@assets/images/image-data.png'),
            });

            const methods = {
                jumpToDataAdd(type) {
                    router.replace({
                        name:  'data-add',
                        query: { type },
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

<style lang="scss">
@mixin flexBox {
    display: flex;
    align-items: center;
}
.card_layer {
    padding: 0 20px;
    >.el-card__body {
        @include flexBox;
        height: calc(100vh - 120px);
        flex-direction: column;
        justify-content: center;
    }
    .card_content {
        @include flexBox;
        .card_box {
            height: 430px;
            flex: 1;
            padding: 20px;
            cursor: pointer;
            .img_box {
                width: 230px;
                height: 230px;
                position: relative;
                left: 50%;
                transform: translateX(-50%);
            }
            h3 {
                font-size: 20px;
                font-weight: bold;
                margin: 20px 0 10px;
            }
            .description {
                line-height: 32px;
            }
        }
        .card_box:first-child {
            margin-right: 40px;
        }
    }
}
</style>
