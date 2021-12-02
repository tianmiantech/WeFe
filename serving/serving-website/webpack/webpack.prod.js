/*!
 * @author claude
 * date 07/05/2019
 * webpack 生产配置
 */
const webpack = require('webpack');
const merge = require('webpack-merge');
const { original } = JSON.parse(process.env.npm_config_argv);
const OptimizeCSSAssetsPlugin = require('optimize-css-assets-webpack-plugin');
const { CleanWebpackPlugin } = require('clean-webpack-plugin');
const TerserJSPlugin = require('terser-webpack-plugin');
// const WebpackZipPlugin = require('webpack-zip-plugin');
const webpackConfig = require('./webpack.common');
const { context } = require('../package.json');

const DEPLOY_ENV = original.length > 3 ? original[3].split('=')[0] : 'prod';
const CONTEXT_ENV = original.length > 3 ? original[3].split('=')[1] : context;

module.exports = merge(webpackConfig, {
    mode:    'production',
    plugins: [
        // 注入环境变量
        new webpack.DefinePlugin({
            'process.env':             require('dotenv-extended').load(),
            'process.env.NODE_ENV':    JSON.stringify('production'),
            'process.env.DEPLOY_ENV':  JSON.stringify(`${DEPLOY_ENV}`),
            'process.env.CONTEXT_ENV': JSON.stringify(`${CONTEXT_ENV}`),
        }),
        new CleanWebpackPlugin({
            cleanOnceBeforeBuildPatterns: ['**/*.js', '**/*.css', '!**lib/*vendor*.js'],
        }),
        new webpack.HashedModuleIdsPlugin(), // 强制缓存
    ],
    optimization: {
        splitChunks: {
            name:                   true,
            chunks:                 'all', // 从哪些chunks里面抽取代码
            minChunks:              2, // 最少引用次数
            minSize:                30000, // 表示抽取出来的文件在压缩前的最小体积，默认为 30000
            maxAsyncRequests:       5, // 最大的按需加载次数
            maxInitialRequests:     3, // 最大的初始化加载次数
            automaticNameDelimiter: '~',
            cacheGroups:            {
                vendors: {
                    // 第三方依赖
                    test:      /[\\/]node_modules[\\/]/,
                    priority:  -10,
                    chunks:    'initial',
                    minSize:   100,
                    minChunks: 2,
                },
                default: {
                    minChunks:          2,
                    priority:           -20,
                    reuseExistingChunk: true,
                },
            },
        },
        minimizer: [
            new OptimizeCSSAssetsPlugin({
                cssProcessor:        require('cssnano'), //引入cssnano配置压缩选项
                cssProcessorOptions: {
                    discardComments: { removeAll: true },
                },
            }),
            new TerserJSPlugin({
                terserOptions: {
                    warnings: false,
                    output:   {
                        comments: false,
                    },
                    compress: {
                        drop_console:  true,
                        drop_debugger: true,
                    },
                },
                sourceMap: false,
                parallel:  true,
            }),
        ],
    },
});
