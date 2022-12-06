import menuTemp from './MenuTemp.vue';
import { getPermission } from '@src/service/permission';
import baseRoutes from '../../router/routes';
import { isQianKun } from '@src/http/utils';
import { mapGetters } from 'vuex';

const prefixPath = process.env.NODE_ENV === 'development' ? '/' : `/${process.env.CONTEXT_ENV}/`;

export default {
    props: {
        isCollapsed: Boolean,
    },
    inject:     ['refresh'],
    components: {
        menuTemp,
    },
    data() {
        return {
            defaultActive: '',
            defaultOpens:  [],
            menuList:      [],
            isInQianKun:   window.__POWERED_BY_QIANKUN__ || false,
        };
    },
    computed: {
        ...mapGetters(['appInfo']),
    },
    watch: {
        '$route.path'() {
            this.defaultActive = this.$route.meta.active || this.$route.path;
        },
    },
    created() {

        this.getTaskAduitList();

        const currentIndex = `${this.$route.meta.index || 0}`;

        this.defaultActive = this.$route.meta.active || this.$route.path;
        this.defaultOpens = [currentIndex.substring(0, 1), currentIndex];
        if(isQianKun()){
            this.getMenuList();
        } else {
            this.menuList = baseRoutes();
        }
    },
    methods: {
        getMenuList(){
            getPermission().then(menu=>{
                // console.log('menu', menu);
                const { list = [] } = menu || {};

                const authority = list.reduce((pre,cur) => {
                    if(cur.resourceType === 'menu')
                        return [...pre, cur.resourceUri];
                    return pre;
                }, []);

                this.menuList = this.dealRoute(baseRoutes(), authority);
                this.$store.commit('MENU_LIST', authority);

            });
        },
        dealRoute (array, authority){
            const one = [];

            array.forEach(item => {

                if(authority.includes(item.name)){
                    one.push({ ...item });
                    one[one.length-1].children = undefined;
                    if(item.children && item.children.length > 0){
                        one[one.length-1].children = this.dealRoute(item.children,authority);
                    }
                }
            });
            return [...one];
        },
        menuSelected (index) {
            if (index === this.$route.path) {
                // 刷新当前路由
                this.$nextTick(() => {
                    // this.refresh();
                });
            }
        },
        // 获取任务审核列表，控制菜单红点显隐
        async getTaskAduitList() {
            const { code, data } = await this.$http.post('/task/paging', {
                params: {
                    status:     'Pending',
                    page_index: 0,
                    page_size:  10,
                },
            });

            if (code === 0) {
                if (data.total) {
                    const route = this.$router.options.routes.find(route => route.path === `${prefixPath}task`);

                    if (route) {
                        const menu = route.children.find(child => child.name === 'task-list');

                        if (menu) {
                            menu.meta.tips = data.total;
                        }
                    }
                }
            }
        },
    },
};
