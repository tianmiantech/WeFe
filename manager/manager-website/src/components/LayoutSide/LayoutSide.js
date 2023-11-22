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

const { prefixPath } = window.api;
const asideCollapsedKey = `${prefixPath}AsideCollapsed`;

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
        const { appContext } = getCurrentInstance();
        const { $bus } = appContext.config.globalProperties;
        const vData = reactive({
            defaultActive: '',
            defaultOpens:  [],
            menuList:      [],
            isCollapsed:   false,
        });
        const changeCollapsed = () => {
            vData.isCollapsed = !vData.isCollapsed;
            window.localStorage.setItem(asideCollapsedKey, vData.isCollapsed);
            setTimeout(() => {
                $bus.$emit('sideCollapsed', vData.isCollapsed);
            }, 330);
        };

        onBeforeMount(() => {
            const currentIndex = `${route.meta.index || 0}`;

            // left menus last collapse state
            const isCollapsed = window.localStorage.getItem(asideCollapsedKey);

            vData.isCollapsed = isCollapsed === 'false' ? false : Boolean(isCollapsed);

            router.options.routes.forEach(route => {
                if (route.meta && !route.meta.requiresLogout) {
                    vData.menuList.push(route);
                }
            });
            vData.defaultActive = route.meta.active || route.path;
            vData.defaultOpens = [currentIndex.substring(0, 1), currentIndex];
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
        };
    },
};
