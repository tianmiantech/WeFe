import axios from 'axios';
import { getHeader, isQianKun, removeToken } from './utils';

const iamInstance = axios.create({
    baseUrl: '',
    headers: {}, // global request headers
    timeout: 1000 * 20,
});

iamInstance.interceptors.request.use((config)=>{
    const { iamUrl } = window.fusionApi || {};
    const commonHeaders = getHeader();

    config.baseURL = iamUrl;
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
