import menuTemp from './MenuTemp.vue';

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
        };
    },
    watch: {
        '$route.path'() {
            this.defaultActive = this.$route.meta.active || this.$route.path;
        },
    },
    created() {
        const currentIndex = `${this.$route.meta.index || 0}`;

        this.$router.options.routes.forEach(route => {
            if (route.meta && !route.meta.requiresLogout) {
                this.menuList.push(route);
            }
        });
        this.defaultActive = this.$route.meta.active || this.$route.path;
        this.defaultOpens = [currentIndex.substring(0, 1), currentIndex];
        this.getTaskAduitList();
    },
    methods: {
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
