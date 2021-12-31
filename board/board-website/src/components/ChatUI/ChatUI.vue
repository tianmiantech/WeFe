<template>
    <VueDragResize
        ref="sysChat"
        v-loading="vData.loading"
        :min-height="`${vData.maxHeight + 160}`"
        :control-points="vData.controlPoints"
        :show-hide-btn="true"
        class="sys-chat-panel"
        min-width="560"
        @drag-start="dragstart"
        @drag-end="dragend"
        @window-hide="hide"
        @window-min="min"
    >
        <div class="sys-chat">
            <div class="chat-tab">
                <el-tabs
                    v-model="vData.tabName"
                    @tab-click="tabChange"
                >
                    <el-tab-pane
                        v-if="vData.chatList.length"
                        name="recent"
                        label="最近"
                    >
                        <ul class="user-list pt5 pb5 recent-list">
                            <li
                                v-for="(item, index) in vData.chatList"
                                :key="item.liaison_account_id"
                                :class="[vData.currentChat && vData.currentChat.liaison_account_id === item.liaison_account_id ? 'active' : '']"
                                @click="checkMsg(item, index)"
                            >
                                <strong class="nickname f14 mr5">{{ item.liaison_account_name }}</strong>
                                <span class="member-name f12">({{ item.liaison_member_name }})</span>
                                <p
                                    v-if="item.unread_num"
                                    class="unread"
                                >
                                    {{ item.unread_num }}
                                </p>
                                <el-icon
                                    class="el-icon-delete"
                                    @click.stop="deleteLastAccount(item, index)"
                                >
                                    <elicon-delete />
                                </el-icon>
                            </li>
                        </ul>
                        <div
                            v-if="vData.chatList.length === 0"
                            class="p10"
                        >
                            <EmptyData />
                        </div>
                    </el-tab-pane>
                    <el-tab-pane
                        name="account"
                        label="通讯录"
                    >
                        <ChatMembers
                            ref="accountList"
                            @start-chat="startChat"
                        />
                    </el-tab-pane>
                </el-tabs>
            </div>
            <div
                v-show="vData.tabName === 'recent'"
                class="chat-range"
            >
                <el-alert
                    v-if="vData.wsServerState"
                    class="ws-error-status"
                    :title="vData.wsServerState === 1 ? '服务已断开, 是否重连?': (vData.wsServerState === 2 ? '正在重连...' : '')"
                    :closable="false"
                    type="error"
                >
                    <template v-if="vData.wsServerState === 1">
                        <el-button
                            type="text"
                            class="el-button-danger"
                            @click="restartWs(false)"
                        >
                            否
                        </el-button>
                        <el-button
                            type="text"
                            class="el-button-danger"
                            @click="restartWs(true)"
                        >
                            是
                        </el-button>
                    </template>
                </el-alert>
                <template v-if="vData.currentChat">
                    <div class="user-info mb10">
                        <strong class="f14">
                            {{ vData.currentChat.liaison_account_name }} <span class="f12"> ({{ vData.currentChat.liaison_member_name }})</span>
                        </strong>
                    </div>
                    <!-- chat messages -->
                    <ChatLog
                        ref="chatLog"
                        :current-chat="vData.currentChat"
                        :max-height="vData.maxHeight"
                    />
                    <div
                        class="chat-msg pt10 mt10"
                        @mousedown.stop
                    >
                        <!-- <div class="chat-tools">
                            i.icon
                        </div> -->
                        <el-input
                            ref="input"
                            v-model="vData.isay"
                            type="textarea"
                            placeholder="回车发送消息, shift+回车换行"
                            @keydown="sendMsg"
                        />
                    </div>
                </template>
                <div
                    v-else
                    class="chat-empty"
                >
                    点击左侧联系人发起聊天 ^-^
                </div>
            </div>
            <div
                v-show="vData.tabName === 'account'"
                class="chat-range"
            >
                <div class="chat-empty">
                    点击左侧联系人发起聊天 ^-^
                </div>
            </div>
        </div>
    </VueDragResize>
</template>

<script>
    import {
        ref,
        reactive,
        computed,
        getCurrentInstance,
        onMounted,
        nextTick,
    } from 'vue';
    import { useStore } from 'vuex';
    // import DBUtil from '@src/utils/dbUtil';
    import ChatMembers from './ChatMembers';
    import ChatLog from './ChatLog';

    export default {
        components: {
            ChatLog,
            ChatMembers,
        },
        emits: ['ws-restart', 'has-new-message'],
        props: {
            ws: Object,
        },
        setup(props, context) {
            const store = useStore();
            const userInfo = computed(() => store.state.base.userInfo);
            const { appContext } = getCurrentInstance();
            const { $bus, $http, $message } = appContext.config.globalProperties;
            const sysChat = ref();
            const accountList = ref();
            const chatLog = ref();
            const input = ref();
            const vData = reactive({
                dom:           null,
                loading:       false,
                tabName:       'account',
                currentChat:   null,
                maxHeight:     300,
                wsServerState: 0, // 0: normal 1: disconnected 2: retry
                chatList:      [],
                isay:          '',
                controlPoints: [
                    {
                        'ctrl-top': {
                            direction: 'horzantical',
                            icon:      'icon-horzantical',
                        },
                    },
                ],
                account: {},
            });

            // hide chat room
            const hide = () => {
                vData.dom.classList.add('hide');
                vData.dom.classList.remove('show');
                window.localStorage.removeItem(`${window.api.baseUrl}_chat`);
            };

            // show chat room
            const show = () => {
                vData.dom.classList.remove('show');
                setTimeout(() => {
                    sysChat.value.methods.init();
                    setTimeout(() => {
                        vData.dom.classList.remove('hide');
                        vData.dom.classList.add('show');
                        $message.success('聊天服务已就绪');
                        $bus.$emit('has-new-message', 0);
                    }, 300);
                }, 300);
            };

            // drag chat room
            const dragstart = () => {
                vData.dom.classList.add('no-delay');
            };
            const dragend = () => {
                vData.dom.classList.remove('no-delay');
            };
            const min = ({ rect }) => {
                rect.top = 100;
                rect.left = window.innerWidth - 560;
            };
            const getLastChatAccount = async (opt = {
                requestFromRefresh: false,
            }) => {
                const { code, data } = await $http.get({
                    url:    '/chat/chat_last_account',
                    params: {
                        accountId:              userInfo.value.id,
                        'request-from-refresh': opt.requestFromRefresh,
                    },
                });

                vData.loading = false;
                if(code === 0) {
                    let unread_num = 0;

                    vData.chatList = data.list.map(row => {
                        unread_num += row.unread_num;
                        return row;
                    });

                    if(unread_num) {
                        $bus.$emit('has-new-message', unread_num);
                    }
                }
            };
            // init chart connection
            const initConnections = async () => {
                const userInfoLocalStorage = window.localStorage.getItem(`${window.api.baseUrl}_userInfo`);

                if(!userInfoLocalStorage) return;

                await getLastChatAccount({ requestFromRefresh: true });

                setTimeout(() => {
                    // every 10s
                    initConnections();
                }, 10 * 10e2);
            };

            const tabChange = (ref) => {
                const callback = {
                    account() {
                        accountList.value.getContacts();
                    },
                    recent() {
                        initConnections();
                    },
                };

                callback[ref.paneName]();
            };
            const restartWs = (state) => {
                if(state) {
                    vData.loading = false;
                    vData.wsServerState = 2;
                    context.emit('ws-restart');
                } else {
                    vData.wsServerState = 0;
                }
            };
            // show current message
            const checkMsg = async (item, index) => {
                if(chatLog.value) {
                    const lastTalkId = chatLog.value.currentChat ? chatLog.value.currentChat.liaison_account_id : '';

                    if(lastTalkId !== item.liaison_account_id) {
                        // changed the current chat person
                        chatLog.value.vData.last_messages = [];
                    } else {
                        // click again to the current person should not happen something
                        return false;
                    }
                }

                vData.currentChat = {
                    ...item,
                };

                // clear unread number
                methods.clearUnread(vData.currentChat, index);

                nextTick(async _ => {
                    // pull new messages
                    await chatLog.value.getRecentLog();
                    chatLog.value.scrollToBottom();
                });
            };

            // start to chat
            const startChat = (account) => {
                // cache last talk person
                const lastPersonId = vData.currentChat ? vData.currentChat.liaison_account_id : '';
                const current = vData.chatList.findIndex(item => item.liaison_account_id === account.to_account_id);
                const content = {
                    id:                   '',
                    status:               4, // 0：readed 1：unread、2：send success、3：send failed、4：readed by the other side
                    unread_num:           0,
                    account_name:         account.nickname,
                    member_name:          account.to_member_name,
                    liaison_account_name: account.to_account_name,
                    liaison_member_name:  account.to_member_name,
                    liaison_account_id:   account.to_account_id,
                    liaison_member_id:    account.to_member_id,
                };

                if(~current) {
                    // top the person
                    vData.chatList.splice(current, 1);
                    // TODO: merge chat list and cahce
                    vData.chatList.unshift(content);
                } else {
                    vData.chatList.unshift(content);
                    // add to the chat list
                    methods.addLastAccount(content);

                }
                vData.tabName = 'recent';
                vData.currentChat = vData.chatList[0];

                // clear unread number
                methods.clearUnread(vData.currentChat, current);

                nextTick(async _ => {
                    if(chatLog.value) {
                        // change person clear messages
                        if(lastPersonId !== account.to_account_id) {
                            chatLog.value.vData.last_messages = [];
                            // pull new messages
                            await chatLog.value.getRecentLog();
                        }

                        chatLog.value.scrollToBottom();
                    }
                });
            };
            // remove from last account list
            const deleteLastAccount = async (account, index) => {
                const { code } = await $http.post({
                    url:  '/chat/delete_chat_last_account',
                    data: {
                        accountId:        userInfo.value.id,
                        liaisonAccountId: account.liaison_account_id,
                    },
                });

                if(code === 0) {
                    vData.currentChat = null;
                    vData.chatList.splice(index, 1);
                    if(vData.chatList.length === 0) {
                        vData.tabName = 'account';
                    }
                    // TODO: delete from the local cache
                }
            };

            // send message
            const sendMsg = async (e) => {
                const lastmsg = vData.isay;

                /* if(e.shiftKey) {
                    // text wrap
                } */
                if(e.keyCode === 13 && !e.shiftKey){
                    setTimeout(_ => {
                        if(!props.ws) {
                            return vData.wsServerState = 1;
                        }

                        const isaid = vData.isay.trim();

                        // lastmsg.trim() !== isaid text is rendered to screen
                        // send message after rendered to screen
                        if(lastmsg.trim() === isaid) {
                            const msg = {
                                messageId:       Date.now(),
                                from_account_id: userInfo.value.id,
                                toAccountId:     vData.currentChat.liaison_account_id,
                                toAccountName:   vData.currentChat.liaison_account_name,
                                toMemberId:      vData.currentChat.liaison_member_id,
                                toMemberName:    vData.currentChat.liaison_member_name,
                                content:         isaid,
                            };

                            if(!isaid) return;
                            vData.isay = '';
                            input.value.blur();

                            setTimeout(() => {
                                input.value.focus();
                                chatLog.value.pushMsg(msg);
                            });

                            // send with ws
                            props.ws.send(JSON.stringify(msg));

                            getLastChatAccount();
                        }
                    }, 33);
                }
            };
            // socket open
            const socketOnOpen = (event) => {
                setTimeout(() => {
                    vData.wsServerState = 0;
                }, 300);
            };
            // socket message
            const socketOnMessage = (event) => {
                const { code, data, type, message } = JSON.parse(event.data || {});

                if(code !== 0) {
                    $message.error(message || '发生错误!');
                } else if(type === 'chat') {
                    if(data.messageId) {
                        // send success message id
                    } else {
                        const { from_account_id } = data.message;

                        if(vData.currentChat && from_account_id === vData.currentChat.liaison_account_id) {
                            // message source === current person, push message
                            chatLog.value.pushMsg(data.message);
                        } else {
                            // 1. push unread number to last account list
                            // 2. // TODO cache to local
                            const index = vData.chatList.findIndex(row => row.liaison_account_id === from_account_id);

                            if(~index) {
                                // top the person if message source is the one in the list
                                const row = vData.chatList[index];

                                row.unread_num++;

                                vData.chatList.splice(index, 1);
                                vData.chatList.unshift(row);
                                // tell unread number to server
                                methods.unreadIncrease(row);
                            } else {
                                // add to new account list
                                const content = {
                                    id:                   '',
                                    status:               4, // 0：readed 1：unread、2：send success、3：send failed、4：readed by the other side
                                    unread_num:           1,
                                    account_name:         userInfo.value.nickname,
                                    member_name:          userInfo.value.member_name,
                                    liaison_account_name: data.message.from_account_name,
                                    liaison_member_name:  data.message.from_member_name,
                                    liaison_account_id:   from_account_id,
                                    liaison_member_id:    data.message.from_member_id,
                                };

                                vData.chatList.unshift(content);
                            }
                        }
                    }
                }
            };
            // socket close
            const socketOnClose = (event) => {
                setTimeout(() => {
                    vData.wsServerState = 1;
                }, 500);
            };
            const methods = {
                async unreadIncrease(member) {
                    await $http.post({
                        url:  '/chat/unread_message_increase_one',
                        data: {
                            toMemberId:    userInfo.value.member_id,
                            toAccountId:   userInfo.value.id,
                            fromAccountId: member.liaison_account_id,
                            fromMemberId:  member.liaison_member_id,
                        },
                    });
                },

                /* // open indexDB
                async openDB(accountId) {
                    return new DBUtil({
                        dbname:  'wefe-chat',
                        table:   accountId,
                        keyPath: 'time',
                    });
                }, */

                // clear unread number
                async clearUnread(item, index) {
                    if(~index) {
                        const { code } = await $http.post({
                            url:  '/chat/update_to_read',
                            data: {
                                fromAccountId: userInfo.value.id,
                                toAccountId:   item.liaison_account_id,
                                chatListType:  'chat',
                            },
                        });

                        if (code === 0) {
                            let unread_num = 0;

                            vData.chatList[index].unread_num = 0;

                            vData.chatList.forEach(row => {
                                unread_num += row.unread_num;
                            });

                            $bus.$emit('has-new-message', unread_num);
                        }
                    }
                },

                // add to new account list
                async addLastAccount(account) {
                    await $http.post({
                        url:  '/chat/add_chat_last_account',
                        data: {
                            accountId:          userInfo.value.id,
                            accountName:        userInfo.value.nickname,
                            memberId:           userInfo.value.member_id,
                            memberName:         userInfo.value.member_name,
                            liaisonAccountId:   account.liaison_account_id,
                            liaisonAccountName: account.liaison_account_name,
                            liaisonMemberId:    account.liaison_member_id,
                            liaisonMemberName:  account.member_name,
                        },
                    });
                },
            };

            onMounted(async () => {
                vData.loading = true;
                vData.dom = sysChat.value.$el;
                await initConnections();
                nextTick(_ => {
                    if(vData.chatList.length) {
                        vData.tabName = 'recent';
                    }
                });
            });

            return {
                vData,
                userInfo,
                sysChat,
                accountList,
                chatLog,
                input,
                hide,
                show,
                dragstart,
                dragend,
                min,
                tabChange,
                restartWs,
                checkMsg,
                startChat,
                deleteLastAccount,
                sendMsg,
                socketOnOpen,
                socketOnMessage,
                socketOnClose,
            };
        },
    };
</script>

<style lang="scss" scoped>
    .sys-chat-panel{
        top: 100px;
        left:100%;
        z-index:10e2;
        border-radius: 5px;
        transition-duration: .3s;
        transform: translateX(0);
        background:#fbfbfb;
        border:2px solid #e6e6e6;
        &.no-delay{transition-duration:0s;}
        &.show{transform: translateX(-110%);}
        &.max{
            :deep(.control-points){display: none;}
        }
        &.hide{
            top: 100px !important;
            left:100% !important;
        }
        :deep(.drag-content){
            top: 25px;
            left: 20px;
            right:0;
            bottom:0;
            width:auto;
            height:auto;
        }
    }
    .sys-chat{
        cursor: default;
        display: flex;
        height: 100%;
        padding-bottom: 20px;
    }
    .sys-btns{
        position: absolute;
        right:25px;
        top: 25px;
        z-index:10;
        height: 24px;
        font-size: 12px;
        color: #fff;
        .icon{
            width: 16px;
            height: 16px;
            line-height: 16px;
            font-style: normal;
            display: inline-block;
            vertical-align: top;
            text-align: center;
            border-radius: 50%;
            font-size: 0;
            &:before{display: inline-block;}
            &:hover{
                font-size:12px;
                cursor: pointer;
            }
        }
        .el-icon-minus{background: #f1b92a;}
        .el-icon-plus{background: #35c895;}
        .el-icon-close{background: #f85564;}
    }
    .chat-tab{
        width: 200px;
        .el-tabs{height:100%;}
        .el-tab-pane{
            height: 100%;
            overflow: auto;
        }
        :deep(.el-tabs__item){
            height:30px;
            line-height:30px;
        }
        :deep(.el-tabs__header){margin:0;}
        :deep(.el-tabs__content){height:calc(100% - 30px);}
    }
    .user-list{
        line-height: 16px;
        background: none;
        li{position: relative;
            cursor:default;
            padding: 10px 30px 10px 10px;
            word-break: break-all;
            display: flex;
            &:hover{
                background: #f1f5fe;
                color: $--color-primary;
                .el-icon-delete{display:block;}
                .unread{display:none;}
            }
            &.active{
                background: #538bf9;
                color: #fff;
            }
        }
        .unread{
            font-size:12px;
            position: absolute;
            right: 10px;
            top: 0;
            height: 20px;
            line-height:1;
            padding: 5px;
            min-width: 20px;
            text-align: center;
            border-radius: 20px;
            background: #f85564;
            color:#fff;
        }
        .el-icon-delete{
            position: absolute;
            right: 11px;
            bottom: 10px;
            top: 50%;
            margin-top: -10px;
            cursor: pointer;
            display:none;
        }
    }
    .chat-range{
        padding:0 10px;
        position: relative;
        border-left: 1px solid #ebebeb;
        flex: 1;
    }
    .ws-error-status{
        position: absolute;
        left: 10px;
        right: 10px;
        top: 35px;
        z-index:2;
        width: auto;
        :deep(.el-alert__content){
            width: 100%;
            display: flex;
            justify-content: space-between;
            align-items: center;
        }
        :deep(.el-alert__description){
            padding-right: 15px;
            margin:0;
        }
    }
    .el-button-danger{
        color:#F85564;
        padding: 0;
        border:0;
    }
    .user-info{
        cursor:default;
        padding: 10px 10px 6px;
        border-bottom: 1px solid #ebebeb;
    }
    .user-name{
        font-weight: bold;
        font-size: 16px;
    }
    .chat-empty{
        height: 90%;
        display: flex;
        font-size: 14px;
        justify-content: center;
        align-items: center;
    }
    .chat-msg{border-top: 1px solid #ebebeb;}
    .member-info{
        .name{
            font-weight: bold;
            padding-bottom: 10px;
            margin-bottom: 10px;
            border-bottom: 1px solid #ebebeb;
        }
        p{line-height:30px;}
    }
</style>
