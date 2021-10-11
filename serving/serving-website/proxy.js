/**
 * dev:https://***.wolaidai.com/board-service-01
 * @author claude
 * @date 2020-04-20
 * @description webpack 开发代理配置
 */

module.exports = {
    '/api': {
        target:       'https://***.wolaidai.com/serving-service-01',
        secure:       false,
        timeout:      1000000,
        changeOrigin: true,
        pathRewrite:  {
            ['^/api']: '/',
        },
    },
};
