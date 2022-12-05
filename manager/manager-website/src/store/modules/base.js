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
    // const SYSTEM_INITED = `${baseUrl}_system_inited`;

    let keepAlive = localStorage.getItem(KEEPALIVE),
        userInfo = localStorage.getItem(USERINFO),
        tagsList = localStorage.getItem(TAGSLIST);
        // systemInited = localStorage.getItem(SYSTEM_INITED);

    let menuList = localStorage.getItem(MENU_LIST);

    keepAlive = keepAlive ? parseKey(keepAlive, false) : false;
    userInfo = userInfo ? parseKey(userInfo, {}) : {};
    tagsList = tagsList ? parseKey(tagsList, []) : [];
    // systemInited = systemInited ? parseKey(systemInited, false) : false;
    menuList = menuList ? JSON.parse(menuList) : [];

    const state = {
        keepAlive,
        userInfo,
        tagsList,
        // systemInited,
        menuList,
        appInfo: {},
    };

    const getters = {
        keepAlive:    state => state.keepAlive,
        tagsList:     state => state.tagsList,
        userInfo:     state => state.userInfo,
        systemInited: state => state.systemInited,
        menuList:     state => state.menuList,
        appInfo:      state => state.appInfo,
    };

    const mutations = {
        'KEEP_ALIVE' (state, data) {
            state.keepAlive = data;
            setStorage().setItem(KEEPALIVE, JSON.stringify(data));
        },
        'UPDATE_USERINFO' (state, data) {
            state.userInfo = data;
            setStorage().setItem(USERINFO, JSON.stringify(data));
        },
        // update tag list
        'UPDATE_TAGSLIST' (state, list) {
            state.tagsList = list || [];
            setStorage().setItem(TAGSLIST, JSON.stringify(list));
        },
        /* 'SYSTEM_INITED' (state, data) {
            state.systemInited = data;
            setStorage().setItem(SYSTEM_INITED, JSON.stringify(data));
        }, */
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
