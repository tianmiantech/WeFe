<template>
    <ul v-if="vData.isTitleOk" :class="['navigator-list', { show: vData.showNavigation, 'top-qiankun': vData.isInQianKun }]">
        <li
            v-for="(item, index) in vData.list"
            :key="index"
        >
            <el-link
                :type="item?.highlight ? 'primary' : 'default'"
                :underline="false"
                @click="jumpto(item)"
            >{{ item?.title }}</el-link>
        </li>
    </ul>
    <div v-if="vData.show" class="navigator">
        <el-icon
            class="backToTop board-icon-arrow-up"
            @click="toBack"
        >
            <elicon-arrow-up />
        </el-icon>
    </div>
</template>

<script>
    import {
        watch,
        reactive,
        onMounted,
        getCurrentInstance,
        onBeforeUnmount,
    } from 'vue';
    import { useRoute } from 'vue-router';

    export default {
        name: 'TitleNavigator',
        setup() {
            let container,
                scrolling = false;
            const { appContext } = getCurrentInstance();
            const { $bus } = appContext.config.globalProperties;
            const route = useRoute();
            const vData = reactive({
                showNavigation:      false,
                show:                false,
                list:                [],
                isTitleOk:           false,
                updateTitleIdxTimer: null,
                updateOkTimer:       null,
                isInQianKun:         window.__POWERED_BY_QIANKUN__ || false,
            });
            const getElementOffset = el => {
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
            };
            const toBack = () => {
                if(scrolling) return;
                scrolling = true;

                container.scrollTo({
                    top:      0,
                    behavior: 'smooth',
                });

                setTimeout(_ => {
                    scrolling = false;
                }, 1000);
            };
            const jumpto = item => {
                const dom = document.getElementsByName(item.title);

                vData.list.forEach(row => {
                    row.highlight = false;
                });

                if(dom) {
                    const { top } = getElementOffset(dom[0]);

                    item.highlight = true;
                    container.scrollTo({
                        behavior: 'smooth',
                        top:      top - 105,
                    });
                }
            };
            const getTitles = () => {
                if(route.meta.navigation) {
                    const titles = container.querySelectorAll('.nav-title');

                    vData.list = [];

                    if(titles.length) {
                        for (const item of titles) {
                            const title = item.getAttribute('name');
                            const show = item.getAttribute('show');
                            const idx = item.getAttribute('idx') || '';

                            if (idx.length) {
                                vData.updateTitleIdxTimer = setTimeout(()=> {
                                    let index = item.getAttribute('idx');

                                    index = Number(index) + 1;
                                    if (show !== 'false') {
                                        vData.list[index] = { title, highlight: false };
                                    }
                                }, 200);
                            } else {
                                if (show !== 'false') { 
                                    vData.list.push({ 
                                        title, 
                                        highlight: false, 
                                    }); 
                                } 
                            }

                            
                        }
                    }
                }
                vData.updateOkTimer = setTimeout(() => {
                    if(vData.list.length) {
                        vData.showNavigation = route.meta.navigation;
                        hightlightTitle();
                    }
                }, 250);
            };
            const hightlightTitle = () => {
                if(route.meta.navigation) {
                    const titles = container.querySelectorAll('.nav-title');

                    vData.list.forEach(item => item.highlight = true);
                    for(let i = 0; i < titles.length; i++) {
                        const item = titles[i];
                        const { top, bottom } = item.getBoundingClientRect();

                        if(top <= 120 && bottom >= 90 && vData.list[i]) {
                            vData.list[i].highlight = true;
                            
                        }
                    }
                    vData.isTitleOk = true;
                    
                }
            };
            const init = () => {
                if(route.meta.navigation) {
                    container = document.getElementById('layout-main');

                    container.addEventListener('scroll', e => {
                        const { scrollTop } = container;

                        vData.show = scrollTop >= 300;

                        hightlightTitle();
                    });

                    getTitles();

                    if(vData.list.length) {
                        vData.list[0].highlight = true;
                    }
                    
                

                }
            };

            onMounted(() => {
                init();
                if($bus){
                    $bus.$on('loginAndRefresh', () => {
                        init();
                    });

                    $bus.$on('update-title-navigator', e => {
                        init();
                    });
                }
            });

            watch(
                () => route.meta,
                (newValue) => {
                    // back page could no be a container
                    if(!container) {
                        container = document.getElementById('layout-main');
                    }

                    if(container) {
                        setTimeout(() => {
                            const { scrollTop } = container;

                            scrolling = false;
                            vData.show = scrollTop >= 300;
                            vData.showNavigation = newValue.navigation;

                            getTitles();
                            hightlightTitle();
                        }, 300);
                    }
                },
            );

            onBeforeUnmount(()=> {
                if (vData.updateTitleIdxTimer) clearTimeout(vData.updateTitleIdxTimer);
                if (vData.updateOkTimer) clearTimeout(vData.updateOkTimer);
            });

            return {
                vData,
                jumpto,
                toBack,
            };
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
    .top-qiankun { top: 140px; }
    .board-link{
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
