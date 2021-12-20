/*!
 * @author claude
 * date 07/05/2019
 * 公共 route 配置文件
 */

/**
 * @param {meta: requiresAuth} Boolean false 无需登录权限即可进入
 * @param {meta: requiresLogout} Boolean true 必须未登录才能访问
 * @param {meta: permission} Boolean 表明当前用户是否有权限访问
 * @param {meta: icon} String 当前菜单的图标
 * @param {meta: title} String 当前菜单的标题
 * @param {meta: asmenu} Boolean 只显示1级菜单
 */
const { pathname } = window.location;
const prefixPath = process.env.NODE_ENV === 'development' ? '/' : `/${process.env.CONTEXT_ENV}/`;

// 主框架路由
const baseRoutes = [
    {
        path: `${prefixPath}`,
        meta: {
        title: '模型管理',
            icon:  'el-icon-monitor',
            index: 0,
        },
        component: () => import('@comp/LayoutBase.vue'),
        children:  [
            {
                path: `${prefixPath}`,
                name: 'model-list',
                meta: {
                    title: '模型列表',
                    index: '0-0',
                },
                component: () => import('@views/model/model-list.vue'),
            },
            {
                path: `${prefixPath}model-view`,
                    name: 'model-view',
                meta: {
                title:  '模型详情',
                    index:  '0-1',
                    hidden: true,
                    active: `${prefixPath}model-view`,
            },
                component: () =>
                import('@views/model/model-view.vue'),
            },
        ],
    },
    {
        path: `${prefixPath}`,
        meta: {
            title: '数据源管理',
            icon:  'el-icon-monitor',
            index: 4,
        },
        component: () => import('@comp/LayoutBase.vue'),
        children:  [
            {
                path: `${prefixPath}data-souce-list`,
                name: 'data-resouce-list',
                meta: {
                    title:  '数据库配置',
                    index:  '4-1',
                    active: `${prefixPath}data-souce-list`,
                },
                component: () => import('@views/data_source/data-source-list.vue'),
            },
            {
                path: `${prefixPath}data-source-view`,
                name: 'data-source-view',
                meta: {
                    title:  '数据库配置详情',
                    index:  '4-2',
                    hidden: true,
                    active: `${prefixPath}data-source-view`,
                },
                component: () => import('@views/data_source/data-source-view.vue'),
            },
        ],
    },
    {
        path: `${prefixPath}`,
        meta: {
            title: '服务中心',
            icon:  'el-icon-monitor',
            index: 5,
        },
        component: () => import('@comp/LayoutBase.vue'),
        children:  [
            {
                path: `${prefixPath}service-list`,
                name: 'service-list',
                meta: {
                    title:  '服务列表',
                    index:  '5-1',
                    active: `${prefixPath}service-list`,
                },
                component: () => import('@views/service/service-list.vue'),
            },
            {
                path: `${prefixPath}service-view`,
                name: 'service-view',
                meta: {
                    title:  '服务详情',
                    index:  '5-2',
                    hidden: true,
                    active: `${prefixPath}service-view`,
                },
                component: () => import('@views/service/service-view.vue'),
            },
        ],
    },
    {
        path: `${prefixPath}member`,
        meta: {
        title: 'member管理',
            icon:  'el-icon-user-solid',
            index: 1,
        },
        component: () => import('@comp/LayoutBase.vue'),
        children:  [
            {
                path: `${prefixPath}member-list`,
                name: 'member-list',
                meta: {
                    title: '成员列表',
                    index: '1-0',
                },
                component: () => import('@views/member/member-list.vue'),
            },
            {
                path: `${prefixPath}member-view`,
                name: 'member-view',
                meta: {
                title:  '模型详情',
                    index:  '1-1',
                    hidden: true,
                    active: `${prefixPath}member-view`,
            },
                component: () =>
                import('@views/member/member-view.vue'),
            },
        ],
    },
    {
        path: `${prefixPath}logger`,
        meta: {
            title: '日志管理',
            icon:  'el-icon-notebook-1',
            index: 2,
        },
        component: () => import('@comp/LayoutBase.vue'),
        children:  [
            {
                path: `${prefixPath}log-list`,
                name: 'log-list',
                meta: {
                    title: '调用记录',
                    index: '2-0',
                },
                component: () => import('@views/logger/log-list.vue'),
            },
            {
                path: `${prefixPath}log-statistics`,
                    name: 'log-statistics',
                meta: {
                title:  '调用统计',
                    index:  '2-1',
                    active: `${prefixPath}log-statistics`,
            },
                component: () =>
                import('@views/logger/log-statistics.vue'),
            },
            {
                path: `${prefixPath}log-view`,
                    name: 'log-view',
                meta: {
                title:  '调用详情',
                    index:  '2-2',
                    hidden: true,
                    active: `${prefixPath}log-view`,
            },
                component: () =>
                import('@views/logger/log-view.vue'),
            },

        ],
    },
    {
        path: `${prefixPath}global-setting`,
            meta: {
            title: '全局设置',
            icon:  'el-icon-setting',
            index: 3,
        },
        component: () => import('@comp/LayoutBase.vue'),
        children:  [
            {
                path: `${prefixPath}global-setting-view`,
                name: 'global-setting-view',
                meta: {
                    title: 'member信息',
                    index: '3-0',
                },
                component: () =>
            import('@views/global_setting/global-setting-view.vue'),
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
        component: () => import('@views/sign/login.vue'),
    },
    {
        path: `${prefixPath}register`,
        name: 'register',
        meta: {
            title:          '注册',
            requiresAuth:   false,
            requiresLogout: true,
        },
        component: () => import('@views/sign/register.vue'),
    },
    {
        path: `${prefixPath}find-password`,
        name: 'find-password',
        meta: {
            title:          '找回密码',
            requiresAuth:   false,
            requiresLogout: true,
        },
        component: () => import('@views/sign/find-password.vue'),
    },
    {
        path: `${prefixPath}notfound`,
        name: 'notfound',
        meta: {
            requiresAuth: false,
            hidden:       true,
        },
        component: () => import('@views/error/404.vue'),
    },
    {
        path: `${prefixPath}forbidden`,
        name: 'forbidden',
        meta: {
            requiresAuth: false,
            hidden:       true,
        },
        component: () => import('@views/error/403.vue'),
    },
    {
        path:      `${prefixPath}init`,
            name:      'init',
        component: () => import('@views/global_setting/global-setting-initialize.vue'),
    },
    {
        path:     '*',
        redirect: {
            path:  `${prefixPath}login`,
            query: {
                redirect: pathname,
            },
        },
    },
];

export default baseRoutes;
