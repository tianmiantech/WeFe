/*!
 * @author claude
 */

const webpack = require('webpack');
const merge = require('webpack-merge');
const webpackConfig = require('./webpack.common.js');
const argv = require('minimist')(process.argv.slice(2));
const argvs = argv._[0] ? argv._[0].split('=') : '';
const proxy = require('../proxy.js');

const DEPLOY_ENV = argvs ? argvs[0] : 'test';

const plugins = [
    new webpack.DefinePlugin({
        'process.env':            require('dotenv-extended').load(),
        'process.env.NODE_ENV':   JSON.stringify('development'),
        'process.env.DEPLOY_ENV': JSON.stringify(`${DEPLOY_ENV}`),
    }),
    new webpack.HotModuleReplacementPlugin(),
];

const devServer = {
    mode:      'development',
    devtool:   '#source-map',
    devServer: {
        compress:           true,
        historyApiFallback: true,
        host:               '127.0.0.1',
        port:               proxy.prot || 5001,
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
        ignored: ['node_modules'],
    },
    plugins,
};

module.exports = merge(webpackConfig, devServer);
