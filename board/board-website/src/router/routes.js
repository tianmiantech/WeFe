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
 * @param {meta: notshowattag} Boolean           not show this page at tag bar
 */
import { getServiceName } from './../utils/constant';


export const prefixPath = () => {
    const serviceName = getServiceName();

    return serviceName ? `/${serviceName}/` : '/';
};

// all routes

function baseRoutes(){
    return [
        {
            path: prefixPath(),
            name: 'index',
            meta: {
                title:  '主页',
                asmenu: true,
            },
            redirect:  `${prefixPath()}home`,
            component: () => import('@comp/LayoutBase.vue'),
            children:  [
                {
                    path: `${prefixPath()}home`,
                    name: 'home',
                    meta: {
                        title:           '主页',
                        icon:            'monitor',
                        loginAndRefresh: true,
                    },
                    component: () => import('../views/index/dashboard.vue'),
                },
            ],
        },
        {
            path: `${prefixPath()}union-center`,
            name: 'union-center',
            meta: {
                title: '联邦',
                icon:  'connection',
            },
            component: () => import('@comp/LayoutBase.vue'),
            children:  [
                {
                    path: `${prefixPath()}member-list`,
                    name: 'member-list',
                    meta: {
                        loginAndRefresh: true,
                        title:           '成员列表',
                    },
                    component: () => import('../views/account/member-list'),
                },
                {
                    path: `${prefixPath()}union-data-list`,
                    name: 'union-data-list',
                    meta: {
                        loginAndRefresh: true,
                        title:           '联邦资源',
                    },
                    component: () => import('../views/data-center/union-data-list'),
                },
                {
                    path: `${prefixPath()}union-data-view`,
                    name: 'union-data-view',
                    meta: {
                        loginAndRefresh: true,
                        hidden:          true,
                        title:           '联邦资源详情',
                        active:          `${prefixPath()}union-data-list`,
                    },
                    component: () => import('../views/data-center/union-data-view'),
                },
            ],
        },
        {
            path: `${prefixPath()}data-center`,
            name: 'data-center',
            meta: {
                title: '资源中心',
                icon:  'coin',
            },
            component: () => import('@comp/LayoutBase.vue'),
            children:  [
                {
                    path: `${prefixPath()}data-list`,
                    name: 'data-list',
                    meta: {
                        loginAndRefresh: true,
                        title:           '我的资源',
                    },
                    component: () => import('../views/data-center/data-list'),
                },
                {
                    path: `${prefixPath()}data-add-transition`,
                    name: 'data-add-transition',
                    meta: {
                        title: '添加资源',
                    },
                    component: () =>
                        import('../views/data-center/data-add-transition.vue'),
                },
                {
                    path: `${prefixPath()}data-add`,
                    name: 'data-add',
                    meta: {
                        hidden:       true,
                        notshowattag: true,
                        title:        '添加资源',
                        active:       `${prefixPath()}data-add-transition`,
                    },
                    component: () => import('../views/data-center/data-add.vue'),
                },
                {
                    path: `${prefixPath()}data-view`,
                    name: 'data-view',
                    meta: {
                        title:  '查看数据资源',
                        hidden: true,
                        active: `${prefixPath()}data-list`,
                    },
                    component: () => import('../views/data-center/data-view.vue'),
                },
                {
                    path: `${prefixPath()}data-update`,
                    name: 'data-update',
                    meta: {
                        hidden: true,
                        title:  '编辑数据资源',
                        active: `${prefixPath()}data-list`,
                    },
                    component: () => import('../views/data-center/data-update.vue'),
                },
                {
                    path: `${prefixPath()}data-check-label`,
                    name: 'data-check-label',
                    meta: {
                        hidden:          true,
                        title:           '查看与标注',
                        loginAndRefresh: true,
                        active:          `${prefixPath()}data-list`,
                    },
                    component: () =>
                        import('../views/data-center/data-check-label.vue'),
                },
                {
                    path: `${prefixPath()}data-label`,
                    name: 'data-label',
                    meta: {
                        hidden:          true,
                        title:           '数据标注',
                        loginAndRefresh: true,
                        active:          `${prefixPath()}data-list`,
                    },
                    component: () => import('../views/data-center/data-label.vue'),
                },
            ],
        },
        {
            path: `${prefixPath()}teamwork`,
            name: 'teamwork',
            meta: {
                title: '合作中心',
                icon:  'list',
            },
            component: () => import('@comp/LayoutBase.vue'),
            children:  [
                {
                    path: `${prefixPath()}teamwork`,
                    name: 'project-list',
                    meta: {
                        loginAndRefresh: true,
                        title:           '项目列表',
                    },
                    component: () => import('../views/teamwork/project'),
                },
                {
                    path: `${prefixPath()}teamwork/create`,
                    name: 'project-create',
                    meta: {
                        loginAndRefresh: true,
                        title:           '创建项目',
                    },
                    component: () => import('../views/teamwork/create'),
                },
                {
                    path: `${prefixPath()}teamwork/detail`,
                    name: 'project-detail',
                    meta: {
                        hidden:          true,
                        loginAndRefresh: true,
                        title:           '项目详情',
                        active:          `${prefixPath()}teamwork`,
                        navigation:      true,
                    },
                    component: () => import('../views/teamwork/detail'),
                },
                {
                    path: `${prefixPath()}teamwork/detail/flow`,
                    name: 'project-flow',
                    meta: {
                        hidden:          true,
                        loginAndRefresh: true,
                        title:           '流程详情',
                        active:          `${prefixPath()}teamwork`,
                        titleParams:     {
                            parentTitle: '项目详情',
                            title:       '项目详情',
                            htmlTitle:   '项目详情',
                            backward:    true,
                        },
                    },
                    component: () => import('../views/teamwork/visual/visual'),
                },
                {
                    path: `${prefixPath()}teamwork/detail/deep-learning/flow`,
                    name: 'project-deeplearning-flow',
                    meta: {
                        hidden:          true,
                        loginAndRefresh: true,
                        title:           '深度学习流程详情',
                        active:          `${prefixPath()}teamwork`,
                        titleParams:     {
                            parentTitle: '项目详情',
                            title:       '项目详情',
                            htmlTitle:   '项目详情',
                            backward:    true,
                        },
                    },
                    component: () => import('../views/teamwork/deeplearning/index'),
                },
                {
                    path: `${prefixPath()}teamwork/detail/deep-learning/check-flow`,
                    name: 'check-flow',
                    meta: {
                        hidden:          true,
                        loginAndRefresh: true,
                        title:           '模型校验',
                        active:          `${prefixPath()}teamwork`,
                        titleParams:     {
                            parentTitle: '项目详情',
                            title:       '项目详情',
                            htmlTitle:   '项目详情',
                            backward:    true,
                        },
                    },
                    component: () =>
                        import('../views/teamwork/deeplearning/check-flow'),
                },
                {
                    path: `${prefixPath()}teamwork/detail/job/history`,
                    name: 'project-job-history',
                    meta: {
                        hidden:          true,
                        loginAndRefresh: true,
                        title:           '流程执行记录',
                        active:          `${prefixPath()}teamwork`,
                        titleParams:     {
                            params:      ['project_id'],
                            name:        'project-detail',
                            parentTitle: '流程列表',
                            title:       '流程执行记录',
                        },
                    },
                    component: () => import('../views/teamwork/job/history'),
                },
                {
                    path: `${prefixPath()}teamwork/detail/job/detail`,
                    name: 'project-job-detail',
                    meta: {
                        hidden:          true,
                        loginAndRefresh: true,
                        title:           '任务执行结果',
                        active:          `${prefixPath()}teamwork`,
                        titleParams:     {
                            params:   ['flow_id'],
                            name:     'project-flow',
                            title:    '流程执行记录',
                            backward: true,
                        },
                        navigation: true,
                    },
                    component: () => import('../views/teamwork/job/detail'),
                },
                {
                    path: `${prefixPath()}teamwork/detail/job/compare`,
                    name: 'project-job-compare',
                    meta: {
                        hidden:          true,
                        loginAndRefresh: true,
                        title:           '任务对比',
                        active:          `${prefixPath()}teamwork`,
                        titleParams:     {
                            params: ['flow_id', 'project_id'],
                            name:   'project-job-history',
                            title:  '流程执行记录',
                        },
                        navigation: true,
                    },
                    component: () => import('../views/teamwork/job/compare'),
                },
                {
                    path: `${prefixPath()}teamwork/detail/fusion-edit`,
                    name: 'fusion-edit',
                    meta: {
                        hidden:          true,
                        loginAndRefresh: true,
                        title:           '新建数据融合任务',
                        active:          `${prefixPath()}teamwork`,
                        navigation:      false,
                    },
                    component: () =>
                        import(
                            '../views/teamwork/components/fusion-job/fusion-edit'
                        ),
                },
                {
                    path: `${prefixPath()}teamwork/detail/fusion-detail`,
                    name: 'fusion-detail',
                    meta: {
                        loginAndRefresh: true,
                        hidden:          true,
                        title:           '数据融合详情',
                        active:          `${prefixPath()}teamwork`,
                        navigation:      false,
                    },
                    component: () =>
                        import(
                            '../views/teamwork/components/fusion-job/fusion-edit'
                        ),
                },
            ],
        },
        {
            path: `${prefixPath()}modeling-list`,
            name: 'modeling-list',
            meta: {
                title:  '模型对比',
                hidden: true,
            },
            component: () => import('@comp/LayoutFullScreen.vue'),
            children:  [
                {
                    path: `${prefixPath()}modeling-list`,
                    name: 'modeling-list',
                    meta: {
                        loginAndRefresh: true,
                        title:           '模型对比',
                    },
                    component: () => import('../views/teamwork/modeling-list.vue'),
                },
            ],
        },
        /* {
            path: `${prefixPath()}account`,
            name: 'account',
            meta: {
                title: '用户管理',
                icon:  'user',
            },
            component: () => import('@comp/LayoutBase.vue'),
            children:  [
                {
                    path: `${prefixPath()}account-list`,
                    name: 'account-list',
                    meta: {
                        loginAndRefresh:  true,
                        title:            '用户列表',
                        normalUserCanSee: true,
                    },
                    component: () => import('../views/account/account-list'),
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
                    component: () =>
                        import('../views/system-config/account-setting'),
                },
            ],
        }, */
        {
            path: `${prefixPath()}global`,
            name: 'global',
            meta: {
                title: '全局设置',
                icon:  'setting',
                // globalTooltip:
                //     '* 只有管理员能对“全局设置”中的配置项进行变更 <br>* 只有超级管理员能对“成员设置”中的配置项进行变更',
            },
            component: () => import('@comp/LayoutBase.vue'),
            children:  [
                {
                    path: `${prefixPath()}member-view`,
                    name: 'member-view',
                    meta: {
                        loginAndRefresh: true,
                        title:           '成员设置',
                        globalTooltip:
                            '* 只有管理员能对“全局设置”中的配置项进行变更 <br>* 只有超级管理员能对“成员设置”中的配置项进行变更',
                    },
                    component: () => import('../views/system-config/member-view'),
                },
                {
                    path: `${prefixPath()}network-set`,
                    name: 'network-set',
                    meta: {
                        loginAndRefresh: true,
                        title:           '专用网络设置',
                    },
                    component: () => import('../views/system-config/network-set'),
                },
                {
                    path: `${prefixPath()}enterprise-certification`,
                    name: 'enterprise-certification',
                    meta: {
                        hidden:          true,
                        loginAndRefresh: true,
                        active:          `${prefixPath()}member-view`,
                        title:           '企业实名认证',
                    },
                    component: () =>
                        import('../views/system-config/enterprise-certification'),
                },
                {
                    path: `${prefixPath()}blacklist`,
                    name: 'blacklist',
                    meta: {
                        loginAndRefresh: true,
                        title:           '成员黑名单',
                    },
                    component: () => import('../views/blacklist/blacklist-list'),
                },
                {
                    path: `${prefixPath()}system-config-view`,
                    name: 'system-config-view',
                    meta: {
                        loginAndRefresh:  true,
                        title:            '系统设置',
                        normalUserCanSee: false,
                    },
                    component: () =>
                        import('../views/system-config/system-config-view'),
                },
                {
                    path: `${prefixPath()}calculation-engine-config`,
                    name: 'calculation-engine-config',
                    meta: {
                        loginAndRefresh:  true,
                        title:            '计算引擎设置',
                        normalUserCanSee: false,
                    },
                    component: () =>
                        import('../views/system-config/calculation-engine-config'),
                },
                {
                    path: `${prefixPath()}dev-tools`,
                    name: 'dev-tools',
                    meta: {
                        loginAndRefresh: true,
                        title:           '运维人员工具',
                        onlySuperAdmin:  true,
                    },
                    component: () => import('../views/dev/dev-tools'),
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
            path: `${prefixPath()}find-password`,
            name: 'find-password',
            meta: {
                title:          '找回密码',
                requiresAuth:   false,
                requiresLogout: true,
            },
            component: () => import('../views/sign/find-password.vue'),
        },
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
        {
            path: `${prefixPath()}init`,
            name: 'init',
            meta: {
                hidden: true,
            },
            component: () => import('../views/member/member-initialize'),
        },
        {
            path: `${prefixPath()}upload`,
            name: 'upload',
            meta: {
                hidden: true,
            },
            component: () => import('../views/data-center/data-uploader'),
        },
        /* {
            path:     `${prefixPath()}:catchAll(.*)`,
            redirect: {
                path: `${prefixPath()}notfound`,
            },
        }, */
    ];
}

export default baseRoutes;
