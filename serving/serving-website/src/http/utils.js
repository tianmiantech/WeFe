import { getToken, createUUID, formatDate } from '@tianmiantech/util';
import { getTokenName } from '@src/utils/tools';
import { removeCookie } from '@tianmiantech/util';

export const getHeader = () => isQianKun() ? ({
    'x-user-token':    getToken(getTokenName()),
    'x-req-rd':        createUUID(),
    'x-req-timestamp': formatDate(),
}) : {};

export const removeToken = () => {
    removeCookie(getTokenName());
};

export const isQianKun = () => window.__POWERED_BY_QIANKUN__;
