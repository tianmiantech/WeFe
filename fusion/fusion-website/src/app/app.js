/*!
 * @author claude
 */

import './public-path';

import Vue from 'vue';
import http from '@src/http/http';
import store from '@src/store/store';
import routers from '@src/router/index';
// import '@js/polyfill/requestAnimationFrame';
import filters from '@src/utils/filters';
import components from './components';
import App from './App.vue';
import '@styles/base.scss';
import { setCookies } from '@tianmiantech/util';
// import { TOKEN } from '@src/utils/constant';
// import { isQianKun } from '@src/http/utils';


Vue.use(components);

filters(Vue);

Vue.prototype.$http = http;
Vue.prototype.$bus = new Vue();

const isMac = /macintosh|mac os x/i.test(navigator.userAgent);

if (!isMac) {
    document.body.classList.add('customed-scrollbar');
}

let app = null;

let router = null;

function render(props = {}) {
    const { container } = props;

    console.log('render function');
    router = routers();
    app = new Vue({
        store,
        router,
        render: (h) => h(App),
    }).$mount(container ? container.querySelector('#fusion-app') : '#fusion-app');
    window.$app = app;
    window.$app._QIANKUN_ENV = props.env;
}
// 独立运行时
if (!window.__POWERED_BY_QIANKUN__) {
    render();
}

function storeTest(props) {
    props.onGlobalStateChange &&
      props.onGlobalStateChange(
        (value, prev) => console.log(`[onGlobalStateChange - ${props.name}]:`, value, prev),
        true,
      );
    props.setGlobalState &&
      props.setGlobalState({
        ignore: props.name,
        user:   {
          name: props.name,
        },
      });
  }

export async function bootstrap() {
    console.log('[fusion] 子应用  bootstrap');
}

export async function mount(props) {
    console.log('[fusion] 子应用 mount props-->', props);
    const { masterProps = {} } = props || {};
    const { getToken } = masterProps;
    const IAM_USER_TOKEN = `iam-${process.env.HOST_ENV}-x-user-token`;


    setCookies({
        [IAM_USER_TOKEN]: getToken(),
    });
    storeTest(props);
    render(props);
}

export async function unmount(props) {
    console.log('[fusion] 子应用  unmounnt卸载fusion', app);
    if (app) {
        app.$destroy();
        app.$el.innerHTML = '';
        app = null;
        router = null;
        // clearUserInfo();
    }
}

export async function update(props) {
    console.log('[fusion] 子应用  update props', props);
}
