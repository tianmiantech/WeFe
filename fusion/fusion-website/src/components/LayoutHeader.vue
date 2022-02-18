<template>
    <div class="layout-header">
        <div class="heading-bar">
            <span
                ref="heading-title"
                class="heading-title float-left"
                v-html="headingTitle || $route.meta.title"
            />
            <span class="heading-tools">
                <el-tooltip
                    effect="light"
                    content="切换全屏"
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
                        <strong>{{ userInfo.nickname }}</strong>
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
import { mapGetters } from 'vuex';
import { baseLogout } from '@src/router/auth';
import LayoutTags from './LayoutTags.vue';

export default {
    components: {
        LayoutTags,
    },
    data() {
        return {
            headingTitle:   '',
            asideCollapsed: false,
        };
    },
    computed: {
        ...mapGetters(['userInfo', 'tagsList']),
    },
    methods: {
        collapseAside() {
            this.asideCollapsed = !this.asideCollapsed;
            this.$bus.$emit('collapseChanged', this.asideCollapsed);
        },

        handleCommand (command) {
            if (!command) return;
            const self = this;
            const policy = {
                async logout() {
                    await self.$http.post({
                        url: '/logout',
                    });

                    baseLogout();
                },
            };

            policy[command]();
        },

        checkFullScreen() {
            const doc = document;

            return Boolean(
                doc.fullscreenElement ||
                    doc.webkitFullscreenElement ||
                    doc.mozFullScreenElement ||
                    doc.msFullscreenElement,
            );
        },

        fullScreenSwitch() {
            const doc = document;

            if (this.checkFullScreen()) {
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
        },
    },
};
</script>

<style lang="scss">
.heading-bar {
    text-align: right;
    padding: 12px 0;
    height: 60px;
    line-height: 36px;
    .heading-tools {
        display: inline-block;
        padding: 0 10px;
        height: 30px;
        line-height: 30px;
        text-align: center;
        [class*="el-icon-"] {
            width: 30px;
            height: 30px;
            line-height: 36px;
            margin-left: 10px;
            cursor: pointer;
            &:hover {
                transform: scale(1.15);
            }
        }
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
