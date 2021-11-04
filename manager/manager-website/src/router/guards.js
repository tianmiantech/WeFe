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

import { baseIsLogin, setStorage } from './auth';

const prefixPath = process.env.NODE_ENV === 'development' ? '/' : `/${process.env.CONTEXT_ENV}/`;
const blacklist = ['register', 'find-password'];

export default router => {
    router.beforeEach((to, from, next) => {
        const { baseUrl } = window.api;
        const isLogin = baseIsLogin();

        let inited = setStorage().getItem(`${baseUrl}_system_inited`);

        inited == null ? inited = false : inited = JSON.parse(inited);

        if (inited && to.name === 'init') {
            next({ name: 'index', replace: true });
        }

        // matched is all path
        if (to.matched.some(record => record.meta.requiresAuth !== false)) {
            // need to login
            if (isLogin) {
                const { redirect } = from.query;

                if(redirect) {
                    let pathIsBlackList = false;
                    const redirectUrl = decodeURIComponent(redirect);
                    const params = redirectUrl.split('?');
                    const path = params[0];
                    const query = params[1];
                    const queryArr = query && query.length ? query.split(/=|&/) : [];
                    const queryObject = {};

                    queryArr.forEach((val, i) => {
                        if (i % 2 === 0) {
                            queryObject[val] = '';
                        } else {
                            queryObject[queryArr[i - 1]] = val;
                        }
                    });

                    for (let i = 0; i < blacklist.length; i++) {
                        if (path === `${prefixPath}${blacklist[i]}`) {
                            pathIsBlackList = true;
                            break;
                        }
                    }

                    if (pathIsBlackList) {
                        next();
                    } else {
                        if (to.fullPath === redirectUrl) { // break the loop
                            next();
                        } else {
                            next({ path, query: queryObject, replace: true });
                        }
                    }
                } else {
                    if (inited) {
                        next();
                    } else {
                        if (to.fullPath === `${prefixPath}init`) {
                            next();
                        } else {
                            next({
                                name:    'init',
                                replace: true,
                            });
                        }
                    }
                }
            } else {
                next({
                    path:    `${prefixPath}login`,
                    query:   { redirect: encodeURIComponent(to.fullPath) },
                    replace: true,
                });
            }
        } else if (to.matched.some(record => record.meta.requiresLogout)) {
            // logged in and to.path === login
            if (isLogin) {
                next({ name: 'index', replace: true });
            } else {
                next();
            }
        } else {
            // no need to login
            next();
        }
    });

    router.beforeResolve((to, from, next) => {
        const { baseUrl } = window.api;
        const isLogin = baseIsLogin();

        let inited = setStorage().getItem(`${baseUrl}_system_inited`);

        inited == null ? inited = false : inited = JSON.parse(inited);

        if (!isLogin) {
            router.$app.config.globalProperties.$bus.$emit('change-layout-header-title', '');
        } else {
            if (inited) {
                router.$app.config.globalProperties.$bus.$emit('change-layout-header-title', '');
            } else {
                if (to.name !== 'init') {
                    return next({
                        name:    'init',
                        replace: true,
                    });
                }
            }
        }
        next();
    });

    router.afterEach(route => {
        if (route.meta) {
            document.title = route.meta.title || '';
        }
    });
};
