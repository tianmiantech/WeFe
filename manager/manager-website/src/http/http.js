/**
 * @author claude
 * http service
 * @param {Object} options request params, same as axios default params
 *
 * urltail will be join to url
 * params:
 * config = {
 *      (add) urltail, url suffix
 *      (add) btnState: {
 *          type, can be ['loading', 'disabled'], default:'loading', false: no effect
 *          target, click event
 *      },
 *      (add) isCancel, cancel last request
 *      (add) isLogin, true: need to login before request
 *      (add) systemError, default: true, show system error dialog
 *      baseURL, url prefix
 *      timeout (Number), request timeout
 *      headers (Object), request header
 *      params (Object), key=value, add to url ?, e.g. https://example.com?a=b
 *      data (Object), for post
 * }
 *
 * @param {isCancel} Object use:
 * isCancel: {
 *      state: true / 'all', true: cancel last one, 'all': cancel all requests
 *      msg: String, reason for cancel, opational
 * }
 *
 * callback always get code & msg,
 * you can deal with 'code'
 * there are 'cancelled', 'timeout' by me
 */

import baseService from './httpCreate.js';
import { deepMerge } from '@src/utils/types';

// some methods normally
const methods = ['get', 'post', 'delete', 'put'];

// create http object
const http = {
    // add cancelToken
    // cancelToken: () => axios.CancelToken.source(),
    // isCancel: (thrown) => String(thrown).includes('Cancel menually: '),
};

// add method to http object
methods.forEach(method => {
    http[method] = (config = {}, options = {}) => {
        if (typeof config === 'string') {
            options.url = config;
        } else if (!config.url) {
            window.$app.config.globalProperties.$message.error('接口不存在!');
            return new Promise(() => {
                Promise.reject('接口入参有误');
            });
        }

        const result = deepMerge(config, options);

        result.method = method;
        //! deepMerge cannot merge the dom Object
        result.btnState = config.btnState || options.btnState;
        // result.cancelToken = options.cancelToken; // deepMerge cannot merge promise
        return baseService(result);
    };
});

export default http;
