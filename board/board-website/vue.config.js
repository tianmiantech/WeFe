const path = require('path');
const webpack = require('webpack');
const { context } = require('./package.json');
const { HashedModuleIdsPlugin } = require('webpack');
const argv = require('minimist')(process.argv.slice(2));
const CopyWebpackPlugin = require('copy-webpack-plugin');
const { BundleAnalyzerPlugin } = require('webpack-bundle-analyzer');
// const SpeedMeasurePlugin = require('speed-measure-webpack-plugin');

const argvs = argv._[1] ? argv._[1].split('=') : '';
const isProd = process.env.NODE_ENV === 'production';
const resolve = dir => path.resolve(__dirname, dir);
const CONTEXT_ENV = argvs[1] || context || '';

const dateFormat = (timestamp, format = 'yyyy-MM-dd hh:mm:ss') => {

    if (!timestamp) {
        return '';
    }

    const $date = new Date(timestamp);

    const map = {
        y: $date.getFullYear(),
        M: $date.getMonth() + 1,
        d: $date.getDate(),
        h: $date.getHours(),
        m: $date.getMinutes(),
        s: $date.getSeconds(),
    };

    return format.replace(/(([yMdhmsT])(\2)*)/g, (all, t1, t2) => {
        const value = map[t2];

        if (t2 === 'y') {
            return `${value}`.substr(4 - t1.length);
        } else if (t2 === 'M' && t1.length > 2) {
            if (t1.length === 3) {
                return dateFormat.months[value - 1].substr(0, 3);
            }
            return dateFormat.months[value - 1];
        }
        return t1.length > 1 ? `0${value}`.substr(-2) : value;
    });
};
const buildDate = dateFormat(+new Date());

module.exports = {
    assetsDir:           isProd ? `${CONTEXT_ENV}` : '',
    indexPath:           isProd ? `${CONTEXT_ENV || '.'}/index.html` : 'index.html',
    productionSourceMap: false,
    pages:               {
        index: {
            entry:    'src/app/app.js',
            template: 'index.html',
            filename: 'index.html',
            BASE_URL: `${CONTEXT_ENV ? `/${CONTEXT_ENV}/` : '/'}`,
            chunks:   'inital',
        },
    },
    css: {
        extract: false, // close it for less tiny css files
    },
    configureWebpack: config => {
        if (isProd) {
            // production...
            const DEPLOY_ENV = argvs[0] || 'prod';

            config.optimization = {
                splitChunks: {
                    chunks:                 'all',
                    minSize:                50 * 10e3, // 50k
                    maxSize:                244 * 10e3, // 244k
                    minChunks:              2,
                    maxAsyncRequests:       4,
                    maxInitialRequests:     4,
                    automaticNameDelimiter: '~',
                    name:                   true,
                    cacheGroups:            {
                        echarts: {
                            name:     'echarts',
                            test:     /[\\/]node_modules[\\/]echarts[\\/]/,
                            priority: 20,
                        },
                        'antv-g6': {
                            name:     'antv-g6',
                            test:     /[\\/]node_modules[\\/]@antv[\\/]/,
                            priority: 20,
                        },
                        'cheetah-grid': {
                            name:     'cheetah-grid',
                            test:     /[\\/]node_modules[\\/](cheetah-grid|vue-cheetah-grid|zrender)[\\/]/,
                            priority: 20,
                        },
                        vendors: {
                            test:     /[\\/]node_modules[\\/](vue|vuex|vue-router|element-plus)[\\/]/,
                            priority: -10,
                        },
                    },
                },
            };

            config.plugins.push(
                new webpack.DefinePlugin({
                    'process.env.DEPLOY_ENV':  JSON.stringify(`${DEPLOY_ENV}`),
                    'process.env.CONTEXT_ENV': JSON.stringify(`${CONTEXT_ENV}`),
                    'process.env.VERSION':     JSON.stringify(`${buildDate}`),
                }),
                new HashedModuleIdsPlugin(),
                new CopyWebpackPlugin([
                    {
                        from: 'public/*',
                        to:   `${CONTEXT_ENV || '.'}/img/`,
                    },
                ]),
            );
        } else {
            // development...
            const DEPLOY_ENV = argvs[0] || 'dev';

            config.plugins.push(
                new webpack.DefinePlugin({
                    'process.env.DEPLOY_ENV':  JSON.stringify(`${DEPLOY_ENV}`),
                    'process.env.VERSION':     JSON.stringify(`${buildDate}`),
                    'process.env.CONTEXT_ENV': '""',
                }),
            );

            config.devtool = 'source-map';
        }

        if (argv['report']) {
            config.plugins.push(new BundleAnalyzerPlugin());
        }
    },
    chainWebpack: config => {
        config.module
            .rule('scss')
            .use('cache-loader')
            .loader('cache-loader')
            .end()
            .oneOfs.store.forEach(item => {
                item.use('cache-loader')
                    .loader('cache-loader')
                    .end()
                    .use('sass-resources-loader')
                    .loader('sass-resources-loader')
                    .options({
                        sourceMap: true,
                        resources: 'src/assets/styles/_variable.scss',
                    })
                    .end();
            });

        config.module
            .rule('vue')
            .use('cache-loader')
            .loader('cache-loader')
            .end()
            .use('vue-loader');

        config.resolve.alias
            .set('@src', resolve('src'))
            .set('@comp', resolve('src/components'))
            .set('@assets', resolve('src/assets'))
            .set('@js', resolve('src/assets/js'))
            .set('@styles', resolve('src/assets/styles'))
            .set('@views', resolve('src/views'))
            .end()
            .end();

        if (isProd) {
            config.optimization.minimizer('terser').tap(args => {
                args[0].terserOptions.compress.warnings = false;
                args[0].terserOptions.compress.drop_console = true;
                args[0].terserOptions.compress.drop_debugger = true;
                args[0].terserOptions.compress.pure_funcs = ['console.log'];
                args[0].extractComments = 'all';
                args[0].parallel = true;

                return args;
            });
        }
        // config.plugin('speed').use(SpeedMeasurePlugin);
    },
    devServer: {
        hot:        true,
        liveReload: true,
        /**
         * http://localhost:8080/board-service
         * @author claude
         * @description webpack devServer proxy
         */
        proxy:      {
            '/api': {
                target:       'https://xxx.com/board-service',
                secure:       false,
                timeout:      1000000,
                changeOrigin: true,
                pathRewrite:  {
                    ['^/api']: '/',
                },
            },
        },
    },
};
