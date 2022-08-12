<template>
    <div>
        <ul :class="['navigator-list', { show: showNavigation }]">
            <li
                v-for="(item, index) in list"
                :key="index"
            >
                <el-link
                    :type="item.highlight ? 'primary' : 'default'"
                    :underline="false"
                    @click="jumpto(item)"
                >
                    {{ item.title }}
                </el-link>
            </li>
        </ul>
        <div
            v-if="show"
            class="navigator"
            @click="toBack"
        >
            <i class="backToTop el-icon-arrow-up" />
        </div>
    </div>
</template>

<script>
let container, scrolling = false;

export default {
    name: 'TitleNavigator',
    data() {
        return {
            showNavigation:      false,
            show:                false,
            list:                [],
            updateTitleIdxTimer: null,
            updateOkTimer:       null,
        };
    },
    watch: {
        '$route': {
            handler (newValue) {
                // back page could no be a container
                if(!container) {
                    container = document.getElementById('layout-main');
                }
                if(container) {
                    setTimeout(() => {
                        const { scrollTop } = container;

                        scrolling = false;
                        this.show = scrollTop >= 300;
                        this.showNavigation = newValue.meta.navigation;

                        this.getTitles();
                        this.hightlightTitle();
                        this.list[0].highlight = true;
                    }, 300);
                }
            },
            deep: true,
        },
    },
    mounted() {
        this.init();
        this.$bus.$on('loginAndRefresh', () => {
            this.init();
        });
        this.$bus.$on('update-title-navigator', e => {
            this.init();
        });
    },
    methods: {
        getElementOffset(el) {
            let offsetTop = el.offsetTop,
                offsetLeft = el.offsetLeft,
                current = el.offsetParent;

            while (current){
                offsetTop += current.offsetTop;
                offsetLeft += current.offsetLeft;
                current = current.offsetParent;
            }

            return {
                top:  offsetTop,
                left: offsetLeft,
            };
        },
        toBack() {
            if(scrolling) return;
            scrolling = true;
            container.scrollTo({
                top:      0,
                behavior: 'smooth',
            });

            setTimeout(_ => {
                scrolling = false;
            }, 1000);
        },
        jumpto(item) {
            const dom = document.getElementsByName(item.title);

            this.list.forEach(row => {
                row.highlight = false;
            });

            if(dom) {
                const { top } = this.getElementOffset(dom[0]);

                item.highlight = true;
                container.scrollTo({
                    behavior: 'smooth',
                    top:      top - 125,
                });
            }
        },
        getTitles() {
            if(this.$route.meta.navigation) {
                const titles = container.querySelectorAll('.nav-title');

                this.list = [];

                if(titles.length) {
                    for (const item of titles) {
                        const title = item.getAttribute('name');

                        this.list.push({
                            title,
                            highlight: false,
                        });
                    }
                }
            }
            if(this.list.length) {
                this.showNavigation = this.$route.meta.navigation;
                this.hightlightTitle();
            }
        },
        hightlightTitle() {
            if(this.$route.meta.navigation) {
                const titles = container.querySelectorAll('.nav-title');

                this.list.forEach(item => item.highlight = false);
                for(let i = 0; i < titles.length; i++) {
                    const item = titles[i];
                    const { top, bottom } = item.getBoundingClientRect();

                    if(top <= 200 && bottom >= 20) {
                        this.list[i].highlight = true;
                    }
                }
            }
        },
        init() {
            if(this.$route.meta.navigation) {
                container = document.getElementById('layout-main');

                container.addEventListener('scroll', e => {
                    const { scrollTop } = container;

                    this.show = scrollTop >= 300;

                    this.hightlightTitle();
                });

                this.getTitles();

                if(this.list.length) {
                    this.list[0].highlight = true;
                }
            }
        },
    },
    
};
</script>

<style lang="scss" scoped>
    .navigator,
    .navigator-list{
        position: fixed;
        z-index: 20;
    }
    .navigator{
        right: 20px;
        bottom: 20px;
    }
    .navigator-list{
        right: -1px;
        top: 100px;
        padding:10px;
        transition-duration: 0.2s;
        transform: translateX(100%);
        border: 1px solid $border-color-base;
        border-radius: 4px;
        background:#fff;
        &.show{transform: translateX(-10px);}
    }
    .el-link{
        font-size: 12px;
        margin-top:5px;
        width: 100%;
        &:first-child{margin-top: 0;}
    }
    .backToTop{
        width: 42px;
        height: 42px;
        line-height: 40px;
        border-radius: 50%;
        border: 1px solid $border-color-base;
        text-align: center;
        background:#fff;
        cursor: pointer;
        &:hover{
            background: $background-color-hover;
        }
    }
</style>
