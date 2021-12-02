<template>
    <div
        ref="chatLog"
        class="chat-log f14"
    >
        <ul
            ref="scrollContainer"
            :class="['scroll-container', { top: vData.scrollType === 'top', bottom: vData.scrollType === 'bottom' }]"
            :style="{
                webkitTransform: `translateY(${vData.offsetY}px)`,
                mozTransform: `translateY(${vData.offsetY}px)`,
                msTransform: `translateY(${vData.offsetY}px)`,
                transform: `translateY(${vData.offsetY}px)`,
            }"
        >
            <li
                v-for="(msg, index) in vData.last_messages"
                :key="`${msg.to_account_id}_${msg.id}_${index}`"
                :class="['mb10', msg.from_account_id === userInfo.id ? 'isaid' : '']"
            >
                <div :class="['msg-time f12', { show: index % 7 === 0 }]">{{ dateFormat(msg.created_time || msg.messageId) }}</div>

                <el-popover
                    v-if="msg.from_account_id === userInfo.id && msg.status === 3"
                    width="100"
                    trigger="hover"
                    placement="top-start"
                    content="发送失败! 点击图标进行重发"
                >
                    <template #reference>
                        <i
                            class="send-state el-icon-warning"
                            @click="resend(msg, index)"
                        />
                    </template>
                </el-popover>

                <!-- message -->
                <span :class="[msg.from_account_id === userInfo.id ? 'isay' : 'they']">{{ msg.content }}</span>
            </li>
        </ul>
    </div>
</template>

<script>
    import {
        ref,
        computed,
        reactive,
        nextTick,
        getCurrentInstance,
        onMounted,
        onBeforeUnmount,
    } from 'vue';
    import { useStore } from 'vuex';
    // import DBUtil from '@src/utils/dbUtil';

    let timer = null;

    export default {
        props: {
            currentChat: Object,
        },
        setup(props) {
            const store = useStore();
            const userInfo = computed(() => store.state.base.userInfo);
            const { appContext } = getCurrentInstance();
            const { $http } = appContext.config.globalProperties;
            const scrollContainer = ref();
            const chatLog = ref();
            const vData = reactive({
                loading:       false,
                locker:        false,
                isLastMsg:     false, // is last one
                scrollType:    '', // show tips for top & bottom
                last_messages: [],
                page_index:    -1,
                page_size:     10,
                offsetY:       0,
            });

            // pull chart messages
            const getRecentLog = async (limitCreateTime) => {
                const { code, data } = await $http.get({
                    url:    '/chat/detail',
                    params: {
                        fromAccountId: userInfo.value.id,
                        toAccountId:   props.currentChat.liaison_account_id,
                        page_size:     vData.page_size,
                        limitCreateTime,
                    },
                });

                setTimeout(() => {
                    vData.loading = false;
                }, 500);

                nextTick(() => {
                    if(code === 0  && data.list) {
                        let { length } = data.list;

                        vData.isLastMsg = false;
                        if(length === 0) {
                            // last page
                            vData.isLastMsg = true;
                            return false;
                        } else {
                            // Reverse order
                            while(length) {
                                vData.last_messages.unshift(data.list.shift());
                                length--;
                            }
                            vData.page_index++;
                        }
                    }
                });
            };
            // scroll to bottom
            const scrollToBottom = () => {
                nextTick(_ => {
                    vData.offsetY =  0;
                });
            };
            const pushMsg = (msg) => {
                vData.last_messages.push(msg);
                nextTick(_ => {
                    scrollToBottom();
                });
            };

            const methods = {
                /* The bottom of the scrolling area shall prevail, up: translateY < 0, down: translateY > 0 */
                mousewheel(e) {
                    e.stopPropagation();
                    e.preventDefault();

                    const { deltaY } = e; // < 0 up  > 0 down
                    const loadThreshold = 50; // Advance loading distance
                    const { offsetHeight } = chatLog.value;
                    const { scrollHeight } = scrollContainer.value;
                    const maxScroll = scrollHeight - offsetHeight;

                    vData.offsetY -= deltaY;

                    if(deltaY < 0 && vData.offsetY > maxScroll - loadThreshold){
                        // to the top
                        vData.offsetY = maxScroll;

                        if(vData.isLastMsg) {
                            vData.offsetY += (maxScroll + 0.5);
                            if(vData.offsetY >= maxScroll + 30) {
                                vData.offsetY = maxScroll + 30;
                            }
                            vData.scrollType = 'top';
                            clearTimeout(timer);
                            timer = setTimeout(() => {
                                vData.offsetY = maxScroll;
                                setTimeout(() => {
                                    vData.scrollType = '';
                                }, 100);
                            }, 33);
                            return;
                        }
                        // load next page
                        if(vData.loading) return;
                        vData.loading = true;
                        getRecentLog(vData.last_messages[0].created_time);
                    } else if(vData.offsetY <= 0) {
                        // to the bottom
                        vData.offsetY -= 0.5;
                        if(vData.offsetY <= 30) {
                            vData.offsetY = -30;
                        }
                        vData.scrollType = 'bottom';
                        clearTimeout(timer);
                        timer = setTimeout(() => {
                            vData.offsetY = 0;
                            setTimeout(() => {
                                vData.scrollType = '';
                            }, 100);
                        }, 33);
                    }
                },

                // load log messages from cache
                /* async loadCache() {
                    const db = new DBUtil({
                        dbname:  'wefe-chat',
                        table:   props.currentChat.liaison_account_id,
                        keyPath: 'time',
                    });

                    console.log(db);
                }, */

                // resend message
                async resend(msg, index) {
                    if(vData.locker) return;
                    const { code, data } = await $http.post({
                        url:  msg.id ? '/chat/resend_message' : '/chat/send_message',
                        data: {
                            member_chat_id: msg.id,
                            toMemberName:   props.currentChat.liaison_member_name,
                            toAccountName:  props.currentChat.liaison_account_name,
                            ...msg,
                        },
                    });

                    nextTick(() => {
                        vData.locker = false;
                        if(code === 0) {
                            vData.last_messages[index].status = 1;
                            if(!msg.id) {
                                // does not exist in server
                                vData.last_messages[index].id = data.id;
                            }
                        }
                    });
                },
            };

            onMounted(() => {
                nextTick(() => {
                    chatLog.value.addEventListener('mousewheel', methods.mousewheel.bind(this));
                });
            });

            onBeforeUnmount(() => {
                chatLog.value.removeEventListener('mousewheel', methods.scroll);
            });

            return {
                vData,
                userInfo,
                scrollToBottom,
                scrollContainer,
                getRecentLog,
                pushMsg,
                chatLog,
            };
        },
    };
</script>

<style lang="scss" scoped>
    .chat-log{
        overflow: hidden;
        position: relative;
        word-break: break-word;
        height: calc(100% - 120px);
        .isaid{text-align: right;}
        .they,
        .isay{
            display: inline-block;
            vertical-align: top;
            border-radius: 6px;
            padding:5px 10px;
            max-width: 85%;
        }
        .they{background: #eee;}
        .isay{background: #c7e5fe;}
    }
    .msg-time{
        visibility: hidden;
        &.show{visibility:visible;}
    }
    li:hover{
        .msg-time{
            visibility: visible;
        }
    }
    .send-state{
        cursor: pointer;
        color: $--color-danger;
        display: inline-block;
        margin: 10px 5px 0 0;
    }
    .scroll-container{
        position: absolute;
        left:0;
        bottom:0;
        width: 100%;
        min-height:100%;
        transition-duration: 0;
        &.top, &.bottom{transition-duration: 0.5s;}
    }
</style>
