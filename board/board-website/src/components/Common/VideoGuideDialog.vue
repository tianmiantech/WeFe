<template>
    <el-dialog
        v-model="vData.show"
        title="操作指引"
        width="60%"
        top="5vh"
        :center="true"
        append-to-body
        destroy-on-close
        @closed="closedDialog"
    >
        <div class="video-guides">
            <el-steps
                :active="vData.active"
                finish-status="success"
                align-center
            >
                <el-step
                    title="上传数据资源"
                    icon="elicon-upload"
                    @click="vData.active = 0"
                />
                <el-step
                    title="寻找合作方"
                    icon="elicon-avatar"
                    @click="vData.active = 1"
                />
                <el-step
                    title="建立合作"
                    icon="elicon-connection"
                    @click="vData.active = 2"
                />
                <el-step
                    title="创建并执行流程"
                    icon="elicon-video-play"
                    @click="vData.active = 3"
                />
            </el-steps>

            <video
                v-show="vData.active === 0"
                controls="controls"
                preload="meta"
                :src="videos[0]"
            />
            <video
                v-show="vData.active === 1"
                controls="controls"
                preload="meta"
                :src="videos[1]"
            />
            <video
                v-show="vData.active === 2"
                controls="controls"
                preload="meta"
                :src="videos[2]"
            />
            <video
                v-show="vData.active === 3"
                controls="controls"
                preload="meta"
                :src="videos[3]"
            />
        </div>

        <template #footer>
            <div class="dialog-footer">
                <el-button
                    :disabled="vData.active < 1"
                    type="primary"
                    @click="preStep"
                >
                    <el-icon class="el-icon-caret-left">
                        <elicon-caret-left />
                    </el-icon>
                    上一个
                </el-button>
                <el-button
                    :disabled="vData.active > 2"
                    type="primary"
                    @click="nextStep"
                >
                    下一个
                    <el-icon class="el-icon-caret-right">
                        <elicon-caret-right />
                    </el-icon>
                </el-button>
                <el-button
                    id="btnHiddenForever"
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
        font-size: 16px;
        font-weight: bold;
        line-height: 1px;
    }
    .el-steps {
        margin-bottom: 10px;
        .el-step{cursor: pointer;}
        :deep(.is-process) {color: $--color-primary !important;}
        :deep(.el-step__title){font-size: 14px;}
    }
    .video-guides{
        height: calc(100vh - 200px);
        margin-bottom: -50px;
        min-height: 400px;
        overflow: auto;
        video{
            width: auto;
            height: calc(100% - 140px);
            display: block;
            margin: 0 auto;
            max-width: 100%;
        }
    }
</style>
