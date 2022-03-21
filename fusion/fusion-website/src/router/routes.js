/*!
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
 * @param {meta: tips} Boolean                  red tips
 */
const { pathname } = window.location;
const prefixPath = process.env.NODE_ENV === 'development' ? '/' : `/${process.env.CONTEXT_ENV}/`;

const baseRoutes = [
    {
        path: `${prefixPath}`,
        meta: {
            title:  '主页',
            asmenu: true,
        },
        component: () => import('@comp/LayoutBase.vue'),
        children:  [
            {
                path: `${prefixPath}`,
                name: 'index',
                meta: {
                    title:  '主页',
                    icon:   'el-icon-monitor',
                    active: `${prefixPath}`,
                },
                component: () => import('../views/index/dataPanel.vue'),
            },
        ],
    },
    {
        path: `${prefixPath}`,
        meta: {
            title: '资源中心',
            icon:  'el-icon-office-building',
        },
        component: () => import('@comp/LayoutBase.vue'),
        children:  [
            {
                path: `${prefixPath}data-set-list`,
                name: 'data-set-list',
                meta: {
                    title:  '我的数据集',
                    active: `${prefixPath}data-set-list`,
                },
                component: () => import('@views/index/data-set-list.vue'),
            },
            {
                path: `${prefixPath}filter-list`,
                name: 'filter-list',
                meta: {
                    title:  '布隆过滤器',
                    active: `${prefixPath}filter-list`,
                },
                component: () => import('@views/index/filter-list.vue'),
            },
            {
                path: `${prefixPath}data-set-view`,
                name: 'data-set-view',
                meta: {
                    hidden: true,
                    title:  '新增数据集',
                    active: `${prefixPath}data-set-view`,
                },
                component: () => import('@views/index/data-set-view.vue'),
            },
            {
                path: `${prefixPath}data-set-detail`,
                name: 'data-set-detail',
                meta: {
                    hidden: true,
                    title:  '数据集详情',
                    active: `${prefixPath}data-set-detail`,
                },
                component: () => import('@views/index/data-set-detail.vue'),
            },
            {
                path: `${prefixPath}filter-view`,
                name: 'filter-view',
                meta: {
                    hidden: true,
                    title:  '新增过滤器',
                    active: `${prefixPath}filter-view`,
                },
                component: () => import('@views/index/filter-view.vue'),
            },
            {
                path: `${prefixPath}filter-data-detail`,
                name: 'filter-data-detail',
                meta: {
                    hidden: true,
                    title:  '过滤器详情',
                    active: `${prefixPath}filter-data-detail`,
                },
                component: () => import('@views/index/filter-data-detail.vue'),
            },
            {
                path: `${prefixPath}data-souce-list`,
                name: 'data-resouce-list',
                meta: {
                    title:  '数据库配置',
                    active: `${prefixPath}data-souce-list`,
                },
                component: () => import('@views/index/data-souce-list.vue'),
            },
            {
                path: `${prefixPath}data-source-view`,
                name: 'data-source-view',
                meta: {
                    hidden: true,
                    title:  '数据库配置详情',
                    active: `${prefixPath}data-source-view`,
                },
                component: () => import('@views/index/data-source-view.vue'),
            },
        ],
    },
    {
        path: `${prefixPath}task`,
        meta: {
            title: '任务中心',
            icon:  'el-icon-s-order',
        },
        component: () => import('@comp/LayoutBase.vue'),
        children:  [
            {
                path: `${prefixPath}task-add`,
                name: 'task-add',
                meta: {
                    title:  '发起任务',
                    active: `${prefixPath}task-add`,
                },
                component: () => import('@views/task/task-add.vue'),
            },
            {
                path: `${prefixPath}task-list`,
                name: 'task-list',
                meta: {
                    title: '任务列表',
                    tips:  0,
                },
                component: () => import('@views/task/task-list.vue'),
            },
            {
                path: `${prefixPath}task-pending-view`,
                name: 'task-pending-view',
                meta: {
                    hidden: true,
                    title:  '任务详情',
                    active: `${prefixPath}task-pending-view`,
                },
                component: () => import('@views/task/task-pending-view.vue'),
            },
            {
                path: `${prefixPath}task-view`,
                name: 'task-view',
                meta: {
                    hidden: true,
                    title:  '任务详情',
                    active: `${prefixPath}task-view`,
                },
                component: () => import('@views/task/task-view.vue'),
            },
        ],
    },
    {
        path: `${prefixPath}partner`,
        meta: {
            title: '合作中心',
            icon:  'el-icon-user-solid',
        },
        component: () => import('@comp/LayoutBase.vue'),
        children:  [
            {
                path: `${prefixPath}partner-list`,
                name: 'partner-list',
                meta: {
                    title: '合作伙伴',
                    index: '2-0',
                },
                component: () => import('@views/partner/partner-list.vue'),
            },
        ],
    },
    {
        path: `${prefixPath}account`,
        meta: {
            title: '用户管理',
            icon:  'el-icon-user',
        },
        component: () => import('@comp/LayoutBase.vue'),
        children:  [
            {
                path: `${prefixPath}account-list`,
                name: 'account-list',
                meta: {
                    loginAndRefresh:  true,
                    title:            '用户列表',
                    normalUserCanSee: false,
                },
                component: () => import('../views/account/account-list'),
            },
            {
                path: `${prefixPath}log-list`,
                name: 'log-list',
                meta: {
                    loginAndRefresh:  true,
                    title:            '用户日志',
                    normalUserCanSee: false,
                },
                component: () => import('../views/account/log-list'),
            },
            {
                path: `${prefixPath}account-setting`,
                name: 'account-setting',
                meta: {
                    loginAndRefresh: true,
                    title:           '账户设置',
                },
                component: () => import('../views/system-config/account-setting'),
            },
        ],
    },
    {
        path: `${prefixPath}global-setting`,
        meta: {
            title: '全局设置',
            icon:  'el-icon-setting',
        },
        component: () => import('@comp/LayoutBase.vue'),
        children:  [
            {
                path: `${prefixPath}global-setting-view`,
                name: 'global-setting-view',
                meta: {
                    title:           '配置信息',
                    loginAndRefresh: true,
                },
                component: () => import('@views/global_setting/global-setting-view.vue'),
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
        path: `${prefixPath}init`,
        name: 'init',
        meta: {
            hidden: true,
        },
        component: () => import('../views/member/member-initialize'),
    },
    {
        path:     '*',
        redirect: {
            path:  `${prefixPath}`,
            query: {
                redirect: pathname,
            },
        },
    },
];

export default baseRoutes;
