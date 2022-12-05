import axios from 'axios';
import { getHeader, removeToken } from './utils';
import { isQianKun } from '@src/http/utils';
import { getOrigin } from '../utils/constant';

console.log('getOrigin()',getOrigin());
const iamInstance = axios.create({
    baseURL: getOrigin(),
    headers: {}, // global request headers
    timeout: 1000 * 20,
});

iamInstance.interceptors.request.use((config)=>{
    const commonHeaders = getHeader();

    config.headers = {
        ...commonHeaders,
    };
    return config;
});

iamInstance.interceptors.response.use((response)=>{
    const { data } = response;

    if (data.code === 'WG0001') {
        removeToken();
        if(isQianKun()){
            window.location.href = `/portal/login?tenantid=${localStorage.getItem('tenantId')}`;
        }
    }
    return data;
});

export default iamInstance;
