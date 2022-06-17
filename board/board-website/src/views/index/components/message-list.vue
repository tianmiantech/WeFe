<template>
    <el-card
        v-loading="message_list_loading"
        class="box-card"
    >
        <!-- <template #header>
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
        </template> -->
        <el-tabs v-model="activeName" class="msg-tabs" type="border-card" @tab-click="tabChange">
            <el-tab-pane label="待办事项" name="todoList">
                <el-collapse
                    v-infinite-scroll="loadMessageList"
                    infinite-scroll-delay="100"
                    class="message_list todoList"
                    accordion
                    @change="handleMessageListCollapseChange"
                >
                    <el-collapse-item
                        v-for="(item,index) in message_list"
                        :key="item.id"
                        :name="index"
                        :class="item.todo_complete ? '' : 'unread'"
                    >
                        <template #title>
                            <i :class="message_level_icon[item.level]" />
                            {{ item.title }}
                            <el-icon v-if="item.unread" class="el-icon-message unread-icon">
                                <elicon-message />
                            </el-icon>
                            <span class="time">{{ dateFormat(item.created_time) }}</span>
                        </template>
                        <div v-if="activeName === 'todoList'">
                            <!-- 数据集审核 -->
                            <div v-if="item.project.data_resource_id">
                                <p>数据集名称：{{item.project.data_resource_name}}</p>
                                <p>数据集ID：{{item.project.data_resource_id}}</p>
                            </div>
                            <!-- 项目审核 -->
                            <div v-else>
                                <p>项目名称：{{item.project.project_name}}</p>
                                <p>项目ID:{{item.project.project_id}}</p>
                            </div>
                            <router-link
                                :to="{name: 'project-detail', query: { project_id: item.project.project_id, project_type: item.project.project_type }}"
                                class="li"
                            >去处理</router-link>
                        </div>
                    </el-collapse-item>
                </el-collapse>
            </el-tab-pane>
            <el-tab-pane label="合作通知" name="cooperateNotice">
                <el-collapse
                    v-infinite-scroll="loadMessageList"
                    infinite-scroll-delay="100"
                    class="message_list todoList"
                    accordion
                    @change="handleMessageListCollapseChange"
                >
                    <el-collapse-item
                        v-for="(item,index) in message_list"
                        :key="item.id"
                        :name="index"
                        class="unread"
                    >
                        <template #title>
                            <i :class="message_level_icon[item.level]" />
                            {{ item.title }}
                            <el-icon v-if="item.unread" class="el-icon-message unread-icon">
                                <elicon-message />
                            </el-icon>
                            <span class="time">{{ dateFormat(item.created_time) }}</span>
                        </template>
                        <div v-if="activeName === 'cooperateNotice'">
                            <p>项目名称：{{item.project.project_name}}</p>
                            <p>项目ID:{{item.project.project_id}}</p>
                            <p>数据集名称：{{item.project.data_resource_name}}</p>
                            <p>数据集ID：{{item.project.data_resource_id}}</p>
                            <router-link
                                :to="{name: 'project-detail', query: { project_id: item.project.project_id, project_type: item.project.project_type }}"
                                class="li"
                            >去处理</router-link>
                        </div>
                    </el-collapse-item>
                </el-collapse>
            </el-tab-pane>
            <el-tab-pane label="系统消息" name="systemMsg">
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
            </el-tab-pane>
        </el-tabs>
        
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
                    unread:     null,
                    page_index: 0,
                    page_size:  15,
                    noMore:     false,
                    todo:       true,
                },

                message_list: [],
                activeName:   'todoList',
            };
        },
        created() {
            this.loadMessageList();
        },
        methods: {
            tabChange(val) {
                this.message_list = [];
                switch(val.paneName) {
                case 'todoList':
                    this.message_search.todo = true;
                    this.message_search.todoComplete = false;
                    if (this.message_search.eventList) delete this.message_search.eventList;
                    break;
                case 'cooperateNotice':
                    this.message_search.eventList = ['CreateProject', 'AgreeJoinProject', 'DisagreeJoinProject', 'ApplyDataResource', 'AgreeApplyDataResource', 'DisagreeApplyDataResource'];
                    if (this.message_search.todo !== '') delete this.message_search.todo;
                    break;
                case 'systemMsg':
                    this.message_search.eventList = ['OnGatewayError', 'OnEmailSendFail'];
                    if (this.message_search.todo !== '') delete this.message_search.todo;
                    break;
                }
                this.message_search.page_index = 0;
                this.noMore = false;
                this.loadMessageList();
            },
            async loadMessageList() {
                if(this.noMore) return;
                this.message_list_loading = true;
                const { code, data } = await this.$http.post({
                    url:  '/message/query',
                    data: this.message_search,
                });

                if(code === 0) {
                    this.noMore = data.list.length < 15;
                    if (this.activeName === 'todoList' || this.activeName === 'cooperateNotice') {
                        const eventlist = ['CreateProject', 'AgreeJoinProject', 'DisagreeJoinProject', 'ApplyDataResource', 'AgreeApplyDataResource', 'DisagreeApplyDataResource'];
                        const list = data.list.map((item, i) => {
                            if (eventlist.indexOf(item.event) !== -1) {
                                const content = JSON.parse(item.content);

                                // console.log(content, content.project_type);

                                return {
                                    ...item,
                                    project: {
                                        project_id:         content.project_id,
                                        project_name:       content.project_name,
                                        data_resource_id:   content.data_resource_id,
                                        data_resource_name: content.data_resource_name,
                                        project_type:       content.project_type,
                                    },
                                };
                            }
                        });

                        for(const i in list){
                            this.message_list.push(list[i]);
                        }
                    } else if (this.activeName === 'systemMsg') {
                        for(const i in data.list){ 
                            this.message_list.push(data.list[i]); 
                        } 
                    }
                    this.message_search.page_index++;
                }
                // console.log(this.message_list);
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
        height: 571px;
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
            max-height: 484px;
            border: unset;
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
        .msg-tabs {
            margin-top: -5px;
            overflow: auto;
            .el-tabs__content {
                padding: 5px;
            }
            .todoList {
                
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
