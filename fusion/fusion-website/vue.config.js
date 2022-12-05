const path = require('path');
const webpack = require('webpack');
const { HashedModuleIdsPlugin } = require('webpack');
const argv = require('minimist')(process.argv.slice(2));
const CopyWebpackPlugin = require('copy-webpack-plugin');
const { BundleAnalyzerPlugin } = require('webpack-bundle-analyzer');
const pkg = require('./package.json');
// const SpeedMeasurePlugin = require('speed-measure-webpack-plugin');

const isProd = process.env.NODE_ENV === 'production';
const resolve = (dir) => path.resolve(__dirname, dir);

const buildDate = '3.0.0';
const { welab } = pkg;
const { contextPath: APP_CODE } = welab || {};

const { HOST_ENV } = argv;

console.log(HOST_ENV, APP_CODE);

module.exports = {
    assetsDir:           './',
    indexPath:           './index.html',
    productionSourceMap: false,
    outputDir:           'dist/' + APP_CODE,
    publicPath:          '/' + APP_CODE,
    pages:               {
        index: {
            entry:    'src/app/app.js',
            template: 'index.html',
            filename: 'index.html',
            BASE_URL: `${APP_CODE ? `/${APP_CODE}/` : '/'}`,
            chunks:   'inital',
        },
    },
    css: {
        extract: false, // close it for less tiny css files
    },
    configureWebpack: (config) => {
        if (isProd) {
            // production...
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
                        /* 'antv-g6': {
                            name:     'antv-g6',
                            test:     /[\\/]node_modules[\\/]@antv[\\/]/,
                            priority: 20,
                        }, */
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
                    'process.env.VERSION':  JSON.stringify(`${buildDate}`),
                    'process.env.HOST_ENV': JSON.stringify(`${HOST_ENV}`),
                    'process.env.APP_CODE': JSON.stringify(`${APP_CODE}`)
                }),
                new HashedModuleIdsPlugin(),
                new CopyWebpackPlugin([
                    {
                        from: 'public/*',
                        to:   `${APP_CODE || '.'}/img/`,
                    },
                ]),
            );
        } else {
            // development...
            config.plugins.push(
                new webpack.DefinePlugin({
                    'process.env.VERSION':  JSON.stringify(`${buildDate}`),
                    'process.env.HOST_ENV': JSON.stringify(`${HOST_ENV}`),
                    'process.env.APP_CODE': JSON.stringify(`${APP_CODE}`) 
               }),
            );

            config.devtool = 'source-map';
        }


        if (argv['report']) {
            config.plugins.push(new BundleAnalyzerPlugin());
        }
        // vue-cli service 直接用config.output = {} 这种写法, 版本4不支持。 所以拆开写.
        // 微应用的包名，这里与主应用中注册的微应用名称一致
        config.output.library = `${APP_CODE}-[name]`;
        // 将你的 library 暴露为所有的模块定义下都可运行的方式
        config.output.libraryTarget = 'umd';
        // 按需加载相关，设置为 webpackJsonp_VueMicroApp 即可
        config.output.jsonpFunction = `webpackJsonp_${APP_CODE}`;
    },
    chainWebpack: (config) => {
        config.module
            .rule('scss')
            .use('cache-loader')
            .loader('cache-loader')
            .end()
            .oneOfs.store.forEach((item) => {
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
            config.optimization.minimizer('terser').tap((args) => {
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
        hot:              true,
        liveReload:       true,
        port:             10801,
        /**
         * http://localhost:8080/board-service
         * @author claude
         * @description webpack devServer proxy
         */
        // 关闭主机检查，使微应用可以被 fetch
        disableHostCheck: true,
        // 配置跨域请求头，解决开发环境的跨域问题
        headers:          {
            'Access-Control-Allow-Origin': '*',
        },
        proxy: {
            '/fusion-service': {
                target:       'https://tbapi-dev.tianmiantech.com/fusion-service/',
                secure:       false,
                timeout:      1000000,
                changeOrigin: true,
                pathRewrite:  {
                    ['^/fusion-service']: '/',
                },
            },
            '/iam': {
                target:       'https://tbapi-dev.tianmiantech.com/',
                secure:       false,
                timeout:      1000000,
                changeOrigin: true,
            },
        },
    },
};
