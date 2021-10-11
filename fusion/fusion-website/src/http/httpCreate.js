/*!
 * @author claude
 */

import axios from 'axios';
import { deepMerge } from '@src/utils/types';

const cancelTokenQueue = {};
const httpInstance = axios.create({
    baseURL: '',
    timeout: 1000 * 60,
    headers: {},
    // withCredentials: true,
    // responseType: 'json', // default
    // responseEncoding: 'utf8', // default
    // xsrfCookieName: 'XSRF-TOKEN', // default
    // xsrfHeaderName: 'X-XSRF-TOKEN', // default
});

httpInstance.interceptors.request.use(config => {
    config.baseURL = window.api.baseUrl;
    return config;
}, error => {
    window.$app.$message.error(error);
    return { msg: error };
});

httpInstance.interceptors.response.use(
    response => {
        const { config, data } = response;

        if (config.systemError !== false) {
            if (data && data.code !== 0 && data.message) {
                window.$app.$message.error(data.message);
            }
        }

        if (data.code == null) {

            return {
                code: 0,
                data,
            };
        }
        return data;
    },
    result => {
        const { $message } = window.$app;
        const { code, response, isCancel, systemError, message } = result;

        if (systemError !== false) {
            if (isCancel) {

                const msg = isCancel.msg ? `Cancel menually: ${isCancel.msg}` : 'Cancel menually';

                $message.error(msg);
                return {
                    code: 'canceled',
                    msg,
                };
            } else if (code === 'ECONNABORTED') {

                const msg = 'request timeout !';

                $message.error(msg);
                return {
                    code: 'timeout',
                    msg,
                };
            } else if (response) {

                const status = +response.status;
                const msg = `${status}: ${response.statusText}`;

                switch (status) {
                    /* case 504:
                        $message.error(msg);
                        break; */
                    default:
                        $message.error(msg);
                }
                return {
                    code: status,
                    msg,
                };
            } else if (message) {

                $message.error(message);
            }
        }

        return {
            code: 1,
            msg:  message,
        };
    },
);

let loadingCount = 0;
const btnQueue = {};
const policy = {
    isCancel(options, state, msg) {
        if (state === true) {
            const cancelToken = cancelTokenQueue[`${options.url}`];

            if (cancelToken) {
                cancelToken.cancel();
                if (msg) {
                    window.$app.$message.error(msg);
                }
            }
        } else if (state === 'all') {

            for (const key in cancelTokenQueue) {
                cancelTokenQueue[key].cancel();
            }
        }
    },
    urlTail(options) {
        if (options.urltail) {
            const { url, urltail } = options;

            options.url = `${url}/${urltail.substr(0, 1) === '/' ? urltail.substr(1) : urltail}`;
        }
    },
    btnState(btnState) {
        if (!btnState || !btnState.target) return;

        let srcElement = btnState.target.srcElement;

        if (srcElement.nodeName !== 'BUTTON') {
            while (srcElement.nodeName !== 'BUTTON') {
                srcElement = srcElement.parentElement;
            }
        }

        if (!srcElement) return false;

        const locker = srcElement.getAttribute('locker');

        if (!locker) {
            if (btnState.type !== false) {
                srcElement.classList.add('is-loading');
                srcElement.setAttribute('locker', +Date.now());

                const icon = document.createElement('i');

                icon.classList.add('el-icon-loading');
                srcElement.insertBefore(icon, srcElement.children[0]);

                btnQueue[locker] = srcElement;
            }
        } else {

            return false;
        }

        return srcElement;
    },
    loading(options) {
        let loadingInstance = null;

        if (options.loading && loadingCount === 0) {
            delete options.loading;
            loadingInstance = window.$app.$loading({ fullscreen: true });
            loadingCount++;
        }
        return loadingInstance;
    },
};

const baseService = (config = {}) => {

    const options = deepMerge({
        loading: false,
        isLogin: false,
    }, config);


    const { isCancel } = options;

    policy.isCancel(options, isCancel);
    if (isCancel && isCancel.state) {
        policy.isCancel(options, isCancel.state, isCancel.msg);
    }

    const source = axios.CancelToken.source();

    cancelTokenQueue[`${options.url}`] = source;
    options.cancelToken = source.token;


    policy.urlTail(options);

    const srcElement = policy.btnState(config.btnState);

    if (srcElement === false) {
        return false;
    }

    if (options.headers !== false) {
        const userInfo = window.localStorage.getItem('userInfo') || window.sessionStorage.getItem('userInfo');

        if (userInfo) {
            options.headers = {
                token: JSON.parse(userInfo).token,
            };
        }
    }

    const loadingInstance = policy.loading(options);

    return new Promise((resolve, reject) => {
        httpInstance({ ...options })
            .then((res) => {
                resolve(res);
            })
            .catch(error => {
                reject(error);
            })
            .finally(() => {

                setTimeout(() => {

                    if (srcElement) {
                        const locker = srcElement.getAttribute('locker');

                        if (options.btnState.type !== false) {
                            srcElement.classList.remove('is-loading');
                            srcElement.removeChild(srcElement.children[0]);
                        }
                        btnQueue[locker] = null;
                        srcElement.removeAttribute('locker');
                    }

                    if (loadingInstance) {
                        loadingCount--;
                        loadingInstance.close();
                    }
                }, 400);
            });
    });
};

export default baseService;
