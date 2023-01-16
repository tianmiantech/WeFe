<template>
    <el-container
        :class="{ 'side-collapsed': vData.isCollapsed }"
        :style="{height: isQianKun() ? 'calc(100vh - var(--tm-header-height))' : '100%'}"
    >
        <layout-side />

        <el-container>
            <el-header
                ref="layout-header"
                height="90px"
            >
                <layout-header
                    v-if="vData.isRouterAlive"
                    @start-chart="startChart"
                />
            </el-header>

            <el-main id="layout-main" class="layout-main">
                <div
                    id="base-wrapper"
                    class="base-wrapper"
                >
                    <router-view
                        v-if="vData.isRouterAlive"
                        :key="$route.name"
                        v-slot="{ Component }"
                    >
                        <template v-if="Component">
                            <transition mode="out-in">
                                <Suspense>
                                    <template #default>
                                        <component :is="Component" />
                                    </template>
                                    <template #fallback>
                                        Loading ...
                                    </template>
                                </Suspense>
                            </transition>
                        </template>
                    </router-view>
                </div>
                <TitleNavigator />
            </el-main>
        </el-container>

        <LoginDialog />

        <ChatUI
            ref="chatui"
            :ws="vData.ws"
            :members="vData.members"
            @ws-restart="restartWs"
        />
    </el-container>
</template>

<script>
    import {
        ref,
        computed,
        reactive,
        provide,
        nextTick,
        getCurrentInstance,
        onBeforeMount,
    } from 'vue';
    import { useStore } from 'vuex';
    import { useRouter } from 'vue-router';
    import LayoutSide from './LayoutSide/LayoutSide.vue';
    import LayoutHeader from './LayoutHeader.vue';
    import LoginDialog from './LoginDialog.vue';
    import ChatUI from './ChatUI/ChatUI.vue';
    import { isQianKun } from '@src/http/utils';
    import { getSystemLicense } from '@src/service/permission';
    import { appCode, baseURL } from '../utils/constant';

    export default {
        components: {
            LayoutSide,
            LayoutHeader,
            LoginDialog,
            ChatUI,
        },
        setup() {
            const store = useStore();
            const $router = useRouter();
            const instance = getCurrentInstance();
            const { $message, $bus, $http } = instance.appContext.config.globalProperties;
            const userInfo = computed(() => store.state.base.userInfo);

            let heartbeat = null,
                wsConnected = false;

            const vData = reactive({
                ws:            null,
                isCollapsed:   false,
                isRouterAlive: true,
                members:       [],
            });
            const chatui = ref();
            const methods = {
                refresh() {
                    setTimeout(_ => {
                        vData.isRouterAlive = false;
                        nextTick(() => {
                            vData.isRouterAlive = true;
                        });
                    }, 300);
                },

                // ws heat beat every 26s
                wsHeartbeat() {
                    clearTimeout(heartbeat);

                    heartbeat = setTimeout(() => {
                        if(wsConnected) {
                            vData.ws.send('ping');
                            methods.wsHeartbeat();
                            /* setTimeout(() => {
                                ws.close();
                                ws = null;
                            }, 5000); */
                        } else {
                            clearTimeout(heartbeat);
                        }
                    }, 26 * 10e2);
                },
            };
            // open chat room
            const startChart = () => {
                if (window.localStorage.getItem(`${appCode()}_chat`) === 'connect') {
                    nextTick(async () => {
                        chatui.value.show();
                        restartWs();
                    });
                }
            };
            const restartWs = () => {
                if(vData.ws) return;

                const protocol = baseURL().replace(/^http/, 'ws');
                const url = process.env.NODE_ENV === 'production' ? protocol : 'wss://xxx.wolaidai.com/board-service-04'; // ws://xxx:8080/board-service-01 // wss://xxx.wolaidai.com/board-service-01
                const websocket = new WebSocket(`${url}/chatserver/${userInfo.value.id}`);

                websocket.addEventListener('open', ev => {
                    vData.ws = websocket;
                    chatui.value.socketOnOpen(ev);
                    wsConnected = true;
                });

                websocket.addEventListener('message', ev => {
                    const data = JSON.parse(ev.data);

                    if(data.code !== 0) {
                        wsConnected = false;
                        if(data.code === 10006) {
                            // need to login
                            $bus.$emit('show-login-dialog');
                        } else {
                            $message.error(data.message || '发生异常');
                        }
                    } else {
                        wsConnected = true;
                        chatui.value.socketOnMessage(ev);
                    }
                });

                websocket.addEventListener('close', ev => {
                    clearTimeout(heartbeat);
                    chatui.value.socketOnClose(ev);
                    vData.ws = null;
                });

                methods.wsHeartbeat();
                // disconnect before refresh
                window.onbeforeunload = function() {
                    websocket.close();
                };
            };

            // 初始化
            const init = async() => {
                let { code, data } = await $http.get({
                    url: '/account/sso_login',
                });

                data = data || {}

                const {ui_config} = data;

                if (code === 10000) {
                    store.commit('SYSTEM_INITED', false);
                    store.commit('UPDATE_USERINFO', data);
                    store.commit('UI_CONFIG', ui_config);

                    $router.replace({
                        name: 'init',
                    });
                } else if (code === 0) {
                    // store.commit('UPDATE_USERINFO', data);
                    store.commit('SYSTEM_INITED', true);
                    store.commit('UI_CONFIG', ui_config || {});

                    const res = await $http.get({
                        url: '/member/detail',
                    });

                    if(res.code === 0) {
                        data.admin_role = true;
                        data.super_admin_role = true;
                        const info = Object.assign(data, res.data);

                        store.commit('UPDATE_USERINFO', info);
                    } else {
                        store.commit('SYSTEM_INITED', false);
                        store.commit('UPDATE_USERINFO', null);
                        $message.error('请重试');
                    }
                    checkEnv();
                }
            };
            // 判断是否为 demo 环境
            const checkEnv = async() => {
                const { code, data } = await $http.get('/env');

                if (code === 0) {
                    const is_demo = data.env_properties.is_demo === 'true';

                    store.commit('IS_DEMO', is_demo);
                } else {
                    store.commit('IS_DEMO', false);
                }
            };

            // 获取应用信息
            const getAppInfo = async() => {
                const data = await getSystemLicense();

                if (data) {
                    store.commit('APP_INFO', data);
                }
            };

            provide('refresh', methods.refresh);

            onBeforeMount(() => {
                init();
                if (isQianKun()) {
                    getAppInfo();
                }
                const asideCollapsedKey = `${appCode()}AsideCollapsed`;

                // collapsed left menus
                const $isCollapsed = window.localStorage.getItem(asideCollapsedKey);

                vData.isCollapsed = $isCollapsed === 'false' ? false : Boolean($isCollapsed);

                if($bus){
                    $bus.$on('sideCollapsed', asideCollapsed => {
                        vData.isCollapsed = asideCollapsed;
                    });

                    $bus.$on('show-login-dialog', () => {
                        setTimeout(() => {
                            chatui.value && chatui.value.hide();
                        }, 1000);
                    });
                }


                // check last state
                if (window.localStorage.getItem(`${appCode()}_chat`) === 'connect') {
                    startChart();
                }
            });

            return {
                vData,
                chatui,
                restartWs,
                startChart,
                isQianKun,
            };
        },
    };
</script>

<style lang="scss" scoped>
    .board-header {
        color: #000;
        position: relative;
        background: $header-background;
        border-bottom: 1px solid $border-color-base;
        z-index: 6;
    }
    .layout-main {
        padding: 20px;
        position: relative;
        min-height: calc(100vh - 120px);
        height: 0;
    }
</style>
