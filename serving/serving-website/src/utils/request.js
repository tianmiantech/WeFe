import { axiosInstance, iam } from '@tianmiantech/request';
import { getToken, createUUID, formatDate } from '@tianmiantech/util';

const TOKEN = `iam-${localStorage.getItem('env') || process.env.HOST_ENV || 'dev'}-x-user-token`;

import { Message } from 'element-ui';

const request = (baseUrl) => axiosInstance({
    message:     Message,
    baseURL:     baseUrl,
    successCode: 0,
    getHeaders:  () => ({
      'x-user-token':    getToken(TOKEN),
      'x-req-rd':        createUUID(),
      'x-req-timestamp': formatDate(),
    }),
    invalidTokenCodes: iam.exitStatusCodes,
    onTokenInvalid:    iam.generateOnTokenInvalid(Message, TOKEN),
});

export default request;
