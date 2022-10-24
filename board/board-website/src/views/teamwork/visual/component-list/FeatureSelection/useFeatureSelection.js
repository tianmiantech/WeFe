import { reactive, getCurrentInstance, nextTick, ref, computed } from 'vue';

export default (props, context) => {
    const { appContext } = getCurrentInstance();
    const { $http, $message } = appContext.config.globalProperties;
    const data = reactive({
        total: 0,
        features: [],
        showDialog: false,
    });
    const list = computed(() => features.filter(({ id }) => id))

    const show = ({ features }) => {
        data.features = features;
        data.total = features.length;
        data.showDialog = true;
    };
    const filter = async (e) => {
        const { code, data, message } = await $http.post({
            url: '/flow/job/task/select',
            data: {
                job_id: props.jobId,
                flow_id: props.flowId,
                flow_node_id: props.flowNodeId,
                members: data.features,
                ...data.params,
            },
            btnState: {
                target: e,
            },
        });

        nextTick(() => {
            if (code === 0) {
                if (data.featureNum) {
                    data.list = data.members.map((row) => {
                        // add id mark
                        row.id = true;
                        return row;
                    });
                } else {
                    data.list = [];
                    $message.error('没有符合条件的特征');
                }
            } else {
                message && $message.error(message);
            }
        });
    };
    const confirm = () => {
        data.showDialog = false;
        context.emit('confirmCheck', { list: data.list });
    };
    return { confirm, filter, show, list };
};
