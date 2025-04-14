/*!
 * @author claude
 * date 07/05/2019
 * 应用程序入口文件
 */
import './public-path';
import Vue from 'vue';
import http from '@src/http/http';
// import request from '@src/utils/request';
import store from '@src/store/store';
import router from '@src/router/index';
import { syncTabsUserState } from '@src/router/auth';
import '@js/polyfill/requestAnimationFrame';
import filters from '@src/utils/filters';
import components from './components';
import App from './App.vue';
import { setCookies } from '@tianmiantech/util';
import { getTokenName } from '@src/utils/tools';
import '@styles/base.scss';

// 自动注册所有组件
Vue.use(components);
// 全局过滤器
filters(Vue);

// 添加 eventbus
Vue.prototype.$bus = new Vue();

// 挂载全局 http
Vue.prototype.$http = http;

/**
 * 应用入口
 */
let app = null;

function render(props = {}) {
    app = new Vue({
        router: router(),
        el:    '#serving-app',
        store: store(),
        created() {
            // 同步多标签用户状态
            syncTabsUserState();
        },
        render: (h) => h(App),
    });
    window.$app = app;
    window.$app._QIANKUN_ENV = props.env;
}

// Mac 系统下使用自带滚动条样式
// 如果不是 Mac 系统就使用自定义滚动条样式
const isMac = /macintosh|mac os x/i.test(navigator.userAgent);

if (!isMac) {
  document.body.classList.add('customed-scrollbar');
}

// 独立运行时
if (!window.__POWERED_BY_QIANKUN__) {
  render();
}

// qiankun 微前端生命周期
 const setUserStorage = (props) => {
    if (!props) return;
    const { masterProps } = props;

    if (masterProps.getToken) {
        setCookies({
            [`${getTokenName()}`]: masterProps.getToken(),
        });
    }
};

/**
 * bootstrap 只会在微应用初始化的时候调用一次，下次微应用重新进入时会直接调用 mount 钩子，不会再重复触发 bootstrap。
 * 通常我们可以在这里做一些全局变量的初始化，比如不会在 unmount 阶段被销毁的应用级别的缓存等。
 */
export async function bootstrap(props) {
    console.log('[serving] 子应用 boostrap props-->');
    setUserStorage(props);
    // console.log('[wefe] 子应用  bootstrap');

}

/**
 * 应用每次进入都会调用 mount 方法，通常我们在这里触发应用的渲染方法
 */
export async function mount(props) {
    console.log('[serving] 子应用 mount props-->', props);
    setUserStorage(props);

    render(props);
}

/**
 * 应用每次 切出/卸载 会调用的方法，通常在这里我们会卸载微应用的应用实例
 */
export async function unmount(props) {
    console.log('[serving] 子应用  unmounnt卸载serving', window.$app);
    if (app) {
        app.$destroy();
        app = null;
    }
}

/**
 * 可选生命周期钩子，仅使用 loadMicroApp 方式加载微应用时生效
 */
export async function update(props) {
    console.log('[fusion] 子应用  update props', props);
}
