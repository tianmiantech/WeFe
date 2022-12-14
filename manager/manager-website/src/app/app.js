/*!
 * @author claude
 * date 07/05/2021
 * app entry
 */

import './public-path';
import { createApp, h } from 'vue';
import store from '@src/store/store';
import baseRoutes from '@src/router/routes';
import components from './components';
import App from './App.vue';
import { clearUserInfo } from '../router/auth';
import { setCookies } from '@tianmiantech/util';
import { isQianKun } from '@src/http/utils';
import { createRouter, createWebHistory } from 'vue-router';


// use origin scrollbar style for MacOS
const isMac = /macintosh|mac os x/i.test(navigator.userAgent);

if (!isMac) {
    document.body.classList.add('customed-scrollbar');
}

let app = null;

let router = null;

let history = null;

function render(props = {}) {
    const { container } = props;

    console.log('render manager');

    const base = window.__POWERED_BY_QIANKUN__ ? '/portal' : '/';

    history = createWebHistory(base),

     router = createRouter({
        routes: baseRoutes(),
        history,
        scrollBehavior() {
            return { x: 0, y: 0 };
        },
    });
    // create app

    app = createApp(App);

    // add router/vuex
    /**
     * 因为render会运行两次，为了避免内存影响加载，所以将router改造成函数的形式
     */
    app.use(router);
    app.use(store(app));
    app.mount(container ? container.querySelector('#manager-app') : '#manager-app');

    console.log('render success');
    // global error handler
    app.config.errorHandler = ({ message }, vm, info) => {
        // app.config.globalProperties.$message.error(`发生错误: ${message}, 请刷新重试`);
    };
    // add app to router
    // router.$app = app;
    // 存储上级系统环境
    app._QIANKUN_ENV = props.env;
    // add router guards
    // guards(router);
    // install components automatically
    components.install(app);

    window.$app = app;
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

/**
 * bootstrap 只会在微应用初始化的时候调用一次，下次微应用重新进入时会直接调用 mount 钩子，不会再重复触发 bootstrap。
 * 通常我们可以在这里做一些全局变量的初始化，比如不会在 unmount 阶段被销毁的应用级别的缓存等。
 */
export async function bootstrap() {
    console.log('[manager] 子应用  bootstrap');
}

/**
 * 应用每次进入都会调用 mount 方法，通常我们在这里触发应用的渲染方法
 */
export async function mount(props) {
    console.log('[manager] 子应用 mount props-->', props);
    const { masterProps = {} } = props || {};
    const { getToken } = masterProps;
    const IAM_USER_TOKEN = `iam-${process.env.HOST_ENV}-x-user-token`;

    setCookies({
        [IAM_USER_TOKEN]: getToken(),
    });
    storeTest(props);
    render(props);
    app.config.globalProperties.$onGlobalStateChange = props.onGlobalStateChange;
    app.config.globalProperties.$setGlobalState = props.setGlobalState;
}

/**
 * 应用每次 切出/卸载 会调用的方法，通常在这里我们会卸载微应用的应用实例
 */
export async function unmount(props) {
    console.log('[manager] 子应用  unmounnt卸载wefe', app, window.$app);
    if (app) {
        app.unmount();
        app._container.innerHTML = '';
        app = null;
        router = null;
        history.destroy();
        // 父级系统退出登录后，子系统清空缓存中的用户信息
        clearUserInfo();
    }
}

/**
 * 可选生命周期钩子，仅使用 loadMicroApp 方式加载微应用时生效
 */
export async function update(props) {
    console.log('[manager] 子应用  update props', props);
}
