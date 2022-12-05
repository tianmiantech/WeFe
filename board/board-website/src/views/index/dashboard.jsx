import { defineComponent } from 'vue';
import { ElCol, ElRow } from 'element-plus';
import MessageList from './components/message-list.vue';
import ServiceStatus from './components/service-available-list.vue';

export default defineComponent({
    setup() {
        return () => (
            <div class="dashBoardPage">
                <ElRow gutter={20}>
                    <ElCol span={12}>
                        <ServiceStatus />
                    </ElCol>
                    <ElCol span={12}>
                        <MessageList />
                    </ElCol>
                </ElRow>
            </div>
        );
    },
});
