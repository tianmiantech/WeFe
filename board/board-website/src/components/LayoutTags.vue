<template>
    <div class="base-tags">
        <div
            ref="tagsNav"
            class="tags-list"
        >
            <el-tag
                v-for="(item, index) in tagsList"
                :key="index"
                :closable="route.name !== item.name"
                :class="{'is-active': item.name === route.name }"
                @close="closeTags(index)"
            >
                <router-link
                    v-if="item.path"
                    :to="{ name: item.name, query: item.query }"
                    class="tags-li-title"
                >
                    {{ item.title }}
                </router-link>
            </el-tag>
        </div>
        <el-button
            size="small"
            type="warning"
            class="tags-close-box"
            @click="closeOthers"
        >
            关闭其他标签
        </el-button>
    </div>
</template>

<script>
    import {
        ref,
        computed,
        onMounted,
        watch,
    } from 'vue';
    import { useRoute, useRouter } from 'vue-router';
    import { useStore } from 'vuex';

    export default {
        setup() {
            const tagsNav = ref();
            const store = useStore();
            const tagsList = computed(() => store.state.base.tagsList || []);
            const route = useRoute();
            const router = useRouter();
            // close one tag
            const closeTags = (index) => {
                const delItem = tagsList.value.splice(index, 1)[0];
                const item = tagsList[index] ? tagsList[index] : tagsList[index - 1];

                if (item) {
                    delItem.name === route.name && router.push({ name: item.name });
                }/*  else {
                    router.push({ name: 'index' });
                } */
                methods.commitStore(tagsList.value);
            };
            const closeAll = () => {
                methods.commitStore([]);
            };
            // close other tags
            const closeOthers = () => {
                const curItem = tagsList.value.filter(item => {
                    return item.name === route.name;
                });

                methods.commitStore(curItem);
            };
            const methods = {
                commitStore(list) {
                    store.commit('UPDATE_TAGSLIST', list);
                },
                // set tags
                setTags(route) {
                    if(route.meta.requiresLogout || route.meta.notshowattag) return; // not remember for login/register

                    const tagIndex = tagsList.value.findIndex(item => item.name === route.name);
                    const result = {
                        title: route.meta.title,
                        path:  route.path,
                        name:  route.name,
                        query: route.query,
                    };

                    if (~tagIndex) {
                        tagsList.value[tagIndex] = result;
                    } else {
                        tagsList.value.push(result);
                    }

                    methods.commitStore(tagsList.value);

                    // show the tag
                    if(tagsNav.value) {
                        const tag = tagsNav.value.children[tagsList.value.findIndex(item => item.name === route.name)];

                        tagsNav.value.scrollTo(0, 0);

                        if(tag && tag.offsetLeft + tag.offsetWidth > tagsNav.value.offsetWidth) {
                            tagsNav.value.scrollTo(tag.offsetLeft - tagsNav.value.offsetWidth + tag.offsetWidth, 0);
                        }
                    }
                },
            };

            onMounted(() => {
                methods.setTags(route);
            });

            watch(
                ()=> route.fullPath,
                () => {
                    methods.setTags(route);
                },
            );

            return {
                tagsList,
                route,
                closeTags,
                closeAll,
                closeOthers,
                tagsNav,
            };
        },
    };
</script>

<style lang="scss">
    .base-tags {
        height: 30px;
        border-top:1px solid #f5f6fa;
        padding: 3px 120px 3px 10px;
        box-shadow: 0 3px 3px rgba(146, 146, 146, 0.1);
        position: absolute;
        bottom:0;
        left: 0;
        right: 0;
    }
    .tags-list {
        overflow-x: auto;
        white-space: nowrap;
        &::-webkit-scrollbar{height: 0;}
        .el-tag {
            border: 0;
            height: 24px;
            line-height: 24px;
            margin-right: 10px;
        }
        .tags-li-title{
            text-decoration: none;
            color: $color-text-light;
        }
        .el-tag__close {
            color: $color-text-light;
        }
        .is-active {
            color: #fff;
            background: $color-link-base;
            &:hover {
                background: $color-link-base-hover;
            }
            .tags-li-title,
            .el-tag__close {
                color: #fff;
            }
        }
    }
    .tags-close-box {
        position: absolute;
        top: 2px;
        right: 10px;
        height:26px;
        line-height: 22px;
        min-height: 26px;
        padding:0 10px;
    }
</style>
