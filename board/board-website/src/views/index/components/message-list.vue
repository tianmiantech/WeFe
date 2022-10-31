<template>
    <el-card
        v-loading="message_list_loading"
        class="box-card"
    >
        <el-tabs v-model="activeName" class="msg-tabs" type="border-card" @tab-click="tabChange">
            <el-tab-pane label="待办事项" name="todoList">
                <div class="search_box">
                    <span>状态：</span>
                    <el-radio-group v-model="message_search.todoComplete" size="small" @change="todoListChange">
                        <el-radio-button v-for="item in todoListSelect" :key="item.value" :label="item.value">{{item.label}}</el-radio-button>
                    </el-radio-group>
                </div>
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
                            <p :class="{'collapse-title': windowWidth<=1440, 'mr5': true}"><span :class="[item.todo_complete ? 'success' : 'warning', 'mr5 ml5']">{{item.todo_complete ? '[已处理]' : '[待处理]'}}</span>{{ item.title }}</p>
                            <router-link
                                v-if="activeName === 'todoList' && item.todo"
                                :to="{name: 'project-detail', query: { project_id: item.project.project_id, project_type: item.project.project_type }}"
                                class="li"
                            >{{item.todo_complete ? '查看详情' : '去处理'}}</router-link>
                            <span class="time">{{ dateFormat(item.created_time) }}</span>
                        </template>
                        <div v-if="activeName === 'todoList' && item.todo" class="list_detail">
                            <div>
                                <p>{{item.project.title}}</p>
                                <p>申请成员：{{item.project.from_member_name}}</p>
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
                            </div>
                            <router-link
                                :to="{name: 'project-detail', query: { project_id: item.project.project_id, project_type: item.project.project_type }}"
                                class="link_item"
                            >{{item.todo_complete ? '查看详情' : '去处理'}}</router-link>
                        </div>
                        <div v-else>{{item.content}}</div>
                    </el-collapse-item>
                </el-collapse>
                <div
                    v-if="!message_list || message_list.length === 0"
                    class="empty-message-list"
                >
                    <img
                        class="empty-data-img"
                        src="@assets/images/bangbangda.png"
                    >
                    <p class="p1">棒棒哒~</p>
                    <p class="p2">您已处理完了所有待办事项</p>
                </div>
            </el-tab-pane>
            <el-tab-pane label="合作通知" name="cooperateNotice">
                <div class="search_box">
                    <span>状态：</span>
                    <el-radio-group v-model="message_search.unread" size="small" @change="systemMsgChange">
                        <el-radio-button v-for="item in systemMsgSelect" :key="item.value" :label="item.value">{{item.label}}</el-radio-button>
                    </el-radio-group>
                </div>
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
                            <!-- <span :class="[item.unread ? 'warning' : 'success', 'mr5 ml5']">{{item.unread ? '[未读]' : '[已读]'}}</span> -->
                            <span v-if="item.unread" class="unread_tips"></span>
                            <p :class="{'collapse-title': windowWidth<=1440, 'mr5': true}">{{ item.title }}</p>
                            <el-icon v-if="item.unread" class="el-icon-message unread-icon">
                                <elicon-message />
                            </el-icon>
                            <span class="time">{{ dateFormat(item.created_time) }}</span>
                        </template>
                        <div v-if="activeName === 'cooperateNotice'" class="list_detail">
                            <div>
                                <p>{{item.project.title}}</p>
                                <p>申请成员：{{item.project.from_member_name}}</p>
                                <p>项目名称：{{item.project.project_name}}</p>
                                <p>项目ID:{{item.project.project_id}}</p>
                                <div v-if="item.project.data_resource_id">
                                    <p>数据集名称：{{item.project.data_resource_name}}</p>
                                    <p>数据集ID：{{item.project.data_resource_id}}</p>
                                </div>
                            </div>
                            <router-link
                                :to="{name: 'project-detail', query: { project_id: item.project.project_id, project_type: item.project.project_type }}"
                                class="link_item"
                            >查看详情</router-link>
                        </div>
                        <div v-else>{{item.content}}</div>
                    </el-collapse-item>
                </el-collapse>
                <div
                    v-if="!message_list || message_list.length === 0"
                    class="empty-message-list"
                >
                    <img
                        class="empty-data-img"
                        src="@assets/images/bangbangda.png"
                    >
                    <p class="p1">棒棒哒~</p>
                    <p class="p2">您已看完了所有合作通知</p>
                    <!-- <p class="p3">
                        <el-button
                            type="text"
                            @click="messageSearchChangeUnread(false)"
                        >
                            查看已读
                        </el-button>
                    </p> -->
                </div>
            </el-tab-pane>
            <el-tab-pane label="系统消息" name="systemMsg">
                <div class="search_box">
                    <span>状态：</span>
                    <el-radio-group v-model="message_search.unread" size="small" @change="systemMsgChange">
                        <el-radio-button v-for="item in systemMsgSelect" :key="item.value" :label="item.value">{{item.label}}</el-radio-button>
                    </el-radio-group>
                </div>
                <el-collapse
                    v-infinite-scroll="loadMessageList"
                    infinite-scroll-delay="100"
                    class="message_list todoList systemList"
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
                            <span v-if="item.unread" class="unread_tips"></span>
                            {{ item.title }}
                            <el-icon v-if="item.unread" class="el-icon-message unread-icon">
                                <elicon-message />
                            </el-icon>
                            <span class="time">{{ dateFormat(item.created_time) }}</span>
                        </template>
                        {{ item.content }}
                    </el-collapse-item>
                </el-collapse>
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
                            查看全部
                        </el-button>
                    </p>
                </div>
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
                    unread:       null,
                    page_index:   0,
                    page_size:    15,
                    noMore:       false,
                    todo:         true,
                    todoComplete: '',
                    eventList:    ['ApplyJoinProject', 'ApplyDataResource'],
                },

                message_list:   [],
                activeName:     'todoList',
                todoListSelect: [
                    {
                        label: '全部',
                        value: '',
                    },
                    {
                        label: '待处理',
                        value: false,
                    },
                    {
                        label: '已处理',
                        value: true,
                    },
                ],
                systemMsgSelect: [
                    {
                        label: '全部',
                        value: null,
                    },
                    {
                        label: '已读',
                        value: false,
                    },
                    {
                        label: '未读',
                        value: true,
                    },
                ],
                windowWidth: document.documentElement.clientWidth,
            };
        },
        created() {
            this.loadMessageList();
            window.onresize = () => {
                this.windowWidth = document.documentElement.clientWidth;
            };
        },
        methods: {
            tabChange(val) {
                this.message_list = [];
                switch(val.paneName) {
                case 'todoList':
                    this.message_search.todo = true;
                    this.message_search.eventList = ['ApplyJoinProject', 'ApplyDataResource', 'AgreeJoinProject'];
                    break;
                case 'cooperateNotice':
                    this.message_search.eventList = ['AgreeJoinProject', 'DisagreeJoinProject', 'AgreeApplyDataResource', 'DisagreeApplyDataResource'];
                    this.message_search.todo = false;
                    break;
                case 'systemMsg':
                    this.message_search.eventList = ['OnGatewayError', 'OnEmailSendFail'];
                    if (this.message_search.todo !== '') delete this.message_search.todo;
                    break;
                }
                this.message_search.page_index = 0;
                this.message_search.todoComplete = '';
                this.noMore = false;
                this.message_search.unread = null;
                this.loadMessageList();
            },
            todoListChange(val) {
                this.message_search.page_index = 0;
                this.noMore = false;
                this.message_list = [];
                this.loadMessageList();
            },
            systemMsgChange() {
                this.message_search.page_index = 0;
                this.noMore = false;
                this.message_list = [];
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
                        let isEventList = true;
                        const eventlist = ['CreateProject', 'AgreeJoinProject', 'ApplyJoinProject', 'DisagreeJoinProject', 'ApplyDataResource', 'AgreeApplyDataResource', 'DisagreeApplyDataResource'];
                        const list = data.list.map((item, i) => {
                            if (eventlist.indexOf(item.event) !== -1) {
                                const content = JSON.parse(item.content);

                                return {
                                    ...item,
                                    project: {
                                        project_id:         content.project_id,
                                        project_name:       content.project_name,
                                        data_resource_id:   content.data_resource_id,
                                        data_resource_name: content.data_resource_name,
                                        project_type:       content.project_type,
                                        from_member_name:   content.from_member_name,
                                        title:              content.title,
                                    },
                                };
                            } else {
                                isEventList = false;
                            }
                        });

                        if (isEventList) {
                            for(const i in list){
                                this.message_list.push(list[i]);
                            }
                        } else {
                            for(const i in data.list){ 
                                this.message_list.push(data.list[i]); 
                            } 
                        }
                    } else {
                        for(const i in data.list){ 
                            this.message_list.push(data.list[i]); 
                        } 
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
                this.noMore = false;
                this.message_search.page_index = 0;
                this.noMore = false;
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
            height: 434px;
            .p1{
                font-size: 24px;
                font-weight: bold;
                line-height: 150%;
                margin-top: 10px;
            }
            .p2 {
                margin-top: 5px;
                margin-bottom: 5px;
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
            .list_detail {
                display: flex;
                justify-content: space-between;
                align-items: flex-end;
            }
            .collapse-title {
                max-width: 320px;
                white-space: nowrap;
                overflow: hidden;
                text-overflow: ellipsis;
            }
            .unread_tips {
                &::before {
                    content: '';
                    border: 3px solid #f00;
                    border-radius: 50%;
                    display: inline-block;
                    margin-right: 6px;
                }
            }
            .link_item {
                min-width: 82px;
            }
        }
        .todoList {
            max-height: 434px;
        }
        .systemList {
            padding-left: 5px;
        }
        .msg-tabs {
            margin-top: -5px;
            overflow: auto;
            .el-tabs__content {
                padding: 5px;
            }
            .search_box {
                height: 50px;
                display: flex;
                align-items: center;
                padding: 0 10px;
                // margin-bottom: 5px;
                // box-shadow: 0 5px 5px rgba(0, 0, 0, 0.2);
                // box-shadow: 3px 3px red, -1em 0 0.4em olive;
                border-bottom: 1px solid #ebeef5;
                span {
                    font-size: 14px;
                }
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
