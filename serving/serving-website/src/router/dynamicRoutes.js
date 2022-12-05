/*!
 * @author claude
 * date 07/05/2019
 * 动态路由模块
 */

const prefixPath = '/';
const dynamicRoutes = [
    {
        path: prefixPath,
        meta: {
            permission: true,
            asmenu:     true,
            role:       '*',
        },
        children: [
            {
                path: '',
                name: 'index',
                meta: {
                    title: '消息面板',
                    icon:  'el-icon-monitor',
                },
                component: () => import('@views/index/dashboard.vue'),
            },
        ],
    },
];

export default dynamicRoutes;
