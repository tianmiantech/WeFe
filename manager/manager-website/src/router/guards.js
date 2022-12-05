/*!
 * @author claude
 * global router guards
 */

/**
 * thinking path:
 * - get to.meta go different path
 *      1, need to login
 *          1.1 logged in
 *              1.1.1 url has redirect
 *                  1.1.1.1 redirect has params
 *                          to.path === redirect => next
 *                  1.1.1.2 no params
 *                          next => redirect
 *          1.2 not login
 *              next => login
 *      2, no need to login
 *          next
 *          logged in and to.path === login or like register, go index
 */

import { setStorage } from './auth';
import { MENU_LIST } from '@src/utils/constant';
import { isQianKun } from '@src/http/utils';

import { prefixPath } from './routes';

const whiteList = [`${prefixPath()}home`];

export default router => {
    router.beforeEach((to, from, next) => {
        const menuList = setStorage().getItem(MENU_LIST) || '[]';
        const authority = JSON.parse(menuList) || [];

        if(whiteList.includes(to.path)){
            return next();
        }

        const isExits = (name) => authority.includes(name);

        if(isExits(to.name) || !to.name || !isQianKun()){
            return next();
        } else {
            // window.$app.config.globalProperties.$message.error({ message: '页面不存在或权限未开放' });
            return next({ path: `${prefixPath()}home` });
        }
    });

    // router.beforeResolve((to, from, next) => {
    //     router.$app.config.globalProperties.$bus.$emit('change-layout-header-title', '');
    //     next();
    // });

    router.afterEach(route => {
        if (route.meta) {
            document.title = route.meta.title || '';
        }
    });
};
