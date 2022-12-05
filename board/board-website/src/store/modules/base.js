import { MENU_LIST,appCode } from '@src/utils/constant';

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
    const { localStorage } = window;
    const USERINFO = `${appCode()}_userInfo`;
    const KEEPALIVE = `${appCode()}_keepAlive`;
    const TAGSLIST = `${appCode()}_tagsList`;
    const SYSTEM_INITED = `${appCode()}_system_inited`;
    const UI_CONFIG = `${appCode()}_uiConfig`;
    const IS_DEMO = `${appCode()}_isDemo`;
    const ADMIN_USER_LIST = `${appCode()}_admin_user_list`;

    let keepAlive = localStorage.getItem(KEEPALIVE),
        userInfo = localStorage.getItem(USERINFO),
        tagsList = localStorage.getItem(TAGSLIST),
        systemInited = localStorage.getItem(SYSTEM_INITED),
        uiConfig = localStorage.getItem(UI_CONFIG),
        isDemo = localStorage.getItem(IS_DEMO),
        adminUserList = localStorage.getItem(ADMIN_USER_LIST),
        menuList = localStorage.getItem(MENU_LIST);

    keepAlive = keepAlive ? parseKey(keepAlive, false) : false;
    userInfo = userInfo ? parseKey(userInfo, {}) : {};
    tagsList = tagsList ? parseKey(tagsList, []) : [];
    systemInited = systemInited ? parseKey(systemInited, false) : false;
    uiConfig = uiConfig ? parseKey(uiConfig, {}) : {};
    isDemo = isDemo ? parseKey(isDemo, false) : false;
    adminUserList = adminUserList ? parseKey(adminUserList, []) : [];
    menuList = menuList ? JSON.parse(menuList) : [];

    const state = {
        keepAlive,
        userInfo,
        tagsList,
        systemInited,
        uiConfig,
        isDemo,
        adminUserList,
        menuList,
        appInfo: {},
    };

    const getters = {
        keepAlive:     state => state.keepAlive,
        tagsList:      state => state.tagsList,
        userInfo:      state => state.userInfo,
        systemInited:  state => state.systemInited,
        uiConfig:      state => state.uiConfig,
        isDemo:        state => state.isDemo,
        adminUserList: state => state.adminUserList,
        menuList:      state => state.menuList,
        appInfo:       state => state.appInfo,
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
        'MENU_LIST'(state, data){
            state.menuList = data;
            setStorage().setItem(MENU_LIST, JSON.stringify(data));
        },
        'APP_INFO'(state, data) {
            state.appInfo = data;
        },
    };

    return {
        getters,
        mutations,
        state,
    };
};
