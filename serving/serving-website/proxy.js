/**
 * dev:https://***.wolaidai.com/board-service-01
 * @author claude
 * @date 2020-04-20
 * @description webpack 开发代理配置
 */

module.exports = {
    '/api': {
        target:       'http://localhost:8080/serving-service',
        secure:       false,
        timeout:      1000000,
        changeOrigin: true,
        pathRewrite:  {
            ['^/api']: '/',
        },
    },
};
