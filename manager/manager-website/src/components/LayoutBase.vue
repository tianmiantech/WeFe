<template>
    <el-container :class="{ 'side-collapsed': vData.isCollapsed }">
        <layout-side />

        <el-container>
            <el-header
                ref="layout-header"
                height="90px"
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
    import LayoutSide from './LayoutSide/LayoutSide.vue';
    import LayoutHeader from './LayoutHeader.vue';
    import LoginDialog from './LoginDialog.vue';

    export default {
        components: {
            LayoutSide,
            LayoutHeader,
            LoginDialog,
        },
        setup() {
            const instance = getCurrentInstance();
            const { $bus } = instance.appContext.config.globalProperties;
            const vData = reactive({
                ws:            null,
                isCollapsed:   false,
                isRouterAlive: true,
                members:       [],
            });
            const methods = {
                refresh() {
                    vData.isRouterAlive = false;
                    nextTick(() => {
                        vData.isRouterAlive = true;
                    });
                },
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
            });

            return {
                vData,
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
        z-index: 200;
    }
    .layout-main {
        padding: 20px;
        position: relative;
        min-height: calc(100vh - 120px);
        height: 0;
    }
</style>
