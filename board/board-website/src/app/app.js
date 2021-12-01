/*!
 * @author claude
 * date 07/05/2021
 * app entry
 */

import { createApp, h } from 'vue';
import store from '@src/store/store';
import router from '@src/router/index';
import guards from '@src/router/guards';
import { syncTabsUserState } from '@src/router/auth';
import components from './components';
import App from './App.vue';

const context = process.env.CONTEXT_ENV.replace(/\//g, '');
const tail = process.env.NODE_ENV === 'production' && process.env.TAIL ? `-${context.substr(context.length - 2)}` : '';
const proxyPrefix = process.env.NODE_ENV === 'development' ? '/api' : process.env[`VUE_APP_${process.env.DEPLOY_ENV.toUpperCase()}`] + `${!process.env.DEPLOY_ENV.includes('prod') ? tail : ''}`; // http(s) url + context, e.g. https://test.com/[context/]login
const prefixPath = process.env.NODE_ENV === 'development' ? '/' : `${process.env.CONTEXT_ENV}`;

// global api
// export window.clientApi to index.html
window.api = {
    env:        window.clientApi ? window.clientApi.env : process.env.DEPLOY_ENV,
    baseUrl:    window.clientApi ? window.clientApi.baseUrl : proxyPrefix,
    prefixPath: window.clientApi ? window.clientApi.prefixPath : prefixPath,
};

// create app
const app = createApp({
    mounted () {
        syncTabsUserState();
    },
    render: () => h(App),
});

// add router/vuex
app.use(components)
    .use(router)
    .use(store(app))
    .mount('#app');
// global error handler
app.config.errorHandler = ({ message }, vm, info) => {
    setTimeout(() => {
        app.config.globalProperties.$message.error(`发生错误: ${message}, 请刷新重试`);
    }, 200);
};
// add app to router
router.$app = app;
// add router guards
guards(router);

window.$app = app;

// use origin scrollbar style for MacOS
const isMac = /macintosh|mac os x/i.test(navigator.userAgent);

if (!isMac) {
    document.body.classList.add('customed-scrollbar');
}
