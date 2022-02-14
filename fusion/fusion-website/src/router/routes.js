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
            title:          '主页',
            requiresLogout: false,
            asmenu:         true,
        },
        component: () => import('@comp/LayoutBase.vue'),
        children:  [
            {
                path: `${prefixPath}index`,
                name: 'index',
                meta: {
                    title:  '主页',
                    icon:   'el-icon-monitor',
                    active: `${prefixPath}index`,
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
            index: 0,
        },
        component: () => import('@comp/LayoutBase.vue'),
        children:  [
            {
                path: `${prefixPath}data-set-list`,
                name: 'data-set-list',
                meta: {
                    title:  '数据集',
                    index:  '0-0',
                    active: `${prefixPath}data-set-list`,
                },
                component: () => import('@views/index/data-set-list.vue'),
            },
            {
                path: `${prefixPath}filter-list`,
                name: 'filter-list',
                meta: {
                    title:  '布隆过滤器',
                    index:  '0-1',
                    active: `${prefixPath}filter-list`,
                },
                component: () => import('@views/index/filter-list.vue'),
            },
            {
                path: `${prefixPath}data-set-view`,
                name: 'data-set-view',
                meta: {
                    title:  '新增数据集',
                    index:  '0-2',
                    hidden: true,
                    active: `${prefixPath}data-set-view`,
                },
                component: () => import('@views/index/data-set-view.vue'),
            },
            {
                path: `${prefixPath}data-set-detail`,
                name: 'data-set-detail',
                meta: {
                    title:  '数据集详情',
                    index:  '0-2',
                    hidden: true,
                    active: `${prefixPath}data-set-detail`,
                },
                component: () => import('@views/index/data-set-detail.vue'),
            },
            {
                path: `${prefixPath}filter-view`,
                name: 'filter-view',
                meta: {
                    title:  '新增过滤器',
                    index:  '0-3',
                    hidden: true,
                    active: `${prefixPath}filter-view`,
                },
                component: () => import('@views/index/filter-view.vue'),
            },
            {
                path: `${prefixPath}filter-data-detail`,
                name: 'filter-data-detail',
                meta: {
                    title:  '过滤器详情',
                    index:  '0-3',
                    hidden: true,
                    active: `${prefixPath}filter-data-detail`,
                },
                component: () => import('@views/index/filter-data-detail.vue'),
            },
            {
                path: `${prefixPath}data-souce-list`,
                name: 'data-resouce-list',
                meta: {
                    title:  '数据库配置',
                    index:  '0-4',
                    active: `${prefixPath}data-souce-list`,
                },
                component: () => import('@views/index/data-souce-list.vue'),
            },
            {
                path: `${prefixPath}data-source-view`,
                name: 'data-source-view',
                meta: {
                    title:  '数据库配置详情',
                    index:  '0-3',
                    hidden: true,
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
            index: 1,
        },
        component: () => import('@comp/LayoutBase.vue'),
        children:  [
            {
                path: `${prefixPath}task-list`,
                name: 'task-list',
                meta: {
                    title: '任务列表',
                    index: '1-0',
                },
                component: () => import('@views/task/task-list.vue'),
            },
            {
                path: `${prefixPath}task-task-list`,
                name: 'task-pending-list',
                meta: {
                    title:  '任务审核',
                    index:  '1-1',
                    active: `${prefixPath}task-pending-list`,
                    tips:   0,
                },
                component: () => import('@views/task/task-pending-list.vue'),
            },
            {
                path: `${prefixPath}task-pending-view`,
                name: 'task-pending-view',
                meta: {
                    title:  '任务详情',
                    index:  '1-2',
                    hidden: true,
                    active: `${prefixPath}task-pending-view`,
                },
                component: () => import('@views/task/task-pending-view.vue'),
            },
            {
                path: `${prefixPath}task-view`,
                name: 'task-view',
                meta: {
                    title:  '任务详情',
                    index:  '1-3',
                    hidden: true,
                    active: `${prefixPath}task-view`,
                },
                component: () => import('@views/task/task-view.vue'),
            },
            {
                path: `${prefixPath}task-add`,
                name: 'task-add',
                meta: {
                    title:  '添加任务',
                    index:  '1-4',
                    hidden: true,
                    active: `${prefixPath}task-add`,
                },
                component: () => import('@views/task/task-add.vue'),
            },
        ],
    },
    {
        path: `${prefixPath}partner`,
        meta: {
            title: '合作中心',
            icon:  'el-icon-user-solid',
            index: 2,
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
                    title: '配置信息',
                    index: '3-0',
                },
                component: () => import('@views/global_setting/global-setting-view.vue'),
            },
        ],
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
