import { isQianKun } from '@src/http/utils';


const CLOUD = [
    'cloud-dev.tianmiantech.com',
    'cloud-fat.tianmiantech.com',
    'cloud.tianmiantech.com',
  ];

export const TOKEN = `iam-${process.env.HOST_ENV}-x-user-token`;

export const getOrigin = () => {

    const { origin, host } = window.location;

    if(CLOUD.includes(host)){
        return origin.replace('cloud', 'tbapi');
    }
    return origin;
};

export const getServiceName = () => {
    // if(!isQianKun()) return '';

    const { pathname } = window.location;
    const split = pathname.split('/') || [];

    return isQianKun() ? split[2] || '' : split[1] || '' ;
};

export const appCode = () => getServiceName() || 'fusion';


// 全局变量
export function baseURL (){
    const appCodes = appCode();
    const lastTwo = appCodes.substring(appCodes.length - 2);
    const second = /^\d+$/.test(lastTwo) ? lastTwo : '';
    if(window._wefeApi){
        /** 提供给客户快速修改请求地址，一般通过修改html head */
        return window._wefeApi;
    }
    return isQianKun() ? `${getOrigin()}/${appCode()}-service` : `${process.env[`VUE_APP_${process.env.HOST_ENV}`]}${second ? `-${second}` : ''}`;
}

// localstorage name

//  拥有权限的菜单列表
export const MENU_LIST=`${appCode()}_menu_list`;
