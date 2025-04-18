/*!
 * @author claude
 * http serve creater
 */

import axios from 'axios';
import { baseLogout } from '@src/router/auth';
import { deepMerge } from '@src/utils/types';
import { getHeader, removeToken,isQianKun} from './utils';
import { clearUserInfo } from '@src/router/auth';
import { baseURL } from '@src/utils/constant';

const cancelTokenQueue = {}; // cancel token queue
// create axios instance
const httpInstance = axios.create({
    baseURL: '',
    headers: {}, // global request headers
    timeout: 1000 * 20,
});

// request interceptors
httpInstance.interceptors.request.use(
    config => {

        // must logged in before sending request
        if (config.isLogin) {
            // cancel all requests first
            for (const key in cancelTokenQueue) {
                cancelTokenQueue[key].cancel();
            }
        }
        config.baseURL = baseURL();
        return config;
    },
    error => {
        window.$app.config.globalProperties.$message.error({
            message:  error,
            grouping: true,
        });
        return {
            msg: error,
        };
    },
);

// time stamp for last message dialog
const lastErrorMessageTime = 0;

// respones interceptors
httpInstance.interceptors.response.use(
    response => {
        const { config, data } = response;

        // global error for request
        if (config.systemError !== false) {
            if (config.responseType !== 'blob' && data && data.code !== 0) {
                if (data.code === 'WG0001' && isQianKun()) {
                    removeToken();
                    clearUserInfo();
                    window.location.href = `/portal/login?tenantid=${localStorage.getItem('tenantId')}`;
                } else if(data.code === 10017) {
                    window.$app.config.globalProperties.$message.error(data.message);
                } else if (data.code === 10050) {
                    window.$app.config.globalProperties.$message.error({
                        message:  '密码强度不够需重新设置密码',
                        grouping: true,
                    });
                    window.$app.config.globalProperties.$router.push('change-password');
                } else if(data.code === 30001) {
                    // graph node has exception occurred
                    window.$app.config.globalProperties.$bus.$emit('node-error', {
                        ...data.data,
                        message: data.message,
                    });
                } else {
                    window.$app.config.globalProperties.$message.error({
                        message:  data.message || '未知错误!',
                        grouping: true,
                    });
                }
            }
        }

        // compatible the error data
        if (data.code == null) {
            // Convert to standard structure
            return {
                code: 0,
                data,
                response,
            };
        }

        // 添加自定义字段以备用
        data._axios_response = response;
        return data;
    },
    result => {
        const { $message } = window.$app.config.globalProperties;
        const { code, response, isCancel, systemError, message } = result;

        if (systemError !== false) {
            if (isCancel) {
                // Actively cancel pop-up prompt
                const msg = isCancel.msg ? `请求已取消: ${isCancel.msg}` : '请求已取消';

                $message.error({
                    message:  msg,
                    grouping: true,
                });
                return {
                    code: 'cancelled',
                    msg,
                };
            } else if (code === 'ECONNABORTED') {
                // Capture error
                const msg = '请求超时 !';

                $message.error(msg);
                return {
                    code: 'timeout',
                    msg,
                };
            } else if (response) {
                const status = +response.status;
                const msg = `${status}: ${response.statusText}`;

                switch (status) {
                    case 401:
                        // to login
                        $message.error({
                            message:  '登录已过期, 请重新登录',
                            grouping: true,
                        });
                        baseLogout();
                        break;
                    /* case 504:
                        $message.error({
                            message: msg,
                            grouping: true,
                        });
                        break; */
                    default:
                        $message.error({
                            message:  msg,
                            grouping: true,
                        });
                }
                return {
                    code: status,
                    msg,
                };
            } else if (message) {
                // Cross domain / network error...
                $message.error({
                    message,
                    grouping: true,
                });
                return {
                    code: 1,
                    msg:  message,
                };
            }
        }
        return {
            code: 1,
            msg:  message,
        };
    },
);

/**
 * public service function
 */
let loadingCount = 0; // loading count
const btnQueue = {}; // request queue for buttons
const policy = {
    isCancel(options, state, msg) {
        if (state === true) {
            const cancelToken = cancelTokenQueue[`${options.url}`];

            if (cancelToken) {
                cancelToken.cancel();
                if (msg) {
                    window.$app.config.globalProperties.$message.error({
                        message:  msg,
                        grouping: true,
                    });
                }
            }
        } else if (state === 'all') {
            // cancel all requests
            for (const key in cancelTokenQueue) {
                cancelTokenQueue[key].cancel();
            }
        }
    },
    urlTail(options) {
        if (options.urltail) {
            const { url, urltail } = options;

            options.url = `${url}/${
                urltail.substr(0, 1) === '/' ? urltail.substr(1) : urltail
            }`;
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
                // insert loading element
                const icon = document.createElement('i');

                icon.classList.add('el-icon-loading');
                srcElement.insertBefore(icon, srcElement.children[0]);
                // add to queue
                btnQueue[locker] = srcElement;
            }
        } else {
            // Block duplicate requests
            return false;
        }

        return srcElement;
    },
    loading(options) {
        let loadingInstance = null;

        if (options.loading && loadingCount === 0) {
            delete options.loading;
            loadingInstance = window.$app.config.globalProperties.$loading({ fullscreen: true });
            loadingCount++;
        }
        return loadingInstance;
    },
};
const createTimeoutLayer = () => {
    const now = Date.now();
    const timer = setTimeout(_ => {
        // check the global loading
        if (!document.body.classList.contains('el-loading-parent--relative')) {
            const loadingLayer = document.createElement('div');

            loadingLayer.id = now;
            loadingLayer.className = 'el-loading-mask is-fullscreen';
            loadingLayer.style.zIndex = 20000000;
            loadingLayer.innerHTML = `<div class="manager-loading-spinner">
                <svg viewBox="25 25 50 50" class="circular">
                    <circle cx="50" cy="50" r="20" fill="none" class="path"></circle>
                </svg>
                <p class="text-c mt10" style="color:#4D84F7;">等待服务器响应, 请稍候...</p>
            </div>`;
            document.body.appendChild(loadingLayer);
        }
    }, 10000);

    return {
        timer,
        now,
    };
};

const baseService = (config = {}) => {
    const options = deepMerge(
        {
            loading:      false,
            isLogin:      false,
            timeoutLayer: false,
        },
        config,
    );
    const { isCancel } = options;

    policy.isCancel(options, isCancel);
    if (isCancel && isCancel.state) {
        policy.isCancel(options, isCancel.state, isCancel.msg);
    }

    const source = axios.CancelToken.source();

    cancelTokenQueue[`${options.url}`] = source;
    options.cancelToken = source.token;

    // add url tail
    policy.urlTail(options);

    const srcElement = policy.btnState(config.btnState); // must be config

    // Block duplicate requests
    if (srcElement === false) {
        return false;
    }

    // set default headers
    const { headers } = options;
    const commonHeaders = getHeader();

    if (headers !== false) {
        if (isQianKun()) {
            options.headers = {
                ...headers,
                ... commonHeaders,
            };
        } else {
            options.headers = {
                ...headers,
            };
        }
    }

    // add global loading
    const loadingInstance = policy.loading(options);

    // call httpInstance
    return new Promise((resolve, reject) => {

        let timeout = null;

        if (options.timeoutLayer) {
            // request tiemout after 5s, then create global loading
            timeout = createTimeoutLayer();
        }

        httpInstance({ ...options })
            .then(res => {
                resolve(res);
            })
            .catch(error => {
                reject(error);
            })
            .finally(() => {
                if (timeout) {
                    clearTimeout(timeout.timer);
                    const layer = document.getElementById(timeout.now);

                    layer && document.body.removeChild(layer);
                }
                setTimeout(() => {
                    // restore button status
                    if (srcElement) {
                        const locker = srcElement.getAttribute('locker');

                        if (options.btnState.type !== false) {
                            srcElement.classList.remove('is-loading');
                            srcElement.removeChild(srcElement.children[0]);
                        }
                        btnQueue[locker] = null;
                        srcElement.removeAttribute('locker');
                    }

                    // close global loading
                    if (loadingInstance) {
                        loadingCount--;
                        loadingInstance.close();
                    }
                }, 400);
            });
    });
};

export default baseService;
