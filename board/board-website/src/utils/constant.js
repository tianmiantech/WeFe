import { isQianKun } from '@src/http/utils';


const CLOUD = [
    'cloud-dev.tianmiantech.com',
    'cloud-fat.tianmiantech.com',
    'cloud.tianmiantech.com',
  ];

  const local = [
    'localhost',
  ]

export const TOKEN = `iam-${process.env.HOST_ENV}-x-user-token`;

export const getOrigin = () => {

    const { origin, host, hostname } = window.location;

    if(local.includes(hostname)){
        return 'https://tbapi-dev.tianmiantech.com'
    }

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

export const appCode = () => getServiceName() || 'board';


// 全局变量
export function baseURL (){
    const appCodes = appCode();
    const lastTwo = appCodes.substring(appCodes.length - 2);
    const second = /^\d+$/.test(lastTwo) ? lastTwo : '';
    return isQianKun() ? `${getOrigin()}/${appCode()}-service` : `${process.env[`VUE_APP_${process.env.HOST_ENV}`]}-${second || '01'}`;
}

// localstorage name

//  拥有权限的菜单列表
export const MENU_LIST=`${appCode()}_menu_list`;
