<template>
    <el-container :class="{ 'side-collapsed': isCollapsed }">
        <layout-side :is-collapsed="isCollapsed" />

        <el-container style="margin-left: 200px">
            <el-header ref="layout-header">
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
        };
    },
    created() {
        this.$nextTick(() => {
            this.loading = false;
        });

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
    },
};
</script>

<style lang="scss">
.el-header {
    color: #000;
    position: fixed;
    top: 0;
    right: 0;
    left: 200px;
    z-index: 200;
    background: $header-background;
    border-bottom: 1px solid $border-color-base;
}
.layout-main {
    position: relative;
    padding: 84px 20px 20px;
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
