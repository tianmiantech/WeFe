function setStorage () {
    /* let keepAlive = localStorage.getItem(KEEPALIVE);

    keepAlive = keepAlive ? JSON.parse(keepAlive) : false;

    return keepAlive ? localStorage : sessionStorage; */
    return window.localStorage;
}

function parseKey (key, defaultType = null) {
    try {
        return JSON.parse(key);
    } catch (e) {
        return defaultType;
    }
}

export default _ => {
    /**
    * Distinguish between multiple environments
    */
    const { baseUrl } = window.api;
    const { localStorage } = window;
    const USERINFO = `${baseUrl}_userInfo`;
    const KEEPALIVE = `${baseUrl}_keepAlive`;
    const TAGSLIST = `${baseUrl}_tagsList`;
    const SYSTEM_INITED = `${baseUrl}_system_inited`;
    const UI_CONFIG = `${baseUrl}_uiConfig`;
    const IS_DEMO = `${baseUrl}_isDemo`;
    const ADMIN_USER_LIST = `${baseUrl}_admin_user_list`;

    let keepAlive = localStorage.getItem(KEEPALIVE),
        userInfo = localStorage.getItem(USERINFO),
        tagsList = localStorage.getItem(TAGSLIST),
        systemInited = localStorage.getItem(SYSTEM_INITED),
        uiConfig = localStorage.getItem(UI_CONFIG),
        isDemo = localStorage.getItem(IS_DEMO),
        adminUserList = localStorage.getItem(ADMIN_USER_LIST);

    keepAlive = keepAlive ? parseKey(keepAlive, false) : false;
    userInfo = userInfo ? parseKey(userInfo, {}) : {};
    tagsList = tagsList ? parseKey(tagsList, []) : [];
    systemInited = systemInited ? parseKey(systemInited, false) : false;
    uiConfig = uiConfig ? parseKey(uiConfig, {}) : {};
    isDemo = isDemo ? parseKey(isDemo, false) : false;
    adminUserList = adminUserList ? parseKey(adminUserList, []) : [];

    const state = {
        keepAlive,
        userInfo,
        tagsList,
        systemInited,
        uiConfig,
        isDemo,
        adminUserList,
    };

    const getters = {
        keepAlive:     state => state.keepAlive,
        tagsList:      state => state.tagsList,
        userInfo:      state => state.userInfo,
        systemInited:  state => state.systemInited,
        uiConfig:      state => state.uiConfig,
        isDemo:        state => state.isDemo,
        adminUserList: state => state.adminUserList,
    };

    const mutations = {
        'KEEP_ALIVE' (state, data) {
            state.keepAlive = data;
            setStorage().setItem(KEEPALIVE, JSON.stringify(data));
        },
        'UPDATE_USERINFO' (state, data) {
            state.userInfo = data;
            setStorage().setItem(USERINFO, data ? JSON.stringify(data) : '');
        },
        // update tag list
        'UPDATE_TAGSLIST' (state, list) {
            state.tagsList = list || [];
            setStorage().setItem(TAGSLIST, JSON.stringify(list));
        },
        'SYSTEM_INITED' (state, data) {
            state.systemInited = data;
            setStorage().setItem(SYSTEM_INITED, JSON.stringify(data));
        },
        'UI_CONFIG' (state, data) {
            state.uiConfig = data;
            setStorage().setItem(UI_CONFIG, JSON.stringify(data));
        },
        'IS_DEMO' (state, data) {
            state.isDemo = data;
            setStorage().setItem(IS_DEMO, JSON.stringify(data));
        },
        'ADMIN_USER_LIST' (state, data) {
            state.adminUserList = data;
            setStorage().setItem(ADMIN_USER_LIST, JSON.stringify(data));
        },
    };

    return {
        getters,
        mutations,
        state,
    };
};
