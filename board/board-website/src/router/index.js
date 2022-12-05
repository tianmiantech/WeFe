/*!
 * @author claude
 * global router
 */

import { createRouter, createWebHistory } from 'vue-router';
// import { appCode } from '../utils/constant';
import guards from './guards';
import baseRoutes from './routes';

function createRoute(){
    const base = window.__POWERED_BY_QIANKUN__ ? `/portal/` : `/`;

    const router = createRouter({
        routes:  baseRoutes(),
        history: createWebHistory(base),
        // scrollBehavior (to, from, savedPosition) {
        //     const scrollBehavior = {
        //         el:       '#layout-main',
        //         top:      savedPosition ? savedPosition.top : 0,
        //         left:     savedPosition ? savedPosition.left : 0,
        //         behavior: 'smooth',
        //     };

        //     return new Promise((resolve, reject) => {
        //         setTimeout(() => {
        //             const el = document.getElementById('layout-main');

        //             el && el.scrollTo(scrollBehavior);
        //             resolve();
        //         }, 200);
        //     });
        // },
        scrollBehavior() {
            return { x: 0, y: 0 };
        },
    });

    // guards(router)
    return router;
}

export default createRoute;
