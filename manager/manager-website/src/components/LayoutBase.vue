<template>
    <el-container
        :class="{ 'side-collapsed': vData.isCollapsed }"
        :style="{height: vData.isInQianKun ? 'calc(100vh - var(--tm-header-height))' : '100%'}"
    >
        <layout-side />

        <el-container>
            <el-header
                ref="layout-header"
                height="80px"
            >
                <layout-header v-if="vData.isRouterAlive" />
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
    </el-container>
</template>

<script>
    import {
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
    import { isQianKun } from '@src/http/utils';
    import { getSystemLicense } from '@src/service/permission';
    import { appCode } from '@src/utils/constant';

    export default {
        components: {
            LayoutSide,
            LayoutHeader,
            LoginDialog,
        },
        setup() {
            const store = useStore();
            const instance = getCurrentInstance();
            const { $bus, $http } = instance.appContext.config.globalProperties;
            const vData = reactive({
                ws:            null,
                isCollapsed:   false,
                isRouterAlive: true,
                members:       [],
                isInQianKun:   window.__POWERED_BY_QIANKUN__ || false,
            });
            const methods = {
                refresh() {
                    setTimeout(_ => {
                        vData.isRouterAlive = false;
                        nextTick(() => {
                            vData.isRouterAlive = true;
                        });
                    }, 1000);
                },
            };

            // 初始化
            const init = async() => {
                const { code, data } = await $http.get({
                    url: '/account/sso_login',
                });

                if (code === 0) {
                    store.commit('UPDATE_USERINFO', data);
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
                if (isQianKun()) {
                    getAppInfo();
                }
                if (!isQianKun()) {
                    init();
                }
                // 默认设置 adminrole & superadminrole 为 true
                const userInfo = {
                    admin_role:       true,
                    super_admin_role: true,
                };

                store.commit('UPDATE_USERINFO', userInfo);

                const asideCollapsedKey = `${appCode()}AsideCollapsed`;

                // collapsed left menus
                const $isCollapsed = window.localStorage.getItem(asideCollapsedKey);

                vData.isCollapsed = $isCollapsed === 'false' ? false : Boolean($isCollapsed);

                $bus.$on('sideCollapsed', asideCollapsed => {
                    vData.isCollapsed = asideCollapsed;
                });
            });

            return {
                vData,
            };
        },
    };
</script>

<style lang="scss" scoped>
    .manager-header {
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
