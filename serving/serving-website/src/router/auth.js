/*!
 * @author claude
 * 用户认证相关
 */

import { appCode } from '@src/utils/constant';
const prefixPath = '/';

export const setStorage = () => {
    return localStorage;
};

/**
 * 清空用户信息
 */
export const clearUserInfo = () => {
    setStorage().removeItem(`${appCode()}_userInfo`);
    setStorage().removeItem(`${appCode()}_system_inited`);
};

/**
 * 检测登录状态
 */
export const baseIsLogin = () => {
    const userInfo = setStorage().getItem(`${appCode()}_userInfo`);

    if (userInfo) {
        // 同步存储信息
        syncLogin(userInfo);
    } else {
        clearUserInfo();
    }
    return Boolean(userInfo);
};

/**
 * 同步登录
 */
export const syncLogin = async (userInfo = {}) => {

    if (userInfo.token) {
        const { $route, $router } = window.$app;

        if ($route.meta.requiresLogout) {
            const { href } = window.location;
            const url = window.decodeURIComponent(href);
            const target = url.split('?redirect=')[1];

            $router.replace(target || prefixPath);
        } else {
            window.location.reload();
        }
    }
};

/**
 * 强制用户下线, 清除登录信息并跳转到登录页面
 */
export const baseLogout = async () => {

    const { $router, $http } = window.$app;
    const { location: { href, pathname } } = window;
    const userInfo = setStorage().getItem(`${appCode()}_userInfo`);

    // 重置 store 和 localstorage
    if (userInfo) {
        await $http.get({
            url:         `/logout?token=${JSON.parse(userInfo).token}`,
            systemError: false,
        });
    }
    clearUserInfo();
    setStorage().removeItem(`${appCode()}_system_inited`);

    let query = {};

    if ($router.currentRoute.path !== prefixPath && $router.currentRoute.path !== pathname && !href.includes('?redirect=')) {
        query = {
            redirect: $router.currentRoute.fullPath,
        };
    }

    $router.replace({
        name: 'login',
        query,
    });
};

/**
 * 同步多标签用户状态
 */
export const syncTabsUserState = async () => {

    // 未登录
    window.addEventListener('storage', (e) => {

        if (e.key === setStorage().getItem(`${appCode()}_userInfo`)) {

            if (e.newValue) {
                // 已登录
                syncLogin(e.newValue);

            } else {
                // 未登录或已过期
                baseLogout();
            }
        }
    });
};
