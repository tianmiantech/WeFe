<template>
    <el-dialog
        v-model="vData.show"
        title="操作指引"
        width="65%"
        top="20px"
        :center="true"
        append-to-body
        destroy-on-close
        @closed="closedDialog"
    >
        <el-steps
            :active="vData.active"
            finish-status="success"
            align-center
        >
            <el-step
                title="上传数据集"
                icon="el-icon-upload"
                @click="vData.active = 0"
            />
            <el-step
                title="寻找合作方"
                icon="el-icon-s-custom"
                @click="vData.active = 1"
            />
            <el-step
                title="建立合作"
                icon="el-icon-connection"
                @click="vData.active = 2"
            />
            <el-step
                title="创建并执行流程"
                icon="el-icon-video-play"
                @click="vData.active = 3"
            />
        </el-steps>

        <div v-show="vData.active === 0">
            <video
                id="zane"
                controls="controls"
                preload="meta"
                :src="videos[0]"
                style="display: block; width: 100%;"
            />
        </div>
        <div v-show="vData.active === 1">
            <video
                id="zane"
                controls="controls"
                preload="meta"
                :src="videos[1]"
                style="display: block; width: 100%;"
            />
        </div>
        <div v-show="vData.active === 2">
            <video
                id="zane"
                controls="controls"
                preload="meta"
                :src="videos[2]"
                style="display: block; width: 100%;"
            />
        </div>
        <div v-show="vData.active === 3">
            <video
                id="zane"
                controls="controls"
                preload="meta"
                :src="videos[3]"
                style="display: block; width: 100%;"
            />
        </div>

        <template #footer>
            <div class="dialog-footer">
                <el-button
                    :disabled="vData.active < 1"
                    type="primary"
                    @click="preStep"
                >
                    <i class="el-icon-caret-left" /> 上一个
                </el-button>
                <el-button
                    :disabled="vData.active > 2"
                    type="primary"
                    @click="nextStep"
                >
                    下一个 <i class="el-icon-caret-right" />
                </el-button>
                <el-button
                    id="btnHiddenForever"
                    type="text"
                    @click="hiddenForever"
                >
                    不再提醒
                </el-button>
            </div>
        </template>
    </el-dialog>
</template>

<script>
    import {
        reactive,
        getCurrentInstance,
        onBeforeMount,
    } from 'vue';
    import { useRoute } from 'vue-router';

    export default {
        name:  'VideoGuideDialog',
        emits: ['show-video-tip'],
        setup(props, context) {
            const route = useRoute();
            const { appContext } = getCurrentInstance();
            const { $bus } = appContext.config.globalProperties;
            const videos = [
                'https://tianmian-wefe.oss-cn-shenzhen.aliyuncs.com/document/%E4%B8%8A%E4%BC%A0%E6%95%B0%E6%8D%AE.mp4',
                'https://tianmian-wefe.oss-cn-shenzhen.aliyuncs.com/document/%E5%AF%BB%E6%89%BE%E5%90%88%E4%BD%9C%E6%96%B9.mp4',
                'https://tianmian-wefe.oss-cn-shenzhen.aliyuncs.com/document/%E5%BB%BA%E7%AB%8B%E5%90%88%E4%BD%9C.mp4',
                'https://tianmian-wefe.oss-cn-shenzhen.aliyuncs.com/document/%E8%BF%90%E8%A1%8C%E6%B5%81%E7%A8%8B.mp4',
            ];
            const vData = reactive({
                active:             0,
                show:               false,
                beforeCloseAnimate: false,
            });
            const closedDialog = () => {
                context.emit('show-video-tip');
            };
            // hide forever
            const hiddenForever = () => {
                vData.show = false;
                window.localStorage.setItem(`${window.api.prefixPath}_hidden_video_guide_forever`, 'true');
            };
            const preStep = () => {
                vData.active--;
                if (vData.active < 0) vData.active = 3;
            };
            const nextStep = () => {
                vData.active++;
                if (vData.active > 3) vData.active = 0;
            };

            onBeforeMount(() => {
                if(route.name === 'index') {
                    vData.show = !window.localStorage.getItem(`${window.api.prefixPath}_hidden_video_guide_forever`);
                }

                $bus.$on('show-guide-video', () => {
                    vData.show = true;
                });
            });

            return {
                vData,
                videos,
                closedDialog,
                hiddenForever,
                preStep,
                nextStep,
            };
        },
    };
</script>

<style lang="scss" scoped>
    #btnHiddenForever{
        position: absolute;
        right: 25px;
        bottom: 20px;
        font-size: 14px;
    }
    .el-steps {
        margin-bottom: 10px;
        .el-step{cursor: pointer;}
        :deep(.is-process) {color: $--color-primary !important;}
        :deep(.el-step__title){font-size: 14px;}
    }
</style>
