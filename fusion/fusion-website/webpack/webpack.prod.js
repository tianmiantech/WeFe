/*!
 * @author claude
 */

const webpack = require('webpack');
const merge = require('webpack-merge');
const argv = require('minimist')(process.argv.slice(2));
const argvs = argv._[0] ? argv._[0].split('=') : '';
const tailSplit = argv._[1] ? argv._[1].split('=')[1] : '';
const OptimizeCSSAssetsPlugin = require('optimize-css-assets-webpack-plugin');
const { CleanWebpackPlugin } = require('clean-webpack-plugin');
const TerserJSPlugin = require('terser-webpack-plugin');
const webpackConfig = require('./webpack.common');
const { context } = require('../package.json');

const DEPLOY_ENV = argvs[0] || 'prod';
const CONTEXT_ENV = argvs[1] || context;

module.exports = merge(webpackConfig, {
    mode:    'production',
    plugins: [
        new webpack.DefinePlugin({
            'process.env':             require('dotenv-extended').load(),
            'process.env.NODE_ENV':    JSON.stringify('production'),
            'process.env.DEPLOY_ENV':  JSON.stringify(`${DEPLOY_ENV}`),
            'process.env.CONTEXT_ENV': JSON.stringify(`${CONTEXT_ENV}`),
            'process.env.TAIL':        JSON.stringify(`${tailSplit}`),
        }),
        new CleanWebpackPlugin({
            cleanOnceBeforeBuildPatterns: ['**/js*', '**/css*', '!**/*vendor*.js'],
        }),
        new webpack.HashedModuleIdsPlugin(),
    ],
    optimization: {
        splitChunks: {
            name:                   true,
            chunks:                 'all',
            minChunks:              2,
            minSize:                30000,
            maxAsyncRequests:       5,
            maxInitialRequests:     3,
            automaticNameDelimiter: '~',
            cacheGroups:            {
                vendors: {
                    test:      /[\\/]node_modules[\\/]/,
                    priority:  -10,
                    chunks:    'initial',
                    minSize:   100,
                    minChunks: 1,
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
                cssProcessor:        require('cssnano'),
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
