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
            title: '模型中心',
            icon:  'el-icon-monitor',
        },
        component: () => import('@comp/LayoutBase.vue'),
        children:  [
            {
                path: `${prefixPath}`,
                name: 'index',
                meta: {
                    title: '模型列表',
                },
                component: () => import('@views/model/model-list.vue'),
            },
            {
                path: `${prefixPath}model-view`,
                name: 'model-view',
                meta: {
                    title:  '模型详情',
                    hidden: true,
                    active: `${prefixPath}model-view`,
                },
                component: () => import('@views/model/model-view.vue'),
            },
            {
                path: `${prefixPath}model-import`,
                name: 'model-import',
                meta: {
                    title: '模型导入',
                },
                component: () => import('@views/model/model-import.vue'),
            },
        ],
    },
    {
        path: `${prefixPath}partner`,
        meta: {
            title: '合作者管理',
            icon:  'el-icon-s-custom',
        },
        component: () => import('@comp/LayoutBase.vue'),
        children:  [
            {
                path: `${prefixPath}partner-list`,
                name: 'partner-list',
                meta: {
                    title:           '合作者列表',
                    loginAndRefresh: true,
                    active:          `${prefixPath}partner-list`,
                },
                component: () => import('@views/partner/partner-list.vue'),
            },
            {
                path: `${prefixPath}partner-add`,
                name: 'partner-add',
                meta: {
                    title:  '新增合作者',
                    hidden: true,
                },
                component: () => import('@views/partner/partner-add.vue'),
            },

            {
                path: `${prefixPath}partner-service-list`,
                name: 'partner-service-list',
                meta: {
                    title:           '合作者服务列表',
                    loginAndRefresh: true,
                },
                component: () => import('@views/partner/partner-service-list.vue'),
            },
            {
                path: `${prefixPath}partner-service-add`,
                name: 'partner-service-add',
                meta: {
                    title:           '新增合作者服务',
                    loginAndRefresh: true,
                    hidden:          true,
                },

                component: () => import('@views/partner/partner-service-add.vue'),
            },
            {
                path: `${prefixPath}partner-service-edit`,
                name: 'partner-service-edit',
                meta: {
                    title:           '编辑合作者服务',
                    loginAndRefresh: true,
                    hidden:          true,
                },

                component: () => import('@views/partner/partner-service-edit.vue'),
            },
            {
                path: `${prefixPath}partner-edit`,
                name: 'partner-edit',
                meta: {
                    title:           '修改合作者',
                    loginAndRefresh: true,
                    hidden:          true,
                },

                component: () => import('@views/partner/partner-edit.vue'),
            },
            {
                path: `${prefixPath}activate-service-add`,
                name: 'activate-service-add',
                meta: {
                    title:           '激活服务',
                    loginAndRefresh: true,
                    hidden:          true,
                },

                component: () => import('@views/partner/activate-service-add.vue'),
            },
            {
                path: `${prefixPath}activate-service-edit`,
                name: 'activate-service-edit',
                meta: {
                    title:           '激活服务',
                    loginAndRefresh: true,
                    hidden:          true,
                },

                component: () => import('@views/partner/activate-service-edit.vue'),
            },
        ],
    },
    {
        path: `${prefixPath}service`,
        meta: {
            title: '服务中心',
            icon:  'el-icon-service',
        },
        component: () => import('@comp/LayoutBase.vue'),
        children:  [
            {
                path: `${prefixPath}service-list`,
                name: 'service-list',
                meta: {
                    title:           '我的服务',
                    active:          `${prefixPath}service-list`,
                    loginAndRefresh: true,
                },
                component: () => import('@views/service/service-list.vue'),
            },
            {
                path: `${prefixPath}union-service-list`,
                name: 'union-service-list',
                meta: {
                    title:           '联邦服务',
                    active:          `${prefixPath}union-service-list`,
                    loginAndRefresh: true,
                },
                component: () => import('@views/service/union-service-list.vue'),
            },
            {
                path: `${prefixPath}service-view`,
                name: 'service-view',
                meta: {
                    title:  '服务详情',
                    hidden: true,
                    active: `${prefixPath}service-view`,
                },
                component: () => import('@views/service/service-view.vue'),
            },
        ],
    },
    {
        path: `${prefixPath}fee`,
        meta: {
            title: '计费中心',
            icon:  'el-icon-wallet',
        },
        component: () => import('@comp/LayoutBase.vue'),
        children:  [
            {
                path: `${prefixPath}request-statistics`,
                name: 'request-statistics',
                meta: {
                    title:           '调用信息',
                    loginAndRefresh: true,
                    active:          `${prefixPath}request-statistics`,
                },
                component: () => import('@views/fee/request-statistics.vue'),
            },
            {
                path: `${prefixPath}fee-detail`,
                name: 'fee-detail',
                meta: {
                    title:           '计费概览',
                    loginAndRefresh: true,
                },
                component: () => import('@views/fee/fee-detail.vue'),
            },
            {
                path: `${prefixPath}payments-records`,
                name: 'payments-records',
                meta: {
                    title:           '收支记录',
                    loginAndRefresh: true,
                },
                component: () => import('@views/fee/payments-records.vue'),
            },
            {
                path: `${prefixPath}payments-records-add`,
                name: 'payments-records-add',
                meta: {
                    title:           '新增收支记录',
                    loginAndRefresh: true,
                    hidden:          true,
                },
                component: () => import('@views/fee/payments-records-add.vue'),
            },
        ],
    },
    {
        path: `${prefixPath}account`,
        meta: {
            title:            '用户中心',
            icon:             'el-icon-user-solid',
            normalUserCanSee: false,
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
        ],
    },
    {
        path: `${prefixPath}member`,
        meta: {
            title: 'member管理',
            icon:  'el-icon-user',
        },
        component: () => import('@comp/LayoutBase.vue'),
        children:  [
            {
                path: `${prefixPath}member-list`,
                name: 'member-list',
                meta: {
                    title: '成员列表',
                },
                component: () => import('@views/member/member-list.vue'),
            },
            {
                path: `${prefixPath}member-view`,
                name: 'member-view',
                meta: {
                    title:  '模型详情',
                    hidden: true,
                    active: `${prefixPath}member-view`,
                },
                component: () => import('@views/member/member-view.vue'),
            },
        ],
    },
    {
        path: `${prefixPath}logger`,
        meta: {
            title: '日志管理',
            icon:  'el-icon-notebook-1',
        },
        component: () => import('@comp/LayoutBase.vue'),
        children:  [
            {
                path: `${prefixPath}order-list`,
                name: 'order-list',
                meta: {
                    title: '订单列表',
                },
                component: () => import('@views/service_order/order-list.vue'),
            },
            {
                path: `${prefixPath}order-statistics`,
                name: 'order-statistics',
                meta: {
                    title: '订单统计',
                },
                component: () => import('@views/service_order/order-statistics.vue'),
            },
            {
                path: `${prefixPath}record-list`,
                name: 'record-list',
                meta: {
                    title: '调用记录',
                },
                component: () => import('@views/logger/log-list.vue'),
            },
            {
                path: `${prefixPath}log-statistics`,
                name: 'log-statistics',
                meta: {
                    title:  '调用统计',
                    active: `${prefixPath}log-statistics`,
                },
                component: () => import('@views/logger/log-statistics.vue'),
            },
            {
                path: `${prefixPath}log-view`,
                name: 'log-view',
                meta: {
                    title:  '调用详情',
                    hidden: true,
                    active: `${prefixPath}log-view`,
                },
                component: () => import('@views/logger/log-view.vue'),
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
                path: `${prefixPath}data-souce-list`,
                name: 'data-resouce-list',
                meta: {
                    title:  '数据源配置',
                    active: `${prefixPath}data-souce-list`,
                },
                component: () => import('@views/data_source/data-source-list.vue'),
            },
            {
                path: `${prefixPath}global-setting-view`,
                name: 'global-setting-view',
                meta: {
                    title: 'member信息',
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
