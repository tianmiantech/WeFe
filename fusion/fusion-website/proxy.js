/**
 * @author claude
 */

module.exports = {
    '/api': {
        target:       'https://xxx.wolaidai.com/data-fusion-service',
        secure:       false,
        timeout:      1000000,
        changeOrigin: true,
        pathRewrite:  {
            ['^/api']: '',
        },
    },
};
