<template>
    <div class="layout-header">
        <div class="heading-bar flexbox">
            <span
                v-if="vData.meta.titleParams"
                class="heading-bar-title text-l f14"
            >
                <el-button
                    class="mr10"
                    type="text"
                    @click="backward"
                >
                    <el-icon class="el-icon-arrow-left">
                        <elicon-arrow-left />
                    </el-icon>返回{{ vData.meta.titleParams.parentTitle || vData.meta.title }}
                </el-button>
                <span
                    v-if="vData.meta.titleParams.htmlTitle"
                    v-html="vData.meta.titleParams.htmlTitle"
                    class="heading-title"
                />
                <span
                    v-else
                    class="heading-title"
                >{{ vData.meta.titleParams.title }}</span>
            </span>
            <span
                v-else
                v-html="vData.headingTitle || vData.meta.title"
                class="heading-bar-title text-l f14"
            />
            <div class="heading-tools">
                <el-tooltip
                    v-model="vData.videoTip"
                    popper-class="video-guide-tip"
                    placement="left"
                    :manual="true"
                >
                    <template #content>
                        <div class="video-guide-tip-content">视频在这里 👉
                            <div class="el-popper__arrow" data-popper-arrow></div>
                        </div>
                    </template>
                    <a
                        href="javascript:;"
                        @click="showVideoGuide"
                    >操作指引</a>
                </el-tooltip>
                <a
                    href="https://www.wjx.top/vj/hW9y0cp.aspx"
                    target="_blank"
                >建议与反馈</a>
                <el-tooltip
                    effect="light"
                    content="开启聊天"
                    placement="bottom"
                >
                    <i
                        class="iconfont icon-chat"
                        @click="getConnect"
                    >
                        <i
                            v-if="vData.hasUnreadNums"
                            class="unread-num"
                        >{{ vData.hasUnreadNums >= 99 ? `99+` : vData.hasUnreadNums }}</i>
                    </i>
                </el-tooltip>
                <el-tooltip
                    effect="light"
                    :content="vData.isFullScreen ? '退出全屏' : '切换全屏'"
                    placement="bottom"
                >
                    <el-icon class="el-icon-full-screen" @click="fullScreenSwitch"><elicon-full-screen /></el-icon>
                </el-tooltip>
                <div class="heading-user ml10">
                    你好,
                    <el-dropdown
                        class="ml5"
                        @command="handleCommand"
                    >
                        <span class="el-dropdown-link">
                            <strong>{{ userInfo.nickname }}</strong>
                            <el-icon class="el-icon-arrow-left">
                                <elicon-arrow-down />
                            </el-icon>
                        </span>
                        <template #dropdown>
                            <el-dropdown-menu>
                                <el-dropdown-item command="logout">
                                    <el-icon>
                                        <elicon-switch-button />
                                    </el-icon>
                                    退出
                                </el-dropdown-item>
                            </el-dropdown-menu>
                        </template>
                    </el-dropdown>
                </div>
            </div>
        </div>
        <layout-tags v-show="tagsList.length" />

        <VideoGuideDialog
            ref="VideoGuideDialog"
            @show-video-tip="showVideoTip"
        />
    </div>
</template>

<script>
    import {
        ref,
        computed,
        reactive,
        getCurrentInstance,
        onBeforeMount,
        watch,
    } from 'vue';
    import { useStore } from 'vuex';
    import { useRoute, useRouter } from 'vue-router';
    import { baseLogout } from '@src/router/auth';
    import LayoutTags from './LayoutTags.vue';

    export default {
        components: {
            LayoutTags,
        },
        emits: ['start-chart'],
        setup(props, context) {
            const route = useRoute();
            const router = useRouter();
            const store = useStore();
            const userInfo = computed(() => store.state.base.userInfo);
            const tagsList = computed(() => store.state.base.tagsList);
            const { appContext } = getCurrentInstance();
            const { $bus } = appContext.config.globalProperties;

            const VideoGuideDialog = ref();
            const vData = reactive({
                headingTitle:  '',
                videoTip:      false,
                isFullScreen:  false,
                loading:       false,
                hasUnreadNums: 0,
                meta:          route.meta,
            });
            const backward = () => {
                const { meta: { titleParams }, query } = route;

                if(titleParams) {
                    const { backward, params } = titleParams;

                    if(backward) {
                        $bus.$emit('history-backward', titleParams);
                    } else {
                        const $params = {};

                        params.forEach(name => {
                            $params[name] = query[name];
                        });

                        router.push({
                            name:  titleParams.name,
                            query: $params,
                        });
                    }
                }
            };
            const handleCommand = (command) => {
                if (!command) return;
                if(vData.loading) return;
                vData.loading = true;

                const policy = {
                    async logout() {
                        vData.loading = false;
                        baseLogout();
                    },
                };

                policy[command]();
            };
            const checkFullScreen = () => {
                const doc = document;

                return Boolean(
                    doc.fullscreenElement ||
                        doc.webkitFullscreenElement ||
                        doc.mozFullScreenElement ||
                        doc.msFullscreenElement,
                );
            };
            const fullScreenSwitch = () => {
                const doc = document;

                if (checkFullScreen()) {
                    vData.isFullScreen = false;
                    const cancelFullScreen = [
                        'cancelFullScreen',
                        'webkitCancelFullScreen',
                        'mozCancelFullScreen',
                        'msExitFullScreen',
                    ];

                    for (const item of cancelFullScreen) {
                        if (doc[item]) {
                            doc[item]();
                            break;
                        }
                    }
                } else {
                    vData.isFullScreen = true;
                    const element = doc.documentElement;
                    const requestFullscreen = [
                        'requestFullscreen',
                        'webkitRequestFullscreen',
                        'mozRequestFullscreen',
                        'msRequestFullscreen',
                    ];

                    for (const item of requestFullscreen) {
                        if (element[item]) {
                            element[item]();
                            break;
                        }
                    }
                }
            };
            // chat connection
            const getConnect = () => {
                const key = `${window.api.baseUrl}_chat`;
                const inited = window.localStorage.getItem(key);

                if(inited !== 'connect') {
                    window.localStorage.setItem(key, 'connect');
                    context.emit('start-chart');
                }
            };
            // show video help
            const showVideoGuide = () => {
                VideoGuideDialog.value.vData.show = true;
            };
            const showVideoTip = () => {
                vData.videoTip = true;
                setTimeout(() => {
                    vData.videoTip = false;
                }, 3000);
            };

            onBeforeMount(() => {
                $bus.$on('change-layout-header-title', data => {
                    // update title
                    if(data.meta) {
                        if(vData.meta.titleParams) {
                            vData.meta.titleParams.htmlTitle = data.meta;
                        } else {
                            vData.meta = {
                                titleParams: {
                                    htmlTitle: data.meta,
                                },
                            };
                        }
                    } else {
                        vData.headingTitle = data;
                    }
                });
                $bus.$on('has-new-message', num => {
                    vData.hasUnreadNums = num;
                });

                // disable f11
                window.onkeydown = function(e) {
                    if(e.keyCode === 122){
                        return false;
                    }
                };

                // listen esc key
                window.onresize = function() {
                    if(!checkFullScreen()){
                        vData.isFullScreen = false;
                    }
                };
            });

            watch(
                () => route.name,
                () => {
                    vData.meta = route.meta;
                },
            );

            return {
                vData,
                userInfo: userInfo.value,
                tagsList: tagsList.value,
                backward,
                handleCommand,
                fullScreenSwitch,
                VideoGuideDialog,
                getConnect,
                showVideoGuide,
                showVideoTip,
            };
        },
    };
</script>

<style lang="scss">
    @keyframes shift {
        0%{transform: translateX(0);}
        50%{transform: translateX(-10px);}
        100%{transform: translateX(0)}
    }
    .video-guide-tip{
        &.is-dark{
            background:transparent;
            padding:0;
        }
        & > .el-popper__arrow{display:none;}
    }
    .video-guide-tip-content{
        position: relative;
        color:#fff;
        padding:10px;
        border-radius: 4px;
        background: #303133;
        animation: shift 1s ease-in-out infinite;
        .el-popper__arrow{
            top: 12px;
            right: -4px;
        }
    }
</style>

<style lang="scss" scoped>
    .heading-bar {
        white-space: nowrap;
        text-align: right;
        line-height: 30px;
        padding: 10px 0 6px;
        .heading-bar-title{
            max-width: 50%;
            overflow: hidden;
            white-space: nowrap;
            text-overflow: ellipsis;
            display: -webkit-box;
            -webkit-line-clamp: 1;
            -webkit-box-orient: vertical;
        }
        .heading-title{
            display: inline-block;
            vertical-align: middle;
            max-width: 300px;
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
        }
        .heading-tools {
            flex: 1;
            height: 30px;
            line-height: 30px;
            [class*="el-icon-"],
            .iconfont {
                width: 30px;
                height: 30px;
                line-height: 30px;
                text-align: center;
                display: inline-block;
                vertical-align:top;
                cursor: pointer;
                &:hover {
                    transform: scale(1.1);
                }
            }

            .el-icon svg{
                position: relative;
                top: 4px;
            }
            a{
                border-bottom: 1px solid #77A1FF;
                margin-right:8px;
                line-height: 20px;
            }
            a:hover{text-decoration: none;}
        }
        .heading-user {
            display: inline-block;
            font-size: 14px;
            height: 30px;
            line-height: 30px;
            cursor: pointer;
            .el-icon-arrow-left{top:-2px;}
        }
        .el-dropdown{
            line-height: 30px;
        }
    }

    @keyframes zoom{
        0%{transform: scale(0.85)};
        50%{transform: scale(1.25)};
        100%{transform: scale(0.85)};
    }
    .icon-chat{
        position: relative;
        .unread-num{
            position: absolute;
            line-height: 16px;
            height: 16px;
            padding:0 4px;
            border-radius: 8px;
            background:$--color-danger;
            font-style: normal;
            font-size: 12px;
            color:#fff;
            left:16px;
            animation: zoom 2s infinite;
        }
    }
</style>
