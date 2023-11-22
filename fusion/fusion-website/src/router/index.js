/*!
 * @author claude
 */

import Vue from 'vue';
import Router from 'vue-router';
import baseRoutes from './routes';
import guards from './guards';

const originalPush = Router.prototype.push;
const originalReplace = Router.prototype.replace;

Router.prototype.push = function push(location) {
    return originalPush.call(this, location).catch(err => err);
};
Router.prototype.replace = function replace(location) {
    return originalReplace.call(this, location).catch(err => err);
};

Vue.use(Router);

const router = new Router({
    routes: baseRoutes,
    mode:   'history',
    scrollBehavior() {
        return { x: 0, y: 0 };
    },
});

// 路由守卫
guards(router);

export default router;
