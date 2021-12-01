<template>
    <el-config-provider :locale="locale">
        <router-view />
    </el-config-provider>
</template>

<script>
    import 'element-plus/theme-chalk/el-message.css';
    import 'element-plus/theme-chalk/el-message-box.css';
    import 'element-plus/theme-chalk/el-notification.css';
    import { ElConfigProvider } from 'element-plus';
    import zhCn from 'element-plus/lib/locale/lang/zh-cn';

    export default {
        name:       'App',
        components: {
            [ElConfigProvider.name]: ElConfigProvider,
        },
        data() {
            return {
                locale: zhCn,
            };
        },
    };
</script>

<style lang="scss">
  #app {
    height: 100%;
    background: $layout-main-background;
    font-family: 'Avenir', Helvetica, Arial, sans-serif;
    -webkit-font-smoothing: antialiased;
    -moz-osx-font-smoothing: grayscale;
  }
</style>
