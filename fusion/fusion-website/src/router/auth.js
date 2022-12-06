/*!
 * @author claude
 * User authentication
 */
import { appCode } from '@src/utils/constant';

const prefixPath = process.env.NODE_ENV === 'development' ? '/' : `${process.env.CONTEXT_ENV ? `/${process.env.CONTEXT_ENV}/` : '/'}`;

export const setStorage = () => {
    /* const KEEPALIVE = `${baseUrl}_keepAlive`;

    let keepAlive = localStorage.getItem(KEEPALIVE);

    keepAlive = keepAlive ? JSON.parse(keepAlive) : false;

    return keepAlive ? localStorage : sessionStorage; */
    return window.localStorage;
};

/**
 * clear user cache
 */
export const clearUserInfo = () => {

    setStorage().removeItem(`${appCode()}_userInfo`);
    setStorage().removeItem(`${appCode()}_system_inited`);
};

/**
 * check login state
 */
export const baseIsLogin = () => {
    const userInfo = setStorage().getItem(`${appCode()}_userInfo`);

    if (userInfo) {
        // sync store user info
        syncLogin(userInfo);
    } else {
        clearUserInfo();
    }
    return Boolean(userInfo);
};

/**
 * sync login state
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
 * force to logout
 */
export const baseLogout = async (opt = { redirect: true }) => {
    const { $router, $http } = window.$app;
    const userInfo = setStorage().getItem(`${appCode()}_userInfo`);

    // reset store & localstorage
    if (userInfo) {
        await $http.get({
            url:         `/logout?token=${JSON.parse(userInfo).token}`,
            systemError: false,
        });
    }
    clearUserInfo();
    setStorage().removeItem(`${appCode()}_system_inited`);

    let query = {};

    if (opt.redirect) {
        if ($router.currentRoute.path !== prefixPath) {
            query = {
                redirect: $router.currentRoute.fullPath,
            };
        }
    }

    $router.replace({
        name: 'login',
        query,
    });
};

/**
 * sync login state for all browser tabs
 */
export const syncTabsUserState = async () => {

    window.addEventListener('storage', (e) => {

        if (e.key === setStorage().getItem(`${appCode()}_userInfo`)) {

            if (e.newValue) {
                // logged in
                syncLogin(e.newValue);

            } else {
                baseLogout();
            }
        }
    });
};
