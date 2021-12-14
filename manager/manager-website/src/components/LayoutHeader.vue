<template>
    <div class="layout-header">
        <div class="heading-bar">
            <span
                v-if="vData.meta.titleParams"
                class="float-left f14"
            >
                <el-button
                    class="mr10"
                    type="text"
                    @click="backward"
                >
                    <i class="el-icon-arrow-left" />返回{{ vData.meta.titleParams.parentTitle || vData.meta.title }}
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
                class="float-left"
            />
            <span class="heading-tools">
                <el-tooltip
                    effect="light"
                    :content="vData.isFullScreen ? '退出全屏' : '切换全屏'"
                    placement="bottom"
                >
                    <i
                        class="el-icon-full-screen"
                        @click="fullScreenSwitch"
                    />
                </el-tooltip>
            </span>
            <div class="heading-user">
                你好,
                <el-dropdown
                    class="ml5"
                    @command="handleCommand"
                >
                    <span class="el-dropdown-link">
                        <strong>{{ userInfo.realname }}</strong>
                        <i class="el-icon-arrow-down" />
                    </span>
                    <template #dropdown>
                        <el-dropdown-menu>
                            <el-dropdown-item command="logout">
                                <i class="el-icon-switch-button" />
                                退出
                            </el-dropdown-item>
                        </el-dropdown-menu>
                    </template>
                </el-dropdown>
            </div>
        </div>
        <layout-tags v-show="tagsList.length" />
    </div>
</template>

<script>
    import {
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
            const { $bus, $http } = appContext.config.globalProperties;

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
                        await $http.post({
                            url: '/logout',
                        });

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
            };
        },
    };
</script>

<style lang="scss" scoped>
    .heading-bar {
        white-space: nowrap;
        text-align: right;
        line-height: 30px;
        padding: 10px 0 6px;
        .heading-title{
            display: inline-block;
            vertical-align: middle;
            max-width: 300px;
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
        }
        .heading-tools {
            display: inline-block;
            vertical-align:top;
            padding: 0 10px;
            height: 30px;
            line-height: 30px;
            text-align: center;
            [class*="el-icon-"],
            .iconfont {
                width: 30px;
                height: 30px;
                line-height: 30px;
                display: inline-block;
                vertical-align:top;
                cursor: pointer;
                &:hover {
                    transform: scale(1.15);
                }
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
            padding-right: 10px;
            font-size: 14px;
            height: 30px;
            line-height: 30px;
            cursor: pointer;
        }
    }
</style>
