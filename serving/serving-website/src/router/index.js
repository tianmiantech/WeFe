/*!
 * @author claude
 * date 07/05/2019
 * 全局路由入口
 */

import Vue from 'vue';
import Router from 'vue-router';
import baseRoutes from './routes';
import guards from './guards';

// 防止路由重复跳转报错
const originalPush = Router.prototype.push;
const originalReplace = Router.prototype.replace;

Router.prototype.push = function push(location) {
  return originalPush.call(this, location).catch((err) => err);
};
Router.prototype.replace = function replace(location) {
  return originalReplace.call(this, location).catch((err) => err);
};

Vue.use(Router);

function createRouter(){
  const base = window.__POWERED_BY_QIANKUN__
  ? '/portal/'
  : '/';

  // 实例化路由
  const router = new Router({
    routes: baseRoutes(),
    base,
    mode:   'history',
    scrollBehavior() {
      return { x: 0, y: 0 };
    },
});

// 路由守卫
// guards(router);
return router
}

export default createRouter;
