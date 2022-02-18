/*!
 * @author claude
 * User authentication
 */

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
    const { baseUrl } = window.api;

    setStorage().removeItem(`${baseUrl}_userInfo`);
};

/**
 * check login state
 */
export const baseIsLogin = () => {
    const { baseUrl } = window.api;
    const userInfo = setStorage().getItem(`${baseUrl}_userInfo`);

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
export const baseLogout = (opt = { redirect: true }) => {
    const { baseUrl } = window.api;

    // reset store & localstorage
    clearUserInfo();
    setStorage().removeItem(`${baseUrl}_system_inited`);

    let query = {};
    const { $router } = window.$app;

    if(opt.redirect) {
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

        const { baseUrl } = window.api;

        if (e.key === setStorage().getItem(`${baseUrl}_userInfo`)) {

            if (e.newValue) {
                // logged in
                syncLogin(e.newValue);

            } else {
                baseLogout();
            }
        }
    });
};
