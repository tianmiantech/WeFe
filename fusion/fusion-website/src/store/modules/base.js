
export default _ => {

    function setStorage () {
        /* let keepAlive = localStorage.getItem(KEEPALIVE);

        keepAlive = keepAlive ? JSON.parse(keepAlive) : false;

        return keepAlive ? localStorage : sessionStorage; */
        return localStorage;
    }

    /* 此处加上后台接口地址作为 存储对象的前缀
    * 用于解决在测试环境中多节点使用相同域名造成 localStorage 数据覆盖的问题
    */
    const { baseUrl } = window.api;
    const { localStorage } = window;
    const USERINFO = `${baseUrl}_userInfo`;
    const KEEPALIVE = `${baseUrl}_keepAlive`;
    const TAGSLIST = `${baseUrl}_tagsList`;
    const SYSTEM_INITED = `${baseUrl}_system_inited`;

    let keepAlive = localStorage.getItem(KEEPALIVE),
        userInfo = localStorage.getItem(USERINFO),
        tagsList = localStorage.getItem(TAGSLIST),
        systemInited = localStorage.getItem(SYSTEM_INITED);

    keepAlive = keepAlive ? JSON.parse(keepAlive) : false;
    userInfo = userInfo ? JSON.parse(userInfo) : {};
    tagsList = tagsList ? JSON.parse(tagsList) : [];
    systemInited = systemInited ? JSON.parse(systemInited) : false;

    const state = {
        keepAlive,
        userInfo,
        tagsList,
        systemInited,
    };

    const getters = {
        keepAlive:    state => state.keepAlive,
        tagsList:     state => state.tagsList,
        userInfo:     state => state.userInfo,
        systemInited: state => state.systemInited,
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
        'UPDATE_TAGSLIST' (state, list) {
            state.tagsList = list;
            setStorage().setItem(TAGSLIST, JSON.stringify(list));
        },
        'SYSTEM_INITED' (state, data) {
            state.systemInited = data;
            setStorage().setItem(SYSTEM_INITED, JSON.stringify(data));
        },
    };

    return {
        getters,
        mutations,
        state,
    };
};
