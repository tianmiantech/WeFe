import { getToken, createUUID, formatDate } from '@tianmiantech/util';
import { removeCookie } from '@tianmiantech/util';

const getTokenName = () => {
    const { _QIANKUN_ENV: qiankun_env } = window.$app || {};
    const tokenName = `iam-${ qiankun_env || process.env.HOST_ENV || 'dev'}-x-user-token`;

    return tokenName;
};

export const getHeader = () => ({
    'x-user-token':    getToken(getTokenName()),
    'x-req-rd':        createUUID(),
    'x-req-timestamp': formatDate(),
});

export const removeToken = () => {
    removeCookie(getTokenName());
};

export const isQianKun = () => window.__POWERED_BY_QIANKUN__;
