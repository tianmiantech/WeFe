
export default _ => {

    function setStorage () {
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

    let keepAlive = localStorage.getItem(KEEPALIVE),
    userInfo = localStorage.getItem(USERINFO),
    tagsList = localStorage.getItem(TAGSLIST);

    keepAlive = keepAlive ? JSON.parse(keepAlive) : false;
    userInfo = userInfo ? JSON.parse(userInfo) : {};
    tagsList = tagsList ? JSON.parse(tagsList) : [];

    const state = {
        keepAlive,
        userInfo,
        tagsList,
    };

    const getters = {
        keepAlive: state => state.keepAlive,
        tagsList:  state => state.tagsList,
        userInfo:  state => state.userInfo,
    };

    const mutations = {
        'KEEP_ALIVE' (state, data) {
            state.keepAlive = data;
            localStorage.setItem(KEEPALIVE, JSON.stringify(data));
        },
        'UPDATE_USERINFO' (state, data) {
            state.userInfo = data;
            setStorage().setItem(USERINFO, JSON.stringify(data));
        },
        // 更新标签栏
        'UPDATE_TAGSLIST' (state, list) {
            state.tagsList = list;
            setStorage().setItem(TAGSLIST, JSON.stringify(list));
        },
    };

    return {
        getters,
        mutations,
        state,
    };
};
