/*!
 * @author claude
 * global router
 */

import { createRouter, createWebHistory } from 'vue-router';
import baseRoutes from './routes';

const router = createRouter({
    routes:  baseRoutes,
    history: createWebHistory(),
    scrollBehavior (to, from, savedPosition) {
        const scrollBehavior = {
            el:       '#layout-main',
            top:      savedPosition ? savedPosition.top : 0,
            left:     savedPosition ? savedPosition.left : 0,
            behavior: 'smooth',
        };

        return new Promise((resolve, reject) => {
            setTimeout(() => {
                const el = document.getElementById('layout-main');

                el && el.scrollTo(scrollBehavior);
                resolve();
            }, 200);
        });
    },
});

export default router;
