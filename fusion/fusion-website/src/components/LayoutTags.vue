<template>
    <div class="base-tags">
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
            关闭其他标签
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

            closeAll() {
                this.commitStore([]);
            },

            closeOther() {
                const tagsList = [...this.tagsList];
                const curItem = tagsList.filter(item => {
                    return item.name === this.$route.name;
                });

                this.commitStore(curItem);
            },

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
    .base-tags {
        // height: 30px;
        border-top:1px solid #f5f6fa;
        background: #fff;
        padding: 3px 120px 3px 10px;
        box-shadow: 0 3px 3px rgba(146, 146, 146, 0.1);
        position: absolute;
        bottom: 0;
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
        top: 3px;
        right: 10px;
        height: 24px;
        line-height: 22px;
        padding:0 10px;
        .el-button {
            height: 24px;
            line-height: 22px;
            padding:0 10px;
        }
    }
</style>
