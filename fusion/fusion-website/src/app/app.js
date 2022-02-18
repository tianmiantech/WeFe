/*!
 * @author claude
 */

import Vue from 'vue';
import http from '@src/http/http';
import store from '@src/store/store';
import router from '@src/router/index';
// import '@js/polyfill/requestAnimationFrame';
import filters from '@src/utils/filters';
import components from './components';
import App from './App.vue';
import '@styles/base.scss';

Vue.use(components);

filters(Vue);

Vue.prototype.$http = http;
Vue.prototype.$bus = new Vue();

const context = process.env.CONTEXT_ENV && process.env.CONTEXT_ENV.replace(/\//g, '');
const tail = process.env.NODE_ENV === 'production' && process.env.TAIL ? `-${context.substr(context.length - 2)}` : '';
const proxyPrefix = process.env.NODE_ENV === 'development' ? '/api' : process.env[`API_${process.env.DEPLOY_ENV.toUpperCase()}`] + `${tail}`;
const prefixPath = process.env.NODE_ENV === 'development' ? '/' : `${process.env.CONTEXT_ENV}`;

window.api = {
    env:     process.env.DEPLOY_ENV,
    baseUrl: proxyPrefix,
    prefixPath,
};

window.$app = new Vue({
    el:     '#app',
    store,
    router,
    render: h => h(App),
});

const isMac = /macintosh|mac os x/i.test(navigator.userAgent);

if (!isMac) {
    document.body.classList.add('customed-scrollbar');
}
