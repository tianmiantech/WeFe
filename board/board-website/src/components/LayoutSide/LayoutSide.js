import {
    computed,
    onBeforeMount,
    getCurrentInstance,
    reactive,
    watch,
} from 'vue';
import {
    useRoute,
    useRouter,
} from 'vue-router';
import { useStore } from 'vuex';
import menuTemp from './MenuTemp.vue';
import { getPermission } from '@src/service/permission';
import baseRoutes from '../../router/routes';
import { isQianKun } from '@src/http/utils';
import { appCode } from '@src/utils/constant';

const asideCollapsedKey = `${appCode()}AsideCollapsed`;

const dealRoute = (array, authority) => {
    const one = [];

    array.forEach(item => {

        if(authority.includes(item.name)){
            one.push({ ...item });
            one[one.length-1].children = undefined;
            if(item.children && item.children.length > 0){
                one[one.length-1].children = dealRoute(item.children,authority);
            }
        }
    });
    return [...one];
};

export default {
    inject:     ['refresh'],
    components: {
        menuTemp,
    },
    setup () {
        const store = useStore();
        const route = useRoute();
        const router = useRouter();
        const userInfo = computed(() => store.state.base.userInfo);
        const appInfo = computed(() => store.state.base.appInfo);
        const { appContext } = getCurrentInstance();
        const { $bus } = appContext.config.globalProperties;
        const vData = reactive({
            defaultActive: '',
            defaultOpens:  [],
            menuList:      [],
            isCollapsed:   false,
            isInQianKun:   window.__POWERED_BY_QIANKUN__ || false,
        });
        const changeCollapsed = () => {
            vData.isCollapsed = !vData.isCollapsed;
            window.localStorage.setItem(asideCollapsedKey, vData.isCollapsed);
            setTimeout(() => {
                $bus.$emit('sideCollapsed', vData.isCollapsed);
            }, 330);
        };
        const getMenuList = () => {
            getPermission().then(menu=>{
                // console.log('menu', menu);
                const { list } = menu || {};

                const authority = (list || []).reduce((pre,cur) => {
                    if(cur.resourceType === 'menu')
                        return [...pre, cur.resourceUri];
                    return pre;
                }, []);

                vData.menuList = dealRoute(baseRoutes(), authority);
                console.log('vData.menuList',dealRoute(baseRoutes(), authority))
                const currentIndex = `${route.meta.index || 0}`;

                // console.log('vData.menuList',vData.menuList,authority)

                store.commit('MENU_LIST', authority);

                // this.$router.options.routes.forEach(route => {
                //     if (route.meta && !route.meta.requiresLogout) {
                //         this.menuList.push(route);
                //     }
                // });
                // console.log(this.menuList,'menulist====');
                vData.defaultActive = route.meta.active || route.path;
                vData.defaultOpens = [currentIndex.substring(0, 1), currentIndex];
            });
        };

        onBeforeMount(() => {
            // const currentIndex = `${route.meta.index || 0}`;

            // left menus last collapse state
            const isCollapsed = window.localStorage.getItem(asideCollapsedKey);

            vData.isCollapsed = isCollapsed === 'false' ? false : Boolean(isCollapsed);

            if (!isQianKun()) {
                const currentIndex = `${route.meta.index || 0}`;

                router.options.routes.forEach(route => {
                    if (route.meta && !route.meta.requiresLogout) {
                        vData.menuList.push(route);
                    }
                });
                vData.defaultActive = route.meta.active || route.path;
                vData.defaultOpens = [currentIndex.substring(0, 1), currentIndex];
            } else {
                getMenuList();
            }
            // router.options.routes.forEach(route => {
            //     if (route.meta && !route.meta.requiresLogout) {
            //         vData.menuList.push(route);
            //     }
            // });
            // vData.defaultActive = route.meta.active || route.path;
            // vData.defaultOpens = [currentIndex.substring(0, 1), currentIndex];
        });

        watch(
            () => route.path,
            () => {
                vData.defaultActive = route.meta.active || route.path;
            },
        );

        return {
            vData,
            userInfo,
            changeCollapsed,
            getMenuList,
            dealRoute,
            appInfo,
        };
    },
};
