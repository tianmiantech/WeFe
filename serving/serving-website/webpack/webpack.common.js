/*!
 * @author claude
 * date 07/05/2019
 * 公共 webpack 配置
 */

const path = require('path');
const { VueLoaderPlugin } = require('vue-loader');
const HtmlWebpackPlugin = require('html-webpack-plugin');
const MiniCssExtractPlugin = require('mini-css-extract-plugin');
const HardSourceWebpackPlugin = require('hard-source-webpack-plugin');
const FriendlyErrorsPlugin = require('friendly-errors-webpack-plugin');

const packageJson = require('../package.json');
const resolve = dir => path.resolve(__dirname, dir);
const devMode = process.env.NODE_ENV !== 'production';
const argv = require('minimist')(process.argv.slice(2));
const argvs = argv._[0] ? argv._[0].split('=') : '';
const context = devMode ? '/' : `${argvs[1] ? `/${argvs[1]}/` : (packageJson.context ? `/${packageJson.context}/` : '/')}`;

const plugins = [];

// 添加分析插件
/* if (envParams.report !== undefined) {
    plugins.push(new BundleAnalyzerPlugin());
}

// Dashboard 插件
if (envParams.log !== undefined) {
    const Dashboard = require('webpack-dashboard');
    const DashboardPlugin = require('webpack-dashboard/plugin');
    const dashboard = new Dashboard();

    plugins.push(new DashboardPlugin(dashboard.setData));
} */

const cssloaders = [
    'css-loader',
    'postcss-loader',
    {
        loader:  'sass-loader',
        options: {
            implementation: require('dart-sass'),
        },
    },
    {
        loader:  'sass-resources-loader',
        options: {
            resources: [
                resolve('../src/assets/styles/_variable.scss'),
            ],
        },
    },
];

if (devMode) {
    plugins.push(new HardSourceWebpackPlugin());
}

const webpackConfig = {
    mode:  'development',
    entry: {
        main: resolve('../src/app/app.js'),
    },
    output: {
        filename:      `js/[name].[${devMode ? 'hash' : 'chunkhash'}:7].js`,
        chunkFilename: `js/[name].[${devMode ? 'hash' : 'chunkhash'}:7].js`,
        path:          resolve(`../dist${context}`),
        publicPath:    context,
    },
    resolve: {
        mainFields: ['main'],
        extensions: ['.js', '.vue', '.scss'],
        modules:    [resolve('../node_modules/')],
        alias:      {
            '@':       resolve('../'),
            '@src':    resolve('../src'),
            '@assets': resolve('../src/assets'),
            '@comp':   resolve('../src/components'),
            '@js':     resolve('../src/assets/js'),
            '@styles': resolve('../src/assets/styles'),
            '@views':  resolve('../src/views'),
        },
    },
    module: {
        rules: [{
            test: /\.(sc|c)ss$/,
            use:  devMode ? [
                'style-loader',
                ...cssloaders,
            ] : [
                    'style-loader',
                    { loader: MiniCssExtractPlugin.loader },
                    ...cssloaders,
                ],
            exclude: [/node_modules/],
        }, {
            test:    /\.js$/,
            use:     ['cache-loader', 'babel-loader'],
            exclude: [/node_modules/],
        }, {
            test: /\.(png|jpe?g|svg|gif)$/i,
            use:  [{
                loader:  'url-loader',
                options: {
                    limit:    8192,    // 8k
                    name:     'images/[name].[hash:7].[ext]',
                    esModule: false,
                },
            }],
            exclude: [],
        }, {
            test: /\.(eot|woff|woff2|ttf)$/i,
            use:  [{
                loader:  'url-loader',
                options: {
                    limit: 8192,    // 8k
                    name:  'fonts/[name].[hash:7].[ext]',
                },
            }],
            exclude: [],
        }, {
            test:    /element-ui\/.*?js$/,
            loader:  ['cache-loader', 'babel-loader'],
            exclude: [/node_modules/],
        }, {
            test: /node_modules\/.*?css$/,
            use:  devMode? [
                'style-loader',
                'css-loader',
                'postcss-loader',
            ] : [
                'style-loader',
                {
                    loader:  MiniCssExtractPlugin.loader,
                    options: {
                        hmr: true,
                    },
                },
                'css-loader',
                'postcss-loader',
            ],
        }, {
            test:    /\.vue$/,
            loader:  'vue-loader',
            exclude: [],
        }],
    },
    watchOptions: {
        //不监听的 node_modules 目录下的文件
        ignored: /node_modules/,
    },
    plugins: [
        new VueLoaderPlugin(),
        new MiniCssExtractPlugin({
            filename:      'css/[name].[hash:7].css',
            chunkFilename: 'css/[id].[hash:7].css',
        }),
        new HtmlWebpackPlugin({
            template: resolve('../index.html'),
            favicon:  resolve('../src/assets/images/x-logo.png'),
            inject:   true,
        }),
        new FriendlyErrorsPlugin(),
        ...plugins,
    ],
};

module.exports = webpackConfig;
