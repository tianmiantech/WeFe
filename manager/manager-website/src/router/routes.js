/*
 * @author claude
 */

/**
 * @param {meta: hidden} Boolean false           show in menus
 * @param {meta: requiresAuth} Boolean false     no need to login
 * @param {meta: requiresLogout} Boolean true    must to login
 * @param {meta: loginAndRefresh} Boolean        login with dialog refresh current page
 * @param {meta: active} String                  highlight parent path
 * @param {meta: icon} String                    menu icon
 * @param {meta: title} String                   menu title
 * @param {meta: asmenu} Boolean                 show as a menu, no children menu
 * @param {meta: navigation} Boolean             show page fixed navigation on the right
 */


import { getServiceName } from './../utils/constant';


export const prefixPath = () => {
    const serviceName = getServiceName();

    return serviceName ? `/${serviceName}/` : '/';
};

function createBaseRoute(){
    return [
        {
            path: prefixPath(),
            name: 'data-center',
            meta: {
                title:          '联邦管理',
                icon:           'connection',
                requiresLogout: false,
            },
            redirect:  `${prefixPath()}home`,
            component: () => import('@comp/LayoutBase.vue'),
            children:  [
                {
                    path: `${prefixPath()}home`,
                    name: 'index',
                    meta: {
                        loginAndRefresh: true,
                        title:           '成员列表',
                    },
                    component: () => import('../views/account/member-list'),
                },
                {
                    path: `${prefixPath()}data-list`,
                    name: 'data-list',
                    meta: {
                        loginAndRefresh: true,
                        title:           '联邦资源',
                    },
                    component: () => import('../views/data-center/data-list'),
                },
                {
                    path: `${prefixPath()}data-view`,
                    name: 'data-view',
                    meta: {
                        loginAndRefresh: true,
                        hidden:          true,
                        title:           '联邦资源详情',
                        active:          `${prefixPath()}data-list`,
                    },
                    component: () => import('../views/data-center/data-view'),
                },
                {
                    path: `${prefixPath()}keywords`,
                    name: 'keywords',
                    meta: {
                        loginAndRefresh: true,
                        title:           '关键词管理',
                    },
                    component: () => import('../views/data-center/keywords'),
                },
            ],
        },
        {
            path: `${prefixPath()}authorize`,
            name: 'authorize-manage',
            meta: {
                title:          '企业实名认证管理',
                icon:           'collection',
                requiresLogout: false,
            },
            component: () => import('@comp/LayoutBase.vue'),
            children:  [
                {
                    path: `${prefixPath()}authorize-types`,
                    name: 'authorize-types',
                    meta: {
                        loginAndRefresh: true,
                        title:           '认证类型',
                    },
                    component: () => import('../views/authorize-list'),
                },
                {
                    path: `${prefixPath()}agreement`,
                    name: 'agreement',
                    meta: {
                        loginAndRefresh: true,
                        title:           '认证协议',
                    },
                    component: () => import('../views/agreement'),
                },
            ],
        },
        {
            path: `${prefixPath()}cert-list`,
            name: 'cert-list-home',
            meta: {
                title:          '证书管理',
                icon:           'postcard',
                requiresLogout: false,
            },
            component: () => import('@comp/LayoutBase.vue'),
            children:  [
                {
                    path: `${prefixPath()}cert-list`,
                    name: 'cert-list',
                    meta: {
                        loginAndRefresh: true,
                        title:           '证书列表',
                    },
                    component: () => import('../views/cert/cert-list'),
                },
                {
                    path: `${prefixPath()}cert-view`,
                    name: 'cert-view',
                    meta: {
                        loginAndRefresh: true,
                        hidden:          true,
                        title:           '证书详情',
                    },
                    component: () => import('../views/cert/cert-view'),
                },
                {
                    path: `${prefixPath()}csr-list`,
                    name: 'csr-list',
                    meta: {
                        loginAndRefresh: true,
                        hidden:          true,
                        title:           '证书请求列表',
                    },
                    component: () => import('../views/cert/csr-list'),
                },
                {
                    path: `${prefixPath()}cert-key-list`,
                    name: 'cert-key-list',
                    meta: {
                        loginAndRefresh: true,
                        hidden:          true,
                        title:           '私钥列表',
                    },
                    component: () => import('../views/cert/cert-key-list'),
                },
            ],
        },
        {
            path: `${prefixPath()}union-list`,
            name: 'union-manage',
            meta: {
                title:          'union节点管理',
                icon:           'operation',
                requiresLogout: false,
            },
            component: () => import('@comp/LayoutBase.vue'),
            children:  [
                {
                    path: `${prefixPath()}union-list`,
                    name: 'union-list',
                    meta: {
                        loginAndRefresh: true,
                        title:           'union节点管理',
                    },
                    component: () => import('../views/union-list'),
                },
            ],
        },
        /* {
            path: `${prefixPath()}user-list`,
            meta: {
                title:          '用户管理',
                icon:           'user',
                requiresLogout: false,
            },
            component: () => import('@comp/LayoutBase.vue'),
            children:  [
                {
                    path: `${prefixPath()}user-list`,
                    name: 'user-list',
                    meta: {
                        loginAndRefresh:  true,
                        title:            '用户列表',
                        normalUserCanSee: true,
                    },
                    component: () => import('../views/system/user-list'),
                },
                {
                    path: `${prefixPath()}log-list`,
                    name: 'log-list',
                    meta: {
                        loginAndRefresh:  true,
                        title:            '用户日志',
                        normalUserCanSee: false,
                    },
                    component: () => import('../views/account/log-list'),
                },
                {
                    path: `${prefixPath()}account-setting`,
                    name: 'account-setting',
                    meta: {
                        loginAndRefresh: true,
                        title:           '账户设置',
                    },
                    component: () => import('../views/system/account-setting'),
                },
            ],
        },
        {
            path: `${prefixPath()}login`,
            name: 'login',
            meta: {
                title:          '登录',
                requiresAuth:   false,
                requiresLogout: true,
            },
            component: () => import('../views/sign/login.vue'),
        },
        {
            path: `${prefixPath()}register`,
            name: 'register',
            meta: {
                title:          '注册',
                requiresAuth:   false,
                requiresLogout: true,
            },
            component: () => import('../views/sign/register.vue'),
        },
        {
            path: `${prefixPath()}change-password`,
            name: 'change-password',
            meta: {
                title:        '修改登录密码',
                hidden:       true,
                requiresAuth: true,
            },
            component: () => import('../views/sign/change-password.vue'),
        },
        {
            path: `${prefixPath()}find-password`,
            name: 'find-password',
            meta: {
                title:          '找回密码',
                requiresAuth:   false,
                requiresLogout: true,
            },
            component: () => import('../views/sign/find-password.vue'),
        }, */
        {
            path: `${prefixPath()}notfound`,
            name: 'notfound',
            meta: {
                requiresAuth: false,
                hidden:       true,
            },
            component: () => import('../views/error/404.vue'),
        },
        {
            path: `${prefixPath()}forbidden`,
            name: 'forbidden',
            meta: {
                requiresAuth: false,
                hidden:       true,
            },
            component: () => import('../views/error/403.vue'),
        },
        /* {
            path:     `${prefixPath()}:catchAll(.*)`,
            redirect: {
                path: `${prefixPath()}notfound`,
            },
        }, */
    ];
}

export default createBaseRoute;
