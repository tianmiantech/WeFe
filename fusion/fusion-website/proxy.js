/**
 * @author claude
 */

module.exports = {
    '/api': {
        target:       'https://xbd-dev.wolaidai.com/data-fusion-service-01',
        secure:       false,
        timeout:      1000000,
        changeOrigin: true,
        pathRewrite:  {
            ['^/api']: '',
        },
    },
};
