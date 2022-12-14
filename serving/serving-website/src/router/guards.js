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
import { isQianKun } from '../http/utils';



import {prefixPath} from './routes';
const whiteList = [`${prefixPath()}home`, `${prefixPath()}init`];

export default router => {
    router.beforeEach((to, from, next) => {
        const menuList = setStorage().getItem(MENU_LIST) || '[]';
        const authority = JSON.parse(menuList) || [];
        const isExits = (name) => authority.includes(name);

        if(whiteList.includes(to.path) || !isQianKun()){
            return next();
        }

        if(isExits(to.name) || !to.name){
            return next();
        } else {
            // window.$app.$message.error({ message: '页面不存在或权限未开放' });
            return next({ name: 'service-list' });
        }

    });

    router.afterEach(route => {
        if (route.meta) {
            document.title = route.meta.title || '';
        }
    });
};
