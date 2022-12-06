import AuthClient from '@tianmiantech/auth/';
import axios from 'axios';
import { appCode,getOrigin } from '@src/utils/constant';
import { getHeader, removeToken,isQianKun } from './utils';


const instance = axios.create({
    baseURL: getOrigin(),
    headers: {
    }, // global request headers
    timeout: 1000 * 20,
});

instance.interceptors.request.use((config)=>{
    config.headers = {
        ...config.headers,
        ...getHeader(),
    };
    return config;
});

instance.interceptors.response.use((response)=>{
    const { data } = response;

    if (data.code === 'WG0001') {
        removeToken();
        if(isQianKun()){
            window.location.href = `/portal/login?tenantid=${localStorage.getItem('tenantId')}`;
        }    }
    return data;
});

const authClient = () => {
    return new AuthClient(appCode(), instance, undefined, undefined, 'iam');
};

export default authClient;
