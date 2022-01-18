<template>
    <el-card
        v-loading="message_list_loading"
        class="box-card"
    >
        <template #header>
            <div style="line-height: 32px;">
                Message
                <el-button
                    style="float: right; padding: 3px 0"
                    type="text"
                >
                    <el-switch
                        v-model="message_search.unread"
                        active-text="仅看未读"
                        inactive-text="查看全部"
                        @change="messageSearchChangeUnread"
                    />
                </el-button>
            </div>
        </template>
        <div
            v-if="!message_list || message_list.length === 0"
            class="empty-message-list"
        >
            <img
                class="empty-data-img"
                src="@assets/images/bangbangda.png"
            >
            <p class="p1">棒棒哒~</p>
            <p class="p2">您已看完了所有系统消息</p>
            <p class="p3">
                <el-button
                    type="text"
                    @click="messageSearchChangeUnread(false)"
                >
                    查看已读
                </el-button>
            </p>
        </div>
        <el-collapse
            v-else
            v-infinite-scroll="loadMessageList"
            infinite-scroll-delay="100"
            class="message_list"
            accordion
            @change="handleMessageListCollapseChange"
        >
            <el-collapse-item
                v-for="(item,index) in message_list"
                :key="item.id"
                :name="index"
                :class="item.unread ? 'unread' : ''"
            >
                <template #title>
                    <i :class="message_level_icon[item.level]" />
                    {{ item.title }}
                    <el-icon v-if="item.unread" class="el-icon-message unread-icon">
                        <elicon-message />
                    </el-icon>
                    <span class="time">{{ dateFormat(item.created_time) }}</span>
                </template>
                {{ item.content }}
            </el-collapse-item>
        </el-collapse>
    </el-card>
</template>

<script>
    import table from '@src/mixins/table.js';

    export default {
        mixins: [table],
        data() {
            return {
                message_list_loading: false,

                message_tag_type: {
                    info:    'info',
                    success: 'success',
                    warning: 'warning',
                    error:   'danger',
                },
                message_level_icon: {
                    info:    'el-icon-info info level',
                    success: 'el-icon-success success level',
                    warning: 'el-icon-warning warning level',
                    error:   'el-icon-error error level',
                },
                message_search: {
                    unread:     true,
                    page_index: 0,
                    page_size:  15,
                },

                message_list: [],
            };
        },
        async created() {
            this.loadMessageList();
        },
        methods: {
            async loadMessageList() {
                this.message_list_loading = true;
                const { code, data } = await this.$http.post({
                    url:  '/message/query',
                    data: this.message_search,
                });

                if(code === 0) {
                    for(const i in data.list){
                        this.message_list.push(data.list[i]);
                    }
                    this.message_search.page_index++;
                }
                this.message_list_loading = false;
            },
            async handleMessageListCollapseChange(index){
                const message = this.message_list[index];

                // mark to readed
                await this.$http.post({
                    url:  '/message/read',
                    data: { id: message.id },
                });
                message.unread = false;
            },
            // switch state refresh message list
            async messageSearchChangeUnread(value){
                // reset state & search
                this.message_list = [];
                this.message_search.page_index = 0;
                if(!value){
                    this.message_search.unread = null;
                }
                this.loadMessageList();
            },
        },
    };
</script>

<style lang="scss">
    .box-card{
        height: 455px;

        .el-card__body {
            height: 100%;
            margin-top: -21px;
        }
        .empty-message-list{
            text-align: center;
            padding-top:15px;
            .p1{
                font-size: 24px;
                font-weight: bold;
                line-height: 150%;
            }
        }
        .message_list {
            overflow-y: auto;
            width: 100%;
            height: 100%;
            .el-collapse-item__header{
                color: #aaa;
            }
            .unread .el-collapse-item__header{
                color: #1B233B;
                font-weight: bold;
                .unread-icon{
                    color: red;
                    margin-left: 5px;
                }
            }
            .el-collapse-item__header{
                position:relative;
                white-space: nowrap;
                overflow: hidden;
                .level{
                    font-size: 18px;
                    padding-left: 5px;
                    padding-right: 5px;
                }
                .time{
                    font-weight: 100;
                    position: absolute;
                    right: 40px;
                    color: #999;
                }
            }
            .el-tag{
                margin-left: 8px;
                margin-right: 8px;
            }
            .el-collapse-item__content{
                padding:8px;
                text-indent: 30px;
            }
        }
    }
    .success{
        color: #35c895;
    }
    .error{
        color: #f85564;
    }
    .warning{
        color: #f1b92a;
    }
    .info{
        color: #28c2d7;
    }
</style>
