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
const prefixPath = process.env.NODE_ENV === 'development' ? '/' : `${process.env.CONTEXT_ENV ? `/${process.env.CONTEXT_ENV}/` : '/'}`;

// all routes
const baseRoutes = [
    {
        path: prefixPath,
        meta: {
            title:          '联邦管理',
            requiresLogout: false,
        },
        component: () => import('@comp/LayoutBase.vue'),
        children:  [
            {
                path: prefixPath,
                name: 'index',
                meta: {
                    loginAndRefresh: true,
                    title:           '成员列表',
                },
                component: () => import('../views/account/member-list'),
            },
            {
                path: `${prefixPath}data-list`,
                name: 'data-list',
                meta: {
                    loginAndRefresh: true,
                    title:           '联邦资源',
                },
                component: () => import('../views/data-center/data-list'),
            },
            {
                path: `${prefixPath}data-view`,
                name: 'data-view',
                meta: {
                    loginAndRefresh: true,
                    hidden:          true,
                    title:           '联邦资源详情',
                    active:          `${prefixPath}data-list`,
                },
                component: () => import('../views/data-center/data-view'),
            },
            {
                path: `${prefixPath}keywords`,
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
        path: `${prefixPath}authorize`,
        meta: {
            title:          '企业实名认证管理',
            requiresLogout: false,
        },
        component: () => import('@comp/LayoutBase.vue'),
        children:  [
            {
                path: `${prefixPath}authorize-types`,
                name: 'authorize-types',
                meta: {
                    loginAndRefresh: true,
                    title:           '认证类型',
                },
                component: () => import('../views/authorize-list'),
            },
            {
                path: `${prefixPath}agreement`,
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
        path: `${prefixPath}union-list`,
        meta: {
            title:          'union节点管理',
            requiresLogout: false,
        },
        component: () => import('@comp/LayoutBase.vue'),
        children:  [
            {
                path: `${prefixPath}union-list`,
                name: 'union-list',
                meta: {
                    loginAndRefresh: true,
                    title:           'union节点管理',
                },
                component: () => import('../views/union-list'),
            },
        ],
    },
    {
        path: `${prefixPath}user-list`,
        meta: {
            title:          '用户管理',
            requiresLogout: false,
        },
        component: () => import('@comp/LayoutBase.vue'),
        children:  [
            {
                path: `${prefixPath}user-list`,
                name: 'user-list',
                meta: {
                    loginAndRefresh: true,
                    title:           '用户列表',
                },
                component: () => import('../views/system/user-list'),
            },
            {
                path: `${prefixPath}account-setting`,
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
        path: `${prefixPath}login`,
        name: 'login',
        meta: {
            title:          '登录',
            requiresAuth:   false,
            requiresLogout: true,
        },
        component: () => import('../views/sign/login.vue'),
    },
    {
        path: `${prefixPath}register`,
        name: 'register',
        meta: {
            title:          '注册',
            requiresAuth:   false,
            requiresLogout: true,
        },
        component: () => import('../views/sign/register.vue'),
    },
    {
        path: `${prefixPath}change-password`,
        name: 'change-password',
        meta: {
            title:        '修改登录密码',
            requiresAuth: true,
        },
        component: () => import('../views/sign/change-password.vue'),
    },
    {
        path: `${prefixPath}find-password`,
        name: 'find-password',
        meta: {
            title:          '找回密码',
            requiresAuth:   false,
            requiresLogout: true,
        },
        component: () => import('../views/sign/find-password.vue'),
    },
    {
        path: `${prefixPath}notfound`,
        name: 'notfound',
        meta: {
            requiresAuth: false,
            hidden:       true,
        },
        component: () => import('../views/error/404.vue'),
    },
    {
        path: `${prefixPath}forbidden`,
        name: 'forbidden',
        meta: {
            requiresAuth: false,
            hidden:       true,
        },
        component: () => import('../views/error/403.vue'),
    },
    {
        path:     `${prefixPath}:catchAll(.*)`,
        redirect: {
            path: `${prefixPath}notfound`,
        },
    },
];

export default baseRoutes;
