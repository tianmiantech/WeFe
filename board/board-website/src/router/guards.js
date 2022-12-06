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
import {prefixPath} from './routes';

const whiteList = ['portal/login', `${prefixPath()}home`,'/portal/manager/', `${prefixPath()}init`];

export default router => {
    router.beforeEach((to, from, next) => {
        const menuList = setStorage().getItem(MENU_LIST) || '[]';
        const authority = JSON.parse(menuList) || [];

        console.log(to.path, 'to.path');
        if(whiteList.includes(to.path)){
            /** 白名单，直接跳 */
            return next();
        }

        // if(!isLogin){
        //     // 未登录 且在微前端中，跳回登录页
        //     if (window.__POWERED_BY_QIANKUN__){
        //             window.location.href = `/portal/login?tenantid=${localStorage.getItem('tenantid')}`;
        //     }
        // }

        const isExits = (name) => authority.includes(name);

        console.log('isExits board', isExits(to.name), to.name, to.path);
        console.log(isQianKun());
        if(isExits(to.name) || !to.name || !isQianKun()){
            next();
        } else {
            // window.$app.config.globalProperties.$message.error({ message: '页面不存在或权限未开放' });
            next({ name: `home` });
        }
    });

    router.afterEach(route => {
        if (route.meta) {
            document.title = route.meta.title || '';
        }
    });
};
