/**
 * @author claude
 */

module.exports = {
    '/api': {
        target:       'https://xx.com/',
        secure:       false,
        timeout:      1000000,
        changeOrigin: true,
        pathRewrite:  {
            ['^/api']: '',
        },
    },
};
