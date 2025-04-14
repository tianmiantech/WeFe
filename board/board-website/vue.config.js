const path = require('path');
const webpack = require('webpack');
const pkg = require('./package.json');
const AutoImport = require('unplugin-auto-import/webpack');
const Components = require('unplugin-vue-components/webpack');
const { ElementPlusResolver } = require('unplugin-vue-components/resolvers');
const ElementPlus = require('unplugin-element-plus/webpack');
// const { HashedModuleIdsPlugin } = require('webpack');
const argv = require('minimist')(process.argv.slice(2));
const CopyWebpackPlugin = require('copy-webpack-plugin');
const tailSplit = argv._[2] ? argv._[2].split('=')[1] : '';
/* const components = require('unplugin-vue-components/webpack');
const { ElementPlusResolver: elementPlusResolver } = require('unplugin-vue-components/resolvers');
const { BundleAnalyzerPlugin } = require('webpack-bundle-analyzer'); */
// const addCssPrefix = require('postcss-change-css-prefix');

// const SpeedMeasurePlugin = require('speed-measure-webpack-plugin');
// const { program } = require('commander');

// 使用program去获取参数, 和参数顺序无关
// program.option('-p, --prod <string>', 'prod');
// program.parse(process.argv);
// const options = program.opts();
const { welab } = pkg;
let { contextPath: APP_CODE } = welab || {};

const resolve = dir => path.resolve(__dirname, dir);

const { HOST_ENV,SERVICE_NAME } = argv;

APP_CODE = SERVICE_NAME || APP_CODE;
console.log(HOST_ENV, APP_CODE,welab.contextPath);

// 对应分支名称.
const isProd = process.env.NODE_ENV === 'production';

const buildDate = '2.5.0';
const port = 8082;

module.exports = {
    assetsDir:           './',
    indexPath:           './index.html',
    productionSourceMap: false,
    outputDir:           `dist/${APP_CODE}`,
    publicPath:          `/${APP_CODE}/`,
    pages:               {
        index: {
            entry:    'src/app/app.js',
            template: 'index.html',
            filename: 'index.html',
            BASE_URL: `${APP_CODE ? `/${APP_CODE}/` : `/${APP_CODE}/`}`,
            chunks:   'inital',
        },
    },
    css: {
        extract:       false,
        loaderOptions: {
            scss: {
                // sass 版本 9 中使用 additionalData 版本 8 中使用 prependData
                prependData: `
                    @use "@src/assets/styles/element.scss" as *;
                `,
            },
        },
    },
    configureWebpack: {
        devtool: 'source-map',
        plugins: [
            new webpack.DefinePlugin({
                'process.env.VERSION':  JSON.stringify(`${buildDate}`),
                'process.env.HOST_ENV': JSON.stringify(`${HOST_ENV}`),
                'process.env.APP_CODE': JSON.stringify(`${APP_CODE}`),
            }),
            // 按需引入会自动引入无需手动import（直接使用api仍需手动import）
            AutoImport({
                resolvers: [ElementPlusResolver(
                    {
                        exclude:     new RegExp(/^(?!.*loading-directive).*$/),
                        importStyle: 'sass',
                    },
                )],
            }),
            Components({
                resolvers: [ElementPlusResolver({ importStyle: 'sass' })],
            }),
            // 加入这句，手动import时才会按需加载对应样式，否则只有html形式使用才有样式
            ElementPlus({
                useSource: true,
            }),
            new CopyWebpackPlugin([
                {
                    from: 'public/*',
                    to:   `${APP_CODE || '.'}/img/`,
                },
            ]),
        ],
        resolve: {
            extensions: ['.js', '.vue', '.json'],
            alias:      {
                '@': path.join(__dirname, './src'),
            },
        },
        output: {
            // packageName需要与主项目中的引入子应用的name值相对应，统一为路由上下文名字
            library:       `${APP_CODE}-[name]`,
            libraryTarget: 'umd', // 把子应用打包成 umd 库格式
            jsonpFunction: `webpackJsonp_${APP_CODE}`,
        },
    },
    chainWebpack: config => {
        config.module
            .rule('scss')
            .oneOfs.store.forEach(item => {
                item.use('sass-resources-loader')
                    .loader('sass-resources-loader')
                    .options({
                        sourceMap: true,
                        resources: ['src/assets/styles/_variable.scss'],
                    })
                    .end();
            });

        /* config.module
            .rule('vue')
            .use('cache-loader')
            .loader('cache-loader')
            .end()
            .use('vue-loader'); */

        config.resolve.alias
            .set('@src', resolve('src'))
            .set('@comp', resolve('src/components'))
            .set('@assets', resolve('src/assets'))
            .set('@js', resolve('src/assets/js'))
            .set('@styles', resolve('src/assets/styles'))
            .set('@views', resolve('src/views'))
            .end();

        /* if (isProd) {
            config.optimization.minimizer('terser').tap(args => {
                args[0].terserOptions.compress.warnings = true;
                args[0].terserOptions.compress.drop_console = true;
                args[0].terserOptions.compress.drop_debugger = true;
                args[0].terserOptions.compress.pure_funcs = ['console.log'];
                args[0].extractComments = 'all';
                args[0].parallel = true;

                return args;
            });
        } */
        // config.plugin('speed').use(SpeedMeasurePlugin);
    },
    devServer: {
        port,
        hot:              true,
        liveReload:       true,
        // 关闭主机检查，使微应用可以被 fetch
        disableHostCheck: true,
        // 配置跨域请求头，解决开发环境的跨域问题
        headers:          {
            'Access-Control-Allow-Origin': '*',
        },
        /**
         * http://localhost:8080/manager-service
         * @author claude
         * @description webpack devServer proxy
         */
        proxy: {
            '/api': {
                target:       'https://xxx.com/',
                secure:       false,
                timeout:      1000000,
                changeOrigin: true,
                pathRewrite:  {
                    ['^/board-service']: '/',
                },
            },
            '/iam': {
                target:       'https://xxx.com/',
                secure:       false,
                timeout:      1000000,
                changeOrigin: true,

            },

        },
    },
};
