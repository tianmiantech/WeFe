<template>
    <div class="service-base-tag">
        <div
            ref="tags-nav"
            class="tags-list"
        >
            <el-tag
                v-for="(item, index) in tagsList"
                :key="index"
                :closable="$route.name !== item.name"
                :class="{'is-active': isActive(item.name)}"
                @close="closeTags(index)"
            >
                <template v-if="item.path">
                    <router-link
                        :to="{ name: item.name, query: item.query }"
                        class="tags-li-title"
                    >
                        {{ item.title }}
                    </router-link>
                </template>
            </el-tag>
        </div>
        <el-button
            size="mini"
            type="warning"
            class="tags-close-box"
            @click="closeOther"
        >
            关闭所有标签
        </el-button>
    </div>
</template>

<script>
    export default {
        computed: {
            tagsList() {
                return this.$store.state.base.tagsList || [];
            },
        },
        watch: {
            '$route.fullPath'(newValue, oldValue) {
                this.setTags(this.$route);
            },
        },
        mounted() {
            this.$nextTick(() => {
                this.setTags(this.$route);
            });
        },
        methods: {
            isActive(name) {
                return name === this.$route.name;
            },
            commitStore(list) {
                this.$store.commit('UPDATE_TAGSLIST', list);
            },
            // 关闭单个标签
            closeTags(index) {
                const tagsList = [...this.tagsList];
                const delItem = tagsList.splice(index, 1)[0];
                const item = tagsList[index] ? tagsList[index] : tagsList[index - 1];

                if (item) {
                    delItem.name === this.$route.name && this.$router.push({ name: item.name });
                }/*  else {
                    this.$router.push({ name: 'index' });
                } */
                this.commitStore(tagsList);
            },
            // 关闭全部标签
            closeAll() {
                this.commitStore([]);
            },
            // 关闭其他标签
            closeOther() {
                const tagsList = [...this.tagsList];
                const curItem = tagsList.filter(item => {
                    return item.name === this.$route.name;
                });

                this.commitStore(curItem);
            },
            // 设置标签
            setTags(route) {
                const tagsList = [...this.tagsList];
                const tagIndex = tagsList.findIndex(item => item.name === route.name);

                if (tagIndex < 0) {
                    tagsList.push({
                        title: route.meta.title,
                        path:  route.path,
                        name:  route.name,
                        query: route.query,
                    });
                } else {
                    tagsList[tagIndex].query = route.query;
                }
                this.commitStore(tagsList);

                this.$nextTick(() => {
                    // 将该位置显示出来
                    const tagsNav = this.$refs['tags-nav'];
                    const tag = tagsNav.children[tagsList.findIndex(item => item.name === route.name)];

                    tagsNav.scrollTo(0, 0);

                    if(tag.offsetLeft + tag.offsetWidth > tagsNav.offsetWidth) {
                        tagsNav.scrollTo(tag.offsetLeft - tagsNav.offsetWidth + tag.offsetWidth, 0);
                    }
                });
            },
            handleTags(command) {
                command === 'other' ? this.closeOther() : this.closeAll();
            },
        },
    };
</script>

<style lang="scss">
    .service-base-tag {
        display: flex;
        justify-content: space-between;
        height: 30px;
        background: #fff;
        padding: 3px 10px;
        box-shadow: 0 3px 3px rgba(146, 146, 146, 0.1);
        border-top: 1px solid #f5f6fa;
        .tags-close-box {
            height: 24px;
            line-height: 22px;
            padding:0 10px;
            position: static;
            min-height: auto;
            .el-button {
                height: 24px;
                line-height: 22px;
                padding:0 10px;
            }
        }
    }
    .service-base-tag .tags-list {
        overflow-x: auto;
        overflow-y: hidden;
        white-space: nowrap;
        width: calc(100% - 120px);
        &::-webkit-scrollbar{height: 0;}
        .el-tag {
            border: 0;
            height: 24px;
            line-height: 24px;
            margin-right: 10px;
            cursor: pointer;
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

</style>
