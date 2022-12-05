<template>
    <el-config-provider :locale="locale" namespace="board">
        <router-view />
    </el-config-provider>
</template>

<script>
    import 'element-plus/theme-chalk/el-message.css';
    import 'element-plus/theme-chalk/el-message-box.css';
    import 'element-plus/theme-chalk/el-notification.css';
    import { defineComponent } from 'vue';
    import { ElConfigProvider } from 'element-plus';
    import zhCn from 'element-plus/lib/locale/lang/zh-cn';

    export default defineComponent({
        name:       'App',
        components: {
            ElConfigProvider,
        },
        data() {
            return {
                locale: zhCn,
            };
        },
    });
</script>

<style lang="scss">
  #board-app {
    // height: calc(100vh - var(--tm-header-height));
    height: 100vh;
    background: $layout-main-background;
    font-family: 'Avenir', Helvetica, Arial, sans-serif;
    -webkit-font-smoothing: antialiased;
    -moz-osx-font-smoothing: grayscale;
    // position: relative;
    z-index: 100;
  }
</style>
