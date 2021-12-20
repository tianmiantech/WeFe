<template>
    <el-container :class="{ 'side-collapsed': vData.isCollapsed }">
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
    import LayoutSide from './LayoutSide/LayoutSide.vue';
    import LayoutHeader from './LayoutHeader.vue';
    import LoginDialog from './LoginDialog.vue';
    import ChatUI from './ChatUI/ChatUI.vue';

    export default {
        components: {
            LayoutSide,
            LayoutHeader,
            LoginDialog,
            ChatUI,
        },
        setup() {
            const store = useStore();
            const instance = getCurrentInstance();
            const { $message, $bus } = instance.appContext.config.globalProperties;
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
                    vData.isRouterAlive = false;
                    nextTick(() => {
                        vData.isRouterAlive = true;
                    });
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
                if (window.localStorage.getItem(`${window.api.prefixPath}_chat`) === 'connect') {
                    nextTick(async () => {
                        chatui.value.show();
                        restartWs();
                    });
                }
            };
            const restartWs = () => {
                if(vData.ws) return;

                const url = process.env.NODE_ENV === 'production' ? `wss://${window.api.baseUrl.replace(/http(s?):\/\//, '')}` : 'wss://xxx.wolaidai.com/board-service-04'; // ws://xxx:8080/board-service-01 // wss://xxx.wolaidai.com/board-service-01
                const websocket = new WebSocket(`${url}/chatserver/${userInfo.value.token}`);

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

            provide('refresh', methods.refresh);

            onBeforeMount(() => {
                const { prefixPath } = window.api;
                const asideCollapsedKey = `${prefixPath}AsideCollapsed`;

                // collapsed left menus
                const $isCollapsed = window.localStorage.getItem(asideCollapsedKey);

                vData.isCollapsed = $isCollapsed === 'false' ? false : Boolean($isCollapsed);

                $bus.$on('sideCollapsed', asideCollapsed => {
                    vData.isCollapsed = asideCollapsed;
                });

                $bus.$on('show-login-dialog', () => {
                    setTimeout(() => {
                        chatui.value && chatui.value.hide();
                    }, 1000);
                });

                // check last state
                if (window.localStorage.getItem(`${prefixPath}_chat`) === 'connect') {
                    startChart();
                }
            });

            return {
                vData,
                chatui,
                restartWs,
                startChart,
            };
        },
    };
</script>

<style lang="scss" scoped>
    .el-header {
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
