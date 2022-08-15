const path = require('path');
const webpack = require('webpack');
const pkg = require('./package.json');
const { HashedModuleIdsPlugin } = require('webpack');
const argv = require('minimist')(process.argv.slice(2));
const CopyWebpackPlugin = require('copy-webpack-plugin');
const tailSplit = argv._[2] ? argv._[2].split('=')[1] : '';
const components = require('unplugin-vue-components/webpack');
const { ElementPlusResolver: elementPlusResolver } = require('unplugin-vue-components/resolvers');
const { BundleAnalyzerPlugin } = require('webpack-bundle-analyzer');
// const SpeedMeasurePlugin = require('speed-measure-webpack-plugin');

const argvs = argv._[1] ? argv._[1].split('=') : '';
const isProd = process.env.NODE_ENV === 'production';
const resolve = dir => path.resolve(__dirname, dir);
const CONTEXT_ENV = argvs[1] || pkg.welab.contextPath || '';
const buildDate = '3.0.0';

console.log('Context_ENV:', CONTEXT_ENV);

module.exports = {
    // TODO: dev, fat, prod, *. publicPath should to be same.
    assetsDir:           './', // isProd ? `${CONTEXT_ENV}` : CONTEXT_ENV,
    indexPath:           './index.html', // isProd ? `${CONTEXT_ENV || '.'}/index.html` : `${CONTEXT_ENV || '.'}/index.html` ,
    productionSourceMap: false,
    outputDir:           'dist/manager-website',
    publicPath:          '/manager-website',
    pages:               {
        index: {
            entry:    'src/app/app.js',
            template: 'index.html',
            filename: 'index.html',
            BASE_URL: `${CONTEXT_ENV ? `/${CONTEXT_ENV}/` : `/${CONTEXT_ENV}/`}`,
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
                    'process.env.TAIL':        JSON.stringify(`${tailSplit}`),
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

        config.plugins.push(
            components({
                resolvers: [elementPlusResolver()],
            }),
        );
        if (argv['report']) {
            config.plugins.push(new BundleAnalyzerPlugin());
        }

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

        // vue-cli service 直接用config.output = {} 这种写法, 版本4不支持。 所以拆开写.
        // 微应用的包名，这里与主应用中注册的微应用名称一致
        config.output.library = 'wefe';
        // 将你的 library 暴露为所有的模块定义下都可运行的方式
        config.output.libraryTarget = 'umd';
        // 按需加载相关，设置为 webpackJsonp_VueMicroApp 即可
        config.output.jsonpFunction = 'webpackJsonp_wefe';

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
        // 配置跨域请求头，解决开发环境的跨域问题
        headers:    {
            'Access-Control-Allow-Origin': '*',
        },
        /**
         * http://localhost:8080/manager-service
         * @author claude
         * @description webpack devServer proxy
         */
        proxy: {
            '/api': {
                target:       'https://xxx.wolaidai.com/manager-service',
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
