/*!
 * @author claude
 * date 07/05/2019
 * webpack dev 配置
 */

const webpack = require('webpack');
const merge = require('webpack-merge');
const webpackConfig = require('./webpack.common.js');
const argv = require('minimist')(process.argv.slice(2));
const argvs = argv._[1] ? argv._[1].split('=') : '';
const { context } = require('../package.json');
const proxy = require('../proxy.js');

const DEPLOY_ENV = argvs[1] || 'dev';

const plugins = [
    // 注入环境变量
    new webpack.DefinePlugin({
        'process.env':             require('dotenv-extended').load(),
        'process.env.NODE_ENV':    JSON.stringify('development'),
        'process.env.DEPLOY_ENV':  JSON.stringify(`${DEPLOY_ENV}`),
        'process.env.CONTEXT_ENV': JSON.stringify(`${context}`),
    }),
    new webpack.HotModuleReplacementPlugin(),
];

const devServer = {
    mode:      'development',
    devtool:   '#source-map',
    devServer: {
        compress:           true,
        historyApiFallback: true,
        // contentBase:        path.join(__dirname, '../dist'),
        host:               '0.0.0.0',
        port:               5000,
        hot:                true,
        // open:               true,
        progress:           false,
        quiet:              true,
        overlay:            {
            warning: true,
            errors:  true,
        },
        proxy,
    },
    watchOptions: {
        ignored: [
            'node_modules',
        ],
    },
    plugins,
};

module.exports = merge(webpackConfig, devServer);
