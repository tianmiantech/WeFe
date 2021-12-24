<template>
    <el-card shadow="never">
        <div class="card_content flexbox">
            <el-card class="card_box" @click="methods.jumpToDataAdd('csv')">
                <el-image :src="vData.baseData"></el-image>
                <h3 class="text-c">结构化数据</h3>
                <p class="description">用于机器学习解决二分类，多分类，回归等问题，挖掘数据中隐藏的模式，适用于信用评分、风控预测、广告推销等场景。</p>
                <el-button type="primary">进入</el-button>
            </el-card>
            <el-card class="card_box" @click="methods.jumpToDataAdd('img')">
                <el-image :src="vData.imgData"></el-image>
                <h3 class="text-c">图像数据</h3>
                <p class="description">定制图像分类，物体检测的AI模型，适用于图像内容检索定位、安防监控、工业质检等场景。</p>
                <el-button type="primary">进入</el-button>
            </el-card>
            <el-card class="card_box" @click="methods.jumpToDataAdd('filter')">
                <el-image :src="vData.imgData"></el-image>
                <h3 class="text-c">过滤器数据</h3>
                <p class="description">将样本数据生成为二进制的布隆过滤器资源，生成后不可更改，如需变更需要重新上传。布隆过滤器资源只适用于样本对齐，无法参与训练模型, 布隆过滤器资源仅用于RSA-PSI样本对齐算法</p>
                <el-button type="primary">进入</el-button>
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
                filter:   require('@assets/images/image-data.png'),
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

<style lang="scss" scoped>
    .card_content {
        overflow-x: auto;
        min-height: calc(100vh - 170px);
    }
    .card_box {
        flex: 1;
        cursor: pointer;
        min-width: 240px;
        position: relative;
        padding-bottom: 80px;
        margin:0 15px;
        .el-image {
            width:100%;
            display:block;
            max-width: 230px;
            margin:20px auto;
            height: auto;
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
    .el-button{
        width: 100%;
        max-width: 200px;
        font-size: 16px;
        position: absolute;
        bottom: 40px;
        left: 50%;
        transform: translateX(-50%);
    }
</style>
