<template>
    <el-container
        :class="{
            'side-collapsed': isCollapsed,
        }"
        :style="{height: isInQianKun ? 'calc(100vh - var(--tm-header-height))' : '100%'}"
    >
        <layout-side :is-collapsed="isCollapsed" />

        <el-container>
            <el-header
                ref="layout-header"
                height="80px"
            >
                <layout-header />
            </el-header>

            <el-main class="layout-main">
                <div class="base-wrapper">
                    <transition name="fade">
                        <!-- <keep-alive /> -->
                        <router-view v-if="isRouterAlive" />
                    </transition>
                </div>
            </el-main>
        </el-container>
        <LoginDialog />
    </el-container>
</template>

<script>
import LayoutSide from '../components/LayoutSide/LayoutSide.vue';
import LayoutHeader from '../components/LayoutHeader.vue';
import LoginDialog from '../components/LoginDialog.vue';
import { getSystemLicense } from '@src/service/permission';

export default {
    name:       'App',
    components: {
        LayoutSide,
        LayoutHeader,
        LoginDialog,
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
            isInQianKun:   window.__POWERED_BY_QIANKUN__ || false,
        };
    },
    created() {
        this.init();

        if (this.isInQianKun) {
            this.getAppInfo();
        }
        this.$nextTick(() => {
            this.loading = false;
        });

        this.isCollapsed = window.localStorage.getItem('AsideCollapsed');

        this.$bus.$on('collapseChanged', (asideCollapsed) => {
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
            let { code, data } = await this.$http.get({
                url: '/account/sso_login',
            });

            data = data || {};

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
    position: relative;
    z-index: 6;
    background: $header-background;
    border-bottom: 1px solid $border-color-base;
}
.layout-main {
    position: relative;
    height: calc(100vh - 120px);
}
.id {
    color: #666;
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
