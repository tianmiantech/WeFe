/*!
 * @author claude
 * date 07/05/2019
 * 应用程序入口文件
 */

import Vue from 'vue';
import http from '@src/http/http';
import store from '@src/store/store';
import router from '@src/router/index';
import { syncTabsUserState } from '@src/router/auth';
import '@js/polyfill/requestAnimationFrame';
import filters from '@src/utils/filters';
import components from './components';
import App from './App.vue';
import '@styles/base.scss';


// 自动注册所有组件
Vue.use(components);
// 全局过滤器
filters(Vue);

// 挂载全局 http
Vue.prototype.$http = http;

// 添加 eventbus
Vue.prototype.$bus = new Vue();

const context = process.env.CONTEXT_ENV && process.env.CONTEXT_ENV.replace(/\//g, '');
const tail = process.env.NODE_ENV === 'production' && process.env.TAIL ? `-${context.substr(context.length - 2)}` : '';
const proxyPrefix = process.env.NODE_ENV === 'development' ? '/api' : process.env[`VUE_APP_${process.env.DEPLOY_ENV.toUpperCase()}`] + `${tail}`;
const prefixPath = process.env.NODE_ENV === 'development' ? '/' : `${process.env.CONTEXT_ENV}`;

// 挂载全局 api 变量
window.api = {
    env:        window.clientApi ? window.clientApi.env : process.env.DEPLOY_ENV,
    baseUrl:    window.clientApi ? window.clientApi.baseUrl : proxyPrefix,
    prefixPath: window.clientApi ? window.clientApi.prefixPath : prefixPath,
};

/**
 * 应用入口
 */
window.$app = new Vue({
    router,
    el:    '#app',
    store: store(),
    created() {
        // 同步多标签用户状态
        syncTabsUserState();
    },
    render: h => h(App),
});

// Mac 系统下使用自带滚动条样式
// 如果不是 Mac 系统就使用自定义滚动条样式
const isMac = /macintosh|mac os x/i.test(navigator.userAgent);

if (!isMac) {
    document.body.classList.add('customed-scrollbar');
}
