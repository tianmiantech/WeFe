<template>
    <el-container
        :class="{ 'side-collapsed': isCollapsed }"
        :style="{height: isQiankun ? 'calc(100vh - var(--tm-header-height))' : '100%'}"
    >
        <!-- 侧边栏开始 -->
        <layout-side :is-collapsed="isCollapsed" />
        <!-- 侧边栏结束 -->

        <el-container>
            <!-- 头部开始 -->
            <el-header
                ref="layout-header"
                height="80px"
            >
                <layout-header />
            </el-header>
            <!-- 头部结束 -->

            <!-- 主体开始 -->
            <el-main
                id="layout-main"
                class="layout-main"
            >
                <div class="base-wrapper">
                    <transition name="fade">
                        <!-- <keep-alive /> -->
                        <router-view v-if="isRouterAlive" />
                    </transition>
                </div>
            </el-main>
            <!-- 主体结束 -->
        </el-container>
    </el-container>
</template>

<script>
import LayoutSide from '../components/LayoutSide/LayoutSide.vue';
import LayoutHeader from '../components/LayoutHeader.vue';
import { getSystemLicense } from '@src/service/permission';
import { isQianKun } from '@src/http/utils';

export default {
    name:       'App',
    components: {
        LayoutSide,
        LayoutHeader,
    },
    provide() {
        return {
            refresh: this.refresh,
        };
    },
    data() {
        return {
            isRouterAlive: true,
            isCollapsed:   false,
            loading:       true,
            isQiankun:     isQianKun(),
        };
    },
    created() {
        this.init();

        if (this.isQiankun) {
            this.getAppInfo();
        }

        this.$nextTick(() => {
            this.loading = false;
        });

        // 左侧菜单折叠状态
        this.isCollapsed = window.localStorage.getItem('AsideCollapsed');

        this.$bus.$on('collapseChanged', asideCollapsed => {
            this.isCollapsed = asideCollapsed;
            window.localStorage.setItem('AsideCollapsed', asideCollapsed);
        });
    },
    methods: {
        refresh() {
            this.isRouterAlive = false;
            this.$nextTick(() => {
                this.isRouterAlive = true;
            });
        },
        async init() {
            const { code, data } = await this.$http.get({
                url: '/account/sso_login',
            });

            if (code === 10000) {
                this.$store.commit('SYSTEM_INITED', false);
                this.$store.commit('UPDATE_USERINFO', data);

                this.$router.replace({
                    name: 'init',
                });
            } else if (code === 0) {
                data.admin_role = true;
                data.super_admin_role = true;
                this.$store.commit('UPDATE_USERINFO', data);
                this.$store.commit('SYSTEM_INITED', true);
            }
        },
        async getAppInfo() {
            const data = await getSystemLicense();

            if (data) {
                this.$store.commit('APP_INFO', data);
            }
        },
    },
};
</script>

<style lang="scss">
.el-header {
    color: #000;
    // position: fixed;
    // top: 0;
    // right: 0;
    // left: 200px;
    // z-index: 200;
    padding: 0;
    background: $header-background;
    border-bottom: 1px solid $border-color-base;
}
// .layout-main {
//     position: relative;
//     padding: 84px 20px 20px;
// }
.layout-main {
    position: relative;
    height: calc(100vh - 120px);
    overflow: auto;
}
.id {
    color: #999;
    font-weight: 100;
    font-size: 12px;
}
.blod {
    font-weight: bold;
}
.empty-data {
    margin-top: 50px;
    margin-bottom: 50px;

    .empty-data-img {
        max-height: 180px;
    }
    .empty-data-message {
        font-size: 24px;
        font-weight: bold;
    }
}
</style>
